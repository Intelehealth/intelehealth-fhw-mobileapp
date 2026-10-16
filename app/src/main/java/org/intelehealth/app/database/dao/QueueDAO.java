package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.QueueDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueDAO extends BaseDao{

    private static final String TAG = "QueueListDAO";

    /**
     * Ticks (with the write time) each time a queue insert has actually been written. The insert runs
     * on its own background thread, so the pull-sync "done" broadcast can arrive before the rows
     * land; screens observe this instead to reload once the data is really there.
     */
    private static final MutableLiveData<Long> QUEUE_CHANGED = new MutableLiveData<>();

    public static LiveData<Long> getQueueChanged() {
        return QUEUE_CHANGED;
    }

    /**
     * Whether the visit has an entry in the last pulled queue snapshot, i.e. the server has already
     * queued it. Synchronous; call off the main thread. False on any error.
     */
    public boolean isVisitInQueue(String visitUuid) {
        if (visitUuid == null || visitUuid.isEmpty()) {
            return false;
        }
        return read(db -> {
            Cursor cursor = db.rawQuery("SELECT 1 FROM " + tableName()
                    + " WHERE visitUuid = ? COLLATE NOCASE LIMIT 1", new String[]{visitUuid});
            try {
                return cursor.moveToFirst();
            } finally {
                cursor.close();
            }
        }, false);
    }

    /** Removes every queue row (logout: the queue belongs to the signed-in health worker). */
    public void deleteAll() {
        read(db -> db.delete(tableName(), null, null), 0);
        QUEUE_CHANGED.postValue(System.currentTimeMillis());
    }

    /**
     * Replaces tbl_queue with the queue snapshot from a pull response. The pull's {@code queueData}
     * is the full current queue (it is sent on every pull, even when nothing else changed), so
     * entries the server dropped - completed, cancelled, removed - must disappear locally too;
     * an upsert alone would keep them forever with their last position.
     * <p>
     * {@code null} (no queueData in this response) leaves the table untouched; an empty list means
     * the queue is now empty and clears it. Delete + insert run in one transaction so the list
     * never reads a half-written table. Runs in the background, then signals
     * {@link #getQueueChanged()} observers.
     */
    public void replaceQueue(@Nullable List<QueueDTO> queueDTOS) {
        if (queueDTOS == null) {
            return;
        }
        List<ContentValues> rows = new ArrayList<>(queueDTOS.size());
        for (QueueDTO queueDTO : queueDTOS) {
            rows.add(toContentValues(createQueueMap(queueDTO)));
        }
        executeInBackground(() -> {
            // Logout clears tbl_queue but also starts a background sync whose pull would refill
            // it; checked at write time so a pull finishing after logout is dropped.
            if (new SessionManager(IntelehealthApplication.getAppContext()).isLogout()) {
                return;
            }
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
            db.beginTransaction();
            try {
                db.delete(tableName(), null, null);
                for (ContentValues row : rows) {
                    db.insertWithOnConflict(tableName(), null, row, SQLiteDatabase.CONFLICT_REPLACE);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                CustomLog.d(TAG, "replaceQueue failed: " + e.getLocalizedMessage());
                return;
            } finally {
                db.endTransaction();
            }
            QUEUE_CHANGED.postValue(System.currentTimeMillis());
        });
    }

    private static ContentValues toContentValues(HashMap<String, Object> map) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                values.putNull(entry.getKey());
            } else if (value instanceof Integer) {
                values.put(entry.getKey(), (Integer) value);
            } else if (value instanceof Long) {
                values.put(entry.getKey(), (Long) value);
            } else if (value instanceof Double) {
                values.put(entry.getKey(), (Double) value);
            } else if (value instanceof Float) {
                values.put(entry.getKey(), (Float) value);
            } else if (value instanceof Boolean) {
                values.put(entry.getKey(), (Boolean) value);
            } else {
                values.put(entry.getKey(), value.toString());
            }
        }
        return values;
    }



    @Override
    String tableName() {
        return "tbl_queue";
    }

    public HashMap<String, Object> createQueueMap(QueueDTO queueDTO) {
        HashMap<String, Object> values = new HashMap<>();

        values.put("queueEntryId", queueDTO.getQueueEntryId());
        values.put("visitUuid", queueDTO.getVisitUuid());
        values.put("speciality", queueDTO.getSpeciality());
        values.put("status", queueDTO.getStatus());
        values.put("emergencyLevel", queueDTO.getEmergencyLevel());
        values.put("caseType", queueDTO.getCaseType());
        values.put("escalated", queueDTO.isEscalated() ? 1 : 0);
        values.put("position", queueDTO.getPosition());
        values.put("etaMinutes", queueDTO.getEtaMinutes());
        values.put("etaAt", resolveEtaAt(queueDTO.getEtaAt(), queueDTO.getQueuedAt(), queueDTO.getEtaMinutes()));
        values.put("etaModelUsed", queueDTO.getEtaModelUsed());
        values.put("assignedDoctorUuid", queueDTO.getAssignedDoctorUuid());
        values.put("queuedAt", queueDTO.getQueuedAt());
        values.put("assignedAt", queueDTO.getAssignedAt());
        values.put("connectedAt", queueDTO.getConnectedAt());
        values.put("completedAt", queueDTO.getCompletedAt());
        values.put("requeueCount", queueDTO.getRequeueCount());
        values.put("heartbeatFlagged", queueDTO.isHeartbeatFlagged() ? 1 : 0);
        values.put("hwUserUuid", queueDTO.getHwUserUuid());
        values.put("patientUuid", queueDTO.getPatientUuid());
        values.put("locationUuid", queueDTO.getLocationUuid());
        values.put("flagged", queueDTO.isFlagged() ? 1 : 0);
        values.put("escalatedAt", queueDTO.getEscalatedAt());
        // chiefComplaint is not stored: it's resolved from the visit's obs
        // (EncounterDAO.getChiefComplaint) when the queue list is read.
        // TODO: proper vitals parsing handled later; store raw JSON string for now (null-safe)
        values.put("vitals", queueDTO.getVitals() != null ? queueDTO.getVitals().toString() : null);
        values.put("waitedMinutes", queueDTO.getWaitedMinutes());
        values.put("priorityScore", queueDTO.getPriorityScore());
        return values;
    }

    /**
     * The ETA instant to store for a queue row: the server's {@code etaAt} when
     * sent, otherwise {@code queuedAt + etaMinutes} (both ISO-8601 UTC). Without
     * an instant the queue list can't tick the wait time or detect "Overdue".
     * Null when neither is usable.
     */
    @Nullable
    private static String resolveEtaAt(@Nullable String etaAt, @Nullable String queuedAt, int etaMinutes) {
        if (etaAt != null && !etaAt.trim().isEmpty()) {
            return etaAt;
        }
        if (queuedAt == null || queuedAt.trim().isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(queuedAt.trim()).plusSeconds(etaMinutes * 60L).toString();
        } catch (Exception e) {
            CustomLog.d(TAG, "resolveEtaAt: " + e.getLocalizedMessage());
            return null;
        }
    }


    /**
     * Single query that joins the locally-synced queue rows with the patient
     * display fields (openmrs_id, name, gender, date_of_birth) they need, so
     * the screen is built from ONE DB round-trip instead of a queue read
     * followed by a per-row patient lookup (N+1).
     *
     * <p>Each joined row is handed to {@code mapper} during the single cursor
     * pass, so the caller builds its final row objects inline — there is no
     * intermediate list and no second loop. Runs a synchronous query, so call
     * it off the main thread. Never throws — on any error it returns whatever
     * was mapped so far (possibly empty) so the caller can still render.
     *
     * <p>Selected patient columns: {@code openmrs_id, first_name, middle_name,
     * last_name, gender, date_of_birth}. LEFT JOIN keeps queue rows whose
     * patient is missing locally (patient columns come back null).
     */
    public <T> ArrayList<T> getQueueWithPatient(int limit, int offset, QueueRowMapper<T> mapper) {
        if (mapper == null) {
            return new ArrayList<>();
        }
        return read(db -> queryQueueWithPatient(db, limit, offset, mapper), new ArrayList<>());
    }

    private <T> ArrayList<T> queryQueueWithPatient(SQLiteDatabase db, int limit, int offset,
                                                   QueueRowMapper<T> mapper) {
        ArrayList<T> rows = new ArrayList<>();
        /*String sql = "SELECT q.status AS status, q.position AS position, " +
                "q.chiefComplaint AS chiefComplaint, " +
                "q.waitedMinutes AS waitedMinutes, q.etaMinutes AS etaMinutes, " +
                "p.openmrs_id AS openmrs_id, p.first_name AS first_name, " +
                "p.middle_name AS middle_name, p.last_name AS last_name, " +
                "p.gender AS gender, p.date_of_birth AS date_of_birth " +
                "FROM " + tableName() + " q " +
                "LEFT JOIN tbl_patient p ON q.patientUuid = p.uuid COLLATE NOCASE " +
                "ORDER BY q.position ASC limit ? offset ?";*/
        // chiefComplaint is NOT read from tbl_queue (the queue service doesn't
        // send it); the visitUuid is carried through so the complaint can be
        // resolved from the visit's obs, exactly like the visit summary screen.
        String sql = queueWithPatientSelect() +
                "ORDER BY q.position ASC limit ? offset ?";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(limit), String.valueOf(offset)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    rows.add(mapper.map(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            CustomLog.d(TAG, "getQueueWithPatient: e " + e.getLocalizedMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return rows;
    }

    /**
     * Same joined row as {@link #getQueueWithPatient(int, int, QueueRowMapper)},
     * but for the single queue entry of {@code visitUuid} (e.g. the Visit
     * Summary queue banner). Synchronous — call it off the main thread. Returns
     * null when the visit has no queue row or on any error.
     */
    public <T> T getQueueWithPatientByVisit(String visitUuid, QueueRowMapper<T> mapper) {
        if (mapper == null || visitUuid == null || visitUuid.isEmpty()) {
            return null;
        }
        return read(db -> queryQueueWithPatientByVisit(db, visitUuid, mapper), null);
    }

    private <T> T queryQueueWithPatientByVisit(SQLiteDatabase db, String visitUuid,
                                               QueueRowMapper<T> mapper) {
        String sql = queueWithPatientSelect() +
                "WHERE q.visitUuid = ? COLLATE NOCASE limit 1";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{visitUuid});
            if (cursor != null && cursor.moveToFirst()) {
                return mapper.map(cursor);
            }
        } catch (Exception e) {
            CustomLog.d(TAG, "getQueueWithPatientByVisit: e " + e.getLocalizedMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Raw chief-complaint obs value for {@code visitUuid}. Same query and "en"
     * JSON handling as {@link EncounterDAO#getChiefComplaint(String)}, but
     * without its transaction.
     * Returns "" when absent or on error. Call off the main thread.
     */
    public String getChiefComplaint(String visitUuid) {
        if (visitUuid == null || visitUuid.isEmpty()) {
            return "";
        }
        String value = read(db -> queryChiefComplaint(db, visitUuid), "");
        if (value.startsWith("{") && value.endsWith("}")) {
            try {
                value = new JSONObject(value).getString("en");
            } catch (JSONException e) {
                CustomLog.d(TAG, "getChiefComplaint: json e " + e.getLocalizedMessage());
            }
        }
        return value;
    }

    private String queryChiefComplaint(SQLiteDatabase db, String visitUuid) {
        String sql = "select o.value from tbl_encounter e, tbl_obs o where " +
                "e.visituuid = ? " +
                "and e.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' " + // adult_initial
                "and e.uuid = o.encounteruuid and o.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'"; // chief complaint
        String value = "";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{visitUuid});
            // Last row wins, matching EncounterDAO.getChiefComplaint().
            while (cursor != null && cursor.moveToNext()) {
                String v = cursor.getString(0);
                value = v != null ? v : "";
            }
        } catch (Exception e) {
            CustomLog.d(TAG, "getChiefComplaint: e " + e.getLocalizedMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return value;
    }

    /** One read against the shared app connection. */
    private interface QueueRead<R> {
        R run(SQLiteDatabase db);
    }

    /**
     * Runs {@code read} on the shared app connection. Queue reads must stay on
     * this single connection: the DB isn't in WAL mode, so a read on a second
     * connection blocks the sync's write transactions and fails them with
     * SQLITE_BUSY ("database is locked"). Returns {@code fallback} on any error
     * so callers never throw.
     */
    private <R> R read(QueueRead<R> read, R fallback) {
        try {
            return read.run(IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb());
        } catch (Exception e) {
            CustomLog.d(TAG, "queue read failed: " + e.getLocalizedMessage());
            return fallback;
        }
    }

    /**
     * Shared SELECT + JOIN (queue -> visit -> patient) used by the queue list
     * and the per-visit lookup, so both map the exact same columns.
     */
    private String queueWithPatientSelect() {
        return "SELECT q.status AS status, q.position AS position, " +
                "q.visitUuid AS visitUuid, " +
                "q.waitedMinutes AS waitedMinutes, q.etaMinutes AS etaMinutes, " +
                "q.etaAt AS etaAt, q.connectedAt AS connectedAt, " +
                "p.openmrs_id AS openmrs_id, p.first_name AS first_name, " +
                "p.middle_name AS middle_name, p.last_name AS last_name, " +
                "p.gender AS gender, p.date_of_birth AS date_of_birth, " +
                "p.patient_photo AS patient_photo " +
                "FROM " + tableName() + " q " +
                "LEFT JOIN tbl_visit v ON q.visitUuid = v.uuid COLLATE NOCASE " +
                "LEFT JOIN tbl_patient p ON v.patientuuid = p.uuid COLLATE NOCASE ";
    }

    /**
     * Maps one joined {@code tbl_queue} + {@code tbl_patient} cursor row into
     * the caller's row type, invoked once per row during the single query pass.
     */
    public interface QueueRowMapper<T> {
        T map(Cursor cursor);
    }

}

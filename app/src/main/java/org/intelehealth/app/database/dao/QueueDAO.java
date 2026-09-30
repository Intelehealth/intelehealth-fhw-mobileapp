package org.intelehealth.app.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;

import androidx.annotation.Nullable;

import org.intelehealth.app.database.QueueReadDatabase;
import org.intelehealth.app.models.dto.QueueDTO;
import org.intelehealth.app.models.queue.QueueItem;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueueDAO extends BaseDao{

    private static final String TAG = "QueueListDAO";


    public boolean insertQueue(List<QueueDTO> queueDTOS) throws DAOException {
        boolean isInserted = true;
        if (queueDTOS != null && !queueDTOS.isEmpty()) {
            List<HashMap<String, Object>> queueLists = new ArrayList<>();
            for (QueueDTO queueDTO : queueDTOS) {
                queueLists.add(createQueueMap(queueDTO));
            }
            executeInBackground(bulkInsert(queueLists));
        }
        return isInserted;
    }


    /**
     * Same as {@link #insertQueue(List)} but for the {@link QueueItem} model
     * returned by the standalone {@code /api/queue/list} microservice. Kept as a
     * separate name because {@code insertQueue(List<QueueDTO>)} and
     * {@code insertQueue(List<QueueItem>)} would erase to the same signature.
     */
    public boolean insertQueueItems(List<QueueItem> queueItems) throws DAOException {
        boolean isInserted = true;
        List<HashMap<String, Object>> queueLists = new ArrayList<>();
        for (QueueItem queueItem : queueItems) {
            queueLists.add(createQueueMap(queueItem));
        }
        executeInBackground(bulkInsert(queueLists));
        return isInserted;
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

    public HashMap<String, Object> createQueueMap(QueueItem queueItem) {
        HashMap<String, Object> values = new HashMap<>();

        values.put("queueEntryId", queueItem.getQueueEntryId());
        values.put("visitUuid", queueItem.getVisitUuid());
        values.put("speciality", queueItem.getSpeciality());
        values.put("status", queueItem.getStatus());
        values.put("emergencyLevel", queueItem.getEmergencyLevel());
        values.put("caseType", queueItem.getCaseType());
        values.put("escalated", queueItem.isEscalated() ? 1 : 0);
        values.put("position", queueItem.getPosition());
        values.put("etaMinutes", queueItem.getEtaMinutes());
        values.put("etaAt", queueItem.getEtaAt());
        values.put("etaModelUsed", queueItem.getEtaModelUsed());
        values.put("assignedDoctorUuid", queueItem.getAssignedDoctorUuid());
        values.put("queuedAt", queueItem.getQueuedAt());
        values.put("assignedAt", queueItem.getAssignedAt());
        values.put("connectedAt", queueItem.getConnectedAt());
        values.put("completedAt", queueItem.getCompletedAt());
        values.put("requeueCount", queueItem.getRequeueCount());
        values.put("heartbeatFlagged", queueItem.isHeartbeatFlagged() ? 1 : 0);
        values.put("hwUserUuid", queueItem.getHwUserUuid());
        values.put("patientUuid", queueItem.getPatientUuid());
        values.put("locationUuid", queueItem.getLocationUuid());
        values.put("flagged", queueItem.isFlagged() ? 1 : 0);
        values.put("escalatedAt", queueItem.getEscalatedAt());
        // chiefComplaint is not stored: it's resolved from the visit's obs
        // (EncounterDAO.getChiefComplaint) when the queue list is read.
        // TODO: proper vitals parsing handled later; store raw JSON string for now (null-safe)
        values.put("vitals", queueItem.getVitals() != null ? queueItem.getVitals().toString() : null);
        values.put("waitedMinutes", queueItem.getWaitedMinutes());
        values.put("priorityScore", queueItem.getPriorityScore());
        return values;
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
        // Own read-only connection (see QueueReadDatabase) so the queue doesn't
        // wait behind other screens' queries on the shared app connection.
        return readWithRetry(db -> queryQueueWithPatient(db, limit, offset, mapper), new ArrayList<>());
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
        } catch (SQLiteDatabaseLockedException e) {
            throw e; // let readWithRetry() retry it
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
        return readWithRetry(db -> queryQueueWithPatientByVisit(db, visitUuid, mapper), null);
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
        } catch (SQLiteDatabaseLockedException e) {
            throw e; // let readWithRetry() retry it
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
     * Raw chief-complaint obs value for {@code visitUuid}, read on the queue's
     * own connection. Same query and "en" JSON handling as
     * {@link EncounterDAO#getChiefComplaint(String)}, which uses the shared
     * connection (and a transaction) and so waited behind other screens' reads.
     * Returns "" when absent or on error. Call off the main thread.
     */
    public String getChiefComplaint(String visitUuid) {
        if (visitUuid == null || visitUuid.isEmpty()) {
            return "";
        }
        String value = readWithRetry(db -> queryChiefComplaint(db, visitUuid), "");
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
        } catch (SQLiteDatabaseLockedException e) {
            throw e; // let readWithRetry() retry it
        } catch (Exception e) {
            CustomLog.d(TAG, "getChiefComplaint: e " + e.getLocalizedMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return value;
    }

    /** One read against the queue's read-only connection. */
    private interface QueueRead<R> {
        R run(SQLiteDatabase db);
    }

    /**
     * Runs {@code read} on {@link QueueReadDatabase}, retrying once if the DB is
     * briefly locked by a sync committing a write (the connection already waits
     * out Android's busy timeout before throwing). Returns {@code fallback} if it
     * still fails or the connection can't be opened, so callers never throw.
     */
    private <R> R readWithRetry(QueueRead<R> read, R fallback) {
        try {
            try {
                return read.run(QueueReadDatabase.get());
            } catch (SQLiteDatabaseLockedException e) {
                CustomLog.d(TAG, "queue read locked, retrying once: " + e.getLocalizedMessage());
                return read.run(QueueReadDatabase.get());
            }
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

package org.intelehealth.app.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.QueueDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueueDAO extends BaseDao{

    private static final String TAG = "QueueListDAO";


    public boolean insertQueue(List<QueueDTO> queueDTOS) throws DAOException {
        boolean isInserted = true;
        List<HashMap<String, Object>> queueLists = new ArrayList<>();
        for (QueueDTO queueDTO : queueDTOS) {
            queueLists.add(createQueueMap(queueDTO));
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
        values.put("chiefComplaint", queueDTO.getChiefComplaint());
        // TODO: proper vitals parsing handled later; store raw JSON string for now (null-safe)
        values.put("vitals", queueDTO.getVitals() != null ? queueDTO.getVitals().toString() : null);
        values.put("waitedMinutes", queueDTO.getWaitedMinutes());
        values.put("priorityScore", queueDTO.getPriorityScore());
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
        ArrayList<T> rows = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        if (db == null || !db.isOpen() || mapper == null) {
            return rows;
        }
        String sql = "SELECT q.position AS position, q.chiefComplaint AS chiefComplaint, " +
                "q.waitedMinutes AS waitedMinutes, q.etaMinutes AS etaMinutes, " +
                "p.openmrs_id AS openmrs_id, p.first_name AS first_name, " +
                "p.middle_name AS middle_name, p.last_name AS last_name, " +
                "p.gender AS gender, p.date_of_birth AS date_of_birth " +
                "FROM " + tableName() + " q " +
                "LEFT JOIN tbl_patient p ON q.patientUuid = p.uuid COLLATE NOCASE " +
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
     * Maps one joined {@code tbl_queue} + {@code tbl_patient} cursor row into
     * the caller's row type, invoked once per row during the single query pass.
     */
    public interface QueueRowMapper<T> {
        T map(Cursor cursor);
    }

}

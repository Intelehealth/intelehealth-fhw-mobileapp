package org.intelehealth.app.database.dao;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatientAttributesDaoNew extends BaseDao {
    private int updatecount = 0;
    private long createdRecordsCount = 0;
    int limit = 10, offset = 0;
    private static final String TAG = "PatientAttributesDaoNew";

    @Override
    String tableName() {
        return "tbl_patient_attribute";
    }
    public boolean patientAttributes(List<PatientAttributesDTO> patientAttributesDTOS) throws DAOException {
        boolean isInserted = true;
        List<HashMap<String, Object>> patientAttributesList = new ArrayList<>();
        for (PatientAttributesDTO patientAttributesDTO : patientAttributesDTOS) {
            patientAttributesList.add(createPatientAttributesMap(patientAttributesDTO));
        }
        executeInBackground(bulkInsert(patientAttributesList));
        /* Old code
        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < patientAttributesDTOS.size(); i++) {
                values.put("uuid", patientAttributesDTOS.get(i).getUuid());
                values.put("person_attribute_type_uuid", patientAttributesDTOS.get(i).getPersonAttributeTypeUuid());
                values.put("patientuuid", patientAttributesDTOS.get(i).getPatientuuid());
                values.put("value", patientAttributesDTOS.get(i).getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", "TRUE");
                db.insertWithOnConflict("tbl_patient_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }*/
        return isInserted;
    }
    public HashMap<String, Object> createPatientAttributesMap(PatientAttributesDTO patientAttributesDTO) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("uuid", patientAttributesDTO.getUuid());
        values.put("person_attribute_type_uuid", patientAttributesDTO.getPersonAttributeTypeUuid());
        values.put("patientuuid", patientAttributesDTO.getPatientuuid());
        values.put("value", patientAttributesDTO.getValue());
        values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
        values.put("sync",  "TRUE");
        return values;
    }
}

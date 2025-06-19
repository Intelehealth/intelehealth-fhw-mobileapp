package org.intelehealth.app.database.dao;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.dto.PatientAttributeTypeMasterDTO;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatientAttributesMasterDaoNew extends BaseDao {
    private int updatecount = 0;
    private long createdRecordsCount = 0;
    int limit = 10, offset = 0;
    private static final String TAG = "ibutesMasterDa";
    private String currentTableName;

    @Override
    String tableName() {
        return "tbl_patient_attribute_master";
    }
    public boolean patinetAttributeMaster(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTOS) throws DAOException {
        boolean isInserted = true;
        List<HashMap<String, Object>> patientAttributesList = new ArrayList<>();
        for (PatientAttributeTypeMasterDTO patientAttrs : patientAttributeTypeMasterDTOS) {
            patientAttributesList.add(createPatientMasterAttributesMap(patientAttrs));
        }
        executeInBackground(bulkInsert(patientAttributesList));

//        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
//        ContentValues values = new ContentValues();
//        db.beginTransaction();
//        try {
//            for (PatientDTO patient : patientDTO) {
//                createPatients(patient, db);
//            }
//            db.setTransactionSuccessful();
//        } catch (SQLException e) {
//            isInserted = false;
//            CustomLog.e(TAG,e.getMessage());
//            throw new DAOException(e.getMessage(), e);
//        } finally {
//            db.endTransaction();
//        }

        return isInserted;
    }
    public HashMap<String, Object> createPatientMasterAttributesMap(PatientAttributeTypeMasterDTO patientAttrDTO) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("uuid", patientAttrDTO.getUuid());
        values.put("name", patientAttrDTO.getName());
        values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
        values.put("sync", "TRUE");
        return values;
    }
}

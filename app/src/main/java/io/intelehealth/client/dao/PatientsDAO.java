package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.PatientAttributeTypeMasterDTO;
import io.intelehealth.client.dto.PatientAttributesDTO;
import io.intelehealth.client.dto.PatientDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.Logger;

public class PatientsDAO {


    private SQLiteDatabase db = null;
    private int updatecount = 0;
    private long createdRecordsCount = 0;

    public boolean insertPatients(List<PatientDTO> patientDTO) throws DAOException {

        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        try {
            for (PatientDTO patient : patientDTO) {
                Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_patient where uuid = ?", new String[]{patient.getUuid()});
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
//                        Logger.logD("update", "update has to happen");
                        updatePatients(patient);
                    }
                } else {
//                    Logger.logD("insert", "insert has to happen");
                    createPatients(patient);

                }
                AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
            }
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }

        return isInserted;
    }

    public boolean createPatients(PatientDTO patient) throws DAOException {
        boolean isCreated = true;

//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
//            for (PatientDTO patient : patientDTO) {
//                Logger.logD("create", "create has to happen");
            values.put("uuid", patient.getUuid());
            values.put("openmrs_id", patient.getOpenmrsId());
            values.put("first_name", patient.getFirstname());
            values.put("middle_name", patient.getMiddlename());
            values.put("last_name", patient.getLastname());
            values.put("address1", patient.getAddress1());
            values.put("country", patient.getCountry());
            values.put("date_of_birth", patient.getDateofbirth());
            values.put("gender", patient.getGender());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("dead", patient.getDead());
            values.put("synced", patient.getSyncd());
//                Logger.logD("pulldata", "datadumper" + values);
            createdRecordsCount = db.insertWithOnConflict("tbl_patient", null, values, SQLiteDatabase.CONFLICT_REPLACE);
//            }
            db.setTransactionSuccessful();
//            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
//            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }
        return isCreated;

    }

    public boolean insertPatientToDB(PatientDTO patientDTO) throws DAOException {
        boolean isCreated = true;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        ArrayList<PatientAttributesDTO> patientAttributesList = new ArrayList<PatientAttributesDTO>();
        try {

//                Logger.logD("create", "create has to happen");
            values.put("uuid", patientDTO.getUuid());
            values.put("openmrs_id", patientDTO.getOpenmrsId());
            values.put("first_name", patientDTO.getFirstname());
            values.put("middle_name", patientDTO.getMiddlename());
            values.put("last_name", patientDTO.getLastname());
            values.put("address1", patientDTO.getAddress1());
            values.put("country", patientDTO.getCountry());
            values.put("date_of_birth", patientDTO.getDateofbirth());
            values.put("gender", patientDTO.getGender());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("dead", patientDTO.getDead());
            values.put("synced", patientDTO.getSyncd());
            patientAttributesList = patientDTO.getPatientAttributesDTOList();
            patientAttributes(patientAttributesList);
//                Logger.logD("pulldata", "datadumper" + values);
            createdRecordsCount = db.insertWithOnConflict("tbl_patient", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }
        return isCreated;

    }

    public boolean updatePatients(PatientDTO patient) throws DAOException {
        boolean isCreated = true;
//        (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase())
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {

//            for (PatientDTO patient : patientDTO) {
//                Logger.logD("update", "update has to happen");
                values.put("openmrs_id", patient.getOpenmrsId());
                values.put("first_name", patient.getFirstname());
                values.put("middle_name", patient.getMiddlename());
                values.put("last_name", patient.getLastname());
                values.put("address1", patient.getAddress1());
                values.put("country", patient.getCountry());
                values.put("date_of_birth", patient.getDateofbirth());
                values.put("gender", patient.getGender());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("dead", patient.getDead());
                values.put("synced", patient.getSyncd());
//                Logger.logD("pulldata", "datadumper" + values);
                updatecount = db.updateWithOnConflict("tbl_patient", values, selection, new String[]{patient.getUuid()}, SQLiteDatabase.CONFLICT_REPLACE);
//            }
            db.setTransactionSuccessful();
//            Logger.logD("updated", "updatedrecords count" + updatecount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
//            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }

        return isCreated;

    }


    public boolean patientAttributes(List<PatientAttributesDTO> patientAttributesDTOS) throws DAOException {
        boolean isInserted = true;

        try (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            for (int i = 0; i < patientAttributesDTOS.size(); i++) {
                values.put("uuid", patientAttributesDTOS.get(i).getUuid());
                values.put("person_attribute_type_uuid", patientAttributesDTOS.get(i).getPersonAttributeTypeUuid());
                values.put("patientuuid", patientAttributesDTOS.get(i).getPatientuuid());
                values.put("value", patientAttributesDTOS.get(i).getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                db.insertWithOnConflict("tbl_patient_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }


        return isInserted;

    }


    public boolean patinetAttributeMaster(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTOS) throws DAOException {
        boolean isInserted = true;

        try (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            for (int i = 0; i < patientAttributeTypeMasterDTOS.size(); i++) {
                values.put("uuid", patientAttributeTypeMasterDTOS.get(i).getUuid());
                values.put("name", patientAttributeTypeMasterDTOS.get(i).getName());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                db.insertWithOnConflict("tbl_patient_attribute_master", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }

        return isInserted;
    }

    public String getUuidForAttribute(String attr) {
        String attributeUuid = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_patient_attribute_master where name = ? COLLATE NOCASE", new String[]{attr});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                attributeUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            }
        }

        return attributeUuid;
    }
}

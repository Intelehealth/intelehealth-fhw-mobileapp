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
            for (int i = 0; i < patientDTO.size(); i++) {
                Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_patient where uuid = ?", new String[]{patientDTO.get(i).getUuid()});
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        Logger.logD("update", "update has to happen");
                        if (updatePatients(patientDTO)) {
                            Logger.logD("update", "updated");
                        } else {
                            Logger.logD("update", "failed to updated");
                        }
                    }
                } else {
                    Logger.logD("insert", "insert has to happen");
                    if (createPatients(patientDTO)) {
                        Logger.logD("insert", "inserted");
                    } else {
                        Logger.logD("insert", "failed to inserted");
                    }

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

    public boolean createPatients(List<PatientDTO> patientDTO) throws DAOException {
        boolean isCreated = true;

//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (PatientDTO patient : patientDTO) {
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
            }
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
//            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }

       /* SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String sql = "INSERT INTO  tbl_patient  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        try {
            for (PatientDTO c : patientDTO) {
                statement.clearBindings();
                statement.bindString(1, c.getOpenmrsUuid());
                statement.bindString(2, c.getOpenmrsUuid());
                statement.bindString(3, c.getOpenmrsId());
                statement.bindString(4, c.getFirstname());
                statement.bindString(5, c.getMiddlename());
                statement.bindString(6, c.getLastname());
                statement.bindString(7, c.getAddress1());
                statement.bindString(8, c.getCountry());
                statement.bindString(9, c.getDateofbirth());
                statement.bindString(10, c.getGender());
                statement.bindString(11, AppConstants.dateAndTimeUtils.currentDateTime());
                statement.bindString(12, "TRUE");
                statement.bindString(13, "TRUE");
                statement.bindString(14, "TRUE");
                statement.bindString(15, "TRUE");
                statement.bindString(16, "TRUE");
                statement.bindString(17, "TRUE");
                statement.bindString(18, "TRUE");
                statement.bindString(19, "TRUE");
//                    statement.b(1, c.());
//                    statement.bindLong(2, c.getCityName());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } catch (SQLException e){

        }finally {
            db.endTransaction();
        }*/

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

    public boolean updatePatients(List<PatientDTO> patientDTO) throws DAOException {
        boolean isCreated = true;
//        (SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase())
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {

            for (PatientDTO patient : patientDTO) {
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
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + updatecount);
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

    private boolean mergePatients() throws DAOException {
        boolean ismerged = true;
        SQLiteDatabase db = null;
        try {
            db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();

//            String insertQuery = "insert into tbl_patient(openmrs_uuid,openmrs_id,first_name,middle_name,last_name) select openmrs_uuid,openmrs_id,first_name,middle_name,last_name from patient_temp where openmrs_uuid not in (select openmrs_uuid from tbl_patient)";
//            db.execSQL(insertQuery);
//
//
//            String updateQuery = "update tbl_patient SET (tbl_patient.openmrs_id,tbl_patient.first_name,middle_name,last_name,date_of_birth,phone_number,address1,address2,city_village,state_province,postal_code,country,gender,sdw,occupation) = (select pt.openmrs_id,pt.first_name,pt.middle_name,pt.last_name,pt.date_of_birth,pt.phone_number,pt.address1,pt.address2,pt.city_village,pt.state_province,pt.postal_code,pt.country,pt.gender,pt.sdw,pt.occupation from patient_temp  pt where tbl_patient.openmrs_uuid=pt.openmrs_uuid)";
//            //String updateQuery = "update tbl_patient set first_name = pt.first_name  from patient_temp pt,tbl_patient  where tbl_patient.openmrs_uuid=patient_temp.openmrs_uuid";
//            db.execSQL(updateQuery);

            /*sql query
            insert into tbl_patient(openmrs_uuid,openmrs_id,first_name,middle_name,last_name) select openmrs_uuid,openmrs_id,first_name,middle_name,last_name from patient_temp where openmrs_uuid not in (select openmrs_uuid from tbl_patient);
            * */

        } catch (SQLException e) {
            ismerged = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }

        return ismerged;
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

package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel;
import org.intelehealth.app.utilities.CustomLog;


import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.intelehealth.app.models.FamilyMemberRes;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.services.MyIntentService;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientAttributeTypeMasterDTO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.pushRequestApiCall.Attribute;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import io.reactivex.Observable;

public class PatientsDAO extends BaseDao {

    private int updatecount = 0;
    private long createdRecordsCount = 0;
    int limit = 10, offset = 0;
    private static final String TAG = "PatientsDAO";
    private String currentTableName;

    public boolean insertPatients(List<PatientDTO> patientDTO) throws DAOException {
        setTableName("tbl_patient");
        boolean isInserted = true;
        List<HashMap<String, Object>> patientList = new ArrayList<>();
        for (PatientDTO patient : patientDTO) {
            patientList.add(createPatientMap(patient));
        }
        executeInBackground(bulkInsert(patientList));

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

    public HashMap<String, Object> createPatientMap(PatientDTO patient) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("uuid", patient.getUuid());
        values.put("openmrs_id", patient.getOpenmrsId());
        values.put("first_name", patient.getFirstname());
        values.put("middle_name", patient.getMiddlename());
        values.put("last_name", patient.getLastname());
        values.put("address1", patient.getAddress1());
        values.put("address2", patient.getAddress2());
        values.put("country", patient.getCountry());
        values.put("date_of_birth",
                DateAndTimeUtils.formatDateFromOnetoAnother(patient.getDateofbirth(),
                        "MMM dd, yyyy hh:mm:ss a", "yyyy-MM-dd"));
        values.put("gender", patient.getGender());
        values.put("postal_code", patient.getPostalcode());
        values.put("state_province", patient.getStateprovince());
        values.put("city_village", patient.getCityvillage());
        values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());

        values.put("guardian_type", patient.getGuardianType());
        values.put("guardian_name", patient.getGuardianName());
        values.put("contact_type", patient.getContactType());
        values.put("em_contact_name", patient.getEmContactName());
        values.put("em_contact_num", patient.getEmContactNumber());

        values.put("address3", patient.getAddress3());
        values.put("address6", patient.getAddress6());
        values.put("countyDistrict", patient.getDistrict());
        values.put("abha_number", patient.getAbhaNumber());
        values.put("abha_address", patient.getAbhaAddress());

        values.put("dead", patient.getDead());
        values.put("sync", patient.getSyncd().toString());
        return values;
    }

    public String getValueByUuid(Set<Attribute> patientAttributesDTO, String targetUuid) {
        for (Attribute dto : patientAttributesDTO) {
            if (dto.getAttributeType().equals(targetUuid)) {
                return dto.getValue(); // Return the value for the matching UUID
            }
        }
        return null; // Return null if no match is found
    }

    public boolean createPatients(PatientDTO patient, SQLiteDatabase db) throws DAOException {
        Logger.logD(TAG, "createPatients = " + new Gson().toJson(patient));

        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", patient.getUuid());
            values.put("openmrs_id", patient.getOpenmrsId());
            values.put("first_name", patient.getFirstname());
            values.put("middle_name", patient.getMiddlename());
            values.put("last_name", patient.getLastname());
            values.put("address1", patient.getAddress1());
            values.put("address2", patient.getAddress2());
            values.put("country", patient.getCountry());
            values.put("date_of_birth", DateAndTimeUtils.formatDateFromOnetoAnother(patient.getDateofbirth(), "MMM dd, yyyy hh:mm:ss a", "yyyy-MM-dd"));
            values.put("gender", patient.getGender());
            values.put("postal_code", patient.getPostalcode());
            values.put("state_province", patient.getStateprovince());
            values.put("city_village", patient.getCityvillage());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());

            values.put("guardian_type", patient.getGuardianType());
            values.put("guardian_name", patient.getGuardianName());
            values.put("contact_type", patient.getContactType());
            values.put("em_contact_name", patient.getEmContactName());
            values.put("em_contact_num", patient.getEmContactNumber());

            values.put("address3", patient.getAddress3());
            values.put("address6", patient.getAddress6());
            values.put("countyDistrict", patient.getDistrict());
            values.put("abha_number", patient.getAbhaNumber());
            values.put("abha_address", patient.getAbhaAddress());

            values.put("dead", patient.getDead());
            values.put("sync", patient.getSyncd());
            createdRecordsCount = db.insertWithOnConflict("tbl_patient", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            isCreated = createdRecordsCount > 0;
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;

    }

    public boolean insertPatientToDB(PatientDTO patientDTO, String uuid) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = null;
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        List<PatientAttributesDTO> patientAttributesList = new ArrayList<PatientAttributesDTO>();
        try {
            Logger.logD("create", "create has to happen");
            values.put("uuid", uuid);
            values.put("openmrs_id", patientDTO.getOpenmrsId());
            values.put("first_name", patientDTO.getFirstname());
            values.put("middle_name", patientDTO.getMiddlename());
            values.put("last_name", patientDTO.getLastname());
            values.put("phone_number", patientDTO.getPhonenumber());
            values.put("address1", patientDTO.getAddress1());
            values.put("address2", patientDTO.getAddress2());
            values.put("country", patientDTO.getCountry());
            values.put("date_of_birth", patientDTO.getDateofbirth());
            values.put("gender", patientDTO.getGender());
            values.put("postal_code", patientDTO.getPostalcode());
            values.put("city_village", patientDTO.getCityvillage());
            values.put("state_province", patientDTO.getStateprovince());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("patient_photo", patientDTO.getPatientPhoto());

            values.put("guardian_type", patientDTO.getGuardianType());
            values.put("guardian_name", patientDTO.getGuardianName());
            values.put("contact_type", patientDTO.getContactType());
            values.put("em_contact_name", patientDTO.getEmContactName());
            values.put("em_contact_num", patientDTO.getEmContactNumber());

            values.put("countyDistrict", patientDTO.getDistrict());
            values.put("address3", patientDTO.getAddress3());
            values.put("address6", patientDTO.getAddress6());
            values.put("abha_number", patientDTO.getAbhaNumber());
            values.put("abha_address", patientDTO.getAbhaAddress());

            values.put("dead", patientDTO.getDead());
            values.put("sync", false);
            patientAttributesList = patientDTO.getPatientAttributesDTOList();
            if (patientAttributesList != null)
                insertPatientAttributes(patientAttributesList, db);
            Logger.logD("pulldata", "datadumper" + values);
            createdRecordsCount1 = db.insert("tbl_patient", null, values);
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;

    }

    public boolean updatePatientToDB(PatientDTO patientDTO, String uuid) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String whereclause = "Uuid=?";
        db.beginTransaction();
        try {

            Logger.logD("create", "create has to happen");
            values.put("uuid", uuid);
            values.put("openmrs_id", patientDTO.getOpenmrsId());
            values.put("first_name", patientDTO.getFirstname());
            values.put("middle_name", patientDTO.getMiddlename());
            values.put("last_name", patientDTO.getLastname());
            values.put("phone_number", patientDTO.getPhonenumber());
            values.put("address1", patientDTO.getAddress1());
            values.put("address2", patientDTO.getAddress2());
            values.put("country", patientDTO.getCountry());
            values.put("date_of_birth", patientDTO.getDateofbirth());
            values.put("gender", patientDTO.getGender());
            values.put("postal_code", patientDTO.getPostalcode());
            values.put("city_village", patientDTO.getCityvillage());
            values.put("state_province", patientDTO.getStateprovince());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("patient_photo", patientDTO.getPatientPhoto());

            values.put("guardian_type", patientDTO.getGuardianType());
            values.put("guardian_name", patientDTO.getGuardianName());
            values.put("contact_type", patientDTO.getContactType());
            values.put("em_contact_name", patientDTO.getEmContactName());
            values.put("em_contact_num", patientDTO.getEmContactNumber());

            values.put("tms_case_number", patientDTO.getTmhCaseNumber());
            values.put("request_id", patientDTO.getRequestId());
            values.put("discipline", patientDTO.getDiscipline());
            values.put("department", patientDTO.getDepartment());

            values.put("countyDistrict", patientDTO.getDistrict());
            values.put("address3", patientDTO.getAddress3());
            values.put("address6", patientDTO.getAddress6());
            values.put("abha_number", patientDTO.getAbhaNumber());
            values.put("abha_address", patientDTO.getAbhaAddress());

            values.put("dead", false);
            values.put("sync", false);

            insertPatientAttributes(patientDTO.getPatientAttributesDTOList(), db);
            Logger.logD("pulldata", "datadumper" + values);
            createdRecordsCount1 = db.update("tbl_patient", values, whereclause, new String[]{uuid});
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;

    }

    public boolean updatePatientToDB1(Patient patientDTO, String uuid, List<PatientAttributesDTO> patientAttributesDTOS) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String whereclause = "Uuid=?";
        db.beginTransaction();
        List<PatientAttributesDTO> patientAttributesList = new ArrayList<PatientAttributesDTO>();
        try {

            Logger.logD("create", "create has to happen");
            values.put("uuid", uuid);
            values.put("openmrs_id", patientDTO.getOpenmrs_id());
            values.put("first_name", patientDTO.getFirst_name());
            values.put("middle_name", patientDTO.getMiddle_name());
            values.put("last_name", patientDTO.getLast_name());
            values.put("phone_number", patientDTO.getPhone_number());
            values.put("address1", patientDTO.getAddress1());
            values.put("address2", patientDTO.getAddress2());
            values.put("country", patientDTO.getCountry());
            values.put("date_of_birth", patientDTO.getDate_of_birth());
            values.put("gender", patientDTO.getGender());
            values.put("postal_code", patientDTO.getPostal_code());
            values.put("city_village", patientDTO.getCity_village());
            values.put("state_province", patientDTO.getState_province());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("patient_photo", patientDTO.getPatient_photo());
            values.put("dead", false);
            values.put("sync", false);

            insertPatientAttributes(patientAttributesDTOS, db);
            Logger.logD("pulldata", "datadumper" + values);
            createdRecordsCount1 = db.update("tbl_patient", values, whereclause, new String[]{uuid});
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;

    }

    public boolean patientAttributes(List<PatientAttributesDTO> patientAttributesDTOS) throws DAOException {
        setTableName("tbl_patient_attribute");
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

    public List<Attribute> getPatientAttributes(String patientuuid) throws DAOException {
        List<Attribute> patientAttributesList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        try {
            String query = "SELECT * from tbl_patient_attribute WHERE patientuuid= '" + patientuuid + "'";
            Cursor cursor = db.rawQuery(query, null, null);
            Attribute attribute = new Attribute();
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    attribute = new Attribute();
                    String attributeType = cursor.getString(cursor.getColumnIndex("person_attribute_type_uuid"));
                    attribute.setAttributeType(attributeType);
                    attribute.setValue(cursor.getString(cursor.getColumnIndex("value")));
                    if (attributeType != null && !attributeType.isEmpty()) {
                        patientAttributesList.add(attribute);
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            //db.setTransactionSuccessful();
        } catch (SQLException e) {
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage());
        } finally {
            //db.endTransaction();

        }
        return patientAttributesList;
    }


    public ArrayList<PatientAttributesDTO> getPatientRoaster(String patientuuid) throws DAOException {
        ArrayList<PatientAttributesDTO> patientAttributesList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        try {
            String query = "SELECT * from tbl_patient_attribute WHERE patientuuid= '" + patientuuid + "'";
            Cursor cursor = db.rawQuery(query, null, null);
            PatientAttributesDTO attribute;
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    attribute = new PatientAttributesDTO();
                    String attributeType = cursor.getString(cursor.getColumnIndex("person_attribute_type_uuid"));
                    attribute.setPersonAttributeTypeUuid(attributeType);
                    attribute.setValue(cursor.getString(cursor.getColumnIndex("value")));
                    attribute.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                    attribute.setPatientuuid(patientuuid);
                    if (attributeType != null && !attributeType.isEmpty()) {
                        patientAttributesList.add(attribute);
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            //db.setTransactionSuccessful();
        } catch (SQLException e) {
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage());
        } finally {
            //db.endTransaction();

        }
        return patientAttributesList;
    }


    //Fetch householdID value using Patient UUID
    public String getHouseHoldValue(String patientuuid) throws DAOException {
        String houseHoldID = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute " +
                    "where patientuuid = ? AND person_attribute_type_uuid=? AND voided='0' COLLATE NOCASE", new String[]{
                    patientuuid, "be8e386b-ca22-447d-82a1-b80366e5f848"});

            // IDA HOUSEHOLD NO - be8e386b-ca22-447d-82a1-b80366e5f848 (newly created)
            // NAS HOUSEHOLD NO - 10720d1a-1471-431b-be28-285d64767093 (already present)

            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    houseHoldID = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }
        return houseHoldID;
    }

    //Fetch all patient UUID's from HouseHoldValue
    public List<String> getPatientUUIDs(String houseHoldValue) throws DAOException {
        List<String> patientUUIDs = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT patientuuid FROM tbl_patient_attribute where value = ? COLLATE NOCASE", new String[]{houseHoldValue});

            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    patientUUIDs.add(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage());
        } finally {
            db.endTransaction();
        }
        return patientUUIDs;
    }

    public List<FamilyMemberRes> getPatientName(String patientuuid) throws DAOException {

        List<FamilyMemberRes> listPatientNames = new ArrayList<>();
        FamilyMemberRes familyMemberRes = new FamilyMemberRes();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT uuid, openmrs_id,first_name,middle_name,last_name FROM tbl_patient where uuid = ? COLLATE NOCASE", new String[]{patientuuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    familyMemberRes.setPatientUUID(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                    familyMemberRes.setOpenMRSID(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    familyMemberRes.setName(cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " + cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    listPatientNames.add(familyMemberRes);
//                  middle_name = cursor.getString(cursor.getColumnIndexOrThrow("middle_name"));
                }
            } else {
                familyMemberRes.setName("Unknown");
                listPatientNames.add(familyMemberRes);
            }
            cursor.close();
            // db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            CustomLog.e(TAG, s.getMessage());
            throw new DAOException(s);
        }
        return listPatientNames;
    }

    public String getAttributesName(String attributeuuid) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        String name = "";
        try {
            String query = "SELECT name from tbl_patient_attribute_master WHERE uuid= '" + attributeuuid + "'";
            Cursor cursor = db.rawQuery(query, null, null);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    name = cursor.getString(cursor.getColumnIndex("name"));
                    cursor.moveToNext();
                }
            }
            cursor.close();
            //db.setTransactionSuccessful();
        } catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage());
        } finally {
            //db.endTransaction();

        }
        return name;
    }

    public boolean insertPatientAttributes(List<PatientAttributesDTO> patientAttributesDTOS) throws DAOException {
        if (patientAttributesDTOS == null) return false;
        boolean isInserted = true;
        ContentValues values = new ContentValues();
        SQLiteDatabase db = null;
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < patientAttributesDTOS.size(); i++) {
                values.put("uuid", patientAttributesDTOS.get(i).getUuid());
                values.put("person_attribute_type_uuid", patientAttributesDTOS.get(i).getPersonAttributeTypeUuid());
                values.put("patientuuid", patientAttributesDTOS.get(i).getPatientuuid());
                values.put("value", patientAttributesDTOS.get(i).getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", false);
                db.insertWithOnConflict("tbl_patient_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;

    }


    public boolean insertPatientAttributes(List<PatientAttributesDTO> patientAttributesDTOS, SQLiteDatabase db) throws DAOException {
        if (patientAttributesDTOS == null) return false;
        boolean isInserted = true;
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (int i = 0; i < patientAttributesDTOS.size(); i++) {
                PatientAttributesDTO patientAttributesDTO = patientAttributesDTOS.get(i);
                if (patientAttributesDTO.getPersonAttributeTypeUuid() != null && !patientAttributesDTO.getPersonAttributeTypeUuid().isEmpty()) {
                    values.put("uuid", patientAttributesDTOS.get(i).getUuid());
                    values.put("person_attribute_type_uuid", patientAttributesDTOS.get(i).getPersonAttributeTypeUuid());
                    values.put("patientuuid", patientAttributesDTOS.get(i).getPatientuuid());
                    values.put("value", patientAttributesDTOS.get(i).getValue());
                    values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                    values.put("sync", false);
                    db.insertWithOnConflict("tbl_patient_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;

    }


/*
    public boolean patinetAttributeMaster(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTOS) throws DAOException {
        setTableName("tbl_patient_attribute_master");
        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < patientAttributeTypeMasterDTOS.size(); i++) {
                values.put("uuid", patientAttributeTypeMasterDTOS.get(i).getUuid());
                values.put("name", patientAttributeTypeMasterDTOS.get(i).getName());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", "TRUE");
                db.insertWithOnConflict("tbl_patient_attribute_master", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;
    }
*/

    public String getUuidForAttribute(String attr) {
        String attributeUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_patient_attribute_master where name = ? COLLATE NOCASE", new String[]{attr});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                attributeUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            }
        }
        cursor.close();

        return attributeUuid;
    }

    public boolean updateOpemmrsId(String openmrsId, String synced, String uuid) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("patinetdao", "updateopenmrs " + uuid + openmrsId + synced);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("openmrs_id", openmrsId);
            values.put("sync", synced);
            values.put("uuid", uuid);
            int i = db.update("tbl_patient", values, whereclause, whereargs);
            Logger.logD("patient", "description" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            CustomLog.e("patient", "patient" + sql.getMessage());
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }
        Intent intent = new Intent(IntelehealthApplication.getAppContext(), MyIntentService.class);
        IntelehealthApplication.getAppContext().startService(intent);
        return isUpdated;
    }

    public List<PatientDTO> unsyncedPatients() throws DAOException {
        List<PatientDTO> patientDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_patient where (sync = ? OR sync=?) COLLATE NOCASE", new String[]{"0", "false"});
            PatientDTO patientDTO = new PatientDTO();
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    patientDTO = new PatientDTO();
                    patientDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    patientDTO.setOpenmrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                    patientDTO.setFirstname(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                    patientDTO.setLastname(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                    patientDTO.setMiddlename(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                    patientDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    patientDTO.setDateofbirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                    patientDTO.setPhonenumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                    patientDTO.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                    patientDTO.setStateprovince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                    patientDTO.setCityvillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                    patientDTO.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                    patientDTO.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                    patientDTO.setPostalcode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                    patientDTO.setGuardianType(idCursor.getString(idCursor.getColumnIndexOrThrow("guardian_type")));
                    patientDTO.setGuardianName(idCursor.getString(idCursor.getColumnIndexOrThrow("guardian_name")));
                    // Patient contatct type
                    patientDTO.setContactType(idCursor.getString(idCursor.getColumnIndexOrThrow("contact_type")));
                    patientDTO.setEmContactName(idCursor.getString(idCursor.getColumnIndexOrThrow("em_contact_name")));
                    patientDTO.setEmContactNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("em_contact_num")));

                    patientDTO.setAddress3(idCursor.getString(idCursor.getColumnIndexOrThrow("address3")));
                    patientDTO.setAddress6(idCursor.getString(idCursor.getColumnIndexOrThrow("address6")));
                    patientDTO.setDistrict(idCursor.getString(idCursor.getColumnIndexOrThrow("countyDistrict")));
                    patientDTO.setAbhaNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("abha_number")));
                    patientDTO.setAbhaAddress(idCursor.getString(idCursor.getColumnIndexOrThrow("abha_address")));
                    patientDTOList.add(patientDTO);

                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return patientDTOList;
    }


    public boolean updatePatientPhoto(String patientuuid, String profilePhotoPath) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("patinetdao", "patientphoto " + patientuuid + profilePhotoPath);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {patientuuid};
        try {
            values.put("patient_photo", profilePhotoPath);
            values.put("uuid", patientuuid);
            int i = db.update("tbl_patient", values, whereclause, whereargs);
            Logger.logD("patient", "description" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(sql);
            CustomLog.e(TAG, sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }
        return isUpdated;
    }

    public String getOpenmrsId(String patientuuid) throws DAOException {
        String id = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT openmrs_id FROM tbl_patient where uuid = ? COLLATE NOCASE", new String[]{patientuuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    id = cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id"));
                }
            }
            cursor.close();
            //db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            CustomLog.e(TAG, s.getMessage());
            throw new DAOException(s);
        } finally {
            //db.endTransaction();

        }
        return id;

    }

    public static String fetch_gender(String patientUuid) {
        String gender = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.query("tbl_patient", new String[]{"gender"}, "uuid=?",
                new String[]{patientUuid}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return gender;
    }

    public static List<PatientDTO> getAllPatientsFromDB(int limit, int offset) {
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String table = "tbl_patient";
        final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY modified_date DESC limit ? offset ?",
                new String[]{String.valueOf(limit), String.valueOf(offset)});
        try {
            if (searchCursor.moveToFirst()) {
                do {
                    PatientDTO model = new PatientDTO();
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                    model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                    model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                    model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setPhonenumber(StringUtils.mobileNumberEmpty
                            (phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                    model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                    model.setGuardianType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_type")));
                    model.setGuardianName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_name")));
                    model.setContactType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("contact_type")));
                    model.setEmContactName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_name")));
                    model.setEmContactNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_num")));
                    modelList.add(model);
                } while (searchCursor.moveToNext());
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }
        return modelList;

    }

    public static List<PatientDTO> getQueryPatients(String query) {
        String search = query/*.trim().replaceAll("\\s", "")*/;
        // search = StringUtils.mobileNumberEmpty(phoneNumber());
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String table = "tbl_patient";
        List<String> patientUUID_List = new ArrayList<>();

        final Cursor search_mobile_cursor = db.rawQuery("SELECT DISTINCT patientuuid FROM tbl_patient_attribute WHERE value = ?",
                new String[]{search});
        /* DISTINCT will get remove the duplicate values. The duplicate value will come when you have created
         * a patient with mobile no. 12345 and patient is pushed than later you edit the mobile no to
         * 12344 or something. In this case, the local db maintains two separate rows both with value: 12344 */
        //if no data is present against that corresponding cursor than cursor count returns = 0 ... i.e cursor_count = 0 ...
        try {
            if (search_mobile_cursor.moveToFirst()) {
                do {
                    patientUUID_List.add(search_mobile_cursor.getString
                            (search_mobile_cursor.getColumnIndexOrThrow("patientuuid")));
                }
                while (search_mobile_cursor.moveToNext());
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }
        CustomLog.d("patientUUID_list", "list: " + patientUUID_List);
        if (patientUUID_List.size() != 0) {
            for (int i = 0; i < patientUUID_List.size(); i++) {
                final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table +
                        " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR address1 LIKE '%" + search + "%' OR uuid = ? " +
                        "OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) " +
                        "LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR " +
                        "(first_name || last_name) LIKE '%" + search + "%'" +
                        "OR openmrs_id " + "LIKE '%" + search + "%' " + "ORDER BY first_name ASC", new String[]{patientUUID_List.get(i)});
                //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
                try {
                    if (searchCursor.moveToFirst()) {
                        do {
                            PatientDTO model = new PatientDTO();
                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                            model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                            model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setPhonenumber(StringUtils.mobileNumberEmpty
                                    (phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                            model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                            model.setGuardianType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_type")));
                            model.setGuardianName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_name")));
                            model.setContactType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("contact_type")));
                            model.setEmContactName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_name")));
                            model.setEmContactNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_num")));
                            modelList.add(model);
                        } while (searchCursor.moveToNext());
                    }
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    CustomLog.e(TAG, e.getMessage());
                }
            }
        } else { // no mobile number was added in search text.
            final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR address1 LIKE '%" + search + "%' " +
                    "OR middle_name LIKE '%" + search + "%' OR last_name LIKE '%" + search + "%' OR " +
                    "(first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) " +
                    "LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%'" +
                    "OR first_name || ' ' || middle_name LIKE" + "'%" + search + "%' OR first_name || ' ' || middle_name || ' ' || last_name LIKE" + "'%" + search + "%' " +
                    "OR middle_name || ' ' || last_name LIKE" + "'%" + search + "%'" +
                    "OR first_name || ' ' || last_name LIKE" + "'%" + search + "%'" +
                    " OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC", null);

            //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
            try {
                if (searchCursor.moveToFirst()) {
                    do {
                        PatientDTO model = new PatientDTO();
                        model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                        model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                        model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                        model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                        model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                        model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                        model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                        model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                        model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                        model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                        model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                        model.setGuardianType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_type")));
                        model.setGuardianName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_name")));
                        model.setContactType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("contact_type")));
                        model.setEmContactName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_name")));
                        model.setEmContactNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_num")));
                        modelList.add(model);
                    } while (searchCursor.moveToNext());
                }
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                CustomLog.e(TAG, e.getMessage());
            }
        }
        return modelList;
    }

    public static Observable<List<PatientDTO>> getQueryPatientsObs(String query) {
        return Observable.create(emitter -> {
            String search = query/*.trim().replaceAll("\\s", "")*/;
            // search = StringUtils.mobileNumberEmpty(phoneNumber());
            List<PatientDTO> modelList = new ArrayList<PatientDTO>();
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
            String table = "tbl_patient";
            List<String> patientUUID_List = new ArrayList<>();

            final Cursor search_mobile_cursor = db.rawQuery("SELECT DISTINCT patientuuid FROM tbl_patient_attribute WHERE value = ?",
                    new String[]{search});
            /* DISTINCT will get remove the duplicate values. The duplicate value will come when you have created
             * a patient with mobile no. 12345 and patient is pushed than later you edit the mobile no to
             * 12344 or something. In this case, the local db maintains two separate rows both with value: 12344 */
            //if no data is present against that corresponding cursor than cursor count returns = 0 ... i.e cursor_count = 0 ...
            try {
                if (search_mobile_cursor.moveToFirst()) {
                    do {
                        patientUUID_List.add(search_mobile_cursor.getString
                                (search_mobile_cursor.getColumnIndexOrThrow("patientuuid")));
                    }
                    while (search_mobile_cursor.moveToNext());
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                CustomLog.e(TAG, e.getMessage());
            }
            CustomLog.d("patientUUID_list", "list: " + patientUUID_List);
            if (patientUUID_List.size() != 0) {
                for (int i = 0; i < patientUUID_List.size(); i++) {
                    final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table +
                            " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR address6 LIKE '%" + search + "%' OR uuid = ? " +
                            "OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) " +
                            "LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR " +
                            "(first_name || last_name) LIKE '%" + search + "%'" +
                            "OR openmrs_id " + "LIKE '%" + search + "%' " + "ORDER BY first_name ASC", new String[]{patientUUID_List.get(i)});
                    //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
                    try {
                        if (searchCursor.moveToFirst()) {
                            do {
                                PatientDTO model = new PatientDTO();
                                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                                model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                                model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                                model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                                model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                                model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                                model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                                model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                                model.setPhonenumber(StringUtils.mobileNumberEmpty
                                        (phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                                model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                                model.setGuardianType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_type")));
                                model.setGuardianName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_name")));
                                model.setContactType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("contact_type")));
                                model.setEmContactName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_name")));
                                model.setEmContactNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_num")));
                                modelList.add(model);
                            } while (searchCursor.moveToNext());
                        }
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        CustomLog.e(TAG, e.getMessage());
                    }
                }
            } else { // no mobile number was added in search text.
                final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' " +
                        "OR middle_name LIKE '%" + search + "%'  OR  address6 LIKE '%" + search + "%' OR last_name LIKE '%" + search + "%' OR " +
                        "(first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) " +
                        "LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%'" +
                        "OR first_name || ' ' || middle_name LIKE" + "'%" + search + "%' OR first_name || ' ' || middle_name || ' ' || last_name LIKE" + "'%" + search + "%' " +
                        "OR middle_name || ' ' || last_name LIKE" + "'%" + search + "%'" +
                        "OR first_name || ' ' || last_name LIKE" + "'%" + search + "%'" +
                        " OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC", null);

                //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
                try {
                    if (searchCursor.moveToFirst()) {
                        do {
                            PatientDTO model = new PatientDTO();
                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                            model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                            model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                            model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                            model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                            model.setGuardianType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_type")));
                            model.setGuardianName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("guardian_name")));
                            model.setContactType(searchCursor.getString(searchCursor.getColumnIndexOrThrow("contact_type")));
                            model.setEmContactName(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_name")));
                            model.setEmContactNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("em_contact_num")));
                            modelList.add(model);
                        } while (searchCursor.moveToNext());
                    }
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    CustomLog.e(TAG, e.getMessage());
                }
            }
            emitter.onNext(modelList);
            emitter.onComplete();
        });
    }

    public static String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute where patientuuid = ? AND " +
                "person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7'", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            CustomLog.e(TAG, s.getMessage());
        }
        idCursor.close();
        return phone;
    }

    public static VisitDTO isVisitPresentForPatient_fetchVisitValues(String patientUUID) {
        VisitDTO visitDTO = new VisitDTO();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit WHERE patientuuid = ?", new String[]{patientUUID});
        try {
            if (idCursor.moveToFirst()) {
                do {
                    visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                    visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
                    String isSynced = idCursor.getString(idCursor.getColumnIndexOrThrow("sync"));
                    boolean sync = false;
                    if (isSynced != null) {
                        if (isSynced.equalsIgnoreCase("0") || isSynced.toLowerCase().equalsIgnoreCase("false"))
                            sync = false;
                        else if (isSynced.equalsIgnoreCase("1") || isSynced.toLowerCase().equalsIgnoreCase("true"))
                            sync = true;
                    }
                    //CustomLog.d("TAG", "isVisitPresentForPatient_fetchVisitValues: " + sync);
                    visitDTO.setSyncd(sync);
                    //CustomLog.d("TAG", "isVisitPresentForPatient_fetchVisitValues: visit: " + visitDTO);
                }
                while (idCursor.moveToNext());
            }
        } catch (SQLException e) {
            CustomLog.e(TAG, e.getMessage());

        }

        return visitDTO;
    }

    public static String[] getPatientDobAgeGender(String patientUuid) {
        String[] result = new String[0];
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("select * from tbl_patient where uuid = ? and " +
                "(sync = 1 OR sync = 'true' OR sync = 'TRUE') and voided = 0", new String[]{patientUuid});


        if (cursor.moveToFirst()) {
            do {
                String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
                String dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
                result = new String[]{gender, dob};
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    public static String[] getPatientsPhoneNumber(String patientUuid) {
        String[] result = new String[0];
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("select * from tbl_patient where uuid = ? and " +
                "(sync = 1 OR sync = 'true' OR sync = 'TRUE') and voided = 0", new String[]{patientUuid});


        if (cursor.moveToFirst()) {
            do {
                String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
                String dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
                result = new String[]{gender, dob};
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    public static PatientDTO getPatientDetailsByUuid(String patientUuid) {
        PatientDTO patientDTO = null;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("select * from tbl_patient where uuid = ? and " +
                "(sync = 1 OR sync = 'true' OR sync = 'TRUE') and voided = 0", new String[]{patientUuid});


        if (cursor.moveToFirst()) {
            do {


                patientDTO = new PatientDTO();
                patientDTO.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                patientDTO.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                patientDTO.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                patientDTO.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                patientDTO.setPhonenumber(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                patientDTO.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                patientDTO.setAddress1(cursor.getString(cursor.getColumnIndexOrThrow("address1")));
                patientDTO.setAddress2(cursor.getString(cursor.getColumnIndexOrThrow("address2")));
                patientDTO.setCityvillage(cursor.getString(cursor.getColumnIndexOrThrow("city_village")));
                patientDTO.setStateprovince(cursor.getString(cursor.getColumnIndexOrThrow("state_province")));
                patientDTO.setCountry(cursor.getString(cursor.getColumnIndexOrThrow("country")));
                patientDTO.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                patientDTO.setPostalcode(cursor.getString(cursor.getColumnIndexOrThrow("postal_code")));
                patientDTO.setPatientPhoto(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                patientDTO.setGuardianType(cursor.getString(cursor.getColumnIndexOrThrow("guardian_type")));
                patientDTO.setGuardianName(cursor.getString(cursor.getColumnIndexOrThrow("guardian_name")));
                patientDTO.setContactType(cursor.getString(cursor.getColumnIndexOrThrow("contact_type")));
                patientDTO.setEmContactName(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_name")));
                patientDTO.setEmContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_num")));

            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return patientDTO;
    }

    public static PatientDTO getPatientDetailsByPatientUUID(String patientUuid) {
        PatientDTO patientDTO = null;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String patientSelection = "uuid = ?";
        String[] patientArgs = {patientUuid};
        String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name", "gender",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw",
                "patient_photo"};
        Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patientDTO = new PatientDTO();
                patientDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                patientDTO.setOpenmrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                patientDTO.setFirstname(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patientDTO.setMiddlename(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patientDTO.setLastname(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patientDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patientDTO.setDateofbirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patientDTO.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patientDTO.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patientDTO.setCityvillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patientDTO.setStateprovince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patientDTO.setPostalcode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patientDTO.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patientDTO.setPhonenumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patientDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patientDTO.setPatientPhoto(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = new PatientsDAO().getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    CustomLog.e(TAG, e.getMessage());
                }

                if (name.equalsIgnoreCase("caste")) {
                    patientDTO.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patientDTO.setPhonenumber(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patientDTO.setEducation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patientDTO.setEconomic(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patientDTO.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patientDTO.setSon_dau_wife(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("NationalID")) {
                    patientDTO.setNationalID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
//                if (name.equalsIgnoreCase("ProfileImageTimestamp")) {
//                    profileImage1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
//                }
                if (name.equalsIgnoreCase("createdDate")) {
                    patientDTO.setCreatedDate(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("providerUUID")) {
                    patientDTO.setProviderUUID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
        return patientDTO;
    }

    public PatientDTO retrievePatientDetails(Cursor cursor) {
        Timber.tag("PatientDao").d("retrievePatientDetails");
        PatientDTO patientDTO = new PatientDTO();
        PatientsDAO patientsDAO = new PatientsDAO();
        if (cursor.moveToFirst()) {
            do {
                Set<Attribute> attributes;
                try {
                    attributes = new HashSet<>(patientsDAO.getPatientAttributes(cursor.getString(cursor.getColumnIndexOrThrow("uuid"))));
                } catch (DAOException e) {
                    throw new RuntimeException(e);
                }

                patientDTO.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                patientDTO.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                patientDTO.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                patientDTO.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                patientDTO.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                patientDTO.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                patientDTO.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                patientDTO.setAddress1(cursor.getString(cursor.getColumnIndexOrThrow("address1")));
                patientDTO.setAddress2(cursor.getString(cursor.getColumnIndexOrThrow("address2")));
                patientDTO.setCityvillage(cursor.getString(cursor.getColumnIndexOrThrow("city_village")));
                patientDTO.setStateprovince(cursor.getString(cursor.getColumnIndexOrThrow("state_province")));
                patientDTO.setPostalcode(cursor.getString(cursor.getColumnIndexOrThrow("postal_code")));
                patientDTO.setCountry(cursor.getString(cursor.getColumnIndexOrThrow("country")));
                patientDTO.setPhonenumber(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                patientDTO.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                patientDTO.setPatientPhoto(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                patientDTO.setGuardianType(cursor.getString(cursor.getColumnIndexOrThrow("guardian_type")));
                patientDTO.setGuardianName(cursor.getString(cursor.getColumnIndexOrThrow("guardian_name")));
                patientDTO.setContactType(cursor.getString(cursor.getColumnIndexOrThrow("contact_type")));
                patientDTO.setEmContactName(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_name")));
                patientDTO.setEmContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_num")));
                patientDTO.setAbhaNumber(cursor.getString(cursor.getColumnIndexOrThrow("abha_number")));
                patientDTO.setAbhaAddress(cursor.getString(cursor.getColumnIndexOrThrow("abha_address")));

                // Attributes
                patientDTO.setPhonenumber(cursor.getString(cursor.getColumnIndexOrThrow("telephone")));
                patientDTO.setEconomic(cursor.getString(cursor.getColumnIndexOrThrow("economicStatus")));
                patientDTO.setEducation(cursor.getString(cursor.getColumnIndexOrThrow("educationLevel")));
                patientDTO.setProviderUUID(cursor.getString(cursor.getColumnIndexOrThrow("provider")));
                patientDTO.setOccupation(cursor.getString(cursor.getColumnIndexOrThrow("occupation")));
                patientDTO.setSon_dau_wife(cursor.getString(cursor.getColumnIndexOrThrow("sdw")));
                patientDTO.setNationalID(cursor.getString(cursor.getColumnIndexOrThrow("nationalId")));
                patientDTO.setProfileTimestamp(cursor.getString(cursor.getColumnIndexOrThrow("profileImageTimestamp")));
                patientDTO.setCaste(cursor.getString(cursor.getColumnIndexOrThrow("caste")));
                patientDTO.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow("createdDate")));

                patientDTO.setTmhCaseNumber(cursor.getString(cursor.getColumnIndexOrThrow("tmhCaseNumber")));
                patientDTO.setRequestId(cursor.getString(cursor.getColumnIndexOrThrow("requestId")));
                patientDTO.setDiscipline(cursor.getString(cursor.getColumnIndexOrThrow("discipline")));
                patientDTO.setRelativePhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("relativePhoneNumber")));
                patientDTO.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow("department")));

                patientDTO.setProvince(cursor.getString(cursor.getColumnIndexOrThrow("provinces")));
                patientDTO.setCity(cursor.getString(cursor.getColumnIndexOrThrow("cities")));
                patientDTO.setRegistrationAddressOfHf(cursor.getString(cursor.getColumnIndexOrThrow("registrationAddressOfHf")));
                patientDTO.setInn(cursor.getString(cursor.getColumnIndexOrThrow("inn")));
                patientDTO.setCodeOfHealthFacility(cursor.getString(cursor.getColumnIndexOrThrow("codeOfHealthFacility")));
                patientDTO.setHealthFacilityName(cursor.getString(cursor.getColumnIndexOrThrow("healthFacilityName")));
                patientDTO.setCodeOfDepartment(cursor.getString(cursor.getColumnIndexOrThrow("codeOfDepartment")));
                patientDTO.setHouseholdLinkingUUIDlinking(cursor.getString(cursor.getColumnIndexOrThrow("HouseHold")));

                patientDTO.setAddress3(cursor.getString(cursor.getColumnIndexOrThrow("address3")));
                patientDTO.setAddress6(cursor.getString(cursor.getColumnIndexOrThrow("address6")));
                patientDTO.setDistrict(cursor.getString(cursor.getColumnIndexOrThrow("countyDistrict")));

            } while (cursor.moveToNext());
        }
        cursor.close();
        return patientDTO;
    }

    //getting followup patient count here
    public static Observable<Integer> getAllFollowupPatientCount() {
        return  Observable.fromCallable(()->{
            int count = 0;

            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

            String query = "SELECT " +
                    "a.uuid as visituuid, " +
                    "a.sync, " +
                    "a.patientuuid, " +
                    "substr(a.startdate, 1, 10) as startdate, "
                    + "DATE(CASE WHEN substr(o.value, 1, 10) LIKE '__-__-____' THEN DATE(SUBSTR(substr(o.value, 1, 10),7,4) || '-' || SUBSTR(substr(o.value, 1, 10),4,2) || '-' || SUBSTR(substr(o.value, 1, 10),1,2)) " +
                    "WHEN substr(o.value, 1, 10) LIKE '____-__-__' THEN substr(o.value, 1, 10) END) as followup_date, " +
                    "o.value as follow_up_info," +
                    "b.patient_photo, " +
                    "a.enddate, b.uuid, " +
                    "b.first_name, " +
                    "b.middle_name, " +
                    "b.last_name, " +
                    "b.date_of_birth, " +
                    "b.openmrs_id, " +
                    "b.gender, " +
                    "c.value AS speciality, " +
                    "SUBSTR(o.value,1,10) AS value_text, " +
                    "MAX(o.obsservermodifieddate) AS obsservermodifieddate " +
                    "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c " +
                    "WHERE " +
                    "a.uuid = c.visit_uuid " +
                    "AND a.patientuuid = b.uuid " +
                    "AND a.uuid = d.visituuid " +
                    "AND d.uuid = o.encounteruuid " +
                    "AND o.conceptuuid = ? " +
                    "AND o.voided='0' " +
                    "AND o.value is NOT NULL " +
                    "AND followup_date is NOT NULL " +
                    "AND a.enddate IS NULL " + // changed for visit removed but count is not dec
                    "GROUP BY a.patientuuid " +
                    "HAVING (value_text is NOT NULL AND LOWER(value_text) != 'no' " +
                    "AND value_text != '' ) ";

            CustomLog.d("QUERY_COUNT", query);

            final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
            if (cursor.moveToFirst()) {
                do {
                    try {
                        String value_text = cursor.getString(cursor.getColumnIndexOrThrow("value_text"));
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        CustomLog.e(TAG, e.getMessage());
                    }
                }
                while (cursor.moveToNext());
            }
            cursor.close();

            return count;
        });
    }

    public boolean updatePatientSurveyInDb(String uuid, List<PatientAttributesDTO> patientAttributesDTOS) throws DAOException {
        Log.d("devKZchk", "updatePatientSurveyInDb: kz attrs : " + new Gson().toJson(patientAttributesDTOS));
        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String whereclause = "Uuid=?";
        db.beginTransaction();
        try {
            Logger.logD("devKZchk", "create has to happen");
            values.put("uuid", uuid);
            values.put("dead", false);
            values.put("sync", false);

            insertPatientAttributes(patientAttributesDTOS, db);
            Logger.logD("devKZchk", "datadumper" + values);

            createdRecordsCount1 = db.update("tbl_patient", values, whereclause, new String[]{uuid});
            db.setTransactionSuccessful();
            Logger.logD("devKZchk", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;

    }

    // Update patient sync = false.
    public boolean updatePatientSyncValue(String patientUUID) throws DAOException {
        Log.d(TAG, "patientUUID: " + patientUUID);
        boolean isCreated = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String whereClause = "uuid=?";
        db.beginTransaction();
        try {
            values.put("sync", false);
            db.update("tbl_patient", values, whereClause, new String[]{patientUUID});
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;

    }

    public HouseholdSurveyModel retrievePatientHouseholdSurveyAttributes(String patientUuid) {
        Timber.tag("devkz").d("retrievePatientHouseholdSurveyAttributes");
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        HouseholdSurveyModel householdSurveyModel = new HouseholdSurveyModel();
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor cursor = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, "retrievePatientHouseholdSurveyAttributes: householdSurveyModel if blk: " + new Gson().toJson(householdSurveyModel));

                // Attributes
                householdSurveyModel.setHouseStructure(cursor.getString(cursor.getColumnIndexOrThrow("HouseStructure")));
                householdSurveyModel.setReportDateOfSurveyStarted(cursor.getString(cursor.getColumnIndexOrThrow("ResultOfVisit")));
                householdSurveyModel.setHouseholdNumberOfSurvey(cursor.getString(cursor.getColumnIndexOrThrow("HouseholdNumber")));
                householdSurveyModel.setNamePrimaryRespondent(cursor.getString(cursor.getColumnIndexOrThrow("NamePrimaryRespondent")));
                //householdSurveyModel.setReportDateOfSurveyStarted(cursor.getString(cursor.getColumnIndexOrThrow("occupation")));
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "retrievePatientHouseholdSurveyAttributes: householdSurveyModel : " + new Gson().toJson(householdSurveyModel));
        cursor.close();
        return householdSurveyModel;
    }

    /* @Override
     String tableName() {
         return "tbl_patient";
     }*/
    public void setTableName(String tableName) {
        Log.d(TAG, "setTableName: tableName  : "+tableName);
        this.currentTableName = tableName;
    }

    @Override
    String tableName() {
        if (currentTableName == null || currentTableName.isEmpty()) {
            throw new RuntimeException("Table name is not set");
        }
        Log.d(TAG, "tableName: currentTableName : "+currentTableName);
        return currentTableName;
    }

    public boolean patinetAttributeMaster(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTOS) throws DAOException {
        setTableName("tbl_patient_attribute_master");
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

    public HashMap<String, Object> createPatientAttributesMap(PatientAttributesDTO patientAttributesDTO) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("uuid", patientAttributesDTO.getUuid());
        values.put("person_attribute_type_uuid", patientAttributesDTO.getPersonAttributeTypeUuid());
        values.put("patientuuid", patientAttributesDTO.getPatientuuid());
        values.put("value", patientAttributesDTO.getValue());
        values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
        values.put("sync", "TRUE");
        return values;
    }
    public String getPatientNameByPatientUuid(String patientUuid) throws DAOException {
        String patientName = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT first_name,middle_name,last_name FROM tbl_patient where uuid = ? COLLATE NOCASE", new String[]{patientUuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    patientName = cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " + cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
                }
            }
            cursor.close();
        } catch (SQLException s) {
            throw new DAOException(s);
        }
        return patientName;
    }

    /**
     * Whether the known HMIS patient (by openmrs_id) is already linked locally to [abhaAddress].
     * Lets the ABDM flow skip a redundant server identifier update.
     */
    public boolean isPatientExistWithAbhaAddress(String patientOpenMRSId, String abhaAddress) {
        boolean exists = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) as count FROM tbl_patient WHERE openmrs_id = ? AND abha_address LIKE ? COLLATE NOCASE",
                new String[]{patientOpenMRSId, "%" + abhaAddress + "%"})) {
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(cursor.getColumnIndexOrThrow("count")) > 0;
            }
        } catch (SQLException s) {
            CustomLog.e(TAG, s.getMessage());
        }
        return exists;
    }

    /**
     * Links ABHA identifiers to an existing local patient (by uuid) and marks the row unsynced so
     * the next push sends them as patient identifiers.
     */
    public boolean updatePatientAbha(String uuid, String abhaNumber, String abhaAddress) throws DAOException {
        boolean isUpdated = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            values.put("abha_number", abhaNumber);
            values.put("abha_address", abhaAddress);
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", false);
            db.update("tbl_patient", values, "uuid=?", new String[]{uuid});
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isUpdated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isUpdated;
    }

    /**
     * Finds the local patient to reconcile against during ABHA verification, matched by ABHA number.
     * Most-recently-modified wins. Returns null when no local copy exists.
     */
    public PatientDTO getPatientForComparisonByAbhaNumber(String abhaNumber) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM tbl_patient WHERE abha_number LIKE ? AND (voided = '0' OR voided IS NULL) ORDER BY modified_date DESC LIMIT 1",
                new String[]{"%" + abhaNumber + "%"})) {
            if (cursor.moveToFirst()) {
                return cursorToComparisonDTO(cursor);
            }
        } catch (SQLException s) {
            CustomLog.e(TAG, s.getMessage());
        }
        return null;
    }

    /**
     * Fallback lookup for the ABHA verify to compare flow when no row matches by ABHA number.
     * Most-recently-modified wins. Returns null when no local copy exists.
     */
    public PatientDTO getPatientForComparisonByPhone(String phoneNumber) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM tbl_patient WHERE phone_number = ? AND (voided = '0' OR voided IS NULL) ORDER BY modified_date DESC LIMIT 1",
                new String[]{phoneNumber})) {
            if (cursor.moveToFirst()) {
                return cursorToComparisonDTO(cursor);
            }
        } catch (SQLException s) {
            CustomLog.e(TAG, s.getMessage());
        }
        return null;
    }

    /**
     * Whether a local patient already exists for an ABHA account shown in the multiple-accounts
     * picker, driving the "Registered / Not registered with Intelehealth" status line.
     */
    public boolean isPatientRegisteredLocally(String abhaNumberLastFour, String firstName, String lastName) {
        boolean registered = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT uuid FROM tbl_patient WHERE abha_number LIKE ? AND first_name = ? AND last_name = ? AND (voided = '0' OR voided IS NULL) LIMIT 1",
                new String[]{"%" + abhaNumberLastFour + "%", firstName, lastName})) {
            registered = cursor.moveToFirst();
        } catch (SQLException s) {
            CustomLog.e(TAG, s.getMessage());
        }
        return registered;
    }

    /**
     * Persists the merged record chosen on the ABHA compare screen back to the local patient row
     * and marks it unsynced. address2, address3 and address6 are deliberately left untouched: the
     * ABHA address carries no reliable equivalent, so overwriting them would lose local data.
     */
    public boolean updatePatientAfterAbhaComparison(String uuid, String firstName, String lastName,
                                                    String dateOfBirth, String gender,
                                                    String address1, String cityVillage,
                                                    String countyDistrict, String stateProvince,
                                                    String pinCode, String phone,
                                                    String abhaNumber, String abhaAddress) throws DAOException {
        boolean isUpdated = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            values.put("first_name", firstName);
            values.put("last_name", lastName);
            values.put("date_of_birth", dateOfBirth);
            values.put("gender", gender);
            values.put("address1", address1);
            values.put("city_village", cityVillage);
            values.put("countyDistrict", countyDistrict);
            values.put("state_province", stateProvince);
            values.put("postal_code", pinCode);
            values.put("phone_number", phone);
            values.put("abha_number", abhaNumber);
            values.put("abha_address", abhaAddress);
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", false);
            db.update("tbl_patient", values, "uuid=?", new String[]{uuid});
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isUpdated = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isUpdated;
    }

    /**
     * The stored ABHA number for a patient, used to decide whether the ABHA card button is shown
     * and to locate the cached card image. Returns null when the patient has no ABHA linked.
     */
    public String getAbhaNumberByUuid(String uuid) {
        String abhaNumber = null;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT abha_number FROM tbl_patient WHERE uuid = ? LIMIT 1",
                new String[]{uuid})) {
            if (cursor.moveToFirst()) {
                abhaNumber = cursor.getString(0);
            }
        } catch (SQLException s) {
            CustomLog.e(TAG, s.getMessage());
        }
        return abhaNumber;
    }

    /**
     * The patient's stored ABHA address, which may be a comma-separated list of every address
     * registered for them on our server. Position 0 is the one currently in use.
     */
    public String getPatientAbhaAddressByUuid(String uuid) {
        String abhaAddress = null;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT abha_address FROM tbl_patient WHERE uuid = ? LIMIT 1",
                new String[]{uuid})) {
            if (cursor.moveToFirst()) {
                abhaAddress = cursor.getString(0);
            }
        } catch (SQLException s) {
            CustomLog.e(TAG, s.getMessage());
        }
        return abhaAddress;
    }

    /**
     * Builds the patient row used by the ABHA compare screen. Both city_village and countyDistrict
     * are populated so PatientDTO's village/district accessors normalise legacy rows that still
     * encode "district:village" in the single column.
     */
    private PatientDTO cursorToComparisonDTO(Cursor cursor) {
        PatientDTO dto = new PatientDTO();
        dto.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
        dto.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
        dto.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
        dto.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
        dto.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
        dto.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
        dto.setAddress1(cursor.getString(cursor.getColumnIndexOrThrow("address1")));
        dto.setAddress2(cursor.getString(cursor.getColumnIndexOrThrow("address2")));
        dto.setCityvillage(cursor.getString(cursor.getColumnIndexOrThrow("city_village")));
        dto.setDistrict(cursor.getString(cursor.getColumnIndexOrThrow("countyDistrict")));
        dto.setStateprovince(cursor.getString(cursor.getColumnIndexOrThrow("state_province")));
        dto.setPostalcode(cursor.getString(cursor.getColumnIndexOrThrow("postal_code")));
        dto.setPhonenumber(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
        dto.setAbhaNumber(cursor.getString(cursor.getColumnIndexOrThrow("abha_number")));
        dto.setAbhaAddress(cursor.getString(cursor.getColumnIndexOrThrow("abha_address")));
        return dto;
    }

}

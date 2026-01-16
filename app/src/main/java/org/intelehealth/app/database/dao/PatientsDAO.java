package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.app.models.ActivePatientModel;
import org.intelehealth.app.ui.baseline_survey.constants.Constants;
import org.intelehealth.app.utilities.CustomLog;


import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.intelehealth.app.enums.FollowupFilterTypeEnum;
import org.intelehealth.app.models.FamilyMemberRes;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.services.MyIntentService;
import org.intelehealth.app.utilities.CustomLog;
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

public class PatientsDAO {

    private int updatecount = 0;
    private long createdRecordsCount = 0;
    int limit = 10, offset = 0;
    private static final String TAG = "PatientsDAO";


    public boolean insertPatients(List<PatientDTO> patientDTO) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (PatientDTO patient : patientDTO) {
                createPatients(patient, db);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isInserted;
    }

    private static final int BATCH_SIZE = 50;

    /**
     * Insert patients in batches
     *
     * @param patients
     * @return
     * @throws DAOException
     */
    public boolean insertPatientsV2(List<PatientDTO> patients) throws DAOException {

        SQLiteDatabase db = null;
        try {
            db = IntelehealthApplication
                    .inteleHealthDatabaseHelper
                    .getWriteDb();

            ContentValues values = new ContentValues();

            for (int i = 0; i < patients.size(); i++) {

                if (i % BATCH_SIZE == 0) {
                    db.beginTransaction();
                }

                values.clear();
                bindPatientValues(values, patients.get(i));
                db.insertWithOnConflict(
                        "tbl_patient",
                        null,
                        values,
                        //SQLiteDatabase.CONFLICT_IGNORE
                        SQLiteDatabase.CONFLICT_REPLACE
                );

                if (i % BATCH_SIZE == BATCH_SIZE - 1 || i == patients.size() - 1) {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            }

            return true;

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    /**
     * Bind patient values to ContentValues
     *
     * @param values
     * @param patient
     */
    private void bindPatientValues(ContentValues values, PatientDTO patient) {

        values.put("uuid", patient.getUuid());
        values.put("openmrs_id", patient.getOpenmrsId());
        values.put("first_name", patient.getFirstname());
        values.put("middle_name", patient.getMiddlename());
        values.put("last_name", patient.getLastname());
        values.put("address1", patient.getAddress1());
        values.put("address2", patient.getAddress2());
        values.put("country", patient.getCountry());
        values.put(
                "date_of_birth",
                DateAndTimeUtils.formatDateFromOnetoAnother(
                        patient.getDateofbirth(),
                        "MMM dd, yyyy hh:mm:ss a",
                        "yyyy-MM-dd"
                )
        );
        values.put("gender", patient.getGender());
        values.put("postal_code", patient.getPostalcode());
        values.put("state_province", patient.getStateprovince());
        values.put("city_village", patient.getCityvillage());
        values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
        values.put("dead", patient.getDead());
        values.put("sync", patient.getSyncd());
    }

    public boolean createPatients(PatientDTO patient, SQLiteDatabase db) throws DAOException {
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

//            values.put("guardian_type", patient.getGuardianType());
//            values.put("guardian_name", patient.getGuardianName());
//            values.put("contact_type", patient.getContactType());
//            values.put("em_contact_name", patient.getEmContactName());
//            values.put("em_contact_num", patient.getEmContactNumber());

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
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

//            values.put("guardian_type", patientDTO.getGuardianType());
//            values.put("guardian_name", patientDTO.getGuardianName());
//            values.put("contact_type", patientDTO.getContactType());
//            values.put("em_contact_name", patientDTO.getEmContactName());
//            values.put("em_contact_num", patientDTO.getEmContactNumber());

            values.put("dead", patientDTO.getDead());
            values.put("sync", false);
            patientAttributesList = patientDTO.getPatientAttributesDTOList();
            if (patientAttributesList != null)
                insertPatientAttributes(patientAttributesList);
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

//            values.put("guardian_type", patientDTO.getGuardianType());
//            values.put("guardian_name", patientDTO.getGuardianName());
//            values.put("contact_type", patientDTO.getContactType());
//            values.put("em_contact_name", patientDTO.getEmContactName());
//            values.put("em_contact_num", patientDTO.getEmContactNumber());

            values.put("dead", false);
            values.put("sync", false);

            insertPatientAttributes(patientDTO.getPatientAttributesDTOList());
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

            insertPatientAttributes(patientAttributesDTOS);
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
        }
        return isInserted;
    }


    /**
     * Insert patient attributes in batches
     *
     * @param list
     * @return
     * @throws DAOException
     */
    public boolean patientAttributesV2(List<PatientAttributesDTO> list) throws DAOException {

        SQLiteDatabase db = null;
        try {
            db = IntelehealthApplication
                    .inteleHealthDatabaseHelper
                    .getWriteDb();

            ContentValues values = new ContentValues();

            for (int i = 0; i < list.size(); i++) {

                if (i % BATCH_SIZE == 0) {
                    db.beginTransaction();
                }

                values.clear();
                PatientAttributesDTO dto = list.get(i);

                values.put("uuid", dto.getUuid());
                values.put("person_attribute_type_uuid", dto.getPersonAttributeTypeUuid());
                values.put("patientuuid", dto.getPatientuuid());
                values.put("value", dto.getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", "TRUE");

                db.insertWithOnConflict(
                        "tbl_patient_attribute",
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );

                if (i % BATCH_SIZE == BATCH_SIZE - 1 || i == list.size() - 1) {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            }

            return true;

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }


    public List<Attribute> getPatientAttributes(String patientuuid) throws DAOException {
        List<Attribute> patientAttributesList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        //db.beginTransaction();
        try {
            String query = "SELECT * from tbl_patient_attribute WHERE patientuuid= '" + patientuuid + "'";
            Cursor cursor = db.rawQuery(query, null, null);
            Attribute attribute = new Attribute();
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    attribute = new Attribute();
                    attribute.setAttributeType(cursor.getString(cursor.getColumnIndex("person_attribute_type_uuid")));
                    attribute.setValue(cursor.getString(cursor.getColumnIndex("value")));
                    patientAttributesList.add(attribute);
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

    public List<Attribute> getPatientAttributesForBaseline(String patientuuid) throws DAOException {
        String queryString = "SELECT * FROM tbl_patient_attribute a WHERE a.patientuuid = ? AND a.value IS NOT NULL AND a.modified_date = (" +
                "SELECT MAX(b.modified_date) FROM tbl_patient_attribute b WHERE b.patientuuid = a.patientuuid " +
                "AND b.person_attribute_type_uuid = a.person_attribute_type_uuid)";

        List<Attribute> patientAttributesList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        try {
            String query = "SELECT * from tbl_patient_attribute WHERE patientuuid = ? AND value IS NOT NULL ORDER BY modified_date DESC LIMIT 1";
            Cursor cursor = db.rawQuery(queryString, new String[]{patientuuid}, null);
            Attribute attribute = new Attribute();
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    attribute = new Attribute();
                    attribute.setAttributeType(cursor.getString(cursor.getColumnIndex("person_attribute_type_uuid")));
                    attribute.setValue(cursor.getString(cursor.getColumnIndex("value")));
                    patientAttributesList.add(attribute);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        } catch (SQLException e) {
            CustomLog.e(TAG, e.getMessage());
            throw new DAOException(e.getMessage());
        }

        return patientAttributesList;
    }

    //Fetch householdID value using Patient UUID
    public String getHouseHoldValue(String patientuuid) throws DAOException {
        String houseHoldID = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid=? AND voided='0' COLLATE NOCASE", new String[]{patientuuid, "10720d1a-1471-431b-be28-285d64767093"});

            if (idCursor.getCount() != 0) {
                if (idCursor.moveToLast()) {
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT patientuuid FROM tbl_patient_attribute where value = ? AND sync='0' COLLATE NOCASE", new String[]{houseHoldValue});

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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        //db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT openmrs_id,first_name,middle_name,last_name FROM tbl_patient where uuid = ? COLLATE NOCASE", new String[]{patientuuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
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

    public List<String> getFamilyMemberIDS(String houseHoldValue, String patientUUID) throws DAOException {
        List<String> subMemberIdList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        try {
            Cursor cursor = db.rawQuery("SELECT patientuuid FROM tbl_patient_attribute where value = ? COLLATE NOCASE", new String[]{houseHoldValue});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    String pid = cursor.getString(cursor.getColumnIndexOrThrow("patientuuid"));
                    if (!Objects.equals(pid, patientUUID)) subMemberIdList.add(pid);
                }
            }
            cursor.close();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            CustomLog.e(TAG, s.getMessage());
            throw new DAOException(s);
        }
        return subMemberIdList;
    }

    public List<FamilyMemberRes> getFamilyMembers(List<String> patientuuids) throws DAOException {
        List<FamilyMemberRes> listPatientNames = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        StringBuilder queryBuilder = new StringBuilder("SELECT openmrs_id, uuid, first_name, middle_name, last_name FROM tbl_patient WHERE uuid IN (");
        String[] queryArgs = new String[patientuuids.size()];

        for (int i = 0; i < patientuuids.size(); i++) {
            queryBuilder.append("?");
            if (i < patientuuids.size() - 1) {
                queryBuilder.append(", ");
            }
            queryArgs[i] = patientuuids.get(i);
        }
        queryBuilder.append(") COLLATE NOCASE");

        try {
            Cursor cursor = db.rawQuery(queryBuilder.toString(), queryArgs);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    FamilyMemberRes familyMemberRes = new FamilyMemberRes();
                    familyMemberRes.setOpenMRSID(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    familyMemberRes.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                    familyMemberRes.setName(cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " +
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    listPatientNames.add(familyMemberRes);
                }
            }
            cursor.close();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            CustomLog.e(TAG, s.getMessage());
            throw new DAOException(s);
        }
        return listPatientNames;
    }


    public String getAttributesName(String attributeuuid) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
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


    public boolean patinetAttributeMaster(List<PatientAttributeTypeMasterDTO> patientAttributeTypeMasterDTOS) throws DAOException {
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

    /**
     *
     * @param list
     * @return
     * @throws DAOException
     */
    public boolean patientAttributeMasterV2(List<PatientAttributeTypeMasterDTO> list) throws DAOException {

        SQLiteDatabase db = null;
        try {
            db = IntelehealthApplication
                    .inteleHealthDatabaseHelper
                    .getWriteDb();

            ContentValues values = new ContentValues();
            String modifiedDate = AppConstants.dateAndTimeUtils.currentDateTime();

            for (int i = 0; i < list.size(); i++) {

                if (i % BATCH_SIZE == 0) {
                    db.beginTransaction();
                }

                values.clear();
                PatientAttributeTypeMasterDTO dto = list.get(i);

                values.put("uuid", dto.getUuid());
                values.put("name", dto.getName());
                values.put("modified_date", modifiedDate);
                values.put("sync", "TRUE");

                db.insertWithOnConflict(
                        "tbl_patient_attribute_master",
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                );

                if (i % BATCH_SIZE == BATCH_SIZE - 1 || i == list.size() - 1) {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            }

            return true;

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e.getMessage(), e);
        }
    }


    public String getUuidForAttribute(String attr) {
        String attributeUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
//                    patientDTO.setGuardianType(idCursor.getString(idCursor.getColumnIndexOrThrow("guardian_type")));
//                    patientDTO.setGuardianName(idCursor.getString(idCursor.getColumnIndexOrThrow("guardian_name")));
//                    patientDTO.setContactType(idCursor.getString(idCursor.getColumnIndexOrThrow("contact_type")));
//                    patientDTO.setEmContactName(idCursor.getString(idCursor.getColumnIndexOrThrow("em_contact_name")));
//                    patientDTO.setEmContactNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("em_contact_num")));
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

    public static String fetchDateOfBirth(String patientUuid) {
        String dob = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.query("tbl_patient", new String[]{"date_of_birth"}, "uuid=?",
                new String[]{patientUuid}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return dob;
    }

    public static List<PatientDTO> getAllPatientsFromDB(int limit, int offset) {
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        String table = "tbl_patient";
        final Cursor searchCursor = db.rawQuery("SELECT uuid, first_name, last_name, date_of_birth, gender, patient_photo FROM " + table + " ORDER BY modified_date DESC limit ? offset ?", new String[]{String.valueOf(limit), String.valueOf(offset)});
        try {
            if (searchCursor.moveToFirst()) {
                do {
                    PatientDTO model = new PatientDTO();
                    model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                    model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                    model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                    model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                    model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                    modelList.add(model);
                } while (searchCursor.moveToNext());
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }

        searchCursor.close();
        return modelList;
    }

    public static List<PatientDTO> getQueryPatients(String query) {
        List<PatientDTO> modelList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        String table = "tbl_patient";
        List<String> patientUUID_List = new ArrayList<>();

        String phoneNumberSearchQuery = "%" + query + "%";
        final Cursor search_mobile_cursor = db.rawQuery("SELECT DISTINCT patientuuid FROM tbl_patient_attribute WHERE value LIKE ?", new String[]{phoneNumberSearchQuery});
        /* DISTINCT will get remove the duplicate values. The duplicate value will come when you have created
         * a patient with mobile no. 12345 and patient is pushed than later you edit the mobile no to
         * 12344 or something. In this case, the local db maintains two separate rows both with value: 12344 */
        //if no data is present against that corresponding cursor than cursor count returns = 0 ... i.e cursor_count = 0 ...
        try {
            if (search_mobile_cursor.moveToFirst()) {
                do {
                    patientUUID_List.add(search_mobile_cursor.getString(search_mobile_cursor.getColumnIndexOrThrow("patientuuid")));
                }
                while (search_mobile_cursor.moveToNext());
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }
        search_mobile_cursor.close();

        if (!patientUUID_List.isEmpty()) {
            for (int i = 0; i < patientUUID_List.size(); i++) {
                final Cursor searchCursor = db.rawQuery("SELECT uuid, first_name, last_name, date_of_birth, gender, patient_photo FROM " + table + " WHERE uuid = ? ", new String[]{patientUUID_List.get(i)});
                try {
                    if (searchCursor.moveToFirst()) {
                        do {
                            PatientDTO model = new PatientDTO();
                            model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                            model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                            model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                            model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                            model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                            modelList.add(model);
                        } while (searchCursor.moveToNext());
                    }
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    CustomLog.e(TAG, e.getMessage());
                }
                searchCursor.close();
            }
        } else { // no mobile number was added in search text.
            String searchQueryString = "%" + query + "%";
            final Cursor searchCursor = db.rawQuery("SELECT uuid, first_name, last_name, date_of_birth, gender, patient_photo FROM " + table +
                            " WHERE first_name LIKE ? OR middle_name LIKE ? OR last_name LIKE ? " +
                            " OR (first_name || middle_name) LIKE ? OR (middle_name || last_name) LIKE ? OR (first_name || last_name) LIKE ? " +
                            " OR (first_name || ' ' || middle_name) LIKE ? OR (middle_name || ' ' || last_name) LIKE ? OR (first_name || ' ' || last_name)" +
                            " OR (first_name || ' ' || middle_name || ' ' || last_name)" +
                            " OR openmrs_id LIKE ? ORDER BY first_name ASC ",
                    new String[]{searchQueryString, searchQueryString, searchQueryString, searchQueryString, searchQueryString, searchQueryString, searchQueryString, searchQueryString, searchQueryString});
            try {
                if (searchCursor.moveToFirst()) {
                    do {
                        PatientDTO model = new PatientDTO();
                        model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                        model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                        model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                        model.setGender(searchCursor.getString(searchCursor.getColumnIndexOrThrow("gender")));
                        model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
                        model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
                        model.setPatientPhoto(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_photo")));
                        modelList.add(model);
                    } while (searchCursor.moveToNext());
                }
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                CustomLog.e(TAG, e.getMessage());
            }
            searchCursor.close();
        }
        return modelList;
    }

    public static String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7'", new String[]{patientuuid});
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor = db.rawQuery("SELECT uuid, startdate, enddate, sync FROM tbl_visit WHERE patientuuid = ?", new String[]{patientUUID});
        try {
            if (idCursor.moveToFirst()) {
                do {
                    visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                    visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
                    String isSynced = idCursor.getString(idCursor.getColumnIndexOrThrow("sync"));
                    boolean sync = false;
                    if (isSynced != null) {
                        if (isSynced.equalsIgnoreCase("0") || isSynced.equalsIgnoreCase("false"))
                            sync = false;
                        else if (isSynced.equalsIgnoreCase("1") || isSynced.equalsIgnoreCase("true"))
                            sync = true;
                    }
                    CustomLog.d("TAG", "isVisitPresentForPatient_fetchVisitValues: " + sync);
                    visitDTO.setSyncd(sync);
                    CustomLog.d("TAG", "isVisitPresentForPatient_fetchVisitValues: visit: " + visitDTO);
                }
                while (idCursor.moveToNext());
            }
        } catch (SQLException e) {
            CustomLog.e(TAG, e.getMessage());

        }

        idCursor.close();
        return visitDTO;
    }

    public static String[] getPatientDobAgeGender(String patientUuid) {
        String[] result = new String[0];
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

    public String getPatientDob(String patientUuid) {
        String dateOfBirth = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT date_of_birth FROM tbl_patient WHERE uuid = ?", new String[]{patientUuid});
        if (cursor.moveToFirst()) {
            do {
                dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dateOfBirth;
    }

    public static String[] getPatientsPhoneNumber(String patientUuid) {
        String[] result = new String[0];
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
//                patientDTO.setGuardianType(cursor.getString(cursor.getColumnIndexOrThrow("guardian_type")));
//                patientDTO.setGuardianName(cursor.getString(cursor.getColumnIndexOrThrow("guardian_name")));
//                patientDTO.setContactType(cursor.getString(cursor.getColumnIndexOrThrow("contact_type")));
//                patientDTO.setEmContactName(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_name")));
//                patientDTO.setEmContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_num")));

            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return patientDTO;
    }

    public static PatientDTO getPatientDetailsByPatientUUID(String patientUuid) {
        PatientDTO patientDTO = null;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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
        if (cursor.moveToFirst()) {
            do {
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
//                patientDTO.setGuardianType(cursor.getString(cursor.getColumnIndexOrThrow("guardian_type")));
//                patientDTO.setGuardianName(cursor.getString(cursor.getColumnIndexOrThrow("guardian_name")));
//                patientDTO.setContactType(cursor.getString(cursor.getColumnIndexOrThrow("contact_type")));
//                patientDTO.setEmContactName(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_name")));
//                patientDTO.setEmContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("em_contact_num")));

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
            } while (cursor.moveToNext());
        }
        cursor.close();
        return patientDTO;
    }

    //getting followup patient count here
    public static Observable<Integer> getAllFollowupPatientCount() {
        return Observable.fromCallable(() -> {
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
                    "AND (select uuid from tbl_visit where patientuuid = b.uuid and (sync = '1' OR sync='true') order by startdate desc limit 1) = a.uuid " + // checking is there new visits or not, if yes, not showing the follow-up item
                    "AND a.patientuuid = b.uuid " +
                    "AND a.uuid = d.visituuid " +
                    "AND d.uuid = o.encounteruuid " +
                    "AND o.conceptuuid = ? " +
                    "AND o.voided='0' " +
                    "AND o.value is NOT NULL " +
                    "AND followup_date is NOT NULL " +
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

    public boolean checkIfBaselineSurveyCompleted(String patientId) {
        boolean isBaselineSurveyCompleted = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        String query = "SELECT * FROM tbl_patient_attribute WHERE patientuuid = ? AND person_attribute_type_uuid = ?";

        final Cursor cursor = db.rawQuery(query, new String[]{patientId, "7bc0540f-6bcf-4fdd-a0c5-4068a3c922f9"});
        if (cursor.moveToFirst()) {
            isBaselineSurveyCompleted = true;
        }

        cursor.close();
        return isBaselineSurveyCompleted;
    }

    public void updatePatientSync(boolean value, String patientId) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        ContentValues values = new ContentValues();
        String whereClause = "uuid=?";
        String[] whereArgs = {patientId};

        db.beginTransaction();
        try {
            values.put("sync", value);
            db.update("tbl_patient", values, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (SQLException sqlException) {
            CustomLog.e("visit", "updated" + sqlException.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public static Patient getPatientDetailsForRedirection(String patientUuid) {
        Patient patient = new Patient();
        String[] columns = {"first_name", "last_name", "date_of_birth"};
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        Cursor cursor = db.query("tbl_patient", columns, "uuid=?", new String[]{patientUuid}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                patient.setDate_of_birth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                patient.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                patient.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        return patient;
    }

    //Fetch value using Patient UUID from tbl_patient_attrb table.
    public String getValueFromPatientAttrbTable(String patientuuid, String attributeUuid) throws DAOException {
        String houseHoldID = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT value FROM tbl_patient_attribute where patientuuid = ? " +
                            "AND person_attribute_type_uuid=? AND voided='0' COLLATE NOCASE",
                    new String[]{patientuuid, attributeUuid});

            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    houseHoldID = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));
                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }
        return houseHoldID;
    }

    public String getPatientAttributeByPatientUuid(String patientUuid, String attributeName) throws DAOException {
        String value = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor cursor = null;
        try {
            // Single query with JOIN
            String sql = "SELECT pa.value\n" +
                    "FROM tbl_patient_attribute pa\n" +
                    "INNER JOIN tbl_patient_attribute_master pam\n" +
                    "    ON pa.person_attribute_type_uuid = pam.uuid\n" +
                    "WHERE pa.patientuuid = ?\n" +
                    "  AND pam.name = ?\n" +
                    "  AND pa.voided = '0' COLLATE NOCASE\n" + " ORDER BY pa.rowid DESC " +
                    "LIMIT 1";
            cursor = db.rawQuery(sql, new String[]{patientUuid, attributeName});

            if (cursor.moveToLast()) {
                value = cursor.getString(cursor.getColumnIndexOrThrow("value"));
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            if (cursor != null) cursor.close();
            db.endTransaction();
        }
        return value;
    }

    public String fetchBaselineMedicalHistory(String patientId) {
        String medicalHistory = null;

        SQLiteDatabase db =
                IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();

        String sql = "SELECT value FROM tbl_patient_attribute a WHERE a.patientuuid = ? AND a.person_attribute_type_uuid = ? " +
                "AND a.value IS NOT NULL AND a.modified_date = (SELECT MAX(b.modified_date) FROM tbl_patient_attribute b " +
                "WHERE b.patientuuid = a.patientuuid AND b.person_attribute_type_uuid = a.person_attribute_type_uuid)";

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{patientId, UuidDictionary.OTHER_MEDICAL_HISTORY}
        )) {
            if (cursor.moveToFirst()) {
                medicalHistory =
                        cursor.getString(cursor.getColumnIndexOrThrow("value"));
            }
        }

        return medicalHistory;
    }
}

package org.intelehealth.ezazi.database.dao;

import static org.intelehealth.ezazi.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;
import static org.intelehealth.ezazi.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EncounterDAO {

    private static final String TAG = "EncounterDAO";

    private String tag = EncounterDAO.class.getSimpleName();
    private long createdRecordsCount = 0;

    public boolean insertEncounter(List<EncounterDTO> encounterDTOS) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (EncounterDTO encounter : encounterDTOS) {
                createEncounters(encounter, db);
            }
            db.setTransactionSuccessful();

        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }
        return isInserted;
    }

    private boolean createEncounters(EncounterDTO encounter, SQLiteDatabase db) throws DAOException {
        boolean isCreated = false;
        ContentValues values = new ContentValues();
        values.put("uuid", encounter.getUuid());
        values.put("visituuid", encounter.getVisituuid());
        values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
        values.put("provider_uuid", encounter.getProvideruuid());
        values.put("encounter_time", encounter.getEncounterTime());
        values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
        values.put("sync", encounter.getSyncd());
        values.put("voided", encounter.getVoided());
        values.put("privacynotice_value", encounter.getPrivacynotice_value());
        try {

            if (isEncounterAlreadyAvailable(encounter.getVisituuid(), encounter.getEncounterTypeUuid())) {
                db.update("tbl_encounter", values, "uuid = ?", new String[]{encounter.getUuid()});
                return true;
            } else {
                Log.d("VALUES:", "VALUES: " + values);
                createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }

    public boolean isEncounterAlreadyAvailable(String visitUUid, String encounterTypeUUID) {
        boolean flag = true;
        if (visitUUid == null || encounterTypeUUID == null) return false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid  = ? and encounter_type_uuid  = ? COLLATE NOCASE", new String[]{visitUUid, encounterTypeUUID});
        if (cursor == null || cursor.getCount() == 0) {
            flag = false;
        }
        cursor.close();
        return flag;
    }

    public boolean createEncountersToDB(EncounterDTO encounter) throws DAOException {
        boolean isCreated = false;
        Log.d(TAG, "createEncountersToDB:length: " + encounter.getEncounterTypeUuid().length());
        Log.d(TAG, "createEncountersToDB: getEncounterTypeUuid : " + encounter.getEncounterTypeUuid());
        if (encounter.getEncounterTypeUuid() != null && encounter.getEncounterTypeUuid().length() > 0) {
            SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("uuid", encounter.getUuid());
            values.put("visituuid", encounter.getVisituuid());
            values.put("encounter_time", encounter.getEncounterTime());
            values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
            values.put("provider_uuid", encounter.getProvideruuid());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", encounter.getSyncd());
            values.put("voided", encounter.getVoided());
            values.put("privacynotice_value", encounter.getPrivacynotice_value());

            if (isEncounterAlreadyAvailable(encounter.getVisituuid(), encounter.getEncounterTypeUuid())) {
                db.update("tbl_encounter", values, "uuid = ?", new String[]{encounter.getUuid()});
                db.setTransactionSuccessful();
                db.endTransaction();
                return true;
            } else {
                try {
                    createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                    if (createdRecordsCount != 0)
                        isCreated = true;
                    db.setTransactionSuccessful();

                } catch (SQLException e) {
                    isCreated = false;
                    throw new DAOException(e.getMessage(), e);

                } finally {
                    db.endTransaction();
                }
            }
        }

        return isCreated;
    }

    public String getEncounterTypeUuid(String attr) {
        String encounterTypeUuid = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_uuid_dictionary where name = ? COLLATE NOCASE", new String[]{attr});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                encounterTypeUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            }
        }
        cursor.close();

        return encounterTypeUuid;
    }

    public String getEncounterTypeNameByUUID(String uuid) {
        if (uuid == null) return "";
        String name = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT name FROM tbl_uuid_dictionary where uuid  = ? COLLATE NOCASE", new String[]{uuid});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            }
        }
        cursor.close();

        return name;
    }

    public String findCurrentStage(String visitUuid) {
        if (visitUuid == null) return "";
        String name = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT U.name FROM tbl_encounter E, tbl_uuid_dictionary U where E.visituuid = ? COLLATE NOCASE" +
                " and E.voided = '0'  and U.uuid = E.encounter_type_uuid ORDER BY U.name DESC LIMIT 1", new String[]{visitUuid});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            }
        }
        cursor.close();

        return name;
    }


    public List<EncounterDTO> unsyncedEncounters() {
        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        //Distinct keyword is used to remove all duplicate records.
/*
        Cursor idCursor = db.rawQuery("SELECT distinct a.uuid,a.visituuid,a.encounter_type_uuid,a.provider_uuid,a.encounter_time," +
                "a.voided,a.privacynotice_value FROM tbl_encounter a,tbl_obs b WHERE (a.sync = ? OR a.sync=?) AND " +
                "a.uuid = b.encounteruuid AND b.sync='false' AND b.voided='0' ", new String[]{"false", "0"});
*/

        Cursor idCursor = db.rawQuery("SELECT * from tbl_encounter where sync = ? OR sync = ?", new String[]{"0", "false"});

        EncounterDTO encounterDTO = new EncounterDTO();
        Log.d("RAINBOW: ", "RAINBOW: " + idCursor.getCount());
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                Log.d("ENCO", "ENCO_type: " + encounterDTO.getEncounterTypeUuid());

                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                Log.d("ENCO", "ENCO_PROV: " + idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                Log.d("ENCO", "ENCO_TIME: " + idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setVoided(idCursor.getInt(idCursor.getColumnIndexOrThrow("voided")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));
                encounterDTOList.add(encounterDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        Gson gson = new Gson();
        Log.d("ENC_GSON: ", "ENC_GSON: " + gson.toJson(encounterDTOList));
        return encounterDTOList;
    }

    public List<EncounterDTO> getAllEncounters() {
        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        // db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter", null);
        EncounterDTO encounterDTO = new EncounterDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setVoided(idCursor.getInt(idCursor.getColumnIndexOrThrow("voided")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));
                encounterDTOList.add(encounterDTO);
            }
        }
        idCursor.close();
        //   db.setTransactionSuccessful();
        //   db.endTransaction();
        //  db.close();

        return encounterDTOList;
    }

    public ArrayList<EncounterDTO> getEncountersByVisitUUID(String visitUUID) {

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        // db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT DISTINCT uuid, visituuid, encounter_type_uuid, provider_uuid, encounter_time " +
                        "FROM tbl_encounter where visituuid = ? and voided = '0' AND encounter_type_uuid != ? ORDER BY encounter_time",
                new String[]{visitUUID, ENCOUNTER_VISIT_COMPLETE});
        EncounterDTO encounterDTO = new EncounterDTO();
        ArrayList<EncounterDTO> encounterDTOList = new ArrayList<>();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTOList.add(encounterDTO);
            }
        }
        idCursor.close();
        //  db.setTransactionSuccessful();
        //  db.endTransaction();
        //  db.close();

        return encounterDTOList;
    }

    public EncounterDTO getEncounterByVisitUUIDLimit1(String visitUUID) {

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        // db.beginTransaction(); aa81db5e-2f59-456f-aa81-0028f11257f4
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and voided = '0' " +
                        "AND encounter_type_uuid != ? AND encounter_type_uuid IS NOT NULL AND encounter_type_uuid != '' " +
                        "ORDER BY encounter_time DESC limit 1",
                new String[]{visitUUID, ENCOUNTER_VISIT_COMPLETE});

        EncounterDTO encounterDTO = new EncounterDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setVoided(idCursor.getInt(idCursor.getColumnIndexOrThrow("voided")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));

            }
        }

        idCursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        db.close();

        return encounterDTO;
    }

    public boolean updateEncounterSync(String synced, String uuid) throws DAOException {
        boolean isUpdated = true;

        Logger.logD("encounterdao", "updatesynv encounter " + uuid + "" + synced);
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("sync", synced);
            values.put("uuid", uuid);

            int i = db.update("tbl_encounter", values, whereclause, whereargs);
            Logger.logD(tag, "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD(tag, "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
        }

        return isUpdated;
    }

    public boolean setEmergency(String visitUuid, boolean emergencyChecked) throws DAOException {
        //delete any existing emergency encounter and insert new
        //this is the expected behavior in openMRS
        boolean isExecuted = false;
        EncounterDAO encounterDAO = new EncounterDAO();
        String emergency_uuid = encounterDAO.getEncounterTypeUuid("EMERGENCY");
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "visituuid = ? AND encounter_type_uuid = ? ";
        String[] whereargs = {visitUuid, emergency_uuid};
        try {
            values.put("voided", "1");
            values.put("sync", false);
            int i = db.update("tbl_encounter", values, whereclause, whereargs);
            Logger.logD("encounter", "description" + i);
            // db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD("encounter", "encounter" + sql.getMessage());
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql.getMessage());
        } finally {
            //   db.endTransaction();

        }
        if (emergencyChecked) {
            String encounteruuid = UUID.randomUUID().toString();
            EncounterDTO encounterDTO = new EncounterDTO();
            encounterDTO.setUuid(encounteruuid);
            encounterDTO.setVisituuid(visitUuid);
            encounterDTO.setVoided(0);
            encounterDTO.setEncounterTypeUuid(emergency_uuid);
            encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
            encounterDTO.setSyncd(false);
            encounterDTO.setProvideruuid(sessionManager.getProviderID());
            Log.d("DTO", "DTOdao: " + encounterDTO.getProvideruuid());

            encounterDAO.createEncountersToDB(encounterDTO);

            ObsDTO obsDTO = new ObsDTO();
            ObsDAO obsDAO = new ObsDAO();
            obsDTO.setConceptuuid(UuidDictionary.EMERGENCY_OBS);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounteruuid);
            obsDTO.setValue("emergency");
            obsDAO.insertObs(obsDTO);
        }
        return isExecuted;
    }

    public String getEmergencyEncounters(String visitUuid, String encounterType) throws DAOException {
        String uuid = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? AND encounter_type_uuid=? AND voided='0' COLLATE NOCASE", new String[]{visitUuid, encounterType});

            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
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
        return uuid;
    }


    public boolean updateEncounterModifiedDate(String encounterUuid) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("encounterdao", "update encounter date and time" + encounterUuid + "" + AppConstants.dateAndTimeUtils.currentDateTime());
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {encounterUuid};
        try {
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("encounter_time", AppConstants.dateAndTimeUtils.currentDateTime());
            int i = db.update("tbl_encounter", values, whereclause, whereargs);
            Logger.logD(tag, "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD(tag, "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }

        return isUpdated;
    }

    public String getVisitCompleteEncounterByVisitUUID(String visitUUID) {
        String isPresent = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        //   db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? AND encounter_type_uuid = ?",
                new String[]{visitUUID, ENCOUNTER_VISIT_COMPLETE});

        if (idCursor.getCount() > 0) { // that means visit complete enc is present ie. stage 2 is ended.
            while (idCursor.moveToNext()) {
                isPresent = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
            }
        } else { // No visit complete encounter is present ie. visit is not completed.
            isPresent = "";
        }
      /*
        if (idCursor.getCount() <= 0) {
            isPresent = false;
        }
        else {
            isPresent = true;
        }*/
        idCursor.close();
        //   db.setTransactionSuccessful();
        //    db.endTransaction();
        //   db.close();

        return isPresent;
    }


    public String getStartVisitNoteEncounterByVisitUUID(String visitUUID) {
        String encounterUuid = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        //   db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? AND encounter_type_uuid = ? AND sync = ?",
                new String[]{visitUUID, ENCOUNTER_VISIT_NOTE, "true"});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterUuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
            }
        }
        idCursor.close();
        //   db.setTransactionSuccessful();
        //   db.endTransaction();
        //   db.close();

        return encounterUuid;
    }

    public void insertStartVisitNoteEncounterToDb(String encounter, String visitUuid) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //  db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", encounter);
            values.put("visituuid", visitUuid);
            values.put("encounter_type_uuid", ENCOUNTER_VISIT_NOTE);
            values.put("sync", "true");

            db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            //   db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            //        db.endTransaction();
        }
    }

    public String twoMinutesAgo(String timeStamp) throws ParseException {
        // NOTE: Since server error -> "The encounter datetime should be between the visit start and stop dates."

//        long FIVE_MINS_IN_MILLIS = 2 * 60 * 1000;
        long TWO_MINS_IN_MILLIS = 2 * 60 * 1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(timeStamp).getTime();

        return df.format(new Date(time - TWO_MINS_IN_MILLIS));
    }

    public String insertVisitCompleteEncounterToDb(String visitUuid, String providerUUID) throws DAOException {
        String encounteruuid = UUID.randomUUID().toString();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //   db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", encounteruuid);
            values.put("visituuid", visitUuid);
            values.put("encounter_time", (twoMinutesAgo(AppConstants.dateAndTimeUtils.currentDateTime())));
//            values.put("encounter_time", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("encounter_type_uuid", ENCOUNTER_VISIT_COMPLETE);
            values.put("provider_uuid", providerUUID);
//            values.put("modified_date", (twoMinutesAgo(AppConstants.dateAndTimeUtils.currentDateTime())));
            values.put("sync", "false");
            values.put("voided", 0);

            db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            //        db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return encounteruuid;
    }

    public boolean isCompletedOrExited(String visitUUID) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        //  db.beginTransaction();

        try {
            // ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"
            //ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30"

            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and encounter_type_uuid in ('629a9d0b-48eb-405e-953d-a5964c88dc30','bd1fbfaa-f5fb-4ebd-b75c-564506fc309e') ", new String[]{visitUUID}); // ENCOUNTER_PATIENT_EXIT_SURVEY
            EncounterDTO encounterDTO = new EncounterDTO();
            if (idCursor.getCount() != 0) {
                return true;
            }
            idCursor.close();
            //     db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            //    db.endTransaction();
        }

        return false;
    }

    // Cursor returns eg: 10 rows but we have return type as String and not list and traversing is from top to bottom.
    // So let the cursor extract, finally it will give the latest record itself coz the value of string will get overridden.
    public String fetchLatestEncounterTypeUuid(String visitUuid) {
        String latestEncounterTypeUuid = "";
        String encounterTypeUuid_Name = "";

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        //    db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT encounter_type_uuid FROM tbl_encounter where visituuid = ? AND encounter_type_uuid != ? ORDER BY encounter_time DESC LIMIT 1",
                //  Cursor idCursor = db.rawQuery("SELECT encounter_type_uuid FROM tbl_encounter where visituuid = ?", // temp testing
                new String[]{visitUuid, ENCOUNTER_VISIT_COMPLETE});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                latestEncounterTypeUuid = idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid"));
            }
        }
        idCursor.close();

        Log.v("Timeline:", "fetch latest encounter: " + latestEncounterTypeUuid);
        // 2nd transcation
        // SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        if (latestEncounterTypeUuid != null && !latestEncounterTypeUuid.equalsIgnoreCase("")) {
            Cursor cursor = db.rawQuery("SELECT name FROM tbl_uuid_dictionary where uuid = ? COLLATE NOCASE", new String[]{latestEncounterTypeUuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    encounterTypeUuid_Name = cursor.getString(cursor.getColumnIndexOrThrow("name")); // this returns Stagex_Hrx_x format encounter
                    Log.v("Timeline:", "fetch encounter: " + encounterTypeUuid_Name);
                }
            }
            cursor.close();
        }

        //   db.setTransactionSuccessful();
        //   db.endTransaction();
        //   db.close();

        return encounterTypeUuid_Name;
    }

    public String getEndDateForVisit(String visitUUID) {
        String endDate = null;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor idCursor = db.rawQuery("SELECT enddate FROM tbl_visit where visituuid = ?",
                new String[]{visitUUID});

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                endDate = idCursor.getString(idCursor.getColumnIndexOrThrow("enddate"));
            }
        }
        idCursor.close();

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        return endDate;
    }

    public String getCompletedEncounterId(String visitUUID) {
        String isPresent = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? AND encounter_type_uuid = ?",
                new String[]{visitUUID, ENCOUNTER_VISIT_COMPLETE});

        if (idCursor.getCount() > 0) { // that means visit complete enc is present ie. stage 2 is ended.
            while (idCursor.moveToNext()) {
                isPresent = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
            }
        } else { // No visit complete encounter is present ie. visit is not completed.
            isPresent = "";
        }
      /*
        if (idCursor.getCount() <= 0) {
            isPresent = false;
        }
        else {
            isPresent = true;
        }*/
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        return isPresent;
    }

}

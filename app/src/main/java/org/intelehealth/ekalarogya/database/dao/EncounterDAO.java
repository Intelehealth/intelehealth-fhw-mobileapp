package org.intelehealth.ekalarogya.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UuidDictionary;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.models.dto.EncounterDTO;
import org.intelehealth.ekalarogya.models.dto.ObsDTO;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

public class EncounterDAO {

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
        try {
            values.put("uuid", encounter.getUuid());
            values.put("visituuid", encounter.getVisituuid());
            values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
            values.put("provider_uuid", encounter.getProvideruuid());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", encounter.getSyncd());
            values.put("voided", encounter.getVoided());
            values.put("privacynotice_value", encounter.getPrivacynotice_value());
            Log.d("VALUES:","VALUES: "+values);
            createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        }
        return isCreated;
    }


    public boolean createEncountersToDB(EncounterDTO encounter) throws DAOException {
        boolean isCreated = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", encounter.getUuid());
            values.put("visituuid", encounter.getVisituuid());
            values.put("encounter_time", encounter.getEncounterTime());
            values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
            values.put("provider_uuid", encounter.getProvideruuid());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "false");
            values.put("voided", encounter.getVoided());
            values.put("privacynotice_value", encounter.getPrivacynotice_value());
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

    public List<EncounterDTO> unsyncedEncounters() {
        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        //Distinct keyword is used to remove all duplicate records.
        Cursor idCursor = db.rawQuery("SELECT distinct a.uuid,a.visituuid,a.encounter_type_uuid,a.provider_uuid,a.encounter_time,a.voided,a.privacynotice_value FROM tbl_encounter a,tbl_obs b WHERE (a.sync = ? OR a.sync=?) AND a.uuid = b.encounteruuid AND b.sync='false' AND b.voided='0' ", new String[]{"false", "0"});
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
        db.setTransactionSuccessful();
        db.endTransaction();
        return encounterDTOList;
    }

    public List<EncounterDTO> getAllEncounters() {
        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
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
        db.setTransactionSuccessful();
        db.endTransaction();
        return encounterDTOList;
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

    public boolean voidEncounter(String uuid) throws DAOException {
        boolean isUpdated = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("voided", "1");
            values.put("uuid", uuid);
            db.update("tbl_encounter", values, whereclause, whereargs);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
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
            values.put("voided", "1"); //visit will be shown in Awaiting consult...
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
            encounterDTO.setVoided(0); //visit will be shown in Priority Consult...
            encounterDTO.setEncounterTypeUuid(emergency_uuid);
            encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
            encounterDTO.setSyncd(false);
            encounterDTO.setProvideruuid(sessionManager.getProviderID());
            Log.d("DTO","DTOdao: "+ encounterDTO.getProvideruuid());

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
                    return uuid;
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

        //for new visit or already uplaoded visit checking...
       /* try {
            Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? AND encounter_type_uuid=? AND voided='1' COLLATE NOCASE", new String[]{visitUuid, encounterType});

            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                   // uuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
                    uuid = "no priority visit";
                    return uuid;
                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        }
        finally {
            db.endTransaction();
        }
*/
        Log.d("uuid", "uuid: "+uuid);
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

    public EncounterDTO getEncounterByVisitUUID(String visitUUID) {

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? limit 1", new String[]{visitUUID});
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
        db.setTransactionSuccessful();
        db.endTransaction();
        return encounterDTO;
    }
    public boolean isCompletedOrExited(String visitUUID) throws DAOException {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"
            //ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30"

            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and encounter_type_uuid in ('629a9d0b-48eb-405e-953d-a5964c88dc30','bd1fbfaa-f5fb-4ebd-b75c-564506fc309e') ", new String[]{visitUUID}); // ENCOUNTER_PATIENT_EXIT_SURVEY
            EncounterDTO encounterDTO = new EncounterDTO();
            if (idCursor.getCount() != 0) {
                return true;
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }

        return false;
    }

    public static HashMap<String, String> getEncountersForRedirection(String visitUuid) {
        EncounterDAO dao = new EncounterDAO();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String encounterIDSelection = "visituuid = ?";
        String[] encounterIDArgs = {visitUuid};
        HashMap<String, String> map = new HashMap<>();

        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (dao.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    String encounterVitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                    map.put(AppConstants.ENCOUNTER_VITALS_KEY, encounterVitalsLocal);
                }
                if (dao.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    String encounterLocalAdultInitial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                    map.put(AppConstants.ENCOUNTER_ADULT_INITIAL, encounterLocalAdultInitial);
                }
            } while (encounterCursor.moveToNext());
            encounterCursor.close();
        }

        return map;
    }
}

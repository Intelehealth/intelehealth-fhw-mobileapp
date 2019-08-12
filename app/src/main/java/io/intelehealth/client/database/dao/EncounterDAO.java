package io.intelehealth.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.models.dto.EncounterDTO;
import io.intelehealth.client.models.dto.ObsDTO;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;

public class EncounterDAO {

    long createdRecordsCount = 0;
    int updatecount = 0;
    private String tag = EncounterDAO.class.getSimpleName();
//    private SQLiteDatabase db = null;

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
            createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
//            db.endTransaction();
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
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where sync = ? OR sync=?  COLLATE NOCASE ORDER BY voided ASC", new String[]{"false", "0"});
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
            Crashlytics.getInstance().core.logException(sql);
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

            encounterDAO.createEncountersToDB(encounterDTO);

            ObsDTO obsDTO = new ObsDTO();
            ObsDAO obsDAO = new ObsDAO();
            obsDTO.setConceptuuid(UuidDictionary.EMERGENCY_OBS);
            obsDTO.setCreator(1);
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
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }
        return uuid;
    }

    public boolean isEmergency(String visitUuid) throws DAOException {
        boolean isEmergency = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String selectQuery = "SELECT uuid FROM tbl_encounter WHERE visituuid='" + visitUuid + "'  AND encounter_type_uuid='ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c' AND voided='0'";
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        isEmergency = true;
                        cursor.moveToNext();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            isEmergency = false;
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }
        return isEmergency;
    }


}

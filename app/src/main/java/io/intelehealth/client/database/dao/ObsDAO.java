package io.intelehealth.client.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.models.dto.ObsDTO;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;

public class ObsDAO {


    //    int updatecount = 0;
//    long createdRecordsCount = 0;
    private SQLiteDatabase db = null;
    SessionManager sessionManager = null;
    String TAG = ObsDAO.class.getSimpleName();

    public boolean insertObsTemp(List<ObsDTO> obsDTOS) throws DAOException {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        try {
            db.beginTransaction();
            Logger.logD("insert", " insert obs");
            for (ObsDTO obs : obsDTOS) {
                if (sessionManager.isFirstTimeSyncExcuted() && obs.getVoided() == 1)
                    continue;//performance reason
                /*Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_obs where uuid=  ?", new String[]{obs.getUuid()});
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
//                        Logger.logD("update", "update has to happen");
                        updateObs(obs);
                    }
                } else {*/
                createObs(obs);
                //}
                //AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
            }
            db.setTransactionSuccessful();
            Logger.logD("insert obs finished", " insert obs finished");
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;

    }

    private boolean createObs(ObsDTO obsDTOS) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount = 0;
        ContentValues values = new ContentValues();
        try {

//           for (ObsDTO obs : obsDTOS) {
//                Logger.logD("insert", "insert has to happen");

            //     db.beginTransaction();

            values.put("uuid", obsDTOS.getUuid());
            values.put("encounteruuid", obsDTOS.getEncounteruuid());
            values.put("creator", obsDTOS.getCreator());
            values.put("conceptuuid", obsDTOS.getConceptuuid());
            values.put("value", obsDTOS.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", obsDTOS.getVoided());
            values.put("sync", "TRUE");
//               createdRecordsCount = db.insert("temp_obs", null, values);
//                Logger.logD("pulldata", "datadumper" + values);
            createdRecordsCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
//            }
            //db.setTransactionSuccessful();
//            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            //db.endTransaction();
        }

        return isCreated;

    }

//    private boolean updateObs(ObsDTO obsDTOS) throws DAOException {
//        boolean isUpdated = true;
//        db.beginTransaction();
//        ContentValues values = new ContentValues();
//        String selection = "uuid = ?";
//        try {
////            for (ObsDTO obs : obsDTOS) {
////                Logger.logD("update", "update has to happen");
//            values.put("encounteruuid", obsDTOS.getEncounteruuid());
//            values.put("creator", obsDTOS.getCreator());
//            values.put("conceptuuid", obsDTOS.getConceptuuid());
//            values.put("value", obsDTOS.getValue());
//            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
//            values.put("voided", obsDTOS.getVoided());
//            values.put("synced", "TRUE");
////                Logger.logD("pulldata", "datadumper" + values);
//            updatecount = db.updateWithOnConflict("tbl_obs", values, selection, new String[]{obsDTOS.getUuid()}, SQLiteDatabase.CONFLICT_REPLACE);
////            }
//            db.setTransactionSuccessful();
////            Logger.logD("updated", "updatedrecords count" + updatecount);
//        } catch (SQLException e) {
//            isUpdated = false;
//            throw new DAOException(e.getMessage(), e);
//        } finally {
//            db.endTransaction();
//        }
//
//        return isUpdated;
//
//    }

    public boolean insertObs(ObsDTO obsDTO) throws DAOException {
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "");
            values.put("sync", "false");
            insertedCount = db.insert("tbl_obs", null, values);

            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }

    public boolean insertEmergencyObs(ObsDTO obsDTO) throws DAOException {
        boolean isCreated = true;
        long insertedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", obsDTO.getUuid());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "");
            values.put("sync", "FALSE");
            insertedCount = db.insert("tbl_obs", null, values);
            if (insertedCount != 0)
                isCreated = true;
            db.setTransactionSuccessful();
            Logger.logD("inserted Emergency ", "created count" + insertedCount);
        } catch (SQLException e) {
            isCreated = false;
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isCreated;

    }


    public boolean updateObs(ObsDTO obsDTO) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {

            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "");
            values.put("sync", "false");

            updatedCount = db.update("tbl_obs", values, selection, new String[]{obsDTO.getUuid()});

            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }
        //If no value is not found, then update fails so insert instead.
        if (updatedCount == 0) {
            try {
                insertObs(obsDTO);
            } catch (DAOException e) {
                Crashlytics.getInstance().core.logException(e);
            }
        }


        return true;
    }

    public boolean insertObsToDb(List<ObsDTO> obsDTO) throws DAOException {
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            for (ObsDTO ob : obsDTO) {
                values.put("uuid", UUID.randomUUID().toString());
                values.put("encounteruuid", ob.getEncounteruuid());
                values.put("creator", ob.getCreator());
                values.put("conceptuuid", ob.getConceptuuid());
                values.put("value", ob.getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("voided", "");
                values.put("sync", "FALSE");
                insertedCount = db.insert("tbl_obs", null, values);
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            Crashlytics.getInstance().core.logException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }

    public List<ObsDTO> obsDTOList(String encounteruuid) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        //take All obs except image obs
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND (conceptuuid != ? AND conceptuuid != ?)", new String[]{encounteruuid, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE});
        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setEncounteruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                obsDTOList.add(obsDTO);
            }
        }
        idCursor.close();

        return obsDTOList;
    }

    public List<String> getImageStrings(String conceptuuid, String encounterUuidAdultIntials) {
        List<String> rawStrings = new ArrayList<>();
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_obs where conceptuuid = ? AND encounteruuid = ? AND voided='0'", new String[]{conceptuuid, encounterUuidAdultIntials});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                rawStrings.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
            }
        }
        idCursor.close();


        return rawStrings;
    }

}

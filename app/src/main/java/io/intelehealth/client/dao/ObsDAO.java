package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.dto.ObsDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;

public class ObsDAO {


    int updatecount = 0;
    long createdRecordsCount = 0;
    private SQLiteDatabase db = null;
    SessionManager sessionManager = null;
    String TAG = ObsDAO.class.getSimpleName();

    public boolean insertObsTemp(List<ObsDTO> obsDTOS) throws DAOException {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
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
            db.close();
        }

        return isInserted;

    }

    private boolean createObs(ObsDTO obsDTOS) throws DAOException {
        boolean isCreated = true;

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
            values.put("synced", "TRUE");
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

    public boolean insertObs(ObsDTO obsDTO) {
        boolean isUpdated = true;
        long insertedCount = 0;
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", AppConstants.NEW_UUID);
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "");
            values.put("synced", "FALSE");
            insertedCount = db.insert("tbl_obs", null, values);
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
        } finally {
            db.endTransaction();
        }

        return isUpdated;

    }


    public boolean updateObs(ObsDTO obsDTO) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
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
            values.put("synced", "FALSE");

            updatedCount = db.update("tbl_obs", values, selection, new String[]{obsDTO.getUuid()});
            //If no value is not found, then update fails so insert instead.
            if (updatedCount == 0) {
                insertObs(obsDTO);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();
            db.close();
        }


        return true;
    }


}

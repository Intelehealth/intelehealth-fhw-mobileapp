package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.ObsDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.Logger;

public class ObsDAO {


    int updatecount = 0;
    long createdRecordsCount = 0;
    private SQLiteDatabase db = null;

    public boolean insertObsTemp(List<ObsDTO> obsDTOS) throws DAOException {
        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (int i = 0; i < obsDTOS.size(); i++) {
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_obs where uuid=  ?", new String[]{obsDTOS.get(i).getUuid()});
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        Logger.logD("update", "update has to happen");
                        if (updateObs(obsDTOS)) {
                            Logger.logD("updated", "update has to happen");
                        } else {
                            Logger.logD("failed", "failed to updated");
                        }
                    }
                } else {
                    Logger.logD("insert", "insert has to happen");
                    if (createObs(obsDTOS)) {
                        Logger.logD("insert", "inserted");
                    } else {
                        Logger.logD("insert", "failed to inserted");
                    }
                }
                AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
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

    private boolean createObs(List<ObsDTO> obsDTOS) throws DAOException {
        boolean isCreated = true;
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {

            for (ObsDTO obs : obsDTOS) {
//                Logger.logD("insert", "insert has to happen");
                values.put("uuid", obs.getUuid());
                values.put("encounteruuid", obs.getEncounteruuid());
                values.put("creator", obs.getCreator());
                values.put("conceptuuid", obs.getConceptuuid());
                values.put("value", obs.getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("voided", obs.getVoided());
                values.put("synced", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                createdRecordsCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isCreated;

    }

    private boolean updateObs(List<ObsDTO> obsDTOS) throws DAOException {
        boolean isUpdated = true;
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {
            for (ObsDTO obs : obsDTOS) {
//                Logger.logD("update", "update has to happen");
                values.put("encounteruuid", obs.getEncounteruuid());
                values.put("creator", obs.getCreator());
                values.put("conceptuuid", obs.getConceptuuid());
                values.put("value", obs.getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("voided", obs.getVoided());
                values.put("synced", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                updatecount = db.updateWithOnConflict("tbl_obs", values, selection, new String[]{obs.getUuid()}, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + updatecount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return isUpdated;

    }

}

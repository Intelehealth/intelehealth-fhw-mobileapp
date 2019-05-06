package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.LocationDTO;
import io.intelehealth.client.exception.DAOException;

public class LocationDAO {


    long createdRecordsCount = 0;
    int updatecount = 0;
    private SQLiteDatabase db = null;

    public boolean insertLocations(List<LocationDTO> locationDTOS) throws DAOException {

        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (LocationDTO location : locationDTOS) {
//                Cursor cursor = db.rawQuery("SELECT locationuuid FROM tbl_location where locationuuid = ?", new String[]{location.getLocationuuid()});
//                if (cursor.getCount() != 0) {
//                    while (cursor.moveToNext()) {
//                        updateLocation(location);
//                    }
//                } else {
//                    Logger.logD("insert", "insert has to happen");
                    createLocation(location);
//                }
//                AppConstants.sqliteDbCloseHelper.cursorClose(cursor);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }

        return isInserted;
    }

    private boolean createLocation(LocationDTO location) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
//        db.beginTransaction();
        try {
//            for (LocationDTO l : locationDTOS) {
//                Logger.logD("insert", "insert has to happen");
            values.put("name", location.getName());
            values.put("locationuuid", location.getLocationuuid());
            values.put("retired", location.getRetired());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                createdRecordsCount = db.insertWithOnConflict("tbl_location", null, values, SQLiteDatabase.CONFLICT_REPLACE);
//            }
//            db.setTransactionSuccessful();
//            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
//            db.endTransaction();
//            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }
        return isCreated;
    }

    private boolean updateLocation(LocationDTO location) throws DAOException {
        boolean isUpdated = true;
        ContentValues values = new ContentValues();
        String selection = "locationuuid = ?";
        db.beginTransaction();
        try {
//            for (LocationDTO l : locationDTOS) {
//                Logger.logD("insert", "insert has to happen");
            values.put("name", location.getName());
            values.put("retired", location.getRetired());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
            updatecount = db.updateWithOnConflict("tbl_location", values, selection, new String[]{location.getLocationuuid()}, SQLiteDatabase.CONFLICT_REPLACE);
//            }
            db.setTransactionSuccessful();
//            Logger.logD("updated", "updatedrecords count" + updatecount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
//            AppConstants.sqliteDbCloseHelper.dbClose(db);
        }
        return isUpdated;
    }

}

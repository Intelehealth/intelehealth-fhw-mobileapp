package io.intelehealth.client.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.dto.LocationDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.utilities.Logger;

public class LocationDAO {


    long createdRecordsCount = 0;
    int updatecount = 0;
    private SQLiteDatabase db = null;

    public boolean insertLocations(List<LocationDTO> locationDTOS) throws DAOException {

        boolean isInserted = true;
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        AppConstants.inteleHealthDatabaseHelper.onCreate(db);
        ContentValues values = new ContentValues();
        try {
            for (int i = 0; i < locationDTOS.size(); i++) {
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_location where locationuuid = ?", new String[]{locationDTOS.get(i).getLocationuuid()});
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        Logger.logD("update", "update has to happen");
                        if (updateLocation(locationDTOS)) {
                            Logger.logD("updated", "update has to happen");
                        } else {
                            Logger.logD("failed", "failed to updated");
                        }
                    }
                } else {
                    Logger.logD("insert", "insert has to happen");
                    if (createLocation(locationDTOS)) {
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

    private boolean createLocation(List<LocationDTO> locationDTOS) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (LocationDTO l : locationDTOS) {
//                Logger.logD("insert", "insert has to happen");
                values.put("name", l.getName());
                values.put("locationuuid", l.getLocationuuid());
                values.put("retired", l.getRetired());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                createdRecordsCount = db.insertWithOnConflict("tbl_location", null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
        return isCreated;
    }

    private boolean updateLocation(List<LocationDTO> locationDTOS) throws DAOException {
        boolean isUpdated = true;
        ContentValues values = new ContentValues();
        String selection = "locationuuid = ?";
        db.beginTransaction();
        try {
            for (LocationDTO l : locationDTOS) {
//                Logger.logD("insert", "insert has to happen");
                values.put("name", l.getName());
                values.put("retired", l.getRetired());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("synced", "TRUE");
//                Logger.logD("pulldata", "datadumper" + values);
                updatecount = db.updateWithOnConflict("tbl_location", values, selection, new String[]{l.getLocationuuid()}, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + updatecount);
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

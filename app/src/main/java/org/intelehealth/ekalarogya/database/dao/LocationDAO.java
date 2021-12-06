package org.intelehealth.ekalarogya.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.models.dto.LocationDTO;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

public class LocationDAO {

    long createdRecordsCount = 0;

    public boolean insertLocations(List<LocationDTO> locationDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (LocationDTO location : locationDTOS) {
                createLocation(location, db);
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

    private boolean createLocation(LocationDTO location, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("name", location.getName());
            values.put("locationuuid", location.getLocationuuid());
            values.put("retired", location.getRetired());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "TRUE");
            createdRecordsCount = db.insertWithOnConflict("tbl_location", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }

    public String getLocationUUID(String location) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Cursor cursor = db.rawQuery("SELECT DISTINCT locationuuid FROM tbl_location where name = ? COLLATE NOCASE", new String[]{location});
        Log.d("count", "count: "+cursor.getCount());
        String locationuuid="";
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                locationuuid=cursor.getString(cursor.getColumnIndex("locationuuid"));
            }
        }
        cursor.close();
        return locationuuid;
    }

}

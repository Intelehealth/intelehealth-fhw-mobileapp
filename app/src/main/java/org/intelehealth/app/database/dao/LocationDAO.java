package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.LocationDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.exception.DAOException;

public class LocationDAO extends BaseDao{


    long createdRecordsCount = 0;
    private String currentTableName;

    public boolean insertLocations(List<LocationDTO> locationDTOS) throws DAOException {
        setTableName("tbl_location");
        boolean isInserted = true;
        List<HashMap<String, Object>> locationsList = new ArrayList<>();
        for (LocationDTO locationDTO : locationDTOS) {
            locationsList.add(createLocationMap(locationDTO));
        }
        executeInBackground(bulkInsert(locationsList));
       /* boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
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

        }*/

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
    public void setTableName(String tableName) {
        this.currentTableName = tableName;
    }

    @Override
    String tableName() {
        if (currentTableName == null || currentTableName.isEmpty()) {
            throw new RuntimeException("Table name is not set");
        }
        return currentTableName;
    }
    public HashMap<String, Object> createLocationMap(LocationDTO location) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("name", location.getName());
        values.put("locationuuid", location.getLocationuuid());
        values.put("retired", location.getRetired());
        values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
        values.put("sync", "TRUE");
        return values;
    }

}

package io.intelehealth.client.database.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.exception.DAOException;

public class BackgroundSyncDAO {
    SessionManager sessionManager;
    private SQLiteDatabase db;

    public boolean insertAfterPull() throws DAOException {
        boolean isInserted = true;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            values.put("locationuuid", sessionManager.getLocationUuid());
            values.put("last_pull_execution_time", sessionManager.getPullExcutedTime());
            values.put("synced", "false");
            values.put("devices_sync", "true");

            db.insertWithOnConflict("tbl_sync", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }

        return isInserted;
    }
}

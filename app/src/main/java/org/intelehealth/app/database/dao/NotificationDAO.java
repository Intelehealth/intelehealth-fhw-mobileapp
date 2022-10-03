package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.List;
import java.util.UUID;

public class NotificationDAO {
    private static long createdRecordsCount = 0;

    public static boolean insertNotifications(List<NotificationModel> notificationModels) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (NotificationModel model : notificationModels) {
                createNotifications(model, db);
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

    private static boolean createNotifications(NotificationModel model, SQLiteDatabase db) throws DAOException {
        boolean isCreated = false;

        ContentValues values = new ContentValues();
        try {
          //  values.put("uuid", UUID.randomUUID().toString());
            values.put("patientuuid", model.getPatientuuid());
            values.put("description", model.getDescription());
            values.put("obs_server_modified_date", model.getObs_server_modified_date());
            values.put("notification_type", model.getNotification_type());
            values.put("sync", model.getSync());

            createdRecordsCount = db.insertWithOnConflict("tbl_notifications",
                    null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }

}

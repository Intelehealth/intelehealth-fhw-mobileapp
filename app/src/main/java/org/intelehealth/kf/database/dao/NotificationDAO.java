package org.intelehealth.kf.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.models.NotificationModel;
import org.intelehealth.kf.utilities.exception.DAOException;

import java.util.List;

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
            values.put("uuid", model.getUuid());
            values.put("description", model.getDescription());
            values.put("notification_type", model.getNotification_type());
            values.put("obs_server_modified_date", model.getObs_server_modified_date());
            values.put("isdeleted", "false");

            createdRecordsCount = db.insertWithOnConflict("tbl_notifications",
                    null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }

    public static NotificationModel showOnly_NonDeletedNotification(NotificationModel model, String date) {
        NotificationModel notificationModel = new NotificationModel();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_notifications WHERE isdeleted = 'false' AND obs_server_modified_date = ?",
                new String[] {date});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            if (description.equalsIgnoreCase(model.getDescription()))
                notificationModel = model;
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return notificationModel;
    }

    public static boolean deleteNotification(NotificationModel models) {
        boolean isDeleted = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("isdeleted", "true");
            db.update("tbl_notifications", values, "uuid = ? AND notification_type = ?",
                    new String[] {models.getUuid(), models.getNotification_type()});
            db.setTransactionSuccessful();
            isDeleted = true;
        }
        catch (SQLException e) {
            Log.v("dd", "error: " + e.getMessage());
            isDeleted = false;
        }
        finally {
            db.endTransaction();
        }

        return isDeleted;
    }

    public static boolean fetchAllFrom_NotificationTbl(NotificationModel model) {
        boolean value = false;

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();

        Cursor cursor_count = db.rawQuery("SELECT * FROM tbl_notifications", new String[] {});
        if (cursor_count.getCount() > 0) {
            while (cursor_count.moveToNext()) {
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_notifications WHERE description = ? AND " +
                                "notification_type = ? AND obs_server_modified_date = ?",
                        new String[] {model.getDescription(), model.getNotification_type(), model.getObs_server_modified_date()});

                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        value = true;
                    }
                }
                cursor.close();
//                db.setTransactionSuccessful();
//                db.endTransaction();
            }
        }

       /* Cursor cursor = db.rawQuery("SELECT * FROM tbl_notifications WHERE uuid = ? AND description = ? AND " +
                        "notification_type = ? AND obs_server_modified_date = ?",
                new String[] {model.getUuid(), model.getDescription(), model.getNotification_type(), model.getObs_server_modified_date()});

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                value = true;
            }
        }*/

        cursor_count.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return value;
    }
}

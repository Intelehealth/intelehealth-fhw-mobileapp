package org.intelehealth.app.database.dao.notification;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public static boolean insertNotifications(List<NotificationModel> notificationModels) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (NotificationModel model : notificationModels) {
                if (!createNotifications(model, db)) {
                    isInserted = false;
                    break; // If one notification fails, break the loop
                }
            }
            if (isInserted) {
                db.setTransactionSuccessful(); // Only mark transaction successful if all notifications are inserted successfully
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isInserted;
    }

    private static boolean createNotifications(NotificationModel model, SQLiteDatabase db) throws DAOException {
        ContentValues values = new ContentValues();
        try {
            values.put(NotificationDbConstants.UUID, model.getUuid());
            values.put(NotificationDbConstants.DESCRIPTION, model.getDescription());
            values.put(NotificationDbConstants.NOTIFICATION_TYPE, model.getNotification_type());
            values.put(NotificationDbConstants.OBS_SERVER_MODIFIED_DATE, model.getObs_server_modified_date());
            values.put(NotificationDbConstants.IS_DELETED, false);

            long createdRecordsCount = db.insertWithOnConflict(NotificationDbConstants.NOTIFICATION_TABLE,
                    null, values, SQLiteDatabase.CONFLICT_REPLACE);

            // Check if record insertion was successful
            return createdRecordsCount != -1;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }
    public static List<NotificationModel> getNonDeletedNotifications() throws DAOException {
        List<NotificationModel> nonDeletedNotifications = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(NotificationDbConstants.NOTIFICATION_TABLE,
                null,
                NotificationDbConstants.IS_DELETED + " = ?",
                new String[]{"0"}, // "0" represents false for IS_DELETED
                null,
                null,
                null)) {
            // "0" represents false for IS_DELETED
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    NotificationModel model = new NotificationModel();
                    int uuidIndex = cursor.getColumnIndex(NotificationDbConstants.UUID);
                    int descIndex = cursor.getColumnIndex(NotificationDbConstants.DESCRIPTION);
                    int typeIndex = cursor.getColumnIndex(NotificationDbConstants.NOTIFICATION_TYPE);
                    int obsIndex = cursor.getColumnIndex(NotificationDbConstants.OBS_SERVER_MODIFIED_DATE);

                    if (uuidIndex >= 0 && descIndex >= 0 && typeIndex >= 0 && obsIndex >= 0) {
                        model.setUuid(cursor.getString(uuidIndex));
                        model.setDescription(cursor.getString(descIndex));
                        model.setNotification_type(cursor.getString(typeIndex));
                        model.setObs_server_modified_date(cursor.getString(obsIndex));
                        nonDeletedNotifications.add(model);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return nonDeletedNotifications;
    }

    public static boolean deleteNotification(String uuid) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(NotificationDbConstants.IS_DELETED, true);

            int rowsAffected = db.update(NotificationDbConstants.NOTIFICATION_TABLE,
                    values,
                    NotificationDbConstants.UUID + " = ?",
                    new String[]{uuid});

            // Check if any rows were affected
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    public static boolean markAllNotificationsAsDeleted() throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(NotificationDbConstants.IS_DELETED, true);

            int rowsAffected = db.update(NotificationDbConstants.NOTIFICATION_TABLE,
                    values,
                    null,
                    null);
            // Check if any rows were affected
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    public static boolean fetchAllFrom_NotificationTbl(NotificationModel model) {
        boolean value = false;

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();

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
        //db.setTransactionSuccessful();
        //db.endTransaction();

        return value;
    }

    public static List<NotificationModel> getAllNotifications() throws DAOException {
        List<NotificationModel> allNotifications = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(NotificationDbConstants.NOTIFICATION_TABLE,
                    new String[]{NotificationDbConstants.UUID, NotificationDbConstants.DESCRIPTION,
                            NotificationDbConstants.NOTIFICATION_TYPE, NotificationDbConstants.OBS_SERVER_MODIFIED_DATE},
                    null,
                    null,
                    null,
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    NotificationModel model = new NotificationModel();
                    int uuidIndex = cursor.getColumnIndex(NotificationDbConstants.UUID);
                    int descIndex = cursor.getColumnIndex(NotificationDbConstants.DESCRIPTION);
                    int typeIndex = cursor.getColumnIndex(NotificationDbConstants.NOTIFICATION_TYPE);
                    int obsIndex = cursor.getColumnIndex(NotificationDbConstants.OBS_SERVER_MODIFIED_DATE);

                    if (uuidIndex >= 0 && descIndex >= 0 && typeIndex >= 0 && obsIndex >= 0) {
                        model.setUuid(cursor.getString(uuidIndex));
                        model.setDescription(cursor.getString(descIndex));
                        model.setNotification_type(cursor.getString(typeIndex));
                        model.setObs_server_modified_date(cursor.getString(obsIndex));
                        allNotifications.add(model);
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return allNotifications;
    }


}

package org.intelehealth.app.database.dao.notification

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.models.NotificationModel
import org.intelehealth.app.utilities.exception.DAOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotificationDAO {

    fun insertNotifications(notificationModels: List<NotificationModel>) {

        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        try {
            db.beginTransaction()
            for (model in notificationModels) {
                if (!createNotification(model, db)) {

                    continue
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {

            e.printStackTrace() // Log the exception
        } finally {
            db.endTransaction()
        }

    }

    @Throws(DAOException::class)
    private fun createNotification(model: NotificationModel, db: SQLiteDatabase): Boolean {
        val values = ContentValues()
        values.put(NotificationDbConstants.UUID, model.uuid)
        values.put(NotificationDbConstants.DESCRIPTION, model.description)
        values.put(NotificationDbConstants.PATIENT_UID, model.patientuuid)
        values.put(NotificationDbConstants.GENDER, model.gender)
        values.put(NotificationDbConstants.ENCOUNTER_UUID, model.encounterUuidAdultIntial)
        values.put(NotificationDbConstants.ENCOUNTER_UUID_VITALS, model.encounterUuidVitals)
        values.put(NotificationDbConstants.NAME, model.first_name)//here firstname means fname and lname
        values.put(NotificationDbConstants.NOTIFICATION_TYPE, model.notification_type)
        values.put(NotificationDbConstants.OBS_SERVER_MODIFIED_DATE, model.obs_server_modified_date)
        values.put(
                NotificationDbConstants.IS_DELETED,
                false
        ) // Assuming 0 represents false for IS_DELETED

        return try {
            val createdRecordsCount = db.insertWithOnConflict(
                    NotificationDbConstants.NOTIFICATION_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            )

            // Check if record insertion was successful
            createdRecordsCount != -1L
        } catch (e: Exception) {
            throw DAOException(e.message, e)
        }
    }

    fun nonDeletedNotifications(): List<NotificationModel> {
        val nonDeletedNotifications: MutableList<NotificationModel> = ArrayList()
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
        try {
            db.query(
                    NotificationDbConstants.NOTIFICATION_TABLE,
                    null,
                    NotificationDbConstants.IS_DELETED + " = ?",
                    arrayOf("0"),  // "0" represents false for IS_DELETED
                    null,
                    null,
                    null
            ).use { cursor ->
                // "0" represents false for IS_DELETED
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val model = NotificationModel()
                        val uuidIndex = cursor.getColumnIndex(NotificationDbConstants.UUID)
                        val descIndex =
                                cursor.getColumnIndex(NotificationDbConstants.DESCRIPTION)
                        val patientUid =
                                cursor.getColumnIndex(NotificationDbConstants.PATIENT_UID)
                        val gender =
                                cursor.getColumnIndex(NotificationDbConstants.GENDER)
                        val name =
                                cursor.getColumnIndex(NotificationDbConstants.NAME)
                        val enUUid =
                                cursor.getColumnIndex(NotificationDbConstants.ENCOUNTER_UUID)
                        val enUUidVitals =
                                cursor.getColumnIndex(NotificationDbConstants.ENCOUNTER_UUID_VITALS)
                        val typeIndex =
                                cursor.getColumnIndex(NotificationDbConstants.NOTIFICATION_TYPE)
                        val obsIndex =
                                cursor.getColumnIndex(NotificationDbConstants.OBS_SERVER_MODIFIED_DATE)

                        model.uuid = cursor.getString(uuidIndex)
                        model.description = cursor.getString(descIndex)
                        model.patientuuid = cursor.getString(patientUid)
                        model.gender = cursor.getString(gender)
                        model.first_name = cursor.getString(name)
                        model.notification_type = cursor.getString(typeIndex)
                        model.encounterUuidAdultIntial = cursor.getString(enUUid)
                        model.encounterUuidVitals = cursor.getString(enUUidVitals)
                        model.obs_server_modified_date = cursor.getString(obsIndex)
                        nonDeletedNotifications.add(model)
                    }
                }
            }
        } catch (e: SQLException) {
            throw DAOException(e.message, e)
        }
        return nonDeletedNotifications
    }

    fun deleteNotification(uuid: String): Boolean {
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        return try {
            val values = ContentValues()
            values.put(NotificationDbConstants.IS_DELETED, true)
            val rowsAffected = db.update(
                    NotificationDbConstants.NOTIFICATION_TABLE,
                    values,
                    NotificationDbConstants.UUID + " = ?", arrayOf(uuid)
            )

            // Check if any rows were affected
            rowsAffected > 0
        } catch (e: SQLException) {
            throw DAOException(e.message, e)
        }
    }

    fun markAllNotificationsAsDeleted(): Boolean {
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        return try {
            val values = ContentValues()
            values.put(NotificationDbConstants.IS_DELETED, true)
            val rowsAffected = db.update(
                    NotificationDbConstants.NOTIFICATION_TABLE,
                    values,
                    null,
                    null
            )
            // Check if any rows were affected
            rowsAffected > 0
        } catch (e: SQLException) {
            throw DAOException(e.message, e)
        }
    }

    fun fetchAllFrom_NotificationTbl(model: NotificationModel): Boolean {
        var value = false
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        //db.beginTransaction();
        val cursor_count = db.rawQuery("SELECT * FROM tbl_notifications", arrayOf())
        if (cursor_count.count > 0) {
            while (cursor_count.moveToNext()) {
                val cursor = db.rawQuery(
                        "SELECT * FROM tbl_notifications WHERE description = ? AND " +
                                "notification_type = ? AND obs_server_modified_date = ?",
                        arrayOf(
                                model.description,
                                model.notification_type,
                                model.obs_server_modified_date
                        )
                )
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        value = true
                    }
                }
                cursor.close()
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
    }*/cursor_count.close()
        //db.setTransactionSuccessful();
        //db.endTransaction();
        return value
    }


    val allNotifications: List<NotificationModel>
        get() {
            val allNotifications: MutableList<NotificationModel> = ArrayList()
            val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
            var cursor: Cursor? = null
            try {
                cursor = db.query(
                        NotificationDbConstants.NOTIFICATION_TABLE, arrayOf(
                        NotificationDbConstants.UUID,
                        NotificationDbConstants.DESCRIPTION,
                        NotificationDbConstants.NOTIFICATION_TYPE,
                        NotificationDbConstants.OBS_SERVER_MODIFIED_DATE
                ),
                        null,
                        null,
                        null,
                        null,
                        null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        val model = NotificationModel()
                        val uuidIndex = cursor.getColumnIndex(NotificationDbConstants.UUID)
                        val descIndex =
                                cursor.getColumnIndex(NotificationDbConstants.DESCRIPTION)
                        val typeIndex =
                                cursor.getColumnIndex(NotificationDbConstants.NOTIFICATION_TYPE)
                        val obsIndex =
                                cursor.getColumnIndex(NotificationDbConstants.OBS_SERVER_MODIFIED_DATE)
                        if (uuidIndex >= 0 && descIndex >= 0 && typeIndex >= 0 && obsIndex >= 0) {
                            model.uuid = cursor.getString(uuidIndex)
                            model.description = cursor.getString(descIndex)
                            model.notification_type = cursor.getString(typeIndex)
                            model.obs_server_modified_date = cursor.getString(obsIndex)
                            allNotifications.add(model)
                        }
                    } while (cursor.moveToNext())
                }
            } catch (e: SQLException) {
                throw DAOException(e.message, e)
            } finally {
                cursor?.close()
            }
            return allNotifications
        }
}

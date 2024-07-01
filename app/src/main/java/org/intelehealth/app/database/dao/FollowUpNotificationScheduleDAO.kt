package org.intelehealth.app.database.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.models.FollowUpNotificationShData
import org.intelehealth.app.utilities.exception.DAOException

/**
 * Created By Tanvir Hasan on 6/4/24 12:15 AM
 * Email: tanvirhasan553@gmail.com
 */
class FollowUpNotificationScheduleDAO {
    fun insertFollowupNotification(followUpNotificationShData: FollowUpNotificationShData): Boolean {
        var isInserted = true
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writeDb
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put("schedule_date_time", followUpNotificationShData.dateTime)
            values.put("visituuid", followUpNotificationShData.visitUuid)
            db.insert(
                "tbl_follow_up_notification_schedule",
                null,
                values
            )
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            isInserted = false
            throw DAOException(e.message, e)
        } finally {
            db.endTransaction()
        }
        return isInserted
    }

    fun getFollowupNotification(): List<FollowUpNotificationShData>{
        /*val notifications: MutableList<FollowUpNotificationShData> = ArrayList()
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
        try {
            db.query(
                    NotificationDbConstants.FOLLOW_UP_NOTIFICATION,
                    null,
                    "schedule_date_time > "+System.currentTimeMillis(),
                    arrayOf("0"),  // "0" represents false for IS_DELETED
                    null,
                    null,
                    null
            ).use { cursor ->
                // "0" represents false for IS_DELETED
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val model = FollowUpNotificationShData(notificationData.name, notificationData.visitUuid + " " + duration)
                        val id = cursor.getColumnIndex("id")
                        val scheduleDateTime =
                                cursor.getColumnIndex("schedule_date_time")

                        model.id = cursor.getString(id)
                        model.scheduleDateTime = cursor.getString(scheduleDateTime)
                        notifications.add(model)
                    }
                }
            }
        } catch (e: SQLException) {
            throw DAOException(e.message, e)
        }
        return notifications*/
        return mutableListOf()
    }

    fun deleteFollowUpNotificationDataFromVisitUuid(visitUuid: String){
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writeDb
        try {
            val deleteClause = "visituuid = ?"
            db.delete("tbl_follow_up_notification_schedule", deleteClause, arrayOf<String>(visitUuid))
        }catch (_:Exception){}
    }


    fun countScheduleByVisitUuid(visitUuid: String): Int {
        var count = -1
        try {
            val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
            db.beginTransactionNonExclusive()
            var cursor: Cursor? = null
            var query = ""
            query = ("SELECT count(*) "
                    + "from tbl_follow_up_notification_schedule where visituuid = '$visitUuid'")
            cursor = db.rawQuery(query, arrayOf())

            db.setTransactionSuccessful()
            db.endTransaction()
            if (cursor != null) {
                cursor.moveToFirst()
                count = cursor.getInt(0)
                cursor.close()
            }
        } catch (e: Exception) {

        }
        Log.d("FOLLOW_UP_NOTIFICATION", "" + count)
        return count
    }
}
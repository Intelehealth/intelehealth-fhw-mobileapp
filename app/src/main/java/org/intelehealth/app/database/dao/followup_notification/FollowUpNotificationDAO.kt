package org.intelehealth.app.database.dao.followup_notification

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.google.gson.Gson
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.models.FollowUpNotificationData
import org.intelehealth.app.models.FollowUpNotificationShData
import org.intelehealth.app.utilities.exception.DAOException

/**
 * Created By Tanvir Hasan on 6/4/24 12:15 AM
 * Email: tanvirhasan553@gmail.com
 */
class FollowUpNotificationDAO {

    /**
     * inserting notification schedule
     */
    fun insertFollowupNotification(followUpNotificationShData: FollowUpNotificationShData): Boolean {
        var isInserted = true
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writeDb
        db.beginTransaction()
        try {

            val values = ContentValues()
            values.put(FollowUpNotificationDbConstant.ID, followUpNotificationShData.id)
            values.put(FollowUpNotificationDbConstant.NAME, followUpNotificationShData.name)
            values.put(
                FollowUpNotificationDbConstant.OPENMRS_ID,
                followUpNotificationShData.openMrsId
            )
            values.put(
                FollowUpNotificationDbConstant.PATIENT_UUID,
                followUpNotificationShData.patientUid
            )
            values.put(
                FollowUpNotificationDbConstant.REQUEST_CODE,
                followUpNotificationShData.requestCode
            )
            values.put(
                FollowUpNotificationDbConstant.DATE_TIME,
                followUpNotificationShData.dateTime
            )
            values.put(
                FollowUpNotificationDbConstant.VALUE,
                followUpNotificationShData.value
            )
            values.put(
                FollowUpNotificationDbConstant.DURATION,
                followUpNotificationShData.duration
            )
            values.put(
                FollowUpNotificationDbConstant.VISIT_UUID,
                followUpNotificationShData.visitUuid
            )
            db.insert(
                FollowUpNotificationDbConstant.TABLE,
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

    /**
     * get all upcoming followup module schedule
     */

    fun getFollowupNotification(): List<FollowUpNotificationShData> {
        val notifications: MutableList<FollowUpNotificationShData> = ArrayList()
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
        try {
            db.query(
                FollowUpNotificationDbConstant.TABLE,
                null,
                FollowUpNotificationDbConstant.DATE_TIME + " > ?",
                arrayOf(System.currentTimeMillis().toString()),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getColumnIndex(FollowUpNotificationDbConstant.ID)
                        val dateTime =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.DATE_TIME)
                        val value =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.VALUE)
                        val duration =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.DURATION)
                        val name = cursor.getColumnIndex(FollowUpNotificationDbConstant.NAME)
                        val openmrsId =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.OPENMRS_ID)
                        val patientId =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.PATIENT_UUID)
                        val visitUuid =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.VISIT_UUID)
                        val requestCode =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.REQUEST_CODE)

                        notifications.add(
                            FollowUpNotificationShData(
                                id = cursor.getString(id),
                                dateTime = cursor.getString(dateTime),
                                value = cursor.getString(value),
                                duration = cursor.getString(duration),
                                name = cursor.getString(name),
                                openMrsId = cursor.getString(openmrsId),
                                patientUid = cursor.getString(patientId),
                                visitUuid = cursor.getString(visitUuid),
                                requestCode = cursor.getString(requestCode)
                            )
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            throw DAOException(e.message, e)
        }
        return notifications
    }

    /**
     * get followup by id
     */
    fun getFollowupNotificationById(id: String): FollowUpNotificationShData? {
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
            try {
            db.query(
                FollowUpNotificationDbConstant.TABLE,
                null,
                FollowUpNotificationDbConstant.ID + " = ?",
                arrayOf(id),
                null,
                null,
                null
            ).use { cursor ->
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getColumnIndex(FollowUpNotificationDbConstant.ID)
                        val dateTime =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.DATE_TIME)
                        val value =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.VALUE)
                        val duration =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.DURATION)
                        val name = cursor.getColumnIndex(FollowUpNotificationDbConstant.NAME)
                        val openmrsId =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.OPENMRS_ID)
                        val patientId =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.PATIENT_UUID)
                        val visitUuid =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.VISIT_UUID)
                        val requestCode =
                            cursor.getColumnIndex(FollowUpNotificationDbConstant.REQUEST_CODE)

                            return FollowUpNotificationShData(
                                id = cursor.getString(id),
                                dateTime = cursor.getString(dateTime),
                                value = cursor.getString(value),
                                duration = cursor.getString(duration),
                                name = cursor.getString(name),
                                openMrsId = cursor.getString(openmrsId),
                                patientUid = cursor.getString(patientId),
                                visitUuid = cursor.getString(visitUuid),
                                requestCode = cursor.getString(requestCode)
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            throw DAOException(e.message, e)
        }
        return null
    }

}
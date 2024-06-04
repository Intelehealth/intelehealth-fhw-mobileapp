package org.intelehealth.app.utilities

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.FollowUpNotificationScheduleDAO
import org.intelehealth.app.models.FollowUpNotificationData
import org.intelehealth.app.models.FollowUpNotificationShData
import org.intelehealth.app.utilities.exception.DAOException
import org.intelehealth.app.worker.ScheduleNotificationWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.round

/**
 * Created by Tanvir Hasan on 26-05-2024 : 11-58.
 * Email: mhasan@intelehealth.org
 */
class NotificationSchedulerUtils {
    companion object {
        @JvmStatic
        fun scheduleFollowUpNotification(context: Context) {

            try {
                val notificationDataList = EncounterDAO.getFollowUpDateListFromConceptId()
                for (notificationData in notificationDataList) {
                    if (FollowUpNotificationScheduleDAO().countScheduleByVisitUuid(notificationData.visitUuid) <= 0) {
                        val followUpTime = getDateTimeMiliFromString( notificationData.value)
                            //System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)

                        scheduleNotification(
                            context,
                            followUpTime,
                            AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                            TimeUnit.MINUTES,
                            notificationData
                        )

                        scheduleNotification(
                            context,
                            followUpTime,
                            AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION,
                            TimeUnit.HOURS,
                            notificationData
                        )
                    }
                }

            } catch (e: DAOException) {
                throw RuntimeException(e)
            }


        }

        private fun getDateTimeMiliFromString(dateTime: String): Long {
            val dateTimeString = /*"2024-06-03T17:53:04.000+0530"*/dateTime
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date? = sdf.parse(dateTimeString)
            return date?.time ?: 0
        }


        @JvmStatic
        fun scheduleNotification(
            context: Context,
            dateTime: Long,
            duration: Long,
            durationType: TimeUnit,
            notificationData: FollowUpNotificationData,
        ) {
            val data = Data.Builder()
                .putString(BundleKeys.TITLE, "Reminder")
                .putString(
                    BundleKeys.DESCRIPTION,
                    context.getString(
                        R.string.follow_up_appointment_will_start_in_hours_for,
                        duration.toInt().toString(),
                        notificationData.name,
                        notificationData.openMrsId
                    )
                )
                .putString(
                    BundleKeys.CHANNEL_ID,
                    "" + dateTime + "" + notificationData.patientUid
                )
                .putString(BundleKeys.PATIENT_UUID, notificationData.patientUid)
                .putString(BundleKeys.PATIENT_ID, notificationData.openMrsId)
                .putString(BundleKeys.VISIT_UUI, notificationData.visitUuid)
                .putString(BundleKeys.GENDER, notificationData.gender)
                .putString(BundleKeys.NAME, notificationData.name)
                .putString(
                    BundleKeys.ENCOUNTER_TYPE_UUID,
                    notificationData.encounterTypeUid
                )
                .putString(BundleKeys.CONCEPT_UUID, notificationData.conceptUuid)
                .putString(BundleKeys.ENCOUNTER_UUID, notificationData.encounterUuid)
                .putString(BundleKeys.VALUE, notificationData.value)
                .build()

            val delay = dateTime - System.currentTimeMillis() - durationType.toMillis(duration)

            Log.d("DDDDDDEEEEE",""+delay+"  "+durationType.toMillis(duration)+"  "+dateTime)
            val workRequest2Hours = OneTimeWorkRequestBuilder<ScheduleNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            if(delay < 0) return

            WorkManager.getInstance(IntelehealthApplication.getAppContext())
                .enqueue(workRequest2Hours)

            FollowUpNotificationScheduleDAO().insertEncounter(
                FollowUpNotificationShData(
                    notificationData.visitUuid,
                    (System.currentTimeMillis() + delay).toString()
                )
            )
        }
    }
}
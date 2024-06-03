package org.intelehealth.app.utilities

import android.util.Log
import android.util.TimeUtils
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.utilities.exception.DAOException
import org.intelehealth.app.worker.ScheduleNotificationWorker
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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
        fun scheduleFollowUpNotification(
        ) {
            /*  val diff = followUpTime - System.currentTimeMillis()
              val minutes = round(diff.toDouble() / 60000.0).toInt()*/


            try {
                val notificationDataList = EncounterDAO.getFollowUpDateListFromConceptId()
                for (notificationData in notificationDataList) {
                    val followUpTime = /*getDateTimeMiliFromString( "")*/System.currentTimeMillis()+TimeUnit.MINUTES.toMillis(5)

                    val diff = followUpTime - System.currentTimeMillis()
                    val minutes = round(diff.toDouble() / 60000.0).toInt()
                    Log.d("DDDDDDDD", "" + followUpTime)
                    val data = Data.Builder().apply {
                        putString(BundleKeys.TITLE, "Reminder")
                        putString(
                            BundleKeys.DESCRIPTION,
                            "Follow up appointment will start in $minutes minutes for ${notificationData.name}"
                        )
                        putString(
                            BundleKeys.CHANNEL_ID,
                            "" + followUpTime + "" + notificationData.patientUid
                        )
                    }.build()

                    scheduleNotification(
                        followUpTime,
                        AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                        TimeUnit.MINUTES,
                        data
                    )

                    scheduleNotification(
                        followUpTime,
                        AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION,
                        TimeUnit.HOURS,
                        data
                    )
                }
            } catch (e: DAOException) {
                throw RuntimeException(e)
            }


        }

        private fun getDateTimeMiliFromString(dateTime: String): Long {
            val dateTimeString = "2024-06-03T17:53:04.000+0530"
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date? = sdf.parse(dateTimeString)
            return date?.time ?: 0
        }


        @JvmStatic
        fun scheduleNotification(
            dateTime: Long,
            duration: Long,
            durationType: TimeUnit,
            data: Data,
        ) {
            val delay = dateTime - System.currentTimeMillis() - durationType.toMillis(duration)
            val workRequest2Hours = OneTimeWorkRequestBuilder<ScheduleNotificationWorker>()
                .setInitialDelay(delay, durationType)
                .setInputData(data)
                .build()
            WorkManager.getInstance(IntelehealthApplication.getAppContext())
                .enqueue(workRequest2Hours)
        }
    }
}
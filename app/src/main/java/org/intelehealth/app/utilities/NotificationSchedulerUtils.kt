package org.intelehealth.app.utilities

import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.worker.ScheduleNotificationWorker
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
            followUpTime: Long,
            patientId: String,
            patientName: String,
        ) {
            val diff = followUpTime - System.currentTimeMillis()
            val minutes = round(diff.toDouble() / 60000.0).toInt()

            val data = Data.Builder()
                .putString(BundleKeys.TITLE, "Reminder")
                .putString(BundleKeys.DESCRIPTION, "Follow up appointment will start in $minutes minutes for $patientName")
                .putString(BundleKeys.CHANNEL_ID, ""+followUpTime+""+patientId)
                .build()

            scheduleNotification(
                followUpTime,
                AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                TimeUnit.HOURS,
                data
            )

            scheduleNotification(
                followUpTime,
                AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION,
                TimeUnit.HOURS,
                data
            )
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
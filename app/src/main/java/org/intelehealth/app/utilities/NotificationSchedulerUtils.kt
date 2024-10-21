package org.intelehealth.app.utilities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.followup_notification.FollowUpNotificationDAO
import org.intelehealth.app.models.FollowUpNotificationData
import org.intelehealth.app.models.FollowUpNotificationShData
import org.intelehealth.app.utilities.exception.DAOException
import org.intelehealth.app.notificationScheduler.ScheduleNotificationReceiver
import org.intelehealth.app.notificationScheduler.ScheduleNotificationWorker
import java.util.concurrent.TimeUnit


/**
 * Created by Tanvir Hasan on 26-05-2024 : 11-58.
 * Email: mhasan@intelehealth.org
 */
class NotificationSchedulerUtils {
    companion object {
        @JvmStatic
        fun scheduleFollowUpNotification(followUpNotificationData: FollowUpNotificationData) {

            try {
                val followUpTime = DateAndTimeUtils.getTimeStampFromString(followUpNotificationData.value,"yyyy-MM-dd, 'Time: 'hh:mm a")
                if (followUpTime > System.currentTimeMillis()) {

                    val followupDateTimeBefore24H =
                        followUpTime - TimeUnit.HOURS.toMillis(AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION)

                    CustomLog.d("24_h_sub",""+followUpTime+"  "+TimeUnit.HOURS.toMillis(AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION)+"  "+followUpNotificationData.value+"  ")
                    /**
                     * if followup date time is less than 24 h triggering the notification immediately
                     */
                    if (followupDateTimeBefore24H < System.currentTimeMillis()) {
                        val currentDateTimeWith2H =
                            followUpTime - TimeUnit.HOURS.toMillis(AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION)

                        if (currentDateTimeWith2H > System.currentTimeMillis()) {
                            scheduleNotificationAlarmManager(
                                followUpTime,
                                AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                                false,
                                followUpNotificationData
                            )
                        }
                        scheduleNotificationAlarmManager(
                            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2),
                            0,
                            false,
                            followUpNotificationData
                        )
                    }
                    //normal schedule for 24h and 2h
                    else {
                        scheduleNotificationAlarmManager(
                            followUpTime,
                            AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                            false,
                            followUpNotificationData
                        )
                        scheduleNotificationAlarmManager(
                            followUpTime,
                            AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION,
                            false,
                            followUpNotificationData
                        )
                    }
                }

            } catch (e: DAOException) {
                CustomLog.e("ERRRR", e.message ?: "Err")
                throw RuntimeException(e)
            }


        }


        @JvmStatic
        fun scheduleNotification(
            dateTime: Long,
            duration: Long,
            durationType: TimeUnit,
            notificationData: FollowUpNotificationData,
        ) {
            val data = Data.Builder()
                .putString(
                    BundleKeys.TITLE,
                    IntelehealthApplication.getAppContext().getString(R.string.reminder)
                )
                .putString(
                    BundleKeys.DESCRIPTION,
                    IntelehealthApplication.getAppContext().getString(
                        R.string.patient_follow_up_appointment_on,
                        notificationData.name,
                        notificationData.openMrsId,
                        DateAndTimeUtils.parseDateTimeToDateTime(notificationData.value)
                    )
                )
                .putString(
                    BundleKeys.CHANNEL_ID,
                    "" + dateTime + "" + notificationData.patientUid
                )
                .putString(BundleKeys.VISIT_UUI, notificationData.visitUuid)
                .putString(BundleKeys.NAME, notificationData.name)
                .build()

            val delay = (dateTime - System.currentTimeMillis()) - durationType.toMillis(duration)

            CustomLog.d(
                "DELAY_NOTIFICATION",
                "" + delay + "  " + durationType.toMillis(duration) + "  " + dateTime + "  " + System.currentTimeMillis()
            )
            val workRequest = OneTimeWorkRequestBuilder<ScheduleNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            if (delay < 0) return

            val workManager = WorkManager.getInstance(IntelehealthApplication.getAppContext())
            workManager.enqueueUniqueWork(
                notificationData.visitUuid + " " + duration,
                ExistingWorkPolicy.KEEP,
                workRequest
            )

        }

        /**
         * handling notification with alarm manager
         */
        @JvmStatic
        fun scheduleNotificationAlarmManager(
            dateTime: Long,
            duration: Long,
            isFromBootComplete: Boolean,
            notificationData: FollowUpNotificationData,
        ) {
            val triggerTime:Long = dateTime - TimeUnit.HOURS.toMillis(duration)
            if (triggerTime < System.currentTimeMillis()) return
            val intent = Intent(
                IntelehealthApplication.getAppContext(),
                ScheduleNotificationReceiver::class.java
            )
                .apply {
                    //addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                    putExtra(
                        BundleKeys.TITLE,
                        IntelehealthApplication.getAppContext().getString(R.string.reminder)
                    )
                    putExtra(
                        BundleKeys.DESCRIPTION,
                        IntelehealthApplication.getAppContext().getString(
                            R.string.patient_follow_up_appointment_on,
                            notificationData.name,
                            notificationData.openMrsId,
                            DateAndTimeUtils.parseDateTimeToDateTime(notificationData.value)
                        )
                    )
                    putExtra(
                        BundleKeys.CHANNEL_ID,
                        "" + dateTime + "" + notificationData.patientUid
                    )
                    putExtra(BundleKeys.VISIT_UUI, notificationData.visitUuid)
                    putExtra(BundleKeys.NAME, notificationData.name)
                    putExtra(BundleKeys.NOTIFICATION_TRIGGER_TIME, triggerTime)
                    putExtra("key",UuidGenerator().UuidGenerator())
                }
            val requestCode = (System.currentTimeMillis()+duration).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                IntelehealthApplication.getAppContext(),
                intent.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = IntelehealthApplication.getAppContext()
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val notification = FollowUpNotificationShData(
                id = notificationData.visitUuid + "-" + duration,
                dateTime = dateTime.toString(),
                value = notificationData.value,
                duration = duration.toString(),
                name = notificationData.name,
                openMrsId = notificationData.openMrsId,
                patientUid = notificationData.patientUid,
                visitUuid = notificationData.visitUuid,
                requestCode = /*requestCode.toString()*/intent.hashCode().toString()
            );
            if (!isFromBootComplete) {
                FollowUpNotificationDAO().insertFollowupNotification(
                    notification
                )
            }

            CustomLog.d("TRIGGER_TIME", "" + triggerTime+" "+Gson().toJson(notificationData)+"  "+Gson().toJson(notification))
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        /**
         * cancelling notification here
         * it will trigger if any visit is ended
         */
        @JvmStatic
        fun cancelNotification(id: String) {
            val intent = Intent(
                IntelehealthApplication.getAppContext(),
                ScheduleNotificationReceiver::class.java
            )
            val notificationData = FollowUpNotificationDAO().getFollowupNotificationById(id)
            if(notificationData != null){
                val pendingIntent = PendingIntent.getBroadcast(
                    IntelehealthApplication.getAppContext(),
                    notificationData.requestCode.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

                )

                val alarmManager = IntelehealthApplication.getAppContext()
                    .getSystemService(Context.ALARM_SERVICE) as AlarmManager

                alarmManager.cancel(pendingIntent)
            }

        }
    }
}
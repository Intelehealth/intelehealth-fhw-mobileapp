package org.intelehealth.app.utilities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.FollowUpNotificationScheduleDAO
import org.intelehealth.app.models.FollowUpNotificationData
import org.intelehealth.app.models.FollowUpNotificationShData
import org.intelehealth.app.utilities.exception.DAOException
import org.intelehealth.app.worker.ScheduleNotificationBroadcasterReceiver
import org.intelehealth.app.worker.ScheduleNotificationWorker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
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
                Log.d("CCCCC", "" + followUpNotificationData.value)
                val followUpTime = parseDateTimeToTimestamp(followUpNotificationData.value)
                Log.d("CCCCCFoll", "" + followUpTime)
                if (followUpTime > System.currentTimeMillis()) {
                    val followupDateTimeBefore24H =
                            followUpTime - TimeUnit.HOURS.toMillis(AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION)

                    if (followupDateTimeBefore24H < System.currentTimeMillis()) {
                        val currentDateTimeWith2H =
                                followUpTime - TimeUnit.HOURS.toMillis(AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION)
                        if (currentDateTimeWith2H > System.currentTimeMillis()) {
                            scheduleNotification(
                                    followUpTime,
                                    AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                                    TimeUnit.HOURS,
                                    followUpNotificationData
                            )
                            Log.d("CCCCCFoll2h", "" + followUpTime)
                        }
                        scheduleNotification(
                                System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2),
                                0,
                                TimeUnit.HOURS,
                                followUpNotificationData
                        )
                        Log.d("CCCCCFollLes24", "" + followUpTime)
                    } else {
                        scheduleNotificationAlarmManager(
                                followUpTime,
                                AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                                TimeUnit.HOURS,
                                followUpNotificationData
                        )
                        scheduleNotificationAlarmManager(
                                followUpTime,
                                AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION,
                                TimeUnit.HOURS,
                                followUpNotificationData
                        )
                        Log.d("CCCCCFollElse", "" + followUpTime)
                    }
                }
                /*val notificationDataList = EncounterDAO.getFollowUpDateListFromConceptId()
                for (notificationData in notificationDataList) {
                    val followUpTime = parseDateTimeToTimestamp(notificationData.value)

                    if (followUpTime > System.currentTimeMillis() && followUpTime > 0) {
                        scheduleNotification(
                                followUpTime,
                                AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION,
                                TimeUnit.HOURS,
                                notificationData
                        )

                        scheduleNotification(
                                followUpTime,
                                AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION,
                                TimeUnit.HOURS,
                                notificationData
                        )
                    }
                }*/

            } catch (e: DAOException) {
                Log.e("ERRRR", e.message ?: "Err")
                throw RuntimeException(e)
            }


        }

        fun parseDateTimeToTimestamp(input: String): Long {
            return try {
                val formatter = SimpleDateFormat("yyyy-MM-dd, 'Time: 'hh:mm a", Locale.getDefault())
                val date = formatter.parse(input)
                date?.time ?: 0
            } catch (e: Exception) {
                Log.e("ERRRR", e?.message ?: "")
                0
            }
        }

        fun parseDateTimeToDateTime(input: String): String {

            val inputFormat = SimpleDateFormat("yyyy-MM-dd, 'Time: 'hh:mm a", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd 'at' h:mm a", Locale.ENGLISH)

            try {
                val date = inputFormat.parse(input)

                return outputFormat.format(date)

            } catch (e: ParseException) {
                Log.e("ERRRR", e?.message ?: "")
                e.printStackTrace()
            }
            return ""
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
                                    parseDateTimeToDateTime(notificationData.value)
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

            Log.d(
                    "DDDDDDEEEEE",
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

            /* FollowUpNotificationScheduleDAO().insertEncounter(
                 FollowUpNotificationShData(
                         notificationData.visitUuid + " " + duration,
                     (System.currentTimeMillis() + delay).toString()
                 )
             )*/
        }

        @JvmStatic
        fun scheduleNotificationAlarmManager(
                dateTime: Long,
                duration: Long,
                durationType: TimeUnit,
                notificationData: FollowUpNotificationData,
        ) {
            val intent = Intent(IntelehealthApplication.getAppContext(), ScheduleNotificationBroadcasterReceiver::class.java)
                    .apply {
                        putExtra(BundleKeys.TITLE, IntelehealthApplication.getAppContext().getString(R.string.reminder))
                        putExtra(BundleKeys.DESCRIPTION,
                                IntelehealthApplication.getAppContext().getString(
                                        R.string.patient_follow_up_appointment_on,
                                        notificationData.name,
                                        notificationData.openMrsId,
                                        parseDateTimeToDateTime(notificationData.value)
                                )
                        )
                        putExtra(BundleKeys.CHANNEL_ID, "" + dateTime + "" + notificationData.patientUid)
                        putExtra(BundleKeys.VISIT_UUI, notificationData.visitUuid)
                        putExtra(BundleKeys.NAME, notificationData.name)
                    }
            val pendingIntent = PendingIntent.getBroadcast(IntelehealthApplication.getAppContext(), System.currentTimeMillis().toInt(), intent, getPendingIntentFlag())

            val alarmManager = IntelehealthApplication.getAppContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = dateTime - TimeUnit.HOURS.toMillis(duration)

            FollowUpNotificationScheduleDAO().insertFollowupNotification(
                    FollowUpNotificationShData(
                            id = notificationData.visitUuid + " " + duration,
                            name = notificationData.name,

                    )
            )

            Log.d("DDDDD", "" + triggerTime)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}

private fun getPendingIntentFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    else PendingIntent.FLAG_UPDATE_CURRENT
}
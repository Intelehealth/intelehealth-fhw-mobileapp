package org.intelehealth.app.utilities

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
        fun scheduleFollowUpNotification() {

            try {
                val notificationDataList = EncounterDAO.getFollowUpDateListFromConceptId()
                for (notificationData in notificationDataList) {
                    val followUpTime = parseDateTimeToTimestamp(notificationData.value)

                    if (followUpTime > System.currentTimeMillis() && followUpTime > 0) {
                        WorkManager.getInstance(IntelehealthApplication.getAppContext()).cancelAllWorkByTag(notificationData.visitUuid)

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
                }

            } catch (e: DAOException) {
                throw RuntimeException(e)
            }


        }

        fun parseDateTimeToTimestamp(input: String): Long {
            return try {
                val datetimeRelevantPart = input.substringBefore(", Remark:")
                val formatter = SimpleDateFormat("dd-MM-yyyy, 'Time:'hh:mm a", Locale.getDefault())
                val date = formatter.parse(datetimeRelevantPart)
                date?.time ?: 0
            }catch (e:Exception){
                Log.e("ERRRR",e?.message?:"")
                0
            }
        }

        fun parseDateTimeToDateTime(input: String): String {

            val inputDateStr = input
            val datetimeRelevantPart = input.substringBefore(", Remark:")

            val inputFormat = SimpleDateFormat("dd-MM-yyyy, 'Time:'hh:mm a", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd 'at' h:mm a", Locale.ENGLISH)

            try {
                val date = inputFormat.parse(datetimeRelevantPart)

                return outputFormat.format(date)

            } catch (e: ParseException) {
                Log.e("ERRRR",e?.message?:"")
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
                .putString(BundleKeys.TITLE, IntelehealthApplication.getAppContext().getString(R.string.reminder))
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

            Log.d(
                "DDDDDDEEEEE",
                "" + delay + "  " + durationType.toMillis(duration) + "  " + dateTime
            )
            val workRequest = OneTimeWorkRequestBuilder<ScheduleNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(notificationData.visitUuid)
                .build()

            if (delay < 0) return

            val workManager = WorkManager.getInstance(IntelehealthApplication.getAppContext())
            workManager.enqueue(workRequest)

            /*    FollowUpNotificationScheduleDAO().insertEncounter(
                    FollowUpNotificationShData(
                        notificationData.visitUuid,
                        (System.currentTimeMillis() + delay).toString()
                    )
                )*/
        }
    }
}
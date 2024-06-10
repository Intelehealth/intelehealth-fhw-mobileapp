package org.intelehealth.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.intelehealth.app.R
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.notification.NotificationDAO
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.models.NotificationModel
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.fcm.utils.NotificationBroadCast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/**
 * Created by Tanvir Hasan on 26-05-2024 : 11-53.
 * Email: mhasan@intelehealth.org
 */
class ScheduleNotificationWorker(context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {
    override fun doWork(): Result {
        val title = inputData.getString(BundleKeys.TITLE)
        val description = inputData.getString(BundleKeys.DESCRIPTION)
        val channelId = inputData.getString(BundleKeys.CHANNEL_ID)
        val patientUuid = inputData.getString(BundleKeys.PATIENT_UUID) ?: ""
        val patientId = inputData.getString(BundleKeys.PATIENT_ID) ?: ""
        val visitUuid = inputData.getString(BundleKeys.VISIT_UUI) ?: ""
        val gender = inputData.getString(BundleKeys.GENDER) ?: ""
        val name = inputData.getString(BundleKeys.NAME) ?: ""
        val encounterTypeUid = inputData.getString(BundleKeys.ENCOUNTER_TYPE_UUID) ?: ""
        val conceptUuid = inputData.getString(BundleKeys.CONCEPT_UUID) ?: ""
        val encounterUuid = inputData.getString(BundleKeys.ENCOUNTER_UUID) ?: ""
        val value = inputData.getString(BundleKeys.VALUE) ?: ""
        val float_ageYear_Month = inputData.getString(BundleKeys.FLOAT_AGE_YEAR_MONTH) ?: ""


        sendNotification(
            title, description, channelId,
            patientUuid,
            name,
            gender,
            encounterTypeUid,
            visitUuid,
            conceptUuid,
            encounterUuid,
            value,
        )
        return Result.success()
    }

    private fun sendNotification(
        title: String?,
        description: String?,
        channelId: String?,
        patientUuid: String = "",
        name: String = "",
        gender: String = "",
        encounterTypeUid: String = "",
        visitUuid: String = "",
        conceptUuid: String = "",
        encounterUuid: String = "",
        value: String = "",
    ) {
        val id = System.currentTimeMillis().toInt()

        val intent = Intent(IntelehealthApplication.getAppContext(), VisitSummaryActivity_New::class.java).apply {
            putExtra("patientUuid", patientUuid)
            putExtra("visitUuid", visitUuid)
            putExtra("gender", gender)
            putExtra("name", name)
            putExtra("encounterUuidVitals", encounterUuid)
            putExtra("encounterUuidAdultIntial", encounterUuid)
            putExtra("float_ageYear_Month", 0)
            putExtra("tag", "Notification")
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            IntelehealthApplication.getAppContext(), System.currentTimeMillis().toInt(), intent,
            getPendingIntentFlag()
        )

        val notificationManager =
            IntelehealthApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder =
            NotificationCompat.Builder(IntelehealthApplication.getAppContext(), channelId ?: "")
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                description,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val list = mutableListOf<NotificationModel>()
        val notificationModel = NotificationModel()
        notificationModel.uuid = visitUuid +" "+ System.currentTimeMillis()
        notificationModel.patientuuid = patientUuid
        notificationModel.gender = gender
        notificationModel.first_name = name
        notificationModel.description = description
        notificationModel.notification_type = NotificationDbConstants.FOLLOW_UP_NOTIFICATION
        notificationModel.obs_server_modified_date = getFormatDateFromTimestamp()

        list.add(notificationModel)

        NotificationDAO().insertNotifications(list)
        NotificationBroadCast.initialize(IntelehealthApplication.getAppContext())

        notificationManager.notify(id, builder.build())
    }

    companion object{
        fun getFormatDateFromTimestamp(): String {
            val timestampMillis = System.currentTimeMillis()

            val date = Date(timestampMillis)

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            return sdf.format(date)
        }
    }

    private fun getPendingIntentFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT
    }
}
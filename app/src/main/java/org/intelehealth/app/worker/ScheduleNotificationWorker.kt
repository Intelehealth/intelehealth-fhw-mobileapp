package org.intelehealth.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.intelehealth.app.R
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New
import org.intelehealth.app.models.FollowUpNotificationData
import org.intelehealth.app.utilities.BundleKeys


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
        val patientUuid = inputData.getString("patientUuid") ?: ""
        val visitUuid = inputData.getString("visitUuid") ?: ""
        val gender = inputData.getString("gender") ?: ""
        val name = inputData.getString("name") ?: ""
        val encounterTypeUid = inputData.getString("encounterTypeUid") ?: ""
        val conceptUuid = inputData.getString("conceptUuid") ?: ""
        val encounterUuid = inputData.getString("encounterUuid") ?: ""
        val value = inputData.getString("value") ?: ""
        val float_ageYear_Month = inputData.getString("float_ageYear_Month") ?: ""
        sendNotification(
            title, description, channelId, FollowUpNotificationData(
                patientUuid,
                name,
                gender,
                encounterTypeUid,
                visitUuid,
                conceptUuid,
                encounterUuid,
                value,
            )
        )
        return Result.success()
    }

    private fun sendNotification(
        title: String?,
        description: String?,
        channelId: String?,
        notificationData: FollowUpNotificationData,
    ) {
        val id = System.currentTimeMillis().toInt()

        val intent = Intent(applicationContext, VisitSummaryActivity_New::class.java).apply {
            putExtra("patientUuid", notificationData.patientUid)
            putExtra("visitUuid", notificationData.visitUuid)
            putExtra("gender", notificationData.gender)
            putExtra("name", notificationData.name)
            putExtra("encounterUuidVitals", notificationData.encounterTypeUid)
            putExtra("encounterUuidAdultIntial", notificationData.encounterTypeUid)
            putExtra("float_ageYear_Month", 12)
            putExtra("tag", "VisitDetailsActivity")
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder =
            NotificationCompat.Builder(applicationContext, channelId ?: "")
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

        notificationManager.notify(id, builder.build())
    }
}
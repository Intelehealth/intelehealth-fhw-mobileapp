package org.intelehealth.app.notificationScheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import org.intelehealth.app.R
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.notification.NotificationDAO
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.models.NotificationModel
import org.intelehealth.app.utilities.BundleKeys
import org.intelehealth.app.utilities.NotificationUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.fcm.utils.NotificationBroadCast

class ScheduleNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val inputData = intent.extras;
        val title = inputData?.getString(BundleKeys.TITLE)
        val description = inputData?.getString(BundleKeys.DESCRIPTION)
        val channelId = inputData?.getString(BundleKeys.CHANNEL_ID)
        val visitUUid = inputData?.getString(BundleKeys.VISIT_UUI)
        val name = inputData?.getString(BundleKeys.NAME)
        val notificationTriggerTime = inputData?.getLong(BundleKeys.NOTIFICATION_TRIGGER_TIME)
        sendNotification(
            title, description, channelId, visitUUid, name
        )
    }

    private fun sendNotification(
        title: String?,
        description: String?,
        channelId: String?,
        visitUUid: String?,
        name: String?
    ) {

        val id = System.currentTimeMillis().toInt()

        val intent =
            Intent(IntelehealthApplication.getAppContext(), FollowUpPatientActivity_New::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            IntelehealthApplication.getAppContext(), System.currentTimeMillis().toInt(), intent,
            getPendingIntentFlag()
        )

        val notificationManager =
            IntelehealthApplication.getAppContext()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder =
            NotificationCompat.Builder(IntelehealthApplication.getAppContext(), channelId ?: "")
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                description,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val list = mutableListOf<NotificationModel>()
        val notificationModel = NotificationModel()
        notificationModel.uuid = visitUUid + " " + System.currentTimeMillis()
        notificationModel.first_name = name
        notificationModel.description = description
        notificationModel.notification_type = NotificationDbConstants.FOLLOW_UP_NOTIFICATION
        notificationModel.obs_server_modified_date =
            ScheduleNotificationWorker.getFormatDateFromTimestamp()

        list.add(notificationModel)

        NotificationDAO().insertNotifications(list)
        NotificationBroadCast.initialize(IntelehealthApplication.getAppContext())

        notificationManager.notify(id, builder.build())
    }

    private fun getPendingIntentFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT
    }


}
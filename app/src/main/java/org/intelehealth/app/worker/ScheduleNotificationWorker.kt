package org.intelehealth.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.intelehealth.app.R
import org.intelehealth.app.utilities.BundleKeys
import kotlin.math.round

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
        sendNotification(title, description,channelId)
        return Result.success()
    }

    private fun sendNotification(title:String?, description: String?, channelId: String?) {
        val id = System.currentTimeMillis().toInt()
        // Logic to send notification
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder =
            NotificationCompat.Builder(applicationContext, channelId?:"")
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher)
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
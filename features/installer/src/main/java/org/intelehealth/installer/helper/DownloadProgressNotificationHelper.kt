package org.intelehealth.installer.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.delay

/**
 * Created by Vaghela Mithun R. on 14-10-2024 - 18:44.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

@SuppressLint("MissingPermission")
class DownloadProgressNotificationHelper private constructor(context: Context) {
    private val channelId = "Dynamic Feature Download/Uninstall Channel"

    private val notificationManager by lazy {
        NotificationManagerCompat.from(context).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(
                    channelId, "Dynamic Feature Notification", NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Dynamic Feature Download/Uninstall Channel"
                    it.createNotificationChannel(this)
                }
            }
        }
    }

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(context, channelId).setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("Intelehealth").setContentText("Downloading").setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true).setProgress(100, 0, true).setOnlyAlertOnce(true).setAutoCancel(true)
    }

    fun setTitle(title: String) {
        notificationBuilder.setContentTitle(title)
    }

    fun setContent(content: String) {
        notificationBuilder.setContentText(content)
    }

    fun updateProgress(progress: Int) {
        notificationBuilder.setProgress(100, progress, false).setContentText("Downloading $progress%")
        notificationManager.notify(DYNAMIC_MODULE_DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun completeProgress() {
        notificationBuilder.setProgress(0, 0, false).setOngoing(false)
        notificationManager.notify(DYNAMIC_MODULE_DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun startNotifying() =
        notificationManager.notify(DYNAMIC_MODULE_DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build())

    private fun cancelNotification() = notificationManager.cancel(DYNAMIC_MODULE_DOWNLOAD_NOTIFICATION_ID)

    fun cancelWithDelay(milli: Long) {
        Thread(Runnable {
            SystemClock.sleep(milli)
            cancelNotification()
        }).start()
    }

    companion object {
        const val DYNAMIC_MODULE_DOWNLOAD_NOTIFICATION_ID = 5151

        @Volatile
        private var instance: DownloadProgressNotificationHelper? = null

        @JvmStatic
        fun getInstance(context: Context): DownloadProgressNotificationHelper = instance ?: synchronized(this) {
            instance ?: DownloadProgressNotificationHelper(context).also { instance = it }
        }
    }
}
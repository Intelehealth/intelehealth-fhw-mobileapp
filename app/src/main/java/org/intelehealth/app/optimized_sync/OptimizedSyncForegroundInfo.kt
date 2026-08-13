package org.intelehealth.app.optimized_sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import org.intelehealth.app.R

fun getOptimizedSyncForeground(context: Context) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ForegroundInfo(
            OptimizedSyncConstants.NOTIFICATION_ID,
            createNotification(context),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    } else {
        ForegroundInfo(
            OptimizedSyncConstants.NOTIFICATION_ID,
            createNotification(context)
        )
    }

private fun createNotification(context: Context): Notification {
    val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = getSyncNotificationChannel(context)
    service.createNotificationChannel(channel)
    return getSyncNotificationBuilder(context)
}

/**
 * The channel name is user-facing — it is what appears under the app's notification settings — so it
 * is resolved from resources like the notification's own text. Existing strings are reused rather than
 * new ones added, and the "Refresh" wording is deliberate: that is what the rest of the app calls this
 * to users, in every language it ships.
 */
private fun getSyncNotificationChannel(context: Context): NotificationChannel = NotificationChannel(
    OptimizedSyncConstants.SYNC_CHANNEL_ID,
    context.getString(R.string.sync),
    NotificationManager.IMPORTANCE_LOW
)

private fun getSyncNotificationBuilder(context: Context) =
    NotificationCompat.Builder(context, OptimizedSyncConstants.SYNC_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_baseline_refresh_24)
        .setContentTitle(context.getString(R.string.sync))
        .setContentText(context.getString(R.string.syncInProgress))
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setOngoing(true)
        .build()
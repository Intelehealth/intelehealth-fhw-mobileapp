package org.intelehealth.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import kotlin.random.Random

/**
 * Created by Vaghela Mithun R. on 08-09-2022.
 * vaghela.mithun@gmail.com
 */
class FcmNotification private constructor(val context: Context) {
    private var notificationChannel: NotificationChannel? = null
    private var channelId = "${context.applicationContext.packageName}$CHANNEL_ID_SUFFIX"
    private var notification: Notification? = null
    var notBuilder = NotificationCompat.Builder(context, channelId)

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun startNotify() {
        //for greater than oreo we need to create a notification channel
        println("startNotify")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(notificationChannel!!)
            notificationManager.notify(notificationId, notification)
            println("Build.VERSION_CODES.O")
            println("startNotify->notificationId=$notificationId")
        } else {
            println("startNotify->notificationId=$notificationId")
            notificationManager.notify(notificationId, notification)
        }
    }

    class Builder(val context: Context) {
        private val appNotification: FcmNotification = FcmNotification(context)
        private val notBuilder = NotificationCompat.Builder(context, appNotification.channelId)

        init {
            notificationId = Random(System.currentTimeMillis()).nextInt(RAN_NUM_MAX)
        }

        fun channelName(channelName: String): Builder {
            appNotification.notificationChannel = initChannel(channelName)
            return this
        }

        fun title(title: String): Builder {
            notBuilder.setContentTitle(title)
            return this;
        }

        fun content(message: String): Builder {
            notBuilder.setContentText(message)
            return this
        }

        fun bigContent(content: String): Builder {
            notBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
            return this
        }

        fun smallIcon(@DrawableRes icon: Int): Builder {
            notBuilder.setSmallIcon(icon)
            return this
        }

        fun bigIcon(@DrawableRes icon: Int): Builder {
            notBuilder.setLargeIcon(BitmapFactory.decodeResource(context.resources, icon))
            return this
        }

        fun contentIntent(pendingIntent: PendingIntent): Builder {
            notBuilder.setContentIntent(pendingIntent)
            return this
        }

        fun action(@DrawableRes icon: Int, label: String, actionIntent: PendingIntent): Builder {
            notBuilder.addAction(icon, label, actionIntent)
            return this
        }

        fun build(): FcmNotification {
            appNotification.notification = getNotification()
            return appNotification
        }

        private fun getNotification(): Notification {
            val ringToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            appNotification.notBuilder = notBuilder.apply {
                setCategory(Notification.CATEGORY_SERVICE)
                setVibrate(longArrayOf(VIBRATE, VIBRATE, VIBRATE, VIBRATE))
                setAutoCancel(true)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setSound(ringToneUri)
                setDefaults(NotificationCompat.DEFAULT_ALL)
                priority = NotificationCompat.PRIORITY_HIGH
            }

            return appNotification.notBuilder.build()
        }

        private fun initChannel(channelName: String): NotificationChannel? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    appNotification.channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )

                channel.enableLights(true)
                channel.lightColor = Color.BLUE
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                return channel
            }

            return null
        }
    }

    companion object {
        const val VIBRATE = 1000L
        private const val RAN_NUM_MAX = 10000
        private const val CHANNEL_ID_SUFFIX = "_channel"
        var notificationId = Random(System.currentTimeMillis()).nextInt(RAN_NUM_MAX)

        fun cancelNotification(context: Context, notificationId: Int) {
            val notManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notManager.cancel(notificationId)
        }
    }
}
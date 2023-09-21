package org.intelehealth.klivekit.call.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.torvis.pavo.R
import com.torvis.pavo.home.view.fragment.chat.model.CallNotificationMessageBody
import com.torvis.pavo.util.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/**
 * Created by Vaghela Mithun R. on 8/28/2021.
 * vaghela@codeglo.com
 */
object NotificationHandlerUtils {

    const val NOTIFICATION_CHANNEL_ID = "notification_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Pavo"

    /**
     * Retrieve NotificationManager instance
     * @param context service context
     * @return NotificationManager instance
     */
    fun getNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Cancel running notification
     * @param notificationId cancelable notification id
     * @param context service context
     */
    fun cancelNotification(notificationId: Int, context: Context) {
        getNotificationManager(context).cancel(notificationId)
    }

    /**
     * An decline action for incoming call
     * @param context Context of current scope
     * @param messageBody an instance of CallNotificationMessageBody to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    private fun getDeclineAction(
        context: Context,
        messageBody: CallNotificationMessageBody
    ) = NotificationCompat.Action.Builder(
        android.R.drawable.ic_menu_call,
        ACTION_DECLINE.span(android.R.color.holo_red_light, context),
        IntentUtils.getDeclinePendingBroadCastIntent(context, messageBody.apply {
            action = CALL_ACTION_DECLINE
            messageType = CALL_BUSY
        })
    ).build()

    /**
     * An accept action for incoming call
     * @param context Context of current scope
     * @param messageBody an instance of CallNotificationMessageBody to send with intent
     * @return PendingIntent type of ChatCallBroadCastReceiver intent
     */
    private fun getAcceptAction(
        context: Context,
        messageBody: CallNotificationMessageBody
    ) = NotificationCompat.Action.Builder(
        android.R.drawable.ic_menu_call,
        ACTION_ACCEPT.span(android.R.color.holo_green_dark, context),
        IntentUtils.getAcceptPendingBroadCastIntent(context, messageBody.apply {
            action = CALL_ACTION_ACCEPT
            messageType = CALL_NONE
        })
    ).build()

    /**
     * hangup the call
     * @param [context] service context
     *
     * */
    fun getHangUpAction(
        context: Context,
        messageBody: CallNotificationMessageBody
    ): NotificationCompat.Action = NotificationCompat.Action.Builder(
        android.R.drawable.ic_menu_call,
        ACTION_HANGUP,
        IntentUtils.getBroadCastIntent(context, messageBody)
    ).build()

    /**
     * hangup the call
     * @param [context] service context
     *
     * */
    private fun getCallAction(
        context: Context,
        messageBody: CallNotificationMessageBody
    ): NotificationCompat.Action = NotificationCompat.Action.Builder(
        android.R.drawable.ic_menu_call,
        ACTION_CALL,
        IntentUtils.getOutGoingCallIntent(context, messageBody)
    ).build()


    /**
     * Build call notification while user trying to connect
     * @param context Context of current scope
     * @param messageBody an instance of CallNotificationMessageBody to send with intent
     * @return NotificationCompat.Builder
     */
    fun outGoingCallNotificationBuilder(
        messageBody: CallNotificationMessageBody,
        context: Context
    ): NotificationCompat.Builder {

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle(messageBody.username)
            .setContentText("Calling")
            .setNotificationSilent()
            .setColor(ContextCompat.getColor(context, R.color.blue_color))
            .setSmallIcon(R.drawable.pavo_notification1)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .addAction(getHangUpAction(context, messageBody.apply { action = CALL_ACTION_HANGUP }))
    }

    /**
     * Build incoming call notification with accept and decline action when received
     * incoming call
     * @param context Context of current scope
     * @param messageBody an instance of CallNotificationMessageBody to send with intent
     * @return NotificationCompat.Builder
     */
    fun getIncomingNotificationBuilder(
        context: Context,
        messageBody: CallNotificationMessageBody
    ): NotificationCompat.Builder {
        val lockScreenIntent = IntentUtils.getPendingActivityIntent(context, messageBody)

        val notificationIntent = IntentUtils.getPendingBroadCastIntent(context, messageBody)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentTitle("Pavo")
            .setContentText("Incoming call from ${messageBody.username ?: "unknown"}")
            .setColor(ContextCompat.getColor(context, R.color.blue_color))
            .setSmallIcon(R.drawable.pavo_notification1)
            .setSound(getDefaultRingtoneUrl())
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(notificationIntent)
            .setFullScreenIntent(lockScreenIntent, true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .addAction(getDeclineAction(context, messageBody))
            .addAction(getAcceptAction(context, messageBody))
    }

    /**
     * Build on going call notification when user accept or attend any incoming call
     * @param context Context of current scope
     * @param messageBody an instance of CallNotificationMessageBody to send with intent
     * @return NotificationCompat.Builder
     */
    fun getAttendedCallNotificationBuilder(
        context: Context,
        messageBody: CallNotificationMessageBody
    ): NotificationCompat.Builder {

        messageBody.notificationTime = SystemClock.elapsedRealtime().toString()
        messageBody.callStatus = CALL_ONGOING
        val notificationIntent = IntentUtils.getPendingBroadCastIntent(context, messageBody)

        Timber.d("Local time date ***** ${messageBody.notificationTime}")

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle("Ongoing call")
            .setColor(ContextCompat.getColor(context, R.color.blue_color))
            .setSmallIcon(R.drawable.pavo_notification1)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(notificationIntent)
            //.setUsesChronometer(true)
            .setNotificationSilent()
            .addAction(getHangUpAction(context, messageBody.apply {
                action = CALL_ACTION_HANGUP
                messageType = CALL_NONE
            }))
    }

    /**
     * Generate NotificationChannel with priority and display setting
     * @param priority notification display priority setting
     * @return NotificationChannel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotificationChannel(priority: Int): NotificationChannel {

        return NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(false)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            if (priority == 1) {
                setSound(getDefaultRingtoneUrl(), NotificationHandlerUtils.getAudioAttributes())
            }
        }
    }

    /**
     * Build missed call notification when user accept or attend any incoming call
     * @param context Context of current scope
     * @param messageBody an instance of CallNotificationMessageBody to send with intent
     * @return NotificationCompat.Builder
     */
    fun buildMissedCallNotification(
        context: Context,
        messageBody: CallNotificationMessageBody
    ): NotificationCompat.Builder {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        messageBody.notificationTime = sdf.format(Date())
        messageBody.messageType = CALL_MISSED
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle("Missed call from ${messageBody.username}")
            .setColor(ContextCompat.getColor(context, R.color.blue_color))
            .setSmallIcon(R.drawable.pavo_notification1)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(IntentUtils.getChatViewPendingActivityIntent(context, messageBody))
            .setNotificationSilent()
            .addAction(getCallAction(context, messageBody))
    }

    fun notifyMissedCall(context: Context, messageBody: CallNotificationMessageBody) {

        val notificationManager = getNotificationManager(context)
        messageBody.notificationId = Random(System.currentTimeMillis()).nextInt(MAX_INT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(getNotificationChannel(2))
        }

        notificationManager.notify(
            messageBody.notificationId,
            buildMissedCallNotification(context, messageBody).build()
        )
    }


    /**
     * Build AudioAttributes
     * @return AudioAttributes for notification ringtone
     */
    fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
    }

    /**
     * To get default device ringtone uri using RingtoneManager
     * @return URI with default ringtone uri
     */
    private fun getDefaultRingtoneUrl() =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)


}
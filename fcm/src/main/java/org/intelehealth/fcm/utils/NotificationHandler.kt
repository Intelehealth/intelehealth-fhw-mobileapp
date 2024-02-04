package org.intelehealth.fcm.utils

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.RemoteMessage.Notification
import com.google.gson.Gson
import org.intelehealth.fcm.FcmBroadcastReceiver
import org.intelehealth.fcm.FcmNotification
import org.intelehealth.fcm.model.NotificationBuilderParam
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by Vaghela Mithun R. on 08-09-2022.
 * vaghela.mithun@gmail.com
 */
//https://stackoverflow.com/questions/40311279/firebase-onmessagereceived-not-called-when-app-is-in-the-background
object NotificationHandler {

    fun isValidJson(json: String): Boolean {
        try {
            JSONObject(json)
        } catch (e: JSONException) {
            return false
        }
        return true
    }

    private fun buildIntentExtra(
        notification: String? = null,
        data: String? = null,
        intent: Intent
    ) {
        println("buildIntentExtra :: $notification")
        println("buildIntentExtra :: $data")
        intent.putExtra(FcmConstants.FCM_DATA_PAYLOAD, data)
        intent.putExtra(FcmConstants.FCM_NOTIFICATION_PAYLOAD, notification)
    }

    fun generateSimpleNotification(
        context: Context,
        notification: Notification? = null,
        data: HashMap<String, String>,
        intent: Intent, smallIcon: Int
    ) {
        var title = data[FcmConstants.FCM_TITLE_KEY]
        var body = data[FcmConstants.FCM_BODY_KEY]

        if (notification != null) {
            title = notification.title
            body = notification.body
        }

        buildIntentExtra(
            notification = Gson().toJson(notification),
            data = Gson().toJson(data),
            intent = intent
        )
        simpleNotify(intent, context, title!!, body!!, smallIcon)
    }

    private fun simpleNotify(
        intent: Intent,
        context: Context,
        title: String,
        message: String,
        smallIcon: Int
    ) {
        println("simpleNotify ::$message")
        generateNotification(
            context, NotificationBuilderParam(
                intent = intent,
                title = title,
                message = message,
                icon = smallIcon
            )
        )
    }

    private fun generateNotification(context: Context, param: NotificationBuilderParam) {
        FcmNotification.Builder(context)
            .channelName(FcmConstants.NOT_CHANNEL_NAME)
            .title(param.title)
            .smallIcon(param.icon)
            .content(param.message)
            .contentIntent(getPendingIntentWithParentStack(context, param.intent.apply {
                putExtra(FcmConstants.NOTIFICATION_ID, FcmNotification.notificationId)
            }))
            .action(
                param.icon,
                param.action,
                getPendingIntentWithParentStack(context, param.intent.apply {
                    putExtra(FcmConstants.NOTIFICATION_ID, FcmNotification.notificationId)
                })
            )
            .build().startNotify()
    }

    fun getPendingIntentBroadcast(
        context: Context,
        notification: String?, data: String?,
        clazz: Class<*>
    ): PendingIntent {
        println("getPendingIntentBroadcast :: $notification")
        println("getPendingIntentBroadcast :: $data")
        return PendingIntent.getBroadcast(
            context, FcmBroadcastReceiver.REQUEST_CODE,
            getBroadcastIntent(context, notification, data, clazz),
            getPendingIntentFlag()
        )
    }

    fun getPendingIntentWithParentStack(context: Context, intent: Intent): PendingIntent {
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addNextIntentWithParentStack(intent)

        return taskStackBuilder.getPendingIntent(
            FcmBroadcastReceiver.REQUEST_CODE,
            getPendingIntentFlag()
        )
    }

    @JvmStatic
    fun getPendingIntentFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    fun getLocalNotificationBroadcast(
        context: Context,
        notification: Notification? = null,
        data: Map<String, String>? = null
    ): Intent {
        return getBroadcastIntent(
            context,
            Gson().toJson(notification),
            Gson().toJson(data)
        ).apply {
            action = FcmConstants.getForegroundBroadcastAction(context)
            println("broadcast action:: $action")
        }
    }

    private fun getBroadcastIntent(
        context: Context,
        notification: String?,
        data: String?,
        clazz: Class<*>? = null
    ): Intent {
        println("getBroadcastIntent :: $notification")
        println("getBroadcastIntent :: $data")
        return clazz?.let {
            return@let Intent(context, clazz).apply {
                println("getBackgroundIntent class ::${clazz.canonicalName}")
                action = FcmConstants.getBackgroundBroadcastAction(context)
                buildIntentExtra(notification, data, this)
            }
        } ?: Intent().apply {
            action = FcmConstants.getBackgroundBroadcastAction(context)
            println("broadcast action:: $action")
            buildIntentExtra(notification, data, this)
        }
    }

    fun isAppInForeground() = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
        Lifecycle.State.RESUMED
    )

    fun isAppInBackground() = !isAppInForeground()
}
package org.intelehealth.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import org.intelehealth.fcm.utils.FcmConstants
import org.intelehealth.fcm.utils.NotificationHandler
import com.google.firebase.messaging.RemoteMessage.Notification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


/**
 * Created by Vaghela Mithu R. on 01-12-2022 - 19:01.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
abstract class FcmBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val REQUEST_CODE = 10001
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        println("FcmBroadcastReceiver")
        intent?.let {
            val action = FcmConstants.getBackgroundBroadcastAction(context!!)
            println("Action => $action")
            println("Intent Action => ${it.action}")
            if (it.action.equals(action, false)) {
                println("FcmBroadcastReceiver ACTION +++ $action")
                hasValidExtras(intent) {
                    extractIntentExtras(context, intent)
                }
            }
        }
    }

    private fun hasValidExtras(intent: Intent, onValid: () -> Unit) {
        if (intent.hasExtra(FcmConstants.FCM_NOTIFICATION_PAYLOAD)
            && intent.hasExtra(FcmConstants.FCM_DATA_PAYLOAD)
        ) {
            onValid.invoke()
        }
    }

    private fun extractIntentExtras(context: Context?, intent: Intent) {
        val jsonNotification = intent.getStringExtra(FcmConstants.FCM_NOTIFICATION_PAYLOAD)
        val jsonData = intent.getStringExtra(FcmConstants.FCM_DATA_PAYLOAD)
        var notification: Notification? = null
        var data = HashMap<String, String>()
        val gson = Gson()
        jsonNotification?.let {
            NotificationHandler.isValidJson(jsonNotification).apply {
                if (this) notification = gson.fromJson(jsonNotification, Notification::class.java)
            }
        }

        val type: Type = object : TypeToken<HashMap<String, String>>() {}.type
        jsonData?.let {
            NotificationHandler.isValidJson(it).apply {
                if (this) data = gson.fromJson(jsonData, type)
            }
        }

        onMessageReceived(context, notification, data)
    }

    fun isAppInForeground() = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
        Lifecycle.State.RESUMED
    )

    abstract fun onMessageReceived(
        context: Context?,
        notification: Notification?,
        data: HashMap<String, String>
    )
}
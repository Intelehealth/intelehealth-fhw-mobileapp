package org.intelehealth.fcm

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import org.intelehealth.fcm.utils.NotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.intelehealth.fcm.utils.NotificationBroadCast


/**
 * Created by Vaghela Mithu R. on 01-12-2022 - 18:07.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
abstract class FBMessageService(private val clazz: Class<*>) : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val gson = Gson()

//        println("RemoteMessage :: ${gson.toJson(message)}")
        val notification = message.notification?.let {
            return@let gson.toJson(it)
        }

        println("notification :: $notification")

        val data = gson.toJson(message.data)
        println("FBMessageService isAppInBackground")
        println("data $data")
        NotificationBroadCast.initialize(this)

        NotificationHandler.getPendingIntentBroadcast(
            context = this,
            notification = notification,
            data = data,
            clazz = clazz
        ).send()

//        if (isAppInForeground()) {
//            println("FBMessageService isAppInForeground")
//            NotificationHandler.getLocalNotificationBroadcast(
//                context = this,
//                notification = notification,
//                data = message.data
//            )
//        } else {
//            println("FBMessageService isAppInBackground")
//            NotificationHandler.getPendingIntentBroadcast(
//                context = this,
//                notification = notification,
//                data = data
//            ).send()
//        }

        println("FBMessageService calling")
    }

    private fun isAppInForeground() = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
        Lifecycle.State.RESUMED
    )
}
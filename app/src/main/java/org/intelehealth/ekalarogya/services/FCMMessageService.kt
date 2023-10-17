package org.intelehealth.ekalarogya.services

import com.github.ajalt.timberkt.Timber
import org.intelehealth.fcm.FBMessageService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.intelehealth.ekalarogya.webrtc.receiver.FCMNotificationReceiver

/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:16.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FCMMessageService : FBMessageService(FCMNotificationReceiver::class.java) {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d { "onNewToke ---> $token" }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d { "Remote message ${Gson().toJson(message)}" }
    }
}
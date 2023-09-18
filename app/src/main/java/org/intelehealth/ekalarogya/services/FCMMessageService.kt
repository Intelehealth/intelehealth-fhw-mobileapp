package org.intelehealth.ekalarogya.services

import android.annotation.SuppressLint
import com.codeglo.fcm.FBMessageService
import com.google.firebase.messaging.RemoteMessage
import org.intelehealth.ekalarogya.webrtc.receiver.FCMNotificationReceiver
import timber.log.Timber

/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:16.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FCMMessageService : FBMessageService(FCMNotificationReceiver::class.java) {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("onNewToke ---> $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}
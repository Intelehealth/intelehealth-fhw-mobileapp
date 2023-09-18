package org.intelehealth.ekalarogya.webrtc.receiver

import android.content.Context
import com.codeglo.fcm.FcmBroadcastReceiver
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FCMNotificationReceiver: FcmBroadcastReceiver() {
    override fun onMessageReceived(
        context: Context?,
        notification: RemoteMessage.Notification?,
        data: HashMap<String, String>
    ) {

    }
}
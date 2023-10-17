package org.intelehealth.ekalarogya.webrtc.receiver

import android.content.Context
import org.intelehealth.fcm.FcmBroadcastReceiver
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.intelehealth.ekalarogya.webrtc.activity.EkalVideoActivity
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.extensions.fromJson

/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:14.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FCMNotificationReceiver : FcmBroadcastReceiver() {
    override fun onMessageReceived(
        context: Context?,
        notification: RemoteMessage.Notification?,
        data: HashMap<String, String>
    ) {
        context?.let {
            val rtcArg: RtcArgs = Gson().fromJson(Gson().toJson(data))
            CallHandlerUtils.operateIncomingCall(it, rtcArg, EkalVideoActivity::class.java)
        }
    }
}
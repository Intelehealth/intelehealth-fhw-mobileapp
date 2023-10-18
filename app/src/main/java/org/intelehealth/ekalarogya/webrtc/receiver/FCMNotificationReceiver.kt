package org.intelehealth.ekalarogya.webrtc.receiver

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.intelehealth.ekalarogya.firebase.RealTimeDataChangedObserver
import org.intelehealth.ekalarogya.utilities.SessionManager
import org.intelehealth.ekalarogya.webrtc.activity.EkalVideoActivity
import org.intelehealth.fcm.FcmBroadcastReceiver
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.call.utils.CallType
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.Constants
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
            if (data.containsKey("type") && data["type"].equals("video_call")) {
                val sessionManager = SessionManager(context)
                Gson().fromJson<RtcArgs>(Gson().toJson(data)).apply {
                    nurseName = sessionManager.chwname
                    callType = CallType.VIDEO
                    url = "wss://" + sessionManager.serverUrl + ":9090"
                    socketUrl = Constants.BASE_URL + "?userId=" + nurseId + "&name=" + nurseName
                }.also { arg ->
                    CallHandlerUtils.operateIncomingCall(it, arg, EkalVideoActivity::class.java)
                }
            }
        }
    }

}
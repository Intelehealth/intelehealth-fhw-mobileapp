package org.intelehealth.app.webrtc.receiver

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.webrtc.activity.EkalVideoActivity
import org.intelehealth.fcm.FcmBroadcastReceiver
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.call.utils.CallMode
import org.intelehealth.klivekit.call.utils.CallStatus
import org.intelehealth.klivekit.call.utils.CallType
import org.intelehealth.klivekit.call.utils.IntentUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.Constants
import org.intelehealth.klivekit.utils.extensions.fromJson
import java.util.HashMap

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
        val sessionManager = SessionManager(context)
        if (sessionManager.isLogout) return
        context?.let {
            if (data.containsKey("type") && data["type"].equals("video_call")) {

                Gson().fromJson<RtcArgs>(Gson().toJson(data)).apply {
                    nurseName = sessionManager.chwname
                    callType = CallType.VIDEO
                    url = "wss://" + sessionManager.serverUrl + ":9090"
                    socketUrl = Constants.BASE_URL + "?userId=" + nurseId + "&name=" + nurseName
                    PatientsDAO().getPatientName(roomId).apply {
                        patientName = get(0).name
                    }
                }.also { arg ->
                    if (isAppInForeground()) {
                        arg.callMode = CallMode.INCOMING
                        arg.className = EkalVideoActivity::class.java.name
                        CallHandlerUtils.saveIncomingCall(context, arg)
                        context.startActivity(IntentUtils.getCallActivityIntent(arg, context))
                    } else {
                        CallHandlerUtils.operateIncomingCall(it, arg, EkalVideoActivity::class.java)
                    }
                }
            }
        }
    }

}
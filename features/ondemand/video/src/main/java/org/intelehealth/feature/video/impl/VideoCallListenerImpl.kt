package org.intelehealth.feature.video.impl

import android.content.Context
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.core.utils.extensions.fromJson
import org.intelehealth.fcm.utils.NotificationHandler.isAppInForeground
import org.intelehealth.features.ondemand.mediator.listener.VideoCallListener
import org.intelehealth.feature.video.model.CallArgs
import org.intelehealth.feature.video.utils.CallHandlerUtils
import org.intelehealth.feature.video.utils.CallIntentUtils
import org.intelehealth.feature.video.utils.CallMode
import org.intelehealth.feature.video.utils.CallType

/**
 * Created by Vaghela Mithun R. on 26-09-2024 - 18:12.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VideoCallListenerImpl: VideoCallListener {
    override fun onIncomingCall(context: Context?, data: HashMap<String, String>) {
        context?.let {
            val sessionManager = SessionManager(context)
            Gson().fromJson<CallArgs>(Gson().toJson(data)).apply {
                nurseName = sessionManager.chwname
                callType = CallType.VIDEO
                url = BuildConfig.LIVE_KIT_URL
                socketUrl =
                    BuildConfig.SOCKET_URL + "?userId=" + nurseId + "&name=" + nurseName
                PatientsDAO().getPatientName(roomId).apply {
                    patientName = get(0).name
                }
            }.also { arg ->
                Timber.tag(TAG).d("onMessageReceived: $arg")
                if (isAppInForeground()) {
                    arg.callMode = CallMode.INCOMING
                    CallHandlerUtils.saveIncomingCall(context, arg)
                    context.startActivity(CallIntentUtils.getCallActivityIntent(arg, context))
                } else {
                    CallHandlerUtils.operateIncomingCall(it, arg)
                }
            }
        }
    }

    override fun testMethod() = TAG

    companion object{
        const val TAG = "VideoCallListenerImpl"
    }
}
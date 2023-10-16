package org.intelehealth.ekalarogya.webrtc.activity

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import org.intelehealth.ekalarogya.R
import org.intelehealth.ekalarogya.shared.BaseActivity
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.extensions.fromJson

/**
 * Created by Vaghela Mithun R. on 10-10-2023 - 18:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallNotificationTestActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_notification_test)
        findViewById<MaterialButton>(R.id.btnStartCallNotification).setOnClickListener {
            startCall()
        }
    }

    private fun startCall() {
        CallHandlerUtils.operateIncomingCall(
            this,
            RtcArgs.dummy().apply {  },
            EkalVideoActivity::class.java
        )
    }
}
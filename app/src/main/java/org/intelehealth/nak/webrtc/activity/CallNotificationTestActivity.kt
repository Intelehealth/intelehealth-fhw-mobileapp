package org.intelehealth.nak.webrtc.activity

import android.os.Bundle
import com.github.ajalt.timberkt.Timber
import com.google.android.material.button.MaterialButton
import org.intelehealth.klivekit.call.ui.custom.FabSwipeable
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.nak.R

/**
 * Created by Vaghela Mithun R. on 10-10-2023 - 18:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallNotificationTestActivity : BaseActivity(), FabSwipeable.SwipeEventListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_incoming_call_ringing)
//        findViewById<MaterialButton>(R.id.btnStartCallNotification).setOnClickListener {
//            startCall()
//        }
    }


    private fun startCall() {
        CallHandlerUtils.operateIncomingCall(
            this,
            RtcArgs.dummy().apply { patientName = "Test User" },
            NammaVideoActivity::class.java
        )
    }

    override fun onTap() {
        Timber.tag("Test").d("onTap")
    }

    override fun onReleased() {
        Timber.tag("Test").d("onRelease")
    }

    override fun onSwipe() {
        Timber.tag("Test").d("onSwipe")
    }

    override fun onCompleted() {
        Timber.tag("Test").d("onCompleted")
    }
}
package org.intelehealth.ezazi.ui.rtc.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.VideoTrack
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.ActivityVideoCallEzaziBinding
import org.intelehealth.klivekit.ui.activity.CoreVideoCallActivity

/**
 * Created by Vaghela Mithun R. on 25-05-2023 - 14:44.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
open class LiveVideoCallActivity : CoreVideoCallActivity() {
    private lateinit var binding: ActivityVideoCallEzaziBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVideoCallEzaziBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        if (isArgsInitiate()) {
            setupActionBar()
            initView()
            initClickListener()
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.actionBarView.toolbar)
        binding.actionBarView.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initView() {
        args.doctorName?.let {
            if (it.startsWith("Dr")) it
            else "Dr $it"
        }.also {
            title = if (args.isIncomingCall) "Incoming Call" else "Ongoing Call"
            binding.incomingCallView.callerNameTv.text = it
            binding.videoCallView.tvRemoteUsername.text = it
        }
    }

    private fun initClickListener() {
        binding.videoCallView.callActionView.btnCallEnd.setOnClickListener { endCall() }
        binding.videoCallView.callActionView.btnMicOnOff.setOnClickListener { videoCallViewModel.toggleMicrophone() }
        binding.videoCallView.callActionView.btnVideoOnOff.setOnClickListener { videoCallViewModel.toggleCamera() }
        binding.videoCallView.callActionView.btnFlipCamera.setOnClickListener { videoCallViewModel.flipCamera() }
        binding.incomingCallView.inCallRejectImv.setOnClickListener { declineCall() }
        binding.incomingCallView.inCallRejectImv.setOnClickListener { acceptCall() }
    }

    override fun onIncomingCall() {
        super.onIncomingCall()
        binding.incomingCallView.root.visibility = View.VISIBLE
        binding.incomingCallView.rippleBackgroundContent.startRippleAnimation()
        playRingtone()
    }

    override fun onCallAccept() {
        super.onCallAccept()
        binding.incomingCallView.root.visibility = View.GONE
        binding.incomingCallView.rippleBackgroundContent.stopRippleAnimation()
    }

    override fun onCallDecline() {
        super.onCallDecline()
        binding.incomingCallView.rippleBackgroundContent.stopRippleAnimation()
    }

    override fun onCallEnd() {
        super.onCallEnd()
        binding.videoCallView.callActionView.btnCallEnd.performClick()
    }

    override fun onCallCountDownStart(duration: String) {
        super.onCallCountDownStart(duration)
    }

    override fun attachLocalVideo(videoTrack: VideoTrack) {
        videoTrack.addRenderer(binding.videoCallView.selfSurfaceView)
    }

    override fun attachRemoteVideo(videoTrack: VideoTrack) {
        videoTrack.addRenderer(binding.videoCallView.incomingSurfaceView)
    }

    override fun getLocalVideoRender(): SurfaceViewRenderer = binding.videoCallView.selfSurfaceView

    override fun getRemoteVideoRender(): SurfaceViewRenderer = binding.videoCallView.selfSurfaceView

    override fun onCameraStatusChanged(enabled: Boolean) {
        super.onCameraStatusChanged(enabled)
        binding.videoCallView.callActionView.btnVideoOnOff.isActivated = enabled.not()
        binding.videoCallView.selfSurfaceView.isVisible = enabled.not()
        binding.videoCallView.selfSurfaceView.isVisible = enabled
    }

    override fun onCameraPositionChanged(cameraPosition: CameraPosition) {
        super.onCameraPositionChanged(cameraPosition)
        binding.videoCallView.callActionView.btnFlipCamera.isActivated =
            cameraPosition == CameraPosition.BACK
    }

    override fun onConnectivityChanged(it: ConnectionQuality?) {
        super.onConnectivityChanged(it)
        binding.videoCallView.statusTv.isVisible = it == ConnectionQuality.POOR
    }

    override fun onLocalParticipantSpeaking(isSpeaking: Boolean) {
        super.onLocalParticipantSpeaking(isSpeaking)
        binding.videoCallView.ivLocalSpeakerStatus.isActivated = isSpeaking
    }

    override fun onMicrophoneStatusChanged(status: Boolean) {
        super.onMicrophoneStatusChanged(status)
        binding.videoCallView.callActionView.btnMicOnOff.isActivated = status.not()
        getCurrentMicStatusIcon(status.not())?.let {
            binding.videoCallView.ivLocalSpeakerStatus.setImageDrawable(it)
        }
    }

    override fun onRemoteParticipantCameraChange(isHide: Boolean) {
        super.onRemoteParticipantCameraChange(isHide)
        binding.videoCallView.incomingSurfaceView.isVisible = isHide
        binding.videoCallView.ivSelfProfile.isVisible = isHide.not()
    }

    override fun onRemoteParticipantMicChange(isMuted: Boolean) {
        super.onRemoteParticipantMicChange(isMuted)
        getCurrentMicStatusIcon(isMuted)?.let {
            binding.videoCallView.ivRemoteSpeakerStatus.setImageDrawable(it)
        }
    }

    override fun onRemoteParticipantSpeaking(isSpeaking: Boolean) {
        super.onRemoteParticipantSpeaking(isSpeaking)
        binding.videoCallView.ivRemoteSpeakerStatus.isActivated = isSpeaking
    }

    private fun getCurrentMicStatusIcon(isMuted: Boolean): Drawable? {
        return if (isMuted) {
            ContextCompat.getDrawable(
                this@LiveVideoCallActivity,
                R.drawable.ic_mic_off
            )
        } else {
            ContextCompat.getDrawable(
                this@LiveVideoCallActivity,
                R.drawable.selector_active_speaker
            )
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showAppClosingDialog()
            }
        }

    private fun showAppClosingDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(R.string.call_end_aler_txt)
            setPositiveButton(R.string.yes) { _, _ ->
                sayBye("app")
            }
            setNegativeButton(R.string.no, null)
        }.create().show()
    }

    companion object {
        private const val TAG = "LiveVideoCallActivity"
    }
}
package org.intelehealth.klivekit.call.ui.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.VideoTrack
import org.intelehealth.klivekit.R
import org.intelehealth.klivekit.databinding.ActivityVideoCallBinding

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 11:06.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VideoCallActivity : CoreVideoCallActivity() {
    private lateinit var binding: ActivityVideoCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        if (isArgsInitiate()) {
            initView()
            initClickListener()
        }
    }

    private fun initView() {
        args.doctorName?.let {
            if (it.startsWith("Dr")) it
            else "Dr $it"
        }.also {
            binding.tvDoctorName.text = it
            binding.callingLayout.callerNameTv.text = it
        }
    }

    private fun initClickListener() {
        binding.actionView.callEndImv.setOnClickListener { endCall() }
        binding.actionView.audioImv.setOnClickListener { videoCallViewModel.toggleMicrophone() }
        binding.actionView.videoImv.setOnClickListener { videoCallViewModel.toggleCamera() }
        binding.actionView.flipImv.setOnClickListener { videoCallViewModel.flipCamera() }
        binding.callingLayout.btnCallReject.setOnClickListener { declineCall() }
        binding.callingLayout.btnCallAccept.setOnClickListener { acceptCall() }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MaterialAlertDialogBuilder(this@VideoCallActivity).apply {
                    setMessage(R.string.call_end_aler_txt)
                    setPositiveButton(R.string.yes) { _, _ ->
                        sayBye("Call ended by you")
                    }
                    setNegativeButton(R.string.no, null)
                }.create().show()
            }
        })

    }

    override fun onIncomingCall() {
        super.onIncomingCall()
        binding.callingLayout.root.visibility = View.VISIBLE
        binding.callingLayout.rippleBackgroundContent.startRippleAnimation()
        playRingtone()
    }

    override fun onCallAccept() {
        super.onCallAccept()
        binding.callingLayout.root.visibility = View.GONE
        binding.callingLayout.rippleBackgroundContent.stopRippleAnimation()
    }

    override fun onCallDecline() {
        super.onCallDecline()
        binding.callingLayout.rippleBackgroundContent.stopRippleAnimation()
    }

    override fun onCallEnd() {
        super.onCallEnd()
        binding.actionView.callEndImv.performClick()
    }

    override fun onCallCountDownStart(duration: String) {
        super.onCallCountDownStart(duration)
        binding.tvTimer.text = duration
    }

    override fun attachLocalVideo(videoTrack: VideoTrack) {
        videoTrack.addRenderer(binding.selfSurfaceView)
    }

    override fun attachRemoteVideo(videoTrack: VideoTrack) {
        videoTrack.addRenderer(binding.incomingSurfaceView)
    }

    override fun getLocalVideoRender(): TextureViewRenderer = binding.selfSurfaceView
    override fun getRemoteVideoRender(): TextureViewRenderer = binding.incomingSurfaceView

    override fun onCameraStatusChanged(enabled: Boolean) {
        super.onCameraStatusChanged(enabled)
        binding.actionView.videoImv.isActivated = enabled.not()
        binding.selfVideoOffLl.isVisible = enabled.not()
    }

    override fun onMicrophoneStatusChanged(status: Boolean) {
        super.onMicrophoneStatusChanged(status)
        binding.actionView.audioImv.isActivated = status.not()
        getCurrentMicStatusIcon(status.not())?.let {
            binding.selfVoiceStatusIv.setImageDrawable(it)
        }
    }

    override fun onRemoteParticipantCameraChange(isHide: Boolean) {
        super.onRemoteParticipantCameraChange(isHide)
        binding.remoteVideoOffLl.isVisible = isHide
    }

    override fun onRemoteParticipantMicChange(isMuted: Boolean) {
        super.onRemoteParticipantMicChange(isMuted)
        getCurrentMicStatusIcon(isMuted)?.let {
            binding.remoteVoiceStatusIv.setImageDrawable(it)
        }
//        binding.ivRemoteMicStatus.isVisible = isMuted
    }

    override fun onLocalParticipantSpeaking(isSpeaking: Boolean) {
        super.onLocalParticipantSpeaking(isSpeaking)
        binding.selfVoiceStatusIv.isActivated = isSpeaking
    }

    override fun onRemoteParticipantSpeaking(isSpeaking: Boolean) {
        super.onRemoteParticipantSpeaking(isSpeaking)
        binding.remoteVoiceStatusIv.isActivated = isSpeaking
    }

    private fun getCurrentMicStatusIcon(isMuted: Boolean): Drawable? {
        return if (isMuted) {
            ContextCompat.getDrawable(this@VideoCallActivity, R.drawable.ic_mic_off)
        } else {
            ContextCompat.getDrawable(this@VideoCallActivity, R.drawable.selector_active_speaker)
        }
    }

    override fun onCameraPositionChanged(cameraPosition: CameraPosition) {
        super.onCameraPositionChanged(cameraPosition)
        binding.actionView.flipImv.isActivated = cameraPosition == CameraPosition.BACK
    }

    override fun onConnectivityChanged(it: ConnectionQuality?) {
        super.onConnectivityChanged(it)
        binding.tvPoorConnectivity.isVisible = it == ConnectionQuality.POOR
    }
}
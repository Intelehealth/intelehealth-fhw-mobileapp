package org.intelehealth.klivekit.ui.activity

import android.Manifest
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.codeglo.coyamore.data.PreferenceHelper
import com.codeglo.coyamore.data.PreferenceHelper.Companion.RTC_DATA
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import io.livekit.android.events.DisconnectReason
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intelehealth.app.registry.PermissionRegistry
import org.intelehealth.app.registry.allGranted
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.ui.viewmodel.CallViewModel
import org.intelehealth.klivekit.ui.viewmodel.SocketViewModel
import org.intelehealth.klivekit.ui.viewmodel.VideoCallViewModel
import org.intelehealth.klivekit.utils.AudioType
import org.intelehealth.klivekit.utils.RTC_ARGS
import org.intelehealth.klivekit.utils.extensions.showToast
import org.intelehealth.klivekit.utils.extensions.viewModelByFactory
import java.util.Calendar


/**
 * Created by Vaghela Mithun R. on 07-06-2023 - 18:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class CoreVideoCallActivity : AppCompatActivity() {

    protected lateinit var args: RtcArgs

    protected val videoCallViewModel: VideoCallViewModel by viewModelByFactory {
        args = IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)
            ?: throw NullPointerException("arg is null!")
        VideoCallViewModel(args.url ?: "", args.appToken ?: "", application)
    }

    private val socketViewModel: SocketViewModel by viewModelByFactory {
        args = IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)
            ?: throw NullPointerException("args is null!")
//        val url: String = Constants.BASE_URL + "?userId=" + args.nurseId + "&name=" + args.nurseId
        SocketViewModel(args)
    }

    private val permissionRegistry: PermissionRegistry by lazy {
        PermissionRegistry(this@CoreVideoCallActivity, activityResultRegistry)
    }

    private val preferenceHelper: PreferenceHelper by lazy {
        PreferenceHelper(applicationContext)
    }

    private val neededPermissions =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)


    // initiate the incoming call ringtone
//    private val ringtone: Ringtone by lazy {
//        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
//        RingtoneManager.getRingtone(applicationContext, notification)
//    }

//    private val audioManager: AudioManager by lazy {
//        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//    }

    private val mediaPlayer: MediaPlayer by lazy {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        MediaPlayer.create(this, notification).apply {
            isLooping = true
        };
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState)
        videoCallViewModel.room.initVideoRenderer(getLocalVideoRender())
        videoCallViewModel.room.initVideoRenderer(getRemoteVideoRender())
        extractRtcParams()
        observeLiveData()
        observerSocketEvent()
    }

    private fun preventDuplicationData(newRtcData: RtcArgs) {
        val oldRtcData: String? = preferenceHelper.get(RTC_DATA, null)
        oldRtcData?.let {
            val previouse = Gson().fromJson(it, RtcArgs::class.java)
            previouse?.let {
                if (it.appToken.equals(newRtcData.appToken)) finish()
                else preferenceHelper.save(RTC_DATA, Gson().toJson(newRtcData))
            }
        }
    }

    private fun observeLiveData() {
        videoCallViewModel.callEnd.observe(this) { if (it) onCallEnd() }
        videoCallViewModel.sayGoodBye.observe(this) { if (it) sayBye("Call ended by you") }
//        videoCallViewModel.microphonePluggedStatus.observe(this) {
//            audioManager.isSpeakerphoneOn = it.not()
//            onMicrophoneStatusChanged(it)
//        }
        videoCallViewModel.runningCallDuration.observe(this) { onCallCountDownStart(it) }
        videoCallViewModel.micEnabled.observe(this) { updateMicrophoneStatus(it) }
        videoCallViewModel.participantConnected.observe(this) { onParticipantConnected(it) }
        videoCallViewModel.cameraEnabled.observe(this) { updateCameraStatus(it) }
        videoCallViewModel.localVideoTrack.observe(this) { it?.let { attachLocalVideo(it) } }
        videoCallViewModel.remoteVideoTrack.observe(this) { it?.let { it1 -> attachRemoteVideo(it1) } }
        videoCallViewModel.isSpeakingStatus.observe(this) { updateMicrophoneSpeakingStatus(it) }
        videoCallViewModel.remoteConnectionQuality.observe(this) { onConnectivityChanged(it) }
        videoCallViewModel.screenshareEnabled.observe(this) {}
        videoCallViewModel.localCameraMirrorStatus.observe(this) {}
        videoCallViewModel.remoteParticipantDisconnected.observe(this) { if (it) sayBye("${args.doctorName} left the call") }
        videoCallViewModel.cameraPosition.observe(this) { onCameraPositionChanged(it) }
        socketViewModel.eventCallRejectByDoctor.observe(this) { if (it) sayBye("Call rejected by ${args.doctorName}") }
        socketViewModel.eventCallCancelByDoctor.observe(this) {
            if (it) {
                Timber.e { "Remain time up mil ${videoCallViewModel.remainTimeupMilliseconds}" }
                sayBye("Call canceled by ${args.doctorName}")
            }
        }
        videoCallViewModel.remoteCallDisconnectedReason.observe(this) {
            it?.let { checkCallDisconnectReason(it) }
        }

        videoCallViewModel.callTimeUpStatus.observe(this) { callTimeUp(it) }
        socketViewModel.eventCallTimeUp.observe(this) { callTimeUp(it) }
    }

    private fun callTimeUp(it: Boolean) {
        if (it) {
            finish()
            Timber.e { "Call time up ${Calendar.getInstance().time}" }
            videoCallViewModel.stopCallTimeoutTimer()
            showToast("Call time up")
        }
    }

    open fun onParticipantConnected(it: Boolean?) {
        Timber.d { "onParticipantConnected $it" }
    }

    open fun onCameraPositionChanged(cameraPosition: CameraPosition) {
        Timber.d { "Current camera position is ${cameraPosition.name}" }
    }

    private fun updateCameraStatus(it: Map<String, Boolean>?) {
        it ?: return
        it.filterKeys { key -> key == CallViewModel.REMOTE_PARTICIPANT }.apply {
            if (isNotEmpty()) onRemoteParticipantCameraChange(values.first())
        }
        it.filterKeys { key -> key == CallViewModel.LOCAL_PARTICIPANT }.apply {
            if (isNotEmpty()) onCameraStatusChanged(values.first())
        }
    }

    private fun updateMicrophoneStatus(it: Map<String, Boolean>?) {
        it ?: return
        it.filterKeys { key -> key == CallViewModel.REMOTE_PARTICIPANT }.apply {
            if (isNotEmpty()) onRemoteParticipantMicChange(values.first())
        }
        it.filterKeys { key -> key == CallViewModel.LOCAL_PARTICIPANT }.apply {
            if (isNotEmpty()) onMicrophoneStatusChanged(values.first())
        }
    }

    private fun updateMicrophoneSpeakingStatus(it: Map<String, Boolean>?) {
        it ?: return
        it.filterKeys { key -> key == CallViewModel.REMOTE_PARTICIPANT }.apply {
            if (isNotEmpty()) onRemoteParticipantSpeaking(values.first())
        }
        it.filterKeys { key -> key == CallViewModel.LOCAL_PARTICIPANT }.apply {
            if (isNotEmpty()) onLocalParticipantSpeaking(values.first())
        }
    }


    private fun observerSocketEvent() {
        socketViewModel.connect()
        socketViewModel.eventNoAnswer.observe(this) { if (it) sayBye("No answer from ${args.doctorName}") }
//        socketViewModel.eventBye.observe(this) { if (it) sayBye() }
        lifecycleScope.launch {
            delay(1000)
            Timber.e { "Socket connected =>${socketViewModel.isConnected()}" }
        }
    }

    private fun extractRtcParams() {
        intent ?: return
        if (intent.hasExtra(RTC_ARGS)) {
            IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)?.let {
                args = it
                Timber.d { "Args => ${Gson().toJson(args)}" }
                Timber.e { "Room Token : ${args.appToken}" }
                preventDuplicationData(args)
                if (args.isIncomingCall) onIncomingCall()
                else onGoingCall()
            }

            intent.data = null
        }
    }

    private fun startConnecting() {
        permissionRegistry.requestPermissions(neededPermissions).observe(this) {
            if (it.allGranted()) {
                startCallTimer()
                videoCallViewModel.connectToRoom()
            }
        }
    }

    open fun playRingtone() {
//        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    open fun stopRingtone() {
        Timber.e { "stopRingtone ${mediaPlayer.isPlaying}" }
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
    }

    override fun onResume() {
        super.onResume()
        videoCallViewModel.registerReceivers(this)
    }

    override fun onPause() {
        super.onPause()
        videoCallViewModel.unregisterBroadcast(this)
    }

    open fun onCallEnd() {
        Timber.d { "Call ended by ${args.doctorName}" }
    }

    open fun onCallAccept() {
        Timber.d { "Call accepted by you" }
        videoCallViewModel.stopCallTimeoutTimer()
    }

    open fun onCallDecline() {
        Timber.d { "Call declined by you" }
        videoCallViewModel.stopCallTimeoutTimer()
    }

    open fun onIncomingCall() {
        Timber.d { "Incoming call from ${args.doctorName}" }
        videoCallViewModel.startCallTimeoutTimer()
    }

    open fun onMicrophoneStatusChanged(status: Boolean) {
        Timber.d { "Microphone status changed $status" }
    }

    open fun onCameraStatusChanged(enabled: Boolean) {
        Timber.d { "Camera status $enabled" }
    }

    open fun onGoingCall() {
        Timber.d { "Calling to ${args.doctorName}" }
        videoCallViewModel.updateAudioSetting(AudioType.SPEAKER_PHONE)
//        audioManager.isSpeakerphoneOn = true
//        audioManager.setStreamVolume(
//            AudioManager.STREAM_MUSIC,
//            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
//            0
//        )
        playRingtone()
        videoCallViewModel.startCallTimeoutTimer()
        socketViewModel.connectWithDoctor()
        startConnecting()
    }

    open fun onConnectivityChanged(it: ConnectionQuality?) {
        Timber.d { "Connectivity => ${it}" }
    }

    open fun onRemoteParticipantMicChange(isMuted: Boolean) {
        val speakerStatus = if (isMuted) "Muted" else "Unmuted"
        Timber.d { "RemoteParticipant speaker is $speakerStatus" }
    }

    open fun onRemoteParticipantCameraChange(isHide: Boolean) {
        val camera = if (isHide) "ON" else "OFF"
        Timber.d { "RemoteParticipant speaker is $camera" }
    }

    open fun onLocalParticipantSpeaking(isSpeaking: Boolean) {
        Timber.d { "You are speaking $isSpeaking" }
    }

    open fun onRemoteParticipantSpeaking(isSpeaking: Boolean) {
        Timber.d { "${args.doctorName} is speaking $isSpeaking" }
    }

    open fun acceptCall() {
        stopRingtone()
        startConnecting()
        onCallAccept()
    }

    open fun declineCall() {
        stopRingtone()
        if (args.isIncomingCall.not()) {
            socketViewModel.emit(
                SocketManager.EVENT_CALL_CANCEL_BY_HW,
                socketViewModel.buildOutGoingCallParams()
            )
        } else {
            socketViewModel.emit(SocketManager.EVENT_HW_CALL_REJECT, args.doctorId)
        }
        onCallDecline()
        finish()
    }

    open fun endCall() {
        sayBye("Call ended by you", "app")
    }

    open fun sayBye(message: String, arg: String? = null) {
        Timber.e { "$message ${Calendar.getInstance().time}" }
        showToast(message)
        arg?.let {
            socketViewModel.emit("bye", args)
        }
        stopRingtone()
        finish()
    }

    open fun onCallCountDownStart(duration: String) {
//        Timber.d { "Call count down  $duration" }
    }

    abstract fun attachLocalVideo(videoTrack: VideoTrack)

    abstract fun attachRemoteVideo(videoTrack: VideoTrack)

    abstract fun getLocalVideoRender(): TextureViewRenderer

    abstract fun getRemoteVideoRender(): SurfaceViewRenderer

    open fun startCallTimer() = videoCallViewModel.startCallDurationTimer()

    fun isArgsInitiate() = ::args.isInitialized

    private fun checkCallDisconnectReason(reason: DisconnectReason) {
        when (reason) {
            DisconnectReason.CLIENT_INITIATED -> showToast("Call disconnected due to not initiated")
            DisconnectReason.DUPLICATE_IDENTITY -> showToast("Call disconnected due to duplicated identity")
            DisconnectReason.SERVER_SHUTDOWN -> showToast("Call disconnected due to server shutdown")
            DisconnectReason.PARTICIPANT_REMOVED -> showToast("Call disconnected due to doctor removed")
            DisconnectReason.ROOM_DELETED -> showToast("Call disconnected due to no room found")
            DisconnectReason.STATE_MISMATCH -> showToast("Call disconnected due to room mismatched")
            DisconnectReason.JOIN_FAILURE -> showToast("Call disconnected due to join failure")
            DisconnectReason.UNKNOWN_REASON -> showToast("Call disconnected due to unkhown reason")
        }

        finish()
    }

    override fun onDestroy() {
        SocketManager.instance.resetCallTimeUpFlag()
        super.onDestroy()
        stopRingtone()
    }
}
package org.intelehealth.klivekit.call.ui.activity

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import io.livekit.android.events.DisconnectReason
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intelehealth.app.registry.PermissionRegistry
import org.intelehealth.app.registry.allGranted
import org.intelehealth.klivekit.R
import org.intelehealth.klivekit.RtcEngine
import org.intelehealth.klivekit.call.notification.HeadsUpNotificationService
import org.intelehealth.klivekit.call.ui.viewmodel.CallViewModel
import org.intelehealth.klivekit.call.ui.viewmodel.VideoCallViewModel
import org.intelehealth.klivekit.call.utils.CallAction
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.call.utils.CallNotificationHandler
import org.intelehealth.klivekit.call.utils.CallStatus
import org.intelehealth.klivekit.data.PreferenceHelper
import org.intelehealth.klivekit.data.PreferenceHelper.Companion.RTC_DATA
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.socket.SocketViewModel
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
    private var isDeclined: Boolean = false
    protected val videoCallViewModel: VideoCallViewModel by viewModelByFactory {
        args = if (intent.hasExtra(RTC_ARGS)) {
            IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)
                ?: throw NullPointerException("arg is null!")
        } else RtcArgs.dummy()

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

    private val neededPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) arrayOf(
        Manifest.permission.MANAGE_OWN_CALLS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    ) else arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)


    // initiate the incoming call ringtone
    private val ringtone: Ringtone by lazy {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        RingtoneManager.getRingtone(applicationContext, notification)
    }

//    private val audioManager: AudioManager by lazy {
//        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//    }

//    private val mediaPlayer: MediaPlayer by lazy {
//        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
//        MediaPlayer.create(this, notification).apply {
//            isLooping = true
//        };
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        super.onCreate(savedInstanceState)
        videoCallViewModel.room.initVideoRenderer(getLocalVideoRender())
        videoCallViewModel.room.initVideoRenderer(getRemoteVideoRender())
        extractRtcParams()
        initView()
//        observeLiveData()
//        observerSocketEvent()
    }

    private fun initView() {
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
        videoCallViewModel.remoteParticipantDisconnected.observe(this) {
            if (it && isDeclined.not()) sayBye(
                getString(
                    R.string.left_the_call, args.doctorName
                )
            )
        }
        videoCallViewModel.cameraPosition.observe(this) { onCameraPositionChanged(it) }
        socketViewModel.eventCallRejectByDoctor.observe(this) {
            if (it && isDeclined.not()) sayBye(
                getString(
                    R.string.call_rejected_by, args.doctorName
                )
            )
        }
        socketViewModel.eventCallCancelByDoctor.observe(this) {
            Timber.e { "args ${args.toJson()}" }
            if (it && args.isIncomingCall() && isDeclined.not() && args.isCallAccepted()
                    .not() && args.isMissedCall().not()
            ) {
                args.callStatus = CallStatus.MISSED
                CallHandlerUtils.notifyCallNotification(args, this)
                sayBye(arg = args.doctorName)
            }

//            if (it && isDeclined.not()) {
//                Timber.e { "Remain time up mil ${videoCallViewModel.remainTimeupMilliseconds}" }
//                sayBye(getString(R.string.call_canceled_by, args.doctorName))
//            }
        }
        videoCallViewModel.remoteCallDisconnectedReason.observe(this) {
            it?.let { checkCallDisconnectReason(it) }
        }

        videoCallViewModel.callTimeUpStatus.observe(this) { callTimeUp(it) }
        socketViewModel.eventCallTimeUp.observe(this) { callTimeUp(it) }
        socketViewModel.eventCallHangUp.observe(this) {
            Timber.d { "Call hangup => $it" }
            if (it) endCall()
        }
    }

    private fun callTimeUp(it: Boolean) {
        if (it) {
            finish()
            Timber.e { "Call time up ${Calendar.getInstance().time}" }
            videoCallViewModel.stopCallTimeoutTimer()
            showToast(getString(R.string.call_time_up))
            if (args.isIncomingCall()) {
                args.callStatus = CallStatus.MISSED
                CallHandlerUtils.notifyCallNotification(args, this@CoreVideoCallActivity)
            }
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
        socketViewModel.eventNoAnswer.observe(this) {
            val reason = getString(R.string.no_answer_from, args.doctorName)
            if (it) sayBye(reason)
        }
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
                handleCallByStatus()
//                if (args.isIncomingCall()) {
//                    onIncomingCall()
//                    stopService(
//                        Intent(
//                            this@CoreVideoCallActivity,
//                            HeadsUpNotificationService::class.java
//                        )
//                    )
//                } else onGoingCall()
            }

            intent.data = null
        }
    }

    private fun handleCallByStatus() {
        if (args.isCallOnGoing()) {
            return
        } else if (args.isIncomingCall() && args.isCallAccepted()) {
            acceptCall()
            args.callStatus = CallStatus.ON_GOING
            CallHandlerUtils.notifyCallNotification(args, this)
        } else if (args.isIncomingCall()) {
            onIncomingCall()
            stopService(
                Intent(
                    this@CoreVideoCallActivity, HeadsUpNotificationService::class.java
                )
            )
        } else onGoingCall()
    }

    private fun startConnecting() {
        Timber.d { "permissions ${neededPermissions.size}" }
        permissionRegistry.requestPermissions(neededPermissions).observe(this) {
            if (it.allGranted()) {
                startCallTimer()
                videoCallViewModel.connectToRoom()
            }
        }
    }

    open fun playRingtone() {
//        mediaPlayer.prepare()
//        mediaPlayer.start()
        if (!ringtone.isPlaying && args.sound) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.isLooping = true
            }
            ringtone.play()
        }
    }

    open fun stopRingtone() {
//        Timber.e { "stopRingtone ${mediaPlayer.isPlaying}" }
//        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        Timber.e { "stopRingtone ${ringtone.isPlaying}" }
        if (ringtone.isPlaying) ringtone.stop()
    }

    override fun onResume() {
        super.onResume()
        Timber.d { "onResume" }
        videoCallViewModel.registerReceivers(this)
        if (args.isCallOnGoing()) videoCallViewModel.resumeRemoteVideoTrack()
    }

    override fun onPause() {
        super.onPause()
        Timber.d { "onPause" }
        videoCallViewModel.unregisterBroadcast(this)
        if (args.isCallOnGoing()) {
            videoCallViewModel.pauseRemoteVideoTrack()
        }
    }

    open fun onCallEnd() {
        Timber.d { "Call ended by ${args.doctorName}" }
    }

    open fun onCallAccept() {
        Timber.d { "Call accepted by you" }
        videoCallViewModel.stopCallTimeoutTimer()
        // when user tap on incoming notification and full screen will visible
        // from full screen user accept the call
        // for this scenario we need to show ongoing call notification
        if (args.isIncomingCall() && args.isCallAccepted().not()) {
            args.callAction = CallAction.ACCEPT
            args.callStatus = CallStatus.ON_GOING
            CallHandlerUtils.notifyCallNotification(args, this@CoreVideoCallActivity)
        }
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
        isDeclined = true
//        showToast(getString(R.string.you_declined_call))
        if (args.isIncomingCall().not()) {
            socketViewModel.emit(SocketManager.EVENT_CALL_CANCEL_BY_HW, args.toJsonArg())
        } else {
            socketViewModel.emit(SocketManager.EVENT_CALL_REJECT_BY_HW, args.doctorId)
        }

        stopRingtone()
        onCallDecline()
        finish()
    }

    open fun endCall() {
        sayBye(arg = "app")
    }

    open fun sayBye(message: String? = null, arg: String? = null) {
        message?.let { showToast(it) }
        Timber.e { "message $message $arg" }
        arg?.let {
            socketViewModel.emit(SocketManager.EVENT_BYE, it)
        }
        stopRingtone()
        stopService(Intent(this@CoreVideoCallActivity, HeadsUpNotificationService::class.java))
        finish()
    }

    open fun onCallCountDownStart(duration: String) {
//        Timber.d { "Call count down  $duration" }
    }

    abstract fun attachLocalVideo(videoTrack: VideoTrack)

    abstract fun attachRemoteVideo(videoTrack: VideoTrack)

    abstract fun getLocalVideoRender(): TextureViewRenderer

    abstract fun getRemoteVideoRender(): TextureViewRenderer

    open fun startCallTimer() = videoCallViewModel.startCallDurationTimer()

    fun isArgsInitiate() = ::args.isInitialized

    private fun checkCallDisconnectReason(reason: DisconnectReason) {
//        when (reason) {
//            DisconnectReason.CLIENT_INITIATED -> showToast(getString(R.string.reason_not_initiated))
//            DisconnectReason.DUPLICATE_IDENTITY -> showToast(getString(R.string.reason_duplicated_identity))
//            DisconnectReason.SERVER_SHUTDOWN -> showToast(getString(R.string.reason_server_shutdown))
//            DisconnectReason.PARTICIPANT_REMOVED -> showToast(getString(R.string.reason_participant_removed))
//            DisconnectReason.ROOM_DELETED -> showToast(getString(R.string.reason_room_deleted))
//            DisconnectReason.STATE_MISMATCH -> showToast(getString(R.string.reason_state_mismatch))
//            DisconnectReason.JOIN_FAILURE -> showToast(getString(R.string.reason_join_failure))
//            DisconnectReason.UNKNOWN_REASON -> showToast(getString(R.string.reason_unknown))
//        }

        finish()
    }

    override fun onDestroy() {
        SocketManager.instance.resetCallTimeUpFlag()
        super.onDestroy()
        stopRingtone()
        videoCallViewModel.disconnect()
    }
}
package org.intelehealth.klivekit.call.utils

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import com.github.ajalt.timberkt.Timber
import org.intelehealth.klivekit.room.WebRtcDatabase
import org.intelehealth.klivekit.R
import org.intelehealth.klivekit.RtcEngine
import org.intelehealth.klivekit.call.CallLogHandler
import org.intelehealth.klivekit.call.data.CallLogRepository
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.call.notification.CallReceiver
import org.intelehealth.klivekit.call.notification.HeadsUpNotificationService
import org.intelehealth.klivekit.data.PreferenceHelper
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.utils.RTC_ARGS
import kotlin.system.exitProcess

/**
 * Created by Vaghela Mithun R. on 8/28/2021.
 * vaghela@codeglo.com
 */
object CallHandlerUtils {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Operate all incoming, outgoing and ongoing call action
     * @param context Context of current scope
     * @param callArgs an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun notifyCallNotification(callArgs: RtcArgs, context: Context) {
        Timber.d { "notifyCallNotification Url: ${callArgs.toJson()}" }
        context.stopService(Intent(context, HeadsUpNotificationService::class.java))
        if (callArgs.isIncomingCall() && callArgs.isMissedCall()) {
            getCallLogHandler(context).changCallStatus(CallStatus.MISSED)
            CallNotificationHandler.notifyMissedCall(context, callArgs)
        } else if (callArgs.isCallDeclined()) {
            SocketManager.instance.emit(SocketManager.EVENT_CALL_REJECT_BY_HW, callArgs.doctorId)
        } else if (callArgs.isCallHangUp()) {
            SocketManager.instance.emitLocalEvent(SocketManager.EVENT_CALL_HANG_UP)
            if (CallNotificationHandler.isAppInBackground()) RtcEngine.leaveRoom()
        } else if (callArgs.isBusyCall()) {
            // cancel notification with busy message
        } else if (callArgs.isIncomingCall() or callArgs.isCallAccepted() or callArgs.isOutGoingCall()) {
            IntentUtils.getHeadsUpNotificationServiceIntent(callArgs, context).also {
                ContextCompat.startForegroundService(context, it)
            }
        }
    }

    /**
     * Operate all incoming, outgoing and ongoing call action
     * @param context Context of current scope
     * @param callArgs an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun operateIncomingCall(context: Context, callArgs: RtcArgs, ) {
        Timber.d { "operateIncomingCall ->Url = ${callArgs.url}" }
        callArgs.callMode = CallMode.INCOMING
        getCallLogHandler(context).saveLog(generateCallLog(callArgs, context))
        notifyCallNotification(callArgs, context)
    }

    fun saveIncomingCall(context: Context, callArgs: RtcArgs) {
        getCallLogHandler(context).saveLog(generateCallLog(callArgs, context))
    }

    private fun getCallLogHandler(context: Context) = CallLogHandler(
        CallLogRepository(WebRtcDatabase.getInstance(context).rtcCallLogDao()),
        PreferenceHelper(context)
    )

    private fun generateCallLog(callArgs: RtcArgs, context: Context) = RtcCallLog(
        callerName = callArgs.doctorName!!,
        callerId = callArgs.doctorId!!,
        calleeId = callArgs.nurseId!!,
        calleeName = callArgs.nurseName!!,
        roomId = callArgs.roomId!!,
        roomName = callArgs.patientName!!,
        callMode = callArgs.callMode,
        callStatus = callArgs.callStatus,
        callTime = System.currentTimeMillis().toString(),
        callUrl = callArgs.url!!,
        chatAction = RtcEngine.getConfig(context)!!.chatIntentClass,
        callAction = RtcEngine.getConfig(context)!!.callIntentClass,
        hasCallAction = false,
        hasChatAction = true
    )

    /**
     * Operate all incoming, outgoing and ongoing call action
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun operateCallAction(messageBody: RtcArgs, context: Context) {
        context.sendBroadcast(Intent(context, CallReceiver::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
            action = IntentUtils.getCallReceiverAction(context)
        })
    }

    /**
     * An action for decline while receive any incoming call
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun declineCall(messageBody: RtcArgs, context: Context) {
        Timber.d { "Call declined **** ${messageBody.callStatus}" }
//        if (messageBody.isAttendedCall() or messageBody.isCallOnGoing()) {
//            messageBody.messageType = CALL_NONE
//            messageBody.action = CALL_ACTION_HANGUP
//            operateCallAction(messageBody, context)
//        } else if (messageBody.isIncomingCall()) {
//            messageBody.messageType = CALL_BUSY
//            messageBody.action = CALL_ACTION_DECLINE
//            operateCallAction(messageBody, context)
//        } else if (messageBody.isOutGoingCall()) {
//            messageBody.messageType = CALL_MISSED
//            messageBody.action = CALL_ACTION_HANGUP
//            operateCallAction(messageBody, context)
//        }
    }

    /**
     * An action for hangup running call
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun hangUpCall(messageBody: RtcArgs, context: Context) {
        context.sendBroadcast(Intent(context, CallReceiver::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
            action = IntentUtils.getCallReceiverAction(context)
        })
    }


    fun finishedCall(messageBody: RtcArgs, context: Context) {
        context.sendBroadcast(Intent(context, CallReceiver::class.java).apply {
//            messageBody.callStatus = CALL_FINISHED
            putExtra(RTC_ARGS, messageBody)
            action = IntentUtils.getCallReceiverAction(context)
        })
    }


    /**
     * An action to perform after call time out
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun callTimedOut(messageBody: RtcArgs, context: Context) {
//        messageBody.callStatus = CALL_TIMED_OUT
        context.sendBroadcast(Intent(context, CallReceiver::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
            action = IntentUtils.getCallReceiverAction(context)
        })
    }

    /**
     * An action to perform after call time out
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun callMissed(messageBody: RtcArgs, context: Context) {
        context.sendBroadcast(Intent(context, CallReceiver::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
            action = IntentUtils.getCallReceiverAction(context)
        })
    }

    /**
     * To start media player while dial a phone call
     * @param context Context of current scope
     * @param audioManager an instance of AudioManager with specific setting
     * @return MediaPlayer
     */
    fun playRingtoneInEarPiece(
        context: Context,
        audioManager: AudioManager,
        messageBody: RtcArgs
    ) {

        mediaPlayer = MediaPlayer.create(context, R.raw.ring_ring)
        mediaPlayer?.isLooping = true

        if (messageBody.isVideoCall().not()) {
            Timber.d { "Audio call ****** " }
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } else {
            Timber.d { "Video call ****** " }
            audioManager.mode = AudioManager.MODE_RINGTONE
        }
        //audioManager.isSpeakerphoneOn = false

        val volume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)

        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            volume,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )

        mediaPlayer?.start()
    }


    /**
     * To stop media player after call join
     */
    fun stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
            }
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }


    fun initSensor(sensorManager: SensorManager, service: HeadsUpNotificationService): Sensor? {

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        return if (sensor != null) {
            sensorManager.registerListener(service, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensor
        } else {
            Timber.d { "Sensor is null **** " }
            null
        }
    }

}
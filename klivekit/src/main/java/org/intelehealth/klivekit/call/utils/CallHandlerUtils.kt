package org.intelehealth.klivekit.call.utils

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import org.intelehealth.klivekit.R
import org.intelehealth.klivekit.call.notification.CallReceiver
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.utils.RTC_ARGS
import timber.log.Timber

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
        if (callArgs.isIncomingCall()) {
            IntentUtils.getHeadsUpNotificationServiceIntent(callArgs, context).also {
                ContextCompat.startForegroundService(context, it)
            }
        } else if (callArgs.isAcceptCall()) {
            // start video call
        } else if (callArgs.isCallDeclined()) {
            SocketManager.instance.emit(SocketManager.EVENT_CALL_REJECT_BY_HW)
            // cancel notification
        } else if (callArgs.isMissedCall()) {
            // cancel notification
            // generate missed call notification with call back action
        } else if (callArgs.isBusyCall()) {
            // cancel notification with busy message
        } else if (callArgs.isCallOnGoing()) {
            // show on going call notification
        } else {
            // do nothing here
        }
    }

    /**
     * Operate all incoming, outgoing and ongoing call action
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun operateCallAction(messageBody: RtcArgs, context: Context) {
        context.sendBroadcast(Intent(context, CallReceiver::class.java).apply {
            putExtra(RTC_ARGS, messageBody)
        })
    }

    /**
     * An action for decline while receive any incoming call
     * @param context Context of current scope
     * @param messageBody an instance of RtcArgs to send with intent
     * @return PendingIntent type of CallActionHandlerReceiver intent
     */
    fun declineCall(messageBody: RtcArgs, context: Context) {
        Timber.d("Call declined **** ${messageBody.callStatus}")
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
        })
    }


    fun finishedCall(messageBody: RtcArgs, context: Context) {
        context.sendBroadcast(Intent(context, CallReceiver::class.java).apply {
//            messageBody.callStatus = CALL_FINISHED
            putExtra(RTC_ARGS, messageBody)
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
            Timber.d("Audio call ****** ")
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } else {
            Timber.d("Video call ****** ")
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


    fun initSensor(sensorManager: SensorManager, service: ChatCallingService): Sensor? {

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        return if (sensor != null) {
            sensorManager.registerListener(service, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensor
        } else {
            Timber.d("Sensor is null **** ")
            null
        }
    }

}
package org.intelehealth.klivekit.call.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.content.IntentCompat
import org.intelehealth.klivekit.call.utils.CallAction
import org.intelehealth.klivekit.call.utils.CallConstants
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.call.utils.CallMode
import org.intelehealth.klivekit.call.utils.CallStatus
import org.intelehealth.klivekit.call.utils.NotificationHandlerUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.RTC_ARGS
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random


/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class HeadsUpNotificationService : Service(), SensorEventListener {

    private var sensor: Sensor? = null
    private var normalStatus: Boolean = false
    private var vibrateStatus: Boolean = false
    private lateinit var vibrationEffect: VibrationEffect
    private lateinit var countDownTimer: CountDownTimer
    private var isTimerRunning = false
    private var sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    //[0] initial delay then subsequent vibrate & pause
    private var vibratePattern: LongArray = longArrayOf(0, 100, 800, 100, 800, 100, 800, 100)
    private var vibrationAmplitude = intArrayOf(0, 50, 0, 50, 0, 50, 0, 50)


    private val audioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val vibratorService by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val powerManager by lazy {
        getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private val ringtone by lazy {
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        RingtoneManager.getRingtone(this, notificationUri)
    }

    private val notificationId by lazy {
        Random(System.currentTimeMillis()).nextInt(CallConstants.MAX_INT)
    }

    private val wakeLock: PowerManager.WakeLock by lazy {
        powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "lock:proximity_screen_off"
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service is created ***** ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrationEffect = VibrationEffect.createWaveform(
                vibratePattern, vibrationAmplitude, -1
            )
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("OnstartCommand is created ***** ")
        intent?.let {

            setVibrateorNormalRinger()
            val messageBody = IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)
            messageBody?.let {
                messageBody.notificationId = notificationId
                Timber.d("Message call type **** ${messageBody.callType}")
                Timber.d("Message call mode **** ${messageBody.callMode}")
                Timber.d("Message call status **** ${messageBody.callStatus}")
                if (messageBody.isAcceptCall()) {
                    showAcceptedCallNotification(messageBody)
                } else if (messageBody.isIncomingCall()) {
                    showIncomingCallNotification(messageBody)
                } else if (messageBody.isOutGoingCall()) {
                    showOutGoingNotification(this, messageBody)
                }


//                if (messageBody.isAttendedCall()) {
//                    initNotificationForCallAttended(messageBody)
//                    callstatus = CALL_ATTENDED
//                } else if (messageBody.isOutGoingCall()) {
//                    countDownTimer(messageBody)
//                    initOutGoingCallNotification(messageBody)
//                    callstatus = OUTGOING_CALL
//                } else if (messageBody.isIncomingCall()) {
//                    Timber.d("Incoming call **** ${isCallOngoingPref.getValue()}")
//                    messageBody.isOtherCallRunning = isCallOngoingPref.getValue()
//                    if (isCallOngoingPref.getValue()) {
//                        messageBody.messageType = CALL_BUSY
//                        messageBody.action = CALL_ACTION_DECLINE
//                        CallHandlerUtils.operateCallAction(messageBody, this)
//                    } else {
//                        initNotificationForIncomingCallBuilder(messageBody)
//                        callstatus = INCOMING_CALL
//                    }
//                }
            }

        }

        return START_REDELIVER_INTENT
    }


    private fun setVibrateorNormalRinger() {

        Timber.d("Audio manager mode ***** ${audioManager.ringerMode}")

        when (audioManager.ringerMode) {

            AudioManager.RINGER_MODE_NORMAL -> {
                normalStatus = true
            }

            AudioManager.RINGER_MODE_SILENT -> {
                normalStatus = false
                vibrateStatus = false
            }

            AudioManager.RINGER_MODE_VIBRATE -> {
                normalStatus = false
                vibrateStatus = true
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(context: Context, priority: Int): NotificationChannel {
        return NotificationHandlerUtils.getNotificationChannel(context, priority)
    }

    private fun showOutGoingNotification(context: Context, messageBody: RtcArgs) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(getNotificationChannel(context, 2))
        }

        val notificationCompatBuilder =
            NotificationHandlerUtils.outGoingCallNotificationBuilder(messageBody, this)

        notificationManager.notify(notificationId, notificationCompatBuilder.build())
        startForeground(notificationId, notificationCompatBuilder.build())

        playRingtoneInEarpiece(messageBody)

        countDownTimer.start()
        isTimerRunning = true

        if (messageBody.isVideoCall().not()) {
            sensor = CallHandlerUtils.initSensor(sensorManager, this)
        }
    }


    private fun showAcceptedCallNotification(messageBody: RtcArgs) {
        destroySetting()

        messageBody.notificationId = notificationId

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationHandlerUtils.getNotificationChannel(this, 2)
            )
        }


        val notificationCompatBuilder =
            NotificationHandlerUtils.getAttendedCallNotificationBuilder(this, messageBody)

        notificationManager.notify(notificationId, notificationCompatBuilder.build())

        if (messageBody.isVideoCall().not()) {
            sensor = CallHandlerUtils.initSensor(sensorManager, this)
        }
    }


    private fun showIncomingCallNotification(messageBody: RtcArgs) {
        messageBody.notificationId = notificationId
        messageBody.callMode = CallMode.INCOMING
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationHandlerUtils.getNotificationChannel(this, 1)
            )
        }

        val notificationCompatBuilder =
            NotificationHandlerUtils.getIncomingNotificationBuilder(this, messageBody)

        val notification: Notification = notificationCompatBuilder.build()
        notification.flags = Notification.FLAG_INSISTENT

        notificationManager.notify(notificationId, notification)
        startForeground(notificationId, notification)

        if (normalStatus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.isLooping = true
            }
            ringtone.play()
        } else if (vibrateStatus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibratorService.vibrate(vibrationEffect)
            } else {
                vibratorService.vibrate(vibratePattern, 0)
            }
        }
    }


    private fun playRingtoneInEarpiece(messageBody: RtcArgs) {
        CallHandlerUtils.playRingtoneInEarPiece(this, audioManager, messageBody)
    }

    private fun destroySetting() {
        ringtone.stop()
        vibratorService.cancel()

        if (isTimerRunning) {
            isTimerRunning = false
            countDownTimer.cancel()
        }

        CallHandlerUtils.stopMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Call service is destroyed ***** ")
        destroySetting()

        wakeLock.let {
            if (it.isHeld) {
                it.release()
                Timber.d("Wake lock is released ******* ")
            } else {
                Timber.d("Wake lock is not released **** ")
            }
        }
        sensor?.let {
            Timber.d("Sensor manager is released **** ")
            sensorManager.unregisterListener(this, it)
        } ?: Timber.d("The sensor is empty ****** ")

        //leave channel
        /* App.rtcEngine?.muteLocalAudioStream(false)
         App.rtcEngine?.setEnableSpeakerphone(true)*/
//        App.rtcEngine?.leaveChannel()
//        RtcEngine.destroy()
        NotificationHandlerUtils.cancelNotification(notificationId, this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        Timber.d("OnSensorChanged **** ")

        event?.let {

            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {

                if (event.values[0] < event.sensor.maximumRange) {

                    Timber.d("OnSensorChanged - acquiring wakelock **** ")

                    Timber.d("Power Manager is screen on **** ${powerManager.isInteractive}")

                    if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {

                        wakeLock?.let {

                            if (it.isHeld) {
                                Timber.d("Release then acquire ***** ")
                                it.release()
                            } else {
                                Timber.d("Not held wake lock ***** ")
                            }
                            it.acquire(10 * 60 * 1000L)
                        }
                    } else {
                        Timber.d("wake lock not supported ****** ")
                    }
                } else {
                    Timber.d("OnSensorChanged - event value greater than sensor max range **** ")
                }
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Timber.d("OnAccuracyChanged - **** $accuracy")
    }


    private fun countDownTimer(messageBody: RtcArgs) {

        Timber.d("Counterdown ***** ")

        countDownTimer = object : CountDownTimer(
            Companion.MAX_TIMEOUT,
            Companion.COUNT_DOWN_INTERVAL
        ) {

            override fun onTick(millisUntilFinished: Long) {
                Timber.d("Remaining time **** $millisUntilFinished")
            }

            override fun onFinish() {
                isTimerRunning = false
                sendCallTimedOutToBackend(messageBody)
            }
        }
    }


    //timeout
    private fun sendCallTimedOutToBackend(messageBody: RtcArgs) {
        messageBody.callMode = CallMode.OUTGOING
        messageBody.callStatus = CallStatus.MISSED
        messageBody.callAction = CallAction.HANG_UP
        CallHandlerUtils.operateCallAction(messageBody, this)
    }

    companion object {
        private const val MAX_TIMEOUT: Long = 30000
        private const val COUNT_DOWN_INTERVAL: Long = 1000
    }
}
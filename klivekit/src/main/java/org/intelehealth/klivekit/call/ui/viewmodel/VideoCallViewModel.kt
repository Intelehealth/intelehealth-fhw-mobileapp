package org.intelehealth.klivekit.call.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.CountDownTimer
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import org.intelehealth.klivekit.utils.AwsS3Utils
import org.intelehealth.klivekit.utils.extensions.hide

/**
 * Created by Vaghela Mithun R. on 07-06-2023 - 19:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VideoCallViewModel(url: String, token: String, application: Application) :
    CallViewModel(url, token, application) {

    private val callEndData = MutableLiveData(false)
    val callEnd = callEndData.hide()

    private val microphoneStatusData = MutableLiveData(false)
    val microphonePluggedStatus = microphoneStatusData.hide()

    private val imgUrlData = MutableLiveData("")
    val imgUrl = imgUrlData.hide()

    private val sayGoodByeData = MutableLiveData(false)
    val sayGoodBye = sayGoodByeData.hide()

    private val callDurationTimerData = MutableLiveData<String>()
    val runningCallDuration = callDurationTimerData.hide()

    private val mutableCallTimeUpData = MutableLiveData(false)
    val callTimeUpStatus = mutableCallTimeUpData.hide()

    var remainTimeupMilliseconds = CALL_PICKUP_EXP_TIME;

    private val callEndBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            callEndData.postValue(true)
        }
    }

    private val imageUrlFormatBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val fileUrl = intent.getStringExtra("fileUrl")
            if (fileUrl == imgUrlData.value) return
            imgUrlData.postValue(fileUrl ?: "")
        }
    }

    private val microphonePluggedStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            val status: Int
            if (Intent.ACTION_HEADSET_PLUG == action) {
                status = intent.getIntExtra("state", -1)
                microphoneStatusData.postValue(status == 1)
            }
        }
    }

    private val phoneStateBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).apply {
                if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    sayGoodByeData.postValue(true)
                }
            }
        }
    }

    fun registerReceivers(context: Context) {
        IntentFilter().apply {
            addAction(CALL_END_FROM_WEB_INTENT_ACTION)
            ContextCompat.registerReceiver(
                context,
                callEndBroadcastReceiver,
                this,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        IntentFilter().apply {
            addAction(AwsS3Utils.ACTION_FILE_UPLOAD_DONE)
            ContextCompat.registerReceiver(
                context,
                imageUrlFormatBroadcastReceiver,
                this,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        IntentFilter(Intent.ACTION_HEADSET_PLUG).apply {
            ContextCompat.registerReceiver(
                context,
                microphonePluggedStatusReceiver,
                this,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        IntentFilter("android.intent.action.PHONE_STATE").apply {
            ContextCompat.registerReceiver(
                context,
                phoneStateBroadcastReceiver,
                this,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

    }

    fun unregisterBroadcast(context: Context) {
        context.unregisterReceiver(callEndBroadcastReceiver)
        context.unregisterReceiver(imageUrlFormatBroadcastReceiver)
        context.unregisterReceiver(microphonePluggedStatusReceiver)
        context.unregisterReceiver(phoneStateBroadcastReceiver)
    }

    private val callDurationTimer = object : CountDownTimer(WAIT_TIMER.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            var timerMilli: Long = WAIT_TIMER - millisUntilFinished
            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val elapsedHours = timerMilli / hoursInMilli
            timerMilli %= hoursInMilli
            val elapsedMinutes = timerMilli / minutesInMilli
            timerMilli %= minutesInMilli
            val elapsedSeconds = timerMilli / secondsInMilli
            val displayTimeString =
                String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds)
            callDurationTimerData.postValue(displayTimeString)
        }

        override fun onFinish() {}
    }

    private val callTimeoutTimer = object : CountDownTimer(CALL_PICKUP_EXP_TIME, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            remainTimeupMilliseconds = millisUntilFinished
        }

        override fun onFinish() {
            mutableCallTimeUpData.postValue(true)
        }
    }

    fun startCallTimeoutTimer(): CountDownTimer = callTimeoutTimer.start()

    fun stopCallTimeoutTimer() = callTimeoutTimer.cancel()

    fun startCallDurationTimer(): CountDownTimer = callDurationTimer.start()

    fun stopCallTimer() = callDurationTimer.cancel()

    companion object {
        const val CALL_END_FROM_WEB_INTENT_ACTION =
            "org.intelehealth.app.CALL_END_FROM_WEB_INTENT_ACTION"
        const val WAIT_TIMER = 6 * 60 * 60 * 1000
        const val CALL_PICKUP_EXP_TIME = 60 * 1000L
    }
}
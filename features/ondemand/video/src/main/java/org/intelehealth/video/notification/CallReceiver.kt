package org.intelehealth.video.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import com.github.ajalt.timberkt.Timber
import org.intelehealth.video.utils.CallHandlerUtils


/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:40.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d { "CallReceiver executed" }
        intent?.let { handleReceivedIntentData(context!!, intent) } ?: Timber.d {
            "call argument intent is null"
        }
    }

    private fun handleReceivedIntentData(context: Context, intent: Intent) {
        intent.printExtra()
        if (intent.hasExtra(RTC_ARGS)) {
            Timber.d { "handleReceivedIntentData executed" }
            val args = IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)
            args?.let {
                CallHandlerUtils.notifyCallNotification(args, context)
            }
        }
    }
}
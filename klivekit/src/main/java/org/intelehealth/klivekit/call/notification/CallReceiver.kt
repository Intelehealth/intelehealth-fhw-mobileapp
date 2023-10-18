package org.intelehealth.klivekit.call.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import com.github.ajalt.timberkt.Timber
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.RTC_ARGS
import org.intelehealth.klivekit.utils.extensions.printExtra


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
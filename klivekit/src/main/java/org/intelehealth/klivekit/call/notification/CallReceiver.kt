package org.intelehealth.klivekit.call.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import org.intelehealth.klivekit.call.utils.CallHandlerUtils
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.RTC_ARGS


/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:40.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { handleReceivedIntentData(context!!, it) }
    }

    private fun handleReceivedIntentData(context: Context, intent: Intent) {
        if (intent.hasCategory(RTC_ARGS)) {
            val args = IntentCompat.getParcelableExtra(intent, RTC_ARGS, RtcArgs::class.java)
            args?.let {
                CallHandlerUtils.notifyCallNotification(args, context)
            }
        }
    }
}
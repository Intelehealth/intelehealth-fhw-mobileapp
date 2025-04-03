package org.intelehealth.klivekit.utils.extensions

import android.content.Intent
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.klivekit.utils.RTC_ARGS

/**
 * Created by Vaghela Mithun R. on 14-10-2023 - 23:51.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
fun Intent.printExtra() {
    Timber.d { "Intent bundle" }
    Timber.d { "Intent bundle has rtc arg = ${hasExtra(RTC_ARGS)} " }

    extras?.let {
        it.keySet().forEach { key ->
            Timber.d { "${key}::${it[key]}" }
        }
    }
}
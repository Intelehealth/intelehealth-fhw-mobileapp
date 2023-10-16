package org.intelehealth.klivekit.utils.extensions

import android.content.Intent
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson

/**
 * Created by Vaghela Mithun R. on 14-10-2023 - 23:51.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
fun Intent.printExtra() {
    Timber.d { "Intent bundle::${Gson().toJson(extras)}" }
}
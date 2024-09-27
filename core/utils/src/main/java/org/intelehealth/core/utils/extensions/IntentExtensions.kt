package org.intelehealth.core.utils.extensions

import android.content.Intent
import com.github.ajalt.timberkt.Timber

/**
 * Created by Vaghela Mithun R. on 14-10-2023 - 23:51.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
fun Intent.printExtra() {
    extras?.let {
        it.keySet().forEach { key ->
            Timber.d { "${key}::${it[key]}" }
        }
    }
}
package org.intelehealth.core.utils.extensions

import android.content.Context

/**
 * Created by Vaghela Mithun R. on 25-09-2024 - 17:07.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

fun Context.appName(): String {
    val applicationInfo = applicationInfo
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
    else getString(stringId)
}
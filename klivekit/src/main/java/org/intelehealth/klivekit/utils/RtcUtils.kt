package org.intelehealth.klivekit.utils

import android.content.Context

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 11:57.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

const val RTC_ARGS: String = "rtc_args"

fun getApplicationName(context: Context): String {
    val applicationInfo = context.applicationInfo
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
    else context.getString(stringId)
}
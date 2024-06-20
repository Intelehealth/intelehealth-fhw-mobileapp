package org.intelehealth.ncd.utils

import android.app.Activity
import android.content.Context
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

fun getApplicationName(context: Context): String {
    val applicationInfo = context.applicationInfo
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
    else context.getString(stringId)
}

fun Activity.hideKeyboard() =
    WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())

fun Activity.showKeyboard() =
    WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.ime())

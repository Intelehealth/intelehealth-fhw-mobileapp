package org.intelehealth.klivekit.utils

import android.content.Context
import android.content.res.Resources
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import org.intelehealth.klivekit.R

/**
 * Created by Vaghela Mithun R. on 26-12-2023 - 14:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class DateTimeResource private constructor(val resource: Resources) {
    companion object {
        @Volatile
        private var INSTANCE: DateTimeResource? = null

        @JvmStatic
        fun build(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = DateTimeResource(context.resources)
            }
        }

        @JvmStatic
        fun getInstance() = INSTANCE
    }

    fun getResourceString(@StringRes resId: Int) = resource.getString(resId)

    fun getResourceString(@StringRes resId: Int, argStr: String) = resource.getString(resId, argStr)
}
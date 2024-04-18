package org.intelehealth.config.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log

/**
 * Created by Vaghela Mithun R. on 17-04-2024 - 16:39.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object ResUtils {
    const val TAG = "ResUtils"

    @SuppressLint("DiscouragedApi")
    @JvmStatic
    fun getStringResourceByName(context: Context, aString: String): String {
        Log.d(TAG, "getStringResourceByName: $aString")
        val packageName: String = context.packageName
        val resId: Int = context.resources.getIdentifier(aString, "string", packageName)
        if (resId == 0) return "No resource found"
        return context.getString(resId)
    }
}
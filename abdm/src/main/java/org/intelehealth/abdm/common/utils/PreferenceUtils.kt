package org.intelehealth.abdm.common.utils

import android.content.Context
import android.content.SharedPreferences
import org.intelehealth.abdm.common.constant.Constants.BEARER_AUTH

object PreferenceUtils {

    private const val PREFERENCE_NAME = "AbdmPreferences"
    private const val AUTH_TOKEN = "auth_token"

    // Initialize SharedPreferences
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    // Save a String value
    fun saveAuthToken(context: Context, value: String) {
        val editor = getPreferences(context).edit()
        editor.putString(AUTH_TOKEN, value)
        editor.apply()
    }

    // Retrieve a String value
    fun getAuthToken(context: Context, defaultValue: String = ""): String? {
        return BEARER_AUTH + getPreferences(context).getString(AUTH_TOKEN, defaultValue)
    }

    // Clear all preferences
    fun clearPreferences(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
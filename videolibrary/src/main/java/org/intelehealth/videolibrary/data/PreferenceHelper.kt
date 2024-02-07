package org.intelehealth.videolibrary.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {

    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    init {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = pref?.edit()
    }

    fun getJwtAuthToken(): String? {
        return pref?.getString(JWT_AUTH_TOKEN, "")
    }

    companion object {
        private const val PREF_NAME: String = "Intelehealth"
        private const val JWT_AUTH_TOKEN: String = "JWT_AUTH_TOKEN"
    }
}
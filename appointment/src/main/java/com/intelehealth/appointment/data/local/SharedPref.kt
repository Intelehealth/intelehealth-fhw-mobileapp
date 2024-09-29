package com.intelehealth.appointment.data.local

import android.content.SharedPreferences

/**
 * Created By Tanvir Hasan
 * Email: tanvirhasan553@gmail.com
 */
class SharedPref constructor(private var sharedPreferences: SharedPreferences) {
    /**
     * all shared pref key will go there
     */
    companion object {
        const val TOKEN = "token"
    }

    /**
     * get any types of value from here
     */
    fun getValue(key: String, default: Any?): Any? {
        return when (default) {
            is Boolean -> sharedPreferences.getBoolean(key, default)
            is String -> sharedPreferences.getString(key, default)
            is Int -> sharedPreferences.getInt(key, default)
            is Float -> sharedPreferences.getFloat(key, default)
            else -> sharedPreferences.getString(key, default as String?)
        }
    }

    /**
     * set any types of value here
     */
    fun setValue(key: String, value: Any?) {
        when (value) {
            is Boolean -> sharedPreferences.edit().putBoolean(key, value).apply()
            is String -> sharedPreferences.edit().putString(key, value).apply()
            is Float -> sharedPreferences.edit().putFloat(key, value).apply()
            is Int -> sharedPreferences.edit().putInt(key, value).apply()
        }
    }

    /**
     * clear whole shared pref
     */
    fun clear(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}
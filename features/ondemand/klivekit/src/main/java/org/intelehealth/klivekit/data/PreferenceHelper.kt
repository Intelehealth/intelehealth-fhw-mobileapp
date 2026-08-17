package org.intelehealth.klivekit.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import javax.inject.Inject

class PreferenceHelper @Inject constructor(val context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        const val AUTH_TOKEN: String = "AUTH_TOKEN"
        const val RTC_DATA = "rtc_data"
        const val ACTIVE_ROOM_ID = "active_room_id"
        const val RTC_CONFIG = "rtc_config"
        const val MESSAGE_BODY = "message_body"
        const val IS_NOTIFICATION = "isNotification"
        const val CONFIG_VERSION = "config_version"

        // Enables the Queue Management System (QMS): home status banner, the
        // "Next In Queue" card, and the Queue bottom-nav tab (which otherwise
        // shows Help).
        const val IS_QMS_CONFIGURE = "isQmsConfigure"

        // Latest "Next In Queue" card payload (JSON), persisted from FCM
        // notifications so the home card reflects the newest queue state even
        // after the app was backgrounded when the notification arrived.
        const val QUEUE_CARD_DATA = "queue_card_data"

        // Latest home status-banner payload (JSON), persisted from "queue_status"
        // FCM notifications so the banner reflects the newest queue status even
        // after the app was backgrounded when the notification arrived.
        const val STATUS_BANNER_DATA = "status_banner_data"
    }

    fun save(key: String?, value: Any?) {
        val editor = getEditor()
        if (value is Boolean) {
            editor.putBoolean(key, (value as Boolean?)!!)
        } else if (value is Int) {
            editor.putInt(key, (value as Int?)!!)
        } else if (value is Float) {
            editor.putFloat(key, (value as Float?)!!)
        } else if (value is Long) {
            editor.putLong(key, (value as Long?)!!)
        } else if (value is String) {
            editor.putString(key, value as String?)
        } else if (value is Enum<*>) {
            editor.putString(key, value.toString())
        } else if (value != null) {
            throw RuntimeException("Attempting to save non-supported preference")
        }
        editor.commit()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun <T> get(key: String?): T {
        return sharedPreferences.all[key] as T
    }

    fun <T> get(key: String?, defValue: T): T {
        return (sharedPreferences.all[key] ?: defValue) as T
    }

    private fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    fun clear(key: String) {
        sharedPreferences.edit().run {
            remove(key)
        }.apply()
    }
}
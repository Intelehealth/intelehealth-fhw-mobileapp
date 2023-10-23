package org.intelehealth.klivekit

import android.content.Context
import com.google.gson.Gson
import org.intelehealth.klivekit.call.ui.activity.CallLogActivity
import org.intelehealth.klivekit.call.ui.activity.VideoCallActivity
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity
import org.intelehealth.klivekit.data.PreferenceHelper
import org.intelehealth.klivekit.data.PreferenceHelper.Companion.RTC_CONFIG

/**
 * Created by Vaghela Mithun R. on 20-10-2023 - 15:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class RtcConfig private constructor(
    val callUrl: String,
    val socketUrl: String,
    val callIntentClass: String,
    val chatIntentClass: String,
    val callLogIntentClass: String
) {

    class Builder() {
        private var callUrl: String = ""
        private var socketUrl: String = ""
        private var callIntentClass: Class<*>? = null
        private var chatIntentClass: Class<*>? = null
        private var callLogIntentClass: Class<*>? = null
        fun callUrl(url: String): Builder {
            this@Builder.callUrl = url
            return this@Builder
        }

        fun socketUrl(socketUrl: String): Builder {
            this@Builder.socketUrl = socketUrl
            return this@Builder
        }

        fun callIntentClass(clazz: Class<*>? = null): Builder {
            this@Builder.callIntentClass = clazz
            return this@Builder
        }

        fun chatIntentClass(clazz: Class<*>? = null): Builder {
            this@Builder.chatIntentClass = clazz
            return this@Builder
        }

        fun callLogIntentClass(clazz: Class<*>? = null): Builder {
            this@Builder.callLogIntentClass = clazz
            return this@Builder
        }

        fun build(): RtcConfig {
            return RtcConfig(
                callUrl = this@Builder.callUrl,
                socketUrl = this@Builder.socketUrl,
                callIntentClass = (this@Builder.callIntentClass
                    ?: VideoCallActivity::class.java).name,
                chatIntentClass = (this@Builder.chatIntentClass ?: ChatActivity::class.java).name,
                callLogIntentClass = (this@Builder.callLogIntentClass
                    ?: CallLogActivity::class.java).name,
            )
        }
    }

    fun saveConfig(context: Context) {
        val preferenceHelper = PreferenceHelper(context)
        preferenceHelper.save(RTC_CONFIG, Gson().toJson(this))
    }

    fun toJson(): String = Gson().toJson(this)

    fun fromJson(json: String): RtcConfig = Gson().fromJson(json, RtcConfig::class.java)

    companion object {
        fun getConfig(context: Context): RtcConfig? {
            val preferenceHelper = PreferenceHelper(context)
            val config = preferenceHelper.get(RTC_CONFIG, "")
            return if (config.isNotEmpty()) {
                Gson().fromJson(config, RtcConfig::class.java)
            } else null
        }
    }
}
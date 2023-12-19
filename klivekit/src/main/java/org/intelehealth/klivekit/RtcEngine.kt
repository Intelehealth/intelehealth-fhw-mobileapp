package org.intelehealth.klivekit

import android.annotation.SuppressLint
import android.content.Context
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import io.livekit.android.ConnectOptions
import io.livekit.android.room.Room
import org.intelehealth.klivekit.call.ui.activity.CallLogActivity
import org.intelehealth.klivekit.call.ui.activity.VideoCallActivity
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity
import org.intelehealth.klivekit.data.PreferenceHelper
import org.intelehealth.klivekit.data.PreferenceHelper.Companion.RTC_CONFIG
import org.intelehealth.klivekit.provider.LiveKitProvider

/**
 * Created by Vaghela Mithun R. on 20-10-2023 - 15:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class RtcEngine private constructor(
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

        fun build(): RtcEngine {
            return RtcEngine(
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

    fun fromJson(json: String): RtcEngine = Gson().fromJson(json, RtcEngine::class.java)

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        var room: Room? = null

        fun getConfig(context: Context): RtcEngine? {
            val preferenceHelper = PreferenceHelper(context)
            val config = preferenceHelper.get(RTC_CONFIG, "")
            return if (config.isNotEmpty()) {
                Gson().fromJson(config, RtcEngine::class.java)
            } else null
        }

        @JvmStatic
        fun create(context: Context): Room = LiveKitProvider.createRoom(context).also {
            room = it
        }

        suspend fun connectInRoom(url: String, token: String) {
//            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
//            scope.launch {
            room?.connect(
                url = url, token = token, options = ConnectOptions(
                    audio = true, video = true, autoSubscribe = true
                )
            )
//            }
        }

        fun leaveRoom() = room?.disconnect() ?: Timber.e { "Room not disconnect" }
    }
}
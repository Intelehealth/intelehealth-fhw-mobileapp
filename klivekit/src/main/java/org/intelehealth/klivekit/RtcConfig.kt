package org.intelehealth.klivekit

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
        private lateinit var callIntentClass: Class<*>
        private lateinit var chatIntentClass: Class<*>
        private lateinit var callLogIntentClass: Class<*>
        fun callUrl(url: String): Builder {
            this@Builder.callUrl = url
            return this@Builder
        }

        fun socketUrl(socketUrl: String): Builder {
            this@Builder.socketUrl = socketUrl
            return this@Builder
        }

        fun callIntentClass(clazz: Class<*>): Builder {
            this@Builder.callIntentClass = clazz
            return this@Builder
        }

        fun chatIntentClass(clazz: Class<*>): Builder {
            this@Builder.chatIntentClass = clazz
            return this@Builder
        }

        fun callLogIntentClass(clazz: Class<*>): Builder {
            this@Builder.callLogIntentClass = clazz
            return this@Builder
        }

        fun build(): RtcConfig {
            return RtcConfig(
                callUrl = this@Builder.callUrl,
                socketUrl = this@Builder.socketUrl,
                callIntentClass = this@Builder.callIntentClass.name,
                chatIntentClass = this@Builder.chatIntentClass.name,
                callLogIntentClass = this@Builder.callLogIntentClass.name,
            )
        }
    }
}
package org.intelehealth.klivekit.chat.socket

import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.client.Socket.EVENT_DISCONNECT
import io.socket.emitter.Emitter
import org.intelehealth.klivekit.chat.listener.ConversationListener
import org.intelehealth.klivekit.chat.listener.MessageListener
import org.intelehealth.klivekit.socket.SocketManager
import java.lang.RuntimeException

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 18:47.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatSocket(private val url: String?) : SocketManager(url) {
    var messageListener: MessageListener? = null
    var conversationListener: ConversationListener? = null

    private fun emitter(event: String) = Emitter.Listener {
        when (event) {
            EVENT_MESSAGE -> {}
            EVENT_CONNECT -> connected(it)
            EVENT_DISCONNECT -> {}
        }
    }

    fun connect() {
        connect(this::emitter)
    }

    private fun connected(status: Array<Any>) {
        Timber.e { "Socket connected => ${Gson().toJson(status)}" }
    }
}
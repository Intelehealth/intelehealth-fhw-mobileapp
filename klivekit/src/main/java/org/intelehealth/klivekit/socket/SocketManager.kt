package org.intelehealth.klivekit.socket

import com.github.ajalt.timberkt.Timber
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.client.Socket.EVENT_DISCONNECT
import io.socket.emitter.Emitter
import java.lang.RuntimeException

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 18:47.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class SocketManager() {
    var socket: Socket? = null
    var emitterListener: ((event: String) -> Emitter.Listener)? = null

    fun connect(url: String?) {
        url?.let {
            socket = IO.socket(url)
            socket?.on(EVENT_CONNECT, emitter(EVENT_CONNECT))
            socket?.on(EVENT_DISCONNECT, emitter(EVENT_DISCONNECT))
            socket?.on(EVENT_IS_READ, emitter(EVENT_IS_READ))
            socket?.on(EVENT_UPDATE_MESSAGE, emitter(EVENT_UPDATE_MESSAGE))
            socket?.on(EVENT_IP_ADDRESS, emitter(EVENT_IP_ADDRESS))
            socket?.on(EVENT_BYE, emitter(EVENT_BYE))
            socket?.on(EVENT_NO_ANSWER, emitter(EVENT_NO_ANSWER))
            socket?.on(EVENT_CREATED, emitter(EVENT_CREATED))
            socket?.on(EVENT_FULL, emitter(EVENT_FULL))
            socket?.on(EVENT_JOIN, emitter(EVENT_JOIN))
            socket?.on(EVENT_JOINED, emitter(EVENT_JOINED))
            socket?.on(EVENT_READY, emitter(EVENT_READY))
            socket?.on(EVENT_LOG, emitter(EVENT_LOG))
            socket?.on(EVENT_MESSAGE, emitter(EVENT_MESSAGE))
            socket?.on(EVENT_CALL, emitter(EVENT_CALL))
            socket?.on(EVENT_ALL_USER, emitter(EVENT_ALL_USER))
            socket?.on(EVENT_CREATE_OR_JOIN_HW, emitter(EVENT_CREATE_OR_JOIN_HW))
            socket?.connect() ?: Timber.e { "Socket is null" }
        } ?: Timber.e { "Socket url must not be empty" }
    }

    private fun emitter(event: String) = Emitter.Listener {
        emitterListener?.invoke(event)?.call(it)
    }

    fun emit(event: String, args: Any? = null) {
        Timber.e { "Socket $event args $args" }
        socket?.emit(event, args) ?: Timber.e { "$event fail due to socket not connected " }
    }

    fun disconnect() {
        socket?.off(EVENT_IS_READ)
        socket?.off(EVENT_UPDATE_MESSAGE)
        socket?.off(EVENT_IP_ADDRESS)
        socket?.off(EVENT_BYE)
        socket?.off(EVENT_NO_ANSWER)
        socket?.off(EVENT_CREATED)
        socket?.off(EVENT_FULL)
        socket?.off(EVENT_JOIN)
        socket?.off(EVENT_JOINED)
        socket?.off(EVENT_READY)
        socket?.off(EVENT_LOG)
        socket?.off(EVENT_MESSAGE)
        socket?.off(EVENT_CALL)
        socket?.off(EVENT_CREATE_OR_JOIN_HW)
        socket?.off(EVENT_ALL_USER)
        socket?.disconnect()
    }

    fun reconnect() = socket?.connect()

    fun isConnected(): Boolean = socket?.connected() ?: false

    companion object {
        const val EVENT_IS_READ = "isread"
        const val EVENT_UPDATE_MESSAGE = "updateMessage"
        const val EVENT_IP_ADDRESS = "ipaddr"
        const val EVENT_BYE = "bye"
        const val EVENT_NO_ANSWER = "no_answer"
        const val EVENT_CREATED = "created"
        const val EVENT_FULL = "full"
        const val EVENT_JOIN = "join"
        const val EVENT_JOINED = "joined"
        const val EVENT_READY = "ready"
        const val EVENT_LOG = "log"
        const val EVENT_MESSAGE = "message"
        const val EVENT_HW_CALL_REJECT = "hw_call_reject"
        const val EVENT_CALL = "call"
        const val EVENT_CREATE_OR_JOIN_HW = "create_or_join_hw"
        const val EVENT_CREATE_OR_JOIN = "create or join"
        const val EVENT_ALL_USER = "allUsers"

        // Chat event
        const val EVENT_MESSAGE_RECEIVED = "message_received" // message read by receiver
        const val EVENT_MESSAGE_SENT = "message_sent" // sent message
        const val EVENT_MESSAGE_SENT_ACK = "message_sent_ack" // sent message ack
        const val EVENT_MESSAGE_DELIVER = "message_deliver" // message delivered to receiver
        const val EVENT_MESSAGE_READ = "message_read" // sender received
        const val EVENT_MESSAGE_READ_ACK = "message_read_ack" // message mark as read ack
        const val EVENT_CHAT_READ_ACK = "chat_read_ack" // all message mark as read
        const val EVENT_CHAT_READ_ACK_SUCCESS = "chat_read_ack_success" // all message mark as read
        const val EVENT_CHAT_READ =
            "chat_read"//sender received status, all message read by receiver
    }
}
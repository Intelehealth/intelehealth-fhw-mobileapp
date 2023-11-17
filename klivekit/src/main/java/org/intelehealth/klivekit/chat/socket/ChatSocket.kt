package org.intelehealth.klivekit.chat.socket

import com.codeglo.coyamore.agora.extensions.fromJson
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.intelehealth.klivekit.chat.listener.ConnectionListener
import org.intelehealth.klivekit.chat.listener.ConversationListener
import org.intelehealth.klivekit.chat.listener.EventCallback
import org.intelehealth.klivekit.chat.listener.MessageListener
import org.intelehealth.klivekit.chat.model.CMessage
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_CHAT_READ
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_CHAT_READ_ACK_SUCCESS
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_DELIVER
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_READ
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_READ_ACK
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_RECEIVED
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_SENT
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_SENT_ACK

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 18:47.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatSocket(private val socketManager: SocketManager) {
    var messageListener: MessageListener? = null
    var conversationListener: ConversationListener? = null
    var connectionListener: ConnectionListener? = null
    private val eventCallbackMap = HashMap<String, EventCallback<Any>?>()

    init {
        socketManager.emitterListener = this::emitter
    }

    fun connect(socketUrl: String) = socketManager.connect(socketUrl)

    fun isConnected() = socketManager.isConnected()

    private fun emitter(event: String) = Emitter.Listener {
        when (event) {
            EVENT_MESSAGE_RECEIVED -> onMessageReceived(it)
            EVENT_MESSAGE_SENT_ACK -> onMessageSent(it)
            EVENT_MESSAGE_DELIVER -> onMessageDeliver(it)
            EVENT_MESSAGE_READ -> onMessageRead(it)
            EVENT_CHAT_READ -> onConversationRead(it)
            EVENT_CHAT_READ_ACK_SUCCESS -> onConversationReadSuccess(it)
            Socket.EVENT_CONNECT -> connectionListener?.onConnected()
            Socket.EVENT_DISCONNECT -> connectionListener?.onDisconnected()
        }
    }

    private fun onConversationReadSuccess(it: Array<Any>?) {
        it?.let {
            val gson = Gson()
            val ack = gson.fromJson<HashMap<String, String>>(gson.toJson(it))
            eventCallbackMap[EVENT_CHAT_READ_ACK_SUCCESS]?.onSuccess(ack)
        }
    }

    private fun onConversationRead(it: Array<Any>?) {
        it?.let {
            val gson = Gson()
            val ack = gson.fromJson<HashMap<String, String>>(gson.toJson(it))
            conversationListener?.onConversationRead(ack[SENDER_ID], ack[RECEIVER_ID])
        }
    }

    private fun onMessageRead(it: Array<Any>?) {
        it?.let {
            val gson = Gson()
            val messages = gson.fromJson<MutableList<CMessage>>(gson.toJson(it))
            messageListener?.onMessageRead(messages)
        }
    }

    private fun onMessageDeliver(it: Array<Any>?) {
        it?.let {
            val gson = Gson()
            val messages = gson.fromJson<MutableList<CMessage>>(gson.toJson(it))
            messageListener?.onMessageDelivered(messages)
        }
    }

    private fun onMessageSent(it: Array<Any>?) {
        it?.let {
            val gson = Gson()
            val messageId = gson.fromJson<HashMap<String, String>>(gson.toJson(it))
            messageId[MESSAGE_ID]?.let { c ->
                eventCallbackMap[EVENT_MESSAGE_SENT_ACK]?.onSuccess(messageId)
            }
        }
    }

    private fun onMessageReceived(it: Array<Any>?) {
        it?.let {
            val gson = Gson()
            val messages = gson.fromJson<MutableList<CMessage>>(gson.toJson(it))
            messageListener?.onMessageReceived(messages)
        }
    }

    fun sentMessage(cMessage: CMessage, callback: EventCallback<Any>? = null) {
        eventCallbackMap[EVENT_MESSAGE_SENT_ACK] = callback
        socketManager.emit(EVENT_MESSAGE_SENT, Gson().toJson(cMessage))
    }


    fun ackMessageRead(messageId: String) {
        HashMap<String, String>().apply {
            put(MESSAGE_ID, messageId)
        }.also {
            socketManager.emit(EVENT_MESSAGE_READ_ACK, Gson().toJson(it))
        }
    }

    fun ackConversationRead(
        senderId: String,
        receiverId: String,
        callback: EventCallback<Any>? = null
    ) {
        eventCallbackMap[EVENT_CHAT_READ_ACK_SUCCESS] = callback
        HashMap<String, String>().apply {
            put(SENDER_ID, senderId)
            put(RECEIVER_ID, receiverId)
        }.also {
            socketManager.emit(EVENT_MESSAGE_READ_ACK, Gson().toJson(it))
        }
    }

    companion object {
        const val SENDER_ID = "senderId"
        const val RECEIVER_ID = "receiverId"
        const val MESSAGE_ID = "messageId"
    }
}
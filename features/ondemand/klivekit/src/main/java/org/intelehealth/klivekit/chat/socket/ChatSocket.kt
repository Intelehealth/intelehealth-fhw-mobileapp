package org.intelehealth.klivekit.chat.socket

import org.intelehealth.klivekit.utils.extensions.fromJson
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.intelehealth.klivekit.chat.listener.ConnectionListener
import org.intelehealth.klivekit.chat.listener.ConversationListener
import org.intelehealth.klivekit.chat.listener.EventCallback
import org.intelehealth.klivekit.chat.listener.MessageListener
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.MessageStatus
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_CHAT_READ_ACK_SUCCESS
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_IS_READ
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_READ_ACK
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_SENT
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MESSAGE_SENT_ACK
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_MSG_DELIVERED
import org.intelehealth.klivekit.socket.SocketManager.Companion.EVENT_UPDATE_MESSAGE
import org.json.JSONArray
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 18:47.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatSocket @Inject constructor(private val socketManager: SocketManager) {
    var messageListener: MessageListener? = null
    var conversationListener: ConversationListener? = null
    var connectionListener: ConnectionListener? = null
    private val eventCallbackMap = HashMap<String, EventCallback<Any>?>()
    private val gson = Gson()

    init {
        socketManager.emitterListener = this::emitter
    }

    fun connect(socketUrl: String) = socketManager.connect(socketUrl)

    fun isConnected() = socketManager.isConnected()

    private fun emitter(event: String) = Emitter.Listener {
        when (event) {
            EVENT_UPDATE_MESSAGE -> onMessageReceived(it)
            EVENT_IS_READ -> onMessageRead(it)
            EVENT_MSG_DELIVERED -> onMessageDeliver(it)

//            EVENT_MESSAGE_RECEIVED -> onMessageReceived(it)
//            EVENT_MESSAGE_SENT_ACK -> onMessageSent(it)
//            EVENT_MESSAGE_DELIVER -> onMessageDeliver(it)
//            EVENT_MESSAGE_READ -> onMessageRead(it)
//            EVENT_CHAT_READ -> onConversationRead(it)
//            EVENT_CHAT_READ_ACK_SUCCESS -> onConversationReadSuccess(it)
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
            val json = JSONArray(it[0]).getJSONArray(0).getJSONObject(0).toString()
            val message = gson.fromJson<ChatMessage>(json)
            messageListener?.onMessageRead(arrayListOf(message))
        }
    }

    private fun onMessageDeliver(it: Array<Any>?) {
        it?.let {
            val json = JSONArray(it[0]).getJSONArray(0).getJSONObject(0).toString()
            val message = gson.fromJson<ChatMessage>(json)
            messageListener?.onMessageDelivered(arrayListOf(message))
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
            val jsonObject = JSONArray(gson.toJson(it[0]))
                .getJSONObject(0)
                .getJSONObject("nameValuePairs")

            val message = gson.fromJson(jsonObject.toString(), ChatMessage::class.java)
            message.messageStatus = MessageStatus.RECEIVED.value
            ackMessageReceived(message.messageId)
            messageListener?.onMessageReceived(arrayListOf(message))
        }
    }

    fun sentMessage(chatMessage: ChatMessage, callback: EventCallback<Any>? = null) {
        eventCallbackMap[EVENT_MESSAGE_SENT_ACK] = callback
        socketManager.emit(EVENT_MESSAGE_SENT, Gson().toJson(chatMessage))
    }


    fun ackMessageRead(messageId: Int) {
        HashMap<String, Int>().apply {
            put(MESSAGE_ID, messageId)
        }.also {
            socketManager.emit(EVENT_MESSAGE_READ_ACK, Gson().toJson(it))
        }
    }

    private fun ackMessageReceived(messageId: Int) {
        HashMap<String, Int>().apply {
            put(MESSAGE_ID, messageId)
        }.also {
            socketManager.emit(SocketManager.EVENT_ACK_MSG_RECEIVED, Gson().toJson(it))
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
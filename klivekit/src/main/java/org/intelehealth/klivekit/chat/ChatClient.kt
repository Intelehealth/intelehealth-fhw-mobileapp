package org.intelehealth.klivekit.chat

import org.intelehealth.klivekit.chat.listener.ConnectionListener
import org.intelehealth.klivekit.chat.listener.EventCallback
import org.intelehealth.klivekit.chat.model.CMessage
import org.intelehealth.klivekit.chat.socket.ChatSocket

/**
 * Created by Vaghela Mithun R. on 08-07-2023 - 12:00.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatClient(
    private val chatSocket: ChatSocket,
    messageHandler: MessageHandler
) : ConnectionListener {

    init {
        chatSocket.messageListener = messageHandler
        chatSocket.conversationListener = messageHandler
        chatSocket.connectionListener = this
    }

    fun connect(socketUrl: String) = chatSocket.connect(socketUrl)

    fun isConnected() = chatSocket.isConnected()

    fun sendMessage(cMessage: CMessage, callback: EventCallback<String>? = null) =
        chatSocket.sentMessage(cMessage)

    fun ackMessageAsRead(messageId: String) = chatSocket.ackMessageRead(messageId)

    fun ackConversationRead(senderId: String, receiverId: String) =
        chatSocket.ackConversationRead(senderId, receiverId)

    override fun onConnected() {

    }

    override fun onDisconnected() {

    }
}
package org.intelehealth.klivekit.chat

import org.intelehealth.klivekit.chat.data.ChatRepository
import org.intelehealth.klivekit.chat.listener.ConversationListener
import org.intelehealth.klivekit.chat.listener.MessageListener
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.socket.ChatSocket

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class MessageHandler(
    private val socket: ChatSocket,
    private val chatRepository: ChatRepository
) : MessageListener, ConversationListener {

    init {
        socket.messageListener = this
        socket.conversationListener = this
    }

    override fun onConversationUpdate() {
        TODO("Not yet implemented")
    }

    override fun onConversationRead(senderId: String?, receiverId: String?) {
        TODO("Not yet implemented")
    }

    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
        TODO("Not yet implemented")
    }

    override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
        TODO("Not yet implemented")
    }

    override fun onMessageDelivered(messages: MutableList<ChatMessage>?) {
        TODO("Not yet implemented")
    }

    override fun onMessageRead(messages: MutableList<ChatMessage>?) {
        TODO("Not yet implemented")
    }
}
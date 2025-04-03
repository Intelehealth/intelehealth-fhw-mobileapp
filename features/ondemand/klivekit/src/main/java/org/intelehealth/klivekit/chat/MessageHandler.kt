package org.intelehealth.klivekit.chat

import org.intelehealth.klivekit.data.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.intelehealth.klivekit.chat.data.ChatRepository
import org.intelehealth.klivekit.chat.listener.ConversationListener
import org.intelehealth.klivekit.chat.listener.MessageListener
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.MessageStatus
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class MessageHandler @Inject constructor(
    private val chatRepository: ChatRepository,
    private val preferenceHelper: PreferenceHelper
) : MessageListener, ConversationListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onConversationUpdate() {

    }

    override fun onConversationRead(senderId: String?, receiverId: String?) {

    }

    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
        scope.launch {
            messages?.let { chatRepository.addMessages(it) }
        }
    }

    override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {

    }

    override fun onMessageDelivered(messages: MutableList<ChatMessage>?) {
        scope.launch {
            messages?.let {
                chatRepository.changeMessageStatus(it.map { it.messageId }, MessageStatus.DELIVERED)
            }
        }
    }

    override fun onMessageRead(messages: MutableList<ChatMessage>?) {
        scope.launch {
            messages?.let {
                chatRepository.changeMessageStatus(it.map { it.messageId }, MessageStatus.READ)
            }
        }
    }

    fun cancel() {
        if (scope.isActive) scope.cancel()
    }
}
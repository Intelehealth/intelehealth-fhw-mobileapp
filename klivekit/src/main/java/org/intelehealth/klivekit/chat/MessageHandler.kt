package org.intelehealth.klivekit.chat

import com.codeglo.coyamore.data.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.intelehealth.klivekit.chat.data.ChatRepository
import org.intelehealth.klivekit.chat.listener.ConversationListener
import org.intelehealth.klivekit.chat.listener.MessageListener
import org.intelehealth.klivekit.chat.model.CMessage

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class MessageHandler(
    private val chatRepository: ChatRepository,
    private val preferenceHelper: PreferenceHelper
) : MessageListener, ConversationListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onConversationUpdate() {

    }

    override fun onConversationRead(senderId: String?, receiverId: String?) {

    }

    override fun onMessageReceived(messages: MutableList<CMessage>?) {

    }

    override fun onCmdMessageReceived(messages: MutableList<CMessage>?) {

    }

    override fun onMessageDelivered(messages: MutableList<CMessage>?) {

    }

    override fun onMessageRead(messages: MutableList<CMessage>?) {

    }
}
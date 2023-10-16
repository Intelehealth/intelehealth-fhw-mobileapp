package org.intelehealth.klivekit.chat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.intelehealth.klivekit.chat.ChatClient
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.call.ui.viewmodel.VideoCallViewModel
import org.intelehealth.klivekit.utils.AwsS3Utils
import org.intelehealth.klivekit.chat.data.ChatRepository
import org.intelehealth.klivekit.chat.model.ChatMessage
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 18-07-2023 - 23:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatViewModel(
    private val chatClient: ChatClient = ChatClient(),
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    fun connect(url: String) {
        if (chatClient.isConnected().not()) chatClient.connect(url)
    }

    fun sendMessage(message: ChatMessage) = chatClient.sendMessage(message)

    fun sendReadStatus(messageId: Int) = chatClient.markMessageAsRead(messageId)

    fun getAllMessages(chatRoomId: String) = chatRepository.getAllMessages(chatRoomId).asLiveData()
}
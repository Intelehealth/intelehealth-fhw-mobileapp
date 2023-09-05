package org.intelehealth.klivekit.chat.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.emitter.Emitter
import org.intelehealth.klivekit.chat.ChatClient
import org.intelehealth.klivekit.chat.data.ChatRepository
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.ui.viewmodel.VideoCallViewModel
import org.intelehealth.klivekit.utils.AwsS3Utils
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 18-07-2023 - 23:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val chatRepository: ChatRepository
) : ViewModel() {

    fun connect(url: String) {
        if (chatClient.isConnected().not()) chatClient.connect(url)
    }

    fun sendMessage(message: ChatMessage) = chatClient.sendMessage(message)

    fun sendReadStatus(messageId: Int) = chatClient.markMessageAsRead(messageId)

    fun getAllMessages(chatRoomId: String) = chatRepository.getAllMessages(chatRoomId).asLiveData()
}
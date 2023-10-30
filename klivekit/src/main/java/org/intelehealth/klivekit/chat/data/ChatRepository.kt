package org.intelehealth.klivekit.chat.data

import org.intelehealth.klivekit.room.dao.ChatDao
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.MessageStatus
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val dataSource: ChatDataSource
) {

    suspend fun addMessage(message: ChatMessage) = chatDao.addMessage(message)

    suspend fun addMessages(messages: List<ChatMessage>) = chatDao.insertAll(messages)

    fun getAllMessages() = chatDao.getAll()

    suspend fun changeMessageStatus(messageId: Int, messageStatus: MessageStatus) =
        chatDao.changeMessageStatus(messageId, messageStatus.name)

    suspend fun changeMessageStatus(messageIds: List<Int>, messageStatus: MessageStatus) =
        chatDao.changeMessageStatus(messageIds, messageStatus.name)

    suspend fun sendMessage(message: ChatMessage) = dataSource.sendMessage(message)
}
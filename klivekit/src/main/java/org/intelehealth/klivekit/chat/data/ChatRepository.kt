package org.intelehealth.klivekit.chat.data

import com.codeglo.billingclient.room.dao.ChatDao
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.MessageStatus

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatRepository(private val chatDao: ChatDao) {

    suspend fun addMessage(message: ChatMessage) = chatDao.addMessage(message)

    suspend fun getAllMessages() = chatDao.getAll()

    suspend fun changeMessageStatus(messageId: String, messageStatus: MessageStatus) =
        chatDao.changeMessageStatus(messageId, messageStatus.name)
}
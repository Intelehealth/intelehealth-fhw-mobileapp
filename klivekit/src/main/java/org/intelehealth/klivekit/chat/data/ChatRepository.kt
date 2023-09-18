package org.intelehealth.klivekit.chat.data

import org.intelehealth.klivekit.RtcApp
import org.intelehealth.klivekit.room.dao.ChatDao
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.ChatRoom
import org.intelehealth.klivekit.chat.model.MessageStatus
import org.intelehealth.klivekit.room.WebRtcDatabase
import org.intelehealth.klivekit.room.dao.ChatRoomDao
import org.webrtc.EglBase10.Context
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatRepository(
    private val chatDao: ChatDao = RtcApp.database.chatDao(),
    private val chatRoomDao: ChatRoomDao = RtcApp.database.chatRoomDao(),
    private val dataSource: ChatDataSource = ChatDataSource()
) {

    suspend fun addMessage(message: ChatMessage) = chatDao.addMessage(message)

    suspend fun addMessages(messages: List<ChatMessage>) = chatDao.insertAll(messages)

    fun getAllMessages(chatRoomId: String) = chatDao.getAll(chatRoomId)

    suspend fun changeMessageStatus(messageId: Int, messageStatus: MessageStatus) =
        chatDao.changeMessageStatusByMessageId(messageId, messageStatus.name)

    suspend fun changeMessageStatus(messageIds: List<Int>, messageStatus: MessageStatus) =
        chatDao.changeMessageStatusByListOfMessageId(messageIds, messageStatus.name)

    suspend fun sendMessage(message: ChatMessage) = dataSource.sendMessage(message)

    suspend fun markAsRead(messageId: Int) = dataSource.markAsRead(messageId)

    suspend fun getChatRoom(roomId: String) = chatRoomDao.getChatRoom(roomId)

    suspend fun addChatRoom(newRoom: ChatRoom) = chatRoomDao.addChatRoom(newRoom)

    suspend fun addAllChatRoom(rooms: List<ChatRoom>) = chatRoomDao.insertAll(rooms)

    fun getAllChatRoom() = chatRoomDao.getChatRooms()

    suspend fun deleteAllChatRooms() = chatRoomDao.deleteAll()
}
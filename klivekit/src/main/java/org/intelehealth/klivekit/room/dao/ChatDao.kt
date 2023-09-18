package org.intelehealth.klivekit.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.jetbrains.annotations.NotNull


/**
 * Created by Vaghela Mithun R. on 04-01-2023 - 15:58.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
@Dao
interface ChatDao {
    @Query("SELECT * FROM tbl_chat_message WHERE roomId =:chatRoomId")
    fun getAll(chatRoomId: String): Flow<List<ChatMessage>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessage>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMessage(message: ChatMessage): Long

    @Query("UPDATE tbl_chat_message SET messageStatus= :status where messageId =:messageId")
    suspend fun changeMessageStatusByMessageId(messageId: Int, status: String): Int

    @Query("UPDATE tbl_chat_message SET messageStatus= :status where messageId IN (:messageIds)")
    suspend fun changeMessageStatusByListOfMessageId(
        messageIds: List<Int>,
        status: String
    ): Int

    @Query("DELETE FROM tbl_chat_message")
    suspend fun deleteAll()
}
package org.intelehealth.klivekit.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.intelehealth.klivekit.chat.model.ChatMessage


/**
 * Created by Vaghela Mithun R. on 04-01-2023 - 15:58.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
@Dao
interface ChatDao {
    @Query("SELECT * FROM tbl_chat_message")
    fun getAll(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMessage(message: ChatMessage)

    @Query("UPDATE tbl_chat_message SET messageStatus= :status where messageId =:messageId")
    suspend fun changeMessageStatus(messageId: Int, status: String)

    @Query("UPDATE tbl_chat_message SET messageStatus= :status where messageId IN (:messageIds)")
    suspend fun changeMessageStatus(messageIds: List<Int>, status: String)

    @Query("DELETE FROM tbl_chat_message")
    suspend fun deleteAll()
}
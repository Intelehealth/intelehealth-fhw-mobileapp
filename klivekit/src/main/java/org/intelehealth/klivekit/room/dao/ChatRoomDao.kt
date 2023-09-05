package org.intelehealth.klivekit.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.ChatRoom


/**
 * Created by Vaghela Mithun R. on 04-01-2023 - 15:58.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
@Dao
interface ChatRoomDao {
    @Query("SELECT * FROM tbl_chat_room")
    fun getChatRooms(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chatRooms: List<ChatRoom>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addChatRoom(newRoom: ChatRoom)

    @Query("DELETE FROM tbl_chat_room")
    suspend fun deleteAll()

    @Query("SELECT roomId FROM tbl_chat_room WHERE roomId =:roomId")
    suspend fun getChatRoom(roomId: String): String?
}
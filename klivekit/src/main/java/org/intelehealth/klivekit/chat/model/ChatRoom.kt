package org.intelehealth.klivekit.chat.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by Vaghela Mithun R. on 04-09-2023 - 16:18.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Parcelize
@Entity(tableName = "tbl_chat_room")
data class ChatRoom(
    @PrimaryKey
    var roomId: String,
    var roomName: String? = null,
    var roomPicture: String? = null,
    var senderId: String? = null,
    var receiverId: String? = null,
) : Parcelable

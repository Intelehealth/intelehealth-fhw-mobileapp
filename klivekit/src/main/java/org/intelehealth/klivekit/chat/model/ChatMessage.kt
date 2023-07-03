package org.intelehealth.klivekit.chat.model

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 15:32.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val receiverName: String = "",
    val message: String = "",
    val time: Long,
    val status: String,
    val msgType: Int,
    var markAsRead: Int = ReadStatus.UNREAD.value
)
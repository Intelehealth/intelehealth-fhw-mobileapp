package org.intelehealth.klivekit.chat.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.intelehealth.klivekit.utils.DateTimeUtils

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 15:32.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Entity(tableName = "tbl_chat_message")
data class ChatMessage(
    @PrimaryKey
    @SerializedName("id")
    var messageId: Int = 0,
    @SerializedName("roomId")
    var roomId: String? = null,
    @SerializedName("fromUser")
    var senderId: String = "",
    @SerializedName("toUser")
    var receiverId: String = "",
    var senderName: String = "",
    var receiverName: String = "",
    @SerializedName("message")
    var message: String = "",
    var messageStatus: Int = 0,
    var hwName: String? = null,
    @SerializedName("isRead")
    var isRead: Boolean = false,
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("patientId")
    var patientId: String? = null,
    @SerializedName("patientName")
    var roomName: String? = null,
    var hwPic: String? = null,
    var patientPic: String? = null,
    var visitId: String? = null,
    @SerializedName("updatedAt")
    var updatedAt: String? = null,
    @SerializedName("type")
    var type: String? = null
) : ItemHeader {

    var layoutType = 0
    var loading = false

    override fun createdDate(): String {
        return createdAt?.let { return@let DateTimeUtils.getCurrentDateWithDBFormat() }!!
    }

    fun getMessageTime(): String? {
        val date = DateTimeUtils.parseUTCDate(createdDate(), DateTimeUtils.DB_FORMAT)
        return DateTimeUtils.formatToLocalDate(date, DateTimeUtils.MESSAGE_TIME_FORMAT)
    }

    fun getMessageDay(): String {
        val date = DateTimeUtils.parseUTCDate(createdDate(), DateTimeUtils.DB_FORMAT)
        return DateTimeUtils.formatToLocalDate(date, DateTimeUtils.MESSAGE_DAY_FORMAT)
    }

    fun isAttachment(): Boolean {
        return if (type == null) false else type == "attachment"
    }

    fun toJson(): String? {
        return Gson().toJson(this)
    }

    override fun isHeader(): Boolean {
        return false
    }
}
package org.intelehealth.klivekit.chat.model


/**
 * Created by Vaghela Mithun R. on 16-03-2023 - 20:52.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
enum class MessageType {
    RECEIVED, DELIVERED, READ, CMD_RECEIVED;

    fun isReceived() = this == RECEIVED

    fun isDelivered() = this == DELIVERED

    fun isRead() = this == READ

    fun isCmdReceived() = this == CMD_RECEIVED
}

enum class MsgType {
    SENT, RECEIVED
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, SEEN
}

enum class ReadStatus(val value: Int) {
    UNREAD(0), READ(1)
}
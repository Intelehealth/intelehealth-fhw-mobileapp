package org.intelehealth.klivekit.chat.listener

import org.intelehealth.klivekit.chat.model.ChatMessage

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface MessageListener {
    fun onMessageReceived(messages: MutableList<ChatMessage>?)
    fun onCmdMessageReceived(messages: MutableList<ChatMessage>?)
    fun onMessageDelivered(messages: MutableList<ChatMessage>?)
    fun onMessageRead(messages: MutableList<ChatMessage>?)
}
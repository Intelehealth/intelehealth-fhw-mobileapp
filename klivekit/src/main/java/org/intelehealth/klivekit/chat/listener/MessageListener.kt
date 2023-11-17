package org.intelehealth.klivekit.chat.listener

import org.intelehealth.klivekit.chat.model.CMessage

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface MessageListener {
    fun onMessageReceived(messages: MutableList<CMessage>?)
    fun onCmdMessageReceived(messages: MutableList<CMessage>?)
    fun onMessageDelivered(messages: MutableList<CMessage>?)
    fun onMessageRead(messages: MutableList<CMessage>?)
}
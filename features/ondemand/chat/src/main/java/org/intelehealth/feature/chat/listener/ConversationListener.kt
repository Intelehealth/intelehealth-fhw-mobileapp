package org.intelehealth.feature.chat.listener

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface ConversationListener {
    fun onConversationUpdate()
    fun onConversationRead(senderId: String?, receiverId: String?)
}
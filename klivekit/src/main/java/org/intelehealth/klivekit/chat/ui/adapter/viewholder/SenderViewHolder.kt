package org.intelehealth.klivekit.chat.ui.adapter.viewholder

import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.databinding.RowMsgItemSenderBinding

/**
 * Created by Vaghela Mithun R. on 15-08-2023 - 00:18.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SenderViewHolder(val binding: RowMsgItemSenderBinding) : BaseViewHolder(binding.root) {

    fun bind(chatMessage: ChatMessage){
        binding.chatMessage = chatMessage
    }
}
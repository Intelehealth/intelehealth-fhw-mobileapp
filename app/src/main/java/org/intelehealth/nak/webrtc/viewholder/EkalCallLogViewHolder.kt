package org.intelehealth.nak.webrtc.viewholder

import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.nak.databinding.RowItemEkalCallLogBinding

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 16:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class EkalCallLogViewHolder(private val binding: RowItemEkalCallLogBinding) :
    BaseViewHolder(binding.root) {
    fun bind(callLog: RtcCallLog) {
        binding.callLog = callLog
        binding.btnCallLogCallback.setOnClickListener(this)
        binding.btnCallLogChat.setOnClickListener(this)
    }
}
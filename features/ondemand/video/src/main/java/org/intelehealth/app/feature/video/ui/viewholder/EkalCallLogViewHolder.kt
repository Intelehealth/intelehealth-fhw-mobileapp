package org.intelehealth.app.feature.video.ui.viewholder

import org.intelehealth.core.ui.viewholder.BaseViewHolder
import org.intelehealth.app.feature.video.databinding.RowItemEkalCallLogBinding
import org.intelehealth.app.feature.video.model.VideoCallLog


/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 16:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class EkalCallLogViewHolder(private val binding: RowItemEkalCallLogBinding) :
    BaseViewHolder(binding.root) {
    fun bind(callLog: VideoCallLog) {
        binding.callLog = callLog
        binding.btnCallLogCallback.setOnClickListener(this)
        binding.btnCallLogChat.setOnClickListener(this)
    }
}
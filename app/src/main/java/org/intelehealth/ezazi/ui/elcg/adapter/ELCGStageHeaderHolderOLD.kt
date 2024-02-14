package org.intelehealth.ezazi.ui.elcg.adapter

import org.intelehealth.ezazi.databinding.RowItemElcgStageHeaderBinding
import org.intelehealth.ezazi.ui.elcg.model.StageHeader
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

class ELCGStageHeaderHolderOLD (private val binding: RowItemElcgStageHeaderBinding) : BaseViewHolder(
        binding.root
) {
    fun bind(stage: StageHeader) {
        binding.stage = binding.tvStage.context.resources.getString(stage.stage)
    }
}
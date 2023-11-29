package org.intelehealth.ezazi.ui.elcg.adapter.viewholder

import org.intelehealth.ezazi.databinding.RowItemElcgDataBinding
import org.intelehealth.ezazi.databinding.RowItemElcgStageHeaderBinding
import org.intelehealth.ezazi.ui.elcg.model.ELCGData
import org.intelehealth.ezazi.ui.elcg.model.StageHeader
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

/**
 * Created by Vaghela Mithun R. on 24-11-2023 - 02:03.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class ELCGStageHeaderHolder(private val binding: RowItemElcgStageHeaderBinding) : BaseViewHolder(
    binding.root
) {
    fun bind(stage: StageHeader) {
        binding.stage = binding.tvStage.context.resources.getString(stage.stage)
    }
}
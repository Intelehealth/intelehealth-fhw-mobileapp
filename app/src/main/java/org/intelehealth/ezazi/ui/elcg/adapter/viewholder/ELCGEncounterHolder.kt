package org.intelehealth.ezazi.ui.elcg.adapter.viewholder

import androidx.core.view.isVisible
import org.intelehealth.ezazi.databinding.RowItemElcgDataBinding
import org.intelehealth.ezazi.databinding.RowItemElcgEncounterBinding
import org.intelehealth.ezazi.models.dto.EncounterDTO
import org.intelehealth.ezazi.ui.elcg.model.ELCGData
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

/**
 * Created by Vaghela Mithun R. on 24-11-2023 - 02:03.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class ELCGEncounterHolder(private val binding: RowItemElcgEncounterBinding) : BaseViewHolder(
    binding.root
) {
    fun bind(encounter: EncounterDTO) {
        binding.encounter = encounter
    }

    fun hideTopLine(hide: Boolean) {
        binding.viewElcgGraphTopLine.isVisible = hide.not()
    }

    fun hideBelowLine(hide: Boolean) {
        binding.viewElcgGraphBottomLine.isVisible = hide.not()
        binding.viewDivider.isVisible = hide.not()
    }
}
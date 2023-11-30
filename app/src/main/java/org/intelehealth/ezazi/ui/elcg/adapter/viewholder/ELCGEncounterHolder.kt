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
        setAlertCount(encounter)
        changeLineColor(encounter)
    }

    private fun changeLineColor(encounter: EncounterDTO) {
        binding.viewIndicatorElcgEncounter1.isActivated = encounter.encounterStatus.isSubmitted
        binding.viewElcgGraphBottomLine.isActivated = encounter.encounterStatus.isSubmitted
        binding.viewElcgGraphTopLine.isActivated = encounter.encounterStatus.isSubmitted
    }

    private fun setAlertCount(encounter: EncounterDTO) {
        if (encounter.alertCount > 22) binding.tvElcgAlertCount.isActivated = true
        else if (encounter.alertCount >= 15) binding.tvElcgAlertCount.isSelected = true
        else {
            binding.tvElcgAlertCount.isSelected = false
            binding.tvElcgAlertCount.isActivated = false
        }
    }

    fun hideTopLine(hide: Boolean) {
        binding.viewElcgGraphTopLine.isVisible = hide.not()
    }

    fun hideBelowLine(hide: Boolean) {
        binding.viewElcgGraphBottomLine.isVisible = hide.not()
        binding.viewDivider.isVisible = hide.not()
    }
}
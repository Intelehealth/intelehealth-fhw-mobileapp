package org.intelehealth.ezazi.partogram.viewholder

import org.intelehealth.ezazi.databinding.RowItemPrescriptionFollowedBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

class PrescriptionFollowedPlanHolder (val binding: RowItemPrescriptionFollowedBinding) :
        BaseViewHolder(binding.root) {
    fun bind(plan: ObsDTO) {
        binding.plan = plan
        binding.btnPrescriptionPlanViewMore.setOnClickListener(this)
        binding.btnEditPlan.setOnClickListener(this)
        binding.btnDeletePlan.setOnClickListener(this)
        binding.btnPrescriptionPlanViewMore.tag = plan
    }
}

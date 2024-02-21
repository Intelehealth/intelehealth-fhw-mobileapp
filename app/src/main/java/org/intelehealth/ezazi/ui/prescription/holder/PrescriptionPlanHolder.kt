package org.intelehealth.ezazi.ui.prescription.holder

import android.view.View
import org.intelehealth.ezazi.databinding.RowItemPrescriptionPlanBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

/**
 * Created by Vaghela Mithun R. on 04-02-2024 - 01:46.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionPlanHolder(val binding: RowItemPrescriptionPlanBinding) :
        BaseViewHolder(binding.root) {
    fun bind(plan: ObsDTO, makeVisible: Boolean) {
        binding.plan = plan
        binding.btnPrescriptionPlanViewMore.setOnClickListener(this)
        binding.btnPrescriptionPlanViewMore.tag = plan
           }
}
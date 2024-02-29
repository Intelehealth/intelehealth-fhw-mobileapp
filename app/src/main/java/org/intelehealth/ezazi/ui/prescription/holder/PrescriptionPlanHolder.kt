package org.intelehealth.ezazi.ui.prescription.holder

import android.view.View
import com.github.ajalt.timberkt.Timber
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
    fun bind(plan: ObsDTO) {
        allowInstantClick = true
        binding.plan = plan
        binding.btnPrescriptionPlanViewMore.setOnClickListener(this)
        binding.btnPrescriptionPlanViewMore.tag = plan
        Timber.d { "noOfLine:: ${plan.noOfLine}" }
        if (plan.noOfLine == 0) {
            binding.txtPlanContent.postDelayed({
                Timber.d { "Line count:: ${binding.txtPlanContent.lineCount}" }
                plan.noOfLine = binding.txtPlanContent.lineCount
                binding.txtPlanContent.maxLines = plan.contentLine
                binding.plan = plan
            }, 25)
        } else binding.txtPlanContent.maxLines = plan.contentLine
    }

    private fun updateData(plan: ObsDTO) {
        binding.plan = plan
        binding.btnPrescriptionPlanViewMore.setOnClickListener(this)
        binding.btnPrescriptionPlanViewMore.tag = plan
    }
}
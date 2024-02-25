package org.intelehealth.ezazi.ui.visit.viewholder

import android.view.View
import org.intelehealth.ezazi.databinding.ListItemVisitStatusPatientEzaziBinding
import org.intelehealth.ezazi.databinding.RowItemPrescriptionPlanBinding
import org.intelehealth.ezazi.models.dto.ObsDTO
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

/**
 * Created by Vaghela Mithun R. on 04-02-2024 - 01:46.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class OutcomePendingViewHolder(val binding: ListItemVisitStatusPatientEzaziBinding) :
    BaseViewHolder(binding.root) {
    fun bind(patient: PatientDTO) {
        binding.patient = patient
        binding.clRowVisitStatus.tag = patient
        binding.clRowVisitStatus.setOnClickListener(this)
    }
}
package org.intelehealth.ezazi.ui.prescription.holder

import org.intelehealth.ezazi.databinding.RowItemPrescriptionMedicationBinding
import org.intelehealth.ezazi.partogram.model.Medication
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder

/**
 * Created by Vaghela Mithun R. on 04-02-2024 - 01:46.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionMedicationHolder(val binding: RowItemPrescriptionMedicationBinding) :
    BaseViewHolder(binding.root) {
    fun bind(medication: Medication, allowAdminister: Boolean) {
        binding.medication = medication
        binding.allowAdminister = allowAdminister
        binding.btnMedicationAdminister.tag = medication
        binding.btnMedicationAdminister.setOnClickListener(this)
    }
}
package org.intelehealth.ezazi.partogram.viewholder;

import android.util.Log;

import androidx.annotation.NonNull;

import org.intelehealth.ezazi.databinding.RowItemIvFluidPrescriptionBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medication;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

/**
 * Created by Kaveri Zaware on 08-02-2024
 * email - kaveri@intelehealth.org
 **/
public class PrescribedIvFluidViewHolder extends BaseViewHolder {
    private final RowItemIvFluidPrescriptionBinding binding;

    public PrescribedIvFluidViewHolder(@NonNull RowItemIvFluidPrescriptionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Medication ivFluid) {
        binding.setMedication(ivFluid);
        binding.btnIvFluidAdminister.setOnClickListener(this);
       // binding.btnExpandCollapseIndicator1.setOnClickListener(this);
        binding.clMedicinePrescriptionRowItemRoot1.setOnClickListener(this);
    }

       public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        Log.d("TAG", "setAccessMode: accessMode : "+accessMode);
        binding.setAccessMode(accessMode);
    }
}

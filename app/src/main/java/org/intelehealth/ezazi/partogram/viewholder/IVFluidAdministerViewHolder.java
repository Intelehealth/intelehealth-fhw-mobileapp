package org.intelehealth.ezazi.partogram.viewholder;

import androidx.annotation.NonNull;

import org.intelehealth.ezazi.databinding.RowItemIvFluidOxytocinAdministerBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medication;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

/**
 * Created by Kaveri Zaware on 08-02-2024
 * email - kaveri@intelehealth.org
 **/
public class IVFluidAdministerViewHolder extends BaseViewHolder {
    private final RowItemIvFluidOxytocinAdministerBinding binding;

    public IVFluidAdministerViewHolder(@NonNull RowItemIvFluidOxytocinAdministerBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Medication medication) {
        binding.setMedication(medication);
        binding.clIvFluidAdministerRowItemRoot1.setOnClickListener(this);
    }

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        binding.setAccessMode(accessMode);
    }
}

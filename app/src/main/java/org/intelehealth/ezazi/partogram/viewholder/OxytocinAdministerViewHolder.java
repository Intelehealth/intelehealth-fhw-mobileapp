package org.intelehealth.ezazi.partogram.viewholder;

import androidx.annotation.NonNull;

import org.intelehealth.ezazi.databinding.RowItemOxytocinAdministerBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medication;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

/**
 * Created by Kaveri Zaware on 09-02-2024
 * email - kaveri@intelehealth.org
 **/
public class OxytocinAdministerViewHolder extends BaseViewHolder {
    private final RowItemOxytocinAdministerBinding binding;

    public OxytocinAdministerViewHolder(@NonNull RowItemOxytocinAdministerBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Medication medication) {
        binding.setMedication(medication);
        binding.clOxytocinAdministerRowItemRoot1.setOnClickListener(this);
    }

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        binding.setAccessMode(accessMode);
    }
}

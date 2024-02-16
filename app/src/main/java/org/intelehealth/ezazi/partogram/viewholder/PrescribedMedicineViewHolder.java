package org.intelehealth.ezazi.partogram.viewholder;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import org.intelehealth.ezazi.databinding.RowItemMedicineBinding;
import org.intelehealth.ezazi.databinding.RowItemMedicinePrescriptionBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

/**
 * Created by Kaveri Zaware on 06-02-2024
 * email - kaveri@intelehealth.org
 **/
public class PrescribedMedicineViewHolder extends BaseViewHolder {
    private final RowItemMedicinePrescriptionBinding binding;

    public PrescribedMedicineViewHolder(@NonNull RowItemMedicinePrescriptionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Medicine medicine, boolean makeVisible) {
        binding.setMedicine(medicine);
        binding.btnMedicineAdminister.setOnClickListener(this);
        binding.btnExpandCollapseIndicator1.setOnClickListener(this);
        binding.clMedicinePrescriptionRowItemRoot1.setOnClickListener(this);
        if (makeVisible) {
            binding.btnMedicineAdminister.setVisibility(View.VISIBLE);
        } else {
            binding.btnMedicineAdminister.setVisibility(View.GONE);
        }
    }

    public void expandDetails(boolean isExpanded) {
        binding.includePrescribedMedicineDetails.getRoot().setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        binding.btnExpandCollapseIndicator1.setRotation(isExpanded ? 270 : 0);
    }

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        Log.d("TAG", "setAccessMode: accessMode : "+accessMode);
        binding.setAccessMode(accessMode);
    }
}

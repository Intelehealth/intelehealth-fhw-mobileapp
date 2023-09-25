package org.intelehealth.ezazi.partogram.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import org.intelehealth.ezazi.databinding.RowItemMedicineBinding;
import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder;

/**
 * Created by Vaghela Mithun R. on 06-09-2023 - 18:02.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class MedicineViewHolder extends BaseViewHolder {
    private final RowItemMedicineBinding binding;

    public MedicineViewHolder(@NonNull RowItemMedicineBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Medicine medicine) {
        binding.setMedicine(medicine);
        binding.btnEditMedicine.setOnClickListener(this);
        binding.btnMedicineDelete.setOnClickListener(this);
        binding.btnExpandCollapseIndicator.setOnClickListener(this);
        binding.clMedicineRowItemRoot.setOnClickListener(this);
    }

    public void expandDetails(boolean isExpanded) {
        binding.includeMedicineDetails.getRoot().setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        binding.btnExpandCollapseIndicator.setRotation(isExpanded ? 270 : 0);
    }

    public void setAccessMode(PartogramConstants.AccessMode accessMode) {
        binding.setAccessMode(accessMode);
    }
}

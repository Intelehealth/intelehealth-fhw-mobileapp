package org.intelehealth.app.activities.visit.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.activities.visit.model.PrescribedMedicineModel;
import org.intelehealth.app.databinding.ItemPrescribedMedicationsBinding;

import java.util.List;


public class PrescribedMedicineAdapter extends RecyclerView.Adapter<PrescribedMedicineAdapter.ViewHolder> {

    private final List<PrescribedMedicineModel> medicineList;

    public PrescribedMedicineAdapter(List<PrescribedMedicineModel> medicineList) {
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemPrescribedMedicationsBinding itemBinding = ItemPrescribedMedicationsBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrescribedMedicineModel medicine = medicineList.get(position);
        holder.bind(position, medicine);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPrescribedMedicationsBinding binding;

        public ViewHolder(ItemPrescribedMedicationsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bind(int position, PrescribedMedicineModel medicine) {
            int medicineCount = position + 1;
            binding.tvMedicineName.setText(medicineCount + ". " + medicine.getMedicineName());
            binding.tvStrength.setText(medicine.getStrength());
            binding.tvNoDays.setText(medicine.getNoOfDays());
            binding.tvTiming.setText(medicine.getTiming());
            binding.tvRemarks.setText(medicine.getRemark());
        }
    }
}

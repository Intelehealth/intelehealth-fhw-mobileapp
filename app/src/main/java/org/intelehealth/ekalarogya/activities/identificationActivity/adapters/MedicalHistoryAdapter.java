package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getMedicalHistoryStrings;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.ViewPagerCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.MedicalHistory;
import org.intelehealth.ekalarogya.databinding.LayoutMedicalHistoryBinding;

import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.MedicalViewHolder> {

    private final List<MedicalHistory> medicalHistoryList;
    private final String locale;
    private final Resources resources;
    private final Resources updatedResources;
    private final ViewPagerCallback callback;

    public MedicalHistoryAdapter(List<MedicalHistory> medicalHistoryList, String locale, Resources resources, Resources updatedResources, ViewPagerCallback callback) {
        this.medicalHistoryList = medicalHistoryList;
        this.locale = locale;
        this.resources = resources;
        this.updatedResources = updatedResources;
        this.callback = callback;
    }

    @NonNull
    @Override
    public MedicalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutMedicalHistoryBinding binding = LayoutMedicalHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MedicalViewHolder(binding, resources, locale, updatedResources, callback);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalViewHolder holder, int position) {
        holder.initData(medicalHistoryList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return medicalHistoryList.size();
    }

    static class MedicalViewHolder extends RecyclerView.ViewHolder {

        public LayoutMedicalHistoryBinding binding;
        public Resources resources;
        public String locale;
        public int position;
        public Resources updatedResources;
        public ViewPagerCallback callback;
        public MedicalHistory medicalHistory;

        public MedicalViewHolder(
                @NonNull LayoutMedicalHistoryBinding binding,
                Resources resources,
                String locale,
                Resources updatedResources,
                ViewPagerCallback callback
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.resources = resources;
            this.locale = locale;
            this.updatedResources = updatedResources;
            binding.llMedicalHistory.setOnClickListener(v -> callback.getMedicalHistory(medicalHistory, position));
        }

        public void initData(MedicalHistory medicalHistory, int position) {
            this.medicalHistory = medicalHistory;
            this.position = position;

            binding.tvHypertension.setText(getMedicalHistoryStrings(medicalHistory.getHypertension(), updatedResources, resources, locale));
            if (!medicalHistory.getHypertension().equalsIgnoreCase(updatedResources.getString(R.string.generic_no))) {
                binding.tvHypertensionMedication.setText(getMedicalHistoryStrings(medicalHistory.getMedicationForBP(), updatedResources, resources, locale));
                binding.llBpMedication.setVisibility(View.VISIBLE);
            }

            if (medicalHistory.getMedicationForBP().equalsIgnoreCase(updatedResources.getString(R.string.generic_yes))) {
                binding.tvHypertensionHealthcare.setText(getMedicalHistoryStrings(medicalHistory.getHealthWorkerForBP(), updatedResources, resources, locale));
                binding.llBpHealthcareWorker.setVisibility(View.VISIBLE);
            }

            if (medicalHistory.getMedicationForBP().equalsIgnoreCase(updatedResources.getString(R.string.generic_no))) {
                binding.tvHypertensionNoMedication.setText(getMedicalHistoryStrings(medicalHistory.getReasonForNoBPMedication(), updatedResources, resources, locale));
                binding.llBpNoMedicationReason.setVisibility(View.VISIBLE);
            }

            binding.tvDiabetes.setText(getMedicalHistoryStrings(medicalHistory.getDiabetes(), updatedResources, resources, locale));

            if (!medicalHistory.getDiabetes().equalsIgnoreCase(updatedResources.getString(R.string.generic_no))) {
                binding.tvDiabetesMedication.setText(getMedicalHistoryStrings(medicalHistory.getMedicationForDiabetes(), updatedResources, resources, locale));
                binding.llDiabetesMedication.setVisibility(View.VISIBLE);
            }

            if (medicalHistory.getMedicationForDiabetes().equalsIgnoreCase(updatedResources.getString(R.string.generic_yes))) {
                binding.tvDiabetesHealthcare.setText(getMedicalHistoryStrings(medicalHistory.getHealthWorkerForDiabetes(), updatedResources, resources, locale));
                binding.llDiabetesHealthcareWorker.setVisibility(View.VISIBLE);
            }

            if (medicalHistory.getMedicationForDiabetes().equalsIgnoreCase(updatedResources.getString(R.string.generic_no))) {
                binding.tvDiabetesNoMedication.setText(getMedicalHistoryStrings(medicalHistory.getReasonForNoDiabetesMedication(), updatedResources, resources, locale));
                binding.llDiabetesNoMedicationReason.setVisibility(View.VISIBLE);
            }

            binding.tvArthritis.setText(getMedicalHistoryStrings(medicalHistory.getArthritis(), updatedResources, resources, locale));
            binding.tvAnaemia.setText(getMedicalHistoryStrings(medicalHistory.getAnaemia(), updatedResources, resources, locale));

            if (!medicalHistory.getAnaemia().equalsIgnoreCase(updatedResources.getString(R.string.generic_no))) {
                binding.tvAnemiaMedication.setText(getMedicalHistoryStrings(medicalHistory.getMedicationForAnemia(), updatedResources, resources, locale));
                binding.llAnemiaMedication.setVisibility(View.VISIBLE);
            }

            if (medicalHistory.getMedicationForAnemia().equalsIgnoreCase(updatedResources.getString(R.string.generic_yes))) {
                binding.tvAnemiaHealthcare.setText(getMedicalHistoryStrings(medicalHistory.getHealthWorkerForAnemia(), updatedResources, resources, locale));
                binding.llAnemiaHealthcareWorker.setVisibility(View.VISIBLE);
            }

            if (medicalHistory.getMedicationForAnemia().equalsIgnoreCase(updatedResources.getString(R.string.generic_no))) {
                binding.tvAnemiaNoMedication.setText(getMedicalHistoryStrings(medicalHistory.getReasonForNoAnemiaMedication(), updatedResources, resources, locale));
                binding.llAnemiaNoMedicationReason.setVisibility(View.VISIBLE);
            }

            binding.tvAnySurgeries.setText(getMedicalHistoryStrings(medicalHistory.getAnySurgeries(), updatedResources, resources, locale));

            if (medicalHistory.getAnySurgeries().equalsIgnoreCase("Yes")) {
                binding.tvReasonForSurgery.setText(medicalHistory.getReasonForSurgery());
                binding.llReasonForSurgery.setVisibility(View.VISIBLE);
            }
        }
    }
}

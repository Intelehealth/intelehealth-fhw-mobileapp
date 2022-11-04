package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getMedicalHistoryStrings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.activities.identificationActivity.callback.ViewPagerCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.MedicalHistory;
import org.intelehealth.ekalarogya.databinding.LayoutMedicalHistoryBinding;

import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.MedicalViewHolder> {

    private final List<MedicalHistory> medicalHistoryList;
    private final String locale;
    private final Context context;
    private final Context updatedContext;
    private final ViewPagerCallback callback;

    public MedicalHistoryAdapter(List<MedicalHistory> medicalHistoryList, String locale, Context context, Context updatedContext, ViewPagerCallback callback) {
        this.medicalHistoryList = medicalHistoryList;
        this.locale = locale;
        this.context = context;
        this.updatedContext = updatedContext;
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
        return new MedicalViewHolder(binding, context, locale, updatedContext, callback);
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
        public Context context;
        public String locale;
        public int position;
        public Context updatedContext;
        public ViewPagerCallback callback;
        public MedicalHistory medicalHistory;

        public MedicalViewHolder(
                @NonNull LayoutMedicalHistoryBinding binding,
                Context context,
                String locale,
                Context updatedContext,
                ViewPagerCallback callback
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
            this.locale = locale;
            this.updatedContext = updatedContext;
            binding.llMedicalHistory.setOnClickListener(v -> callback.getMedicalHistory(medicalHistory, position));
        }

        public void initData(MedicalHistory medicalHistory, int position) {
            this.medicalHistory = medicalHistory;
            this.position = position;

            binding.tvHypertension.setText(getMedicalHistoryStrings(medicalHistory.getHypertension(), updatedContext, context, locale));
            binding.tvDiabetes.setText(getMedicalHistoryStrings(medicalHistory.getDiabetes(), updatedContext, context, locale));
            binding.tvArthritis.setText(getMedicalHistoryStrings(medicalHistory.getArthritis(), updatedContext, context, locale));
            binding.tvAnaemia.setText(getMedicalHistoryStrings(medicalHistory.getAnaemia(), updatedContext, context, locale));
            binding.tvAnySurgeries.setText(getMedicalHistoryStrings(medicalHistory.getAnySurgeries(), updatedContext, context, locale));

            if (medicalHistory.getAnySurgeries().equalsIgnoreCase("Yes")) {
                binding.tvReasonForSurgery.setText(medicalHistory.getReasonForSurgery());
                binding.llReasonForSurgery.setVisibility(View.VISIBLE);
            }
        }
    }
}

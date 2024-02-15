package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getSmokingHistoryStrings;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.activities.identificationActivity.callback.ViewPagerCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.SmokingHistory;
import org.intelehealth.ekalarogya.databinding.LayoutSmokingHistoryBinding;

import java.util.List;

public class SmokingHistoryAdapter extends RecyclerView.Adapter<SmokingHistoryAdapter.SmokingViewHolder> {

    private final List<SmokingHistory> smokingHistoryList;
    private final String locale;
    private final Resources resources;
    private final Resources updatedResources;
    private final ViewPagerCallback callback;

    public SmokingHistoryAdapter(
            List<SmokingHistory> smokingHistoryList,
            String locale,
            Resources resources,
            Resources updatedResources,
            ViewPagerCallback callback
    ) {
        this.smokingHistoryList = smokingHistoryList;
        this.locale = locale;
        this.resources = resources;
        this.updatedResources = updatedResources;
        this.callback = callback;
    }

    @NonNull
    @Override
    public SmokingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutSmokingHistoryBinding binding = LayoutSmokingHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SmokingViewHolder(binding, resources, locale, updatedResources, callback);
    }

    @Override
    public void onBindViewHolder(@NonNull SmokingViewHolder holder, int position) {
        holder.initData(smokingHistoryList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return smokingHistoryList.size();
    }

    static class SmokingViewHolder extends RecyclerView.ViewHolder {
        public LayoutSmokingHistoryBinding binding;
        public Resources resources;
        public String locale;
        public int position;
        public Resources updatedResources;
        public ViewPagerCallback callback;
        public SmokingHistory smokingHistory;

        public SmokingViewHolder(
                @NonNull LayoutSmokingHistoryBinding binding,
                Resources resources,
                String locale,
                Resources updatedResources,
                ViewPagerCallback callback
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.locale = locale;
            this.resources = resources;
            this.updatedResources = updatedResources;
            binding.llSmokingHistory.setOnClickListener(v -> callback.getSmokingHistory(smokingHistory, position));
        }

        public void initData(SmokingHistory history, int position) {
            this.smokingHistory = history;
            this.position = position;

            binding.tvSmokingHistory.setText(getSmokingHistoryStrings(history.getSmokingStatus(), updatedResources, resources, locale));

            if (!history.getRateOfSmoking().equalsIgnoreCase("-") && !history.getDurationOfSmoking().equalsIgnoreCase("-")) {
                binding.tvSmokingRate.setText(getSmokingHistoryStrings(history.getRateOfSmoking(), updatedResources, resources, locale));
                binding.tvSmokingDuration.setText(getSmokingHistoryStrings(history.getDurationOfSmoking(), updatedResources, resources, locale));
                binding.tvSmokingFrequency.setText(getSmokingHistoryStrings(history.getFrequencyOfSmoking(), updatedResources, resources, locale));
                binding.llSmoking.setVisibility(View.VISIBLE);
            }
        }
    }
}

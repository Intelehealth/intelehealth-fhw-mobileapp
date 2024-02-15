package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getAlcoholHistory;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.activities.identificationActivity.callback.ViewPagerCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.databinding.LayoutAlcoholConsumptionHistoryBinding;

import java.util.List;

public class AlcoholConsumptionHistoryAdapter extends RecyclerView.Adapter<AlcoholConsumptionHistoryAdapter.ConsumptionHistoryViewHolder> {

    private final List<AlcoholConsumptionHistory> consumptionHistoryList;
    private final String locale;
    private final Resources resources;
    private final Resources updatedResources;
    private final ViewPagerCallback callback;

    public AlcoholConsumptionHistoryAdapter(
            List<AlcoholConsumptionHistory> consumptionHistoryList,
            String locale,
            Resources resources,
            Resources updatedResources,
            ViewPagerCallback callback
    ) {
        this.consumptionHistoryList = consumptionHistoryList;
        this.locale = locale;
        this.resources = resources;
        this.updatedResources = updatedResources;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ConsumptionHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutAlcoholConsumptionHistoryBinding binding = LayoutAlcoholConsumptionHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ConsumptionHistoryViewHolder(binding, resources, locale, updatedResources, callback);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsumptionHistoryViewHolder holder, int position) {
        holder.initData(consumptionHistoryList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return consumptionHistoryList.size();
    }

    static class ConsumptionHistoryViewHolder extends RecyclerView.ViewHolder {
        public LayoutAlcoholConsumptionHistoryBinding binding;
        public Resources resources;
        public String locale;
        public int position;
        public Resources updatedResources;
        public ViewPagerCallback callback;
        public AlcoholConsumptionHistory alcoholConsumptionHistory;

        public ConsumptionHistoryViewHolder(
                @NonNull LayoutAlcoholConsumptionHistoryBinding binding,
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
            binding.llAlcoholHistory.setOnClickListener(v -> callback.getAlcoholHistory(alcoholConsumptionHistory, position));
        }

        public void initData(AlcoholConsumptionHistory history, int position) {
            this.alcoholConsumptionHistory = history;
            this.position = position;

            binding.tvConsumptionHistory.setText(getAlcoholHistory(history.getHistoryOfAlcoholConsumption(), updatedResources, resources, locale));

            if (!history.getRateOfAlcoholConsumption().equalsIgnoreCase("-") && !history.getDurationOfAlcoholConsumption().equalsIgnoreCase("-")) {
                binding.tvConsumptionRate.setText(getAlcoholHistory(history.getRateOfAlcoholConsumption(), updatedResources, resources, locale));
                binding.tvConsumptionDuration.setText(getAlcoholHistory(history.getDurationOfAlcoholConsumption(), updatedResources, resources, locale));
                binding.tvAlcoholConsumptionFrequency.setText(getAlcoholHistory(history.getFrequencyOfAlcoholConsumption(), updatedResources, resources, locale));
                binding.llAlcoholConsumption.setVisibility(View.VISIBLE);
            }
        }
    }
}
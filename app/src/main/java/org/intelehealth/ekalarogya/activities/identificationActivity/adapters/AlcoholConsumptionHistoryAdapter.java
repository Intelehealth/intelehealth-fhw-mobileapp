package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.databinding.LayoutAlcoholConsumptionHistoryBinding;

import java.util.List;

public class AlcoholConsumptionHistoryAdapter extends RecyclerView.Adapter<AlcoholConsumptionHistoryAdapter.ConsumptionHistoryViewHolder> {

    private final List<AlcoholConsumptionHistory> consumptionHistoryList;
    private final String locale;
    private final Context context;

    public AlcoholConsumptionHistoryAdapter(
            List<AlcoholConsumptionHistory> consumptionHistoryList,
            String locale,
            Context context
    ) {
        this.consumptionHistoryList = consumptionHistoryList;
        this.locale = locale;
        this.context = context;
    }

    @NonNull
    @Override
    public ConsumptionHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutAlcoholConsumptionHistoryBinding binding = LayoutAlcoholConsumptionHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ConsumptionHistoryViewHolder(binding, context, locale);
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
        public Context context;
        public String locale;
        public int position;

        public ConsumptionHistoryViewHolder(
                @NonNull LayoutAlcoholConsumptionHistoryBinding binding,
                Context context,
                String locale
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.locale = locale;
            this.context = context;
        }

        public void initData(AlcoholConsumptionHistory history, int position) {
            binding.tvConsumptionHistory.setText(history.getHistoryOfAlcoholConsumption());

            if (!history.getRateOfAlcoholConsumption().equalsIgnoreCase("-") && !history.getDurationOfAlcoholConsumption().equalsIgnoreCase("-")) {
                binding.tvConsumptionRate.setText(history.getRateOfAlcoholConsumption());
                binding.tvConsumptionDuration.setText(history.getDurationOfAlcoholConsumption());
                binding.llAlcoholConsumption.setVisibility(View.VISIBLE);
            }
        }
    }
}
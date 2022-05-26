package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getAlcoholHistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.activities.identificationActivity.callback.ViewPagerCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.SmokingHistory;
import org.intelehealth.ekalarogya.databinding.LayoutAlcoholConsumptionHistoryBinding;

import java.util.List;

public class AlcoholConsumptionHistoryAdapter extends RecyclerView.Adapter<AlcoholConsumptionHistoryAdapter.ConsumptionHistoryViewHolder> {

    private final List<AlcoholConsumptionHistory> consumptionHistoryList;
    private final String locale;
    private final Context context;
    private final Context updatedContext;
    private final ViewPagerCallback callback;

    public AlcoholConsumptionHistoryAdapter(
            List<AlcoholConsumptionHistory> consumptionHistoryList,
            String locale,
            Context context,
            Context updatedContext,
            ViewPagerCallback callback
    ) {
        this.consumptionHistoryList = consumptionHistoryList;
        this.locale = locale;
        this.context = context;
        this.updatedContext = updatedContext;
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
        return new ConsumptionHistoryViewHolder(binding, context, locale, updatedContext, callback);
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
        public Context updatedContext;
        public ViewPagerCallback callback;
        public AlcoholConsumptionHistory alcoholConsumptionHistory;

        public ConsumptionHistoryViewHolder(
                @NonNull LayoutAlcoholConsumptionHistoryBinding binding,
                Context context,
                String locale,
                Context updatedContext,
                ViewPagerCallback callback
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.locale = locale;
            this.context = context;
            this.updatedContext = updatedContext;
            binding.llAlcoholHistory.setOnClickListener(v -> callback.getAlcoholHistory(alcoholConsumptionHistory, position));
        }

        public void initData(AlcoholConsumptionHistory history, int position) {
            this.alcoholConsumptionHistory = history;
            this.position = position;

            binding.tvConsumptionHistory.setText(getAlcoholHistory(history.getHistoryOfAlcoholConsumption(), updatedContext, context, locale));

            if (!history.getRateOfAlcoholConsumption().equalsIgnoreCase("-") && !history.getDurationOfAlcoholConsumption().equalsIgnoreCase("-")) {
                binding.tvConsumptionRate.setText(getAlcoholHistory(history.getRateOfAlcoholConsumption(), updatedContext, context, locale));
                binding.tvConsumptionDuration.setText(getAlcoholHistory(history.getDurationOfAlcoholConsumption(), updatedContext, context, locale));
                binding.llAlcoholConsumption.setVisibility(View.VISIBLE);
            }
        }
    }
}
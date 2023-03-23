package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getSmokingHistoryStrings;
import static org.intelehealth.ekalarogya.utilities.StringUtils.getTobaccoHistoryStrings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalarogya.activities.identificationActivity.callback.ViewPagerCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.TobaccoHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.TobaccoHistory;
import org.intelehealth.ekalarogya.databinding.LayoutTobaccoHistoryBinding;
import org.intelehealth.ekalarogya.databinding.LayoutTobaccoHistoryBinding;

import java.util.List;

public class TobaccoHistoryAdapter extends RecyclerView.Adapter<TobaccoHistoryAdapter.TobaccoViewHolder> {

    private final List<TobaccoHistory> tobaccoHistoryList;
    private final String locale;
    private final Context context;
    private final Context updatedContext;
    private final ViewPagerCallback callback;

    public TobaccoHistoryAdapter(
            List<TobaccoHistory> tobaccoHistoryList,
            String locale,
            Context context,
            Context updatedContext,
            ViewPagerCallback callback
    ) {
        this.tobaccoHistoryList = tobaccoHistoryList;
        this.locale = locale;
        this.context = context;
        this.updatedContext = updatedContext;
        this.callback = callback;
    }

    @NonNull
    @Override
    public TobaccoHistoryAdapter.TobaccoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutTobaccoHistoryBinding binding = LayoutTobaccoHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TobaccoHistoryAdapter.TobaccoViewHolder(binding, context, locale, updatedContext, callback);
    }

    @Override
    public void onBindViewHolder(@NonNull TobaccoHistoryAdapter.TobaccoViewHolder holder, int position) {
        holder.initData(tobaccoHistoryList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return tobaccoHistoryList.size();
    }

    static class TobaccoViewHolder extends RecyclerView.ViewHolder {
        public LayoutTobaccoHistoryBinding binding;
        public Context context;
        public String locale;
        public int position;
        public Context updatedContext;
        public ViewPagerCallback callback;
        public TobaccoHistory tobaccoHistory;

        public TobaccoViewHolder(
                @NonNull LayoutTobaccoHistoryBinding binding,
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
            binding.llTobaccoHistory.setOnClickListener(v -> callback.getTobaccoHistory(tobaccoHistory, position));
        }

        public void initData(TobaccoHistory history, int position) {
            this.tobaccoHistory = history;
            this.position = position;

            binding.tvTobaccoHistory.setText(getTobaccoHistoryStrings(history.getChewTobaccoStatus(), updatedContext, context, locale));

/*
            if (!history.getRateOfSmoking().equalsIgnoreCase("-") && !history.getDurationOfSmoking().equalsIgnoreCase("-")) {
                binding.tvSmokingRate.setText(getSmokingHistoryStrings(history.getRateOfSmoking(), updatedContext, context, locale));
                binding.tvSmokingDuration.setText(getSmokingHistoryStrings(history.getDurationOfSmoking(), updatedContext, context, locale));
                binding.tvSmokingFrequency.setText(getSmokingHistoryStrings(history.getFrequencyOfSmoking(), updatedContext, context, locale));
                binding.llSmoking.setVisibility(View.VISIBLE);
            }
*/
        }
    }
}

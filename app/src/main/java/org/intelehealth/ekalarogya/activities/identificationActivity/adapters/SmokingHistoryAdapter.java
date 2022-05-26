package org.intelehealth.ekalarogya.activities.identificationActivity.adapters;

import static org.intelehealth.ekalarogya.utilities.StringUtils.getSmokingHistoryStrings;

import android.content.Context;
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
    private final Context context;
    private final Context updatedContext;
    private final ViewPagerCallback callback;

    public SmokingHistoryAdapter(
            List<SmokingHistory> smokingHistoryList,
            String locale,
            Context context,
            Context updatedContext,
            ViewPagerCallback callback
    ) {
        this.smokingHistoryList = smokingHistoryList;
        this.locale = locale;
        this.context = context;
        this.updatedContext = updatedContext;
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
        return new SmokingViewHolder(binding, context, locale, updatedContext, callback);
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
        public Context context;
        public String locale;
        public int position;
        public Context updatedContext;
        public ViewPagerCallback callback;
        public SmokingHistory smokingHistory;

        public SmokingViewHolder(
                @NonNull LayoutSmokingHistoryBinding binding,
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
            binding.llSmokingHistory.setOnClickListener(v -> callback.getSmokingHistory(smokingHistory, position));
        }

        public void initData(SmokingHistory history, int position) {
            this.smokingHistory = history;
            this.position = position;

            binding.tvSmokingHistory.setText(getSmokingHistoryStrings(history.getSmokingStatus(), updatedContext, context, locale));

            if (!history.getRateOfSmoking().equalsIgnoreCase("-") && !history.getDurationOfSmoking().equalsIgnoreCase("-")) {
                binding.tvSmokingRate.setText(getSmokingHistoryStrings(history.getRateOfSmoking(), updatedContext, context, locale));
                binding.tvSmokingDuration.setText(getSmokingHistoryStrings(history.getDurationOfSmoking(), updatedContext, context, locale));
                binding.llSmoking.setVisibility(View.VISIBLE);
            }
        }
    }
}

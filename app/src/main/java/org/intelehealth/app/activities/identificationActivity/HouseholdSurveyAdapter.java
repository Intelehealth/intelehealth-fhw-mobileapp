package org.intelehealth.app.activities.identificationActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.LayoutDiseasePresentationBinding;
import org.intelehealth.app.utilities.StringUtils;

import java.util.List;

public class HouseholdSurveyAdapter extends RecyclerView.Adapter<HouseholdSurveyAdapter.SurveyViewHolder> {

    private final List<HealthIssues> healthIssuesList;
    private final ViewPagerCallback callback;
    private final String locale;
    private final Context context;

    public HouseholdSurveyAdapter(
            List<HealthIssues> healthIssuesList,
            ViewPagerCallback callback,
            String locale,
            Context context
    ) {
        this.healthIssuesList = healthIssuesList;
        this.callback = callback;
        this.locale = locale;
        this.context = context;
    }

    @NonNull
    @Override
    public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutDiseasePresentationBinding binding = LayoutDiseasePresentationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new SurveyViewHolder(binding, callback, context, locale);
    }

    @Override
    public void onBindViewHolder(@NonNull SurveyViewHolder holder, int position) {
//        holder.binding.nameOfHouseholdMemberValueTextView.setText(surveyPOJOList.get(position).getHouseholdMemberName());
//        holder.binding.healthIssueReportedValueTextView.setText(healthIssuesList.get(position).getHealthIssueReported());
//        holder.binding.numberOfIssuesEpisodesInTheLastYearValueTextView.setText(healthIssuesList.get(position).getNumberOfEpisodesInTheLastYear());
//        holder.binding.primaryHealthCareProviderValueTextView.setText(healthIssuesList.get(position).getPrimaryHealthcareProviderValue());
//        holder.binding.firstLocationOfVisitValueTextView.setText(healthIssuesList.get(position).getFirstLocationOfVisit());
//        holder.binding.referredToValueTextView.setText(healthIssuesList.get(position).getReferredTo());
//        holder.binding.modeOfTransportationValueTextView.setText(healthIssuesList.get(position).getModeOfTransportation());
//        holder.binding.averageCostIncurredOnTravelAndStayValueTextView.setText(healthIssuesList.get(position).getAverageCostOfTravelAndStayPerEpisode());
//        holder.binding.averageCostIncurredOnConsultationFeesValueTextView.setText(healthIssuesList.get(position).getAverageCostOfConsultation());
//        holder.binding.averageCostIncurredOnMedicinesValueTextView.setText(healthIssuesList.get(position).getAverageCostOfMedicine());
//        holder.binding.scoreOfExperienceValueTextView.setText(healthIssuesList.get(position).getScoreForExperienceOfTreatment());
        holder.initData(healthIssuesList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return healthIssuesList.size();
    }

    static class SurveyViewHolder extends RecyclerView.ViewHolder {
        public LayoutDiseasePresentationBinding binding;
        public HealthIssues healthIssues;
        public ViewPagerCallback callback;
        public Context context;
        public String locale;
        public int position;

        public SurveyViewHolder(
                @NonNull LayoutDiseasePresentationBinding binding,
                ViewPagerCallback callback,
                Context context,
                String locale
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.callback = callback;
            this.locale = locale;
            this.context = context;
            binding.linearLayout.setOnClickListener(v -> this.callback.getIssueClicked(healthIssues, position));
        }

        public void initData(HealthIssues issues, int position) {
            this.healthIssues = issues;
            this.position = position;

            Context updatedContext = context;
            if (locale.equalsIgnoreCase("mr")) {
                updatedContext = context.createConfigurationContext(StringUtils.getMarathiConfiguration());
            }

            if (issues.getHealthIssueReported().contains(updatedContext.getString(R.string.other))) {
                String value = StringUtils.getOtherStringEdit(issues.getHealthIssueReported())[1];
                binding.healthIssueReportedValueTextView.setText(value);
            } else {
                binding.healthIssueReportedValueTextView.setText(StringUtils.getHealthIssueReportedEdit(issues.getHealthIssueReported(), locale, context));
            }

            binding.numberOfIssuesEpisodesInTheLastYearValueTextView.setText(issues.getNumberOfEpisodesInTheLastYear());

            if (issues.getPrimaryHealthcareProviderValue().contains(updatedContext.getString(R.string.other_specify))) {
                String value = StringUtils.getOtherStringEdit(issues.getPrimaryHealthcareProviderValue())[1];
                binding.primaryHealthCareProviderValueTextView.setText(value);
            } else {
                binding.primaryHealthCareProviderValueTextView.setText(StringUtils.getPrimaryHealthcareProviderEdit(issues.getPrimaryHealthcareProviderValue(), locale, context));
            }

            if (issues.getFirstLocationOfVisit().contains(updatedContext.getString(R.string.other_specify))) {
                String value = StringUtils.getOtherStringEdit(issues.getFirstLocationOfVisit())[1];
                binding.firstLocationOfVisitValueTextView.setText(value);
            } else
                binding.firstLocationOfVisitValueTextView.setText(StringUtils.getFirstLocationOfVisitEdit(issues.getFirstLocationOfVisit(), locale, context));

            if (issues.getReferredTo().contains(updatedContext.getString(R.string.other_specify))) {
                String value = StringUtils.getOtherStringEdit(issues.getReferredTo())[1];
                binding.referredToValueTextView.setText(value);
            } else
                binding.referredToValueTextView.setText(StringUtils.getReferredToEdit(issues.getReferredTo(), locale, context));

            if (issues.getModeOfTransportation().contains(updatedContext.getString(R.string.other_specify))) {
                String value = StringUtils.getOtherStringEdit(issues.getModeOfTransportation())[1];
                binding.modeOfTransportationValueTextView.setText(value);
            } else
                binding.modeOfTransportationValueTextView.setText(StringUtils.getModeOfTransportationEdit(issues.getModeOfTransportation(), locale, context));

            binding.averageCostIncurredOnTravelAndStayValueTextView.setText(issues.getAverageCostOfTravelAndStayPerEpisode());
            binding.averageCostIncurredOnConsultationFeesValueTextView.setText(issues.getAverageCostOfConsultation());
            binding.averageCostIncurredOnMedicinesValueTextView.setText(issues.getAverageCostOfMedicine());
            binding.scoreOfExperienceValueTextView.setText(StringUtils.getScoreOfExperienceEdit(issues.getScoreForExperienceOfTreatment(), locale, context));
        }
    }
}
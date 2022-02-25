package org.intelehealth.app.activities.identificationActivity;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.databinding.LayoutDiseasePresentationBinding;

import java.util.List;

public class HouseholdSurveyAdapter extends RecyclerView.Adapter<HouseholdSurveyAdapter.SurveyViewHolder> {

    private final List<HealthIssues> healthIssuesList;
    private final ViewPagerCallback callback;

    public HouseholdSurveyAdapter(List<HealthIssues> healthIssuesList, ViewPagerCallback callback) {
        this.healthIssuesList = healthIssuesList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutDiseasePresentationBinding binding = LayoutDiseasePresentationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new SurveyViewHolder(binding, callback);
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
        public int position;

        public SurveyViewHolder(@NonNull LayoutDiseasePresentationBinding binding, ViewPagerCallback viewPagerCallback) {
            super(binding.getRoot());
            this.binding = binding;
            this.callback = viewPagerCallback;
            binding.linearLayout.setOnClickListener(v -> this.callback.getIssueClicked(healthIssues, position));
        }

        public void initData(HealthIssues issues, int position) {
            this.healthIssues = issues;
            this.position = position;
            binding.healthIssueReportedValueTextView.setText(issues.getHealthIssueReported());
            binding.numberOfIssuesEpisodesInTheLastYearValueTextView.setText(issues.getNumberOfEpisodesInTheLastYear());
            binding.primaryHealthCareProviderValueTextView.setText(issues.getPrimaryHealthcareProviderValue());
            binding.firstLocationOfVisitValueTextView.setText(issues.getFirstLocationOfVisit());
            binding.referredToValueTextView.setText(issues.getReferredTo());
            binding.modeOfTransportationValueTextView.setText(issues.getModeOfTransportation());
            binding.averageCostIncurredOnTravelAndStayValueTextView.setText(issues.getAverageCostOfTravelAndStayPerEpisode());
            binding.averageCostIncurredOnConsultationFeesValueTextView.setText(issues.getAverageCostOfConsultation());
            binding.averageCostIncurredOnMedicinesValueTextView.setText(issues.getAverageCostOfMedicine());
            binding.scoreOfExperienceValueTextView.setText(issues.getScoreForExperienceOfTreatment());
        }
    }
}

package org.intelehealth.app.activities.identificationActivity;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.databinding.LayoutDiseasePresentationBinding;

import java.util.List;

public class HouseholdSurveyAdapter extends RecyclerView.Adapter<HouseholdSurveyAdapter.SurveyViewHolder> {

    private final List<HealthIssues> healthIssuesList;

    public HouseholdSurveyAdapter(List<HealthIssues> healthIssuesList) {
        this.healthIssuesList = healthIssuesList;
    }

    @NonNull
    @Override
    public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutDiseasePresentationBinding binding = LayoutDiseasePresentationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new SurveyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SurveyViewHolder holder, int position) {
//        holder.binding.nameOfHouseholdMemberValueTextView.setText(surveyPOJOList.get(position).getHouseholdMemberName());
        holder.binding.healthIssueReportedValueTextView.setText(healthIssuesList.get(position).getHealthIssueReported());
        holder.binding.numberOfIssuesEpisodesInTheLastYearValueTextView.setText(healthIssuesList.get(position).getNumberOfEpisodesInTheLastYear());
        holder.binding.primaryHealthCareProviderValueTextView.setText(healthIssuesList.get(position).getPrimaryHealthcareProviderValue());
        holder.binding.firstLocationOfVisitValueTextView.setText(healthIssuesList.get(position).getFirstLocationOfVisit());
        holder.binding.referredToValueTextView.setText(healthIssuesList.get(position).getReferredTo());
        holder.binding.modeOfTransportationValueTextView.setText(healthIssuesList.get(position).getModeOfTransportation());
        holder.binding.averageCostIncurredOnTravelAndStayValueTextView.setText(healthIssuesList.get(position).getAverageCostOfTravelAndStayPerEpisode());
        holder.binding.averageCostIncurredOnConsultationFeesValueTextView.setText(healthIssuesList.get(position).getAverageCostOfConsultation());
        holder.binding.averageCostIncurredOnMedicinesValueTextView.setText(healthIssuesList.get(position).getAverageCostOfMedicine());
        holder.binding.scoreOfExperienceValueTextView.setText(healthIssuesList.get(position).getScoreForExperienceOfTreatment());
    }

    @Override
    public int getItemCount() {
        return healthIssuesList.size();
    }

    static class SurveyViewHolder extends RecyclerView.ViewHolder {
        public LayoutDiseasePresentationBinding binding;

        public SurveyViewHolder(@NonNull LayoutDiseasePresentationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

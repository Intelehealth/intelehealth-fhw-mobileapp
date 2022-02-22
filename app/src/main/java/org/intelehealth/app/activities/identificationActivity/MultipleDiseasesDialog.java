package org.intelehealth.app.activities.identificationActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.LayoutDiseaseBinding;

import java.util.Objects;

public class MultipleDiseasesDialog extends DialogFragment {

    public static final String TAG = "MultipleDiseasesDialog";
    private LayoutDiseaseBinding binding;
    private SurveyCallback callback;
    private Bundle bundle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (SurveyCallback) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = LayoutDiseaseBinding.inflate(inflater);

        if (bundle != null) {
            setBundleData();
        }

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    HealthIssues survey = fetchSurveyData();
                    if (bundle != null) {
                        callback.saveSurveyDataAtPosition(survey, bundle.getInt("position"));
                    } else {
                        callback.saveSurveyData(survey);
                    }
                })
                .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.dismiss()));

        setMenus();
        setListeners();
        return builder.create();
    }


    private void setMenus() {
        binding.healthIssueValueTextView.setOnClickListener(v -> showMenu(v, R.menu.health_issue_menu));
        binding.primaryHealthCareProviderValueEditText.setOnClickListener(v -> showMenu(v, R.menu.primary_healthcare_provider));
        binding.firstLocationOfVisitValueEditText.setOnClickListener(v -> showMenu(v, R.menu.first_location_of_visit));
        binding.referredToEditText.setOnClickListener(v -> showMenu(v, R.menu.referred_to_menu));
        binding.modeOfTransportationEditText.setOnClickListener(v -> showMenu(v, R.menu.mode_of_transportation));
        binding.scoreOfExperienceEditText.setOnClickListener(v -> showMenu(v, R.menu.score_of_experience));
    }

    private void setListeners() {
        binding.healthIssueValueTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(getString(R.string.other))) {
                    binding.otherHealthIssueLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.otherHealthIssueLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.primaryHealthCareProviderValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(getString(R.string.other_specify))) {
                    binding.otherPrimaryHealthCareProviderLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.otherPrimaryHealthCareProviderLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.firstLocationOfVisitValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(getString(R.string.other_specify))) {
                    binding.otherFirstLocationOfVisitLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.otherFirstLocationOfVisitLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.referredToEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(getString(R.string.other_specify))) {
                    binding.otherReferredToLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.otherReferredToLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.modeOfTransportationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(getString(R.string.other_specify))) {
                    binding.otherModeOfTransportationLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.otherModeOfTransportationLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private HealthIssues fetchSurveyData() {
        HealthIssues survey = new HealthIssues();

//        String householdMemberName = binding.nameOfHouseholdMemberValueTextView.getText().toString();
        String healthIssueReported = Objects.requireNonNull(binding.healthIssueValueTextView.getText()).toString();
        String numberOfEpisodesInTheLastYear = Objects.requireNonNull(binding.numberOfEpisodesValueTextView.getText()).toString();
        String primaryHealthcareProviderValue = Objects.requireNonNull(binding.primaryHealthCareProviderValueEditText.getText()).toString();
        String firstLocationOfVisit = Objects.requireNonNull(binding.firstLocationOfVisitValueEditText.getText()).toString();
        String referredTo = Objects.requireNonNull(binding.referredToEditText.getText()).toString();
        String modeOfTransportation = Objects.requireNonNull(binding.modeOfTransportationEditText.getText()).toString();
        String averageCostOfTravelAndStayPerEpisode = Objects.requireNonNull(binding.averageCostIncurredOnTravelAndStayTextView.getText()).toString();
        String averageCostOfConsultation = Objects.requireNonNull(binding.averageCostIncurredOnConsultationFeesTextView.getText()).toString();
        String averageCostOfMedicine = Objects.requireNonNull(binding.averageCostIncurredOnMedicinesTextView.getText()).toString();
        String scoreForExperienceOfTreatment = Objects.requireNonNull(binding.scoreOfExperienceEditText.getText()).toString();

        if (healthIssueReported.equals(getString(R.string.other)))
            healthIssueReported = Objects.requireNonNull(binding.otherHealthIssueTextView.getText()).toString();
        if (primaryHealthcareProviderValue.equals(getString(R.string.other_specify)))
            primaryHealthcareProviderValue = Objects.requireNonNull(binding.otherPrimaryHealthCareProviderTextView.getText()).toString();
        if (firstLocationOfVisit.equals(getString(R.string.other_specify)))
            firstLocationOfVisit = Objects.requireNonNull(binding.otherFirstLocationOfVisitTextView.getText()).toString();
        if (referredTo.equals(getString(R.string.other_specify)))
            referredTo = Objects.requireNonNull(binding.otherReferredToEditText.getText()).toString();
        if (modeOfTransportation.equals(getString(R.string.other_specify)))
            modeOfTransportation = Objects.requireNonNull(binding.otherModeOfTransportationTextView.getText()).toString();

//        survey.setHouseholdMemberName(householdMemberName);
        survey.setHealthIssueReported(healthIssueReported);
        survey.setNumberOfEpisodesInTheLastYear(numberOfEpisodesInTheLastYear);
        survey.setPrimaryHealthcareProviderValue(primaryHealthcareProviderValue);
        survey.setFirstLocationOfVisit(firstLocationOfVisit);
        survey.setReferredTo(referredTo);
        survey.setModeOfTransportation(modeOfTransportation);
        survey.setAverageCostOfTravelAndStayPerEpisode(averageCostOfTravelAndStayPerEpisode);
        survey.setAverageCostOfConsultation(averageCostOfConsultation);
        survey.setAverageCostOfMedicine(averageCostOfMedicine);
        survey.setScoreForExperienceOfTreatment(scoreForExperienceOfTreatment);

        return survey;
    }

    private void showMenu(View view, @MenuRes Integer menuRes) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(menuRes, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            TextInputEditText editText = (TextInputEditText) view;
            editText.setText(item.getTitle());
            return true;
        });
        popup.show();
    }

    private void setBundleData() {
//        binding.nameOfHouseholdMemberValueTextView.setText(bundle.getString("householdMemberName"));
        binding.healthIssueValueTextView.setText(bundle.getString("healthIssueReported"));
        binding.numberOfEpisodesValueTextView.setText(bundle.getString("numberOfEpisodesInTheLastYear"));
        binding.primaryHealthCareProviderValueEditText.setText(bundle.getString("primaryHealthcareProviderValue"));
        binding.firstLocationOfVisitValueEditText.setText(bundle.getString("firstLocationOfVisit"));
        binding.referredToEditText.setText(bundle.getString("referredTo"));
        binding.modeOfTransportationEditText.setText(bundle.getString("modeOfTransportation"));
        binding.averageCostIncurredOnTravelAndStayTextView.setText(bundle.getString("averageCostOfTravelAndStayPerEpisode"));
        binding.averageCostIncurredOnConsultationFeesTextView.setText(bundle.getString("averageCostOfConsultation"));
        binding.averageCostIncurredOnMedicinesTextView.setText(bundle.getString("averageCostOfMedicine"));
        binding.scoreOfExperienceEditText.setText(bundle.getString("scoreForExperienceOfTreatment"));
    }
}

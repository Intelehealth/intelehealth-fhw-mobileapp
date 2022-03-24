package org.intelehealth.app.activities.identificationActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.R;
import org.intelehealth.app.databinding.LayoutDiseaseBinding;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.StringUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MultipleDiseasesDialog extends DialogFragment {

    public static final String TAG = "MultipleDiseasesDialog";
    private LayoutDiseaseBinding binding;
    private SurveyCallback callback;
    private String appLanguage;
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
        appLanguage = ((IdentificationActivity) Objects.requireNonNull(getActivity())).sessionManager.getAppLanguage();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AtomicReference<HealthIssuesValidationState> state = new AtomicReference<>(new HealthIssuesValidationState());
        binding = LayoutDiseaseBinding.inflate(inflater);

        if (bundle != null) {
            setBundleData();
        }

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })
                .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.dismiss()));

        setMenus();
        setListeners();

        // Overridden setOnShowListener to dismiss the dialog only when all user data is entered
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                HealthIssues survey = fetchSurveyData();
                state.set(validateData(survey));

                if (state.get().getAreDetailsCorrect()) {
                    if (bundle != null) {
                        callback.saveSurveyDataAtPosition(survey, bundle.getInt("position"));
                    } else {
                        callback.saveSurveyData(survey);
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), state.get().getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
    }

    private HealthIssuesValidationState validateData(HealthIssues survey) {
        HealthIssuesValidationState state = new HealthIssuesValidationState();
        state.setCorrect(true);
        String errorString = "  " + getResources().getString(R.string.error_field_required);
//        String errorString = " field cannot be empty";

        if (survey.getHealthIssueReported() == null || survey.getHealthIssueReported().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.health_issue_reported) + errorString);
            return state;
        }

        if (survey.getNumberOfEpisodesInTheLastYear() == null || survey.getNumberOfEpisodesInTheLastYear().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.number_of_episodes_in_the_last_year) + errorString);
            return state;
        }

        if (survey.getPrimaryHealthcareProviderValue() == null || survey.getPrimaryHealthcareProviderValue().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.primary_health_care_provider) + errorString);
            return state;
        }

        if (survey.getFirstLocationOfVisit() == null || survey.getFirstLocationOfVisit().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.first_location_of_visit) + errorString);
            return state;
        }

        if (survey.getReferredTo() == null || survey.getReferredTo().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.referred_to) + errorString);
            return state;
        }

        if (survey.getModeOfTransportation() == null || survey.getModeOfTransportation().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.mode_of_transportations_used_to_reach_facility_provider));
            return state;
        }

        if (survey.getAverageCostOfTravelAndStayPerEpisode() == null || survey.getAverageCostOfTravelAndStayPerEpisode().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.average_cost_incurred_on_travel_and_stay_per_episode) + errorString);
            return state;
        }

        if (survey.getAverageCostOfConsultation() == null || survey.getAverageCostOfConsultation().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.average_cost_incurred_on_consultation_fees_per_episode) + errorString);

            return state;
        }

        if (survey.getAverageCostOfMedicine() == null || survey.getAverageCostOfMedicine().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.average_cost_incurred_on_medicines_per_episode) + errorString);
            return state;
        }

        if (survey.getScoreForExperienceOfTreatment() == null || survey.getScoreForExperienceOfTreatment().isEmpty()) {
            state.setCorrect(false);
            state.setErrorMessage(getString(R.string.score_for_experience_of_treatment) + errorString);
            return state;
        }

        return state;
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

        Context updatedContext = requireContext();
        if (appLanguage.equalsIgnoreCase("mr")) {
            updatedContext = updatedContext.createConfigurationContext(StringUtils.getEnglishConfiguration());
        }

//        String householdMemberName = binding.nameOfHouseholdMemberValueTextView.getText().toString();

        String healthIssueReported = StringUtils.getHealthIssueReported(Objects.requireNonNull(binding.healthIssueValueTextView.getText()).toString(), appLanguage, getContext());
        String numberOfEpisodesInTheLastYear = Objects.requireNonNull(binding.numberOfEpisodesValueTextView.getText()).toString();
        String primaryHealthcareProviderValue = StringUtils.getPrimaryHealthcareProvider(Objects.requireNonNull(binding.primaryHealthCareProviderValueEditText.getText()).toString(), appLanguage, getContext());
        String firstLocationOfVisit = StringUtils.getFirstLocationOfVisit(Objects.requireNonNull(binding.firstLocationOfVisitValueEditText.getText()).toString(), appLanguage, getContext());
        String referredTo = StringUtils.getReferredTo(Objects.requireNonNull(binding.referredToEditText.getText()).toString(), appLanguage, getContext());
        String modeOfTransportation = StringUtils.getModeOfTransportation(Objects.requireNonNull(binding.modeOfTransportationEditText.getText()).toString(), appLanguage, getContext());
        String averageCostOfTravelAndStayPerEpisode = Objects.requireNonNull(binding.averageCostIncurredOnTravelAndStayTextView.getText()).toString();
        String averageCostOfConsultation = Objects.requireNonNull(binding.averageCostIncurredOnConsultationFeesTextView.getText()).toString();
        String averageCostOfMedicine = Objects.requireNonNull(binding.averageCostIncurredOnMedicinesTextView.getText()).toString();
        String scoreForExperienceOfTreatment = StringUtils.getScoreOfExperience((Objects.requireNonNull(binding.scoreOfExperienceEditText.getText()).toString()), appLanguage, getContext());

        if (healthIssueReported.equals(updatedContext.getString(R.string.other)))
            healthIssueReported = StringUtils.getOtherString(updatedContext.getString(R.string.other), Objects.requireNonNull(binding.otherHealthIssueTextView.getText()).toString());

        if (primaryHealthcareProviderValue.equals(updatedContext.getString(R.string.other_specify)))
            primaryHealthcareProviderValue = StringUtils.getOtherString(updatedContext.getString(R.string.other_specify), Objects.requireNonNull(binding.otherPrimaryHealthCareProviderTextView.getText()).toString());

        if (firstLocationOfVisit.equals(updatedContext.getString(R.string.other_specify)))
            firstLocationOfVisit = StringUtils.getOtherString(updatedContext.getString(R.string.other_specify), Objects.requireNonNull(binding.otherFirstLocationOfVisitTextView.getText()).toString());

        if (referredTo.equals(updatedContext.getString(R.string.other_specify)))
            referredTo = StringUtils.getOtherString(updatedContext.getString(R.string.other_specify), Objects.requireNonNull(binding.otherReferredToEditText.getText()).toString());

        if (modeOfTransportation.equals(updatedContext.getString(R.string.other_specify)))
            modeOfTransportation = StringUtils.getOtherString(updatedContext.getString(R.string.other_specify), Objects.requireNonNull(binding.otherModeOfTransportationTextView.getText()).toString());

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

        Context updatedContext = requireContext();
        if (appLanguage.equalsIgnoreCase("mr")) {
            updatedContext = updatedContext.createConfigurationContext(StringUtils.getEnglishConfiguration());
        }

        if (bundle.getString("healthIssueReported").contains(updatedContext.getString(R.string.other))) {
            String[] otherArray = StringUtils.getOtherStringEdit(bundle.getString("healthIssueReported"));
            String other = otherArray[0];
            String otherValue = otherArray[1];
            binding.healthIssueValueTextView.setText(StringUtils.getHealthIssueReportedEdit(other, appLanguage, getContext()));
            binding.otherHealthIssueTextView.setText(otherValue);
            binding.otherHealthIssueLayout.setVisibility(View.VISIBLE);
        } else {
            binding.healthIssueValueTextView.setText(StringUtils.getHealthIssueReportedEdit(bundle.getString("healthIssueReported"), appLanguage, getContext()));
        }

        binding.numberOfEpisodesValueTextView.setText(bundle.getString("numberOfEpisodesInTheLastYear"));

        if (bundle.getString("primaryHealthcareProviderValue").contains(updatedContext.getString(R.string.other_specify))) {
            String[] otherArray = StringUtils.getOtherStringEdit(bundle.getString("primaryHealthcareProviderValue"));
            String other = otherArray[0];
            String otherValue = otherArray[1];
            binding.primaryHealthCareProviderValueEditText.setText(StringUtils.getPrimaryHealthcareProviderEdit(other, appLanguage, getContext()));
            binding.otherPrimaryHealthCareProviderTextView.setText(otherValue);
            binding.otherPrimaryHealthCareProviderLayout.setVisibility(View.VISIBLE);
        } else {
            binding.primaryHealthCareProviderValueEditText.setText(StringUtils.getPrimaryHealthcareProviderEdit(bundle.getString("primaryHealthcareProviderValue"), appLanguage, getContext()));
        }

        if (bundle.getString("firstLocationOfVisit").contains(updatedContext.getString(R.string.other_specify))) {
            String[] otherArray = StringUtils.getOtherStringEdit(bundle.getString("firstLocationOfVisit"));
            String other = otherArray[0];
            String otherValue = otherArray[1];
            binding.firstLocationOfVisitValueEditText.setText(StringUtils.getFirstLocationOfVisitEdit(other, appLanguage, getContext()));
            binding.otherFirstLocationOfVisitTextView.setText(otherValue);
            binding.otherFirstLocationOfVisitLayout.setVisibility(View.VISIBLE);
        } else
            binding.firstLocationOfVisitValueEditText.setText(StringUtils.getFirstLocationOfVisitEdit(bundle.getString("firstLocationOfVisit"), appLanguage, getContext()));

        if (bundle.getString("referredTo").contains(updatedContext.getString(R.string.other_specify))) {
            String[] otherArray = StringUtils.getOtherStringEdit(bundle.getString("referredTo"));
            String other = otherArray[0];
            String otherValue = otherArray[1];
            binding.referredToEditText.setText(StringUtils.getReferredToEdit(other, appLanguage, getContext()));
            binding.otherReferredToEditText.setText(otherValue);
            binding.otherReferredToLayout.setVisibility(View.VISIBLE);
        } else
            binding.referredToEditText.setText(StringUtils.getReferredToEdit(bundle.getString("referredTo"), appLanguage, getContext()));

        if (bundle.getString("modeOfTransportation").contains(updatedContext.getString(R.string.other_specify))) {
            String[] otherArray = StringUtils.getOtherStringEdit(bundle.getString("modeOfTransportation"));
            String other = otherArray[0];
            String otherValue = otherArray[1];
            binding.modeOfTransportationEditText.setText(StringUtils.getModeOfTransportationEdit(other, appLanguage, getContext()));
            binding.otherModeOfTransportationTextView.setText(otherValue);
            binding.otherModeOfTransportationLayout.setVisibility(View.VISIBLE);
        } else
            binding.modeOfTransportationEditText.setText(StringUtils.getModeOfTransportationEdit(bundle.getString("modeOfTransportation"), appLanguage, getContext()));

        binding.averageCostIncurredOnTravelAndStayTextView.setText(bundle.getString("averageCostOfTravelAndStayPerEpisode"));
        binding.averageCostIncurredOnConsultationFeesTextView.setText(bundle.getString("averageCostOfConsultation"));
        binding.averageCostIncurredOnMedicinesTextView.setText(bundle.getString("averageCostOfMedicine"));
        binding.scoreOfExperienceEditText.setText(StringUtils.getScoreOfExperienceEdit(bundle.getString("scoreForExperienceOfTreatment"), appLanguage, getContext()));
    }
}

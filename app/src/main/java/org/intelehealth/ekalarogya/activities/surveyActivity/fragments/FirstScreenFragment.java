package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import static org.intelehealth.ekalarogya.utilities.StringUtils.checkIfCheckboxesEmpty;
import static org.intelehealth.ekalarogya.utilities.StringUtils.checkIfEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.databinding.FragmentFirstScreenBinding;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirstScreenFragment extends Fragment {

    private FragmentFirstScreenBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setListeners() {
        binding.otherIncomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherSourcesOfIncomeLayout.setVisibility(View.VISIBLE);
            else
                binding.otherSourcesOfIncomeLayout.setVisibility(View.GONE);
        });

        binding.householdElectricityRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.householdElectricityYes.getId())
                binding.llLoadShedding.setVisibility(View.VISIBLE);
            else
                binding.llLoadShedding.setVisibility(View.GONE);
        });

        binding.householdRunningWaterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.waterSupplyYes.getId())
                binding.runningWaterAvailabilityLinearLayout.setVisibility(View.VISIBLE);
            else
                binding.runningWaterAvailabilityLinearLayout.setVisibility(View.GONE);
        });

        binding.nextButtonLinearLayout.setOnClickListener(v -> {
            if (!areFieldsValid()) {
                Toast.makeText(requireContext(), "Please fill up all the required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                insertData();
            } catch (DAOException exception) {
                exception.printStackTrace();
            }
        });
    }

    private boolean areFieldsValid() {
        AtomicBoolean validations = new AtomicBoolean(true);

        // Validation for household structure RadioGroup
        if (binding.householdStructureRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validation for head of household EditText
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.nameOfHeadOfHouseholdEditText.getText()).toString()))
            validations.set(false);

        // Validation for religion spinner
        if (checkIfEmpty(requireContext(), binding.religionDropDown.getSelectedItem().toString()))
            validations.set(false);

        // Validation for caste spinner
        if (checkIfEmpty(requireContext(), binding.casteDropDown.getSelectedItem().toString()))
            validations.set(false);

        // Validation for number of smartphones field
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.numberOfSmartphonesEditText.getText()).toString()))
            validations.set(false);

        // Validation for number of feature phones field
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.numberOfFeaturePhonesEditText.getText()).toString()))
            validations.set(false);

        // Validation for number of earning members field
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.noOfEarningMembersEditText.getText()).toString()))
            validations.set(false);

        // Validation for number of smartphones field
        if (checkIfCheckboxesEmpty(binding.primarySourceOfIncomeCheckboxLinearLayout))
            validations.set(false);

        // Validation for Other field
        if (binding.otherIncomeCheckbox.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.otherSourcesOfIncomeEditText.getText()).toString()))
            validations.set(false);

        // Validations for Household Electricity Group
        if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validations for Load Shedding Hours EditText
        if (binding.householdElectricityYes.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.loadSheddingHoursEditText.getText()).toString()))
            validations.set(false);

        // Validations for Load Shedding Days EditText
        if (binding.householdElectricityYes.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.loadSheddingDaysEditText.getText()).toString()))
            validations.set(false);

        // Validations for Household Toilet Radio Group
        if (binding.householdToiletRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validations for Running Water Hours Edit Text
        if (binding.householdRunningWaterRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validations for Running Water Days Edit Text
        if (binding.waterSupplyYes.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.runningWaterHoursEditText.getText()).toString()))
            validations.set(false);

        // Validations for Cultivable Radio Group
        if (binding.cultivableLandRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validations for Units Radio Group
        if (binding.unitsRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validations for Income Radio Group
        if (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validations for Annual Health Expenditure Radio Group
        if (binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        // Validations for Education Expenditure Radio Group
        if (binding.educationExpenditureRadioGroup.getCheckedRadioButtonId() == -1)
            validations.set(false);

        return validations.get();
    }

    private void insertData() throws DAOException {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_container, new SecondScreenFragment())
                .addToBackStack(null)
                .commit();
    }
}
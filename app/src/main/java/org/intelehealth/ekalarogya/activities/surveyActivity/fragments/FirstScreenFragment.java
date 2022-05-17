package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import static org.intelehealth.ekalarogya.activities.surveyActivity.SurveyActivity.patientAttributesDTOList;
import static org.intelehealth.ekalarogya.utilities.StringUtils.checkIfCheckboxesEmpty;
import static org.intelehealth.ekalarogya.utilities.StringUtils.checkIfEmpty;
import static org.intelehealth.ekalarogya.utilities.StringUtils.getIndex;
import static org.intelehealth.ekalarogya.utilities.StringUtils.setSelectedCheckboxes;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.databinding.FragmentFirstScreenBinding;
import org.intelehealth.ekalarogya.models.dto.PatientAttributesDTO;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.StringUtils;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirstScreenFragment extends Fragment {

    private FragmentFirstScreenBinding binding;
    private String patientUuid;
    private final PatientsDAO patientsDAO = new PatientsDAO();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = requireActivity().getIntent();
        if (intent != null)
            patientUuid = intent.getStringExtra("patientUuid");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstScreenBinding.inflate(inflater, container, false);
        setData(patientUuid);
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
        if (binding.householdStructureRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for head of household EditText
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.nameOfHeadOfHouseholdEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for religion spinner
        if (checkIfEmpty(requireContext(), binding.religionDropDown.getSelectedItem().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for caste spinner
        if (checkIfEmpty(requireContext(), binding.casteDropDown.getSelectedItem().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for number of smartphones field
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.numberOfSmartphonesEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for number of feature phones field
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.numberOfFeaturePhonesEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for number of earning members field
        if (checkIfEmpty(requireContext(), Objects.requireNonNull(binding.noOfEarningMembersEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Primary Source of Income Checkbox Layout
        if (checkIfCheckboxesEmpty(binding.primarySourceOfIncomeCheckboxLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Other Income field
        if (binding.otherIncomeCheckbox.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.otherSourcesOfIncomeEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Household Electricity Group
        if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Load Shedding Hours EditText
        if (binding.householdElectricityYes.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.loadSheddingHoursEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Load Shedding Days EditText
        if (binding.householdElectricityYes.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.loadSheddingDaysEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Household Toilet Radio Group
        if (binding.householdToiletRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Running Water Hours Edit Text
        if (binding.householdRunningWaterRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Running Water Days Edit Text
        if (binding.waterSupplyYes.isChecked() && checkIfEmpty(requireContext(), Objects.requireNonNull(binding.runningWaterHoursEditText.getText()).toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Cultivable Radio Group
        if (binding.cultivableLandRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Units Radio Group
        if (binding.unitsRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Income Radio Group
        if (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Annual Health Expenditure Radio Group
        if (binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Education Expenditure Radio Group
        if (binding.educationExpenditureRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        return validations.get();
    }

    private void insertData() throws DAOException {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO;

        // householdStructureType
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdStructureType"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.householdStructureRadioGroup.findViewById(binding.householdStructureRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // nameOfHeadOfHousehold
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nameOfHeadOfHousehold"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.nameOfHeadOfHouseholdEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // religion
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("religion"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.religionDropDown.getSelectedItem().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // caste
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.casteDropDown.getSelectedItem().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // numberOfSmartphones
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfSmartphones"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.numberOfSmartphonesEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // numberOfFeaturePhones
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfFeaturePhones"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.numberOfFeaturePhonesEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // numberOfEarningMembers
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfEarningMembers"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.noOfEarningMembersEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // primarySourceOfIncome
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("primarySourceOfIncome"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.primarySourceOfIncomeCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // electricityStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("electricityStatus"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.householdElectricityRadioGroup.findViewById(binding.householdElectricityRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // loadSheddingHoursPerDay
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingHoursPerDay"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.loadSheddingHoursEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // loadSheddingDaysPerWeek
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingDaysPerWeek"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.loadSheddingDaysEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // householdToiletStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdToiletStatus"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.householdToiletRadioGroup.findViewById(binding.householdToiletRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // runningWaterAvailability
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("runningWaterAvailability"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.householdToiletRadioGroup.findViewById(binding.householdToiletRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // waterSupplyAvailabilityHoursPerDay
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHoursPerDay"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.runningWaterHoursEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // waterSupplyAvailabilityDaysPerWeek
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysPerWeek"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.runningWaterDaysEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // cultivableLandOwned
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("cultivableLandOwned"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.cultivableLandRadioGroup.findViewById(binding.cultivableLandRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // unitsOfCultivableLand
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("unitsOfCultivableLand"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.unitsRadioGroup.findViewById(binding.unitsRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // averageAnnualHouseholdIncome
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageAnnualHouseholdIncome"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.averageAnnualHouseholdIncomeRadioGroup.findViewById(binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // averageExpenditureOnHealth
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnHealth"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.annualHealthExpenditureRadioGroup.findViewById(binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // averageExpenditureOnEducation
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnEducation"));
        patientAttributesDTO.setValue(StringUtils.getSurveyValue(
                ((RadioButton) binding.educationExpenditureRadioGroup.findViewById(binding.educationExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        boolean isPatientUpdated = patientsDAO.surveyUpdatePatientToDB(patientUuid, patientAttributesDTOList);
        Logger.logD("TAG", String.valueOf(isPatientUpdated));

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_container, new SecondScreenFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setData(String patientUuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";

        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                // householdStructureType
                if (name.equalsIgnoreCase("householdStructureType")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdStructureRadioGroup, value1);
                    }
                }

                // nameOfHeadOfHousehold
                if (name.equalsIgnoreCase("nameOfHeadOfHousehold")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.nameOfHeadOfHouseholdEditText.setText(value1);
                    }
                }

                // religion
                if (name.equalsIgnoreCase("religion")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        int position = getIndex(binding.religionDropDown, value1);
                        binding.religionDropDown.setSelection(position);
                    }
                }

                // caste
                if (name.equalsIgnoreCase("caste")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        int position = getIndex(binding.casteDropDown, value1);
                        binding.casteDropDown.setSelection(position);
                    }
                }

                // numberOfSmartphones
                if (name.equalsIgnoreCase("numberOfSmartphones")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.numberOfSmartphonesEditText.setText(value1);
                    }
                }

                // numberOfFeaturePhones
                if (name.equalsIgnoreCase("numberOfFeaturePhones")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.numberOfFeaturePhonesEditText.setText(value1);
                    }
                }

                // numberOfEarningMembers
                if (name.equalsIgnoreCase("numberOfEarningMembers")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.noOfEarningMembersEditText.setText(value1);
                    }
                }

                // primarySourceOfIncome
                if (name.equalsIgnoreCase("primarySourceOfIncome")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    setSelectedCheckboxes(binding.primarySourceOfIncomeCheckboxLinearLayout, value1);
                }

                // electricityStatus
                if (name.equalsIgnoreCase("electricityStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdElectricityRadioGroup, value1);
                    }
                }

                // loadSheddingHoursPerDay
                if (name.equalsIgnoreCase("loadSheddingHoursPerDay")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.loadSheddingHoursEditText.setText(value1);
                    }
                }

                // loadSheddingDaysPerWeek
                if (name.equalsIgnoreCase("loadSheddingDaysPerWeek")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.loadSheddingDaysEditText.setText(value1);
                    }
                }

                // householdToiletStatus
                if (name.equalsIgnoreCase("householdToiletStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdToiletRadioGroup, value1);
                    }
                }

                // runningWaterAvailability
                if (name.equalsIgnoreCase("runningWaterAvailability")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdRunningWaterRadioGroup, value1);
                    }
                }

                // waterSupplyAvailabilityHoursPerDay
                if (name.equalsIgnoreCase("waterSupplyAvailabilityHoursPerDay")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.runningWaterHoursEditText.setText(value1);
                    }
                }

                // waterSupplyAvailabilityDaysPerWeek
                if (name.equalsIgnoreCase("waterSupplyAvailabilityDaysPerWeek")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.runningWaterDaysEditText.setText(value1);
                    }
                }

                // cultivableLandOwned
                if (name.equalsIgnoreCase("cultivableLandOwned")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.cultivableLandRadioGroup, value1);
                    }
                }

                // unitsOfCultivableLand
                if (name.equalsIgnoreCase("unitsOfCultivableLand")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.unitsRadioGroup, value1);
                    }
                }

                // averageAnnualHouseholdIncome
                if (name.equalsIgnoreCase("averageAnnualHouseholdIncome")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.averageAnnualHouseholdIncomeRadioGroup, value1);
                    }
                }

                // averageExpenditureOnHealth
                if (name.equalsIgnoreCase("averageExpenditureOnHealth")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.annualHealthExpenditureRadioGroup, value1);
                    }
                }

                // averageExpenditureOnEducation
                if (name.equalsIgnoreCase("averageExpenditureOnEducation")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.educationExpenditureRadioGroup, value1);
                    }
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
    }
}
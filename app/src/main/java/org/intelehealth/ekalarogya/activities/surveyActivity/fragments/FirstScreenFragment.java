package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import static org.intelehealth.ekalarogya.activities.surveyActivity.SurveyActivity.patientAttributesDTOList;
import static org.intelehealth.ekalarogya.utilities.StringUtils.checkIfEmpty;
import static org.intelehealth.ekalarogya.utilities.StringUtils.getIndex;
import static org.intelehealth.ekalarogya.utilities.StringUtils.getSurveyStrings;
import static org.intelehealth.ekalarogya.utilities.StringUtils.setSelectedCheckboxes;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.surveyActivity.SurveyActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.databinding.FragmentFirstScreenBinding;
import org.intelehealth.ekalarogya.models.dto.PatientAttributesDTO;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.StringUtils;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirstScreenFragment extends Fragment {

    private FragmentFirstScreenBinding binding;
    private String patientUuid;
    private final PatientsDAO patientsDAO = new PatientsDAO();
    private Resources updatedResources = null;
    private SessionManager sessionManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = requireActivity().getIntent();
        if (intent != null)
            patientUuid = intent.getStringExtra("patientUuid");
        updatedResources = ((SurveyActivity) requireActivity()).getUpdatedResources();
        sessionManager = ((SurveyActivity) requireActivity()).getSessionManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstScreenBinding.inflate(inflater, container, false);
        setListeners();
        setAdapters();
        setData(patientUuid);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setListeners() {
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

        binding.nextButton.setOnClickListener(v -> {
            if (!areFieldsValid()) {
                Toast.makeText(requireContext(), getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
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
        // Validation for religion spinner
        if (checkIfEmpty(requireContext(), binding.religionDropDown.getSelectedItem().toString())) {
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

        // religion
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("religion"));
        patientAttributesDTO.setValue(StringUtils.getSurveyStrings(binding.religionDropDown.getSelectedItem().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
        ));
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

        // electricityStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("electricityStatus"));
        patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                ((RadioButton) binding.householdElectricityRadioGroup.findViewById(binding.householdElectricityRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
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

        // runningWaterAvailability
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("runningWaterAvailability"));
        patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                ((RadioButton) binding.householdRunningWaterRadioGroup.findViewById(binding.householdRunningWaterRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
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

        // averageAnnualHouseholdIncome
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageAnnualHouseholdIncome"));
        patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                ((RadioButton) binding.averageAnnualHouseholdIncomeRadioGroup.findViewById(binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // averageExpenditureOnHealth
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnHealth"));
        patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                ((RadioButton) binding.annualHealthExpenditureRadioGroup.findViewById(binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // averageExpenditureOnEducation
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnEducation"));
        patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                ((RadioButton) binding.educationExpenditureRadioGroup.findViewById(binding.educationExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                requireContext().getResources(),
                updatedResources,
                sessionManager.getAppLanguage()
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

                // religion
                if (name.equalsIgnoreCase("religion")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        value1 = getSurveyStrings(value1, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
                        int position = getIndex(binding.religionDropDown, value1);
                        binding.religionDropDown.setSelection(position);
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

                // electricityStatus
                if (name.equalsIgnoreCase("electricityStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdElectricityRadioGroup, value1, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
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

                // runningWaterAvailability
                if (name.equalsIgnoreCase("runningWaterAvailability")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdRunningWaterRadioGroup, value1, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
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

                // averageAnnualHouseholdIncome
                if (name.equalsIgnoreCase("averageAnnualHouseholdIncome")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.averageAnnualHouseholdIncomeRadioGroup, value1, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
                    }
                }

                // averageExpenditureOnHealth
                if (name.equalsIgnoreCase("averageExpenditureOnHealth")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.annualHealthExpenditureRadioGroup, value1, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
                    }
                }

                // averageExpenditureOnEducation
                if (name.equalsIgnoreCase("averageExpenditureOnEducation")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.educationExpenditureRadioGroup, value1, updatedResources, requireContext().getResources(), sessionManager.getAppLanguage());
                    }
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
    }

    private void setAdapters() {
        ArrayAdapter<CharSequence> religionAdapter = null, casteAdapter = null;
        // Religion ArrayAdapter
        try {
            String religionLanguage = "religion_" + sessionManager.getAppLanguage();
            int religionId = getResources().getIdentifier(religionLanguage, "array", requireContext().getPackageName());
            if (religionId != 0) {
                religionAdapter = ArrayAdapter.createFromResource(requireContext(), religionId, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.religionDropDown.setAdapter(religionAdapter);
        } catch (Exception e) {
            Logger.logE("FirstScreenFragment", "#648", e);
        }
    }
}
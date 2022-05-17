package org.intelehealth.ekalarogya.activities.surveyActivity.fragments;

import static org.intelehealth.ekalarogya.activities.surveyActivity.SurveyActivity.patientAttributesDTOList;
import static org.intelehealth.ekalarogya.utilities.StringUtils.checkIfCheckboxesEmpty;
import static org.intelehealth.ekalarogya.utilities.StringUtils.checkIfEmpty;
import static org.intelehealth.ekalarogya.utilities.StringUtils.getSelectedCheckboxes;
import static org.intelehealth.ekalarogya.utilities.StringUtils.getSurveyValue;
import static org.intelehealth.ekalarogya.utilities.StringUtils.setSelectedCheckboxes;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.database.dao.SyncDAO;
import org.intelehealth.ekalarogya.databinding.FragmentThirdScreenBinding;
import org.intelehealth.ekalarogya.models.dto.PatientAttributesDTO;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.NetworkConnection;
import org.intelehealth.ekalarogya.utilities.StringUtils;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThirdScreenFragment extends Fragment {

    private FragmentThirdScreenBinding binding;
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
        binding = FragmentThirdScreenBinding.inflate(inflater, container, false);
        setData(patientUuid);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setOnClickListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setOnClickListener() {
        binding.otherCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherSourcesOfFuelLayout.setVisibility(View.VISIBLE);
            else
                binding.otherSourcesOfFuelLayout.setVisibility(View.GONE);
        });

        binding.otherSourceOfLightingCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherSourcesOfLightingLayout.setVisibility(View.VISIBLE);
            else
                binding.otherSourcesOfLightingLayout.setVisibility(View.GONE);
        });

        binding.otherSourceOfWaterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherSourcesOfDrinkingWaterLayout.setVisibility(View.VISIBLE);
            else
                binding.otherSourcesOfDrinkingWaterLayout.setVisibility(View.GONE);
        });

        binding.otherWaysOfPurifyingWaterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherWaysOfPurifyingWaterLayout.setVisibility(View.VISIBLE);
            else
                binding.otherWaysOfPurifyingWaterLayout.setVisibility(View.GONE);
        });

        binding.otherFacilityCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherToiletFacilityLayout.setVisibility(View.VISIBLE);
            else
                binding.otherToiletFacilityLayout.setVisibility(View.GONE);
        });

        binding.otherOpenDefecationReasonCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherOpenDefecationLayout.setVisibility(View.VISIBLE);
            else
                binding.otherOpenDefecationLayout.setVisibility(View.GONE);
        });

        binding.otherSoapOccasionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                binding.otherHandWashOccasionLayout.setVisibility(View.VISIBLE);
            else
                binding.otherHandWashOccasionLayout.setVisibility(View.GONE);
        });

        binding.submitButton.setOnClickListener(v -> {
            if (!areFieldsValid()) {
                Toast.makeText(requireContext(), "Please fill up all the required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                insertData();
            } catch (DAOException e) {
                e.printStackTrace();
            }
        });
    }


    private void insertData() throws DAOException {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();

        // cookingFuel
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("cookingFuel"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // householdLighting
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdLighting"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // sourceOfDrinkingWater
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("sourceOfDrinkingWater"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // timeTakenToWalkTillWaterSource
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("timeTakenToWalkTillWaterSource"));
        patientAttributesDTO.setValue(getSurveyValue(binding.timeToWalkTillWaterSourceEditText.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        // waterTreatment
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterTreatment"));
        patientAttributesDTO.setValue(getSurveyValue(
                ((RadioButton) binding.treatWaterRadioGroup.findViewById(binding.treatWaterRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // saferWaterMethods
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("saferWaterMethods"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // toiletFacility
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("toiletFacility"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // defecatedInOpen
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("defecatedInOpen"));
        patientAttributesDTO.setValue(getSurveyValue(
                ((RadioButton) binding.openDefecationRadioGroup.findViewById(binding.openDefecationRadioGroup.getCheckedRadioButtonId())).getText().toString()
        ));
        patientAttributesDTOList.add(patientAttributesDTO);

        // reasonForOpenDefecation
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("reasonForOpenDefecation"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.reasonForOpenDefecationCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // soapHandWashingOccasion
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("soapHandWashingOccasion"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.handWashOccasionLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        // foodItemsPreparedInTwentyFourHours
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("foodItemsPreparedInTwentyFourHours"));
        patientAttributesDTO.setValue(getSelectedCheckboxes(binding.foodCookedInTwentyFourHoursLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        boolean isPatientUpdated = patientsDAO.surveyUpdatePatientToDB(patientUuid, patientAttributesDTOList);
        if (NetworkConnection.isOnline(requireActivity().getApplication())) {
            SyncDAO syncDAO = new SyncDAO();
            boolean isPush = syncDAO.pushDataApi();
            Logger.logD("IsPushed", String.valueOf(isPush));
        }

        if (isPatientUpdated) {
            MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(requireActivity());
            alertDialog.setTitle(requireActivity().getResources().getString(R.string.surveyDialogTitle));
            alertDialog.setMessage(requireActivity().getResources().getString(R.string.surveyDialogMessage));
            alertDialog.setPositiveButton(requireActivity().getResources().getString(R.string.ok),
                    (dialog, which) -> {
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("hasPrescription", "false");
                        startActivity(intent);
                        dialog.dismiss();
                    });
            AlertDialog dialog = alertDialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(requireActivity().getResources().getColor(R.color.colorPrimaryDark));
            IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), dialog);
        }
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

                // cookingFuel
                if (name.equalsIgnoreCase("cookingFuel")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, value1);
                    }
                }

                // householdLighting
                if (name.equalsIgnoreCase("householdLighting")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, value1);
                    }
                }

                // sourceOfDrinkingWater
                if (name.equalsIgnoreCase("sourceOfDrinkingWater")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, value1);
                    }
                }

                // timeTakenToWalkTillWaterSource
                if (name.equalsIgnoreCase("timeTakenToWalkTillWaterSource")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        binding.timeToWalkTillWaterSourceEditText.setText(value1);
                    }
                }

                // waterTreatment
                if (name.equalsIgnoreCase("waterTreatment")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.treatWaterRadioGroup, value1);
                    }
                }

                // saferWaterMethods
                if (name.equalsIgnoreCase("saferWaterMethods")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, value1);
                    }
                }

                // toiletFacility
                if (name.equalsIgnoreCase("toiletFacility")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, value1);
                    }
                }

                // defecatedInOpen
                if (name.equalsIgnoreCase("defecatedInOpen")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.openDefecationRadioGroup, value1);
                    }
                }

                // reasonForOpenDefecation
                if (name.equalsIgnoreCase("reasonForOpenDefecation")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.reasonForOpenDefecationCheckboxLinearLayout, value1);
                    }
                }

                // soapHandWashingOccasion
                if (name.equalsIgnoreCase("soapHandWashingOccasion")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.handWashOccasionLinearLayout, value1);
                    }
                }

                // foodItemsPreparedInTwentyFourHours
                if (name.equalsIgnoreCase("foodItemsPreparedInTwentyFourHours")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-")) {
                        setSelectedCheckboxes(binding.foodCookedInTwentyFourHoursLinearLayout, value1);
                    }
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
    }

    private boolean areFieldsValid() {
        AtomicBoolean validations = new AtomicBoolean(true);

        // Validations for Household Cooking Fuel Linear Layout
        if (checkIfCheckboxesEmpty(binding.householdCookingFuelCheckboxLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Other Source Of Fuel EditText
        if (binding.otherCheckbox.isChecked() && checkIfEmpty(requireContext(), binding.otherSourcesOfFuelEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Main Source Of Lighting Linear Layout
        if (checkIfCheckboxesEmpty(binding.mainSourceOfLightingCheckboxLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Other Source Of Lighting Linear Layout
        if (binding.otherSourceOfLightingCheckbox.isChecked() && checkIfEmpty(requireContext(), binding.otherSourcesOfLightingEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Main Source of Drinking Water Linear Layout
        if (checkIfCheckboxesEmpty(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Other Sources of Drinking Water EditText
        if (binding.otherSourceOfWaterCheckbox.isChecked() && checkIfEmpty(requireContext(), binding.otherSourcesOfDrinkingWaterEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Time To Walk Till Water Source
        if (checkIfEmpty(requireContext(), binding.timeToWalkTillWaterSourceEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Treat Water Radio Group
        if (binding.treatWaterRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validations for Household Make Safe Water Checkbox Linear Layout
        if (checkIfCheckboxesEmpty(binding.householdMakeSafeWaterCheckboxLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Other Ways of Purifying Water Edit Text
        if (binding.otherWaysOfPurifyingWaterCheckbox.isChecked() && checkIfEmpty(requireContext(), binding.otherWaysOfPurifyingWaterEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Family Toilet Facility Linear Layout
        if (checkIfCheckboxesEmpty(binding.familyToiletFacilityCheckboxLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Other Toilet Facility Layout
        if (binding.otherFacilityCheckbox.isChecked() && checkIfEmpty(requireContext(), binding.otherToiletFacilityEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Open Defecation Radio Group
        if (binding.openDefecationRadioGroup.getCheckedRadioButtonId() == -1) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Reason For Open Defecation Linear Layout
        if (checkIfCheckboxesEmpty(binding.reasonForOpenDefecationCheckboxLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Other Reason for Open Defecation Layout
        if (binding.otherOpenDefecationReasonCheckbox.isChecked() && checkIfEmpty(requireContext(), binding.otherOpenDefecationEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Hand Wash Occasion Linear Layout
        if (checkIfCheckboxesEmpty(binding.handWashOccasionLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Other Hand Wash Occasion Layout
        if (binding.otherSoapOccasionCheckbox.isChecked() && checkIfEmpty(requireContext(), binding.otherHandWashOccasionEditText.getText().toString())) {
            validations.set(false);
            return validations.get();
        }

        // Validation for Food Cooked In Twenty Four Hours Layout
        if (checkIfCheckboxesEmpty(binding.foodCookedInTwentyFourHoursLinearLayout)) {
            validations.set(false);
            return validations.get();
        }

        return validations.get();
    }
}
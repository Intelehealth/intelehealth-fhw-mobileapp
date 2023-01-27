package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;
import static org.intelehealth.app.utilities.StringUtils.getWaterSourceDistance;
import static org.intelehealth.app.utilities.StringUtils.getWaterSourceDistanceEdit;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.databinding.FragmentThirdScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ThirdScreenFragment#} factory method to
 * create an instance of this fragment.
 */

// TODO: Refer the implementation of FirstScreen and SecondScreen for data interoperability and flow and
//  follow same for all other screens -- Prajwal 17-02-2022.


public class ThirdScreenFragment extends Fragment {

    private FragmentThirdScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;
    private List<View> mandatoryFields = new ArrayList<>();
    PatientsDAO patientsDAO = new PatientsDAO();

    MaterialCheckBox village_tank, open_well, handpump, borewell, river, pond, other;

    public ThirdScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
        }

        sessionManager = new SessionManager(getActivity());
        String language = sessionManager.getAppLanguage();
        Log.d("lang", "lang: " + language);
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(config,
                    getActivity().getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //View rootView =  inflater.inflate(R.layout.fragment_third_screen, container, false);
        binding = FragmentThirdScreenBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        village_tank = rootView.findViewById(R.id.village_tank_checkbox);
        open_well = rootView.findViewById(R.id.open_well_checkbox);
        handpump = rootView.findViewById(R.id.hand_pump_checkbox);
        borewell = rootView.findViewById(R.id.bore_well_checkbox);
        river = rootView.findViewById(R.id.river_checkbox);
        pond = rootView.findViewById(R.id.pond_checkbox);
        other = rootView.findViewById(R.id.other_checkbox);

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    insertData();
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }
        });
        binding.prevButton.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });
        binding.otherCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.otherSourcesOfWaterLayout.setVisibility(View.VISIBLE);
            } else {
                binding.otherSourcesOfWaterLayout.setVisibility(View.GONE);
            }
        });

        binding.householdElectricityRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.electricity_yes_checkbox) {
                binding.loadSheddingHoursLayout.setVisibility(View.VISIBLE);
                binding.loadSheddingDaysPerWeekLayout.setVisibility(View.VISIBLE);
            } else {
                //reset the fields as well...
                binding.loadSheddingHoursTextView.setText("0");
                binding.loadSheddingDaysPerWeekTextView.setText("0");

                binding.loadSheddingHoursLayout.setVisibility(View.GONE);
                binding.loadSheddingDaysPerWeekLayout.setVisibility(View.GONE);
            }
        });

        binding.runningWaterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.running_water_no_checkbox) {
                binding.primarySourceOfWaterTextView.setVisibility(View.VISIBLE);
                binding.primarySourceOfWaterCheckboxLinearLayout.setVisibility(View.VISIBLE);

                if (binding.otherCheckbox.isChecked()) {
                    binding.otherSourcesOfWaterLayout.setVisibility(View.VISIBLE);
                }

                binding.waterSourceDistanceRadioGroup.setVisibility(View.VISIBLE);
                binding.waterSourceDistanceTextView.setVisibility(View.VISIBLE);
                binding.waterSourceDistanceTextInput.setVisibility(View.VISIBLE);

                binding.waterSupplyAvailabilityTextView.setVisibility(View.GONE);
                binding.waterSupplyAvailabilityDaysPerWeekTextView.setVisibility(View.GONE);

                binding.waterSupplyAvailabilityEditText.setVisibility(View.GONE);
                binding.waterSupplyAvailabilityEditText.setText("0");
                binding.waterSupplyAvailabilityDaysPerWeekEditText.setVisibility(View.GONE);
                binding.waterSupplyAvailabilityDaysPerWeekEditText.setText("0");
            } else {
                binding.primarySourceOfWaterTextView.setVisibility(View.GONE);
                binding.primarySourceOfWaterCheckboxLinearLayout.setVisibility(View.GONE);
                binding.waterSourceDistanceRadioGroup.setVisibility(View.GONE);
                binding.waterSourceDistanceTextView.setVisibility(View.GONE);
                binding.otherSourcesOfWaterLayout.setVisibility(View.GONE);
                binding.waterSourceDistanceTextInput.setVisibility(View.GONE);

                binding.waterSupplyAvailabilityTextView.setVisibility(View.VISIBLE);
                binding.waterSupplyAvailabilityEditText.setVisibility(View.VISIBLE);

                binding.waterSupplyAvailabilityDaysPerWeekTextView.setVisibility(View.VISIBLE);
                binding.waterSupplyAvailabilityDaysPerWeekEditText.setVisibility(View.VISIBLE);
            }
        });

        binding.waterSourceDistanceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.waterSourceDistanceTextInput.setVisibility(View.VISIBLE);
        });

        mandatoryFields.addAll(Arrays.asList(binding.householdElectricityRadioGroup, binding.waterSourceDistanceRadioGroup, binding.bankAccountRadioGroup));

        getPatientUuidsForHouseholdValue(patientUuid);
        // setData(patientUuid);
        return rootView;
    }

    public void getPatientUuidsForHouseholdValue(String patientUuid) {
        String houseHoldValue = "";
        try {
            houseHoldValue = patientsDAO.getHouseHoldValue(patientUuid);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (!houseHoldValue.equalsIgnoreCase("")) {
            //Fetch all patient UUID from houseHoldValue
            try {
                List<String> patientUUIDs = new ArrayList<>(patientsDAO.getPatientUUIDs(houseHoldValue));
                Log.e("patientUUIDss", "" + patientUUIDs);
                for (int i = 0; i < patientUUIDs.size(); i++) {
                    setData(patientUUIDs.get(i));
                }
            } catch (Exception e) {
            }
        }
    }

    private void insertData() throws DAOException {
//        if (!StringUtils.validateFields(mandatoryFields)) {
//            Toast.makeText(getContext(), R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
//            return;
//        }

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //householdElectricityStatus - start
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdElectricityStatus"));

        if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO.setValue(binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == binding.electricityYesCheckbox.getId() ?
                    StringUtils.getPreTerm(binding.electricityYesCheckbox.getText().toString(), sessionManager.getAppLanguage()) :
                    StringUtils.getPreTerm(binding.electricityNoCheckbox.getText().toString(), sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);

            //yes/no start
            if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == binding.electricityYesCheckbox.getId()) {
                //noOfLoadSheddingHrsPerDay
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerDay"));
                patientAttributesDTO.setValue(StringUtils.getValue(binding.loadSheddingHoursTextView.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

                //noOfLoadSheddingHrsPerWeek
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerWeek"));
                patientAttributesDTO.setValue(StringUtils.getValue(binding.loadSheddingDaysPerWeekTextView.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
            }
          /*  else if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == binding.electricityNoCheckbox.getId()) {
                // For Yes, we're adding default values to avoid data discrepancy in local db
                //noOfLoadSheddingHrsPerDay
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerDay"));
                patientAttributesDTO.setValue("No load shedding hours per day");
                patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

                //noOfLoadSheddingHrsPerWeek
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerWeek"));
                patientAttributesDTO.setValue("No load shedding hours per week");
                patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
            } else {
                //noOfLoadSheddingHrsPerDay
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerDay"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

                //noOfLoadSheddingHrsPerWeek
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerWeek"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
            }*/
            // yes/no - end
        } else {
            patientAttributesDTO.setValue("-");
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //houseelectristatus - end

        //runningWaterStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("runningWaterStatus"));
        if (binding.runningWaterRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO.setValue(
                    binding.runningWaterRadioGroup.getCheckedRadioButtonId() == binding.runningWaterYesCheckbox.getId() ?
                            StringUtils.getPreTerm(binding.runningWaterYesCheckbox.getText().toString(), sessionManager.getAppLanguage()) :
                            StringUtils.getPreTerm(binding.runningWaterNoCheckbox.getText().toString(), sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);

            // yes and no checking - start
            if (binding.runningWaterRadioGroup.getCheckedRadioButtonId() == binding.runningWaterYesCheckbox.getId()) {
                //waterSupplyAvailabilityHrsPerDay
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHrsPerDay"));
                patientAttributesDTO.setValue(StringUtils.getValue(binding.waterSupplyAvailabilityEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                //waterSupplyAvailabilityDaysPerWeek
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysperWeek"));
                patientAttributesDTO.setValue(StringUtils.getValue(binding.waterSupplyAvailabilityDaysPerWeekEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // For No, we're adding default values
                //primarySourceOfRunningWater
               /* patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("primarySourceOfRunningWater"));

                String otherSourceOfRunningWater = "-";
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);

                //waterSourceDistance
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSourceDistance"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);*/
            }
            else if (binding.runningWaterRadioGroup.getCheckedRadioButtonId() == binding.runningWaterNoCheckbox.getId()) {
                //primarySourceOfRunningWater
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("primarySourceOfRunningWater"));

                String otherSourceOfRunningWater;
                if (binding.otherCheckbox.isChecked()) {
                    otherSourceOfRunningWater = StringUtils.getValue(binding.otherSourcesOfWaterEditText.getText().toString());
                } else {
                    otherSourceOfRunningWater = "-";
                }

                patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.primarySourceOfWaterCheckboxLinearLayout,
                        sessionManager.getAppLanguage(),
                        getContext(),
                        otherSourceOfRunningWater));
                patientAttributesDTOList.add(patientAttributesDTO);

                //waterSourceDistance
                if (binding.waterSourceDistanceRadioGroup.getCheckedRadioButtonId() != -1) {
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSourceDistance"));

                    String distance = StringUtils.getValue(binding.waterSourceDistanceEditText.getText().toString()) + " " +
                            (binding.waterSourceDistanceRadioGroup.getCheckedRadioButtonId() ==
                                    binding.waterSourceDistanceMeter.getId() ?
                                    getWaterSourceDistance(binding.waterSourceDistanceMeter.getText().toString(), requireContext(), sessionManager.getAppLanguage()) :
                                    getWaterSourceDistance(binding.waterSourceDistanceKilometer.getText().toString(), requireContext(), sessionManager.getAppLanguage())
                            );

                    patientAttributesDTO.setValue(distance);
                    patientAttributesDTOList.add(patientAttributesDTO);
                }

                // For Yes, we're adding default values to avoid data discrepancy in local db
                //waterSupplyAvailabilityHrsPerDay
             /*   patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHrsPerDay"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);

                //waterSupplyAvailabilityDaysPerWeek
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysperWeek")); //TODO add here new value
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);*/
            }
/*
            else {
                //waterSupplyAvailabilityHrsPerDay
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHrsPerDay"));
                patientAttributesDTO.setValue(StringUtils.getValue("-"));
                patientAttributesDTOList.add(patientAttributesDTO);

                //waterSupplyAvailabilityDaysPerWeek
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysperWeek"));
                patientAttributesDTO.setValue(StringUtils.getValue("-"));
                patientAttributesDTOList.add(patientAttributesDTO);

                //primarySourceOfRunningWater
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("primarySourceOfRunningWater"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);

                //waterSourceDistance
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSourceDistance"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);
            }
*/
            // yes and no checking - end

        } else {
            patientAttributesDTO.setValue("-");
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        // running water -- end

        //householdBankAccountStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdBankAccountStatus"));

        if (binding.bankAccountRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO.setValue(binding.bankAccountRadioGroup.getCheckedRadioButtonId() == binding.bankAccountYes.getId() ?
                    StringUtils.getPreTerm(binding.bankAccountYes.getText().toString(), sessionManager.getAppLanguage()) :
                    StringUtils.getPreTerm(binding.bankAccountNo.getText().toString(), sessionManager.getAppLanguage())
            );
        } else {
            patientAttributesDTO.setValue("-");
        }

        patientAttributesDTOList.add(patientAttributesDTO);

        Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("screen", "secondscreen: \n" + gson.toJson(patientAttributesDTOList));

        // TODO: this logic just for testing purpose have added here. Once all screens is done than at the end of 7th screen
        //  by clicking on SUBMIT button add this code on that button clicklistener...
        boolean isPatientUpdated = patientsDAO.SurveyupdatePatientToDB(patientUuid, patientAttributesDTOList);
//        if (NetworkConnection.isOnline(getActivity().getApplication())) {
//            SyncDAO syncDAO = new SyncDAO();
//            boolean ispush = syncDAO.pushDataApi();
//
//        }
//        // Upto here so that data is stored in localdb and pushed by clicking on FAB...
//
//        if (isPatientUpdated) {
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.framelayout_container, new FourthScreenFragment())
//                    .commit();
//        }
        getFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new FourthScreenFragment())
                .addToBackStack(null)
                .commit();

    }

    private void setData(String patientUuid) {
        Context updatedContext;

        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            updatedContext = requireContext().createConfigurationContext(configuration);
        } else {
            updatedContext = requireContext();
        }

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
                if (name.equalsIgnoreCase("householdElectricityStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && value1.equalsIgnoreCase(updatedContext.getString(R.string.yes)))
                        binding.electricityYesCheckbox.setChecked(true);
                    else if (value1 != null && value1.equalsIgnoreCase(updatedContext.getString(R.string.no)))
                        binding.electricityNoCheckbox.setChecked(true);
                }
                if (name.equalsIgnoreCase("noOfLoadSheddingHrsPerDay")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("No load shedding hours per day") && !value1.equalsIgnoreCase("-"))
                        binding.loadSheddingHoursTextView.setText(value1);
                }
                if (name.equalsIgnoreCase("noOfLoadSheddingHrsPerWeek")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("No load shedding hours per week") && !value1.equalsIgnoreCase("-"))
                        binding.loadSheddingDaysPerWeekTextView.setText(value1);
                }
                if (name.equalsIgnoreCase("runningWaterStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && value1.equalsIgnoreCase(updatedContext.getString(R.string.yes)))
                        binding.runningWaterYesCheckbox.setChecked(true);
                    else if (value1 != null && value1.equalsIgnoreCase(updatedContext.getString(R.string.no)))
                        binding.runningWaterNoCheckbox.setChecked(true);
                }
                if (name.equalsIgnoreCase("primarySourceOfRunningWater")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.village_tank)))
                        village_tank.setChecked(true);
                    else
                        village_tank.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.open_well)))
                        open_well.setChecked(true);
                    else
                        open_well.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.hand_pump_checkbox)))
                        handpump.setChecked(true);
                    else
                        handpump.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.bore_well)))
                        borewell.setChecked(true);
                    else
                        borewell.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.river)))
                        river.setChecked(true);
                    else
                        river.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.pond)))
                        pond.setChecked(true);
                    else
                        pond.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.other))) {
                        other.setChecked(true);

                        Context tempContext;

                        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
                            configuration.setLocale(new Locale("en"));
                            tempContext = requireContext().createConfigurationContext(configuration);
                        } else {
                            tempContext = requireContext();
                        }

                        try {
                            String otherSourceOfWater = "";
                            JSONArray jsonArray = new JSONArray(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String element = jsonArray.getString(i);
                                if (element.contains(tempContext.getString(R.string.other))) {
                                    otherSourceOfWater = jsonArray.getString(i);
                                    otherSourceOfWater = otherSourceOfWater.substring(otherSourceOfWater.indexOf(":") + 2);
                                }
                            }
                            binding.otherSourcesOfWaterEditText.setText(otherSourceOfWater);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        other.setChecked(false);
                    }

                }
                if (name.equalsIgnoreCase("waterSourceDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("No water source distance") && !value1.equalsIgnoreCase("-")) {
                        String[] splitString = value1.split(" ");
                        splitString[1] = getWaterSourceDistanceEdit(splitString[1], requireContext(), sessionManager.getAppLanguage());

                        if (splitString[1].equalsIgnoreCase(getString(R.string.meter)))
                            binding.waterSourceDistanceMeter.setChecked(true);
                        else
                            binding.waterSourceDistanceKilometer.setChecked(true);

                        binding.waterSourceDistanceEditText.setText(splitString[0]);
                    }
                }

                if (name.equalsIgnoreCase("waterSupplyAvailabilityHrsPerDay")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("No water supply for days") && !value1.equalsIgnoreCase("-"))
                        binding.waterSupplyAvailabilityEditText.setText(value1);
                }

                if (name.equalsIgnoreCase("waterSupplyAvailabilityDaysperWeek")) { //TODO: Add new uuid here
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("No water supply for week") && !value1.equalsIgnoreCase("-"))
                        binding.waterSupplyAvailabilityDaysPerWeekEditText.setText(value1);
                }

                if (name.equalsIgnoreCase("householdBankAccountStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && value1.equalsIgnoreCase(updatedContext.getString(R.string.yes)))
                        binding.bankAccountYes.setChecked(true);
                    else if (value1 != null && value1.equalsIgnoreCase(updatedContext.getString(R.string.no)))
                        binding.bankAccountNo.setChecked(true);
                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

}
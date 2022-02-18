package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentSecondScreenBinding;
import org.intelehealth.app.databinding.FragmentThirdScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.Locale;
import java.util.UUID;

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ThirdScreenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

// TODO: Refer the implementation of FirstScreen and SecondScreen for data interoperability and flow and
//  follow same for all other screens -- Prajwal 17-02-2022.


public class ThirdScreenFragment extends Fragment {

    private FragmentThirdScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;

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


        binding.householdElectricityRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.electricity_yes_checkbox) {
                binding.loadSheddingHoursLayout.setVisibility(View.VISIBLE);
                binding.loadSheddingDaysPerWeekLayout.setVisibility(View.VISIBLE);
            } else {
                binding.loadSheddingHoursLayout.setVisibility(View.GONE);
                binding.loadSheddingDaysPerWeekLayout.setVisibility(View.GONE);
            }
        });

        binding.runningWaterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            primary_source_of_water_text_view primary_source_of_water_checkbox_linear_layout
            if (checkedId == R.id.running_water_no_checkbox) {
                binding.primarySourceOfWaterTextView.setVisibility(View.VISIBLE);
                binding.primarySourceOfWaterCheckboxLinearLayout.setVisibility(View.VISIBLE);
                binding.waterSourceDistanceRadioGroup.setVisibility(View.VISIBLE);
            } else {
                binding.primarySourceOfWaterTextView.setVisibility(View.GONE);
                binding.primarySourceOfWaterCheckboxLinearLayout.setVisibility(View.GONE);
                binding.waterSourceDistanceRadioGroup.setVisibility(View.GONE);
            }
        });

        return rootView;
    }

    private void insertData() throws DAOException {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //householdElectricityStatus
        if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdElectricityStatus"));
            patientAttributesDTO.setValue(binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == binding.electricityYesCheckbox.getId() ? binding.electricityYesCheckbox.getText().toString() : binding.electricityNoCheckbox.getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //noOfLoadSheddingHrsPerDay
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerDay"));
        patientAttributesDTO.setValue(binding.loadSheddingHoursTextView.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        //noOfLoadSheddingHrsPerWeek
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("noOfLoadSheddingHrsPerWeek"));
        patientAttributesDTO.setValue(binding.loadSheddingDaysPerWeekTextView.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...


        //runningWaterStatus
        if (binding.runningWaterRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("runningWaterStatus"));
            patientAttributesDTO.setValue(binding.runningWaterRadioGroup.getCheckedRadioButtonId() == binding.runningWaterYesCheckbox.getId() ? binding.runningWaterYesCheckbox.getText().toString() : binding.runningWaterNoCheckbox.getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //primarySourceOfRunningWater
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("primarySourceOfRunningWater"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.primarySourceOfWaterCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        //waterSourceDistance
        if (binding.waterSourceDistanceRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSourceDistance"));
            patientAttributesDTO.setValue(binding.waterSourceDistanceEditText.getText().toString() + " " +
                    (binding.waterSourceDistanceRadioGroup.getCheckedRadioButtonId() == binding.waterSourceDistanceMeter.getId() ? binding.waterSourceDistanceMeter.getText().toString() : binding.waterSourceDistanceKilometer.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //waterSupplyAvailabilityHrsPerDay
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHrsPerDay"));
        patientAttributesDTO.setValue(binding.waterSupplyAvailabilityEditText.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //householdBankAccountStatus
        if (binding.bankAccountRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdBankAccountStatus"));
            patientAttributesDTO.setValue(binding.bankAccountRadioGroup.getCheckedRadioButtonId() == binding.bankAccountYes.getId() ? binding.bankAccountYes.getText().toString() : binding.bankAccountNo.getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }


        Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("screen", "secondscreen: \n"+ gson.toJson(patientAttributesDTOList));

        // TODO: this logic just for testing purpose have added here. Once all screens is done than at the end of 7th screen
        //  by clicking on SUBMIT button add this code on that button clicklistener...
//        boolean isPatientUpdated = patientsDAO.SurveyupdatePatientToDB(patientUuid, patientAttributesDTOList);
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
                    .commit();

    }
}
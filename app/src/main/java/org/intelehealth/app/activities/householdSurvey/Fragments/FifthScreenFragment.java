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
import android.widget.RadioButton;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentFifthScreenBinding;
import org.intelehealth.app.databinding.FragmentFourthScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.Locale;
import java.util.UUID;

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;

public class FifthScreenFragment extends Fragment {

    private FragmentFifthScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;

    public FifthScreenFragment() {
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
        //View rootView =  inflater.inflate(R.layout.fragment_fifth_screen, container, false);
        binding = FragmentFifthScreenBinding.inflate(inflater, container, false);
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

        binding.otherCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.otherSourcesOfFuelLayout.setVisibility(View.VISIBLE);
            } else {
                binding.otherSourcesOfFuelLayout.setVisibility(View.GONE);
            }
        });

        binding.otherSourceOfLightingCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.otherSourcesOfLightingLayout.setVisibility(View.VISIBLE);
            } else {
                binding.otherSourcesOfLightingLayout.setVisibility(View.GONE);
            }
        });

        binding.otherSourceOfWaterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.otherSourcesOfDrinkingWaterLayout.setVisibility(View.VISIBLE);
            } else {
                binding.otherSourcesOfDrinkingWaterLayout.setVisibility(View.GONE);
            }
        });

        binding.otherWaysOfPurifyingWaterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.otherWaysOfPurifyingWaterEditText.setVisibility(View.VISIBLE);
            } else {
                binding.otherWaysOfPurifyingWaterEditText.setVisibility(View.GONE);
            }
        });

        return rootView;
    }

    private void insertData() throws DAOException {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //cookingFuelType
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("cookingFuelType"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);


        //mainLightingSource
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("mainLightingSource"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        //mainDrinkingWaterSource
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("mainDrinkingWaterSource"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        //saferWaterProcess
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("saferWaterProcess"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

        //householdToiletFacility
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdToiletFacility"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout));
        patientAttributesDTOList.add(patientAttributesDTO);

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
//                    .replace(R.id.framelayout_container, new SixthScreenFragment())
//                    .commit();
//        }
            getFragmentManager().beginTransaction()
                    .replace(R.id.framelayout_container, new SixthScreenFragment())
                    .commit();
    }
}
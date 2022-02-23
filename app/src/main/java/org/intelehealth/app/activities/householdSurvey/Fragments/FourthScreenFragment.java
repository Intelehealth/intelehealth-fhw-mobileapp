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
import android.widget.Toast;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentFourthScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;

public class FourthScreenFragment extends Fragment {

    private FragmentFourthScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;
    private List<View> mandatoryFields = new ArrayList<>();
    public FourthScreenFragment() {
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
//        View rootView =  inflater.inflate(R.layout.fragment_fourth_screen, container, false);
        binding = FragmentFourthScreenBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    insertData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        binding.cultivableLandRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.cultivableLandLayout.setVisibility(View.VISIBLE);
        });

        binding.annualHealthExpenditureRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.greater_than_thirty_thousand_radio_button) {
                binding.moreThanThirtyThousandLayout.setVisibility(View.VISIBLE);
            } else {
                binding.moreThanThirtyThousandLayout.setVisibility(View.GONE);
            }
        });

        mandatoryFields.addAll(Arrays.asList(binding.cultivableLandRadioGroup, binding.averageAnnualHouseholdIncomeRadioGroup, binding.monthlyFoodExpenditureRadioGroup, binding.annualHealthExpenditureRadioGroup, binding.annualEducationExpenditureRadioGroup
                , binding.annualClothingExpenditureRadioGroup, binding.monthlyIntoxicantsExpenditureRadioGroup, binding.bplCardCouponRadioGroup, binding.antodayaCardCouponRadioGroup, binding.rsbyCardRadioGroup, binding.mgnregaCardRadioGroup));
        return rootView;
    }

    private void insertData() throws Exception {
//        if (!StringUtils.validateFields(mandatoryFields)) {
//            Toast.makeText(getContext(), R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
//            return;
//        }

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //householdCultivableLand
        if (binding.cultivableLandRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdCultivableLand"));
            patientAttributesDTO.setValue(binding.cultivableLandEditText.getText().toString() + " " +
                    ((RadioButton) binding.cultivableLandRadioGroup.findViewById(binding.cultivableLandRadioGroup.getCheckedRadioButtonId())).getText());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //averageAnnualHouseholdIncome
        if (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageAnnualHouseholdIncome"));
            patientAttributesDTO.setValue(((RadioButton) binding.averageAnnualHouseholdIncomeRadioGroup.findViewById(binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //monthlyFoodExpenditure
        if (binding.monthlyFoodExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("monthlyFoodExpenditure"));
            patientAttributesDTO.setValue(((RadioButton) binding.monthlyFoodExpenditureRadioGroup.findViewById(binding.monthlyFoodExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }


        //annualHealthExpenditure
        if (binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("annualHealthExpenditure"));
            patientAttributesDTO.setValue(((RadioButton) binding.annualHealthExpenditureRadioGroup.findViewById(binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //annualEducationExpenditure
        if (binding.annualEducationExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("annualEducationExpenditure"));
            patientAttributesDTO.setValue(((RadioButton) binding.annualEducationExpenditureRadioGroup.findViewById(binding.annualEducationExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //annualClothingExpenditure
        if (binding.annualClothingExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("annualClothingExpenditure"));
            patientAttributesDTO.setValue(((RadioButton) binding.annualClothingExpenditureRadioGroup.findViewById(binding.annualClothingExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //monthlyIntoxicantsExpenditure
        if (binding.monthlyIntoxicantsExpenditureRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("monthlyIntoxicantsExpenditure"));
            patientAttributesDTO.setValue(((RadioButton) binding.monthlyIntoxicantsExpenditureRadioGroup.findViewById(binding.monthlyIntoxicantsExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }


        //householdBPLCardStatus
        if (binding.bplCardCouponRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdBPLCardStatus"));
            patientAttributesDTO.setValue(((RadioButton) binding.bplCardCouponRadioGroup.findViewById(binding.bplCardCouponRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //householdAntodayaCardStatus
        if (binding.antodayaCardCouponRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdAntodayaCardStatus"));
            patientAttributesDTO.setValue(((RadioButton) binding.antodayaCardCouponRadioGroup.findViewById(binding.antodayaCardCouponRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //householdRSBYCardStatus
        if (binding.rsbyCardRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdRSBYCardStatus"));
            patientAttributesDTO.setValue(((RadioButton) binding.rsbyCardRadioGroup.findViewById(binding.rsbyCardRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
        }

        //householdMGNREGACardStatus
        if (binding.mgnregaCardRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdMGNREGACardStatus"));
            patientAttributesDTO.setValue(((RadioButton) binding.mgnregaCardRadioGroup.findViewById(binding.mgnregaCardRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...
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
//                    .replace(R.id.framelayout_container, new FifthScreenFragment())
//                    .commit();
//        }

        getFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new FifthScreenFragment())
                .commit();

    }
}
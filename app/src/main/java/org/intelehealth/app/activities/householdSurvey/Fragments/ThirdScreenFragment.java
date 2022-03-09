package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentSecondScreenBinding;
import org.intelehealth.app.databinding.FragmentThirdScreenBinding;
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
                binding.waterSourceDistanceRadioGroup.setVisibility(View.VISIBLE);
                binding.waterSourceDistanceTextView.setVisibility(View.VISIBLE);

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
            }
            catch (Exception e) {
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

        //waterSupplyAvailabilityDaysPerWeek
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysperWeek")); //TODO add here new value
        patientAttributesDTO.setValue(binding.waterSupplyAvailabilityDaysPerWeekEditText.getText().toString());
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
                    .addToBackStack(null)
                    .commit();

    }

    private void setData(String patientUuid)
    {
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
                    if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.yes)))
                        binding.electricityYesCheckbox.setChecked(true);

                    else if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.no)))
                        binding.electricityNoCheckbox.setChecked(true);
                }
                if (name.equalsIgnoreCase("noOfLoadSheddingHrsPerDay")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        binding.loadSheddingHoursTextView.setText(value1);
                }
                if (name.equalsIgnoreCase("noOfLoadSheddingHrsPerWeek")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        binding.loadSheddingDaysPerWeekTextView.setText(value1);
                }
                if (name.equalsIgnoreCase("runningWaterStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.yes)))
                        binding.runningWaterYesCheckbox.setChecked(true);
                    else if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.no)))
                        binding.runningWaterNoCheckbox.setChecked(true);
                }
                if (name.equalsIgnoreCase("primarySourceOfRunningWater")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.village_tank)))
                        village_tank.setChecked(true);
                    else
                        village_tank.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.open_well)))
                        open_well.setChecked(true);
                    else
                        open_well.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.hand_pump_checkbox)))
                        handpump.setChecked(true);
                    else
                        handpump.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.bore_well)))
                        borewell.setChecked(true);
                    else
                        borewell.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.river)))
                        river.setChecked(true);
                    else
                        river.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.pond)))
                        pond.setChecked(true);
                    else
                        pond.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(getString(R.string.other)))
                        other.setChecked(true);
                    else
                        other.setChecked(false);

                }
                if (name.equalsIgnoreCase("waterSourceDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.meter)))
                        binding.waterSourceDistanceMeter.setChecked(true);
                    else if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.km)))
                        binding.waterSourceDistanceKilometer.setChecked(true);
                }

                if (name.equalsIgnoreCase("waterSupplyAvailabilityHrsPerDay")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        binding.waterSupplyAvailabilityEditText.setText(value1);
                }

                if (name.equalsIgnoreCase("waterSupplyAvailabilityDaysperWeek")) { //TODO: Add new uuid here
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        binding.waterSupplyAvailabilityDaysPerWeekEditText.setText(value1);
                }

                if (name.equalsIgnoreCase("householdBankAccountStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.yes)))
                        binding.bankAccountYes.setChecked(true);
                    else if(value1!=null && value1.equalsIgnoreCase(getResources().getString(R.string.no)))
                        binding.bankAccountNo.setChecked(true);
                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

}
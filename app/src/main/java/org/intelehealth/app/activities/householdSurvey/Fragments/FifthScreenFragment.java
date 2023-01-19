package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentFifthScreenBinding;
import org.intelehealth.app.databinding.FragmentFourthScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;

public class FifthScreenFragment extends Fragment {

    private FragmentFifthScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;
    PatientsDAO patientsDAO = new PatientsDAO();

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

        binding.prevButton.setOnClickListener(view -> {
            getActivity().onBackPressed();
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
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //cookingFuelType
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("cookingFuelType"));

        String otherCookingFuel;
        if (binding.otherCheckbox.isChecked()) {
            otherCookingFuel = StringUtils.getValue(binding.otherSourcesOfFuelLayout.getEditText().getText().toString());
        } else {
            otherCookingFuel = "-";
        }

        patientAttributesDTO.setValue(StringUtils
                .getSelectedCheckboxes(
                        binding.householdCookingFuelCheckboxLinearLayout,
                        sessionManager.getAppLanguage(),
                        getContext(),
                        otherCookingFuel)
        );
        patientAttributesDTOList.add(patientAttributesDTO);


        //mainLightingSource
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("mainLightingSource"));

        String otherLightingSource;
        if (binding.otherSourceOfLightingCheckbox.isChecked()) {
            otherLightingSource = StringUtils.getValue(binding.otherSourcesOfLightingLayout.getEditText().getText().toString());
        } else {
            otherLightingSource = "-";
        }

        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout,
                sessionManager.getAppLanguage(),
                getContext(),
                otherLightingSource));
        patientAttributesDTOList.add(patientAttributesDTO);

        //mainDrinkingWaterSource
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("mainDrinkingWaterSource"));

        String otherDrinkingWaterSource;
        if (binding.otherSourceOfWaterCheckbox.isChecked()) {
            otherDrinkingWaterSource = StringUtils.getValue(binding.otherSourcesOfDrinkingWaterLayout.getEditText().getText().toString());
        } else {
            otherDrinkingWaterSource = "-";
        }

        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout,
                sessionManager.getAppLanguage(),
                getContext(),
                otherDrinkingWaterSource));
        patientAttributesDTOList.add(patientAttributesDTO);

        //saferWaterProcess
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("saferWaterProcess"));

        String otherSaferWaterProcess;
        if (binding.otherWaysOfPurifyingWaterCheckbox.isChecked()) {
            otherSaferWaterProcess = StringUtils.getValue(binding.otherWaysOfPurifyingWaterEditText.getEditText().getText().toString());
        } else {
            otherSaferWaterProcess = "-";
        }

        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout,
                sessionManager.getAppLanguage(),
                getContext(),
                otherSaferWaterProcess));
        patientAttributesDTOList.add(patientAttributesDTO);

        //householdToiletFacility
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdToiletFacility"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, sessionManager.getAppLanguage(), getContext(), ""));
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
//                    .replace(R.id.framelayout_container, new SixthScreenFragment())
//                    .commit();
//        }
        getFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new SixthScreenFragment())
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
                if (name.equalsIgnoreCase("cookingFuelType")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.electricity)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.electricity));
                    else
                        binding.electricityCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.lpg_natural_gas)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.lpg_natural_gas));
                    else
                        binding.lpgNaturalGasCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.biogas_checkbox)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.biogas_checkbox));
                    else
                        binding.biogasCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.kerosene)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.kerosene));
                    else
                        binding.keroseneCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.coal_lignite)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.coal_lignite));
                    else
                        binding.coalCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.wood)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.wood));
                    else
                        binding.woodCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.charcoal)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.charcoal));
                    else
                        binding.charcoalCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.straw_shrubs_grass)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.straw_shrubs_grass));
                    else
                        binding.strawShrubsGrassCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.agricultural_crop_waste)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.agricultural_crop_waste));
                    else
                        binding.agriculturalCropWasteCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.dung_cakes)))
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.dung_cakes));
                    else
                        binding.dungCakesCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.other_specify))) {
                        setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, getString(R.string.other_specify));

                        Context tempContext;

                        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
                            configuration.setLocale(new Locale("en"));
                            tempContext = requireContext().createConfigurationContext(configuration);
                        } else {
                            tempContext = requireContext();
                        }

                        try {
                            String otherSourceOfFuel = "";
                            JSONArray jsonArray = new JSONArray(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String element = jsonArray.getString(i);
                                if (element.contains(tempContext.getString(R.string.other_specify))) {
                                    otherSourceOfFuel = jsonArray.getString(i);
                                    otherSourceOfFuel = otherSourceOfFuel.substring(otherSourceOfFuel.indexOf(":") + 2);
                                }
                            }

                            if (!otherSourceOfFuel.equalsIgnoreCase("-"))
                                binding.otherSourcesOfFuelLayout.getEditText().setText(otherSourceOfFuel);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else
                        binding.otherCheckbox.setChecked(false);
                }

                if (name.equalsIgnoreCase("mainLightingSource")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.lantern)))
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.lantern));
                    else
                        binding.lanternCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.kerosene_lamp)))
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.kerosene_lamp));
                    else
                        binding.keroseneLampCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.candle)))
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.candle));
                    else
                        binding.candleCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.electric)))
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.electric));
                    else
                        binding.electricCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.lpg)))
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.lpg));
                    else
                        binding.lpgCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.solar_energy)))
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.solar_energy));
                    else
                        binding.solarEnergyCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.none)))
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.none));
                    else
                        binding.noneCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.other_specify))) {
                        setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, getString(R.string.other_specify));
                        Context tempContext;

                        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
                            configuration.setLocale(new Locale("en"));
                            tempContext = requireContext().createConfigurationContext(configuration);
                        } else {
                            tempContext = requireContext();
                        }

                        try {
                            String mainSourceOfLighting = "";
                            JSONArray jsonArray = new JSONArray(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String element = jsonArray.getString(i);
                                if (element.contains(tempContext.getString(R.string.other_specify)) && !element.equalsIgnoreCase("-")) {
                                    mainSourceOfLighting = jsonArray.getString(i);
                                    mainSourceOfLighting = mainSourceOfLighting.substring(mainSourceOfLighting.indexOf(":") + 2);
                                }
                            }

                            if (!mainSourceOfLighting.equalsIgnoreCase("-"))
                                binding.otherSourcesOfLightingLayout.getEditText().setText(mainSourceOfLighting);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else
                        binding.otherSourceOfLightingCheckbox.setChecked(false);
                }

                if (name.equalsIgnoreCase("mainDrinkingWaterSource")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.piped_into_dwelling)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.piped_into_dwelling));
                    else
                        binding.pipedIntoDwellingCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.piped_into_yard_plot)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.piped_into_yard_plot));
                    else
                        binding.pipedIntoYardPlotCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.public_tap_standpipe)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.public_tap_standpipe));
                    else
                        binding.publicTapStandpipeCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.tube_well_borehole)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.tube_well_borehole));
                    else
                        binding.tubeWellBoreholeCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.protected_well_checkbox)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.protected_well_checkbox));
                    else
                        binding.protectedWellCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.unprotected_well)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.unprotected_well));
                    else
                        binding.unprotectedWellCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.protected_spring)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.protected_spring));
                    else
                        binding.protectedSpringCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.unprotected_spring)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.unprotected_spring));
                    else
                        binding.unprotectedSpringCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.rainwater)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.rainwater));
                    else
                        binding.rainwaterCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.tanker_truck)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.tanker_truck));
                    else
                        binding.tankerTruckCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.cart_with_small_tank)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.cart_with_small_tank));
                    else
                        binding.cartWithSmallTankCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.surface_water)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.surface_water));
                    else
                        binding.surfaceWaterCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.common_hand_pump)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.common_hand_pump));
                    else
                        binding.commonHandPumpCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.hand_pump_at_home)))
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.hand_pump_at_home));
                    else
                        binding.handPumpAtHomeCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.other_specify))) {
                        setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout, getString(R.string.other_specify));

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
                                if (element.contains(tempContext.getString(R.string.other_specify)) && !element.equalsIgnoreCase("-")) {
                                    otherSourceOfWater = jsonArray.getString(i);
                                    otherSourceOfWater = otherSourceOfWater.substring(otherSourceOfWater.indexOf(":") + 2);
                                }
                            }

                            if (!otherSourceOfWater.equalsIgnoreCase("-"))
                                binding.otherSourcesOfDrinkingWaterLayout.getEditText().setText(otherSourceOfWater);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else
                        binding.otherSourceOfWaterCheckbox.setChecked(false);
                }

                if (name.equalsIgnoreCase("saferWaterProcess")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.boil)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.boil));
                    else
                        binding.boilCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.use_alum)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.use_alum));
                    else
                        binding.useAlumCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.add_bleach_chlorine_tablets_drops)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.add_bleach_chlorine_tablets_drops));
                    else
                        binding.addBleachChlorineTabletsDropsCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.strain_through_a_cloth)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.strain_through_a_cloth));
                    else
                        binding.strainThroughAClothCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.use_water_filter_ceramic_sand_composite_etc)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.use_water_filter_ceramic_sand_composite_etc));
                    else
                        binding.useWaterFilterCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.use_electronic_purifier)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.use_electronic_purifier));
                    else
                        binding.useElectronicPurifierCheckbox.setChecked(false);
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.no_measures_taken_for_purification_drinking_as_it_is)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.no_measures_taken_for_purification_drinking_as_it_is));
                    else
                        binding.otherSourceOfWaterNoMeasuresTakenForPurificationDrinkingAsItIs.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.let_it_stand_and_settle)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.let_it_stand_and_settle));
                    else
                        binding.letItStandAndSettleCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.not_treated)))
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.not_treated));
                    else
                        binding.notTreatedCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.other_specify))) {
                        setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout, getString(R.string.other_specify));

                        Context tempContext;

                        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
                            configuration.setLocale(new Locale("en"));
                            tempContext = requireContext().createConfigurationContext(configuration);
                        } else {
                            tempContext = requireContext();
                        }

                        try {
                            String otherSourceOfPurifyingWater = "";
                            JSONArray jsonArray = new JSONArray(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String element = jsonArray.getString(i);
                                if (element.contains(tempContext.getString(R.string.other_specify)) && !element.equalsIgnoreCase("-")) {
                                    otherSourceOfPurifyingWater = jsonArray.getString(i);
                                    otherSourceOfPurifyingWater = otherSourceOfPurifyingWater.substring(otherSourceOfPurifyingWater.indexOf(":") + 2);
                                }
                            }

                            if (!otherSourceOfPurifyingWater.equalsIgnoreCase("-"))
                                binding.otherWaysOfPurifyingWaterEditText.getEditText().setText(otherSourceOfPurifyingWater);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else
                        binding.otherWaysOfPurifyingWaterCheckbox.setChecked(false);
                }

                if (name.equalsIgnoreCase("householdToiletFacility")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.flush_to_piped_sewer_system)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.flush_to_piped_sewer_system));
                    else
                        binding.flushToPipedSewerSystemCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.flush_to_septic_tank)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.flush_to_septic_tank));
                    else
                        binding.flushToSepticTankCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.flush_to_pit_latrine)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.flush_to_pit_latrine));
                    else
                        binding.flushToPitLatrineCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.flush_to_somewhere_else)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.flush_to_somewhere_else));
                    else
                        binding.flushToSomewhereElseCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.flush_dont_know_where)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.flush_dont_know_where));
                    else
                        binding.flushDontKnowWhereCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.ventilated_improved_pit_biogas_latrine)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.ventilated_improved_pit_biogas_latrine));
                    else
                        binding.ventilatedImprovedPitCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.pit_latrine_with_slab)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.pit_latrine_with_slab));
                    else
                        binding.pitLatrineWithSlabCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.pit_latrine_without_slab_open_pit)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.pit_latrine_without_slab_open_pit));
                    else
                        binding.pitLatrineWithoutSlabCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.twin_pit_composting_toilet)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.twin_pit_composting_toilet));
                    else
                        binding.twinPitCompostingToiletCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.dry_toilet)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.dry_toilet));
                    else
                        binding.dryToiletCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.communal_toilet)))
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.communal_toilet));
                    else
                        binding.communalToiletCheckbox.setChecked(false);

                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null && (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))).contains(updatedContext.getString(R.string.no_facility_uses_open_space_or_field))) {
                        setSelectedCheckboxes(binding.familyToiletFacilityCheckboxLinearLayout, getString(R.string.no_facility_uses_open_space_or_field));
                    } else
                        binding.noFacilityUsesOpenFieldCheckbox.setChecked(false);

                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

    private void setSelectedCheckboxes(ViewGroup viewGroup, String s) {
        if (viewGroup == null)
            return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof CheckBox && ((CheckBox) childAt).getText().toString().equalsIgnoreCase(s)) {
                ((CheckBox) childAt).setChecked(true);
            }
        }
    }
}
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentFifthScreenBinding;
import org.intelehealth.app.databinding.FragmentSixthScreenBinding;
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
 * Use the {@link SixthScreenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SixthScreenFragment extends Fragment {

    private FragmentSixthScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;
    private List<View> mandatoryFields = new ArrayList<>();
    PatientsDAO patientsDAO = new PatientsDAO();

    public SixthScreenFragment() {
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
        binding = FragmentSixthScreenBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.prevButton.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });
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

        mandatoryFields.addAll(Arrays.asList(binding.defecationInOpenRadioGroup));
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

        //householdOpenDefecationStatus
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdOpenDefecationStatus"));
        String householdOpenDefecationStatus = "-";

        if (binding.defecationInOpenRadioGroup.getCheckedRadioButtonId() != -1) {
            householdOpenDefecationStatus = StringUtils.getPreTerm(
                    ((RadioButton) binding.defecationInOpenRadioGroup.findViewById(binding.defecationInOpenRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage()
            );
        }

        patientAttributesDTO.setValue(householdOpenDefecationStatus);
        patientAttributesDTOList.add(patientAttributesDTO);

        //foodItemsPreparedInTwentyFourHrs
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("foodItemsPreparedInTwentyFourHrs"));
        patientAttributesDTO.setValue(StringUtils.getSelectedCheckboxes(binding.foodPreparedInThePastTwentyFourHoursLinearLayout, sessionManager.getAppLanguage(), getContext(), ""));
        patientAttributesDTOList.add(patientAttributesDTO);


        Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("screen", "secondscreen: \n" + gson.toJson(patientAttributesDTOList));

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
//                    .replace(R.id.framelayout_container, new SeventhScreenFragment())
//                    .commit();
//        }
        getFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new SeventhScreenFragment())
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
                if (name.equalsIgnoreCase("householdOpenDefecationStatus")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (value1.equalsIgnoreCase(updatedContext.getString(R.string.yes)))
                            binding.defecationYesRadioButton.setChecked(true);
                        else if (value1.equalsIgnoreCase(updatedContext.getString(R.string.no)))
                            binding.defecationNoRadioButton.setChecked(true);
                    }
                }
                if (name.equalsIgnoreCase("foodItemsPreparedInTwentyFourHrs")) {
                    if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")) != null) {

                        if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")).contains(updatedContext.getString(R.string.starch_staple_food)))
                            binding.starchStapleFoodCheckbox.setChecked(true);

                        if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")).contains(updatedContext.getString(R.string.beans_and_peas)))
                            binding.beansAndPeasCheckbox.setChecked(true);

                        if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")).contains(updatedContext.getString(R.string.nuts_and_seeds)))
                            binding.nutsAndSeedsCheckbox.setChecked(true);

                        if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")).contains(updatedContext.getString(R.string.dairy)))
                            binding.dairyCheckbox.setChecked(true);

                        if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")).contains(updatedContext.getString(R.string.eggs)))
                            binding.eggsCheckbox.setChecked(true);

                        if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")).contains(updatedContext.getString(R.string.flesh_food)))
                            binding.fleshFoodCheckbox.setChecked(true);

                        if (idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")).contains(updatedContext.getString(R.string.any_vegetables)))
                            binding.anyVegetablesCheckbox.setChecked(true);
                    }
                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

    void defaultSelectRB(RadioGroup radioGroup, String s) {
        int childCount = radioGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            try {
                RadioButton rButton = (RadioButton) radioGroup.getChildAt(i);
                if (rButton.getText().toString().equalsIgnoreCase(s)) {
                    rButton.setChecked(true);
                    return;
                }
            } catch (Exception e) {

            }
        }
    }
}
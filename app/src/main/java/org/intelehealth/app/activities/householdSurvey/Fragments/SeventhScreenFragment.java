package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.databinding.FragmentSeventhScreenBinding;
import org.intelehealth.app.databinding.FragmentSixthScreenBinding;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;

public class SeventhScreenFragment extends Fragment {

    private FragmentSeventhScreenBinding binding;
    private String patientUuid;
    private SessionManager sessionManager;
    private List<View> mandatoryFields = new ArrayList<>();
    PatientsDAO patientsDAO = new PatientsDAO();

    public SeventhScreenFragment() {
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
        binding = FragmentSeventhScreenBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        binding.prevButton.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    insertData();
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }
        });

        mandatoryFields.addAll(Arrays.asList(binding.distanceToSubCentreRadioGroup, binding.distanceToNearestPrimaryHealthCentresRadioGroup, binding.distanceToNearestCommunityHealthCentresRadioGroup, binding.distanceToNearestDistrictHospitalRadioGroup, binding.distanceToNearestPathologicalLabRadioGroup, binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup, binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup, binding.distanceToNearestTertiaryCareFacilityRadioGroup));

        getPatientUuidsForHouseholdValue(patientUuid);
        // setData(patientUuid);
        return rootView;
    }

    public void getPatientUuidsForHouseholdValue(String patientUuid) {
        // Getting the household value and then getting all the Patientuuids listed to it so that we
        // can insert all of this data into each of them.
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

        //subCentreDistance
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("subCentreDistance"));
        String subCentreDistance = "-";

        if (binding.distanceToSubCentreRadioGroup.getCheckedRadioButtonId() != -1) {
            subCentreDistance = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToSubCentreRadioGroup.findViewById(binding.distanceToSubCentreRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(subCentreDistance);
        patientAttributesDTOList.add(patientAttributesDTO);

        //nearestPrimaryHealthCenterDistance
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPrimaryHealthCenterDistance"));
        String nearestPrimaryHealthCenterDistance = "-";

        if (binding.distanceToNearestPrimaryHealthCentresRadioGroup.getCheckedRadioButtonId() != -1) {
            nearestPrimaryHealthCenterDistance = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToNearestPrimaryHealthCentresRadioGroup.findViewById(binding.distanceToNearestPrimaryHealthCentresRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(nearestPrimaryHealthCenterDistance);
        patientAttributesDTOList.add(patientAttributesDTO);

        //nearestCommunityHealthCenterDistance
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestCommunityHealthCenterDistance"));
        String nearestCommunityHealthCenterDistance = "-";

        if (binding.distanceToNearestCommunityHealthCentresRadioGroup.getCheckedRadioButtonId() != -1) {
            nearestCommunityHealthCenterDistance = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToNearestCommunityHealthCentresRadioGroup.findViewById(binding.distanceToNearestCommunityHealthCentresRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(nearestCommunityHealthCenterDistance);
        patientAttributesDTOList.add(patientAttributesDTO);

        //nearestDistrictHospitalDistance
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestDistrictHospitalDistance"));
        String nearestDistrictHospitalDistance = "-";

        if (binding.distanceToNearestDistrictHospitalRadioGroup.getCheckedRadioButtonId() != -1) {
            nearestDistrictHospitalDistance = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToNearestDistrictHospitalRadioGroup.findViewById(binding.distanceToNearestDistrictHospitalRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(nearestDistrictHospitalDistance);
        patientAttributesDTOList.add(patientAttributesDTO);

        //nearestPathologicalLabDistance
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPathologicalLabDistance"));
        String nearestPathologicalLabDistance = "-";

        if (binding.distanceToNearestPathologicalLabRadioGroup.getCheckedRadioButtonId() != -1) {
            nearestPathologicalLabDistance = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToNearestPathologicalLabRadioGroup.findViewById(binding.distanceToNearestPathologicalLabRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(nearestPathologicalLabDistance);
        patientAttributesDTOList.add(patientAttributesDTO);

        //nearestPrivateClinicMBBSDoctor
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPrivateClinicMBBSDoctor"));
        String nearestPrivateClinicMBBSDoctor = "-";

        if (binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.getCheckedRadioButtonId() != -1) {
            nearestPrivateClinicMBBSDoctor = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.findViewById(binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(nearestPrivateClinicMBBSDoctor);
        patientAttributesDTOList.add(patientAttributesDTO);

        //nearestPrivateClinicAlternateMedicine
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPrivateClinicAlternateMedicine"));
        String nearestPrivateClinicAlternateMedicine = "-";

        if (binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup.getCheckedRadioButtonId() != -1) {
            nearestPrivateClinicAlternateMedicine = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup.findViewById(binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(nearestPrivateClinicAlternateMedicine);
        patientAttributesDTOList.add(patientAttributesDTO);

        //nearestTertiaryCareFacility
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestTertiaryCareFacility"));
        String nearestTertiaryCareFacility = "-";

        if (binding.distanceToNearestTertiaryCareFacilityRadioGroup.getCheckedRadioButtonId() != -1) {
            nearestTertiaryCareFacility = StringUtils.getDistanceTranslations(
                    ((RadioButton) binding.distanceToNearestTertiaryCareFacilityRadioGroup.findViewById(binding.distanceToNearestTertiaryCareFacilityRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    sessionManager.getAppLanguage(),
                    requireContext()
            );
        }

        patientAttributesDTO.setValue(nearestTertiaryCareFacility);
        patientAttributesDTOList.add(patientAttributesDTO);

        //ReportDate of survey started	e9b991df-6791-4787-9664-ef348d523f64
        //ReportDate of survey ended	adc6351a-3d19-40b7-b765-fae50dc49a7a
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ReportDate of survey submitted"));

        patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTimeFormat());
//        patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());
        patientAttributesDTOList.add(patientAttributesDTO);

        Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("screen", "secondscreen: \n" + gson.toJson(patientAttributesDTOList));

        // TODO: this logic just for testing purpose have added here. Once all screens is done than at the end of 7th screen
        //  by clicking on SUBMIT button add this code on that button clicklistener...
        boolean isPatientUpdated = patientsDAO.SurveyupdatePatientToDB(patientUuid, patientAttributesDTOList);
        if (NetworkConnection.isOnline(getActivity().getApplication())) {
            SyncDAO syncDAO = new SyncDAO();
            boolean ispush = syncDAO.pushDataApi();

        }
        // Upto here so that data is stored in localdb and pushed by clicking on FAB...

        if (isPatientUpdated) {
            MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(getActivity());
            alertDialog.setTitle(getActivity().getResources().getString(R.string.surveyDialogTitle));
            alertDialog.setMessage(getActivity().getResources().getString(R.string.surveyDialogMessage));
            alertDialog.setPositiveButton(getActivity().getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("hasPrescription", "false");
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = alertDialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
            IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), dialog);
        }
    }

    private void setData(String patientUuid) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        Context updatedContext;
        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            updatedContext = requireContext().createConfigurationContext(configuration);
        } else {
            updatedContext = requireContext();
        }

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
                if (name.equalsIgnoreCase("subCentreDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_5_minutes).equalsIgnoreCase(value1))
                            binding.subCenter5Minutes.setChecked(true);

                        if (updatedContext.getString(R.string.five_fifteen_minutes).equalsIgnoreCase(value1))
                            binding.subCenter515Minutes.setChecked(true);

                        if (updatedContext.getString(R.string.fifteen_thirty_minutes).equalsIgnoreCase(value1))
                            binding.subCenter1530Minutes.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_thirty_minutes).equalsIgnoreCase(value1))
                            binding.subCenterMoreThan30Minutes.setChecked(true);
                    }
                }

                if (name.equalsIgnoreCase("nearestPrimaryHealthCenterDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_1_km).equalsIgnoreCase(value1))
                            binding.phc1Km.setChecked(true);

                        if (updatedContext.getString(R.string.one_to_three_kms).equalsIgnoreCase(value1))
                            binding.phc13Km.setChecked(true);

                        if (updatedContext.getString(R.string.three_to_five_kms).equalsIgnoreCase(value1))
                            binding.phc35Km.setChecked(true);

                        if (updatedContext.getString(R.string.five_to_ten_kms).equalsIgnoreCase(value1))
                            binding.phc510Km.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_ten_kms).equalsIgnoreCase(value1))
                            binding.phcMoreThan10Km.setChecked(true);
                    }
                }

                if (name.equalsIgnoreCase("nearestCommunityHealthCenterDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_5_kms).equalsIgnoreCase(value1))
                            binding.chcWithin5Kms.setChecked(true);

                        if (updatedContext.getString(R.string.five_to_ten_kms).equalsIgnoreCase(value1))
                            binding.chc510Kms.setChecked(true);

                        if (updatedContext.getString(R.string.ten_to_twenty_kms).equalsIgnoreCase(value1))
                            binding.chc1020Kms.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_twenty_km).equalsIgnoreCase(value1))
                            binding.chcMoreThan20Kms.setChecked(true);
                    }
                }

                if (name.equalsIgnoreCase("nearestDistrictHospitalDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_ten_km).equalsIgnoreCase(value1))
                            binding.dhWithin10Kms.setChecked(true);

                        if (updatedContext.getString(R.string.ten_to_twenty_kms).equalsIgnoreCase(value1))
                            binding.dh1020Kms.setChecked(true);

                        if (updatedContext.getString(R.string.twenty_to_forty_km).equalsIgnoreCase(value1))
                            binding.dh2040Kms.setChecked(true);

                        if (updatedContext.getString(R.string.fifty_to_seventy_km).equalsIgnoreCase(value1))
                            binding.dh5070Kms.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_seventy_km).equalsIgnoreCase(value1))
                            binding.dhMoreThan70Kms.setChecked(true);
                    }
                }

                if (name.equalsIgnoreCase("nearestPathologicalLabDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_ten_km).equalsIgnoreCase(value1))
                            binding.plWithin10Km.setChecked(true);

                        if (updatedContext.getString(R.string.ten_to_twenty_kms).equalsIgnoreCase(value1))
                            binding.pl1020Km.setChecked(true);

                        if (updatedContext.getString(R.string.twenty_to_forty_km).equalsIgnoreCase(value1))
                            binding.pl2040Km.setChecked(true);

                        if (updatedContext.getString(R.string.fifty_to_seventy_km).equalsIgnoreCase(value1))
                            binding.pl5070Km.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_seventy_km).equalsIgnoreCase(value1))
                            binding.plMoreThan70Km.setChecked(true);
                    }
                }

                if (name.equalsIgnoreCase("nearestPrivateClinicMBBSDoctor")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_5_kms).equalsIgnoreCase(value1))
                            binding.pcWithin5Km.setChecked(true);

                        if (updatedContext.getString(R.string.five_to_ten_kms).equalsIgnoreCase(value1))
                            binding.pc510Km.setChecked(true);

                        if (updatedContext.getString(R.string.ten_to_twenty_kms).equalsIgnoreCase(value1))
                            binding.pc1020Km.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_twenty_km).equalsIgnoreCase(value1))
                            binding.pcMoreThan20Km.setChecked(true);
                    }
                }

                if (name.equalsIgnoreCase("nearestPrivateClinicAlternateMedicine")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_5_kms).equalsIgnoreCase(value1))
                            binding.alternateWithin5Km.setChecked(true);

                        if (updatedContext.getString(R.string.five_to_ten_kms).equalsIgnoreCase(value1))
                            binding.alternate510Km.setChecked(true);

                        if (updatedContext.getString(R.string.ten_to_twenty_kms).equalsIgnoreCase(value1))
                            binding.alternate1020Km.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_twenty_km).equalsIgnoreCase(value1))
                            binding.alternateMoreThan20Km.setChecked(true);
                    }
                }

                if (name.equalsIgnoreCase("nearestTertiaryCareFacility")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null) {
                        if (updatedContext.getString(R.string.within_5_kms).equalsIgnoreCase(value1))
                            binding.tertiaryWithin5Km.setChecked(true);

                        if (updatedContext.getString(R.string.five_to_ten_kms).equalsIgnoreCase(value1))
                            binding.tertiary510Km.setChecked(true);

                        if (updatedContext.getString(R.string.ten_to_twenty_kms).equalsIgnoreCase(value1))
                            binding.tertiary1020Km.setChecked(true);

                        if (updatedContext.getString(R.string.twenty_to_thirty_kms).equalsIgnoreCase(value1))
                            binding.tertiary2030Km.setChecked(true);

                        if (updatedContext.getString(R.string.more_than_thirty_kms).equalsIgnoreCase(value1))
                            binding.tertiaryMoreThan30Km.setChecked(true);
                    }
                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

    void defaultSelectRB(RadioGroup radioGroup, String s) {
        int childCount = radioGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            RadioButton rButton = (RadioButton) radioGroup.getChildAt(i);
            if (rButton.getText().toString().equalsIgnoreCase(s)) {
                rButton.setChecked(true);
                return;
            }

        }
    }
}
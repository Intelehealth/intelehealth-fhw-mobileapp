package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

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

import java.util.ArrayList;
import java.util.Arrays;
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
        setData(patientUuid);
        return rootView;
    }

    private void insertData() throws DAOException {

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();

        //subCentreDistance
        if (binding.distanceToSubCentreRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("subCentreDistance"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToSubCentreRadioGroup.findViewById(binding.distanceToSubCentreRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //nearestPrimaryHealthCenterDistance
        if (binding.distanceToNearestPrimaryHealthCentresRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPrimaryHealthCenterDistance"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToNearestPrimaryHealthCentresRadioGroup.findViewById(binding.distanceToNearestPrimaryHealthCentresRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //nearestCommunityHealthCenterDistance
        if (binding.distanceToNearestCommunityHealthCentresRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestCommunityHealthCenterDistance"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToNearestCommunityHealthCentresRadioGroup.findViewById(binding.distanceToNearestCommunityHealthCentresRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //nearestDistrictHospitalDistance
        if (binding.distanceToNearestDistrictHospitalRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestDistrictHospitalDistance"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToNearestDistrictHospitalRadioGroup.findViewById(binding.distanceToNearestDistrictHospitalRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //nearestPathologicalLabDistance
        if (binding.distanceToNearestPathologicalLabRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPathologicalLabDistance"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToNearestPathologicalLabRadioGroup.findViewById(binding.distanceToNearestPathologicalLabRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //nearestPrivateClinicMBBSDoctor
        if (binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPrivateClinicMBBSDoctor"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.findViewById(binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //nearestPrivateClinicAlternateMedicine
        if (binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestPrivateClinicAlternateMedicine"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup.findViewById(binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        //nearestTertiaryCareFacility
        if (binding.distanceToNearestTertiaryCareFacilityRadioGroup.getCheckedRadioButtonId() != -1) {
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nearestTertiaryCareFacility"));
            patientAttributesDTO.setValue(((RadioButton) binding.distanceToNearestTertiaryCareFacilityRadioGroup.findViewById(binding.distanceToNearestTertiaryCareFacilityRadioGroup.getCheckedRadioButtonId())).getText().toString());
            patientAttributesDTOList.add(patientAttributesDTO);
        }

        Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("screen", "secondscreen: \n"+ gson.toJson(patientAttributesDTOList));

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
                if (name.equalsIgnoreCase("subCentreDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToSubCentreRadioGroup, value1);

                }
                if (name.equalsIgnoreCase("nearestPrimaryHealthCenterDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToNearestPrimaryHealthCentresRadioGroup, value1);
                }
                if (name.equalsIgnoreCase("nearestCommunityHealthCenterDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToNearestCommunityHealthCentresRadioGroup, value1);
                }
                if (name.equalsIgnoreCase("nearestDistrictHospitalDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToNearestDistrictHospitalRadioGroup, value1);
                }
                if (name.equalsIgnoreCase("nearestPathologicalLabDistance")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToNearestPathologicalLabRadioGroup, value1);
                }
                if (name.equalsIgnoreCase("nearestPrivateClinicMBBSDoctor")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup, value1);
                }
                if (name.equalsIgnoreCase("nearestPrivateClinicAlternateMedicine")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToNearestPrivateClinicWithAlternateMedicalPractitionersRadioGroup, value1);
                }
                if (name.equalsIgnoreCase("nearestTertiaryCareFacility")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if(value1!=null)
                        defaultSelectRB(binding.distanceToNearestTertiaryCareFacilityRadioGroup, value1);
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
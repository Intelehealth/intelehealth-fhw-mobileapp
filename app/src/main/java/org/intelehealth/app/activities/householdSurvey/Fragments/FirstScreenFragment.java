package org.intelehealth.app.activities.householdSurvey.Fragments;

/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;
import static org.intelehealth.app.utilities.StringUtils.en__as_dob;
import static org.intelehealth.app.utilities.StringUtils.getPethBlock;
import static org.intelehealth.app.utilities.StringUtils.getPethBlockVillage;
import static org.intelehealth.app.utilities.StringUtils.getPethBlockVillage_edit;
import static org.intelehealth.app.utilities.StringUtils.getPethBlock_edit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FirstScreenFragment extends Fragment implements View.OnClickListener {
    EditText nameInvestigator, villageSurvey, blockSurvey, districtSurvey, namePerson, householdNumber;
    //    EditText dateofVisit;
    RadioButton kuchaRadioButton, puccaRadioButton;
    RadioButton availableAccepted, availableDeferred, notavailableSurvey, notavailableSecondVisit, notavailableThirdVisit, refusedParticipate;
    ImageButton next_button;
    SessionManager sessionManager = null;
    String patientUuid, mhouseStructure, mresultVisit;
    private DatePickerDialog mDOBPicker;
    Calendar dob = Calendar.getInstance();
    Calendar today = Calendar.getInstance();
    private int mDOBYear;
    private int mDOBMonth;
    private int mDOBDay;
    private static final String TAG = FirstScreenFragment.class.getSimpleName();
    private List<View> mandatoryFields = new ArrayList<>();
    PatientsDAO patientsDAO = new PatientsDAO();
    RadioGroup household_structure_radio_group;
    RadioGroup result_of_visit_radio_group;


    public FirstScreenFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_first_screen, container, false);
        initUI(rootView);
        radioButtonClickListener();
        getResult_HouseStructure_RadioButtonCheckedValue();
        getPatientUuidsForHouseholdValue(patientUuid);
        setData(patientUuid);
        return rootView;
    }

    private void getResult_HouseStructure_RadioButtonCheckedValue() {
        // House Structure -> Start
        if (kuchaRadioButton.isChecked()) {
            mhouseStructure = "Kucha";
        } else if (puccaRadioButton.isChecked()) {
            mhouseStructure = "Pucca";
        }
        // House Structure -> End

        // Result of Visit -> Start
        if (availableAccepted.isChecked()) {
            mresultVisit = "available and accepted";
        } else if (availableDeferred.isChecked()) {
            mresultVisit = "available and deferred";
        } else if (notavailableSurvey.isChecked()) {
            mresultVisit = "Not available on Survey";
        } else if (notavailableSecondVisit.isChecked()) {
            mresultVisit = "Not available on second visit";
        } else if (notavailableThirdVisit.isChecked()) {
            mresultVisit = "Not available on third visit";
        } else if (refusedParticipate.isChecked()) {
            mresultVisit = "Refused to Participate";
        }
        // Result of Visit -> End...

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

    private void radioButtonClickListener() {
        kuchaRadioButton.setOnClickListener(this);
        puccaRadioButton.setOnClickListener(this);
        // result of visit
        availableAccepted.setOnClickListener(this);
        availableDeferred.setOnClickListener(this);
        notavailableSurvey.setOnClickListener(this);
        notavailableSecondVisit.setOnClickListener(this);
        notavailableThirdVisit.setOnClickListener(this);
        refusedParticipate.setOnClickListener(this);
        // next button
        next_button.setOnClickListener(this);

        mDOBYear = today.get(Calendar.YEAR);
        mDOBMonth = today.get(Calendar.MONTH);
        mDOBDay = today.get(Calendar.DAY_OF_MONTH);

        mDOBPicker = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        //Set the DOB calendar to the date selected by the user
                        dob.set(year, monthOfYear, dayOfMonth);
//                        dateofVisit.setError(null);
                        //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                        // Locale.setDefault(Locale.ENGLISH);
                        //Formatted so that it can be read the way the user sets
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                        dob.set(year, monthOfYear, dayOfMonth);
                        String dobString = simpleDateFormat.format(dob.getTime());
                        // dob_indexValue = monthOfYear; //fetching the inex value of month selected...

//                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                            String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
//                            String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
//                            String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
//                            String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
//                            String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
//                            String dob_text = en__te_dob(dobString); //to show text of English into telugu...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
//                            String dob_text = en__mr_dob(dobString); //to show text of English into telugu...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
//                            String dob_text = en__as_dob(dobString); //to show text of English into telugu...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
//                            String dob_text = en__ml_dob(dobString); //to show text of English into telugu...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
//                            String dob_text = en__kn_dob(dobString); //to show text of English into telugu...
//                            dateofVisit.setText(dob_text);
//                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
//                            String dob_text = en__ru_dob(dobString); //to show text of English into telugu...
//                            dateofVisit.setText(dob_text);
//                        } else {
//                            dateofVisit.setText(dobString);
//                        }
                        mDOBYear = year;
                        mDOBMonth = monthOfYear;
                        mDOBDay = dayOfMonth;
                    }
                }, mDOBYear, mDOBMonth, mDOBDay);


        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
//        dateofVisit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDOBPicker.show();
//            }
//        });
    }

    @Override
    public void onClick(View view) {
        boolean checked = false;
        if (view.getId() != R.id.next_button)
            checked = ((RadioButton) view).isChecked();

        int id = view.getId();
        if (id == R.id.next_button) {
            try {
                insertData();
            } catch (DAOException e) {
                e.printStackTrace();
            }

            if (checked)
                mhouseStructure = "Kucha";
            Log.v(TAG, "structure:" + mhouseStructure);
        } else if (id == R.id.kuchaRadioButton) {
            if (checked)
                mhouseStructure = "Kucha";
            Log.v(TAG, "structure:" + mhouseStructure);
        } else if (id == R.id.puccaRadioButton) {
            if (checked)
                mhouseStructure = "Pucca";
            Log.v(TAG, "structure:" + mhouseStructure);
        } else if (id == R.id.availableAccepted) {
            if (checked)
                mresultVisit = "available and accepted";
            Log.v(TAG, "resultVisit:" + mresultVisit);
        } else if (id == R.id.availableDeferred) {
            if (checked)
                mresultVisit = "available and deferred";
            Log.v(TAG, "resultVisit:" + mresultVisit);
        } else if (id == R.id.notavailableSurvey) {
            if (checked)
                mresultVisit = "Not available on Survey";
            Log.v(TAG, "resultVisit:" + mresultVisit);
        } else if (id == R.id.notavailableSecondVisit) {
            if (checked)
                mresultVisit = "Not available on second visit";
            Log.v(TAG, "resultVisit:" + mresultVisit);
        } else if (id == R.id.notavailableThirdVisit) {
            if (checked)
                mresultVisit = "Not available on third visit";
            Log.v(TAG, "resultVisit:" + mresultVisit);
        } else if (id == R.id.RefusedParticipate) {
            if (checked)
                mresultVisit = "Refused to Participate";
            Log.v(TAG, "resultVisit:" + mresultVisit);
        }
    }

    private void insertData() throws DAOException {
        if (!StringUtils.validateFields(mandatoryFields)) {
            Toast.makeText(getContext(), R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

//        //investigator
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nameOfInvestigator"));
//        patientAttributesDTO.setValue(nameInvestigator.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);

//        //Village name
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("villageNameSurvey"));
//        patientAttributesDTO.setValue(getPethBlockVillage(villageSurvey.getText().toString(),sessionManager.getAppLanguage()));
//        patientAttributesDTOList.add(patientAttributesDTO);

//        //block no
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("blockSurvey"));
////        patientAttributesDTO.setValue(blockSurvey.getText().toString());
//        patientAttributesDTO.setValue(getPethBlock(blockSurvey.getText().toString(),sessionManager.getAppLanguage()));
//        patientAttributesDTOList.add(patientAttributesDTO);

//        //District
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("districtSurvey"));
//        patientAttributesDTO.setValue(districtSurvey.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);

        //Date of Visit
//        patientAttributesDTO = new PatientAttributesDTO();
//        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
//        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("dateOfVisit"));
//        patientAttributesDTO.setValue(dateofVisit.getText().toString());
//        patientAttributesDTOList.add(patientAttributesDTO);

        //ReportDate of survey started	e9b991df-6791-4787-9664-ef348d523f64
        //ReportDate of survey ended	adc6351a-3d19-40b7-b765-fae50dc49a7a
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ReportDate of survey started"));
        patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTimeFormat());
        patientAttributesDTOList.add(patientAttributesDTO);

        // Name of investigator
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nameOfInvestigator"));
        patientAttributesDTO.setValue(sessionManager.getChwname());
        patientAttributesDTOList.add(patientAttributesDTO);

        //Name primary respondent
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NamePrimaryRespondent"));
        patientAttributesDTO.setValue(StringUtils.getValue(namePerson.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        //House no.
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("HouseholdNumber"));
        patientAttributesDTO.setValue(StringUtils.getValue(householdNumber.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        //House Structure
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("HouseStructure"));
        patientAttributesDTO.setValue(StringUtils.getValue(mhouseStructure));
        patientAttributesDTOList.add(patientAttributesDTO);

        // Result of Visit
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ResultOfVisit"));
        patientAttributesDTO.setValue(StringUtils.getValue(mresultVisit));
        patientAttributesDTOList.add(patientAttributesDTO);

        /*Gson gson = new Gson();
        gson.toJson(patientAttributesDTOList);
        Log.v("firstscreen", "firstscreen: \n"+ gson.toJson(patientAttributesDTOList));*/

        Log.v("puuid", "puuid__: " + patientUuid);
        boolean isPatientUpdated = patientsDAO.SurveyupdatePatientToDB(patientUuid, patientAttributesDTOList);

/*
        if (NetworkConnection.isOnline(getActivity().getApplication())) {
            SyncDAO syncDAO = new SyncDAO();
            ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
            boolean ispush = syncDAO.pushDataApi();
            boolean isPushImage = imagesPushDAO.patientProfileImagesPush();
        }
*/

        //  if (isPatientUpdated) {

        if (refusedParticipate.isChecked()) {
            getActivity().finish();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.framelayout_container, new SecondScreenFragment())
                    .addToBackStack(null)
                    .commit();
            //  }
        }
    }

    private void initUI(View rootView) {
//        nameInvestigator = rootView.findViewById(R.id.investigator_name_edit_text);
//        villageSurvey = rootView.findViewById(R.id.villageSurvey);
//        blockSurvey = rootView.findViewById(R.id.block_dropdown);
//        districtSurvey = rootView.findViewById(R.id.districtSurvey);
//        dateofVisit = rootView.findViewById(R.id.dateVisit);
        namePerson = rootView.findViewById(R.id.primary_respondent_name_edit_text);
        householdNumber = rootView.findViewById(R.id.household_number_edit_text);
        kuchaRadioButton = rootView.findViewById(R.id.kuchaRadioButton);
        puccaRadioButton = rootView.findViewById(R.id.puccaRadioButton);
        availableAccepted = rootView.findViewById(R.id.availableAccepted);
        availableDeferred = rootView.findViewById(R.id.availableDeferred);
        notavailableSurvey = rootView.findViewById(R.id.notavailableSurvey);
        notavailableSecondVisit = rootView.findViewById(R.id.notavailableSecondVisit);
        notavailableThirdVisit = rootView.findViewById(R.id.notavailableThirdVisit);
        refusedParticipate = rootView.findViewById(R.id.RefusedParticipate);
        next_button = rootView.findViewById(R.id.next_button);

        household_structure_radio_group = rootView.findViewById(R.id.household_structure_radio_group);
        result_of_visit_radio_group = rootView.findViewById(R.id.result_of_visit_radio_group);
        mandatoryFields.addAll(Arrays.asList(namePerson, householdNumber
                , household_structure_radio_group, result_of_visit_radio_group));

        setMenus();
    }

    private void setMenus() {
//        districtSurvey.setOnClickListener(v -> showMenu(districtSurvey, R.menu.menu_nas_district));
//        blockSurvey.setOnClickListener(v -> showMenu(blockSurvey, R.menu.menu_nas_block));
//        blockSurvey.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                switch (s.toString()) {
//                    case "Peth Block":
//                        villageSurvey.setOnClickListener(v -> showMenu(villageSurvey, R.menu.menu_peth_block_villages));
//                        break;
//
//                    case "Suragana Block":
//                        villageSurvey.setOnClickListener(v -> showMenu(villageSurvey, R.menu.menu_suragana_block_villages));
//
//                        break;
//                    case "पेठ तालुका":
//                        villageSurvey.setOnClickListener(v -> showMenu(villageSurvey, R.menu.menu_peth_block_villages));
//                        break;
//                    case "सुरगाणा तालुका":
//                        villageSurvey.setOnClickListener(v -> showMenu(villageSurvey, R.menu.menu_suragana_block_villages));
//
//                        break;
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
    }

    private void showMenu(EditText editText, @MenuRes Integer menuRes) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), editText);
        popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            editText.setText(item.getTitle());
            if (editText == blockSurvey)
                villageSurvey.setText(null);
            return true;
        });
        popupMenu.show();
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
//                if (name.equalsIgnoreCase("nameOfInvestigator")) {
//                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
//                    if (value1 != null)
//                        nameInvestigator.setText(value1);
//                }
////                if (name.equalsIgnoreCase("villageNameSurvey")) {
//                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
//                    if (value1 != null)
//                        villageSurvey.setText(getPethBlockVillage_edit(value1,sessionManager.getAppLanguage()));
////                        villageSurvey.setText(value1);
//                }
//                if (name.equalsIgnoreCase("blockSurvey")) {
//                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
//                    if (value1 != null)
//                        blockSurvey.setText(getPethBlock_edit(value1,sessionManager.getAppLanguage()));
////                        blockSurvey.setText(value1);
//                }
//                if (name.equalsIgnoreCase("districtSurvey")) {
//                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
//                    if (value1 != null)
//                        districtSurvey.setText(value1);
//                }
//                if (name.equalsIgnoreCase("dateOfVisit")) {
//                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
//                    if (value1 != null)
//                        dateofVisit.setText(value1);
//                }
                if (name.equalsIgnoreCase("NamePrimaryRespondent")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-"))
                        namePerson.setText(value1);
                }
                if (name.equalsIgnoreCase("HouseholdNumber")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    if (value1 != null && !value1.equalsIgnoreCase("-"))
                        householdNumber.setText(value1);
                }
                if (name.equalsIgnoreCase("HouseStructure")) {
                    String value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    mhouseStructure = value1;
                    if (value1 != null && value1.equalsIgnoreCase("Pucca"))
                        puccaRadioButton.setChecked(true);
                    else if (value1 != null && value1.equalsIgnoreCase("Kucha"))
                        kuchaRadioButton.setChecked(true);
                }
                if (name.equalsIgnoreCase("ResultOfVisit")) {
                    String result = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    mresultVisit = result;
                    if (result != null && result.equalsIgnoreCase("available and accepted"))
                        availableAccepted.setChecked(true);
                    if (result != null && result.equalsIgnoreCase("available and deferred"))
                        availableDeferred.setChecked(true);
                    if (result != null && result.equalsIgnoreCase("Not available on Survey"))
                        notavailableSurvey.setChecked(true);
                    if (result != null && result.equalsIgnoreCase("Not available on second visit"))
                        notavailableSecondVisit.setChecked(true);
                    if (result != null && result.equalsIgnoreCase("Not available on third visit"))
                        notavailableThirdVisit.setChecked(true);
                    if (result != null && result.equalsIgnoreCase("Refused to Participate"))
                        refusedParticipate.setChecked(true);
                }
            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }
}
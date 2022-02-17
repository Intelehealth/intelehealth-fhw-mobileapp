package org.intelehealth.app.activities.householdSurvey.Fragments;

/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;
import static org.intelehealth.app.utilities.StringUtils.en__as_dob;
import static org.intelehealth.app.utilities.StringUtils.en__bn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__gu_dob;
import static org.intelehealth.app.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.app.utilities.StringUtils.en__kn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ml_dob;
import static org.intelehealth.app.utilities.StringUtils.en__mr_dob;
import static org.intelehealth.app.utilities.StringUtils.en__or_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ru_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ta_dob;
import static org.intelehealth.app.utilities.StringUtils.en__te_dob;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FirstScreenFragment extends Fragment implements View.OnClickListener{
    EditText nameInvestigator, villageSurvey, blockSurvey, districtSurvey, dateofVisit, namePerson, householdNumber;
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
        View rootView =  inflater.inflate(R.layout.fragment_first_screen, container, false);
        initUI(rootView);
        radioButtonClickListener();
        return rootView;
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
                        dateofVisit.setError(null);
                        //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                        // Locale.setDefault(Locale.ENGLISH);
                        //Formatted so that it can be read the way the user sets
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                        dob.set(year, monthOfYear, dayOfMonth);
                        String dobString = simpleDateFormat.format(dob.getTime());
                       // dob_indexValue = monthOfYear; //fetching the inex value of month selected...

                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                            String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                            dateofVisit.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                            String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                            dateofVisit.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                            String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
                            dateofVisit.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                            String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
                            dateofVisit.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                            String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
                            dateofVisit.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                            String dob_text = en__te_dob(dobString); //to show text of English into telugu...
                            dateofVisit.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                            String dob_text = en__mr_dob(dobString); //to show text of English into telugu...
                            dateofVisit.setText(dob_text);
                        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                            String dob_text = en__as_dob(dobString); //to show text of English into telugu...
                            dateofVisit.setText(dob_text);
                        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                            String dob_text = en__ml_dob(dobString); //to show text of English into telugu...
                            dateofVisit.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                            String dob_text = en__kn_dob(dobString); //to show text of English into telugu...
                            dateofVisit.setText(dob_text);
                        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                            String dob_text = en__ru_dob(dobString); //to show text of English into telugu...
                            dateofVisit.setText(dob_text);
                        } else {
                            dateofVisit.setText(dobString);
                        }
                        mDOBYear = year;
                        mDOBMonth = monthOfYear;
                        mDOBDay = dayOfMonth;
                    }
                }, mDOBYear, mDOBMonth, mDOBDay);


        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        dateofVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        boolean checked = false;
        if(view.getId() != R.id.next_button)
            checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.next_button:
                try {
                    insertData();
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            case R.id.kuchaRadioButton:
                if (checked)
                    mhouseStructure = "Kucha";
                Log.v(TAG, "structure:" + mhouseStructure);
                break;
            case R.id.puccaRadioButton:
                if (checked)
                    mhouseStructure = "Pucca";
                Log.v(TAG, "structure:" + mhouseStructure);
                break;
            case R.id.availableAccepted:
                if (checked)
                    mresultVisit = "available and accepted";
                Log.v(TAG, "resultVisit:" + mresultVisit);
                break;
            case R.id.availableDeferred:
                if (checked)
                    mresultVisit = "available and deferred";
                Log.v(TAG, "resultVisit:" + mresultVisit);
                break;
            case R.id.notavailableSurvey:
                if (checked)
                    mresultVisit = "Not available on Survey";
                Log.v(TAG, "resultVisit:" + mresultVisit);
                break;
            case R.id.notavailableSecondVisit:
                if (checked)
                    mresultVisit = "Not available on second visit";
                Log.v(TAG, "resultVisit:" + mresultVisit);
                break;
            case R.id.notavailableThirdVisit:
                if (checked)
                    mresultVisit = "Not available on third visit";
                Log.v(TAG, "resultVisit:" + mresultVisit);
                break;
            case R.id.RefusedParticipate:
                if (checked)
                    mresultVisit = "Refused to Participate";
                Log.v(TAG, "resultVisit:" + mresultVisit);
                break;
        }
    }

    private void insertData() throws DAOException {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
       // List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

        //investigator
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("nameOfInvestigator"));
        patientAttributesDTO.setValue(nameInvestigator.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //Village name
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("villageNameSurvey"));
        patientAttributesDTO.setValue(villageSurvey.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //block no
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("blockSurvey"));
        patientAttributesDTO.setValue(blockSurvey.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //District
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("districtSurvey"));
        patientAttributesDTO.setValue(districtSurvey.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //Date of Visit
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("dateOfVisit"));
        patientAttributesDTO.setValue(dateofVisit.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //Name primary respondent
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("NamePrimaryRespondent"));
        patientAttributesDTO.setValue(namePerson.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO);

        //House no.
        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(patientUuid); // Intent from PatientDetail screen...
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("HouseholdNumber"));
        patientAttributesDTO.setValue(householdNumber.getText().toString());
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

        Log.v("puuid", "puuid__: "+patientUuid);
      //  boolean isPatientUpdated = patientsDAO.SurveyupdatePatientToDB(patientUuid, patientAttributesDTOList);

/*
        if (NetworkConnection.isOnline(getActivity().getApplication())) {
            SyncDAO syncDAO = new SyncDAO();
            ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
            boolean ispush = syncDAO.pushDataApi();
            boolean isPushImage = imagesPushDAO.patientProfileImagesPush();
        }
*/

      //  if (isPatientUpdated) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.framelayout_container, new SecondScreenFragment())
                    .commit();
      //  }
    }

    private void initUI(View rootView) {
        nameInvestigator = rootView.findViewById(R.id.investigator_name_edit_text);
        villageSurvey = rootView.findViewById(R.id.villageSurvey);
        blockSurvey = rootView.findViewById(R.id.block_dropdown);
        districtSurvey = rootView.findViewById(R.id.districtSurvey);
        dateofVisit = rootView.findViewById(R.id.dateVisit);
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
    }

}
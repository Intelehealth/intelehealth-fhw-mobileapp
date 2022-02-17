package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import static org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity.patientAttributesDTOList;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.Locale;
import java.util.UUID;

public class SecondScreenFragment extends Fragment implements View.OnClickListener{
    EditText nameHOHEditText, noOfSmartPhoneEditText, noOfFeaturePhoneEditText, noOfEarningEditText;
    RadioButton maleHoHRadio, femaleHoHRadio;
    Spinner religionSpinner, casteSpinner;
    ArrayAdapter<CharSequence> religionAdapter, casteAdapter;
    MaterialCheckBox saleCerealCheckbox, saleAnimalsCheckbox, agriLaborCheckbox, salariedWorkerCheckbox, selfEmployedCheckbox,  dailyLaborCheckbox,
    nregaCheckbox, seasonalLaborCheckbox, noPaidWorkCheckbox, pensionCheckbox, remittancesCheckbox, otherCheckbox;
    SessionManager sessionManager = null;
    String patientUuid;
    ImageButton next_button;
    private static final String TAG = SecondScreenFragment.class.getSimpleName();

    public SecondScreenFragment() {
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
        View rootView =  inflater.inflate(R.layout.fragment_second_screen, container, false);
        initUI(rootView);
        ClickListener();
        
       /* ImageButton next_button = rootView.findViewById(R.id.next_button);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.framelayout_container, new ThirdScreenFragment())
                        .commit();
            }
        });*/
        return rootView;
    }

    private void initUI(View rootView) {
       // nameHOHEditText, noOfSmartPhoneEditText, noOfFeaturePhoneEditText, noOfEarningEditText;
        nameHOHEditText = rootView.findViewById(R.id.head_of_household_name_EditText);
        noOfSmartPhoneEditText = rootView.findViewById(R.id.editText_smartphones);
        noOfFeaturePhoneEditText = rootView.findViewById(R.id.editTextFeaturePhone);
        noOfEarningEditText = rootView.findViewById(R.id.editText_earningmember);
        next_button = rootView.findViewById(R.id.next_button);
        // TODO: Similarly init other views

    }

    private void ClickListener() {
        // TODO: InitViews for this below views and then uncomment.
        next_button.setOnClickListener(this);

       /* maleHoHRadio.setOnClickListener(this);
        femaleHoHRadio.setOnClickListener(this);

        religionSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        casteSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        saleCerealCheckbox.setOnClickListener(this);
        saleAnimalsCheckbox.setOnClickListener(this);
        agriLaborCheckbox.setOnClickListener(this);
        salariedWorkerCheckbox.setOnClickListener(this);
        selfEmployedCheckbox.setOnClickListener(this);
        dailyLaborCheckbox.setOnClickListener(this);
        nregaCheckbox.setOnClickListener(this);
        seasonalLaborCheckbox.setOnClickListener(this);
        noPaidWorkCheckbox.setOnClickListener(this);
        pensionCheckbox.setOnClickListener(this);
        remittancesCheckbox.setOnClickListener(this);
        otherCheckbox.setOnClickListener(this);*/

    }

    @Override
    public void onClick(View view) {
        // TODO: InitViews for this then uncomment.
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
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdHeadName"));
        patientAttributesDTO.setValue(nameHOHEditText.getText().toString());
        patientAttributesDTOList.add(patientAttributesDTO); // have set this variable static so we can use its values throughout the screens...

        // TODO: Generate patientattributes for other fieds similarly...


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
            getFragmentManager().beginTransaction()
                    .replace(R.id.framelayout_container, new ThirdScreenFragment())
                    .commit();
        }

    }
}
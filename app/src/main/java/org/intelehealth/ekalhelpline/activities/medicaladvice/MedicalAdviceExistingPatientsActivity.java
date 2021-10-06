package org.intelehealth.ekalhelpline.activities.medicaladvice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalhelpline.activities.patientSurveyActivity.PatientSurveyActivity;
import org.intelehealth.ekalhelpline.activities.privacyNoticeActivity.PrivacyNotice_Activity;
import org.intelehealth.ekalhelpline.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.ekalhelpline.app.AppConstants;
import org.intelehealth.ekalhelpline.database.dao.EncounterDAO;
import org.intelehealth.ekalhelpline.database.dao.ObsDAO;
import org.intelehealth.ekalhelpline.database.dao.VisitAttributeListDAO;
import org.intelehealth.ekalhelpline.database.dao.VisitsDAO;
import org.intelehealth.ekalhelpline.knowledgeEngine.Node;
import org.intelehealth.ekalhelpline.models.dto.EncounterDTO;
import org.intelehealth.ekalhelpline.models.dto.ObsDTO;
import org.intelehealth.ekalhelpline.models.dto.VisitDTO;
import org.intelehealth.ekalhelpline.syncModule.SyncUtils;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.StringUtils;
import org.intelehealth.ekalhelpline.utilities.UuidDictionary;
import org.intelehealth.ekalhelpline.utilities.exception.DAOException;
import org.intelehealth.ekalhelpline.widget.materialprogressbar.CustomProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MedicalAdviceExistingPatientsActivity extends AppCompatActivity {
    private static final String EXTRA_PATIENT_UUID = "EXTRA_PATIENT_UUID";
    private static final String EXTRA_PATIENT_NAME = "EXTRA_PATIENT_NAME";
    SessionManager sessionManager = null;
    String patientUuid, patientName;
    Context context;

    Intent i_privacy;
    String privacy_value;
    CustomProgressDialog cpd;
    private CheckBox chb_agree_privacy, cbVaccineGuide, cbCovidConcern, cbManagingBreathlessness, cbManageVoiceIssue,
            cbManageEating, cbDealProblems, cbMentalHealth, cbExercises, cbOthers;
    private TextView txt_privacy;
    private EditText et_medical_advice_extra, et_medical_advice_additional;
    TextInputLayout curosity_textinputlayout;
    Spinner spinner_curosityResolution;
    ArrayAdapter<CharSequence> adapter_curosityResolution;
    String curosityInfo = "";

    public static void start(Context context, String patientUuid, String patientName) {
        Intent starter = new Intent(context, MedicalAdviceExistingPatientsActivity.class);
        starter.putExtra(EXTRA_PATIENT_UUID, patientUuid);
        starter.putExtra(EXTRA_PATIENT_NAME, patientName);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_advice_existing_patients);
        setTitle(R.string.title_activity_identification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cpd = new CustomProgressDialog(this);
        i_privacy = getIntent();
        context = MedicalAdviceExistingPatientsActivity.this;
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null && intent.hasExtra(EXTRA_PATIENT_UUID)) {
            this.setTitle(getString(R.string.text_medical_advice));
            patientUuid = intent.getStringExtra(EXTRA_PATIENT_UUID);
            patientName = intent.getStringExtra(EXTRA_PATIENT_NAME);

        }

        View llMedicalAdvice = findViewById(R.id.ll_medical_advice);
        llMedicalAdvice.setVisibility(View.VISIBLE);

        cbVaccineGuide = llMedicalAdvice.findViewById(R.id.cbVaccineGuide);
        cbCovidConcern = llMedicalAdvice.findViewById(R.id.cbCovidConcern);
        cbManagingBreathlessness = llMedicalAdvice.findViewById(R.id.cbManagingBreathlessness);
        cbManageVoiceIssue = llMedicalAdvice.findViewById(R.id.cbManageVoiceIssue);
        cbManageEating = llMedicalAdvice.findViewById(R.id.cbManageEating);
        cbDealProblems = llMedicalAdvice.findViewById(R.id.cbDealProblems);
        cbMentalHealth = llMedicalAdvice.findViewById(R.id.cbMentalHealth);
        cbExercises = llMedicalAdvice.findViewById(R.id.cbExercises);
        cbOthers = llMedicalAdvice.findViewById(R.id.cbOthers);
        et_medical_advice_extra = llMedicalAdvice.findViewById(R.id.et_medical_advice_extra); //no use now

        spinner_curosityResolution = llMedicalAdvice.findViewById(R.id.spinner_curosityResolution);
        curosity_textinputlayout = llMedicalAdvice.findViewById(R.id.curosity_textinputlayout);
        et_medical_advice_additional = llMedicalAdvice.findViewById(R.id.et_medical_advice_additional);

        try { // curosity resolution Spinner
            String curosityLanguage = "curosityResolution_array_" + sessionManager.getAppLanguage();
            int curosity = getResources().getIdentifier(curosityLanguage, "array", getApplicationContext().getPackageName());
            if (curosity != 0) {
                adapter_curosityResolution = ArrayAdapter.createFromResource(this,
                        curosity, R.layout.custom_spinner);
            }
            spinner_curosityResolution.setAdapter(adapter_curosityResolution);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        } //curosity resolution Spinner End...


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (patientUuid != null) {
                createMedicalAdviceVisit();
            }
        });

        //curosity start
        spinner_curosityResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected_curosityOption = parent.getItemAtPosition(position).toString();

                if (selected_curosityOption.equalsIgnoreCase("Other") ||
                        selected_curosityOption.equalsIgnoreCase("अन्य")) {
                    curosity_textinputlayout.setVisibility(View.VISIBLE);
                    et_medical_advice_additional.setFocusable(true);
                } else {
                    curosity_textinputlayout.setVisibility(View.GONE);
                    et_medical_advice_additional.setText("");
                    et_medical_advice_additional.setError(null);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //curosity end...

        cbOthers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_medical_advice_extra.setEnabled(true);
                    et_medical_advice_extra.requestFocus();
                } else {
                    et_medical_advice_extra.setText("");
                    et_medical_advice_extra.setEnabled(false);
                }
            }
        });
        chb_agree_privacy = findViewById(R.id.chb_agree_privacy);
        txt_privacy = findViewById(R.id.txt_privacy);
        txt_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrivacyNotice_Activity.start(MedicalAdviceExistingPatientsActivity.this, true);
            }
        });
        if (!TextUtils.isEmpty(patientUuid)) {
            findViewById(R.id.buttons).setVisibility(View.GONE);
        }
    }

    void createMedicalAdviceVisit() {

        //curosity validation - start
        if (spinner_curosityResolution.getSelectedItemPosition() == 0) {
            TextView errorText = (TextView) spinner_curosityResolution.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(getString(R.string.error_field_required));//changes the selected item text to this
            spinner_curosityResolution.requestFocus();
//            focusView = spinner_curosityResolution;
//            cancel = true;
            return;
        }

        //editText validation for Other
        if (spinner_curosityResolution.getSelectedItem().toString().equalsIgnoreCase("Other") ||
                spinner_curosityResolution.getSelectedItem().toString().equalsIgnoreCase("अन्य") ||
                spinner_curosityResolution.getSelectedItem().toString().equalsIgnoreCase("इतर")) {

            if (et_medical_advice_additional.getText().toString().equalsIgnoreCase("")) {
                et_medical_advice_additional.setError(getString(R.string.error_medical_visit_data));
                et_medical_advice_additional.requestFocus();
//                focusView = et_medical_advice_additional;
//                cancel = true;
                return;
            } else {
                et_medical_advice_additional.setError(null);
            }
        }


        //curosity validation - end

        //curosity - start
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                if (spinner_curosityResolution.getSelectedItem().toString().equalsIgnoreCase("अन्य"))
                    curosityInfo = et_medical_advice_additional.getText().toString();
                else
                    curosityInfo = StringUtils.getProvided(spinner_curosityResolution);
            } else {
                if (spinner_curosityResolution.getSelectedItem().toString().equalsIgnoreCase("Other"))
                    curosityInfo = et_medical_advice_additional.getText().toString();
                else
                    curosityInfo = StringUtils.getProvided(spinner_curosityResolution);
            }
        //curosity - end

/*
        if (!cbCovidConcern.isChecked()
                && !cbVaccineGuide.isChecked()
                && !cbCovidConcern.isChecked()
                && !cbManagingBreathlessness.isChecked()
                && !cbManageVoiceIssue.isChecked()
                && !cbManageEating.isChecked()
                && !cbDealProblems.isChecked()
                && !cbMentalHealth.isChecked()
                && !cbExercises.isChecked()
                && !cbOthers.isChecked()
                && TextUtils.isEmpty(et_medical_advice_additional.getText())) {
            Toast.makeText(context, R.string.error_medical_visit_data, Toast.LENGTH_SHORT).show();
            return;
        }
*/

//        if (cbOthers.isChecked() && TextUtils.isEmpty(et_medical_advice_extra.getText())) {
//            Toast.makeText(context, R.string.error_medical_visit_data, Toast.LENGTH_SHORT).show();
//            return;
//        }

       /* if (TextUtils.isEmpty(et_medical_advice_additional.getText())) {
            Toast.makeText(context, R.string.error_medical_visit_data, Toast.LENGTH_SHORT).show();
            return;
        }

        //check if privacy notice is checked
        if (TextUtils.isEmpty(patientUuid) && !chb_agree_privacy.isChecked()) {
            Toast.makeText(context, getString(R.string.please_read_out_privacy_consent_first), Toast.LENGTH_SHORT).show();
            return;
        }*/


        //formats used in databases to store the start & end date
        SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
//        SimpleDateFormat endFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
        Calendar today = Calendar.getInstance();
        today.add(Calendar.MINUTE, -1);
        today.set(Calendar.MILLISECOND, 0);
        Date todayDate = today.getTime();
        String endDate = startFormat.format(todayDate);
        today.add(Calendar.MILLISECOND, (int) -TimeUnit.MINUTES.toMillis(5));
        String startDate = startFormat.format(today.getTime());

        //create & save visit visitUuid & encounter in the DB
        String visitUuid = UUID.randomUUID().toString();
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setEncounterTypeUuid(UuidDictionary.ENCOUNTER_ADULTINITIAL);
        encounterDTO.setEncounterTime(startDate);
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value(getString(R.string.accept));//privacy value added.

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        boolean returning = false;
        sessionManager.setReturning(returning);

        //create & save visit in the DB
        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setUuid(visitUuid);
        visitDTO.setPatientuuid(patientUuid);
        visitDTO.setStartdate(startDate);
//        visitDTO.setEnddate(endDate);
        visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
        visitDTO.setLocationuuid(sessionManager.getLocationUuid());
        visitDTO.setSyncd(false);
        visitDTO.setCreatoruuid(sessionManager.getCreatorID());//static
        VisitsDAO visitsDAO = new VisitsDAO();

        try {
            visitsDAO.insertPatientToDB(visitDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //create & save obs data in the DB
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
        obsDTO.setEncounteruuid(encounterDTO.getUuid());
        obsDTO.setCreator(sessionManager.getCreatorID());

        //append all the selected items to the OBS value
        String insertion = Node.bullet_arrow + "<b>" + "Curiosity Resolution" + "</b>" + ": ";
        if (cbVaccineGuide.isChecked())
            insertion = insertion.concat(Node.next_line + cbVaccineGuide.getText());
        if (cbCovidConcern.isChecked())
            insertion = insertion.concat(Node.next_line + cbCovidConcern.getText());
        if (cbManagingBreathlessness.isChecked())
            insertion = insertion.concat(Node.next_line + cbManagingBreathlessness.getText());
        if (cbManageVoiceIssue.isChecked())
            insertion = insertion.concat(Node.next_line + cbManageVoiceIssue.getText());
        if (cbManageEating.isChecked())
            insertion = insertion.concat(Node.next_line + cbManageEating.getText());
        if (cbDealProblems.isChecked())
            insertion = insertion.concat(Node.next_line + cbDealProblems.getText());
        if (cbMentalHealth.isChecked())
            insertion = insertion.concat(Node.next_line + cbMentalHealth.getText());
        if (cbExercises.isChecked())
            insertion = insertion.concat(Node.next_line + cbExercises.getText());
        if (cbOthers.isChecked())
            insertion = insertion.concat(Node.next_line + String.format("%s: %s", cbOthers.getText(), et_medical_advice_extra.getText()));

       /* if (!TextUtils.isEmpty(et_medical_advice_additional.getText()))
            insertion = insertion.concat(Node.next_line + String.format("%s: %s", getString(R.string.txt_additional_info), et_medical_advice_additional.getText()));*/
        //adding of data...
        if(curosity_textinputlayout.getVisibility() == View.VISIBLE) {
            if (!TextUtils.isEmpty(et_medical_advice_additional.getText().toString())) {
                insertion = insertion.concat(Node.next_line + String.format("%s: %s",
                        getString(R.string.txt_additional_info), curosityInfo));
            }
        }
        else {
            insertion = insertion.concat(Node.next_line + String.format("%s: %s",
                    getString(R.string.txt_additional_info), curosityInfo));
        }

        obsDTO.setValue(insertion);

        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //create & save visit attributes - required for syncing the data
        VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
        try {
            speciality_attributes.insertVisitAttributes(visitUuid, AppConstants.CURIOSITY_RES);
            // speciality_attributes.insertVisitAttributes(visitUuid, " Specialist doctor not needed");
        } catch (DAOException e) {
            e.printStackTrace();
        }
        uploadPatientSurvey(visitUuid);
//        endVisit(visitUuid, patientUuid, endDate);
    }

    private void uploadPatientSurvey(String visitUuid) {
        Intent intent = new Intent(MedicalAdviceExistingPatientsActivity.this, PatientSurveyActivity.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("name", patientName);
        intent.putExtra("tag", "medicalAdvice");
        startActivity(intent);
    }

    private void endVisit(String visitUuid, String patientUuid, String endTime) {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, endTime);
            Toast.makeText(this, R.string.text_advice_created, Toast.LENGTH_SHORT).show();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        new SyncUtils().syncForeground(""); //Sync function will work in foreground of app and
        sessionManager.removeVisitSummary(patientUuid, visitUuid);
        /*setResult(RESULT_OK);
        finish();*/
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
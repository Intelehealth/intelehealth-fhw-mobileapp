package org.intelehealth.vikalphelpline.activities.medicaladvice;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.vikalphelpline.R;
import org.intelehealth.vikalphelpline.activities.homeActivity.HomeActivity;
import org.intelehealth.vikalphelpline.activities.privacyNoticeActivity.PrivacyNotice_Activity;
import org.intelehealth.vikalphelpline.app.AppConstants;
import org.intelehealth.vikalphelpline.database.dao.EncounterDAO;
import org.intelehealth.vikalphelpline.database.dao.ObsDAO;
import org.intelehealth.vikalphelpline.database.dao.VisitAttributeListDAO;
import org.intelehealth.vikalphelpline.database.dao.VisitsDAO;
import org.intelehealth.vikalphelpline.knowledgeEngine.Node;
import org.intelehealth.vikalphelpline.models.dto.EncounterDTO;
import org.intelehealth.vikalphelpline.models.dto.ObsDTO;
import org.intelehealth.vikalphelpline.models.dto.VisitDTO;
import org.intelehealth.vikalphelpline.syncModule.SyncUtils;
import org.intelehealth.vikalphelpline.utilities.SessionManager;
import org.intelehealth.vikalphelpline.utilities.UuidDictionary;
import org.intelehealth.vikalphelpline.utilities.exception.DAOException;
import org.intelehealth.vikalphelpline.widget.materialprogressbar.CustomProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//import static org.intelehealth.ekalhelpline.utilities.StringUtils.en__as_dob;

public class MedicalAdviceExistingPatientsActivity extends AppCompatActivity {
    private static final String EXTRA_PATIENT_UUID = "EXTRA_PATIENT_UUID";
    SessionManager sessionManager = null;
    String patientUuid;
    Context context;

    Intent i_privacy;
    String privacy_value;
    CustomProgressDialog cpd;
    private CheckBox chb_agree_privacy, cbVaccineGuide, cbCovidConcern, cbManagingBreathlessness, cbManageVoiceIssue,
            cbManageEating, cbDealProblems, cbMentalHealth, cbExercises, cbOthers;
    private TextView txt_privacy;
    private EditText et_medical_advice_extra, et_medical_advice_additional;

    public static void start(Context context, String patientUuid) {
        Intent starter = new Intent(context, MedicalAdviceExistingPatientsActivity.class);
        starter.putExtra(EXTRA_PATIENT_UUID, patientUuid);
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
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (patientUuid != null) {
                createMedicalAdviceVisit();
            }
        });


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
        et_medical_advice_extra = llMedicalAdvice.findViewById(R.id.et_medical_advice_extra);
        et_medical_advice_additional = llMedicalAdvice.findViewById(R.id.et_medical_advice_additional);
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

        if (cbOthers.isChecked() && TextUtils.isEmpty(et_medical_advice_extra.getText())) {
            Toast.makeText(context, R.string.error_medical_visit_data, Toast.LENGTH_SHORT).show();
            return;
        }

        //check if privacy notice is checked
        if (TextUtils.isEmpty(patientUuid) && !chb_agree_privacy.isChecked()) {
            Toast.makeText(context, getString(R.string.please_read_out_privacy_consent_first), Toast.LENGTH_SHORT).show();
            return;
        }


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
        String insertion = Node.bullet_arrow + "<b>" + "Medical Advice" + "</b>" + ": ";
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
        if (!TextUtils.isEmpty(et_medical_advice_additional.getText()))
            insertion = insertion.concat(Node.next_line + String.format("%s: %s", getString(R.string.txt_additional_info), et_medical_advice_additional.getText()));
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
            speciality_attributes.insertVisitAttributes(visitUuid, AppConstants.DOCTOR_NOT_NEEDED);
           // speciality_attributes.insertVisitAttributes(visitUuid, " Specialist doctor not needed");
        } catch (DAOException e) {
            e.printStackTrace();
        }

        endVisit(visitUuid, patientUuid, endDate);
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

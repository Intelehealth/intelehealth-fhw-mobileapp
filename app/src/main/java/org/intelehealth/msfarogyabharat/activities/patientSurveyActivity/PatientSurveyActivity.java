package org.intelehealth.msfarogyabharat.activities.patientSurveyActivity;

import static org.intelehealth.msfarogyabharat.database.dao.VisitsDAO.startTimeByVisitUuid;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.database.dao.EncounterDAO;
import org.intelehealth.msfarogyabharat.database.dao.ObsDAO;
import org.intelehealth.msfarogyabharat.database.dao.VisitsDAO;
import org.intelehealth.msfarogyabharat.models.dto.EncounterDTO;
import org.intelehealth.msfarogyabharat.models.dto.ObsDTO;
import org.intelehealth.msfarogyabharat.syncModule.SyncUtils;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UuidDictionary;

import org.intelehealth.msfarogyabharat.activities.homeActivity.HomeActivity;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;

public class PatientSurveyActivity extends AppCompatActivity {
    private static final String TAG = PatientSurveyActivity.class.getSimpleName();
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    SyncUtils syncUtils = new SyncUtils();
    Context context;
    SQLiteDatabase db;

    ImageButton mScaleButton1;
    ImageButton mScaleButton2;
    ImageButton mScaleButton3;
    ImageButton mScaleButton4;
    ImageButton mScaleButton5;
    EditText mComments;

    TextView mSkip;
    TextView mSubmit;

    String rating = "0";
    String comments;

    SessionManager sessionManager = null;
    String appLanguage;

    //Pre-defined note: By Nishita
    Spinner notesSpinner;
    ArrayList<String> patientNoteList;
    ArrayAdapter<String> patientNoteAdapter;
    String noteText = "";
    private RatingBar ratingBar;

    private final ExecutorService submitExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService skipExecutorService = Executors.newSingleThreadExecutor();

    @Override
    public void onBackPressed() {
        //do nothing
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_survey);
        setTitle(R.string.title_activity_login);
        sessionManager = new SessionManager(this);
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        context = getApplicationContext();

        notesSpinner = findViewById(R.id.noteSpinner);
        patientNoteList = getPatientNoteList();
        patientNoteAdapter = new ArrayAdapter<>(PatientSurveyActivity.this, android.R.layout.simple_spinner_dropdown_item, patientNoteList);
        notesSpinner.setAdapter(patientNoteAdapter);

        mScaleButton1 = findViewById(R.id.button_scale_1);
        mScaleButton2 = findViewById(R.id.button_scale_2);
        mScaleButton3 = findViewById(R.id.button_scale_3);
        mScaleButton4 = findViewById(R.id.button_scale_4);
        mScaleButton5 = findViewById(R.id.button_scale_5);
        mComments = findViewById(R.id.editText_exit_survey);
        mSkip = findViewById(R.id.button_survey_skip);
        mSubmit = findViewById(R.id.button_survey_submit);
        ratingBar = findViewById(R.id.ratingBar);

        notesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (notesSpinner.getSelectedItem().equals("Other"))
                    mComments.setVisibility(View.VISIBLE);
                else
                    mComments.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetScale();
                rating = "0"; //String.valueOf(v.getTag())
                v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        };

        ArrayList<ImageButton> scale = new ArrayList<>();
        scale.add(mScaleButton1);
        scale.add(mScaleButton2);
        scale.add(mScaleButton3);
        scale.add(mScaleButton4);
        scale.add(mScaleButton5);
        for (int i = 0; i < scale.size(); i++) {
            ImageButton button = scale.get(i);
            button.setOnClickListener(listener);
        }

        resetScale();

        mSubmit.setOnClickListener(v -> {

//                if(notesSpinner.getSelectedItem().equals("Other")) {
//                    if(mComments.getText().toString().equalsIgnoreCase(""))
//                        mComments.setError("This field is required");
//                    else
//                    {
//                        noteText = mComments.getText().toString();
//                    }
//                }
//                else
            noteText = notesSpinner.getSelectedItem().toString();
            rating = String.valueOf(ratingBar.getRating());
            if (rating != null && !TextUtils.isEmpty(rating) && !rating.equalsIgnoreCase("0.0") && !noteText.equalsIgnoreCase("") && !notesSpinner.getSelectedItem().toString().equalsIgnoreCase(getString(R.string.spinner_select_reason)) && !noteText.equalsIgnoreCase(getString(R.string.spinner_select_reason))) {
                Log.d(TAG, "Rating is " + rating);
                submitExecutorService.execute(() -> {
                    uploadSurvey();
                    endVisit();
                });
            } else {
                if (notesSpinner.getSelectedItem().toString().equalsIgnoreCase(getString(R.string.spinner_select_reason))) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.exit_servey_validation_toast_for_reasone), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.exit_servey_validation_toast_for_Rating), Toast.LENGTH_LONG).show();
                }
            }

        });

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipExecutorService.execute(() -> endVisit());
            }
        });
    }

    private ArrayList<String> getPatientNoteList() {
        ArrayList<String> notes = new ArrayList<>();
        notes.add(getString(R.string.spinner_select_reason));
        notes.add(getString(R.string.spinner_referred));
        notes.add(getString(R.string.spinner_died));
        notes.add(getString(R.string.spinner_loss_followUp));
        notes.add(getString(R.string.spinner_refuse_followUp));
        notes.add(getString(R.string.spinner_followup_scheduled));
        notes.add(getString(R.string.spinner_not_aplicable));

        return notes;
    }

    private void resetScale() {
        ArrayList<ImageButton> scale = new ArrayList<>();
        scale.add(mScaleButton1);
        scale.add(mScaleButton2);
        scale.add(mScaleButton3);
        scale.add(mScaleButton4);
        scale.add(mScaleButton5);
        for (int i = 0; i < scale.size(); i++) {
            ImageButton button = scale.get(i);
            button.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        rating = "0";
    }

    private void uploadSurvey() {
        String startDateTime = startTimeByVisitUuid(visitUuid);

        //        ENCOUNTER_PATIENT_EXIT_SURVEY
        EncounterDTO encounterDTO = new EncounterDTO();
        String uuid = UUID.randomUUID().toString();
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(uuid);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_PATIENT_EXIT_SURVEY"));

        //As per issue #785 - we fixed it by subtracting 1 minute from Encounter Time
        try {
            /**
             * the solution of minus five seconds was not working fine in cases where there was no complusion of visit presc
             * in such case visti was creating and ending within 1min as well. So this another workaround of
             * adding +5 seconds to the startvisitTime of Visit itself in that way it makes sure that the time is always within start
             * and end time as visit is impossible to end within 5secs. - Prajwal.
             */
            String time = plusFiveSecondsToVisitStartDateTime(startDateTime);
            Log.v("VTime", "VTime: " + time);
            encounterDTO.setEncounterTime(time);

            //  encounterDTO.setEncounterTime(twoMinutesAgo(AppConstants.dateAndTimeUtils.currentDateTime()));
//            encounterDTO.setEncounterTime(fiveMinutesAgo(AppConstants.dateAndTimeUtils.currentDateTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        encounterDTO.setVisituuid(visitUuid);
//        encounterDTO.setProvideruuid(encounterDTO.getProvideruuid());  //handles correct provideruuid for every patient
        encounterDTO.setProvideruuid(sessionManager.getProviderID());  //handles correct provideruuid for every patient
        encounterDTO.setSyncd(false);
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        List<ObsDTO> obsDTOList = new ArrayList<>();
        obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(uuid);
        obsDTO.setValue(rating);
        obsDTO.setConceptuuid(UuidDictionary.RATING);
        obsDTOList.add(obsDTO);
        obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(uuid);
        obsDTO.setValue(noteText);
        obsDTO.setConceptuuid(UuidDictionary.COMMENTS);
        obsDTOList.add(obsDTO);
        try {
            obsDAO.insertObsToDb(obsDTOList);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

//      AppConstants.notificationUtils.DownloadDone("Upload survey", "Survey uploaded", 3, PatientSurveyActivity.this);

    }

    private String plusFiveSecondsToVisitStartDateTime(String startDateTime) throws ParseException {
        long FIVE_SECONDS = 5 * 1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(startDateTime).getTime();

        return df.format(new Date(time + FIVE_SECONDS));
    }

    public void setLocale(String appLanguage) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
            getApplicationContext().createConfigurationContext(conf);
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
    }

    /* public String fiveMinutesAgo(String timeStamp) throws ParseException {

         long FIVE_MINS_IN_MILLIS = 5 * 60 * 1000;
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
         long time = df.parse(timeStamp).getTime();

         return df.format(new Date(time - FIVE_MINS_IN_MILLIS));
     } */
    public String twoMinutesAgo(String timeStamp) throws ParseException {

        long TWO_MINS_IN_MILLIS = 2 * 60 * 1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(timeStamp).getTime();

        return df.format(new Date(time - TWO_MINS_IN_MILLIS));
    }

    private void endVisit() {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, AppConstants.dateAndTimeUtils.currentDateTime());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        //SyncDAO syncDAO = new SyncDAO();
        //syncDAO.pushDataApi();
        syncUtils.syncForeground("survey"); //Sync function will work in foreground of app and
        // the Time will be changed for last sync.

//        AppConstants.notificationUtils.DownloadDone(getString(R.string.end_visit_notif), getString(R.string.visit_ended_notif), 3, PatientSurveyActivity.this);

        runOnUiThread(() -> {
            sessionManager.removeVisitSummary(patientUuid, visitUuid);

            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }


}

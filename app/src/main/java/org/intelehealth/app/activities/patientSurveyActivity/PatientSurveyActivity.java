package org.intelehealth.app.activities.patientSurveyActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.utilities.exception.DAOException;

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

    String rating;
    String comments;

    SessionManager sessionManager = null;

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
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        context = getApplicationContext();

        mScaleButton1 = findViewById(R.id.button_scale_1);
        mScaleButton2 = findViewById(R.id.button_scale_2);
        mScaleButton3 = findViewById(R.id.button_scale_3);
        mScaleButton4 = findViewById(R.id.button_scale_4);
        mScaleButton5 = findViewById(R.id.button_scale_5);
        mComments = findViewById(R.id.editText_exit_survey);
        mSkip = findViewById(R.id.button_survey_skip);
        mSubmit = findViewById(R.id.button_survey_submit);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetScale();
                rating = String.valueOf(v.getTag());
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

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rating != null && !TextUtils.isEmpty(rating)) {
                    Log.d(TAG, "Rating is " + rating);
                    uploadSurvey();
                    endVisit();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.exit_survey_toast), Toast.LENGTH_LONG).show();
                }

            }
        });

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endVisit();
            }
        });
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
        rating = "";
    }

    private void uploadSurvey() {
        String newFollowUpDate = "";
        String originalStartFollowUp = getStartDate(patientUuid);
        if(!originalStartFollowUp.isEmpty() && !originalStartFollowUp.equals("")) {
            String originalStartDate = originalStartFollowUp.substring(0,10);
            boolean followUpSchedule = getComplaintData(visitUuid);
            String visitStartDate = getCurrentVisitStartDate(visitUuid).substring(0, 10);
            int no_of_days = getDatesBetween(originalStartDate, visitStartDate);
            if (no_of_days < 20 && followUpSchedule) {
                try {
                    Date visitStart = new SimpleDateFormat("yyyy-MM-dd").parse(visitStartDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(visitStart);
                    cal.add(Calendar.DATE, 7);
                    Date modifiedDate = cal.getTime();
                    newFollowUpDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(modifiedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
//        ENCOUNTER_PATIENT_EXIT_SURVEY

        EncounterDTO encounterDTO = new EncounterDTO();
        String uuid = UUID.randomUUID().toString();
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(uuid);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_PATIENT_EXIT_SURVEY"));

        //As per issue #785 - we fixed it by subtracting 1 minute from Encounter Time
        try {
            encounterDTO.setEncounterTime(fiveMinutesAgo(AppConstants.dateAndTimeUtils.currentDateTime()));
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
        obsDTO.setValue(mComments.getText().toString());
        obsDTO.setConceptuuid(UuidDictionary.COMMENTS);
        obsDTOList.add(obsDTO);
        if (!newFollowUpDate.equals("") && !newFollowUpDate.isEmpty()) {
            obsDTO = new ObsDTO();
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(uuid);
            obsDTO.setValue(newFollowUpDate + " " + "tag: Referral");
            obsDTO.setConceptuuid(UuidDictionary.FOLLOW_UP_VISIT);
            obsDTOList.add(obsDTO);
        }
        try {
            obsDAO.insertObsToDb(obsDTOList);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

//      AppConstants.notificationUtils.DownloadDone("Upload survey", "Survey uploaded", 3, PatientSurveyActivity.this);

    }

    private int getDatesBetween(String originalStartDate, String visitStartDate) {
        int no_of_days = 0;
        try {
            Date originalStart = new SimpleDateFormat("yyyy-MM-dd").parse(originalStartDate);
            Date visitStart = new SimpleDateFormat("yyyy-MM-dd").parse(visitStartDate);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(visitStart.getTime() - originalStart.getTime());
            no_of_days = c.get(Calendar.DAY_OF_MONTH) - 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return no_of_days;
    }

    private String getCurrentVisitStartDate(String visitUuid) {
        String startDate = "";
        String query = "SELECT * FROM tbl_visit WHERE uuid = ?";
        final Cursor searchCursor = db.rawQuery(query, new String[]{visitUuid});
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    startDate = searchCursor.getString(searchCursor.getColumnIndexOrThrow("startdate"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();
        return startDate;
    }

    private boolean getComplaintData(String visitUuid) {
        boolean scheduleFollowUp = false;
        String query = "SELECT d.uuid, d.visituuid, o.value FROM tbl_encounter d, tbl_obs o WHERE d.visituuid = ? AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND o.value LIKE '%No,but will visit%'";
        final Cursor searchCursor = db.rawQuery(query, new String[]{visitUuid, UuidDictionary.CURRENT_COMPLAINT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    scheduleFollowUp = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();
        searchCursor.close();
        return scheduleFollowUp;
    }

    private String getStartDate(String patientuuid) {
        String startDate = "";
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, o.value FROM tbl_visit a, tbl_encounter d, tbl_obs o WHERE a.patientuuid = ? AND a.enddate is NOT NULL AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ?  AND o.value  LIKE '%tag: Referral%' GROUP BY a.uuid ORDER BY a.startdate ASC LIMIT 1";
        final Cursor searchCursor = db.rawQuery(query, new String[]{patientuuid, UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    startDate = searchCursor.getString(searchCursor.getColumnIndexOrThrow("startdate"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (searchCursor.moveToNext());
        }
        searchCursor.close();

        return startDate;
    }

    public String fiveMinutesAgo(String timeStamp) throws ParseException {

        long FIVE_MINS_IN_MILLIS = 5 * 60 * 1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(timeStamp).getTime();

        return df.format(new Date(time - FIVE_MINS_IN_MILLIS));
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
        syncUtils.syncForeground("survey"); //Sync function will work in foreground of org and
        // the Time will be changed for last sync.

//        AppConstants.notificationUtils.DownloadDone(getString(R.string.end_visit_notif), getString(R.string.visit_ended_notif), 3, PatientSurveyActivity.this);

        sessionManager.removeVisitSummary(patientUuid, visitUuid);

        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


}

package org.intelehealth.unicef.activities.patientSurveyActivity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.BaseActivity;
import org.intelehealth.unicef.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.database.dao.EncounterDAO;
import org.intelehealth.unicef.database.dao.ObsDAO;
import org.intelehealth.unicef.database.dao.VisitsDAO;
import org.intelehealth.unicef.models.dto.EncounterDTO;
import org.intelehealth.unicef.models.dto.ObsDTO;
import org.intelehealth.unicef.syncModule.SyncUtils;
import org.intelehealth.unicef.utilities.NetworkUtils;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.UuidDictionary;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PatientSurveyActivity_New extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = PatientSurveyActivity_New.class.getSimpleName();
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;
    SyncUtils syncUtils = new SyncUtils();
    Context context;
    SQLiteDatabase db;

    EditText mComments;
    ImageButton mSkip, refresh;
    Button mSubmit;
    private RatingBar ratingBar;

    String rating;
    String comments;

    SessionManager sessionManager = null;
    private NetworkUtils networkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_survey_new);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        getIntentValues();
        initUI();
        networkUtils = new NetworkUtils(this, this);
        clickListeners();
    }

    private void clickListeners() {

        // Submit button click will end the visit.
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting the rating
                rating = String.valueOf(ratingBar.getRating());
                if (rating != null && !TextUtils.isEmpty(rating)) {
                    Log.d(TAG, "Rating is " + rating);
                    uploadSurvey();
                    endVisit("Feedback screen with feedback");
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.exit_survey_toast), Toast.LENGTH_LONG).show();
                    return;
                }

            }
        });

        // skip button click will skip this feedback screen.
        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endVisit("Feedback screen without feedback");
            }
        });

    }

    private void getIntentValues() {
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
        }
    }

    private void initUI() {
        setTitle(R.string.title_activity_login);
        sessionManager = new SessionManager(this);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        context = getApplicationContext();

        mComments = findViewById(R.id.editText_exit_survey);
        mSkip = findViewById(R.id.cancelbtn);
        mSubmit = findViewById(R.id.btn_submit);
        ratingBar = (RatingBar) findViewById(R.id.ratingbar);
        refresh = findViewById(R.id.refresh);
    }


    @Override
    public void onBackPressed() {
        //do nothing
    }

    /**
     * This will upload the Feedback value to backend...
     */
    private void uploadSurvey() {
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
        encounterDTO.setProvideruuid(sessionManager.getProviderID());  //handles correct provideruuid for every patient
        encounterDTO.setSyncd(false);
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        // Rating adding in this section.
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        List<ObsDTO> obsDTOList = new ArrayList<>();
        obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(uuid);
        obsDTO.setValue(rating);
        obsDTO.setConceptuuid(UuidDictionary.RATING);
        obsDTOList.add(obsDTO);

        // Comments adding in this section.
        obsDTO = new ObsDTO();
        obsDTO.setUuid(UUID.randomUUID().toString());
        obsDTO.setEncounteruuid(uuid);
        obsDTO.setValue(mComments.getText().toString());
        obsDTO.setConceptuuid(UuidDictionary.COMMENTS);
        obsDTOList.add(obsDTO);

        try {
            obsDAO.insertObsToDb(obsDTOList);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /**
     * This function returns a timestamp with -5 minutes interval.
     *
     * @param timeStamp
     * @return
     * @throws ParseException
     */
    public String fiveMinutesAgo(String timeStamp) throws ParseException {
        long FIVE_MINS_IN_MILLIS = 5 * 60 * 1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(timeStamp).getTime();
        return df.format(new Date(time - FIVE_MINS_IN_MILLIS));
    }

    /**
     * This function will end the visit for this patient.
     */
    private void endVisit(String tag) {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, AppConstants.dateAndTimeUtils.currentDateTime());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        syncUtils.syncForeground("survey"); //Sync function will work in foreground of org and
        // the Time will be changed for last sync.

        sessionManager.removeVisitSummary(patientUuid, visitUuid);
        Intent i = new Intent(this, HomeScreenActivity_New.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("intentTag", tag);
        startActivity(i);
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
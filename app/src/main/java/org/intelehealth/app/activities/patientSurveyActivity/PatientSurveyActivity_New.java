package org.intelehealth.app.activities.patientSurveyActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import org.intelehealth.app.appointmentNew.ScheduleAppointmentActivity_New;
import org.intelehealth.app.utilities.CustomLog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.NotificationSchedulerUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private BroadcastReceiver mBroadcastReceiver;
    Set<Integer> broadcasterReceiverStatusMap = new HashSet<>();
    private boolean submitClicked = false;
    private int mStatusCount = 0;
    private String endVisitTag = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_survey_new);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        getIntentValues();
        initUI();
        networkUtils = new NetworkUtils(this, this);
        clickListeners();

        //handling survey submit result on the broadcaster receiver
        //because we triggering sync after ending visit

        //so, if we close the screen and go to any other screen and initiate any query related to visit
        //the we might not get the actual result

        //hence checking that sync is completed or not
        //if completed then closing the activity and showing success result
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context, getString(R.string.sync_completed), Toast.LENGTH_SHORT).show();
                CustomLog.v(TAG, "onReceive JOB =  " + intent.getIntExtra("JOB", -1));
                if (submitClicked) {
                    mStatusCount = 0;
                    broadcasterReceiverStatusMap.add(intent.getIntExtra("JOB", -1));
                    //sometimes the broadcaster receiver returning same status multipple times
                    //that's why added those values on SET then calculating
                    for(int status : broadcasterReceiverStatusMap){
                        mStatusCount+=status;
                    }
                    //mStatusCount = mStatusCount + intent.getIntExtra("JOB", -1);
                    if (mStatusCount == AppConstants.SYNC_PULL_PUSH_APPOINTMENT_PULL_DATA_DONE) {
                        // the Time will be changed for last sync.
                        endVisitTag = "";
                        submitClicked = false;

                        sessionManager.removeVisitSummary(patientUuid, visitUuid);
                        Intent i = new Intent(PatientSurveyActivity_New.this, HomeScreenActivity_New.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("intentTag", endVisitTag);
                        startActivity(i);
                    }
                }
            }
        };

        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
        ContextCompat.registerReceiver(
                this,
                mBroadcastReceiver,
                filterSend,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void clickListeners() {

        // Submit button click will end the visit.
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting the rating
                rating = String.valueOf(ratingBar.getRating());
                if (rating != null && !TextUtils.isEmpty(rating)) {
                    CustomLog.d(TAG, "Rating is " + rating);
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
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        context = getApplicationContext();

        mComments = findViewById(R.id.editText_exit_survey);
        mSkip = findViewById(R.id.cancelbtn);
        mSubmit = findViewById(R.id.btn_submit);
        ratingBar = (RatingBar) findViewById(R.id.ratingbar);
        refresh = findViewById(R.id.refresh);
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

            //cancelling alarm manager for end visit followup
            NotificationSchedulerUtils.cancelNotification(visitUuid + "-" + AppConstants.FOLLOW_UP_SCHEDULE_ONE_DURATION);
            NotificationSchedulerUtils.cancelNotification(visitUuid + "-" + AppConstants.FOLLOW_UP_SCHEDULE_TWO_DURATION);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        submitClicked = true;
        endVisitTag = tag;
        syncUtils.syncForeground("survey"); //Sync function will work in foreground of org and

        //handling success result on the broadcaster receiver
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));
        }
        else {
            refresh.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));
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
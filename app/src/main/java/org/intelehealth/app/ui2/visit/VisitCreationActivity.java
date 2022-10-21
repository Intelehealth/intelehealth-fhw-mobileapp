package org.intelehealth.app.ui2.visit;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;
import org.intelehealth.app.utilities.SessionManager;

public class VisitCreationActivity extends AppCompatActivity implements VisitCreationActionListener {

    private static final String TAG = VisitCreationActivity.class.getSimpleName();
    private static final String VITAL_FRAGMENT = "VITAL";
    private static final String VITAL_SUMMARY_FRAGMENT = "VITAL_SUMMARY";
    private static final String VISIT_REASON_FRAGMENT = "VISIT_REASON";
    public static final int STEP_1_VITAL = 1;
    public static final int STEP_1_VITAL_SUMMARY = 1001;
    public static final int STEP_2_VISIT_REASON = 2;
    public static final int STEP_2_VISIT_REASON_QUESTION = 3;
    public static final int STEP_2_VISIT_REASON_QUESTION_ASSOCIATE_SYMPTOMS = 4;
    public static final int STEP_3_PHYSICAL_EXAMINATION = 5;
    public static final int STEP_3_MEDICAL_HISTORY = 6;

    private int mCurrentStep = STEP_1_VITAL;

    SessionManager sessionManager;
    private String patientName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";

    private FrameLayout mSummaryFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_creation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }
        mSummaryFrameLayout = findViewById(R.id.fl_steps_summary);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            Log.v(TAG, "Patient ID: " + patientUuid);
            Log.v(TAG, "Visit ID: " + visitUuid);
            Log.v(TAG, "Patient Name: " + patientName);
            Log.v(TAG, "Intent Tag: " + intentTag);
        }


        Bundle bundle = new Bundle();
        bundle.putString("patientUuid", patientUuid);
        bundle.putString("visitUuid", visitUuid);
        bundle.putString("encounterUuidVitals", encounterVitals);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(bundle), VITAL_FRAGMENT).
                commit();
    }

    public void backPress(View view) {
        finish();
    }

    @Override
    public void onFormSubmitted(int nextAction) {
        mCurrentStep = nextAction;
        switch (nextAction) {
            case STEP_1_VITAL_SUMMARY:
                Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                mSummaryFrameLayout.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_summary, VitalCollectionSummaryFragment.newInstance(), VITAL_SUMMARY_FRAGMENT).
                        commit();
                break;
        }
    }

    @Override
    public void onProgress(int progress) {

    }

    @Override
    public void onManualClose() {
        switch (mCurrentStep) {
            case STEP_1_VITAL_SUMMARY:
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
package io.intelehealth.client.activities.patient_survey_activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.home_activity.HomeActivity;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.services.ClientService;

public class PatientSurveyActivity extends AppCompatActivity {

    private static final String TAG = "PatientSurveyActivity";

    Integer patientID;
    String visitID;
    String state;
    String patientName;
    String intentTag;
    String visitUUID;

    Context context;
    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    ImageButton mScaleButton1;
    ImageButton mScaleButton2;
    ImageButton mScaleButton3;
    ImageButton mScaleButton4;
    ImageButton mScaleButton5;
    EditText mComments;
    Button mSkip;
    Button mSubmit;

    String rating;
    String comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getIntExtra("patientID", -1);
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_survey);

        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();
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
                uploadSurvey();
                endVisit();
            }
        });

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endVisit();
            }
        });

    }

    private void resetScale(){
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

        comments = mComments.getText().toString();
        if (rating != null) {
            String[] DELAYED_JOBS_PROJECTION = new String[]{DelayedJobQueueProvider._ID, DelayedJobQueueProvider.SYNC_STATUS};
            String SELECTION = DelayedJobQueueProvider.JOB_TYPE + "=? AND " +
                    DelayedJobQueueProvider.PATIENT_ID + "=? AND " +
                    DelayedJobQueueProvider.VISIT_ID + "=?";
            String[] ARGS = new String[]{"survey", String.valueOf(patientID), visitID};

            Cursor c = getContentResolver().query(DelayedJobQueueProvider.CONTENT_URI,
                    DELAYED_JOBS_PROJECTION, SELECTION, ARGS, null);

            if (c != null && c.moveToFirst() && c.getCount() > 0) {
                int sync_status = c.getInt(c.getColumnIndexOrThrow(DelayedJobQueueProvider.SYNC_STATUS));
                switch (sync_status) {
                    case ClientService.STATUS_SYNC_STOPPED: {
                        Intent serviceIntent = new Intent(PatientSurveyActivity.this, ClientService.class);
                        serviceIntent.putExtra("serviceCall", "survey");
                        serviceIntent.putExtra("patientID", patientID);
                        serviceIntent.putExtra("visitID", visitID);
                        serviceIntent.putExtra("name", patientName);
                        serviceIntent.putExtra("rating", rating);
                        if (comments != null && !comments.isEmpty()) {
                            serviceIntent.putExtra("comments", comments);
                        }
                        startService(serviceIntent);
                        break;
                    }
                    case ClientService.STATUS_SYNC_IN_PROGRESS: {
                        Toast.makeText(context, getString(R.string.sync_in_progress), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:
                }
            } else {
                Intent serviceIntent = new Intent(PatientSurveyActivity.this, ClientService.class);
                serviceIntent.putExtra("serviceCall", "survey");
                serviceIntent.putExtra("patientID", patientID);
                serviceIntent.putExtra("visitID", visitID);
                serviceIntent.putExtra("rating", rating);
                if (comments != null && !comments.isEmpty()) {
                    serviceIntent.putExtra("comments", comments);
                }
                serviceIntent.putExtra("name", patientName);
                startService(serviceIntent);
            }
            c.close();
        }
    }

    private void endVisit() {
        if (visitUUID == null || visitUUID.isEmpty()) {
            String[] columnsToReturn = {"openmrs_visit_uuid"};
            String visitIDorderBy = "start_datetime";
            String visitIDSelection = "_id = ?";
            String[] visitIDArgs = {visitID};
            final Cursor visitIDCursor = db.query("visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
            if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
                visitIDCursor.moveToFirst();
                visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("openmrs_visit_uuid"));
            }
            if (visitIDCursor != null) visitIDCursor.close();
        }

        Log.d(TAG, "endVisit: uuid ok");
        String[] DELAYED_JOBS_PROJECTION = new String[]{DelayedJobQueueProvider._ID, DelayedJobQueueProvider.SYNC_STATUS};
        String SELECTION = DelayedJobQueueProvider.JOB_TYPE + "=? AND " +
                DelayedJobQueueProvider.PATIENT_ID + "=? AND " +
                DelayedJobQueueProvider.VISIT_ID + "=?";
        String[] ARGS = new String[]{"endVisit", String.valueOf(patientID), visitID};

        Cursor c = getContentResolver().query(DelayedJobQueueProvider.CONTENT_URI,
                DELAYED_JOBS_PROJECTION, SELECTION, ARGS, null);

        if (c != null && c.moveToFirst() && c.getCount() > 0) {
            int sync_status = c.getInt(c.getColumnIndexOrThrow(DelayedJobQueueProvider.SYNC_STATUS));
            switch (sync_status) {
                case ClientService.STATUS_SYNC_STOPPED: {
                    Intent serviceIntent = new Intent(PatientSurveyActivity.this, ClientService.class);
                    serviceIntent.putExtra("serviceCall", "endVisit");
                    serviceIntent.putExtra("patientID", patientID);
                    serviceIntent.putExtra("visitUUID", visitUUID);
                    serviceIntent.putExtra("name", patientName);
                    startService(serviceIntent);
                    Intent intent = new Intent(PatientSurveyActivity.this, HomeActivity.class);
                    startActivity(intent);
                    break;
                }
                case ClientService.STATUS_SYNC_IN_PROGRESS: {
                    Toast.makeText(context, getString(R.string.sync_in_progress), Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
            }
        } else {
            Log.d(TAG, "endVisit: delayed job first");
            Intent serviceIntent = new Intent(PatientSurveyActivity.this, ClientService.class);
            serviceIntent.putExtra("serviceCall", "endVisit");
            serviceIntent.putExtra("patientID", patientID);
            serviceIntent.putExtra("visitUUID", visitUUID);
            serviceIntent.putExtra("name", patientName);
            startService(serviceIntent);
            SharedPreferences.Editor editor = context.getSharedPreferences(patientID + "_" + visitID, MODE_PRIVATE).edit();
            editor.remove("exam_" + patientID + "_" + visitID);
            editor.commit();
            Intent intent = new Intent(PatientSurveyActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        c.close();
    }


}

package io.intelehealth.client.activities.patientSurveyActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.homeActivity.HomeActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.database.dao.EncounterDAO;
import io.intelehealth.client.database.dao.ObsDAO;
import io.intelehealth.client.database.dao.PullDataDAO;
import io.intelehealth.client.database.dao.VisitsDAO;
import io.intelehealth.client.models.dto.EncounterDTO;
import io.intelehealth.client.models.dto.ObsDTO;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;

public class PatientSurveyActivity extends AppCompatActivity {
    private static final String TAG = PatientSurveyActivity.class.getSimpleName();
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String intentTag;

    Context context;
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
    String thisDate = "";

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

        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date todayDate = new Date();
        thisDate = currentDate.format(todayDate);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_survey);
        setTitle(R.string.title_activity_login);

        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
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
                endVisit(thisDate);
            }
        });

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endVisit(thisDate);
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
//        ENCOUNTER_PATIENT_EXIT_SURVEY
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);

        EncounterDTO encounterDTO = new EncounterDTO();
        String uuid = UUID.randomUUID().toString();
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(uuid);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_PATIENT_EXIT_SURVEY"));
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
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
        obsDAO.insertObsToDb(obsDTOList);


        AppConstants.notificationUtils.DownloadDone("Upload survey", "Survey uploaded", PatientSurveyActivity.this);


    }

    private void endVisit(String endate) {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, endate);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);

        }
        PullDataDAO pullDataDAO = new PullDataDAO();
        pullDataDAO.pushDataApi();

        AppConstants.notificationUtils.DownloadDone("End visit", "Visit ended", PatientSurveyActivity.this);
        SharedPreferences.Editor editor = context.getSharedPreferences(patientUuid + "_" + visitUuid, MODE_PRIVATE).edit();
        editor.remove("exam_" + patientUuid + "_" + visitUuid);
        editor.commit();
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


}

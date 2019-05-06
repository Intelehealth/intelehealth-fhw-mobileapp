package io.intelehealth.client.views.activites;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import io.intelehealth.client.R;
import io.intelehealth.client.databinding.ActivityVitalsBinding;
import io.intelehealth.client.utilities.SessionManager;

public class VitalsActivity extends AppCompatActivity {
    private static final String TAG = VitalsActivity.class.getSimpleName();
    ActivityVitalsBinding binding;
    SessionManager sessionManager;
    private String patientName = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_vitals);
//        Toolbar toolbar = findViewById(R.id.toolbar);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vitals);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle(R.string.title_activity_vitals);
        setTitle(patientName + ": " + getTitle());
        sessionManager = new SessionManager(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            //    physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along


            Log.v(TAG, "Patient ID: " + patientUuid);
            Log.v(TAG, "Visit ID: " + visitUuid);
            Log.v(TAG, "Patient Name: " + patientName);
            Log.v(TAG, "Intent Tag: " + intentTag);
        }
    }

}

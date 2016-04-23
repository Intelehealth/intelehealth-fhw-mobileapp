package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class PatientDetailActivity extends AppCompatActivity {

    private ArrayList<String> mPatientInfo;
    private String mPatientName;
    private String mPatientDob;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null && intent.getStringArrayListExtra("patientInfo") != null) {
            this.mPatientInfo = intent.getStringArrayListExtra("patientInfo");
            this.mPatientName = this.mPatientInfo.get(0) + ", " + this.mPatientInfo.get(1)
                    + " " + this.mPatientInfo.get(2);
            this.mPatientDob = "Date of Birth: "  + this.mPatientInfo.get(3) + " (Age "
                    + this.mPatientInfo.get(4) + ")";

            TextView textViewName = (TextView) findViewById(R.id.textview_patient_info_name);
            textViewName.setText(this.mPatientName);

            TextView textViewDob = (TextView) findViewById(R.id.textview_patient_info_dob);
            textViewDob.setText(this.mPatientDob);
        }

        // TODO: Access LocalDB and retrieve any available information
        // TODO: Update the image with the picture of the patient as stored on the filesystem
        // TODO: Connect to OpenMRS via the Middleware API and get more info on the Patient
    }

}

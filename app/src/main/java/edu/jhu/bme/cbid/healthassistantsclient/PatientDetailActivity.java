package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PatientDetailActivity extends AppCompatActivity {

    LocalRecordsDatabaseHelper mDbHelper;


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
            ArrayList<String> mPatientInfo = intent.getStringArrayListExtra("patientInfo");
            String mPatientName = mPatientInfo.get(0) + ", " + mPatientInfo.get(1)
                    + " " + mPatientInfo.get(2);
            String mPatientDob = "Date of Birth: " + mPatientInfo.get(3) + " (Age "
                    + mPatientInfo.get(4) + ")";
            String mAddress = mPatientInfo.get(5);
            String mCityState = mPatientInfo.get(6);
            String mPhone = "Phone Number: " + mPatientInfo.get(7);
            String mSdw = "Son/Daughter/Wife of " + mPatientInfo.get(8);
            String mOccupation = "Occupation: " + mPatientInfo.get(9);

            //TextView textViewName = (TextView) findViewById(R.id.textview_patient_details);
            //textViewName.setText(this.mPatientName);

            getSupportActionBar().setTitle(mPatientName);

            TextView textViewDob = (TextView) findViewById(R.id.textview_patient_info_age);
            textViewDob.setText(mPatientDob);

            TextView textViewSdw = (TextView) findViewById(R.id.textView_sdw);
            textViewSdw.setText(mSdw);

            TextView textViewOcc = (TextView) findViewById(R.id.textview_occup);
            textViewOcc.setText(mOccupation);

            TextView textViewAddr = (TextView) findViewById(R.id.textView_addr1);
            textViewAddr.setText(mAddress);

            TextView textViewCityState = (TextView) findViewById(R.id.textView_addr2);
            textViewCityState.setText(mCityState);

            TextView textViewPhone = (TextView) findViewById(R.id.textView_phone);
            textViewPhone.setText(mPhone);

            Bitmap imageBitmap = BitmapFactory.decodeFile(mPatientInfo.get(11));
            ImageView mImageView = (ImageView) findViewById(R.id.detail_image);
            mImageView.setImageBitmap(imageBitmap);

            UpdatePatientTask upd = new UpdatePatientTask();
            upd.execute(mPatientInfo.get(10));
        }
    }

    protected class UpdatePatientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Snackbar.make(findViewById(R.id.clayout_detail), "Refreshing patient data", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        @Override
        public Void doInBackground(String... params) {
            String patientId = params[0];
            queryExtraInfo(patientId);
            loadImage(patientId);

            return null;
        }

        public void queryExtraInfo(String id) {
            // TODO: Connect to OpenMRS via the Middleware API and get more info on the Patient
        }

        public void loadImage(String id) {
            // TODO: Update the image with the picture of the patient as stored on the filesystem
        }

    }

}

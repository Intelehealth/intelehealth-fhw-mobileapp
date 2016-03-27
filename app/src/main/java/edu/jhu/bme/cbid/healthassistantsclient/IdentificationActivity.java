package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.gson.Gson;

import java.util.ArrayList;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

/**
 * Created by Amal Afroz Alam on 3/25/16.
 */

public class IdentificationActivity extends AppCompatActivity {

    EditText mFirstName;
    EditText mMiddleName;
    EditText mLastName;
    EditText mDOB;
    EditText mPhoneNum;
    EditText mAge;
    EditText mAddress1;
    EditText mAddress2;
    EditText mCity;
    EditText mState;
    EditText mPostal;
    EditText mCountry;
    RadioButton mGenderM;
    RadioButton mGenderF;
    String mGender;
    EditText mRelationship;
    EditText mOccupation;

    private InsertPatientTable mTask = null;

    int patientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mFirstName = (EditText) findViewById(R.id.identification_first_name);
        mMiddleName = (EditText) findViewById(R.id.identification_middle_name);
        mLastName = (EditText) findViewById(R.id.identification_last_name);
        mDOB = (EditText) findViewById(R.id.identification_birth_date);
        mPhoneNum = (EditText) findViewById(R.id.identification_phone_number);
        mAge = (EditText) findViewById(R.id.identification_age);
        mAddress1 = (EditText) findViewById(R.id.identification_address1);
        mAddress2 = (EditText) findViewById(R.id.identification_address2);
        mCity = (EditText) findViewById(R.id.identification_city);
        mState = (EditText) findViewById(R.id.identification_state);
        mPostal = (EditText) findViewById(R.id.identification_postal_code);
        mCountry = (EditText) findViewById(R.id.identification_country);
        mGenderM = (RadioButton) findViewById(R.id.identification_gender_male);
        mGenderF = (RadioButton) findViewById(R.id.identification_gender_female);
        mRelationship = (EditText) findViewById(R.id.identification_relationship);
        mOccupation = (EditText) findViewById(R.id.identification_occupation);

        mGenderF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mGenderM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createNewPatient();
                Snackbar.make(view, R.string.snack_patient_created, Snackbar.LENGTH_LONG);
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.identification_gender_male:
                if (checked)
                    mGender = "male";
                break;
            case R.id.identification_gender_female:
                if (checked)
                    mGender = "female";
                break;
        }
    }

    public void createNewPatient() {

        boolean cancel = false;
        View focusView = null;

        ArrayList<EditText> values = new ArrayList<EditText>();
        values.add(mFirstName);
        values.add(mMiddleName);
        values.add(mLastName);
        values.add(mDOB);
        values.add(mPhoneNum);
        values.add(mAddress1);
        values.add(mAddress2);
        values.add(mCity);
        values.add(mState);
        values.add(mPostal);
        values.add(mCountry);
        values.add(mRelationship);
        values.add(mOccupation);

        for (int i = 0; i < values.size(); i++) {
            EditText et = values.get(i);
            if (TextUtils.isEmpty(et.getText().toString()) && et.getTag() == null) {
                et.setError(getString(R.string.error_field_required));
                focusView = et;
                cancel = true;
                break;
            }

        }


        if (cancel) {
            focusView.requestFocus();
        } else {
            Patient currentPatient = new Patient();
            try {
                currentPatient.setFirstName(mFirstName.getText().toString());
                if (TextUtils.isEmpty(mMiddleName.getText().toString())) {
                    currentPatient.setMiddleName("null");
                } else {
                    currentPatient.setMiddleName(mMiddleName.getText().toString());
                }
                currentPatient.setLastName(mLastName.getText().toString());
                currentPatient.setDateOfBirth(mDOB.getText().toString());
                currentPatient.setPhoneNumber(mPhoneNum.getText().toString());
                currentPatient.setAddress1(mAddress1.getText().toString());
                if (TextUtils.isEmpty(mAddress2.getText().toString())) {
                    currentPatient.setAddress2("null");
                } else {
                    currentPatient.setAddress2(mAddress2.getText().toString());
                }
                currentPatient.setCityVillage(mCity.getText().toString());
                currentPatient.setStateProvince(mState.getText().toString());
                currentPatient.setCountry(mCountry.getText().toString());
                currentPatient.setGender(mGender);
                currentPatient.setPatientIdentifier1(mRelationship.getText().toString());
                currentPatient.setPatientIdentifier2(mOccupation.getText().toString());
            } catch (NullPointerException e) {
                Snackbar.make(findViewById(R.id.cl_table), R.string.error_data_fields, Snackbar.LENGTH_SHORT);
            }

            mTask = new InsertPatientTable(currentPatient);
            mTask.execute((Void) null);

        }
    }

    public class InsertPatientTable extends AsyncTask<Void, Void, Boolean>
            implements DialogInterface.OnCancelListener {

        int patientID;
        Patient patient;

        InsertPatientTable(Patient currentPatient) {
            patient = currentPatient;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Gson gson = new Gson();
            Log.i("Patient", gson.toJson(patient));
            //TODO:insert patient into DB table
            return null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }
    }

}


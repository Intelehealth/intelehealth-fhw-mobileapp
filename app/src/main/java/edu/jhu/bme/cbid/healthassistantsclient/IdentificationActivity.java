package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

/**
 * Created by Amal Afroz Alam on 3/25/16.
 */

public class IdentificationActivity extends AppCompatActivity {

    EditText mFirstName;
    EditText mMiddleName;
    EditText mLastName;
    EditText mDOB;
    TextView mDOBTitle;
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
    DatePickerDialog mDOBPicker;

    private InsertPatientTable mTask = null;
    LocalRecordsDatabaseHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new LocalRecordsDatabaseHelper(this);

        mFirstName = (EditText) findViewById(R.id.identification_first_name);
        mMiddleName = (EditText) findViewById(R.id.identification_middle_name);
        mLastName = (EditText) findViewById(R.id.identification_last_name);
        mDOBTitle = (TextView) findViewById(R.id.identification_birth_date_text_view);
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

        Calendar calendar = Calendar.getInstance();
        mDOBPicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mDOB.setText(new StringBuilder().append(monthOfYear + 1)
                        .append("-").append(dayOfMonth).append("-").append(year)
                        .append(" "));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });

        mDOBTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createNewPatient();
                Snackbar.make(view, R.string.snack_patient_created, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        //TODO: check if gender is checked
        //TODO: check if date was selected for DOB

        ArrayList<EditText> values = new ArrayList<>();
        values.add(mFirstName);
        values.add(mMiddleName);
        values.add(mLastName);
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
                    currentPatient.setMiddleName(null);
                } else {
                    currentPatient.setMiddleName(mMiddleName.getText().toString());
                }
                currentPatient.setLastName(mLastName.getText().toString());
                currentPatient.setDateOfBirth(mDOB.getText().toString());
                currentPatient.setPhoneNumber(mPhoneNum.getText().toString());
                currentPatient.setAddress1(mAddress1.getText().toString());
                if (TextUtils.isEmpty(mAddress2.getText().toString())) {
                    currentPatient.setAddress2(null);
                } else {
                    currentPatient.setAddress2(mAddress2.getText().toString());
                }
                currentPatient.setCityVillage(mCity.getText().toString());
                currentPatient.setStateProvince(mState.getText().toString());
                currentPatient.setPostalCode(mPostal.getText().toString());
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

        long patientID;
        Patient patient;

        InsertPatientTable(Patient currentPatient) {
            patient = currentPatient;
        }


        ContentValues patientEntries = new ContentValues();

        public void gatherEntries() {
            patientEntries.put("first_name", patient.getFirstName());
            patientEntries.put("middle_name", patient.getMiddleName());
            patientEntries.put("last_name", patient.getLastName());
            patientEntries.put("date_of_birth", patient.getDateOfBirth());
            patientEntries.put("phone_number", patient.getPhoneNumber());
            patientEntries.put("address1", patient.getAddress1());
            patientEntries.put("address2", patient.getAddress2());
            patientEntries.put("city_village", patient.getCityVillage());
            patientEntries.put("state_province", patient.getStateProvince());
            patientEntries.put("postal_code", patient.getPostalCode());
            patientEntries.put("country", patient.getCountry());
            patientEntries.put("gender", patient.getGender());
            patientEntries.put("patient_identifier1", patient.getPatientIdentifier1());
            patientEntries.put("patient_identifier2", patient.getPatientIdentifier2());
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            gatherEntries();
            SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
            patientID = localdb.insert(
                    "patient",
                    null,
                    patientEntries
            );

            patient.setId(patientID);
            //TODO: record the patientID back into the same row as the patient was stored into

            Gson gson = new GsonBuilder().serializeNulls().create();
            Log.i("Patient", gson.toJson(patient));


            return null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            super.onPostExecute(aBoolean);

//            Intent intent = new Intent(IdentificationActivity.this, IdService.class);
//            intent.putExtra(Intent.EXTRA_TEXT, patient.getId().toString());
//            intent.setType("type/plain");
//            startService(intent);


            Intent intent2 = new Intent(IdentificationActivity.this, ComplaintSelectActivity.class);
            intent2.putExtra(Intent.EXTRA_TEXT, patient.getId().toString());
            intent2.setType("type/plain");
            startActivity(intent2);
        }
    }
}


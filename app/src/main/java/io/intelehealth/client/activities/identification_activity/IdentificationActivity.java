package io.intelehealth.client.activities.identification_activity;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.camera_activity.CameraActivity;
import io.intelehealth.client.activities.patient_detail_activity.PatientDetailActivity;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.utilities.HelperMethods;

import static io.intelehealth.client.utilities.HelperMethods.REQUEST_CAMERA;
import static io.intelehealth.client.utilities.HelperMethods.REQUEST_READ_EXTERNAL;

/**
 * Created by Amal Afroz Alam on 3/25/16.
 */

public class IdentificationActivity extends AppCompatActivity {
    String TAG = IdentificationActivity.class.getSimpleName();

    EditText mFirstName;
    EditText mMiddleName;
    EditText mLastName;
    EditText mDOB;
    EditText mPhoneNum;
    EditText mAge;
    EditText mAddress1;
    EditText mAddress2;
    EditText mCity;
    EditText mPostal;
    RadioButton mGenderM;
    RadioButton mGenderF;
    String mGender;
    EditText mRelationship;
    EditText mOccupation;
    DatePickerDialog mDOBPicker;
    EditText countryText;
    EditText stateText;
    Spinner mCountry;
    Spinner mState;

    String mPhoto;
    Patient patient = new Patient();
    Patient patient1 = new Patient();
    String patientID;
    String patientID_edit;

    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();

    LocalRecordsDatabaseHelper mDbHelper;
    String idPreFix;
    String visitID;

    ImageView mImageView;
    String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //Initialize the local database to store patient information
        mDbHelper = new LocalRecordsDatabaseHelper(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        idPreFix = sharedPref.getString(SettingsActivity.KEY_PREF_ID_PREFIX, "JHU");

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("pid")) {
                this.setTitle("Update Patient");
                patientID_edit = intent.getStringExtra("pid");
                patient1.setId(patientID_edit);
                setscreen(String.valueOf(patientID_edit));
            }
        }

        mFirstName = (EditText) findViewById(R.id.identification_first_name);
        mMiddleName = (EditText) findViewById(R.id.identification_middle_name);
        mLastName = (EditText) findViewById(R.id.identification_last_name);
        mDOB = (EditText) findViewById(R.id.identification_birth_date_text_view);
        mPhoneNum = (EditText) findViewById(R.id.identification_phone_number);
        mAge = (EditText) findViewById(R.id.identification_age);
        mAddress1 = (EditText) findViewById(R.id.identification_address1);
        mAddress2 = (EditText) findViewById(R.id.identification_address2);
        mCity = (EditText) findViewById(R.id.identification_city);
        stateText = (EditText) findViewById(R.id.identification_state);
        mState = (Spinner) findViewById(R.id.spinner_state);
        mPostal = (EditText) findViewById(R.id.identification_postal_code);
        countryText = (EditText) findViewById(R.id.identification_country);
        mCountry = (Spinner) findViewById(R.id.spinner_country);
        mGenderM = (RadioButton) findViewById(R.id.identification_gender_male);
        mGenderF = (RadioButton) findViewById(R.id.identification_gender_female);
        mRelationship = (EditText) findViewById(R.id.identification_relationship);
        mOccupation = (EditText) findViewById(R.id.identification_occupation);


        //setting the fields when user clikcs edit details
        mFirstName.setText(patient1.getFirstName());
        mMiddleName.setText(patient1.getMiddleName());
        mLastName.setText(patient1.getLastName());
        mDOB.setText(patient1.getDateOfBirth());
        mPhoneNum.setText(patient1.getPhoneNumber());
        mAddress1.setText(patient1.getAddress1());
        mAddress2.setText(patient1.getAddress2());
        mCity.setText(patient1.getCityVillage());
        mPostal.setText(patient1.getPostalCode());
        mRelationship.setText(patient1.getSdw());
        mOccupation.setText(patient1.getOccupation());


        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountry.setAdapter(countryAdapter);

        // generate patientid only if there is no intent for Identification activity
        if (patientID_edit == null) {
            generateID();
        }

        // setting radio button automatically according to the databse when user clicks edit details
        if (patientID == null) {
            if (patient1.getGender().equals("M")) {
                mGenderM.setChecked(true);
                if (mGenderF.isChecked())
                    mGenderF.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                mGenderF.setChecked(true);
                if (mGenderM.isChecked())
                    mGenderM.setChecked(false);
                Log.v(TAG, "yes");
            }
        }
        if (mGenderM.isChecked()) {
            mGender = "M";
        } else {
            mGender = "F";
        }


        if (patientID_edit != null) {
            // setting country accordig database
            mCountry.setSelection(countryAdapter.getPosition(String.valueOf(patient1.getCountry())));
        }

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.state_error, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(stateAdapter);

        mCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String country = adapterView.getItemAtPosition(i).toString();

                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_india, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);
                        // setting state according database when user clicks edit details
                        if (patientID_edit != null) {
                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getStateProvince())));
                        }

                    } else if (country.matches("United States")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_us, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);
                        if (patientID_edit != null) {
                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getStateProvince())));
                        }

                    }
                } else {
                    ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.state_error, android.R.layout.simple_spinner_item);
                    stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mState.setAdapter(stateAdapter);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Check to see if the permission was given to take pictures.
        /*if (ContextCompat.checkSelfPermission(IdentificationActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(IdentificationActivity.this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }*/

        //When either button is clicked, that information needs to be stored.
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

        /*
        The patient's picture will be taken here and then stored using the method below.
        This picture will then be displayed right after, allowing the user to verify the picture was well taken.
        */
        mImageView = (ImageView) findViewById(R.id.imageview_id_picture);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String[] results = HelperMethods.startImageCapture(IdentificationActivity.this,
                //        IdentificationActivity.this);
                //if (results != null) {
                //    mPhoto = results[0];
                //    mCurrentPhotoPath = results[1];
                //}
                File filePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "patient_photo");
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                Intent cameraIntent = new Intent(IdentificationActivity.this, CameraActivity.class);

                cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patientID);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
            }
        });

        //DOB is set using an AlertDialog
        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);

                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dob.set(year, monthOfYear, dayOfMonth);
                String dobString = simpleDateFormat.format(dob.getTime());

                mDOB.setText(dobString);

                //Age should be calculated based on the date
                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                mAge.setText(String.valueOf(age));

            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        //DOB Picker is shown when clicked
        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });

        /*
        User has to have option where they can enter the age.
        Some patients do not actually know their DOB, but they remember their age.
        If only age is provided, then the DOB is calculated as January 1, the year being current year minus their age.
         */
        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = IdentificationActivity.this;
                final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
                textInput.setTitle(R.string.identification_screen_dialog_age);
                final EditText dialogEditText = new EditText(context);
                dialogEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

                String prevValue = mAge.getText().toString();
                if (!prevValue.isEmpty()) {
                    dialogEditText.setText(prevValue);
                }

                textInput.setView(dialogEditText);

                textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ageString = (dialogEditText.getText().toString());

                        if (ageString.isEmpty() || ageString.matches("")) {
                            dialog.dismiss();
                        } else {
                            mAge.setText(ageString);
                            Calendar calendar = Calendar.getInstance();
                            int curYear = calendar.get(Calendar.YEAR);
                            int birthYear = curYear - Integer.valueOf(ageString);
                            String calcDOB = String.valueOf(birthYear) + "-01-01";
                            mDOB.setText(calcDOB);
                            dialog.dismiss();
                        }

                    }
                });

                textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                textInput.show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewPatient();
            }
        });


    }

    // This method is for setting the screen with existing values in database whenn user clicks edit details
    private void setscreen(String str) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {str};
        String[] patientColumns = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        Cursor idCursor = db.query("patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient1.setFirstName(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient1.setMiddleName(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient1.setLastName(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient1.setDateOfBirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient1.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient1.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient1.setCityVillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient1.setStateProvince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient1.setPostalCode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient1.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient1.setPhoneNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient1.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient1.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient1.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient1.setPatientPhoto(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));

            } while (idCursor.moveToNext());
        }

    }


    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.identification_gender_male:
                if (checked)
                    mGender = "M";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.identification_gender_female:
                if (checked)
                    mGender = "F";
                Log.v(TAG, "gender:" + mGender);
                break;
        }
    }

    /**
     * This method is primarily for data validation.
     * First, the screen is checked to see if a gender was selected for the patient.
     * If no gender selected, you get a dialog box telling you to do so.
     * <p/>
     * Next, if the DOB is after today's date, then you are asked to go back and correct it.
     * <p/>
     * Finally, all the text boxes are checked. With the text boxes, each EditText has a tag written in the XML.
     * If an EditText box does not have a tag, then it is assumed to be required.
     * Only optional fields have an "optional" tag on them in the XML.
     * If any of the EditText validations fail, that box is brought to focus.
     * <p/>
     * Finally, after everything is checked and made sure to be correct, a Patient object is created.
     * The object is filled with all the data that was provided, and then an Async Task is created to insert the information into the database.
     */
    public void createNewPatient() {

        boolean cancel = false;
        View focusView = null;


        if (dob.after(today)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IdentificationActivity.this);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();

            mDOBPicker.show();
            alertDialog.show();
            return;
        }

        ArrayList<EditText> values = new ArrayList<>();
        values.add(mFirstName);
        values.add(mMiddleName);
        values.add(mLastName);
        values.add(mDOB);
        values.add(mPhoneNum);
        values.add(mAddress1);
        values.add(mAddress2);
        values.add(mCity);
        values.add(mPostal);
        values.add(mRelationship);
        values.add(mOccupation);

        for (int i = 0; i < values.size(); i++) {
            EditText et = values.get(i);
            if (TextUtils.isEmpty(et.getText().toString()) && et.getTag() == null) {
                et.setError(getString(R.string.error_field_required));
                focusView = et;
                cancel = true;
                return;
            } else {
                et.setError(null);
            }
        }

        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IdentificationActivity.this);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }


        if (mCountry.getSelectedItemPosition() == 0) {
            countryText.setError(getString(R.string.error_field_required));
            focusView = countryText;
            cancel = true;
            return;
        } else {
            countryText.setError(null);
        }

        if (mState.getSelectedItemPosition() == 0) {
            stateText.setError(getString(R.string.error_field_required));
            focusView = stateText;
            cancel = true;
            return;
        } else {
            stateText.setError(null);
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            try {
                patient1.setFirstName(mFirstName.getText().toString());
                patient.setFirstName(mFirstName.getText().toString());
                if (TextUtils.isEmpty(mMiddleName.getText().toString())) {
                    patient.setMiddleName("");
                    patient1.setMiddleName("");
                } else {
                    patient.setMiddleName(mMiddleName.getText().toString());
                    patient1.setMiddleName(mMiddleName.getText().toString());
                }

                if (TextUtils.isEmpty(mLastName.getText().toString())) {
                    patient.setLastName("");
                    patient1.setLastName("");
                } else {
                    patient.setLastName(mLastName.getText().toString());
                    patient1.setLastName(mLastName.getText().toString());
                }
                if (TextUtils.isEmpty(mDOB.getText().toString())) {
                    patient.setDateOfBirth("");
                    patient1.setDateOfBirth("");
                } else {
                    patient.setDateOfBirth(mDOB.getText().toString());
                    patient1.setDateOfBirth(mDOB.getText().toString());
                }
                if (TextUtils.isEmpty(mPhoneNum.getText().toString())) {
                    patient.setPhoneNumber("");
                    patient1.setPhoneNumber("");
                } else {
                    patient.setPhoneNumber(mPhoneNum.getText().toString());
                    patient1.setPhoneNumber(mPhoneNum.getText().toString());
                }
                if (TextUtils.isEmpty(mAddress1.getText().toString())) {
                    patient.setAddress1("");
                    patient1.setAddress1("");
                } else {
                    patient.setAddress1(mAddress1.getText().toString());
                    patient1.setAddress1(mAddress1.getText().toString());
                }
                if (TextUtils.isEmpty(mAddress2.getText().toString())) {
                    patient.setAddress2("");
                    patient1.setAddress2("");
                } else {
                    patient.setAddress2(mAddress2.getText().toString());
                    patient1.setAddress2(mAddress2.getText().toString());
                }
                if (TextUtils.isEmpty(mCity.getText().toString())) {
                    patient.setCityVillage("");
                    patient1.setCityVillage("");
                } else {
                    patient.setCityVillage(mCity.getText().toString());
                    patient1.setCityVillage(mCity.getText().toString());
                }
//                if (TextUtils.isEmpty(mState.getText().toString())) {
//                    currentPatient.setStateProvince("");
//                } else {
//                    currentPatient.setStateProvince(mState.getText().toString());
//                }
                if (TextUtils.isEmpty(mPostal.getText().toString())) {
                    patient.setPostalCode("");
                    patient1.setPostalCode("");
                } else {
                    patient.setPostalCode(mPostal.getText().toString());
                    patient1.setPostalCode(mPostal.getText().toString());
                }
                if (TextUtils.isEmpty(mRelationship.getText().toString())) {
                    patient.setSdw("");
                    patient1.setSdw("");
                } else {
                    patient.setSdw(mRelationship.getText().toString());
                    patient1.setSdw(mRelationship.getText().toString());
                }
                if (TextUtils.isEmpty(mOccupation.getText().toString())) {
                    patient.setOccupation("");
                    patient1.setOccupation("");
                } else {
                    patient.setOccupation(mOccupation.getText().toString());
                    patient1.setOccupation(mOccupation.getText().toString());
                }

                //currentPatient.setCountry(mCountry.getText().toString());
                patient.setGender(mGender);
                patient1.setGender(mGender);
                patient.setCountry(mCountry.getSelectedItem().toString());
                patient1.setCountry(mCountry.getSelectedItem().toString());
                patient.setStateProvince(mState.getSelectedItem().toString());
                patient1.setStateProvince(mState.getSelectedItem().toString());
                Log.v(TAG, "" + mState.getSelectedItem());
            } catch (NullPointerException e) {
                Snackbar.make(findViewById(R.id.cl_table), R.string.identification_screen_error_data_fields, Snackbar.LENGTH_SHORT);
            }
            if (patientID_edit != null) {
                new UpdatePatientTable(patient1).execute();
            } else {
                new InsertPatientTable(patient).execute();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "Result Received");
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            Log.v(TAG, "Request Code " + CameraActivity.TAKE_IMAGE);
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");
                mCurrentPhotoPath = data.getStringExtra("RESULT");
                Log.v("IdentificationActivity", mCurrentPhotoPath);
                Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                mImageView.setImageBitmap(imageBitmap);
            }
        }
    }

    public class InsertPatientTable extends AsyncTask<Void, Void, Boolean>
            implements DialogInterface.OnCancelListener {

        SQLiteDatabase db4 = mDbHelper.getWritableDatabase();

        InsertPatientTable(Patient currentPatient) {
            patient = currentPatient;
        }

        ContentValues patientEntries = new ContentValues();
        ContentValues visitData = new ContentValues();

        public void gatherEntries() {
            patientEntries.put("_id", patient.getId());
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
            patientEntries.put("sdw", patient.getSdw());
            patientEntries.put("occupation", patient.getOccupation());
            patientEntries.put("patient_photo", mCurrentPhotoPath);

            //TODO: move identifier1 and id2 from patient table to patient_attribute table
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            gatherEntries();

            db4.insert(
                    "patient",
                    null,
                    patientEntries
            );
            db4.close();
            return null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            cancel(true);
            dialog.dismiss();
        }

        /**
         * Starting from Identification Activity, the patientID is passed between all activities.
         * All activities will put the ID out from the intent, and then package it into the next one after.
         * patientID - the ID number used for local storage; also used when posting to OpenMRS
         * tag - used when the user edits a patient's information
         */
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Intent intent2 = new Intent(IdentificationActivity.this, PatientDetailActivity.class);
            String fullName = patient.getFirstName() + " " + patient.getLastName();
            intent2.putExtra("patientID", patientID);
            intent2.putExtra("name", fullName);
            intent2.putExtra("tag", "new");
            startActivity(intent2);
        }
    }

    // This is a async method for updating the database if user changes any details of patient
    public class UpdatePatientTable extends AsyncTask<Void, Void, Boolean> implements DialogInterface.OnCancelListener {
        SQLiteDatabase db1 = mDbHelper.getWritableDatabase();

        UpdatePatientTable(Patient currepatient) {

            patient1 = currepatient;
        }

        ContentValues patientEntries1 = new ContentValues();

        public void gatherEntries1() {
            patientEntries1.put("_id", patient1.getId());
            patientEntries1.put("first_name", patient1.getFirstName());
            patientEntries1.put("middle_name", patient1.getMiddleName());
            patientEntries1.put("last_name", patient1.getLastName());
            patientEntries1.put("date_of_birth", patient1.getDateOfBirth());
            patientEntries1.put("phone_number", patient1.getPhoneNumber());
            patientEntries1.put("address1", patient1.getAddress1());
            patientEntries1.put("address2", patient1.getAddress2());
            patientEntries1.put("city_village", patient1.getCityVillage());
            patientEntries1.put("state_province", patient1.getStateProvince());
            patientEntries1.put("postal_code", patient1.getPostalCode());
            patientEntries1.put("country", patient1.getCountry());
            patientEntries1.put("gender", patient1.getGender());
            patientEntries1.put("sdw", patient1.getSdw());
            patientEntries1.put("occupation", patient1.getOccupation());

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            gatherEntries1();
            db1.update("patient", patientEntries1, "_id=?", new String[]{String.valueOf(patient1.getId())});

            db1.close();

            return null;
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }

        @Override
        protected void onPostExecute(Boolean bBoolean) {
            super.onPostExecute(bBoolean);
            Intent intent3 = new Intent(IdentificationActivity.this, PatientDetailActivity.class);
            String fullName = patient1.getFirstName() + " " + patient1.getLastName();

            intent3.putExtra("patientID", patientID_edit);
            intent3.putExtra("name", fullName);
            intent3.putExtra("tag", "new");

            startActivity(intent3);
        }
    }

    public static String getUserCountryFunction(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
//            Log.d("Country", simCountry);
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
//                Log.d("Country", networkCountry);
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String[] results = HelperMethods.dispatchTakePictureIntent(REQUEST_CAMERA, IdentificationActivity.this);
                if (results != null) {
                    mPhoto = results[0];
                    mCurrentPhotoPath = results[1];
                }
            } else {
                Log.e("Camera Permissions", "Permission Denied");
            }
        } else if (requestCode == REQUEST_READ_EXTERNAL) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e("Read/Write Permissions", "Permission Denied");
            }
        }
    }

    public void generateID() {
        SQLiteDatabase db1 = mDbHelper.getWritableDatabase();
        String table = "patient_content";
        String[] columnsToReturn = {"docid"};
        String orderBy = "docid";
        final Cursor idCursor = db1.query(table, columnsToReturn, null, null, null, null, orderBy);
        idCursor.moveToLast();

        if (idCursor.getCount() > 0) {
            // String lastIDString = idCursor.getString(idCursor.getColumnIndexOrThrow("_id")); //Grab the last patientID
            // Log.d(TAG, lastIDString);

            Integer lastIntegerID = idCursor.getInt(idCursor.getColumnIndexOrThrow("docid"));
            // TODO: Handle case where ID is changed to something else and then changed back
            // The above will most likely be solved by the automatic assignment of IDs in the future
          /*  try {
                if (lastIDString.substring(0, lastIDString.length() - 1).equals(idPreFix)) { // ID hasn't changed
                    String lastID = lastIDString.substring(idPreFix.length()); //Grab the last integer of the patientID
//                        Log.d(TAG, String.valueOf(lastID));
                    newInteger = Integer.valueOf(lastID);
                }
            } catch (Exception e) {
                newInteger = 0; // ID was probably changed
            } finally {
                Log.d(TAG, String.valueOf(newInteger));
                newInteger++; //Increment it by 1
            }*/
            lastIntegerID++;
            patientID = idPreFix + String.valueOf(lastIntegerID); //This patient is assigned the new incremented number
//                Log.d(TAG, patientID);
            patient.setId(patientID);
        } else {
            patientID = idPreFix + String.valueOf(1); //This patient is assigned the new incremented number
//                Log.d(TAG, patientID);
            patient.setId(patientID);
        }

        idCursor.close();
        patientID = patient.getId();
    }
}


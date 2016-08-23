package edu.jhu.bme.cbid.healthassistantsclient;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

/**
 * Created by Amal Afroz Alam on 3/25/16.
 */

public class IdentificationActivity extends AppCompatActivity {
    String LOG_TAG = "Identification Activity";

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
    DatePickerDialog mDOBPicker;

    String mPhoto;

    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase localdb;
    String idPreFix;
    String visitID;

    ImageView mImageView;
    String mCurrentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //Initialize the local database to store patient information
        mDbHelper = new LocalRecordsDatabaseHelper(this);
        localdb = mDbHelper.getWritableDatabase();
        idPreFix = "JHU";

        mFirstName = (EditText) findViewById(R.id.identification_first_name);
        mMiddleName = (EditText) findViewById(R.id.identification_middle_name);
        mLastName = (EditText) findViewById(R.id.identification_last_name);
        mDOB = (EditText) findViewById(R.id.identification_birth_date_text_view);
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

        //This is supposed to get the country that the tablet was set to, yet is not working due to unknown reasons.
        //TODO: Country should be auto-filled.
        String country = getUserCountryFunction(IdentificationActivity.this);
        //Log.d(LOG_TAG, country);
        mCountry.setText(country);

        //Check to see if the permission was given to take pictures.
        if (ContextCompat.checkSelfPermission(IdentificationActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(IdentificationActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 2); // 2 is a constant
        }

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
                dispatchTakePictureIntent();
            }
        });

        //DOB is set using an AlertDialog
        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);

                //Formatted so that it can be read the way the user sets
                //TODO: Settings option to change date format
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
                textInput.setView(dialogEditText);

                textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ageString = (dialogEditText.getText().toString());
                        mAge.setText(ageString);

                        Calendar calendar = Calendar.getInstance();
                        int curYear = calendar.get(Calendar.YEAR);
                        int birthYear = curYear - Integer.valueOf(ageString);
                        String calcDOB = String.valueOf(birthYear) + "-01-01";
                        mDOB.setText(calcDOB);
                        dialog.dismiss();
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

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.identification_gender_male:
                if (checked)
                    mGender = "M";
                break;
            case R.id.identification_gender_female:
                if (checked)
                    mGender = "F";
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
                    currentPatient.setMiddleName("");
                } else {
                    currentPatient.setMiddleName(mMiddleName.getText().toString());
                }
                if (TextUtils.isEmpty(mLastName.getText().toString())) {
                    currentPatient.setLastName("");
                } else {
                    currentPatient.setLastName(mLastName.getText().toString());
                }
                if (TextUtils.isEmpty(mDOB.getText().toString())) {
                    currentPatient.setDateOfBirth("");
                } else {
                    currentPatient.setDateOfBirth(mDOB.getText().toString());
                }
                if (TextUtils.isEmpty(mPhoneNum.getText().toString())) {
                    currentPatient.setPhoneNumber("");
                } else {
                    currentPatient.setPhoneNumber(mPhoneNum.getText().toString());
                }
                if (TextUtils.isEmpty(mAddress1.getText().toString())) {
                    currentPatient.setAddress1("");
                } else {
                    currentPatient.setAddress1(mAddress1.getText().toString());
                }
                if (TextUtils.isEmpty(mAddress2.getText().toString())) {
                    currentPatient.setAddress2("");
                } else {
                    currentPatient.setAddress2(mAddress2.getText().toString());
                }
                if (TextUtils.isEmpty(mCity.getText().toString())) {
                    currentPatient.setCityVillage("");
                } else {
                    currentPatient.setCityVillage(mCity.getText().toString());
                }
                if (TextUtils.isEmpty(mState.getText().toString())) {
                    currentPatient.setStateProvince("");
                } else {
                    currentPatient.setStateProvince(mState.getText().toString());
                }
                if (TextUtils.isEmpty(mPostal.getText().toString())) {
                    currentPatient.setPostalCode("");
                } else {
                    currentPatient.setPostalCode(mPostal.getText().toString());
                }
                if (TextUtils.isEmpty(mRelationship.getText().toString())) {
                    currentPatient.setPatientIdentifier1("");
                } else {
                    currentPatient.setPatientIdentifier1(mRelationship.getText().toString());
                }
                if (TextUtils.isEmpty(mOccupation.getText().toString())) {
                    currentPatient.setPatientIdentifier2("");
                } else {
                    currentPatient.setPatientIdentifier2(mOccupation.getText().toString());
                }
                //currentPatient.setCountry(mCountry.getText().toString());
                currentPatient.setGender(mGender);
            } catch (NullPointerException e) {
                Snackbar.make(findViewById(R.id.cl_table), R.string.identification_screen_error_data_fields, Snackbar.LENGTH_SHORT);
            }

            new InsertPatientTable(currentPatient).execute();
        }
    }

    /**
     * Once a picture of the patient is taken, this method timestamps the picture and stores it.
     *
     * @return File
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        mPhoto = "PATIENT_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                mPhoto,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getAbsolutePath();

        //TODO: upload this to google drive using a service, and then store the public share link into android


        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            mImageView.setImageBitmap(imageBitmap);
        }
    }

    public class InsertPatientTable extends AsyncTask<Void, Void, Boolean>
            implements DialogInterface.OnCancelListener {

        String patientID;
        Patient patient;

        InsertPatientTable(Patient currentPatient) {
            patient = currentPatient;
        }


        ContentValues patientEntries = new ContentValues();
        ContentValues visitData = new ContentValues();

        public void generateID(){
            String table = "patient";
            String[] columnsToReturn = {"patient_id"};
            String orderBy = "patient_id";
            final Cursor idCursor = localdb.query(table, columnsToReturn, null, null, null, null, orderBy);
            idCursor.moveToLast();
            String lastIDString = idCursor.getString(idCursor.getColumnIndexOrThrow("_id")); //Grab the last patientID
            idCursor.close();

            if (lastIDString != null) {
                String lastID = lastIDString.substring(idPreFix.length()); //Grab the last integer of the patientID
                Log.d(LOG_TAG, String.valueOf(lastID));
                Integer newInteger = Integer.valueOf(lastID) + 1; //Increment it by 1
                Log.d(LOG_TAG, String.valueOf(newInteger));
                patientID = idPreFix + String.valueOf(newInteger); //This patient is assigned the new incremented number
                Log.d(LOG_TAG, patientID);
                patient.setId(patientID);
            } else {
                patientID = idPreFix + String.valueOf(1); //This patient is assigned the new incremented number
                Log.d(LOG_TAG, patientID);
                patient.setId(patientID);
            }

            patientID = patient.getId();
        }

        public void gatherEntries() {
            patientEntries.put("patient_id", patient.getId());
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
            patientEntries.put("patient_photo", mCurrentPhotoPath);

            //TODO: move identifier1 and id2 from patient table to patient_attribute table
        }

        public void gatherVisitData() {
            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date todayDate = new Date();
            String thisDate = currentDate.format(todayDate);

            visitData.put("patient_id", patient.getId());
            visitData.put("start_datetime", thisDate);
            visitData.put("visit_type_id", 0);
            visitData.put("visit_location_id", 0);
            visitData.put("visit_creator", 0);
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            generateID();
            gatherEntries();
            gatherVisitData();

            localdb.insert(
                    "patient",
                    null,
                    patientEntries
            );

            Long visitLong = localdb.insert(
                    "visit",
                    null,
                    visitData
            );

            visitID = String.valueOf(visitLong);

            localdb.close();

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
         * status - used for other activities that change based on "new" or "return" patients
         * tag - used when the user edits a patient's information
         */
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Intent intent2 = new Intent(IdentificationActivity.this, PatientHistoryActivity.class);
            String fullName = patient.getFirstName() + " " + patient.getLastName();
            intent2.putExtra("patientID", patientID);
            intent2.putExtra("visitID", visitID);
            intent2.putExtra("name", fullName);
            intent2.putExtra("status", "new");
            intent2.putExtra("tag", "");
            startActivity(intent2);
        }
    }

    public static String getUserCountryFunction(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            Log.d("Country", simCountry);
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                Log.d("Country", networkCountry);
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


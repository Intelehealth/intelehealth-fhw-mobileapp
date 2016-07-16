package edu.jhu.bme.cbid.healthassistantsclient;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    String mFileName;

    final Calendar today = Calendar.getInstance();
    final Calendar dob = Calendar.getInstance();

    private InsertPatientTable mTask = null;
    LocalRecordsDatabaseHelper mDbHelper;


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

        mDbHelper = new LocalRecordsDatabaseHelper(this);

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


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(IdentificationActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(IdentificationActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 2); // 2 is a contanst
        }

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


        mImageView = (ImageView) findViewById(R.id.imageview_id_picture);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        //Change minimum


        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dob.set(year, monthOfYear, dayOfMonth);

                int month = monthOfYear + 1;
                String formattedMonth = "" + month;
                String formattedDayOfMonth = "" + dayOfMonth;

                if (month < 10) {

                    formattedMonth = "0" + month;
                }
                if (dayOfMonth < 10) {

                    formattedDayOfMonth = "0" + dayOfMonth;
                }
                String dobString = formattedDayOfMonth + "/" + formattedMonth + "/" + year;

                //can use SimpleDateFormat, but couldn't get it to work
                mDOB.setText(dobString);

                //Age calculation
                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                mAge.setText(String.valueOf(age));

            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));


        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentDOB = mDOB.getText().toString();
                if (currentDOB.matches("")) ;
                {
                    mDOBPicker.show();

                }
            }
        });

        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentAge = mAge.getText().toString();
                if (currentAge.matches("")) ;
                {

                    Context context = IdentificationActivity.this;

                    final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
                    textInput.setTitle(R.string.dialog_age);
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
                            String calcDOB = "01/01/" + birthYear;
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

        if(!mGenderF.isChecked() && !mGenderM.isChecked()){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IdentificationActivity.this);
            alertDialogBuilder.setMessage(R.string.dialog_error_gender);
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

        if (dob.after(today)){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IdentificationActivity.this);
            alertDialogBuilder.setMessage(R.string.dialog_error_dob);
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        mFileName = "PATIENT_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                mFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getAbsolutePath();

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

        Long patientID;
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
            patientEntries.put("patient_identifier3", mCurrentPhotoPath);
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

            ContentValues contentValuesUpdate = new ContentValues();
            contentValuesUpdate.put("_id", patient.getId());
            String selection = "first_name = ?";
            String[] args = {patient.getFirstName()};

            localdb.update(
                    "patient",
                    contentValuesUpdate,
                    selection,
                    args
            );

            Log.d(LOG_TAG, String.valueOf(patientID));

            //Gson gson = new GsonBuilder().serializeNulls().create();
            //Log.i("Patient", gson.toJson(patient));


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
            Intent intent2 = new Intent(IdentificationActivity.this, ComplaintNodeActivity.class);
            intent2.putExtra("patientID", patientID);
            startActivity(intent2);
        }
    }
}


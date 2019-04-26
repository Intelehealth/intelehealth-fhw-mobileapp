package io.intelehealth.client.views.activites;

import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.databinding.ActivityIdentificationBinding;
import io.intelehealth.client.utilities.ConfigUtils;
import io.intelehealth.client.utilities.DateAndTimeUtils;
import io.intelehealth.client.utilities.EditTextUtils;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UuidGenerator;
import io.intelehealth.client.viewModels.IdentificationViewModel;
import io.intelehealth.client.viewModels.requestModels.Patient;

public class IdentificationActivity extends AppCompatActivity {
    private static final String TAG = IdentificationActivity.class.getSimpleName();
    ActivityIdentificationBinding binding;
    IdentificationViewModel identificationViewModel;
    SessionManager sessionManager = null;
    InteleHealthDatabaseHelper mDbHelper = null;
    private boolean hasLicense = false;
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;
    UuidGenerator uuidGenerator = new UuidGenerator();
    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();
    Patient patient1;
    Patient patient_new1;
    private String patientUuid = "";
    private String mGender;
    private int patientID_edit = -1;
    private int mDOBYear;
    private int mDOBMonth;
    private int mDOBDay;
    private DatePickerDialog mDOBPicker;
    private int mAgeYears = 0;
    private int mAgeMonths = 0;
    private AlertDialog.Builder mAgePicker;
    private String country1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_identification);
        identificationViewModel = ViewModelProviders.of(this).get(IdentificationViewModel.class);
        binding.setIdentificationViewModel(identificationViewModel);
        binding.setLifecycleOwner(this);

        setTitle(R.string.title_activity_identification);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionManager = new SessionManager(this);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        identificationViewModel.getPatient().observe(this, new Observer<Patient>() {
            @Override
            public void onChanged(@Nullable Patient patient) {
                identificationViewModel.onPatientCreateClicked();
            }
        });
//Initialize the local database to store patient information
        mDbHelper = new InteleHealthDatabaseHelper(this);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("pid")) {
                this.setTitle("Update Patient");
                patientID_edit = intent.getIntExtra("pid", -1);
            }
        }
        if (sessionManager.valueContains("licensekey"))
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            String mFileName = "config.json";
            if (hasLicense) {
                obj = new JSONObject(FileUtils.readFileRoot(mFileName, this)); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));

            }
            ConfigUtils configUtils = new ConfigUtils(this);

            //Display the fields on the Add Patient screen as per the config file

            String country1;
            country1 = obj.getString("mCountry");

            if (country1.equalsIgnoreCase("India")) {
                EditTextUtils.setEditTextMaxLength(10, binding.identificationPhoneNumber);
            } else if (country1.equalsIgnoreCase("Philippines")) {
                EditTextUtils.setEditTextMaxLength(11, binding.identificationPhoneNumber);
            }

        } catch (JSONException e) {
            e.printStackTrace();
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }
        Resources res = getResources();
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCountry.setAdapter(countryAdapter);

        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(this,
                R.array.caste, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCaste.setAdapter(casteAdapter);
        try {
            String economicLanguage = "economic_" + Locale.getDefault().getLanguage();
            int economics = res.getIdentifier(economicLanguage, "array", getApplicationContext().getPackageName());
            if (economics != 0) {
                economicStatusAdapter = ArrayAdapter.createFromResource(this,
                        economics, android.R.layout.simple_spinner_item);
            }
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerEconomicStatus.setAdapter(economicStatusAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Economic values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try {
            String educationLanguage = "education_" + Locale.getDefault().getLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(this,
                        educations, android.R.layout.simple_spinner_item);

            }
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerEducation.setAdapter(educationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Education values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // generate patientid only if there is no intent for Identification activity

        // generate patientid only if there is no intent for Identification activity

        if (patientID_edit == -1) {
            generateUuid();
//            backup orginal code
//            generateID();
        }

        // setting radio button automatically according to the databse when user clicks edit details
        if (patientID_edit != -1) {
            if (patient_new1.getGender().equals("M")) {
                binding.identificationGenderMale.setChecked(true);
                if (binding.identificationGenderFemale.isChecked())
                    binding.identificationGenderFemale.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                binding.identificationGenderFemale.setChecked(true);
                if (binding.identificationGenderMale.isChecked())
                    binding.identificationGenderMale.setChecked(false);
                Log.v(TAG, "yes");
            }
        }
        if (binding.identificationGenderMale.isChecked()) {
            mGender = "M";
        } else {
            mGender = "F";
        }
        if (patientID_edit != -1) {
            // setting country according database
            binding.spinnerCountry.setSelection(countryAdapter.getPosition(String.valueOf(patient_new1.getCountry())));
            if (patient_new1.getEducation_level().equals(getString(R.string.not_provided)))
                binding.spinnerEducation.setSelection(0);
            else
                binding.spinnerEducation.setSelection(educationAdapter.getPosition(patient_new1.getEducation_level()));
            if (patient_new1.getEconomic_status().equals(getString(R.string.not_provided)))
                binding.spinnerEconomicStatus.setSelection(0);
            else
                binding.spinnerEconomicStatus.setSelection(economicStatusAdapter.getPosition(patient_new1.getEconomic_status()));
            if (patient_new1.getCaste().equals(getString(R.string.not_provided)))
                binding.spinnerCaste.setSelection(0);
            else
                binding.spinnerCaste.setSelection(casteAdapter.getPosition(patient_new1.getCaste()));
        } else {
            binding.spinnerCountry.setSelection(countryAdapter.getPosition(country1));
        }

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.state_error, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerState.setAdapter(stateAdapter);

        binding.spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String state = parent.getItemAtPosition(position).toString();
                if (state.matches("Odisha")) {
                    //Creating the instance of ArrayAdapter containing list of fruit names
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.odisha_villages, android.R.layout.simple_spinner_item);
                    binding.identificationCity.setThreshold(1);//will start working from first character
                    binding.identificationCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else if (state.matches("Bukidnon")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.bukidnon_villages, android.R.layout.simple_spinner_item);
                    binding.identificationCity.setThreshold(1);//will start working from first character
                    binding.identificationCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else {
                    binding.identificationCity.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String country = adapterView.getItemAtPosition(i).toString();

                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_india, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerState.setAdapter(stateAdapter);
                        // setting state according database when user clicks edit details

                        if (patientID_edit != -1) {
                            binding.spinnerState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {
                            binding.spinnerState.setSelection(stateAdapter.getPosition("Odisha"));
                        }

                    } else if (country.matches("United States")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_us, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerState.setAdapter(stateAdapter);

                        if (patientID_edit != -1) {

                            binding.spinnerState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        }
                    } else if (country.matches("Philippines")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_philippines, android.R.layout.simple_spinner_item);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerState.setAdapter(stateAdapter);

                        if (patientID_edit != -1) {
                            binding.spinnerState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {
                            binding.spinnerState.setSelection(stateAdapter.getPosition("Bukidnon"));
                        }
                    }
                } else {
                    ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.state_error, android.R.layout.simple_spinner_item);
                    stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerState.setAdapter(stateAdapter);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.identificationGenderFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        binding.identificationGenderMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        binding.imageviewIdPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String[] results = HelperMethods.startImageCapture(IdentificationActivity.this,
                //        IdentificationActivity.this);
                //if (results != null) {
                //    mPhoto = results[0];
                //    mCurrentPhotoPath = results[1];
                //}
                File filePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator +
                        "Patient_Images" + File.separator + patientUuid);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                Intent cameraIntent = new Intent(IdentificationActivity.this, CameraActivity.class);

                // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patientUuid);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath);
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
            }
        });
        mDOBYear = today.get(Calendar.YEAR);
        mDOBMonth = today.get(Calendar.MONTH);
        mDOBDay = today.get(Calendar.DAY_OF_MONTH);
        //DOB is set using an AlertDialog
        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                binding.identificationBirthDateTextView.setError(null);
                binding.identificationAge.setError(null);
                //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);

                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault());
                dob.set(year, monthOfYear, dayOfMonth);
                String dobString = simpleDateFormat.format(dob.getTime());
                binding.identificationBirthDateTextView.setText(dobString);

                mAgeYears = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                mAgeMonths = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);


                if (mAgeMonths < 0) {
                    mAgeMonths = mAgeMonths + 12;
                    mAgeYears = mAgeYears - 1;
                }

                if (mAgeMonths < 0 || mAgeYears < 0 || dob.after(today)) {
                    binding.identificationBirthDateTextView.setError(getString(R.string.identification_screen_error_dob));
                    binding.identificationAge.setError(getString(R.string.identification_screen_error_age));
                    return;
                }

                mDOBYear = year;
                mDOBMonth = monthOfYear;
                mDOBDay = dayOfMonth;

                String ageString = mAgeYears + getString(R.string.identification_screen_text_years) + mAgeMonths + getString(R.string.identification_screen_text_months);
                binding.identificationAge.setText(ageString);
            }
        }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        binding.identificationBirthDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });

        //if patient update then age will be set
        if (patientID_edit != -1) {
            int age = DateAndTimeUtils.getAge(patient_new1.getDate_of_birth());
            binding.identificationBirthDateTextView.setText(patient_new1.getDate_of_birth());
            int month = DateAndTimeUtils.getMonth(patient_new1.getDate_of_birth());
            binding.identificationAge.setText(age + getString(R.string.identification_screen_text_years) + month + getString(R.string.identification_screen_text_months));
        }
        binding.identificationAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAgePicker = new AlertDialog.Builder(IdentificationActivity.this, R.style.AlertDialogStyle);
                mAgePicker.setTitle(R.string.identification_screen_prompt_age);
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
                mAgePicker.setView(convertView);
                final NumberPicker yearPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
                final NumberPicker monthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
                final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
                final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
                middleText.setText(getString(R.string.identification_screen_picker_years));
                endText.setText(getString(R.string.identification_screen_picker_months));
                yearPicker.setMinValue(0);
                yearPicker.setMaxValue(100);
                monthPicker.setMinValue(0);
                monthPicker.setMaxValue(12);
                if (mAgeYears > 0) {
                    yearPicker.setValue(mAgeYears);
                }
                if (mAgeMonths > 0) {
                    monthPicker.setValue(mAgeMonths);
                }

                mAgePicker.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yearPicker.setValue(yearPicker.getValue());
                        monthPicker.setValue(monthPicker.getValue());
                        String ageString = yearPicker.getValue() + getString(R.string.identification_screen_text_years) + monthPicker.getValue() + getString(R.string.identification_screen_text_months);
                        binding.identificationAge.setText(ageString);


                        Calendar calendar = Calendar.getInstance();
                        int curYear = calendar.get(Calendar.YEAR);
                        int birthYear = curYear - yearPicker.getValue();
                        int curMonth = calendar.get(Calendar.MONTH);
                        int birthMonth = curMonth - monthPicker.getValue();
                        mDOBYear = birthYear;
                        mDOBMonth = birthMonth;
                        mDOBDay = 1;

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.getDefault());
                        dob.set(mDOBYear, mDOBMonth, mDOBDay);
                        String dobString = simpleDateFormat.format(dob.getTime());
                        binding.identificationBirthDateTextView.setText(dobString);
                        mDOBPicker.updateDate(mDOBYear, mDOBMonth, mDOBDay);
                        dialog.dismiss();
                    }
                });
                mAgePicker.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                mAgePicker.show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                createNewPatient();
            }
        });


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

    public void generateUuid() {

        patientUuid = uuidGenerator.UuidGenerator();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to go back ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setNegativeButton("No", null).show();

    }

    public void showAlertDialogButtonClicked(String errorMessage) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Config Error");
        alertDialogBuilder.setMessage(errorMessage);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Intent i = new Intent(IdentificationActivity.this, SetupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// This flag ensures all activities on top of the CloseAllViewsDemo are cleared.
                startActivity(i);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}

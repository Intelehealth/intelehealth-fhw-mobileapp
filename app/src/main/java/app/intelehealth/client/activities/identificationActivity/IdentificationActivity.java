package app.intelehealth.client.activities.identificationActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.patientDetailActivity.PatientDetailActivity;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.database.dao.ImagesDAO;
import app.intelehealth.client.database.dao.ImagesPushDAO;
import app.intelehealth.client.database.dao.PatientsDAO;
import app.intelehealth.client.database.dao.SyncDAO;
import app.intelehealth.client.models.Patient;
import app.intelehealth.client.models.dto.PatientAttributesDTO;
import app.intelehealth.client.models.dto.PatientDTO;
import app.intelehealth.client.utilities.DateAndTimeUtils;
import app.intelehealth.client.utilities.EditTextUtils;
import app.intelehealth.client.utilities.FileUtils;
import app.intelehealth.client.utilities.IReturnValues;
import app.intelehealth.client.utilities.Logger;
import app.intelehealth.client.utilities.SessionManager;
import app.intelehealth.client.utilities.UuidGenerator;

import app.intelehealth.client.activities.cameraActivity.CameraActivity;
import app.intelehealth.client.activities.homeActivity.HomeActivity;
import app.intelehealth.client.activities.setupActivity.SetupActivity;
import app.intelehealth.client.utilities.NetworkConnection;
import app.intelehealth.client.utilities.StringUtils;
import app.intelehealth.client.utilities.exception.DAOException;

public class IdentificationActivity extends AppCompatActivity {
    private static final String TAG = IdentificationActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;
    UuidGenerator uuidGenerator = new UuidGenerator();
    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();
    Patient patient1 = new Patient();
    private String patientUuid = "";
    private String mGender;
    String patientID_edit;
    private int mDOBYear;
    private int mDOBMonth;
    private int mDOBDay;
    private DatePickerDialog mDOBPicker;
    private int mAgeYears = 0;
    private int mAgeMonths = 0;
    private int mAgeDays = 0;
    private String country1,state;
    PatientsDAO patientsDAO = new PatientsDAO();
    EditText mFirstName;
    EditText mMiddleName;
    EditText mLastName;
    EditText mDOB;
    EditText mPhoneNum;
    EditText mAge;
    MaterialAlertDialogBuilder mAgePicker;
    EditText mAddress1;
    EditText mAddress2;
    AutoCompleteTextView mCity;
    EditText mPostal;
    RadioGroup radioGroup;
    RadioButton mGenderM;
    RadioButton mGenderF;
    EditText mRelationship;
    EditText mOccupation;
    EditText countryText;
    EditText stateText;
    EditText casteText;
    Spinner mCountry;
    Spinner mState;
    EditText economicText;
    EditText educationText;
    TextInputLayout casteLayout;
    TextInputLayout economicLayout;
    TextInputLayout educationLayout;
    LinearLayout countryStateLayout;
    Spinner mCaste;
    Spinner mEducation;
    Spinner mEconomicStatus;
    ImageView mImageView;
    String uuid = "";
    PatientDTO patientdto = new PatientDTO();
    ImagesDAO imagesDAO = new ImagesDAO();
    private String mCurrentPhotoPath;
    Context context;
    private String BlockCharacterSet_Others = "0123456789\\@$!=><&^*+€¥£`~";
    private String BlockCharacterSet_Name = "\\@$!=><&^*+\"\'€¥£`~";

    Intent i_privacy;
    String privacy_value;
    private int retainPickerYear;
    private int retainPickerMonth;
    private int retainPickerDate;

    //Health_Scheme_Fields
    MaterialCheckBox ma_checkbox, ab_checkbox, none_checkbox;
    FrameLayout frameLayout;
    TextView health_textview, address_details_textview, personal_info_textview;
    String html_health, result_selection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        setTitle(R.string.title_activity_identification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        i_privacy = getIntent();
        context = IdentificationActivity.this;
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        sessionManager = new SessionManager(this);
        mFirstName = findViewById(R.id.identification_first_name);
        mFirstName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        mMiddleName = findViewById(R.id.identification_middle_name);
        mMiddleName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        mLastName = findViewById(R.id.identification_last_name);
        mLastName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        radioGroup = findViewById(R.id.radioGrp);

        mDOB = findViewById(R.id.identification_birth_date_text_view);
        mPhoneNum = findViewById(R.id.identification_phone_number);

     /*   mPhoneNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    if(mPhoneNum.getText().toString().trim().length() < 10)
                        mPhoneNum.setError("Enter 10 digits");
                    else
                        mPhoneNum.setError(null);
                }
            }
        });*/

        mAge = findViewById(R.id.identification_age);
        mAddress1 = findViewById(R.id.identification_address1);
        mAddress1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50

        mAddress2 = findViewById(R.id.identification_address2);
        mAddress2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50

        mCity = findViewById(R.id.identification_city);
        mCity.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        stateText = findViewById(R.id.identification_state);
        mState = findViewById(R.id.spinner_state);
        mPostal = findViewById(R.id.identification_postal_code);
        countryText = findViewById(R.id.identification_country);
        mCountry = findViewById(R.id.spinner_country);
        mGenderM = findViewById(R.id.identification_gender_male);
        mGenderF = findViewById(R.id.identification_gender_female);
        address_details_textview = findViewById(R.id.address_details_textview);
        personal_info_textview = findViewById(R.id.personal_info_textview);
        mRelationship = findViewById(R.id.identification_relationship);
        mRelationship.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        mOccupation = findViewById(R.id.identification_occupation);
        mOccupation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        mCaste = findViewById(R.id.spinner_caste);
        mEducation = findViewById(R.id.spinner_education);
        mEconomicStatus = findViewById(R.id.spinner_economic_status);
        casteText = findViewById(R.id.identification_caste);
        educationText = findViewById(R.id.identification_education);
        economicText = findViewById(R.id.identification_econiomic_status);

        casteLayout = findViewById(R.id.identification_txtlcaste);
        economicLayout = findViewById(R.id.identification_txtleconomic);
        educationLayout = findViewById(R.id.identification_txtleducation);
        countryStateLayout = findViewById(R.id.identification_llcountry_state);
        mImageView = findViewById(R.id.imageview_id_picture);

        ma_checkbox = findViewById(R.id.ma_checkbox);
        ab_checkbox = findViewById(R.id.ab_checkbox);
        none_checkbox = findViewById(R.id.none_checkbox);
        frameLayout = findViewById(R.id.health_framelayout);
        health_textview = findViewById(R.id.health_textview);

//Initialize the local database to store patient information

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                this.setTitle(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
            }
        }
//        if (sessionManager.valueContains("licensekey"))
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context),
                                String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

            //Display the fields on the Add Patient screen as per the config file
            if (obj.getBoolean("mFirstName")) {
                mFirstName.setVisibility(View.VISIBLE);
            } else {
                mFirstName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mMiddleName")) {
                mMiddleName.setVisibility(View.VISIBLE);
            } else {
                mMiddleName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mLastName")) {
                mLastName.setVisibility(View.VISIBLE);
            } else {
                mLastName.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mDOB")) {
                mDOB.setVisibility(View.VISIBLE);
            } else {
                mDOB.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPhoneNum")) {
                mPhoneNum.setVisibility(View.VISIBLE);
            } else {
                mPhoneNum.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAge")) {
                mAge.setVisibility(View.VISIBLE);
            } else {
                mAge.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress1")) {
                mAddress1.setVisibility(View.VISIBLE);
            } else {
                mAddress1.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress2")) {
                mAddress2.setVisibility(View.VISIBLE);
            } else {
                mAddress2.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mCity")) {
                mCity.setVisibility(View.VISIBLE);
            } else {
                mCity.setVisibility(View.GONE);
            }

            if (obj.getBoolean("countryStateLayout")) {
                countryStateLayout.setVisibility(View.VISIBLE);
            } else {
                countryStateLayout.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPostal")) {
                mPostal.setVisibility(View.VISIBLE);
            } else {
                mPostal.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mGenderM")) {
                mGenderM.setVisibility(View.VISIBLE);
            } else {
                mGenderM.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mGenderF")) {
                mGenderF.setVisibility(View.VISIBLE);
            } else {
                mGenderF.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mRelationship")) {
                mRelationship.setVisibility(View.VISIBLE);
            } else {
                mRelationship.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mOccupation")) {
                mOccupation.setVisibility(View.VISIBLE);
            } else {
                mOccupation.setVisibility(View.GONE);
            }
            if (obj.getBoolean("casteLayout")) {
                casteLayout.setVisibility(View.VISIBLE);
            } else {
                casteLayout.setVisibility(View.GONE);
            }
            if (obj.getBoolean("educationLayout")) {
                educationLayout.setVisibility(View.VISIBLE);
            } else {
                educationLayout.setVisibility(View.GONE);
            }
            if (obj.getBoolean("economicLayout")) {
                economicLayout.setVisibility(View.VISIBLE);
            } else {
                economicLayout.setVisibility(View.GONE);
            }
            country1 = obj.getString("mCountry");
            state = obj.getString("mState");

            if (country1.equalsIgnoreCase("India")) {
                EditTextUtils.setEditTextMaxLength(10, mPhoneNum);
            } else if (country1.equalsIgnoreCase("Philippines")) {
                EditTextUtils.setEditTextMaxLength(11, mPhoneNum);
            }

            if (obj.getBoolean("health_scheme_card")) {
                frameLayout.setVisibility(View.VISIBLE);
            } else {
                frameLayout.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }

        //setting the fields when user clicks edit details
        mFirstName.setText(patient1.getFirst_name());
        mMiddleName.setText(patient1.getMiddle_name());
        mLastName.setText(patient1.getLast_name());
        mDOB.setText(patient1.getDate_of_birth());
        mPhoneNum.setText(patient1.getPhone_number());
        mAddress1.setText(patient1.getAddress1());
        mAddress2.setText(patient1.getAddress2());
        mCity.setText(patient1.getCity_village());
        mPostal.setText(patient1.getPostal_code());
        mRelationship.setText(patient1.getSdw());
        mOccupation.setText(patient1.getOccupation());
        //helath_scheme...
        Log.d("Health_scheme", "Scheme: " + patient1.getHealth_scheme());
        if (patient1.getHealth_scheme() != null && !patient1.getHealth_scheme().isEmpty()) {

            if (patient1.getHealth_scheme().equalsIgnoreCase("Mukhyamantri Amrutam scheme")) {
                ma_checkbox.setChecked(true);
            } else if (patient1.getHealth_scheme().equalsIgnoreCase("Ayushman Bharat Card")) {
                ab_checkbox.setChecked(true);
            } else if (patient1.getHealth_scheme().equalsIgnoreCase("None of the above")) {
                none_checkbox.setChecked(true);
            } else if (patient1.getHealth_scheme().equalsIgnoreCase
                    ("Mukhyamantri Amrutam scheme, " + "Ayushman Bharat Card")) {
                ma_checkbox.setChecked(true);
                ab_checkbox.setChecked(true);
            }
        } else {
            ma_checkbox.setChecked(false);
            ab_checkbox.setChecked(false);
            none_checkbox.setChecked(false);
        }

        if (patient1.getPatient_photo() != null && !patient1.getPatient_photo().trim().isEmpty())
            mImageView.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatient_photo()));

        Resources res = getResources();
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, R.layout.custom_spinner);
        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountry.setAdapter(countryAdapter);

        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(this,
                R.array.caste, R.layout.custom_spinner);
        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCaste.setAdapter(casteAdapter);
        try {
            String economicLanguage = "economic_" + Locale.getDefault().getLanguage();
            int economics = res.getIdentifier(economicLanguage, "array", getApplicationContext().getPackageName());
            if (economics != 0) {
                economicStatusAdapter = ArrayAdapter.createFromResource(this,
                        economics, R.layout.custom_spinner);
            }
            // countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mEconomicStatus.setAdapter(economicStatusAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Economic values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try {
            String educationLanguage = "education_" + Locale.getDefault().getLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(this,
                        educations, R.layout.custom_spinner);

            }
            // countryAdapter.setDropDownViewResource(R.layout.custom_spinner);
            mEducation.setAdapter(educationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Education values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }


        if (null == patientID_edit || patientID_edit.isEmpty()) {
            generateUuid();

        }

        // setting radio button automatically according to the databse when user clicks edit details
        if (patientID_edit != null) {

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
            // setting country according database
            mCountry.setSelection(countryAdapter.getPosition(String.valueOf(patient1.getCountry())));

            if (patient1.getEducation_level().equals(getString(R.string.not_provided)))
                mEducation.setSelection(0);
            else
                mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
            if (educationAdapter == null) {
                Toast.makeText(context, "Education Level: " + patient1.getEducation_level(), Toast.LENGTH_LONG).show();
            }


            if (patient1.getEconomic_status().equals(getString(R.string.not_provided)))
                mEconomicStatus.setSelection(0);
            else
                mEconomicStatus.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));

            if (patient1.getCaste().equals(getString(R.string.not_provided)))
                mCaste.setSelection(0);
            else
                mCaste.setSelection(casteAdapter.getPosition(patient1.getCaste()));
        } else {
            mCountry.setSelection(countryAdapter.getPosition(country1));
        }

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.state_error, R.layout.custom_spinner);
        //  stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(stateAdapter);

        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stateText.setError(null);
                String state = parent.getItemAtPosition(position).toString();
                if (state.matches("Odisha")) {
                    //Creating the instance of ArrayAdapter containing list of fruit names
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.odisha_villages, R.layout.custom_spinner);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else if (state.matches("Bukidnon")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.bukidnon_villages, R.layout.custom_spinner);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else {
                    mCity.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                if (i != 0) {
                    countryText.setError(null); //to clear the error message on clicked...
                    String country = adapterView.getItemAtPosition(i).toString();

                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_india, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);
                        // setting state according database when user clicks edit details

                        if (patientID_edit != null)
                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        else
                            mState.setSelection(stateAdapter.getPosition(state));

                        //just a comment..
                    } else if (country.matches("United States")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_us, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);

                        if (patientID_edit != null) {

                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        }
                    } else if (country.matches("Philippines")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_philippines, R.layout.custom_spinner);
                        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);

                        if (patientID_edit != null) {
                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {
                            mState.setSelection(stateAdapter.getPosition("Bukidnon"));
                        }
                    }
                } else {
                    ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.state_error, R.layout.custom_spinner);
                    // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mState.setAdapter(stateAdapter);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String state = parent.getItemAtPosition(position).toString();
                if (state.matches("Odisha")) {
                    //Creating the instance of ArrayAdapter containing list of fruit names
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.odisha_villages, R.layout.custom_spinner);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                }
                else if (state.matches("Bukidnon")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.bukidnon_villages, R.layout.custom_spinner);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                }
                else if(state.matches("Gujarat")) {
                    ArrayAdapter<CharSequence> adapter =
                            ArrayAdapter.createFromResource(IdentificationActivity.this,
                                    R.array.gujarat_villages, R.layout.custom_spinner);
                    mCity.setThreshold(1);   //will start working from first character
                    mCity.setAdapter(adapter);  //setting the adapter data into the AutoCompleteTextView
                }
                else {
                    mCity.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientTemp = "";
                if (patientUuid.equalsIgnoreCase("")) {
                    patientTemp = patientID_edit;
                } else {
                    patientTemp = patientUuid;
                }
                File filePath = new File(AppConstants.IMAGE_PATH + patientTemp);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                Intent cameraIntent = new Intent(IdentificationActivity.this, CameraActivity.class);

                // cameraIntent.putExtra(CameraActivity.SHOW_DIALOG_MESSAGE, getString(R.string.camera_dialog_default));
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patientTemp);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
            }
        });
        mDOBYear = today.get(Calendar.YEAR);
        mDOBMonth = today.get(Calendar.MONTH);
        mDOBDay = today.get(Calendar.DAY_OF_MONTH);
        //DOB is set using an AlertDialog
        Locale.setDefault(Locale.ENGLISH);

        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                mDOB.setError(null);
                mAge.setError(null);
                //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                Locale.setDefault(Locale.ENGLISH);
                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                dob.set(year, monthOfYear, dayOfMonth);
                String dobString = simpleDateFormat.format(dob.getTime());
                mDOB.setText(dobString);
                mDOBYear = year;
                mDOBMonth = monthOfYear;
                mDOBDay = dayOfMonth;

                String age = getYear(dob.get(Calendar.YEAR), dob.get(Calendar.MONTH), dob.get(Calendar.DATE), today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
                //get years months days
                String[] frtData = age.split("-");

                String[] yearData = frtData[0].split(" ");
                String[] monthData = frtData[1].split(" ");
                String[] daysData = frtData[2].split(" ");

                mAgeYears = Integer.valueOf(yearData[0]);
                mAgeMonths = Integer.valueOf(monthData[1]);
                mAgeDays = Integer.valueOf(daysData[1]);
                mAge.setText(age);

            }
        }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });
        //if patient update then age will be set
        if (patientID_edit != null) {
            mDOB.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
            //get year month days
            String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth(), context);

            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth()).split(" ");
            mAgeYears = Integer.valueOf(ymdData[0]);
            mAgeMonths = Integer.valueOf(ymdData[1]);
            mAgeDays = Integer.valueOf(ymdData[2]);
            mAge.setText(yrMoDays);
        }
        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAgePicker = new MaterialAlertDialogBuilder(IdentificationActivity.this, R.style.AlertDialogStyle);
                mAgePicker.setTitle(R.string.identification_screen_prompt_age);
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
                mAgePicker.setView(convertView);
                NumberPicker yearPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
                NumberPicker monthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
                NumberPicker dayPicker = convertView.findViewById(R.id.dialog_3_numbers_unit);
                dayPicker.setVisibility(View.VISIBLE);

                final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
                final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
                final TextView dayTv = convertView.findViewById(R.id.dialog_2_numbers_text_3);
                dayPicker.setVisibility(View.VISIBLE);

                int totalDays = today.getActualMaximum(Calendar.DAY_OF_MONTH);
                dayTv.setText(getString(R.string.days));
                middleText.setText(getString(R.string.identification_screen_picker_years));
                endText.setText(getString(R.string.identification_screen_picker_months));


                yearPicker.setMinValue(0);
                yearPicker.setMaxValue(100);
                monthPicker.setMinValue(0);
                monthPicker.setMaxValue(12);

                dayPicker.setMinValue(0);
                dayPicker.setMaxValue(31);

                EditText yearText = yearPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
                EditText monthText = monthPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
                EditText dayText = dayPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));


                yearPicker.setValue(mAgeYears);
                monthPicker.setValue(mAgeMonths);
                dayPicker.setValue(mAgeDays);

                //year
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mAgeYears = Integer.valueOf(value);
                    }
                }, yearText);

                //month
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mAgeMonths = Integer.valueOf(value);
                    }
                }, monthText);

                //day
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mAgeDays = Integer.valueOf(value);
                    }
                }, dayText);
                mAgePicker.setPositiveButton(R.string.generic_ok, (dialog, which) -> {
                    String ageString = mAgeYears + getString(R.string.identification_screen_text_years) + " - " +
                            mAgeMonths + getString(R.string.identification_screen_text_months) + " - " +
                            mAgeDays + getString(R.string.days);
                    mAge.setText(ageString);


                    Calendar calendar = Calendar.getInstance();
                    int curYear = calendar.get(Calendar.YEAR);
                    //int birthYear = curYear - yearPicker.getValue();
                    int birthYear = curYear - mAgeYears;
                    int curMonth = calendar.get(Calendar.MONTH);
                    //int birthMonth = curMonth - monthPicker.getValue();
                    int birthMonth = curMonth - mAgeMonths;
                    //int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - dayPicker.getValue();
                    int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - mAgeDays;
                    mDOBYear = birthYear;
                    mDOBMonth = birthMonth;

                    if (birthDay < 0) {
                        mDOBDay = birthDay + totalDays - 1;
                        mDOBMonth--;

                    } else {
                        mDOBDay = birthDay;
                    }
                    Locale.setDefault(Locale.ENGLISH);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    dob.set(mDOBYear, mDOBMonth, mDOBDay);
                    String dobString = simpleDateFormat.format(dob.getTime());
                    mDOB.setText(dobString);
                    mDOBPicker.updateDate(mDOBYear, mDOBMonth, mDOBDay);
                    dialog.dismiss();
                });
                mAgePicker.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = mAgePicker.show();
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
            }
        });

        ma_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                health_textview.setError(null);
                none_checkbox.setChecked(false);
            }
        });

        ab_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                health_textview.setError(null);
                none_checkbox.setChecked(false);
            }
        });

        none_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                health_textview.setError(null);
                ma_checkbox.setChecked(false);
                ab_checkbox.setChecked(false);
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (patientID_edit != null) {
                onPatientUpdateClicked(patient1);
            } else {
                onPatientCreateClicked();
            }
        });
    }

    public String getYear(int syear, int smonth, int sday, int eyear, int emonth, int eday) {
        String calculatedAge = null;
        int resmonth;
        int resyear;
        int resday;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            LocalDate today = LocalDate.now();
            LocalDate birthday = LocalDate.of(syear, smonth + 1, sday);

            Period p = Period.between(birthday, today);
            System.out.println(p.getDays());
            System.out.println(p.getMonths());
            System.out.println(p.getYears());
            calculatedAge = p.getYears() + " years  - " + p.getMonths() + " month - " + p.getDays() + " days";


        } else {

            //calculating year
            resyear = eyear - syear;

            //calculating month
            if (emonth >= smonth) {
                resmonth = emonth - smonth;
            } else {
                resmonth = emonth - smonth;
                resmonth = 12 + resmonth;
                resyear--;
            }

            //calculating date
            if (eday >= sday) {
                resday = eday - sday;
            } else {
                resday = eday - sday;
                resday = 30 + resday;
                if (resmonth == 0) {
                    resmonth = 11;
                    resyear--;
                } else {
                    resmonth--;
                }
            }

            //displaying error if calculated age is negative
            if (resday < 0 || resmonth < 0 || resyear < 0) {
                Toast.makeText(this, R.string.current_date_must_be, Toast.LENGTH_LONG).show();
                mDOB.setError(getString(R.string.identification_screen_error_dob));
                mAge.setError(getString(R.string.identification_screen_error_age));
            } else {
                // t1.setText("Age: " + resyear + " years /" + resmonth + " months/" + resday + " days");

                calculatedAge = resyear + " years - " + resmonth + " months - " + resday + " days";
            }
        }

        return calculatedAge != null ? calculatedAge : " ";
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
        mGenderM.setError(null);
        mGenderF.setError(null);
    }

    private InputFilter inputFilter_Name = new InputFilter() { //filter input for name fields
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null && BlockCharacterSet_Name.contains(("" + charSequence))) {
                return "";
            }
            return null;
        }
    };

    private InputFilter inputFilter_Others = new InputFilter() { //filter input for all other fields
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            if (charSequence != null && BlockCharacterSet_Others.contains(("" + charSequence))) {
                return "";
            }
            return null;
        }
    };

    public void generateUuid() {

        patientUuid = uuidGenerator.UuidGenerator();

    }

    // This method is for setting the screen with existing values in database whenn user clicks edit details
    private void setscreen(String str) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection = "uuid=?";
        String[] patientArgs = {str};
        String[] patientColumns = {"uuid", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo",
                "economic_status", "education_status", "caste", "health_scheme"};
        Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
        if (idCursor.moveToFirst()) {
            do {
                patient1.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                patient1.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient1.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient1.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient1.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient1.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient1.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient1.setCity_village(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient1.setState_province(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient1.setPostal_code(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient1.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient1.setPhone_number(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient1.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient1.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient1.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient1.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                patient1.setHealth_scheme(idCursor.getString(idCursor.getColumnIndexOrThrow("health_scheme")));

            } while (idCursor.moveToNext());
            idCursor.close();
        }
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {str};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        final Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient1.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient1.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient1.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient1.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient1.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient1.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if(name.equalsIgnoreCase("Health Scheme Card")){
                    patient1.setHealth_scheme(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }


    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.are_you_want_go_back);
        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent i_back = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i_back);
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.generic_no, null);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    public void showAlertDialogButtonClicked(String errorMessage) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
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
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result Received");
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            Log.v(TAG, "Request Code " + CameraActivity.TAKE_IMAGE);
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");
                mCurrentPhotoPath = data.getStringExtra("RESULT");
                Log.v("IdentificationActivity", mCurrentPhotoPath);

                Glide.with(this)
                        .load(new File(mCurrentPhotoPath))
                        .thumbnail(0.25f)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(mImageView);
            }
        }
    }

    public void onPatientCreateClicked() {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = UUID.randomUUID().toString();

        patientdto.setUuid(uuid);
        Gson gson = new Gson();

//        boolean cancel = false;
//        View focusView = null;


        // New Validation
        if(mFirstName.getText().toString().equals("") && mFirstName.getText().toString().isEmpty() && mLastName.getText().toString().equals("") && mLastName.getText().toString().isEmpty() && !mGenderF.isChecked() && !mGenderM.isChecked() && mDOB.getText().toString().equals("") && mDOB.getText().toString().isEmpty() && mAge.getText().toString().equals("") && mAge.getText().toString().isEmpty() && mCity.getText().toString().equals("") && mCity.getText().toString().isEmpty())
        {
            personal_info_textview.requestFocus();

            mFirstName.setError(getString(R.string.error_field_required));
//            mFirstName.requestFocus();

            mLastName.setError(getString(R.string.error_field_required));
//            mLastName.requestFocus();

            mGenderF.setError(getString(R.string.error_field_required));
//            mGenderF.requestFocus();

            mDOB.setError(getString(R.string.error_field_required));
//            mDOB.requestFocus();

            mAge.setError(getString(R.string.error_field_required));
//            mAge.requestFocus();

//            countryText.setError(getString(R.string.error_field_required));
//            countryText.requestFocus();

//            stateText.setError(getString(R.string.error_field_required));
//            stateText.requestFocus();

            mCity.setError(getString(R.string.error_field_required));
//            mCity.requestFocus();

            if (frameLayout.getVisibility() == View.VISIBLE) {
                if (!ma_checkbox.isChecked() && !ab_checkbox.isChecked() && !none_checkbox.isChecked()) {
                    health_textview.setError(getString(R.string.error_field_required));
                    /*ma_checkbox.setError(getString(R.string.error_field_required));
                    ab_checkbox.setError(getString(R.string.error_field_required));
                    none_checkbox.setError(getString(R.string.error_field_required));*/
                }
            }

            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_required_fields);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
            personal_info_textview.clearFocus();
            return;
        }


        if (mFirstName.getText().toString().equals("") && mFirstName.getText().toString().isEmpty()) {
            mFirstName.setError(getString(R.string.error_field_required));
            mFirstName.requestFocus();
            return;
        }

        if (mLastName.getText().toString().equals("") && mLastName.getText().toString().isEmpty()) {
            mLastName.setError(getString(R.string.error_field_required));
            mLastName.requestFocus();
            return;
        }

        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            mGenderF.setError(getString(R.string.error_field_required));
            personal_info_textview.requestFocus();
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
            personal_info_textview.clearFocus();
            return;
        }

        if (mDOB.getText().toString().equals("") && mDOB.getText().toString().isEmpty()) {
            mDOB.setError(getString(R.string.error_field_required));
            personal_info_textview.requestFocus();
            Toast.makeText(this, R.string.please_enter_DOB, Toast.LENGTH_SHORT).show();
            personal_info_textview.clearFocus();
            return;
        }

        if (dob.equals("") || dob.toString().equals("")) {
            if (dob.after(today)) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
                //alertDialogBuilder.setMessage(getString(R.string.identification_dialog_date_error));
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();

                mDOBPicker.show();
                alertDialog.show();

                Button postiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                postiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                // postiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
                return;
            }
        }

        if (mAge.getText().toString().equals("") && mAge.getText().toString().isEmpty()) {
            mAge.setError(getString(R.string.error_field_required));
            mAge.requestFocus();
            return;
        }

        if (mCountry.getSelectedItemPosition() == 0) {
            countryText.setError(getString(R.string.error_field_required));
//            mCountry.requestFocus();
            address_details_textview.requestFocus();
            Toast.makeText(this, R.string.please_select_country, Toast.LENGTH_SHORT).show();
            address_details_textview.clearFocus();
            return;
        } else {
            countryText.setError(null);
        }

        if (mState.getSelectedItemPosition() == 0) {
            stateText.setError(getString(R.string.error_field_required));
//            mState.requestFocus();
            address_details_textview.requestFocus();
            Toast.makeText(this, R.string.please_select_state, Toast.LENGTH_SHORT).show();
            address_details_textview.clearFocus();
            return;
        }
        else {
            stateText.setError(null);
        }

        if (mCity.getText().toString().equals("") && mCity.getText().toString().isEmpty()) {
            mCity.setError(getString(R.string.error_field_required));
            address_details_textview.requestFocus();
            Toast.makeText(this, R.string.please_enter_village, Toast.LENGTH_SHORT).show();
            address_details_textview.clearFocus();
            return;
        }

        if (frameLayout.getVisibility() == View.VISIBLE) {
            if (!ma_checkbox.isChecked() && !ab_checkbox.isChecked() && !none_checkbox.isChecked()) {
                health_textview.setError(getString(R.string.error_field_required));
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle("Health Scheme Card");
                alertDialogBuilder.setMessage(R.string.please_select_option_hsc);
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

                return;
            }
        }


        if(mPhoneNum.getText().toString().trim().length() > 0) {
            if(mPhoneNum.getText().toString().trim().length() < 10) {
                mPhoneNum.requestFocus();
                mPhoneNum.setError("Enter 10 digits");
                return;
            }
        }

   /*     ArrayList<EditText> values = new ArrayList<>();
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
        values.add(mOccupation);*/

/*
        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            return;
        }
*/

//if(frameLayout.getVisibility() == View.VISIBLE) {
//
//    if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
//            && !mCity.getText().toString().equals("") && !countryText.getText().toString().equals("") &&
//            !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked()) && (ma_checkbox.isChecked() || ab_checkbox.isChecked() || none_checkbox.isChecked())) {
//
//        Log.v(TAG, "Result");
//
//    } else {
//
//        if(mFirstName.getText().toString().equals("") || mLastName.getText().toString().equals("") || mCity.getText().toString().equals("") || mDOB.getText().toString().equals("") || mAge.getText().toString().equals(""))
//        {
//            Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
//
//            if (mFirstName.getText().toString().equals("")) {
//                mFirstName.setError(getString(R.string.error_field_required));
//            }
//
//            if (mLastName.getText().toString().equals("")) {
//                mLastName.setError(getString(R.string.error_field_required));
//            }
//
//            if (mDOB.getText().toString().equals("")) {
//                mDOB.setError(getString(R.string.error_field_required));
//            }
//
//            if (mAge.getText().toString().equals("")) {
//                mAge.setError(getString(R.string.error_field_required));
//            }
//
//            if (mCity.getText().toString().equals("")) {
//                mCity.setError(getString(R.string.error_field_required));
//            }
//
////            Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
//            return;
//        }
//
//
//        if (frameLayout.getVisibility() == View.VISIBLE) {
//            if (!ma_checkbox.isChecked() && !ab_checkbox.isChecked() && !none_checkbox.isChecked()) {
//                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//                alertDialogBuilder.setTitle("Health Scheme Card");
//                alertDialogBuilder.setMessage("Please select the option for Health Scheme Card");
//                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alertDialog = alertDialogBuilder.create();
//                alertDialog.show();
//
//                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
//
////                health_textview.setError("Please Select an option");
////                health_textview.requestFocus();
//            }
//
//        }
//
//        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
//            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//            alertDialogBuilder.setTitle(R.string.error);
//            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
//            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            AlertDialog alertDialog = alertDialogBuilder.create();
//            alertDialog.show();
//
//            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//            //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
//
//        }
//
//
////        Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
//        return;
//    }
//}
//else
//{
//        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
//                && !mCity.getText().toString().equals("") && !countryText.getText().toString().equals("") &&
//                !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked())) {
//
//            Log.v(TAG, "Result");
//
//        } else {
//            if (mFirstName.getText().toString().equals("")) {
//                mFirstName.setError(getString(R.string.error_field_required));
//            }
//
//            if (mLastName.getText().toString().equals("")) {
//                mLastName.setError(getString(R.string.error_field_required));
//            }
//
//            if (mDOB.getText().toString().equals("")) {
//                mDOB.setError(getString(R.string.error_field_required));
//            }
//
//            if (mAge.getText().toString().equals("")) {
//                mAge.setError(getString(R.string.error_field_required));
//            }
//
//            if (mCity.getText().toString().equals("")) {
//                mCity.setError(getString(R.string.error_field_required));
//            }
//
//            if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
//                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//                alertDialogBuilder.setTitle(R.string.error);
//                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
//                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alertDialog = alertDialogBuilder.create();
//                alertDialog.show();
//
//                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
//
//            }
//
//
//            Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
//        }
//}
//        if (mCountry.getSelectedItemPosition() == 0) {
//            countryText.setError(getString(R.string.error_field_required));
//            focusView = countryText;
//            cancel = true;
//            return;
//        } else {
//            countryText.setError(null);
//        }
//
//
//        if (mState.getSelectedItemPosition() == 0) {
//            stateText.setError(getString(R.string.error_field_required));
//            focusView = stateText;
//            cancel = true;
//            return;
//        } else {
//            stateText.setError(null);
//        }


//        if (cancel) {
//            focusView.requestFocus();
//        } else {

        patientdto.setFirstname(StringUtils.getValue(mFirstName.getText().toString()));
        patientdto.setMiddlename(StringUtils.getValue(mMiddleName.getText().toString()));
        patientdto.setLastname(StringUtils.getValue(mLastName.getText().toString()));
        patientdto.setPhonenumber(StringUtils.getValue(mPhoneNum.getText().toString()));
        patientdto.setGender(StringUtils.getValue(mGender));
        patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(mDOB.getText().toString())));
        patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
        patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));
        patientdto.setCityvillage(StringUtils.getValue(mCity.getText().toString()));
        patientdto.setPostalcode(StringUtils.getValue(mPostal.getText().toString()));
        patientdto.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
        patientdto.setPatientPhoto(mCurrentPhotoPath);
//          patientdto.setEconomic(StringUtils.getValue(m));
        patientdto.setStateprovince(StringUtils.getValue(mState.getSelectedItem().toString()));

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
        patientAttributesDTO.setValue(StringUtils.getProvided(mCaste));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
        patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNum.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
        patientAttributesDTO.setValue(StringUtils.getValue(mRelationship.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
        patientAttributesDTO.setValue(StringUtils.getValue(mOccupation.getText().toString()));
        patientAttributesDTOList.add(patientAttributesDTO);


            if(frameLayout.getVisibility() == View.VISIBLE)
            {
                html_health = health_condition();
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid
                        (patientsDAO.getUuidForAttribute("Health Scheme Card"));
                patientAttributesDTO.setValue(StringUtils
                        .getValue(html_health));
                patientAttributesDTOList.add(patientAttributesDTO);
            }

//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid
//                    (patientsDAO.getUuidForAttribute("Health insurance card"));
//            patientAttributesDTO.setValue(StringUtils
//                    .getValue(html_health));
//            patientAttributesDTOList.add(patientAttributesDTO);
//        }

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
        patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicStatus));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
        patientAttributesDTO.setValue(StringUtils.getProvided(mEducation));
        patientAttributesDTOList.add(patientAttributesDTO);

        patientAttributesDTO = new PatientAttributesDTO();
        patientAttributesDTO.setUuid(UUID.randomUUID().toString());
        patientAttributesDTO.setPatientuuid(uuid);
        patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ProfileImageTimestamp"));
        patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());

        patientAttributesDTOList.add(patientAttributesDTO);
        Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
        patientdto.setPatientAttributesDTOList(patientAttributesDTOList);
        patientdto.setSyncd(false);
        Logger.logD("patient json : ", "Json : " + gson.toJson(patientdto, PatientDTO.class));

//        }

        try {
            Logger.logD(TAG, "insertpatinet ");
            boolean isPatientInserted = patientsDAO.insertPatientToDB(patientdto, uuid);
            boolean isPatientImageInserted = imagesDAO.insertPatientProfileImages(mCurrentPhotoPath, uuid);

            if (NetworkConnection.isOnline(getApplication())) {
//                patientApiCall();
//                frameJson();

//                AppConstants.notificationUtils.showNotifications(getString(R.string.patient_data_upload),
//                        getString(R.string.uploading) + patientdto.getFirstname() + "" + patientdto.getLastname() +
//                                "'s data", 2, getApplication());

                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean push = syncDAO.pushDataApi();
                boolean pushImage = imagesPushDAO.patientProfileImagesPush();

//                if (push)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirstname() + "" + patientdto.getLastname() + "'s data upload complete.", 2, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirstname() + "" + patientdto.getLastname() + "'s data not uploaded.", 2, getApplication());

//                if (pushImage)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirstname() + "" + patientdto.getLastname() + "'s Image upload complete.", 4, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirstname() + "" + patientdto.getLastname() + "'s Image not complete.", 4, getApplication());


//
            }
//            else {
//                AppConstants.notificationUtils.showNotifications(getString(R.string.patient_data_failed), getString(R.string.check_your_connectivity), 2, IdentificationActivity.this);
//            }
            if (isPatientInserted && isPatientImageInserted) {
                Logger.logD(TAG, "inserted");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirstname() + " " + patientdto.getLastname());
                i.putExtra("tag", "newPatient");
                i.putExtra("privacy", privacy_value);
                i.putExtra("hasPrescription", "false");
                Log.d(TAG, "Privacy Value on (Identification): " + privacy_value); //privacy value transferred to PatientDetail activity.
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(i);
            } else {
                Toast.makeText(IdentificationActivity.this, "Error of adding the data", Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    public void onPatientUpdateClicked(Patient patientdto) {
        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = patientdto.getUuid();

        patientdto.setUuid(uuid);
        Gson gson = new Gson();

//        boolean cancel = false;
//        View focusView = null;

//        if (dob.equals("") || dob.toString().equals("")) {
//            if (dob.after(today)) {
//                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//                alertDialogBuilder.setTitle(R.string.error);
//                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
//                //alertDialogBuilder.setMessage(getString(R.string.identification_dialog_date_error));
//                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alertDialog = alertDialogBuilder.create();
//
//                mDOBPicker.show();
//                alertDialog.show();
//
//                Button postiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                postiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//                // postiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
//                return;
//            }
//        }

        // New Validation
        if(mFirstName.getText().toString().equals("") && mFirstName.getText().toString().isEmpty() && mLastName.getText().toString().equals("") && mLastName.getText().toString().isEmpty() && !mGenderF.isChecked() && !mGenderM.isChecked() && mDOB.getText().toString().equals("") && mDOB.getText().toString().isEmpty() && mAge.getText().toString().equals("") && mAge.getText().toString().isEmpty() && mCity.getText().toString().equals("") && mCity.getText().toString().isEmpty())
        {
            personal_info_textview.requestFocus();

            mFirstName.setError(getString(R.string.error_field_required));
//            mFirstName.requestFocus();

            mLastName.setError(getString(R.string.error_field_required));
//            mLastName.requestFocus();

            //gender..
//            mGenderM.setError(getString(R.string.error_field_required));
            mGenderF.setError(getString(R.string.error_field_required));

            mDOB.setError(getString(R.string.error_field_required));
//            mDOB.requestFocus();

            mAge.setError(getString(R.string.error_field_required));
//            mAge.requestFocus();

//            countryText.setError(getString(R.string.error_field_required));
//            countryText.requestFocus();

//            stateText.setError(getString(R.string.error_field_required));
//            stateText.requestFocus();

            mCity.setError(getString(R.string.error_field_required));
//            mCity.requestFocus();

            if (frameLayout.getVisibility() == View.VISIBLE) {
                if (!ma_checkbox.isChecked() && !ab_checkbox.isChecked() && !none_checkbox.isChecked()) {
                    health_textview.setError(getString(R.string.error_field_required));
                    /*ma_checkbox.setError(getString(R.string.error_field_required));
                    ab_checkbox.setError(getString(R.string.error_field_required));
                    none_checkbox.setError(getString(R.string.error_field_required));*/
                }
            }

            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_required_fields);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
            personal_info_textview.clearFocus();
            return;
        }

        if (mFirstName.getText().toString().equals("") && mFirstName.getText().toString().isEmpty()) {
            mFirstName.setError(getString(R.string.error_field_required));
            mFirstName.requestFocus();
            return;
        }

        if (mLastName.getText().toString().equals("") && mLastName.getText().toString().isEmpty()) {
            mLastName.setError(getString(R.string.error_field_required));
            mLastName.requestFocus();
            return;
        }

        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            mGenderF.setError(getString(R.string.error_field_required));
            personal_info_textview.requestFocus();
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
            personal_info_textview.clearFocus();
            return;
        }

        if (mDOB.getText().toString().equals("") && mDOB.getText().toString().isEmpty()) {
            mDOB.setError(getString(R.string.error_field_required));
            personal_info_textview.requestFocus();
            Toast.makeText(this, "Please enter Date Of Birth", Toast.LENGTH_SHORT).show();
            personal_info_textview.clearFocus();
            return;
        }

        if (dob.equals("") || dob.toString().equals("")) {
            if (dob.after(today)) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_dob);
                //alertDialogBuilder.setMessage(getString(R.string.identification_dialog_date_error));
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();

                mDOBPicker.show();
                alertDialog.show();

                Button postiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                postiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                // postiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
                return;
            }
        }

        if (mAge.getText().toString().equals("") && mAge.getText().toString().isEmpty()) {
            mAge.setError(getString(R.string.error_field_required));
            mAge.requestFocus();
            return;
        }

        if (mCountry.getSelectedItemPosition() == 0) {
            countryText.setError(getString(R.string.error_field_required));
//            mCountry.requestFocus();
            address_details_textview.requestFocus();
            Toast.makeText(this, "Please select Country", Toast.LENGTH_SHORT).show();
            address_details_textview.clearFocus();
            return;
        } else {
            countryText.setError(null);
        }

        if (mState.getSelectedItemPosition() == 0) {
            stateText.setError(getString(R.string.error_field_required));
//            mState.requestFocus();
            address_details_textview.requestFocus();
            Toast.makeText(this, "Please select State", Toast.LENGTH_SHORT).show();
            address_details_textview.clearFocus();
            return;
        } else {
            stateText.setError(null);
        }

        if (mCity.getText().toString().equals("") && mCity.getText().toString().isEmpty()) {
            mCity.setError(getString(R.string.error_field_required));
            address_details_textview.requestFocus();
            Toast.makeText(this, "Please enter Village/Town/City", Toast.LENGTH_SHORT).show();
            address_details_textview.clearFocus();
            return;
        }

        if (frameLayout.getVisibility() == View.VISIBLE) {
            if (!ma_checkbox.isChecked() && !ab_checkbox.isChecked() && !none_checkbox.isChecked()) {
                health_textview.setError(getString(R.string.error_field_required));
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle("Health Scheme Card");
                alertDialogBuilder.setMessage("Please select the option for Health Scheme Card");
                alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

                return;
            }
        }


        if(mPhoneNum.getText().toString().trim().length() > 0) {
            if(mPhoneNum.getText().toString().trim().length() < 10) {
                mPhoneNum.requestFocus();
                mPhoneNum.setError("Enter 10 digits");
                return;
            }
        }

       /* ArrayList<EditText> values = new ArrayList<>();
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
        values.add(mOccupation);*/

/*
        if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
            alertDialogBuilder.setTitle(R.string.error);
            alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            return;
        }
*/

//        if (frameLayout.getVisibility() == View.VISIBLE) {
//            if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
//                    && !mCity.getText().toString().equals("") && !countryText.getText().toString().equals("") &&
//                    !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked()) && (ma_checkbox.isChecked() || ab_checkbox.isChecked() || none_checkbox.isChecked())) {
//
//                Log.v(TAG, "Result");
//
//            } else {
//                if (mFirstName.getText().toString().equals("")) {
//                    mFirstName.setError(getString(R.string.error_field_required));
//                }
//
//                if (mLastName.getText().toString().equals("")) {
//                    mLastName.setError(getString(R.string.error_field_required));
//                }
//
//                if (mDOB.getText().toString().equals("")) {
//                    mDOB.setError(getString(R.string.error_field_required));
//                }
//
//                if (mAge.getText().toString().equals("")) {
//                    mAge.setError(getString(R.string.error_field_required));
//                }
//
//                if (mCity.getText().toString().equals("")) {
//                    mCity.setError(getString(R.string.error_field_required));
//                }
//
//                if (frameLayout.getVisibility() == View.VISIBLE) {
//                    if (!ma_checkbox.isChecked() && !ab_checkbox.isChecked() && !none_checkbox.isChecked()) {
//                        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//                        alertDialogBuilder.setTitle("Health Scheme Card");
//                        alertDialogBuilder.setMessage("Please select the option for Health Scheme Card");
//                        alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        AlertDialog alertDialog = alertDialogBuilder.create();
//                        alertDialog.show();
//
//                        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//                        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//                        IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
//
////                health_textview.setError("Please Select an option");
////                health_textview.requestFocus();
//                    }
//
//                }
//
//                if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
//                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//                    alertDialogBuilder.setTitle(R.string.error);
//                    alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
//                    alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    AlertDialog alertDialog = alertDialogBuilder.create();
//                    alertDialog.show();
//
//                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//                    //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//                    IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
//
//                }
//
//
//                Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
//                return;
//            }
//        } else {
//            if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
//                    && !mCity.getText().toString().equals("") && !countryText.getText().toString().equals("") &&
//                    !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked())) {
//
//                Log.v(TAG, "Result");
//
//            } else {
//                if (mFirstName.getText().toString().equals("")) {
//                    mFirstName.setError(getString(R.string.error_field_required));
//                }
//
//                if (mLastName.getText().toString().equals("")) {
//                    mLastName.setError(getString(R.string.error_field_required));
//                }
//
//                if (mDOB.getText().toString().equals("")) {
//                    mDOB.setError(getString(R.string.error_field_required));
//                }
//
//                if (mAge.getText().toString().equals("")) {
//                    mAge.setError(getString(R.string.error_field_required));
//                }
//
//                if (mCity.getText().toString().equals("")) {
//                    mCity.setError(getString(R.string.error_field_required));
//                }
//
//                if (!mGenderF.isChecked() && !mGenderM.isChecked()) {
//                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this);
//                    alertDialogBuilder.setTitle(R.string.error);
//                    alertDialogBuilder.setMessage(R.string.identification_screen_dialog_error_gender);
//                    alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    AlertDialog alertDialog = alertDialogBuilder.create();
//                    alertDialog.show();
//
//                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//                    //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//                    IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
//
//                }
//
//
//                Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
//                return;
//            }
//        }
//        if (mCountry.getSelectedItemPosition() == 0) {
//            countryText.setError(getString(R.string.error_field_required));
//            focusView = countryText;
//            cancel = true;
//            return;
//        } else {
//            countryText.setError(null);
//        }
//
//
//        if (mState.getSelectedItemPosition() == 0) {
//            stateText.setError(getString(R.string.error_field_required));
//            focusView = stateText;
//            cancel = true;
//            return;
//        } else {
//            stateText.setError(null);
//        }

//        if (cancel) {
//            focusView.requestFocus();
//        } else {
//            if (mCurrentPhotoPath == null)
//                mCurrentPhotoPath = patientdto.getPatient_photo();


            patientdto.setFirst_name(StringUtils.getValue(mFirstName.getText().toString()));
            patientdto.setMiddle_name(StringUtils.getValue(mMiddleName.getText().toString()));
            patientdto.setLast_name(StringUtils.getValue(mLastName.getText().toString()));
            patientdto.setPhone_number(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientdto.setGender(StringUtils.getValue(mGender));
            patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(mDOB.getText().toString())));
            patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));
            patientdto.setCity_village(StringUtils.getValue(mCity.getText().toString()));
            patientdto.setPostal_code(StringUtils.getValue(mPostal.getText().toString()));
            patientdto.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
            patientdto.setPatient_photo(mCurrentPhotoPath);
//                patientdto.setEconomic(StringUtils.getValue(m));
            patientdto.setState_province(StringUtils.getValue(patientdto.getState_province()));
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mCaste));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRelationship.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
            patientAttributesDTO.setValue(StringUtils.getValue(mOccupation.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);


            //based on availability of this atribute, it will be passed to the Edit field to update the attribute value.
            if (frameLayout.getVisibility() == View.VISIBLE) {
                html_health = health_condition();
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid
                        (patientsDAO.getUuidForAttribute("Health Scheme Card"));
                patientAttributesDTO.setValue(StringUtils
                        .getValue(html_health));
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicStatus));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mEducation));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ProfileImageTimestamp"));
            patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());

            patientAttributesDTOList.add(patientAttributesDTO);
            Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
            //patientdto.setPatientAttributesDTOList(patientAttributesDTOList);

            Logger.logD("patient json onPatientUpdateClicked : ", "Json : " + gson.toJson(patientdto, Patient.class));

//        }


        try {
            Logger.logD(TAG, "update ");
            boolean isPatientUpdated = patientsDAO.updatePatientToDB(patientdto, uuid, patientAttributesDTOList);
            boolean isPatientImageUpdated = imagesDAO.updatePatientProfileImages(mCurrentPhotoPath, uuid);

            if (NetworkConnection.isOnline(getApplication())) {
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean ispush = syncDAO.pushDataApi();
                boolean isPushImage = imagesPushDAO.patientProfileImagesPush();

//                if (ispush)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s data upload complete.", 2, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s data not uploaded.", 2, getApplication());

//                if (isPushImage)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s Image upload complete.", 4, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientdto.getFirst_name() + "" + patientdto.getLast_name() + "'s Image not complete.", 4, getApplication());

            }
            if (isPatientUpdated && isPatientImageUpdated) {
                Logger.logD(TAG, "updated");
                Intent i = new Intent(getApplication(), PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientdto.getFirst_name() + " " + patientdto.getLast_name());
                i.putExtra("tag", "newPatient");
                i.putExtra("hasPrescription", "false");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(i);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    public String health_condition() {
        if (ma_checkbox.isChecked() && !ab_checkbox.isChecked()) {
            html_health = ma_checkbox.getText().toString();
            result_selection = "option_1";
        } else if (ab_checkbox.isChecked() && !ma_checkbox.isChecked()) {
            html_health = ab_checkbox.getText().toString();
            result_selection = "option_2";
        } else if (none_checkbox.isChecked()) {
            html_health = none_checkbox.getText().toString();
            result_selection = "option_3";
        } else if (ma_checkbox.isChecked() && ab_checkbox.isChecked()) {
            html_health = ma_checkbox.getText() + ", " + ab_checkbox.getText();
            result_selection = "option_4";
        }

        return html_health;
    }

}

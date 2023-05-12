package org.intelehealth.unicef.activities.identificationActivity;

import static org.intelehealth.unicef.utilities.StringUtils.mSwitch_Country;
import static org.intelehealth.unicef.utilities.StringUtils.mSwitch_Country_edit;
import static org.intelehealth.unicef.utilities.StringUtils.mSwitch_State;
import static org.intelehealth.unicef.utilities.StringUtils.ru__or_dob;
import static org.intelehealth.unicef.utilities.StringUtils.switch_hi_caste_edit;
import static org.intelehealth.unicef.utilities.StringUtils.switch_hi_economic_edit;
import static org.intelehealth.unicef.utilities.StringUtils.switch_hi_education_edit;
import static org.intelehealth.unicef.utilities.StringUtils.switch_or_caste_edit;
import static org.intelehealth.unicef.utilities.StringUtils.switch_or_economic_edit;
import static org.intelehealth.unicef.utilities.StringUtils.switch_or_education_edit;
import static org.intelehealth.unicef.utilities.StringUtils.switch_ru_economic;
import static org.intelehealth.unicef.utilities.StringUtils.switch_ru_economic_edit;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.telecom.Call;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.multidex.MultiDex;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.cameraActivity.CameraActivity;
import org.intelehealth.unicef.activities.homeActivity.HomeActivity;
import org.intelehealth.unicef.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.unicef.activities.setupActivity.SetupActivity;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.database.dao.ImagesDAO;
import org.intelehealth.unicef.database.dao.ImagesPushDAO;
import org.intelehealth.unicef.database.dao.PatientsDAO;
import org.intelehealth.unicef.database.dao.SyncDAO;
import org.intelehealth.unicef.models.Patient;
import org.intelehealth.unicef.models.dto.PatientAttributesDTO;
import org.intelehealth.unicef.models.dto.PatientDTO;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.EditTextUtils;
import org.intelehealth.unicef.utilities.FileUtils;
import org.intelehealth.unicef.utilities.IReturnValues;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.NetworkConnection;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.StringUtils;
import org.intelehealth.unicef.utilities.UuidGenerator;
import org.intelehealth.unicef.utilities.exception.DAOException;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class IdentificationActivity extends AppCompatActivity {
    private static final String TAG = IdentificationActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> casteAdapter;
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
    private int SelectedMonth;
    private int mDOBDay;
    private DatePickerDialog mDOBPicker;
    private int mAgeYears = 0;
    private int mAgeMonths = 0;
    private int mAgeDays = 0;
    private String country1 = "", state = "";
    PatientsDAO patientsDAO = new PatientsDAO();
    EditText mCitizenIdEditText;
    EditText mFirstNameEditText;
    EditText mMiddleNameEditText;
    EditText mLastNameEditText;
    EditText mDOBEditText;
    EditText mPhoneNumEditText;
    EditText mAgeEditText;
    MaterialAlertDialogBuilder mAgePicker;
    EditText mAddress1EditText;
    EditText mAddress2EditText;
    AutoCompleteTextView mCityAutoCompleteTextView;
    EditText mPostalEditText;
    RadioButton mGenderMRadioButton;
    RadioButton mGenderFRadioButton;
    EditText mRelationshipEditText;
    EditText mOccupationEditText;
    EditText mCountryEditText;
    EditText mStateEditText;
    EditText mCasteEditText;
    Spinner mCountrySpinner;
    Spinner mStateSpinner;
    EditText mEconomicEditText;
    EditText mEducationEditText;
    TextInputLayout casteLayout;
    TextInputLayout economicLayout;
    TextInputLayout educationLayout;
    LinearLayout countryStateLayout;
    Spinner mCasteSpinner;
    Spinner mEducationSpinner;
    Spinner mEconomicStatusSpinner;
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
    int dob_indexValue = 15;
    //random value assigned to check while editing. If user didnt updated the dob and just clicked on fab
    //in that case, the edit() will get the dob_indexValue as 15 and we  will check if the
    //dob_indexValue == 15 then just get the mDOB editText value and add in the db.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        Log.d("lang", "lang: " + language);
        //In case of crash still the unicef should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        //  sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

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

        mFirstNameEditText = findViewById(R.id.identification_first_name);
        mFirstNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        mMiddleNameEditText = findViewById(R.id.identification_middle_name);
        mMiddleNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        mLastNameEditText = findViewById(R.id.identification_last_name);
        mLastNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        mCitizenIdEditText = findViewById(R.id.identification_citizen_id);


        mDOBEditText = findViewById(R.id.identification_birth_date_text_view);
        mPhoneNumEditText = findViewById(R.id.identification_phone_number);

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

        mAgeEditText = findViewById(R.id.identification_age);
        mAddress1EditText = findViewById(R.id.identification_address1);
        mAddress1EditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50

        mAddress2EditText = findViewById(R.id.identification_address2);
        mAddress2EditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50), inputFilter_Name}); //maxlength 50

        mCityAutoCompleteTextView = findViewById(R.id.identification_city);
        mCityAutoCompleteTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        mStateEditText = findViewById(R.id.identification_state);
        mStateSpinner = findViewById(R.id.spinner_state);
        mPostalEditText = findViewById(R.id.identification_postal_code);
        mCountryEditText = findViewById(R.id.identification_country);
        mCountrySpinner = findViewById(R.id.spinner_country);
        mGenderMRadioButton = findViewById(R.id.identification_gender_male);
        mGenderFRadioButton = findViewById(R.id.identification_gender_female);
        mRelationshipEditText = findViewById(R.id.identification_relationship);
        mRelationshipEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        mOccupationEditText = findViewById(R.id.identification_occupation);
        mOccupationEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        mCasteSpinner = findViewById(R.id.spinner_caste);
        mEducationSpinner = findViewById(R.id.spinner_education);
        mEconomicStatusSpinner = findViewById(R.id.spinner_economic_status);
        mCasteEditText = findViewById(R.id.identification_caste);
        mEducationEditText = findViewById(R.id.identification_education);
        mEconomicEditText = findViewById(R.id.identification_econiomic_status);

        casteLayout = findViewById(R.id.identification_txtlcaste);
        economicLayout = findViewById(R.id.identification_txtleconomic);
        educationLayout = findViewById(R.id.identification_txtleducation);
        countryStateLayout = findViewById(R.id.identification_llcountry_state);
        mImageView = findViewById(R.id.imageview_id_picture);
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
                mFirstNameEditText.setVisibility(View.VISIBLE);
            } else {
                mFirstNameEditText.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mMiddleName")) {
                mMiddleNameEditText.setVisibility(View.VISIBLE);
            } else {
                mMiddleNameEditText.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mLastName")) {
                mLastNameEditText.setVisibility(View.VISIBLE);
            } else {
                mLastNameEditText.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mDOB")) {
                mDOBEditText.setVisibility(View.VISIBLE);
            } else {
                mDOBEditText.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPhoneNum")) {
                mPhoneNumEditText.setVisibility(View.VISIBLE);
            } else {
                mPhoneNumEditText.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAge")) {
                mAgeEditText.setVisibility(View.VISIBLE);
            } else {
                mAgeEditText.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress1")) {
                mAddress1EditText.setVisibility(View.VISIBLE);
            } else {
                mAddress1EditText.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAddress2")) {
                mAddress2EditText.setVisibility(View.VISIBLE);
            } else {
                mAddress2EditText.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mCity")) {
                mCityAutoCompleteTextView.setVisibility(View.VISIBLE);
            } else {
                mCityAutoCompleteTextView.setVisibility(View.GONE);
            }

            if (obj.getBoolean("countryStateLayout")) {
                countryStateLayout.setVisibility(View.VISIBLE);
            } else {
                countryStateLayout.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPostal")) {
                mPostalEditText.setVisibility(View.VISIBLE);
                findViewById(R.id.identification_postal_code_ti).setVisibility(View.VISIBLE);
            } else {
                mPostalEditText.setVisibility(View.GONE);
                findViewById(R.id.identification_postal_code_ti).setVisibility(View.GONE);
            }

            if (obj.getBoolean("mGenderM")) {
                mGenderMRadioButton.setVisibility(View.VISIBLE);
            } else {
                mGenderMRadioButton.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mGenderF")) {
                mGenderFRadioButton.setVisibility(View.VISIBLE);
            } else {
                mGenderFRadioButton.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mRelationship")) {
                mRelationshipEditText.setVisibility(View.VISIBLE);
            } else {
                mRelationshipEditText.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mOccupation")) {
                mOccupationEditText.setVisibility(View.VISIBLE);
            } else {
                mOccupationEditText.setVisibility(View.GONE);
            }
            /*if (obj.getBoolean("casteLayout")) {
                casteLayout.setVisibility(View.VISIBLE);
            } else {
                casteLayout.setVisibility(View.GONE);
            }*/
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
//            country1 = obj.getString("mCountry");
//            state = obj.getString("mState");
            country1 = patient1 != null ? patient1.getCountry() : "";
            country1 = country1 == null || country1.isEmpty() ? sessionManager.getAppLanguage().equals("ru") ? "Кыргызстан" : "Kyrgyzstan" : country1;
            if (country1.equalsIgnoreCase("India") || country1.equalsIgnoreCase("Индия")) {
                EditTextUtils.setEditTextMaxLength(10, mPhoneNumEditText); //+91 (XXXXX XXXXX)
                ((TextView) findViewById(R.id.country_code_tv)).setText("+91");
            } else if (country1.equalsIgnoreCase("Kyrgyzstan") || country1.equalsIgnoreCase("Кыргызстан")) {
                EditTextUtils.setEditTextMaxLength(9, mPhoneNumEditText); //+996 (XXX XXXXXX)
                ((TextView) findViewById(R.id.country_code_tv)).setText("+996");
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }

        //setting the fields when user clicks edit details
        mFirstNameEditText.setText(patient1.getFirst_name());
        mMiddleNameEditText.setText(patient1.getMiddle_name());
        mLastNameEditText.setText(patient1.getLast_name());
        mDOBEditText.setText(patient1.getDate_of_birth());
        mPhoneNumEditText.setText(patient1.getPhone_number());
        mAddress1EditText.setText(patient1.getAddress1());
        mAddress2EditText.setText(patient1.getAddress2());
        mCityAutoCompleteTextView.setText(patient1.getCity_village());
        mPostalEditText.setText(patient1.getPostal_code());
        mRelationshipEditText.setText(patient1.getSdw());
        mOccupationEditText.setText(patient1.getOccupation());
        mCitizenIdEditText.setText(patient1.getCitizenID());

        if (patient1.getPatient_photo() != null && !patient1.getPatient_photo().trim().isEmpty())
            mImageView.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatient_photo()));

        Resources res = getResources();
//        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
//                R.array.countries, R.layout.custom_spinner);
//        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mCountrySpinner.setAdapter(countryAdapter);
//
        ArrayAdapter<CharSequence> countryAdapter = null;
        try {

            String mCountriesLanguage = "countries_" + sessionManager.getAppLanguage();
            int country = res.getIdentifier(mCountriesLanguage, "array", getApplicationContext().getPackageName());
            if (country != 0) {
                countryAdapter = ArrayAdapter.createFromResource(this,
                        country, R.layout.custom_spinner);

            }
            mCountrySpinner.setAdapter(countryAdapter);
        } catch (Exception e) {
//            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }


//        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(this,
//                R.array.caste, R.layout.custom_spinner);
//        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mCaste.setAdapter(casteAdapter);
        try {
            String casteLanguage = "caste_" + sessionManager.getAppLanguage();
            int castes = res.getIdentifier(casteLanguage, "array", getApplicationContext().getPackageName());
            if (castes != 0) {
                casteAdapter = ArrayAdapter.createFromResource(this,
                        castes, R.layout.custom_spinner);

            }
            mCasteSpinner.setAdapter(casteAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try {
            String economicLanguage = "economic_" + sessionManager.getAppLanguage();
            int economics = res.getIdentifier(economicLanguage, "array", getApplicationContext().getPackageName());
            if (economics != 0) {
                economicStatusAdapter = ArrayAdapter.createFromResource(this,
                        economics, R.layout.custom_spinner);
            }
            // countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mEconomicStatusSpinner.setAdapter(economicStatusAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.economic_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try {
            String educationLanguage = "education_" + sessionManager.getAppLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(this,
                        educations, R.layout.custom_spinner);

            }
            // countryAdapter.setDropDownViewResource(R.layout.custom_spinner);
            mEducationSpinner.setAdapter(educationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }


        if (null == patientID_edit || patientID_edit.isEmpty()) {
            generateUuid();

        }

        // setting radio button automatically according to the databse when user clicks edit details
        if (patientID_edit != null) {

            if (patient1.getGender().equals("M")) {
                mGenderMRadioButton.setChecked(true);
                if (mGenderFRadioButton.isChecked())
                    mGenderFRadioButton.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                mGenderFRadioButton.setChecked(true);
                if (mGenderMRadioButton.isChecked())
                    mGenderMRadioButton.setChecked(false);
                Log.v(TAG, "yes");
            }

        }
        if (mGenderMRadioButton.isChecked()) {
            mGender = "M";
        } else {
            mGender = "F";
        }
        if (patientID_edit != null) {
            // setting country according database
//            mCountrySpinner.setSelection(countryAdapter.getPosition(String.valueOf(patient1.getCountry())));
            mCountrySpinner.setSelection(countryAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_Country_edit(patient1.getCountry(), sessionManager.getAppLanguage()))));

            if (patient1.getEducation_level() == null || patient1.getEducation_level().equals(getResources().getString(R.string.not_provided)))
                mEducationSpinner.setSelection(0);
//            else
//                mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);

            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String education = switch_hi_education_edit(patient1.getEducation_level());
                    mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String education = switch_or_education_edit(patient1.getEducation_level());
                    mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else {
                    mEducationSpinner.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
                }
            }

            if (educationAdapter == null) {
                Toast.makeText(context, "Education Level: " + patient1.getEducation_level(), Toast.LENGTH_LONG).show();
            }


            if (patient1.getEconomic_status() == null || patient1.getEconomic_status().equals(getResources().getString(R.string.not_provided)))
                mEconomicStatusSpinner.setSelection(0);
//            else
//                mEconomicStatus.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));

            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String economic = switch_hi_economic_edit(patient1.getEconomic_status());
                    mEconomicStatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String economic = switch_or_economic_edit(patient1.getEconomic_status());
                    mEconomicStatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String economic = switch_ru_economic_edit(patient1.getEconomic_status());
                    mEconomicStatusSpinner.setSelection(economicStatusAdapter.getPosition(economic));
                } else {
                    mEconomicStatusSpinner.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));
                }
            }

            if (patient1.getCaste() == null || patient1.getCaste().equals(getResources().getString(R.string.not_provided)))
                mCasteSpinner.setSelection(0);
//            else
//                mCaste.setSelection(casteAdapter.getPosition(patient1.getCaste()));

            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String caste = switch_hi_caste_edit(patient1.getCaste());
                    mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String caste = switch_or_caste_edit(patient1.getCaste());
                    mCasteSpinner.setSelection(casteAdapter.getPosition(caste));
                } else {
                    mCasteSpinner.setSelection(casteAdapter.getPosition(patient1.getCaste()));
                }

            }

        } else {

            mCountrySpinner.setSelection(countryAdapter.getPosition(country1));
           /* mCountrySpinner.setSelection(countryAdapter.getPosition(StringUtils.getValue(mSwitch_Country_edit(country1,
                    sessionManager.getAppLanguage()))));*/
        }

//        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.state_error, R.layout.custom_spinner);
//        //  stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mStateSpinner.setAdapter(stateAdapter);
        try {
            ArrayAdapter<CharSequence> stateAdapter = null;
            String mStateLanguage = "state_error_" + sessionManager.getAppLanguage();
            int state = res.getIdentifier(mStateLanguage, "array", getApplicationContext().getPackageName());

            if (state != 0) {
                stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                        state, R.layout.custom_spinner);
            }
            mStateSpinner.setAdapter(stateAdapter);
        } catch (Exception e) {

            Logger.logE("Exception", "#state ", e);
        }


        mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (index != 0) {
                    String country = StringUtils.getValue(mSwitch_Country(mCountrySpinner.getSelectedItem().toString(),
                            sessionManager.getAppLanguage()));
                    if (patient1 == null || patient1.getCountry() == null || !patient1.getCountry().equalsIgnoreCase(country))
                        mPhoneNumEditText.setText("");
                    if (country.equalsIgnoreCase("India") || country.equalsIgnoreCase("Индия")) {
                        EditTextUtils.setEditTextMaxLength(10, mPhoneNumEditText); //+91 (XXXXX XXXXX)
                        ((TextView) findViewById(R.id.country_code_tv)).setText("+91");
                    } else if (country.equalsIgnoreCase("Kyrgyzstan") || country.equalsIgnoreCase("Кыргызстан")) {
                        EditTextUtils.setEditTextMaxLength(9, mPhoneNumEditText); //+996 (XXX XXXXXX)
                        ((TextView) findViewById(R.id.country_code_tv)).setText("+996");
                    }
                    //String country = adapterView.getItemAtPosition(index).toString();
                    ArrayAdapter<CharSequence> stateAdapter = null;

                    if (index == 2) {

                        try {
                            String mStateLanguage = "states_india_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStateLanguage, "array", getApplicationContext().getPackageName());

                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                        state, R.layout.custom_spinner);
                            }
                            mStateSpinner.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }

                        if (patientID_edit != null)
//                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));

                            mStateSpinner.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_State_edit(patient1.getState_province(), sessionManager.getAppLanguage()))));

                        else
                            mStateSpinner.setSelection(0);
//                            mStateSpinner.setSelection(stateAdapter.getPosition(getResources().getString(R.string.str_check_Odisha)));

                    } else if (index == 1) {
                        try {
                            String mStatesLanguage = "states_kyrgyzstan_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStatesLanguage, "array", getApplicationContext().getPackageName());
                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                        state, R.layout.custom_spinner);
                            }
                            mStateSpinner.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }
                        if (patientID_edit != null) {
                            mStateSpinner.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_State_edit(patient1.getState_province(), sessionManager.getAppLanguage()))));

//                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {
                            mStateSpinner.setSelection(0);
                        }
                    } else {

                        String mStatesLanguage = "state_error_" + sessionManager.getAppLanguage();
                        int state = res.getIdentifier(mStatesLanguage, "array", getApplicationContext().getPackageName());
                        if (state != 0) {
                            stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                    state, R.layout.custom_spinner);
                        }
//                        stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                R.array.state_error, R.layout.custom_spinner);
//                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mStateSpinner.setAdapter(stateAdapter);

                    }
//                    if (index == 2) {
//                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                R.array.states_india, R.layout.custom_spinner);
//                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        mStateSpinner.setAdapter(stateAdapter);
//                        // setting state according database when user clicks edit details
//
//                        if (patientID_edit != null)
//                            mStateSpinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
//                        else
//                            mStateSpinner.setSelection(stateAdapter.getPosition(state));
//
//                    } else if (index == 1) {
//                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                                sessionManager.getAppLanguage().equals("ru") ? R.array.states_kyrgyzstan_ru : R.array.states_kyrgyzstan, R.layout.custom_spinner);
//                        mStateSpinner.setAdapter(stateAdapter);
//                        // setting state according database when user clicks edit details
//
//                        if (patientID_edit != null)
//                            mStateSpinner.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
//                        else
//                            mStateSpinner.setSelection(0);
//
//                    }
//                } else {
//                    ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                            R.array.state_error, R.layout.custom_spinner);
//                    // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    mStateSpinner.setAdapter(stateAdapter);
//                }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String state = parent.getItemAtPosition(position).toString();
                mCityAutoCompleteTextView.setAdapter(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mGenderFRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mGenderMRadioButton.setOnClickListener(new View.OnClickListener() {
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
        // Locale.setDefault(Locale.ENGLISH);

        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                mDOBEditText.setError(null);
                mAgeEditText.setError(null);
                //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                // Locale.setDefault(Locale.ENGLISH);
                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                dob.set(year, monthOfYear, dayOfMonth);
                String dobString = simpleDateFormat.format(dob.getTime());
                dob_indexValue = monthOfYear; //fetching the inex value of month selected...
                SelectedMonth = monthOfYear;
                /*if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                    mDOBEditText.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                    mDOBEditText.setText(dob_text);
                }else */
                if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String dob_text = ru__or_dob(dobString); //to show text of English into Odiya...
                    mDOBEditText.setText(dob_text);
                } else {
                    mDOBEditText.setText(dobString);
                }

                //  mDOB.setText(dobString);
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
                String ageS = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " +
                        mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " +
                        mAgeDays + getResources().getString(R.string.days);
                mAgeEditText.setText(ageS);

            }
        }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDOBEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });
        //if patient update then age will be set
        if (patientID_edit != null) {
            //dob to be displayed based on translation...
            String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth());
            /*if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String dob_text = en__hi_dob(dob); //to show text of English into Hindi...

                mDOBEditText.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                mDOBEditText.setText(dob_text);
            } else*/
            if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                String dob_text = ru__or_dob(dob); //to show text of English into Hindi...
                mDOBEditText.setText(dob_text);
            } else {

                mDOBEditText.setText(dob);
            }

            // mDOB.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
            //get year month days
            String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth(), context);

            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth()).split(" ");
            mAgeYears = Integer.valueOf(ymdData[0]);
            mAgeMonths = Integer.valueOf(ymdData[1]);
            mAgeDays = Integer.valueOf(ymdData[2]);
            String age = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " +
                    mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " +
                    mAgeDays + getResources().getString(R.string.days);
            mAgeEditText.setText(age);
        }

        mAgeEditText.setOnClickListener(new View.OnClickListener() {
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

                if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.JANUARY ||
                        Calendar.getInstance().get(Calendar.MONTH) == Calendar.MARCH || Calendar.getInstance().get(Calendar.MONTH) == Calendar.MAY ||
                        Calendar.getInstance().get(Calendar.MONTH) == Calendar.JULY || Calendar.getInstance().get(Calendar.MONTH) == Calendar.AUGUST ||
                        Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER || Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER) {
                    dayPicker.setMaxValue(31);

//             }
//                if (SelectedMonth == Calendar.JANUARY ||
//                        SelectedMonth == Calendar.MARCH || SelectedMonth == Calendar.MAY ||
//                        SelectedMonth == Calendar.JULY || SelectedMonth == Calendar.AUGUST ||
//                        SelectedMonth == Calendar.OCTOBER || SelectedMonth == Calendar.DECEMBER) {
//                    dayPicker.setMaxValue(31);

                } else if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.FEBRUARY) {
                    dayPicker.setMaxValue(28);
                } else {
                    dayPicker.setMaxValue(30);
                }


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
                    mAgeEditText.setText(ageString);

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -mAgeDays);
                    calendar.add(Calendar.MONTH, -mAgeMonths);
                    calendar.add(Calendar.YEAR, -mAgeYears);

                    mDOBYear = calendar.get(Calendar.YEAR);
                    mDOBMonth = calendar.get(Calendar.MONTH);
                    mDOBDay = calendar.get(Calendar.DAY_OF_MONTH);
//                    cal.add(Calendar.YEAR, -mAgeYears);
//                    cal.add(Calendar.MONTH, -mAgeMonths);
////                    cal.add(Calendar.DAY_OF_YEAR, -mAgeDays);
//                    cal.add(Calendar.DAY_OF_YEAR, -mAgeDays+1);
//
//                    // have added one day because this method does not include today's day to calculate difference: By Nishita
//                    Calendar calendar = Calendar.getInstance();
//                    int curYear = calendar.get(Calendar.YEAR);
//                    //int birthYear = curYear - yearPicker.getValue();
//                    int birthYear = curYear - mAgeYears;
//                    int curMonth = calendar.get(Calendar.MONTH);
//                    //int birthMonth = curMonth - monthPicker.getValue();
//                    int birthMonth = curMonth - mAgeMonths;
//                    //int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - dayPicker.getValue();
//                    int leafyearDiffrence = birthYear - curYear;
//                    Log.d("Month", "982>>>>" + birthMonth);
//                    Log.d("mAgeMonths", "982>>>>" + mAgeMonths);
//                    Log.d("leafyearDiffrence", "983>>>>" + leafyearDiffrence);
//
////                    int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - mAgeDays-(leafyearDiffrence/4);
////                    int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - mAgeDays-(leafyearDiffrence/4);
//                    int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - mAgeDays;
//                    mDOBYear = birthYear;
//                    mDOBMonth = birthMonth;
//
//                    if (birthDay < 0) {
//                        mDOBDay = birthDay + totalDays - 1;
////                        mDOBDay = birthDay + totalDays - 1;
//                        mDOBMonth--;
//
//                    } else {
//                        mDOBDay = birthDay;
//                    }
//                    //   Locale.setDefault(Locale.ENGLISH);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy",
                            Locale.ENGLISH);
                    dob.set(mDOBYear, mDOBMonth, mDOBDay);
//                    dob_indexValue = mDOBMonth;

                    String dobString = simpleDateFormat.format(calendar.getTime());

                    if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String dob_text = ru__or_dob(dobString); //to show text of English into Odiya...
                        mDOBEditText.setText(dob_text);
                    } else {
                        mDOBEditText.setText(dobString);

                    }

//                    mDOB.setText(dobString);
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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (patientID_edit != null) {
                onPatientUpdateClicked(patient1);
            } else {
                onPatientCreateClicked();
            }
        });
    }

    //    @RequiresApi(api = Build.VERSION_CODES.O)
    private int getMonthNumber(String monthName) {
        int monthNumber = 0;
        try {
            Calendar cal = Calendar.getInstance();
            Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(monthName);//put your month name in english here
            cal.setTime(date);
            monthNumber = cal.get(Calendar.MONTH);
            System.out.println(monthNumber);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return monthNumber;
    }

    public String getYear(int syear, int smonth, int sday, int eyear, int emonth, int eday) {
        //three ten implementation
//        org.threeten.bp.LocalDate localDateTime1 = org.threeten.bp.LocalDate.now();
//        org.threeten.bp.LocalDate localDateTime2 = org.threeten.bp.LocalDate.now();
//        localDateTime2 = localDateTime2.withYear(syear).withMonth(smonth + 1).withDayOfMonth(sday);
//        org.threeten.bp.Period p = org.threeten.bp.Period.between(localDateTime2, localDateTime1);
//return p.getYears() + " years  - " + p.getMonths() + " month - " + p.getDays() + " days";

        org.joda.time.LocalDate birthdate = new org.joda.time.LocalDate(syear, smonth + 1, sday);
        org.joda.time.LocalDate now = new org.joda.time.LocalDate();
        org.joda.time.Period p = new org.joda.time.Period(birthdate, now, PeriodType.yearMonthDay());
        return p.getYears() + " years  - " + p.getMonths() + " month - " + p.getDays() + " days";

//        String calculatedAge = null;
//        int resmonth;
//        int resyear;
//        int resday;
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//
//            LocalDate today = LocalDate.now();
//            LocalDate birthday = LocalDate.of(syear, smonth + 1, sday);
//
//            Period p = Period.between(birthday, today);
//            System.out.println(p.getDays());
//            System.out.println(p.getMonths());
//            System.out.println(p.getYears());
//            calculatedAge = p.getYears() + " years  - " + p.getMonths() + " month - " + p.getDays() + " days";
//
//
//        } else {
//
//            //calculating year
//            resyear = eyear - syear;
//
//            //calculating month
//            if (emonth >= smonth) {
//                resmonth = emonth - smonth;
//            } else {
//                resmonth = emonth - smonth;
//                resmonth = 12 + resmonth;
//                resyear--;
//            }
//
//            //calculating date
//            if (eday >= sday) {
//                resday = eday - sday;
//            } else {
//                resday = eday - sday;
//                resday = 30 + resday;
//                if (resmonth == 0) {
//                    resmonth = 11;
//                    resyear--;
//                } else {
//                    resmonth--;
//                }
//            }
//
//            //displaying error if calculated age is negative
//            if (resday < 0 || resmonth < 0 || resyear < 0) {
//                //Toast.makeText(this, "Current Date must be greater than Date of Birth", Toast.LENGTH_LONG).show();
//                mDOBEditText.setError(getString(R.string.identification_screen_error_dob));
//                mAgeEditText.setError(getString(R.string.identification_screen_error_age));
//            } else {
//                // t1.setText("Age: " + resyear + " years /" + resmonth + " months/" + resday + " days");
//
//                calculatedAge = resyear + " years - " + resmonth + " months - " + resday + " days";
//            }
//        }
//
//        return calculatedAge != null ? calculatedAge : " ";
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
                "economic_status", "education_status", "caste"};
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

                if (name.equalsIgnoreCase("Citizen Id")) {
                    patient1.setCitizenID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
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
        alertDialogBuilder.setTitle(R.string.config_error);
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

        boolean cancel = false;
        View focusView = null;


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

        /*if (mPhoneNumEditText.getText().toString().trim().length() > 0) {
            if (mPhoneNumEditText.getText().toString().trim().length() < 12) {
                mPhoneNumEditText.requestFocus();
                mPhoneNumEditText.setError(getString(R.string.enter_12_digits));
                return;
            }
        }*/

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


        if (!mFirstNameEditText.getText().toString().equals("") && !mLastNameEditText.getText().toString().equals("")
                && !mCityAutoCompleteTextView.getText().toString().equals("") && !mCountryEditText.getText().toString().equals("") &&
                !mStateEditText.getText().toString().equals("") && !mDOBEditText.getText().toString().equals("") && !mAgeEditText.getText().toString().equals("") && (mGenderFRadioButton.isChecked() || mGenderMRadioButton.isChecked())) {

            Log.v(TAG, "Result");

        } else {
            if (mFirstNameEditText.getText().toString().equals("")) {
                mFirstNameEditText.setError(getString(R.string.error_field_required));
            }

            if (mLastNameEditText.getText().toString().equals("")) {
                mLastNameEditText.setError(getString(R.string.error_field_required));
            }

            if (mDOBEditText.getText().toString().equals("")) {
                mDOBEditText.setError(getString(R.string.error_field_required));
            }

            if (mAgeEditText.getText().toString().equals("")) {
                mAgeEditText.setError(getString(R.string.error_field_required));
            }

            if (mCityAutoCompleteTextView.getText().toString().equals("")) {
                mCityAutoCompleteTextView.setError(getString(R.string.error_field_required));
            }

            if (!mGenderFRadioButton.isChecked() && !mGenderMRadioButton.isChecked()) {
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

            }
            Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
            return;
        }

        if (!mCitizenIdEditText.getText().toString().trim().isEmpty() && mCitizenIdEditText.getText().toString().length() != 14) {
            mCitizenIdEditText.setError(getString(R.string.citizen_id_validation_message));
            return;
        } else {
            mCitizenIdEditText.setError(null);
        }

        if (mCountrySpinner.getSelectedItemPosition() == 0) {
            mCountryEditText.setError(getString(R.string.error_field_required));
            focusView = mCountryEditText;
            cancel = true;
            return;
        } else {
            mCountryEditText.setError(null);
        }

        if (mStateSpinner.getSelectedItemPosition() == 0) {
            mStateEditText.setError(getString(R.string.error_field_required));
            focusView = mStateEditText;
            cancel = true;
            return;
        } else {
            mStateEditText.setError(null);
        }

        String country = StringUtils.getValue(mSwitch_Country(mCountrySpinner.getSelectedItem().toString(),
                sessionManager.getAppLanguage()));

        if (country.equalsIgnoreCase("India") || country.equalsIgnoreCase("Индия")) {
            if (mPhoneNumEditText.getText().toString().trim().length() > 0) {
                if (mPhoneNumEditText.getText().toString().trim().length() < 10) {
                    mPhoneNumEditText.requestFocus();
                    mPhoneNumEditText.setError(getString(R.string.enter_10_digits));
                    return;
                }
            }
        } else if (country.equalsIgnoreCase("Kyrgyzstan") || country.equalsIgnoreCase("Кыргызстан")) {
            if (mPhoneNumEditText.getText().toString().trim().length() > 0) {
                if (mPhoneNumEditText.getText().toString().trim().length() < 9) {
                    mPhoneNumEditText.requestFocus();
                    mPhoneNumEditText.setError(getString(R.string.enter_9_digits));
                    return;
                }
            }
        }


       /* if (mState.getSelectedItemPosition() == 0) {
            stateText.setError(getString(R.string.error_field_required));
            focusView = stateText;
            cancel = true;
            return;
        } else {
            stateText.setError(null);
        }*/
        if (cancel) {
            focusView.requestFocus();
        } else {

            patientdto.setFirstname(StringUtils.getValue(mFirstNameEditText.getText().toString()));
            patientdto.setMiddlename(StringUtils.getValue(mMiddleNameEditText.getText().toString()));
            patientdto.setLastname(StringUtils.getValue(mLastNameEditText.getText().toString()));
            patientdto.setPhonenumber(StringUtils.getValue(mPhoneNumEditText.getText().toString()));
            patientdto.setGender(StringUtils.getValue(mGender));


            String[] dob_array = mDOBEditText.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or__en_noEdit(mDOBEditText.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob)));
            } else {
                dob_indexValue = getMonthNumber(StringUtils.hi_or__en_noEdit(dob_array[1], sessionManager.getAppLanguage()));
                String dob = StringUtils.hi_or__en_month(dob_indexValue);
                dob_array[1] = dob_array[1].replace(dob_array[1], dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];
                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob_value)));
            }

            patientdto.setAddress1(StringUtils.getValue(mAddress1EditText.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2EditText.getText().toString()));
            patientdto.setCityvillage(StringUtils.getValue(mCityAutoCompleteTextView.getText().toString()));
            if (!mPostalEditText.getText().toString().isEmpty() && StringUtils.getValue(mPostalEditText.getText().toString().trim()).length() != 6) {
                mPostalEditText.setError(getString(R.string.postal_code_invalid_txt));
                mPostalEditText.requestFocus();
                return;
            } else {
                mPostalEditText.setError(null);
            }
            patientdto.setPostalcode(StringUtils.getValue(mPostalEditText.getText().toString()));
//            patientdto.setCountry(StringUtils.getValue(mCountrySpinner.getSelectedItem().toString()));
            patientdto.setCountry(StringUtils.getValue(mSwitch_Country(mCountrySpinner.getSelectedItem().toString(),
                    sessionManager.getAppLanguage())));
//
            patientdto.setPatientPhoto(mCurrentPhotoPath);
//          patientdto.setEconomic(StringUtils.getValue(m));
//            patientdto.setStateprovince(StringUtils.getValue(mStateSpinner.getSelectedItem().toString()));
            patientdto.setStateprovince(StringUtils.getValue(mSwitch_State(mStateSpinner.getSelectedItem().toString(), sessionManager.getAppLanguage())));


            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Citizen Id"));
            patientAttributesDTO.setValue(StringUtils.getValue(mCitizenIdEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            //patientAttributesDTO.setValue(StringUtils.getProvided(mCasteSpinner));
            patientAttributesDTO.setValue(mCasteSpinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNumEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRelationshipEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
            patientAttributesDTO.setValue(StringUtils.getValue(mOccupationEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
            //patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicStatusSpinner));
            patientAttributesDTO.setValue(switch_ru_economic(mEconomicStatusSpinner.getSelectedItem().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            //patientAttributesDTO.setValue(StringUtils.getProvided(mEducationSpinner));
            patientAttributesDTO.setValue(mEducationSpinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ProfileImageTimestamp"));
            patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());

            //House Hold Registration
//            if (sessionManager.getHouseholdUuid().equals("")){
//
//                String HouseHold_UUID = UUID.randomUUID().toString();
//                sessionManager.setHouseholdUuid(HouseHold_UUID);
//
//                patientAttributesDTO = new PatientAttributesDTO();
//                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//                patientAttributesDTO.setPatientuuid(uuid);
//                patientAttributesDTO.setPersonAttributeTypeUuid
//                        (patientsDAO.getUuidForAttribute("householdID"));
//                patientAttributesDTO.setValue(HouseHold_UUID);
//
//            } else {
//
//                String HouseHold_UUID = sessionManager.getHouseholdUuid();
//                patientAttributesDTO = new PatientAttributesDTO();
//                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//                patientAttributesDTO.setPatientuuid(uuid);
//                patientAttributesDTO.setPersonAttributeTypeUuid
//                        (patientsDAO.getUuidForAttribute("householdID"));
//                patientAttributesDTO.setValue(HouseHold_UUID);
//
//            }

            patientAttributesDTOList.add(patientAttributesDTO);
            Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
            patientdto.setPatientAttributesDTOList(patientAttributesDTOList);
            patientdto.setSyncd(false);
            Logger.logD("patient json : ", "Json : " + gson.toJson(patientdto, PatientDTO.class));

        }

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

        boolean cancel = false;
        View focusView = null;


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

        /*if (mPhoneNumEditText.getText().toString().trim().length() > 0) {
            if (mPhoneNumEditText.getText().toString().trim().length() < 12) {
                mPhoneNumEditText.requestFocus();
                mPhoneNumEditText.setError(getString(R.string.enter_12_digits));
                return;
            }
        }*/

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

        if (!mFirstNameEditText.getText().toString().equals("") && !mLastNameEditText.getText().toString().equals("")
                && !mCityAutoCompleteTextView.getText().toString().equals("") && !mCountryEditText.getText().toString().equals("") &&
                !mStateEditText.getText().toString().equals("") && !mDOBEditText.getText().toString().equals("") && !mAgeEditText.getText().toString().equals("") && (mGenderFRadioButton.isChecked() || mGenderMRadioButton.isChecked())) {

            Log.v(TAG, "Result");

        } else {
            if (mFirstNameEditText.getText().toString().equals("")) {
                mFirstNameEditText.setError(getString(R.string.error_field_required));
            }

            if (mLastNameEditText.getText().toString().equals("")) {
                mLastNameEditText.setError(getString(R.string.error_field_required));
            }

            if (mDOBEditText.getText().toString().equals("")) {
                mDOBEditText.setError(getString(R.string.error_field_required));
            }

            if (mAgeEditText.getText().toString().equals("")) {
                mAgeEditText.setError(getString(R.string.error_field_required));
            }

            if (mCityAutoCompleteTextView.getText().toString().equals("")) {
                mCityAutoCompleteTextView.setError(getString(R.string.error_field_required));
            }

            if (!mGenderFRadioButton.isChecked() && !mGenderMRadioButton.isChecked()) {
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

            }


            Toast.makeText(IdentificationActivity.this, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
            return;
        }
        if (!mCitizenIdEditText.getText().toString().trim().isEmpty() && mCitizenIdEditText.getText().toString().length() != 14) {
            mCitizenIdEditText.setError(getString(R.string.citizen_id_validation_message));
            return;
        } else {
            mCitizenIdEditText.setError(null);
        }
        if (mCountrySpinner.getSelectedItemPosition() == 0) {
            mCountryEditText.setError(getString(R.string.error_field_required));
            focusView = mCountryEditText;
            cancel = true;
            return;
        } else {
            mCountryEditText.setError(null);
        }


        if (mStateSpinner.getSelectedItemPosition() == 0) {
            mStateEditText.setError(getString(R.string.error_field_required));
            focusView = mStateEditText;
            cancel = true;
            return;
        } else {
            mStateEditText.setError(null);
        }

        String country = StringUtils.getValue(mSwitch_Country(mCountrySpinner.getSelectedItem().toString(),
                sessionManager.getAppLanguage()));

        if (country.equalsIgnoreCase("India") || country.equalsIgnoreCase("Индия")) {
            if (mPhoneNumEditText.getText().toString().trim().length() > 0) {
                if (mPhoneNumEditText.getText().toString().trim().length() < 10) {
                    mPhoneNumEditText.requestFocus();
                    mPhoneNumEditText.setError(getString(R.string.enter_10_digits));
                    return;
                }
            }
        } else if (country.equalsIgnoreCase("Kyrgyzstan") || country.equalsIgnoreCase("Кыргызстан")) {
            if (mPhoneNumEditText.getText().toString().trim().length() > 0) {
                if (mPhoneNumEditText.getText().toString().trim().length() < 9) {
                    mPhoneNumEditText.requestFocus();
                    mPhoneNumEditText.setError(getString(R.string.enter_9_digits));
                    return;
                }
            }
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            if (mCurrentPhotoPath == null)
                mCurrentPhotoPath = patientdto.getPatient_photo();

            patientdto.setFirst_name(StringUtils.getValue(mFirstNameEditText.getText().toString()));
            patientdto.setMiddle_name(StringUtils.getValue(mMiddleNameEditText.getText().toString()));
            patientdto.setLast_name(StringUtils.getValue(mLastNameEditText.getText().toString()));
            patientdto.setPhone_number(StringUtils.getValue(mPhoneNumEditText.getText().toString()));
            patientdto.setGender(StringUtils.getValue(mGender));

            String[] dob_array = mDOBEditText.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or__en_noEdit
                        (mDOBEditText.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or__en_month(dob_indexValue);
                String dob_month_split = dob_array[1];
                dob_array[1] = dob_month_split.replace(dob_month_split, dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];

                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob_value)));
            }

            if (!mPostalEditText.getText().toString().isEmpty() && StringUtils.getValue(mPostalEditText.getText().toString().trim()).length() != 6) {
                mPostalEditText.setError(getString(R.string.postal_code_invalid_txt));
                mPostalEditText.requestFocus();
                return;
            } else {
                mPostalEditText.setError(null);
            }

            // patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(mDOB.getText().toString())));
            patientdto.setAddress1(StringUtils.getValue(mAddress1EditText.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2EditText.getText().toString()));
            patientdto.setCity_village(StringUtils.getValue(mCityAutoCompleteTextView.getText().toString()));
            patientdto.setPostal_code(StringUtils.getValue(mPostalEditText.getText().toString()));
//            patientdto.setCountry(StringUtils.getValue(mCountrySpinner.getSelectedItem().toString()));
            patientdto.setCountry(StringUtils.getValue(mSwitch_Country(mCountrySpinner.getSelectedItem().toString(),
                    sessionManager.getAppLanguage())));
//
            patientdto.setPatient_photo(mCurrentPhotoPath);
//                patientdto.setEconomic(StringUtils.getValue(m));
//            patientdto.setState_province(StringUtils.getValue(patientdto.getState_province()));

            patientdto.setState_province(StringUtils.getValue(mSwitch_State(mStateSpinner.getSelectedItem().toString(), sessionManager.getAppLanguage())));

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Citizen Id"));
            patientAttributesDTO.setValue(StringUtils.getValue(mCitizenIdEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            //patientAttributesDTO.setValue(StringUtils.getProvided(mCasteSpinner));
            patientAttributesDTO.setValue(mCasteSpinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNumEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Son/wife/daughter"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRelationshipEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
            patientAttributesDTO.setValue(StringUtils.getValue(mOccupationEditText.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
            //patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicStatusSpinner));
            patientAttributesDTO.setValue(switch_ru_economic(mEconomicStatusSpinner.getSelectedItem().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            //patientAttributesDTO.setValue(StringUtils.getProvided(mEducationSpinner));
            patientAttributesDTO.setValue(mEducationSpinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ProfileImageTimestamp"));
            patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());


            //House Hold Registration
            if (sessionManager.getHouseholdUuid().equals("")) {

                String HouseHold_UUID = UUID.randomUUID().toString();
                sessionManager.setHouseholdUuid(HouseHold_UUID);

                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid
                        (patientsDAO.getUuidForAttribute("householdID"));
                patientAttributesDTO.setValue(HouseHold_UUID);

            } else {

                String HouseHold_UUID = sessionManager.getHouseholdUuid();
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid
                        (patientsDAO.getUuidForAttribute("householdID"));
                patientAttributesDTO.setValue(HouseHold_UUID);

            }
//          patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTOList.add(patientAttributesDTO);
            Logger.logD(TAG, "PatientAttribute list size" + patientAttributesDTOList.size());
            //patientdto.setPatientAttributesDTOList(patientAttributesDTOList);

            Logger.logD("patient json onPatientUpdateClicked : ", "Json : " + gson.toJson(patientdto, Patient.class));

        }
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
//        Locale locale = new Locale(appLanguage);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
            context.createConfigurationContext(conf);
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

}

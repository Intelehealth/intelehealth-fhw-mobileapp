package org.intelehealth.ekalarogya.activities.identificationActivity;


import static org.intelehealth.ekalarogya.utilities.StringUtils.*;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.cameraActivity.CameraActivity;
import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.activities.identificationActivity.adapters.AlcoholConsumptionHistoryAdapter;
import org.intelehealth.ekalarogya.activities.identificationActivity.adapters.MedicalHistoryAdapter;
import org.intelehealth.ekalarogya.activities.identificationActivity.adapters.SmokingHistoryAdapter;
import org.intelehealth.ekalarogya.activities.identificationActivity.adapters.TobaccoHistoryAdapter;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.AlcoholConsumptionCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.MedicalHistoryCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.SmokingHistoryCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.TobaccoHistoryCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.ViewPagerCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.MedicalHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.SmokingHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.TobaccoHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.dialogs.AlcoholConsumptionHistoryDialog;
import org.intelehealth.ekalarogya.activities.identificationActivity.dialogs.MedicalHistoryDialog;
import org.intelehealth.ekalarogya.activities.identificationActivity.dialogs.SmokingHistoryDialog;
import org.intelehealth.ekalarogya.activities.identificationActivity.dialogs.TobaccoHistoryDialog;
import org.intelehealth.ekalarogya.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ekalarogya.activities.setupActivity.LocationArrayAdapter;
import org.intelehealth.ekalarogya.activities.setupActivity.SetupActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.database.dao.ImagesDAO;
import org.intelehealth.ekalarogya.database.dao.ImagesPushDAO;
import org.intelehealth.ekalarogya.database.dao.NewLocationDao;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.database.dao.SyncDAO;
import org.intelehealth.ekalarogya.databinding.ActivityIdentificationBinding;
import org.intelehealth.ekalarogya.models.Patient;
import org.intelehealth.ekalarogya.models.dto.PatientAttributesDTO;
import org.intelehealth.ekalarogya.models.dto.PatientDTO;
import org.intelehealth.ekalarogya.utilities.DateAndTimeUtils;
import org.intelehealth.ekalarogya.utilities.EditTextUtils;
import org.intelehealth.ekalarogya.utilities.FileUtils;
import org.intelehealth.ekalarogya.utilities.IReturnValues;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.NetworkConnection;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.StringUtils;
import org.intelehealth.ekalarogya.utilities.UuidGenerator;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;
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

public class IdentificationActivity extends AppCompatActivity implements
        AlcoholConsumptionCallback, MedicalHistoryCallback, SmokingHistoryCallback, ViewPagerCallback,
        TobaccoHistoryCallback {
    private static final String TAG = IdentificationActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    private ArrayAdapter<CharSequence> educationAdapter;
    private ArrayAdapter<CharSequence> casteAdapter;
    private ArrayAdapter<CharSequence> economicStatusAdapter;
    private ArrayAdapter<CharSequence> hohRelationshipAdapter, maritalAdapter;
    UuidGenerator uuidGenerator = new UuidGenerator();
    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();
    Patient patient1 = new Patient();
    private String patientUuid = "";
    private String mGender, mVaccination;
    String patientID_edit;
    private int mDOBYear;
    private int mDOBMonth;
    private int mDOBDay;
    private DatePickerDialog mDOBPicker;
    private int mAgeYears = 0;
    private int mAgeMonths = 0;
    private int mTimeHours = 0;
    private int mTimeMins = 0;
    private int mAgeDays = 0;
    private String country1, state;
    PatientsDAO patientsDAO = new PatientsDAO();
    EditText mFirstName;
    EditText mMiddleName;
    EditText mLastName;
    EditText mDOB;
    EditText mPhoneNum;
    EditText mAge;
    MaterialAlertDialogBuilder mAgePicker;
    MaterialAlertDialogBuilder mTimePicker;
    EditText mAddress1;
    EditText mAddress2;
    //AutoCompleteTextView mCity;
    EditText mPostal;
    RadioButton mGenderM;
    RadioButton mGenderF;
    RadioButton mGenderO;
    RadioButton radioYes;
    RadioButton radioNo;
    EditText mRelationship;
    //  EditText mOccupation;
    EditText countryText;
    EditText stateText, villageText;
    EditText casteText;
    Spinner mCountry;
    Spinner mState, mVillage;
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
    FrameLayout framelayout_vaccination, framelayout_vaccine_question;
    Spinner spinner_vaccination;
    private LinearLayoutCompat ll18;
    private AppCompatImageButton addMedicalHistoryButton, addSmokingStatusButton,
            addTobaccoStatusButton, addAlcoholConsumptionButton;

    private Context updatedContext;
    private ActivityIdentificationBinding binding;

    // History Lists
    private List<AlcoholConsumptionHistory> alcoholConsumptionHistoryList = new ArrayList<>();
    private List<MedicalHistory> medicalHistoryList = new ArrayList<>();
    private List<SmokingHistory> smokingHistoryList = new ArrayList<>();
    private List<TobaccoHistory> tobaccoHistoryList = new ArrayList<>();

    // Adapters
    private AlcoholConsumptionHistoryAdapter alcoholConsumptionHistoryAdapter;
    private MedicalHistoryAdapter medicalHistoryAdapter;
    private SmokingHistoryAdapter smokingHistoryAdapter;
    private TobaccoHistoryAdapter tobaccoHistoryAdapter;

    // ViewPager2
    private ViewPager2 alcoholViewPager;
    private ViewPager2 medicalHistoryViewPager;
    private ViewPager2 smokingHistoryViewPager;
    private ViewPager2 tobaccoHistoryViewPager;

    // RadioButtons
    private MaterialRadioButton hohYes, hohNo;

    // RadioGroup
    private RadioGroup hohRadioGroup, ekalProcessRadioGroup, waterSourceWithin30minutesRadioGroup;

    Intent i_privacy;
    String privacy_value;
    private int retainPickerYear;
    private int retainPickerMonth;
    private int retainPickerDate;
    Spinner occupation_spinner, bankaccount_spinner, mobilephone_spinner, whatsapp_spinner, water_availability_spinner,
            toilet_facility_spinner, structure_of_house_spinner, hohRelationshipSpinner, maritalStatusSpinner,
            bpSpinner, sugarLevelSpinner, hbLevelSpinner, bmiLevelSpinner, unitsSpinner;
  //  MaterialCheckBox time_water_checkbox;
    EditText time_water_editText, no_of_member_edittext, no_of_staying_members_edittext, landOwnedEditText, occupation_edittext,
            toiletfacility_edittext, otherHohRelationshipEditText;
    CardView cardview_household, hohRelationshipCardView;
    ArrayAdapter<CharSequence> occupation_adapt, bankaccount_adapt, mobile_adapt, whatsapp_adapt, vaccination_adapt,
            sourcewater_adapt, watersafe_adapt, availa_adapt, toiletfacility_adapt, structure_adapt,
            bp_adapt, sugar_adapt, hbLevel_adapt, bmi_adapt, unitsAdapter, religionAdapter;
    String occupation_edittext_value = "", watersafe_edittext_value = "", toilet_edittext_value = "";
    int dob_indexValue = 15;
    //random value assigned to check while editing. If user didnt updated the dob and just clicked on fab
    //in that case, the edit() will get the dob_indexValue as 15 and we  will check if the
    //dob_indexValue == 15 then just get the mDOB editText value and add in the db.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        setUpTranslationTools();

        super.onCreate(savedInstanceState);
        binding = ActivityIdentificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        // sessionManager = new SessionManager(this);
        mFirstName = findViewById(R.id.identification_first_name);
        mFirstName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        mMiddleName = findViewById(R.id.identification_middle_name);
        mMiddleName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

        mLastName = findViewById(R.id.identification_last_name);
        mLastName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25

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

        // mCity = findViewById(R.id.identification_city);
        //  mCity.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        stateText = findViewById(R.id.identification_state);
        mState = findViewById(R.id.spinner_state);

        villageText = findViewById(R.id.identification_village);
        mVillage = findViewById(R.id.spinner_village);

        mPostal = findViewById(R.id.identification_postal_code);
        countryText = findViewById(R.id.identification_country);
        mCountry = findViewById(R.id.spinner_country);
        mGenderM = findViewById(R.id.identification_gender_male);
        mGenderF = findViewById(R.id.identification_gender_female);
        mGenderO = findViewById(R.id.identification_gender_other);
        radioYes = findViewById(R.id.identification_yes);
        radioNo = findViewById(R.id.identification_no);
        framelayout_vaccination = findViewById(R.id.framelayout_vaccination);
        framelayout_vaccine_question = findViewById(R.id.framelayout_vaccine_question);
        spinner_vaccination = findViewById(R.id.spinner_vaccination);
        mRelationship = findViewById(R.id.identification_relationship);
        mRelationship.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        // mOccupation = findViewById(R.id.identification_occupation);
        // mOccupation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

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

        //Spinner
        occupation_spinner = findViewById(R.id.occupation_spinner);
        occupation_edittext = findViewById(R.id.occupation_edittext);
        bankaccount_spinner = findViewById(R.id.bankaccount_spinner);
        mobilephone_spinner = findViewById(R.id.mobilephone_spinner);
        whatsapp_spinner = findViewById(R.id.whatsapp_spinner);
      //  water_availability_spinner = findViewById(R.id.water_availability_spinner);
        toilet_facility_spinner = findViewById(R.id.toilet_facility_spinner);
        toiletfacility_edittext = findViewById(R.id.toiletfacility_edittext);
        structure_of_house_spinner = findViewById(R.id.structure_of_house_spinner);
        unitsSpinner = findViewById(R.id.units_spinner);
        hohRelationshipSpinner = findViewById(R.id.hoh_relationship_spinner);
        maritalStatusSpinner = findViewById(R.id.marital_status_spinner);
        bpSpinner = findViewById(R.id.bp_spinner);
        sugarLevelSpinner = findViewById(R.id.sugar_level_spinner);
        hbLevelSpinner = findViewById(R.id.hb_level_spinner);
    //    bmiLevelSpinner = findViewById(R.id.bmi_level_spinner);

        //HOH - Checkbox
    //    time_water_checkbox = findViewById(R.id.time_water_checkbox);

        // LinearLayout
        ll18 = findViewById(R.id.ll_18);

        //EditText
   //     time_water_editText = findViewById(R.id.time_water_editText);
        no_of_member_edittext = findViewById(R.id.no_of_member_edittext);
        no_of_staying_members_edittext = findViewById(R.id.no_of_staying_members_edittext);
        otherHohRelationshipEditText = findViewById(R.id.other_hoh_relationship_editText);
        landOwnedEditText = findViewById(R.id.land_owned_edit_text);

        //Cardview
        cardview_household = findViewById(R.id.cardview_household);
        hohRelationshipCardView = findViewById(R.id.cardview_hoh_relationship);

        // Button
        addMedicalHistoryButton = findViewById(R.id.add_medical_history_button);
        addSmokingStatusButton = findViewById(R.id.add_smoking_status_button);
        addTobaccoStatusButton = findViewById(R.id.add_tobacco_status_button);
        addAlcoholConsumptionButton = findViewById(R.id.add_alcohol_consumption_button);

        // ViewPager2
        alcoholViewPager = findViewById(R.id.alcohol_consumption_view_pager);
        medicalHistoryViewPager = findViewById(R.id.medical_history_view_pager);
        smokingHistoryViewPager = findViewById(R.id.smoking_history_view_pager);
        tobaccoHistoryViewPager = findViewById(R.id.tobacco_history_view_pager);

        // RadioButton
        hohYes = findViewById(R.id.hoh_yes);
        hohNo = findViewById(R.id.hoh_no);

        // RadioGroup
        hohRadioGroup = findViewById(R.id.hoh_radio_group);
        ekalProcessRadioGroup = findViewById(R.id.ekal_process_radio_group);
        waterSourceWithin30minutesRadioGroup = findViewById(R.id.water_source_30minutes_radio_group);

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
           /* if (obj.getBoolean("mCity")) {
                mCity.setVisibility(View.VISIBLE);
            } else {
                mCity.setVisibility(View.GONE);
            }*/

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
//            if (obj.getBoolean("mOccupation")) {
//                mOccupation.setVisibility(View.VISIBLE);
//            } else {
//                mOccupation.setVisibility(View.GONE);
//            }
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
            //state = obj.getString("mState");
            state = sessionManager.getStateName();

            if (country1.equalsIgnoreCase("India")) {
                EditTextUtils.setEditTextMaxLength(10, mPhoneNum);
            } else if (country1.equalsIgnoreCase("Philippines")) {
                EditTextUtils.setEditTextMaxLength(11, mPhoneNum);
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }

        // aeat 524: hoh already set radiobtn - start
        try {
            checkIfPatientIsHoHOrNot(sessionManager.getHouseholdUuid());
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
        // aeat 524: hoh already set radiobtn - end


        //setting the fields when user clicks edit details
        mFirstName.setText(patient1.getFirst_name());
        mMiddleName.setText(patient1.getMiddle_name());
        mLastName.setText(patient1.getLast_name());

  /*      if(patient1.getDate_of_birth() != null) {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                mDOB.setText(StringUtils.en__hi_dob(patient1.getDate_of_birth()));
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                mDOB.setText(StringUtils.en__or_dob(patient1.getDate_of_birth()));
            }else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                mDOB.setText(StringUtils.en__gu_dob(patient1.getDate_of_birth()));
            } else {
                mDOB.setText(patient1.getDate_of_birth());
            }
        }
        else {
            mDOB.setText(patient1.getDate_of_birth());
        }*/
        mDOB.setText(patient1.getDate_of_birth());
        Log.v("main", "dob: " + patient1.getDate_of_birth());


        mPhoneNum.setText(patient1.getPhone_number());
        mAddress1.setText(patient1.getAddress1());
        mAddress2.setText(patient1.getAddress2());
        //mCity.setText(patient1.getCity_village());

        mPostal.setText(patient1.getPostal_code());
        mRelationship.setText(patient1.getSdw());
        // mOccupation.setText(patient1.getOccupation());

        if (patient1.getPatient_photo() != null && !patient1.getPatient_photo().trim().isEmpty())
            mImageView.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatient_photo()));

        Resources res = getResources();
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, R.layout.custom_spinner);
        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountry.setAdapter(countryAdapter);
        mCountry.setEnabled(false);

//        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(this,
//                R.array.caste, R.layout.custom_spinner);
//        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mCaste.setAdapter(casteAdapter);

        try { //Caste adapter setting...
            String casteLanguage = "caste_" + sessionManager.getAppLanguage();
            int castes = res.getIdentifier(casteLanguage, "array", getApplicationContext().getPackageName());
            if (castes != 0) {
                casteAdapter = ArrayAdapter.createFromResource(this,
                        castes, R.layout.custom_spinner);

            }
            mCaste.setAdapter(casteAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try { //Economic adapter setting...
            String economicLanguage = "economic_" + sessionManager.getAppLanguage();
            int economics = res.getIdentifier(economicLanguage, "array", getApplicationContext().getPackageName());
            if (economics != 0) {
                economicStatusAdapter = ArrayAdapter.createFromResource(this,
                        economics, R.layout.custom_spinner);
            }
            // countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mEconomicStatus.setAdapter(economicStatusAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.economic_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
        try { //Education adapter setting....
            String educationLanguage = "education_" + sessionManager.getAppLanguage();
            int educations = res.getIdentifier(educationLanguage, "array", getApplicationContext().getPackageName());
            if (educations != 0) {
                educationAdapter = ArrayAdapter.createFromResource(this,
                        educations, R.layout.custom_spinner);

            }
            // countryAdapter.setDropDownViewResource(R.layout.custom_spinner);
            mEducation.setAdapter(educationAdapter);
        } catch (Exception e) {
            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // Hoh Relationship Adapter
        try {
            String hohRelationshipLanguage = "hoh_relationship_" + sessionManager.getAppLanguage();
            int hoh_relationship_id = res.getIdentifier(hohRelationshipLanguage, "array", getApplicationContext().getPackageName());
            if (hoh_relationship_id != 0) {
                hohRelationshipAdapter = ArrayAdapter.createFromResource(this, hoh_relationship_id, android.R.layout.simple_spinner_dropdown_item);
            }
            hohRelationshipSpinner.setAdapter(hohRelationshipAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Head of Household Values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // Marital Status Spinner
        try {
            String maritalStatusLanguage = "marital_status_" + sessionManager.getAppLanguage();
            int marital_id = res.getIdentifier(maritalStatusLanguage, "array", getApplicationContext().getPackageName());
            if (marital_id != 0) {
                maritalAdapter = ArrayAdapter.createFromResource(this, marital_id, android.R.layout.simple_spinner_dropdown_item);
            }
            maritalStatusSpinner.setAdapter(maritalAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Marital Status Values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        //  BP Spinner
        try {
            String bpLanguage = "test_status_" + sessionManager.getAppLanguage();
            int bp_id = res.getIdentifier(bpLanguage, "array", getApplicationContext().getPackageName());
            if (bp_id != 0) {
                bp_adapt = ArrayAdapter.createFromResource(this, bp_id, android.R.layout.simple_spinner_dropdown_item);
            }
            bpSpinner.setAdapter(bp_adapt);
        } catch (Exception e) {
            Toast.makeText(this, "Values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        //  Sugar Level Spinner
        try {
            String sugarLanguage = "test_status_" + sessionManager.getAppLanguage();
            int sugar_id = res.getIdentifier(sugarLanguage, "array", getApplicationContext().getPackageName());
            if (sugar_id != 0) {
                sugar_adapt = ArrayAdapter.createFromResource(this, sugar_id, android.R.layout.simple_spinner_dropdown_item);
            }
            sugarLevelSpinner.setAdapter(sugar_adapt);
        } catch (Exception e) {
            Toast.makeText(this, "Values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        //  Hb Level Spinner
        try {
            String hbLanguage = "test_status_" + sessionManager.getAppLanguage();
            int hb_id = res.getIdentifier(hbLanguage, "array", getApplicationContext().getPackageName());
            if (hb_id != 0) {
                hbLevel_adapt = ArrayAdapter.createFromResource(this, hb_id, android.R.layout.simple_spinner_dropdown_item);
            }
            hbLevelSpinner.setAdapter(hbLevel_adapt);
        } catch (Exception e) {
            Toast.makeText(this, "Values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

      /*  //  BMI Level Spinner
        try {
            String bmiLanguage = "test_status_" + sessionManager.getAppLanguage();
            int bmi_id = res.getIdentifier(bmiLanguage, "array", getApplicationContext().getPackageName());
            if (bmi_id != 0) {
                bmi_adapt = ArrayAdapter.createFromResource(this, bmi_id, android.R.layout.simple_spinner_dropdown_item);
            }
            bmiLevelSpinner.setAdapter(hbLevel_adapt);
        } catch (Exception e) {
            Toast.makeText(this, "Values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }
*/
        hohRelationshipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItem().toString().equals(getString(R.string.other_specify))) {
                    otherHohRelationshipEditText.setVisibility(View.VISIBLE);
                } else {
                    otherHohRelationshipEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Occupation Adapter ...
        try {
            String occupationLanguage = "occupation_spinner_" + sessionManager.getAppLanguage();
            int occupation_id = res.getIdentifier(occupationLanguage, "array", getApplicationContext().getPackageName());
            if (occupation_id != 0) {
                occupation_adapt = ArrayAdapter.createFromResource(this,
                        occupation_id, android.R.layout.simple_spinner_dropdown_item);

            }

            occupation_spinner.setAdapter(occupation_adapt);
        } catch (Exception e) {
            Toast.makeText(this, "Occupation values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        //Household Head
//        occupation_adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
//                getResources().getStringArray(R.array.occupation_spinner));

        //Vaccination - start
        //Vaccination - end

//        occupation_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 13) {
//                    occupation_edittext.setVisibility(View.VISIBLE);
//                    occupation_edittext.requestFocus();
//                    occupation_edittext.setFocusable(true);
//                    occupation_edittext.setFocusableInTouchMode(true);
//                } else {
//                    occupation_edittext.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        //Bank Account Adapter ...
        try {
            String bankaccountLanguage = "bank_account_spinner_" + sessionManager.getAppLanguage();
            int bankaccount_id = res.getIdentifier(bankaccountLanguage, "array", getApplicationContext().getPackageName());
            if (bankaccount_id != 0) {
                bankaccount_adapt = ArrayAdapter.createFromResource(this,
                        bankaccount_id, android.R.layout.simple_spinner_dropdown_item);
            }
            bankaccount_spinner.setAdapter(bankaccount_adapt);

        } catch (Exception e) {
            Toast.makeText(this, "BankAccount values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

//        bankaccount_adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
//                getResources().getStringArray(R.array.bank_account_spinner));

        //Mobile Type Adapter ...
        try {
            String mobileTypeLanguage = "mobilephone_spinner_" + sessionManager.getAppLanguage();
            int mobiletype_id = res.getIdentifier(mobileTypeLanguage, "array", getApplicationContext().getPackageName());
            if (mobiletype_id != 0) {
                mobile_adapt = ArrayAdapter.createFromResource(this, mobiletype_id, android.R.layout.simple_spinner_dropdown_item);
            }
            mobilephone_spinner.setAdapter(mobile_adapt);

        } catch (Exception e) {
            Toast.makeText(this, "Mobile Type values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

//        mobile_adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
//                getResources().getStringArray(R.array.mobilephone_spinner));

//        whatsapp_adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
//                getResources().getStringArray(R.array.familymember_whatsapp));

        //Whatsapp App Adapter ...
        try {
            String whatsappLanguage = "familymember_whatsapp_" + sessionManager.getAppLanguage();
            int whatsapp_id = res.getIdentifier(whatsappLanguage, "array", getApplicationContext().getPackageName());
            if (whatsapp_id != 0) {
                whatsapp_adapt = ArrayAdapter.createFromResource(this,
                        whatsapp_id, android.R.layout.simple_spinner_dropdown_item);
            }
            whatsapp_spinner.setAdapter(whatsapp_adapt);

        } catch (Exception e) {
            Toast.makeText(this, "Whatsapp values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        //Vaccination Spinner Adapter...
        //Whatsapp App Adapter ...
        try {
            String vaccinationLanguage = "vaccination_spinner_" + sessionManager.getAppLanguage();
            int vaccination = res.getIdentifier(vaccinationLanguage, "array", getApplicationContext().getPackageName());
            if (vaccination != 0) {
                vaccination_adapt = ArrayAdapter.createFromResource(this,
                        vaccination, android.R.layout.simple_spinner_dropdown_item);
            }
            spinner_vaccination.setAdapter(vaccination_adapt);

        } catch (Exception e) {
            Toast.makeText(this, "Vaccination values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

//        availa_adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
//                getResources().getStringArray(R.array.water_availability_spinner));

      /*  // Water Availability Adapter ...
        try {
            String wateravailLanguage = "water_availability_spinner_" + sessionManager.getAppLanguage();
            int wateravail_id = res.getIdentifier(wateravailLanguage, "array", getApplicationContext().getPackageName());
            if (wateravail_id != 0) {
                availa_adapt = ArrayAdapter.createFromResource(this,
                        wateravail_id, android.R.layout.simple_spinner_dropdown_item);
            }
            water_availability_spinner.setAdapter(availa_adapt);

        } catch (Exception e) {
            Toast.makeText(this, "Water Availability values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }*/

//        toiletfacility_adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
//                getResources().getStringArray(R.array.toiletFacility_spinner));

        // Toilet Facility Adapter ...
        try {
            String toiletFacilityLanguage = "toiletFacility_spinner_" + sessionManager.getAppLanguage();
            int toiletfacil_id = res.getIdentifier(toiletFacilityLanguage, "array", getApplicationContext().getPackageName());
            if (toiletfacil_id != 0) {
                toiletfacility_adapt = ArrayAdapter.createFromResource(this,
                        toiletfacil_id, android.R.layout.simple_spinner_dropdown_item);
            }
            toilet_facility_spinner.setAdapter(toiletfacility_adapt);

        } catch (Exception e) {
            Toast.makeText(this, "Toilet Facility values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // Religion ArrayAdapter
        try {
            String religionLanguage = "religion_" + sessionManager.getAppLanguage();
            int religionId = getResources().getIdentifier(religionLanguage, "array", getApplicationContext().getPackageName());
            if (religionId != 0) {
                religionAdapter = ArrayAdapter.createFromResource(this, religionId, android.R.layout.simple_spinner_dropdown_item);
            }
            binding.religionDropDown.setAdapter(religionAdapter);
        } catch (Exception e) {
            Logger.logE("FirstScreenFragment", "#648", e);
        }

        toilet_facility_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItem().toString().equalsIgnoreCase("Other [Enter]") ||
                        parent.getSelectedItem().toString().equalsIgnoreCase("अन्य [दर्ज करें]") ||
                        parent.getSelectedItem().toString().equalsIgnoreCase("ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]")) {
                    toiletfacility_edittext.setVisibility(View.VISIBLE);
                    toiletfacility_edittext.requestFocus();
                    toiletfacility_edittext.setFocusable(true);
                    toiletfacility_edittext.setFocusableInTouchMode(true);
                } else {
                    toiletfacility_edittext.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        structure_adapt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
//                getResources().getStringArray(R.array.structure_house));

        // House Structure Adapter ...
        try {
            String houseStructureLanguage = "structure_house_" + sessionManager.getAppLanguage();
            int houseStruct_id = res.getIdentifier(houseStructureLanguage, "array", getApplicationContext().getPackageName());
            if (houseStruct_id != 0) {
                structure_adapt = ArrayAdapter.createFromResource(this,
                        houseStruct_id, android.R.layout.simple_spinner_dropdown_item);
            }
            structure_of_house_spinner.setAdapter(structure_adapt);

        } catch (Exception e) {
            Toast.makeText(this, "House Structure values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        // Land Owned Units Adapter
        try {
            String unitsLanguage = "land_units_" + sessionManager.getAppLanguage();
            int unitsID = res.getIdentifier(unitsLanguage, "array", getApplicationContext().getPackageName());
            if (unitsID != 0) {
                unitsAdapter = ArrayAdapter.createFromResource(this, unitsID, android.R.layout.simple_spinner_dropdown_item);
            }
            unitsSpinner.setAdapter(unitsAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Unit values are missing", Toast.LENGTH_SHORT).show();
            Logger.logE("Identification", "#648", e);
        }

        unitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    landOwnedEditText.setVisibility(View.VISIBLE);
                }
                else {
                    landOwnedEditText.setVisibility(View.GONE);
                }

                if (position == 5) {
                    landOwnedEditText.setText("");    // so that when user selects this option that the amount becomes "" for Landless.
                    landOwnedEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

/*
        binding.openDefecationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.openDefecationYes.getId()) {
                binding.llReasonForOpenDefecation.setVisibility(View.VISIBLE);
            } else {
                binding.llReasonForOpenDefecation.setVisibility(View.GONE);
            }
        });
*/

        if (null == patientID_edit || patientID_edit.isEmpty()) {
            generateUuid();

        }

        addMedicalHistoryButton.setOnClickListener(v -> {
            MedicalHistoryDialog dialog = new MedicalHistoryDialog();
            dialog.show(getSupportFragmentManager(), MedicalHistoryDialog.TAG);
        });

        addSmokingStatusButton.setOnClickListener(v -> {
            SmokingHistoryDialog dialog = new SmokingHistoryDialog();
            dialog.show(getSupportFragmentManager(), SmokingHistoryDialog.TAG);
        });
        // Tobacco Card + button click
        addTobaccoStatusButton.setOnClickListener(v -> {
            TobaccoHistoryDialog dialog = new TobaccoHistoryDialog();
            dialog.show(getSupportFragmentManager(), TobaccoHistoryDialog.TAG);
        });

        addAlcoholConsumptionButton.setOnClickListener(v -> {
            AlcoholConsumptionHistoryDialog dialog = new AlcoholConsumptionHistoryDialog();
            dialog.show(getSupportFragmentManager(), AlcoholConsumptionHistoryDialog.TAG);
        });

        binding.runningWaterHoursEditText.setOnClickListener(v -> timePicker(getString(R.string.identification_screen_picker_hours), binding.runningWaterHoursEditText, 24));

        binding.runningWaterDaysEditText.setOnClickListener(v -> timePicker(getString(R.string.days), binding.runningWaterDaysEditText, 7));

        binding.loadSheddingHoursEditText.setOnClickListener(v -> timePicker(getString(R.string.identification_screen_picker_hours), binding.loadSheddingHoursEditText, 24));

        binding.loadSheddingDaysEditText.setOnClickListener(v -> timePicker(getString(R.string.days), binding.loadSheddingDaysEditText, 7));

/*
        time_water_editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTimePicker = new MaterialAlertDialogBuilder(IdentificationActivity.this, R.style.AlertDialogStyle);
                mTimePicker.setTitle(R.string.identification_screen_prompt_time);
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
                mTimePicker.setView(convertView);
                NumberPicker hourPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
                NumberPicker minsPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);


                final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
                final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_3);

                int totalDays = today.getActualMaximum(Calendar.DAY_OF_MONTH);

                middleText.setText(getString(R.string.identification_screen_picker_hours));
                endText.setText(getString(R.string.identification_screen_picker_minute));


                hourPicker.setMinValue(0);
                hourPicker.setMaxValue(24);
                minsPicker.setMinValue(0);
                minsPicker.setMaxValue(60);

                EditText hourText = hourPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
                EditText minsText = minsPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));

                hourPicker.setValue(mTimeHours);
                minsPicker.setValue(mTimeMins);

                //year
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mTimeHours = Integer.valueOf(value);
                    }
                }, hourText);

                //month
                EditTextUtils.returnEditextValues(new IReturnValues() {
                    @Override
                    public void onReturnValue(String value) {
                        mTimeMins = Integer.valueOf(value);
                    }
                }, minsText);


                mTimePicker.setPositiveButton(R.string.generic_ok, (dialog, which) -> {
                    String ageString = mTimeHours + " " + getString(R.string.identification_screen_picker_hours) + " - " +
                            mTimeMins + " " + getString(R.string.identification_screen_picker_minute);
                    time_water_editText.setText(ageString);

                    dialog.dismiss();
                });
                mTimePicker.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = mTimePicker.show();
                IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);

            }
        });
*/

/*
        time_water_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    time_water_checkbox.setError(null);
                    time_water_editText.setError(null);
                    time_water_editText.setVisibility(View.GONE);
                } else {
                    time_water_editText.setVisibility(View.VISIBLE);
                }
            }
        });
*/

        binding.householdRunningWaterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.waterSupplyYes.getId())
                binding.runningWaterAvailabilityLinearLayout.setVisibility(View.VISIBLE);
            else {
                binding.runningWaterAvailabilityLinearLayout.setVisibility(View.GONE);
                binding.runningWaterHoursEditText.setText(null);
                binding.runningWaterDaysEditText.setText(null);
            }
        });

        binding.householdElectricityRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.householdElectricityYes.getId())
                binding.llLoadShedding.setVisibility(View.VISIBLE);
            else {
                binding.llLoadShedding.setVisibility(View.GONE);
                binding.loadSheddingHoursEditText.setText(null);
                binding.loadSheddingDaysEditText.setText(null);
            }
        });

        // setting radio button automatically according to the databse when user clicks edit details
        if (patientID_edit != null) {

            if (patient1.getGender().equals("M")) {
                mGenderM.setChecked(true);
                if (mGenderF.isChecked())
                    mGenderF.setChecked(false);
                if (mGenderO.isChecked())
                    mGenderO.setChecked(false);
                Log.v(TAG, "yes");
            } else if (patient1.getGender().equals("F")) {
                mGenderF.setChecked(true);
                if (mGenderM.isChecked())
                    mGenderM.setChecked(false);
                if (mGenderO.isChecked())
                    mGenderO.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                mGenderO.setChecked(true);
                if (mGenderM.isChecked())
                    mGenderM.setChecked(false);
                if (mGenderF.isChecked())
                    mGenderF.setChecked(false);
            }

            //vacciantion...
            if (patient1.getVaccination() != null) {
                if (!patient1.getVaccination().equalsIgnoreCase("No")) {
                    if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                        framelayout_vaccine_question.setVisibility(View.GONE);
                        framelayout_vaccination.setVisibility(View.GONE);
                        int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                        spinner_vaccination.setSelection(spinner_position);
                        radioYes.setChecked(false);
                        radioNo.setChecked(false);
                    } else {
                        framelayout_vaccination.setVisibility(View.VISIBLE);
                        int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                        spinner_vaccination.setSelection(spinner_position);
                        radioYes.setChecked(true);
                        if (radioNo.isChecked())
                            radioNo.setChecked(false);
                    }
                } else {
                    framelayout_vaccination.setVisibility(View.GONE);
                    spinner_vaccination.setSelection(0);
                    radioNo.setChecked(true);
                    if (radioYes.isChecked())
                        radioYes.setChecked(false);
                }
            } else {
                //on edit if no data was present that means that age was less than 18 when registring a patient. So, if null then hide then question and not show that question...
                framelayout_vaccine_question.setVisibility(View.GONE);
                framelayout_vaccination.setVisibility(View.GONE);
//                int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
//                spinner_vaccination.setSelection(spinner_position);
                radioYes.setChecked(false);
                radioNo.setChecked(false);
            }
            //vacciantion - end

        }
        if (mGenderM.isChecked()) {
            mGender = "M";
        } else if (mGenderF.isChecked()) {
            mGender = "F";
        } else {
            mGender = "O";
        }


        if (patientID_edit != null) {
            // setting country according database
            mCountry.setSelection(countryAdapter.getPosition(String.valueOf(patient1.getCountry())));

            if (patient1.getEducation_level().equals(getResources().getString(R.string.not_provided)))
                mEducation.setSelection(0);
            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String education = switch_hi_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String education = switch_or_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                }else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String education = switch_bn_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String education = switch_kn_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String education = switch_mr_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String education = switch_gu_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String education = switch_as_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else {
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
//                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                    String education = switch_hi_education_edit(patient1.getEducation_level());
//                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
//                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
//                    String education = switch_or_education_edit(patient1.getEducation_level());
//                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
//                } else {
//                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
//                }
                }
            }

            if (patient1.getEducation_level() != null && !patient1.getEducation_level().equalsIgnoreCase("")) {
                String education = getEducationStrings(patient1.getEducation_level(), updatedContext, context, sessionManager.getAppLanguage());
                int position = educationAdapter.getPosition(education);
                mEducation.setSelection(position);

            }

            //mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
            if (educationAdapter == null) {
                Toast.makeText(context, "Education Level: " + patient1.getEducation_level(), Toast.LENGTH_LONG).show();
            }


            if (patient1.getCaste() == null || patient1.getCaste().equals(getResources().getString(R.string.not_provided))) {
                mCaste.setSelection(0);
            } else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String caste = switch_hi_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String caste = switch_or_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String caste = switch_bn_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String caste = switch_kn_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String caste = switch_mr_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String caste = switch_gu_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String caste = switch_as_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else {
                    mCaste.setSelection(casteAdapter.getPosition(patient1.getCaste()));
                }
            }

            //Houselhold Head...
            if (patient1.getNo_of_family_members() != null && !patient1.getNo_of_family_members().equalsIgnoreCase("")
                    && !patient1.getNo_of_family_members().isEmpty()) {
                hohYes.setChecked(true);
                cardview_household.setVisibility(View.VISIBLE);
                //sessionManager.setHOH_checkbox(false);
            } else {
                hohNo.setChecked(true);
                cardview_household.setVisibility(View.GONE);
            }

            if (patient1.getOccupation() != null && !patient1.getOccupation().equalsIgnoreCase("")) {
                String occupation_Transl = "";
                /*if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    occupation_Transl = StringUtils.switch_hi_occupation_edit(patient1.getOccupation());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    occupation_Transl = StringUtils.switch_or_occupation_edit(patient1.getOccupation());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    occupation_Transl = StringUtils.switch_gu_occupation_edit(patient1.getOccupation());
                }else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    occupation_Transl = StringUtils.switch_as_occupation_edit(patient1.getOccupation());
                } else {
                    occupation_Transl = patient1.getOccupation();
                }*/

                occupation_Transl = getOccupationString(patient1.getOccupation(), updatedContext, context, sessionManager.getAppLanguage());
                int spinner_position = occupation_adapt.getPosition(occupation_Transl);
                if (spinner_position >= 0) {
                    occupation_spinner.setSelection(spinner_position); //user selected value items from spinner
                }
                //since here we will show the value of the dynamic occupation value...
                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        occupation_spinner.setSelection(occupation_adapt.getPosition("वर्णन करे"));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        occupation_spinner.setSelection(occupation_adapt.getPosition("[ବର୍ଣ୍ଣନା କର]"));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        occupation_spinner.setSelection(occupation_adapt.getPosition("[বর্ণনা]"));
                    }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        occupation_spinner.setSelection(occupation_adapt.getPosition("[ವಿವರಿಸಿ]"));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        occupation_spinner.setSelection(occupation_adapt.getPosition("[वर्णन करणे]"));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        //-------------change gujrati language---------------
                        occupation_spinner.setSelection(occupation_adapt.getPosition("[વર્ણન કરો]"));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        //-------------change assamese language---------------
                        occupation_spinner.setSelection(occupation_adapt.getPosition("[বৰ্ণনা কৰা]"));
                    } else {
                        occupation_spinner.setSelection(occupation_adapt.getPosition("[Describe]"));
                    }

//                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                        occupation_spinner.setSelection(occupation_adapt.getPosition("वर्णन करे"));
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
//                        occupation_spinner.setSelection(occupation_adapt.getPosition("[ବର୍ଣ୍ଣନା କର]"));
//                    } else {
//                        occupation_spinner.setSelection(occupation_adapt.getPosition("[Describe]"));
//                    }
                    occupation_spinner.setSelection(occupation_adapt.getPosition(getString(R.string.other_specify)));
                    occupation_edittext.setVisibility(View.VISIBLE);
                    occupation_edittext.setText(patient1.getOccupation());
                }
            }
            if (patient1.getBank_account() != null && !patient1.getBank_account().equalsIgnoreCase("")) {
                String bankacc_Transl = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    bankacc_Transl = StringUtils.switch_hi_bankaccount_edit(patient1.getBank_account());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    bankacc_Transl = StringUtils.switch_or_bankaccount_edit(patient1.getBank_account());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    bankacc_Transl = StringUtils.switch_bn_bankaccount_edit(patient1.getBank_account());
                }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    bankacc_Transl = StringUtils.switch_kn_bankaccount_edit(patient1.getBank_account());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    bankacc_Transl = StringUtils.switch_mr_bankaccount_edit(patient1.getBank_account());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    bankacc_Transl = StringUtils.switch_gu_bankaccount_edit(patient1.getBank_account());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    bankacc_Transl = StringUtils.switch_as_bankaccount_edit(patient1.getBank_account());
                } else {
                    bankacc_Transl = patient1.getBank_account();
                }

                int spinner_position = bankaccount_adapt.getPosition(bankacc_Transl);
                bankaccount_spinner.setSelection(spinner_position);
            }
            if (patient1.getMobile_type() != null && !patient1.getMobile_type().equalsIgnoreCase("")) {
                String mobile_Transl = "";
                /*if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    mobile_Transl = StringUtils.switch_hi_mobiletype_edit(patient1.getMobile_type());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    mobile_Transl = StringUtils.switch_or_mobiletype_edit(patient1.getMobile_type());
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    mobile_Transl = StringUtils.switch_gu_mobiletype_edit(patient1.getMobile_type());
                }else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    mobile_Transl = StringUtils.switch_as_mobiletype_edit(patient1.getMobile_type());
                } else {
                    mobile_Transl = patient1.getMobile_type();
                }*/

                mobile_Transl = getMobilePhoneOwnership(patient1.getMobile_type(), updatedContext, context, sessionManager.getAppLanguage());
                int spinner_position = mobile_adapt.getPosition(mobile_Transl);
                mobilephone_spinner.setSelection(spinner_position);
            }
            if (patient1.getWhatsapp_mobile() != null && !patient1.getWhatsapp_mobile()
                    .equalsIgnoreCase("")) {
//                String whatsapp_Transl = "";
//                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                    whatsapp_Transl = StringUtils.switch_hi_whatsapp_edit(patient1.getWhatsapp_mobile());
//                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
//                    whatsapp_Transl = StringUtils.switch_or_whatsapp_edit(patient1.getWhatsapp_mobile());
//                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
//                    whatsapp_Transl = StringUtils.switch_gu_whatsapp_edit(patient1.getWhatsapp_mobile());
//                } else {
//                    whatsapp_Transl = patient1.getWhatsapp_mobile();
//                }
//
//                int spinner_position = whatsapp_adapt.getPosition(whatsapp_Transl);
//                whatsapp_spinner.setSelection(spinner_position);

                String whatsAppTranslation = getWhatsAppStrings(patient1.getWhatsapp_mobile(), updatedContext, context, sessionManager.getAppLanguage());
                int whatsAppPosition = whatsapp_adapt.getPosition(whatsAppTranslation);
                whatsapp_spinner.setSelection(whatsAppPosition);
            }

            //vaccination - start
            if (patient1.getVaccination() != null &&
                    !patient1.getVaccination().equalsIgnoreCase("")) {
                String vaccination_Transl = "";
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {

                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = StringUtils.switch_hi_vaccination_edit(patient1.getVaccination());
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("en")) {
                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = patient1.getVaccination();
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {

                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = StringUtils.switch_or_vaccination_edit(patient1.getVaccination());
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {

                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = StringUtils.switch_bn_vaccination_edit(patient1.getVaccination());
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {

                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = StringUtils.switch_kn_vaccination_edit(patient1.getVaccination());
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {

                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = StringUtils.switch_mr_vaccination_edit(patient1.getVaccination());
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {

                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = StringUtils.switch_as_vaccination_edit(patient1.getVaccination());
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {

                    if (patient1.getVaccination().equalsIgnoreCase("No")) {
                        framelayout_vaccination.setVisibility(View.GONE);
                        spinner_vaccination.setSelection(0);
                    } else {
                        if (patient1.getVaccination().equalsIgnoreCase("Age less than 18 years")) {
                            framelayout_vaccine_question.setVisibility(View.GONE);
                            framelayout_vaccination.setVisibility(View.GONE);
                            int spinner_position = vaccination_adapt.getPosition(patient1.getVaccination());
                            spinner_vaccination.setSelection(spinner_position);
                            radioYes.setChecked(false);
                            radioNo.setChecked(false);
                        } else {
                            vaccination_Transl = StringUtils.switch_gu_vaccination_edit(patient1.getVaccination());
                            framelayout_vaccination.setVisibility(View.VISIBLE);
                            int spinner_position = vaccination_adapt.getPosition(vaccination_Transl);
                            spinner_vaccination.setSelection(spinner_position);
                        }
                    }
                }
            }
            //vaccinatio - end

            // Set data for marital spinner
            if (patient1.getMaritalStatus() != null && !patient1.getMaritalStatus().equalsIgnoreCase("")) {
                int spinnerPosition = maritalAdapter.getPosition(getMaritalStatusStrings
                        (patient1.getMaritalStatus(), updatedContext, context, sessionManager.getAppLanguage()));
                maritalStatusSpinner.setSelection(spinnerPosition);
            }

            // Set data for bp spinner
            if (patient1.getBpChecked() != null && !patient1.getBpChecked().equalsIgnoreCase("")) {
                int spinnerPosition = bp_adapt.getPosition(getTestStrings(patient1.getBpChecked(), updatedContext, context, sessionManager.getAppLanguage()));
                bpSpinner.setSelection(spinnerPosition);
            }

            // Set data for sugar level spinner
            if (patient1.getSugarLevelChecked() != null && !patient1.getSugarLevelChecked().equalsIgnoreCase("")) {
                int spinnerPosition = sugar_adapt.getPosition(getTestStrings(patient1.getSugarLevelChecked(), updatedContext, context, sessionManager.getAppLanguage()));
                sugarLevelSpinner.setSelection(spinnerPosition);
            }

            // Set data for hb checked spinner
            if (patient1.getHbChecked() != null && !patient1.getHbChecked().equalsIgnoreCase("")) {
                int spinnerPosition = hbLevel_adapt.getPosition(getTestStrings(patient1.getHbChecked(), updatedContext, context, sessionManager.getAppLanguage()));
                hbLevelSpinner.setSelection(spinnerPosition);
            }

           /* // Set data for bmi checked spinner
            if (patient1.getBmiChecked() != null && !patient1.getBmiChecked().equalsIgnoreCase("")) {
                int spinnerPosition = bmi_adapt.getPosition(getTestStrings(patient1.getBmiChecked(), updatedContext, context, sessionManager.getAppLanguage()));
                bmiLevelSpinner.setSelection(spinnerPosition);
            }
*/
            if (patient1.getAyushmanCardStatus() != null && !patient1.getAyushmanCardStatus().equalsIgnoreCase("")) {
                setSelectedCheckboxes(binding.ayushmanRadioGroup, patient1.getAyushmanCardStatus(),
                        updatedContext, context, sessionManager.getAppLanguage());
            }

            if (patient1.getMgnregaCardStatus() != null && !patient1.getMgnregaCardStatus().equalsIgnoreCase("")) {
                setSelectedCheckboxes(binding.mgnregaRadioGroup, patient1.getMgnregaCardStatus(),
                        updatedContext, context, sessionManager.getAppLanguage());
            }


            // Set data for head of household
            if (patient1.getHeadOfHousehold() != null && !patient1.getHeadOfHousehold().equalsIgnoreCase("-")) {
                binding.hohNo.setChecked(true);
                binding.cardviewHousehold.setVisibility(View.GONE);
                binding.cardviewHohRelationship.setVisibility(View.VISIBLE);

                int spinnerPosition = hohRelationshipAdapter.getPosition(hohRelationship(patient1.getHeadOfHousehold(), updatedContext, context, sessionManager.getAppLanguage()));
                hohRelationshipSpinner.setSelection(spinnerPosition);
            }

            if (patient1.getHeadOfHousehold() != null && patient1.getHeadOfHousehold().equalsIgnoreCase("-")) {
                binding.hohYes.setChecked(true);
                binding.cardviewHousehold.setVisibility(View.VISIBLE);
                binding.cardviewHohRelationship.setVisibility(View.GONE);

                //editText values values are set for the household fields ...
                no_of_member_edittext.setText(patient1.getNo_of_family_members());
                no_of_staying_members_edittext.setText(patient1.getNo_of_family_currently_live());

                if (patient1.getTime_travel_water() != null && !patient1.getTime_travel_water().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.waterSource30minutesRadioGroup,
                            patient1.getTime_travel_water(),
                            updatedContext,
                            context,
                            sessionManager.getAppLanguage()
                    );
                }

/*
                if (patient1.getTime_travel_water() != null || patient1.getHectars_land() != null) {
                    if (patient1.getTime_travel_water().equalsIgnoreCase("Declined to answer")) {
                        time_water_editText.setVisibility(View.GONE);
                        time_water_checkbox.setChecked(true);
                    } else {
                        time_water_editText.setVisibility(View.VISIBLE);
                        time_water_editText.setText(patient1.getTime_travel_water().replaceAll("hours",
                                getResources().getString(R.string.identification_screen_picker_hours)).replaceAll("minute",
                                getResources().getString(R.string.identification_screen_picker_minute)));
                    }
                }
*/

                if (patient1.getSource_of_water() != null && !patient1.getSource_of_water().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout,
                            patient1.getSource_of_water(),
                            updatedContext,
                            context,
                            sessionManager.getAppLanguage()
                    );
                }

                if (patient1.getWater_safe() != null && !patient1.getWater_safe().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.householdMakeSafeWaterCheckboxLinearLayout,
                            patient1.getWater_safe(),
                            updatedContext,
                            context,
                            sessionManager.getAppLanguage());
                }

                if (patient1.getHectars_land() != null && !patient1.getHectars_land().equalsIgnoreCase("")) {
                    String[] splitString = patient1.getHectars_land().split(" ");
                    if (splitString.length == 2) {
                        if (!splitString[0].equalsIgnoreCase("-") && !splitString[0].equalsIgnoreCase(""))
                            binding.landOwnedEditText.setText(splitString[0].trim());
                        int spinnerPosition = unitsAdapter.getPosition(getLandOwnedStrings(splitString[1],
                                updatedContext, context, sessionManager.getAppLanguage()));
                        unitsSpinner.setSelection(spinnerPosition);

                    }
                    else {
//                        if (!splitString[0].equalsIgnoreCase("-") && !splitString[0].equalsIgnoreCase(""))
//                            binding.landOwnedEditText.setText(splitString[0].trim());
                        int spinnerPosition = unitsAdapter.getPosition(getLandOwnedStrings(splitString[0],
                                updatedContext, context, sessionManager.getAppLanguage()));
                        unitsSpinner.setSelection(spinnerPosition);

                    }
                }

                // ration card moved here - start...
                if (patient1.getRationCardStatus() != null && !patient1.getRationCardStatus().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.rationCardRadioGroup, patient1.getRationCardStatus(),
                            updatedContext, context, sessionManager.getAppLanguage());
                }

                // Economic Card
                if (patient1.getEconomic_status() == null || patient1.getEconomic_status().equals(getResources().getString(R.string.not_provided)))
                    mEconomicStatus.setSelection(0);
                else {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String economic = switch_hi_economic_edit(patient1.getEconomic_status());
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String economic = switch_or_economic_edit(patient1.getEconomic_status());
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String economic = switch_bn_economic_edit(patient1.getEconomic_status());
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                    }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String economic = switch_kn_economic_edit(patient1.getEconomic_status());
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String economic = switch_mr_economic_edit(patient1.getEconomic_status());
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String economic = switch_gu_economic_edit(patient1.getEconomic_status());
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String economic = switch_as_economic_edit(patient1.getEconomic_status());
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                    } else {
                        mEconomicStatus.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));
                    }
                }
                // moved here - end

                if (patient1.getReligion() != null && !patient1.getReligion().equalsIgnoreCase("")) {
                    String religionTranslation = getReligionStrings(patient1.getReligion(), updatedContext, context, sessionManager.getAppLanguage());
                    int position = religionAdapter.getPosition(religionTranslation);
                    binding.religionDropDown.setSelection(position);
                }

                if (patient1.getNumberOfSmartphones() != null && !patient1.getNumberOfSmartphones().equalsIgnoreCase("")) {
                    binding.numberOfSmartphonesEditText.setText(patient1.getNumberOfSmartphones());
                }

                if (patient1.getNumberOfFeaturePhones() != null && !patient1.getNumberOfFeaturePhones().equalsIgnoreCase("")) {
                    binding.numberOfFeaturePhonesEditText.setText(patient1.getNumberOfFeaturePhones());
                }

                if (patient1.getNumberOfEarningMembers() != null && !patient1.getNumberOfEarningMembers().equalsIgnoreCase("")) {
                    binding.noOfEarningMembersEditText.setText(patient1.getNumberOfEarningMembers());
                }

                if (patient1.getWaterSupplyStatus() != null && !patient1.getWaterSupplyStatus().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.householdRunningWaterRadioGroup, patient1.getWaterSupplyStatus(), updatedContext, context, sessionManager.getAppLanguage());

                    if (patient1.getWaterSupplyHoursPerDay() != null && !patient1.getWaterSupplyStatus().equalsIgnoreCase("") &&
                            !patient1.getWaterSupplyStatus().equalsIgnoreCase(getString(R.string.generic_no))) {
                        String[] timeArray = patient1.getWaterSupplyHoursPerDay().split(" ");
                        if (timeArray.length == 2) {
                            String time = timeArray[0] + " " + getTimeStrings(timeArray[1], updatedContext, context, sessionManager.getAppLanguage());
                            binding.runningWaterHoursEditText.setText(time);
                        }
                    }

                    if (patient1.getWaterSupplyDaysPerWeek() != null && !patient1.getWaterSupplyDaysPerWeek().equalsIgnoreCase("") &&
                            !patient1.getWaterSupplyStatus().equalsIgnoreCase(getString(R.string.generic_no))) {
                        String[] timeArray = patient1.getWaterSupplyDaysPerWeek().split(" ");
                        if (timeArray.length == 2) {
                            String time = timeArray[0] + " " + getTimeStrings(timeArray[1], updatedContext, context, sessionManager.getAppLanguage());
                            binding.runningWaterDaysEditText.setText(time);
                        }
                    }

                }

                if (patient1.getElectricityStatus() != null && !patient1.getElectricityStatus().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.householdElectricityRadioGroup, patient1.getElectricityStatus(), updatedContext, context, sessionManager.getAppLanguage());

                    if (patient1.getLoadSheddingHoursPerDay() != null && !patient1.getLoadSheddingHoursPerDay().equalsIgnoreCase("")
                            && !patient1.getElectricityStatus().equalsIgnoreCase(getString(R.string.generic_no))) {
                        String[] timeArray = patient1.getLoadSheddingHoursPerDay().split(" ");
                        if (timeArray.length == 2) {
                            String time = timeArray[0] + " " + getTimeStrings(timeArray[1], updatedContext, context, sessionManager.getAppLanguage());
                            binding.loadSheddingHoursEditText.setText(time);
                        }
                    }

                    if (patient1.getLoadSheddingDaysPerWeek() != null && !patient1.getLoadSheddingDaysPerWeek().equalsIgnoreCase("")
                            && !patient1.getElectricityStatus().equalsIgnoreCase(getString(R.string.generic_no))) {
                        String[] timeArray = patient1.getLoadSheddingDaysPerWeek().split(" ");
                        if (timeArray.length == 2) {
                            String time = timeArray[0] + " " + getTimeStrings(timeArray[1], updatedContext, context, sessionManager.getAppLanguage());
                            binding.loadSheddingDaysEditText.setText(time);
                        }
                    }

                }

                if (patient1.getAverageAnnualHouseholdIncome() != null && !patient1.getAverageAnnualHouseholdIncome().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.averageAnnualHouseholdIncomeRadioGroup, patient1.getAverageAnnualHouseholdIncome(), updatedContext, this, sessionManager.getAppLanguage());
                }

              /*  if (patient1.getAverageExpenditureOnHealth() != null && !patient1.getAverageExpenditureOnHealth().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.annualHealthExpenditureRadioGroup, patient1.getAverageExpenditureOnHealth(), updatedContext, this, sessionManager.getAppLanguage());
                }
*/
/*
                if (patient1.getAverageExpenditureOnEducation() != null && !patient1.getAverageExpenditureOnEducation().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.educationExpenditureRadioGroup, patient1.getAverageExpenditureOnEducation(), updatedContext, this, sessionManager.getAppLanguage());
                }
*/

                // Ekal process on Edit click -> Set fields with values from db.
                if (patient1.getEkalProcess() != null && !patient1.getEkalProcess().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.ekalProcessRadioGroup, patient1.getEkalProcess(),
                            updatedContext, this, sessionManager.getAppLanguage());
                }
                // Water source within 30 minutes -> set fields.
                if (patient1.getTime_travel_water() != null && !patient1.getTime_travel_water().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.waterSource30minutesRadioGroup, patient1.getTime_travel_water(),
                            updatedContext, this, sessionManager.getAppLanguage());
                }


                if (patient1.getCookingFuel() != null && !patient1.getCookingFuel().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.householdCookingFuelCheckboxLinearLayout, patient1.getCookingFuel(),
                            updatedContext, this, sessionManager.getAppLanguage());
                }

                if (patient1.getHouseholdLighting() != null && !patient1.getHouseholdLighting().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.mainSourceOfLightingCheckboxLinearLayout, patient1.getHouseholdLighting(),
                            updatedContext, this, sessionManager.getAppLanguage());
                }

/*
                if (patient1.getOpenDefecationStatus() != null && !patient1.getOpenDefecationStatus().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.openDefecationRadioGroup, patient1.getOpenDefecationStatus(), updatedContext, context, sessionManager.getAppLanguage());
                    if (patient1.getOpenDefecationStatus().equalsIgnoreCase(getString(R.string.generic_yes)))
                        binding.llReasonForOpenDefecation.setVisibility(View.VISIBLE);
                    else
                        binding.llReasonForOpenDefecation.setVisibility(View.GONE);
                }
*/

                if (binding.llReasonForOpenDefecation.getVisibility() == View.VISIBLE &&
                        patient1.getReasonForOpenDefecation() != null && !patient1.getReasonForOpenDefecation().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.reasonForOpenDefecationCheckboxLinearLayout, patient1.getReasonForOpenDefecation(), updatedContext, context, sessionManager.getAppLanguage());
                }

                if (patient1.getHandWashOccasion() != null && !patient1.getHandWashOccasion().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.handWashOccasionLinearLayout, patient1.getHandWashOccasion(),
                            updatedContext, context, sessionManager.getAppLanguage());
                }

/*
                if (patient1.getFoodPreparedInTwentyFourHours() != null && !patient1.getFoodPreparedInTwentyFourHours().equalsIgnoreCase("")) {
                    setSelectedCheckboxes(binding.foodCookedInTwentyFourHoursLinearLayout, patient1.getFoodPreparedInTwentyFourHours(), updatedContext, context, sessionManager.getAppLanguage());
                }
*/

              /*  if (patient1.getWater_availability() != null && !patient1.getWater_availability()
                        .equalsIgnoreCase("")) {

                    String wateravail_Transl = "";
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        wateravail_Transl = StringUtils.switch_hi_wateravail_edit(patient1.getWater_availability());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        wateravail_Transl = StringUtils.switch_or_wateravail_edit(patient1.getWater_availability());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        wateravail_Transl = StringUtils.switch_bn_wateravail_edit(patient1.getWater_availability());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        wateravail_Transl = StringUtils.switch_gu_wateravail_edit(patient1.getWater_availability());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        wateravail_Transl = StringUtils.switch_as_wateravail_edit(patient1.getWater_availability());
                    } else {
                        wateravail_Transl = patient1.getWater_availability();
                    }

                    int spinner_position = availa_adapt.getPosition(wateravail_Transl);
                    water_availability_spinner.setSelection(spinner_position);
                }
              */
                if (patient1.getToilet_facility() != null && !patient1.getToilet_facility()
                        .equalsIgnoreCase("")) {

                    String toiletfacility_Transl = "";
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        toiletfacility_Transl = StringUtils.switch_hi_toiletfacil_edit(patient1.getToilet_facility());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        toiletfacility_Transl = StringUtils.switch_or_toiletfacil_edit(patient1.getToilet_facility());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        toiletfacility_Transl = StringUtils.switch_bn_toiletfacil_edit(patient1.getToilet_facility());
                    }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        toiletfacility_Transl = StringUtils.switch_kn_toiletfacil_edit(patient1.getToilet_facility());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        toiletfacility_Transl = StringUtils.switch_mr_toiletfacil_edit(patient1.getToilet_facility());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        toiletfacility_Transl = StringUtils.switch_gu_toiletfacil_edit(patient1.getToilet_facility());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        toiletfacility_Transl = StringUtils.switch_as_toiletfacil_edit(patient1.getToilet_facility());
                    } else {
                        toiletfacility_Transl = patient1.getToilet_facility();
                    }

                    int spinner_position = toiletfacility_adapt.getPosition(toiletfacility_Transl);
                    toilet_facility_spinner.setSelection(spinner_position);

                    if (spinner_position >= 0) {
                        toilet_facility_spinner.setSelection(spinner_position); //user selected value items from spinner
                    }
                    //since we will have to show our dynamic values here..
                    else {
                        //on edit the spinner value will be selected based on the current app lang...
                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("अन्य [दर्ज करें]"));
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]"));
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("অন্যান্য [এন্টার]"));
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("ಇತರೆ[ನಮೂದಿಸಿ]"));
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("इतर[एंटर]"));
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                            //-------replace with gujrati
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("અન્ય [દાખલ કરો]"));
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                            //-------replace with assamese
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("অন্যান্য [প্ৰৱেশ কৰা]"));
                        } else {
                            toilet_facility_spinner.setSelection(toiletfacility_adapt.getPosition("Other[Enter]"));
                        }

                        toiletfacility_edittext.setVisibility(View.VISIBLE);
                        toiletfacility_edittext.setText(patient1.getToilet_facility());
                    }
                }

                if (patient1.getStructure_house() != null && !patient1.getStructure_house()
                        .equalsIgnoreCase("")) {

                    String housestruct_Transl = "";
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        housestruct_Transl = StringUtils.switch_hi_housestructure_edit(patient1.getStructure_house());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        housestruct_Transl = StringUtils.switch_or_housestructure_edit(patient1.getStructure_house());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        housestruct_Transl = StringUtils.switch_bn_housestructure_edit(patient1.getStructure_house());
                    }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        housestruct_Transl = StringUtils.switch_kn_housestructure_edit(patient1.getStructure_house());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        housestruct_Transl = StringUtils.switch_mr_housestructure_edit(patient1.getStructure_house());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        housestruct_Transl = StringUtils.switch_gu_housestructure_edit(patient1.getStructure_house());
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        housestruct_Transl = StringUtils.switch_as_housestructure_edit(patient1.getStructure_house());
                    } else {
                        housestruct_Transl = patient1.getStructure_house();
                    }

                    int spinner_position = structure_adapt.getPosition(housestruct_Transl);
                    structure_of_house_spinner.setSelection(spinner_position);
                }
            }


        } else {
            mCountry.setSelection(countryAdapter.getPosition(country1));
        }

        //mCaste.setSelection(casteAdapter.getPosition(patient1.getCaste())); //edit...

        NewLocationDao newLocationDao = new NewLocationDao();
        List<String> villageList = newLocationDao.getVillageList(sessionManager.getStateName(), sessionManager.getDistrictName()
                , sessionManager.getSanchName(), context);
        if (villageList.size() > 1) {
            LocationArrayAdapter locationArrayAdapter =
                    new LocationArrayAdapter(IdentificationActivity.this, villageList);
            mVillage.setAdapter(locationArrayAdapter);
            mVillage.setEnabled(false);
            if (patientID_edit != null) {
                mVillage.setSelection(locationArrayAdapter.getPosition(patient1.getCity_village()));
            } else {
                mVillage.setSelection(locationArrayAdapter.getPosition(sessionManager.getVillageName()));
            }
        }

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.state_error, R.layout.custom_spinner);
        //  stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(stateAdapter);
        mState.setEnabled(false);

        mCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
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
                    // mCity.setThreshold(1);//will start working from first character
                    // mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else if (state.matches("Bukidnon")) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.bukidnon_villages, R.layout.custom_spinner);
                    // mCity.setThreshold(1);//will start working from first character
                    // mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else {
                    // mCity.setAdapter(null);
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

        mGenderO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        radioYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        radioNo.setOnClickListener(new View.OnClickListener() {
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
        //  Locale.setDefault(Locale.ENGLISH);

        mDOBPicker = new

                DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                mDOB.setError(null);
                mAge.setError(null);
                //Set Maximum date to current date because even after bday is less than current date
                // it goes to check date is set after today...
                mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                //   Locale.setDefault(Locale.ENGLISH);
                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy",
                        Locale.forLanguageTag(sessionManager.getAppLanguage()));

                dob.set(year, monthOfYear, dayOfMonth);
                dob_indexValue = monthOfYear; //fetching the inex value of month selected...
                String dobString = simpleDateFormat.format(dob.getTime());
                mDOB.setText(dobString);
                mDOBYear = year;
                mDOBMonth = monthOfYear;
                mDOBDay = dayOfMonth;
//                mDOB.setText(mDOBDay + getResources().getString(R.));


                String age = getYear(dob.get(Calendar.YEAR), dob.get(Calendar.MONTH), dob.get(Calendar.DATE),
                        today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
                //get years months days
                String[] frtData = age.split("-");

                String[] yearData = frtData[0].split(" ");
                String[] monthData = frtData[1].split(" ");
                String[] daysData = frtData[2].split(" ");

                mAgeYears = Integer.valueOf(yearData[0]);
                mAgeMonths = Integer.valueOf(monthData[1]);
                mAgeDays = Integer.valueOf(daysData[1]);
                String ageString = mAgeYears + getString(R.string.identification_screen_text_years) + " - " +
                        mAgeMonths + getString(R.string.identification_screen_text_months) + " - " +
                        mAgeDays + getString(R.string.days);

                mAge.setText(ageString);

                //vaccination if above or equal to 18 than show visibility....
                if (mAgeYears >= 18) {
                    framelayout_vaccine_question.setVisibility(View.VISIBLE);
                    ll18.setVisibility(View.VISIBLE);
                    // framelayout_vaccination.setVisibility(View.GONE);
                } else {
                    framelayout_vaccine_question.setVisibility(View.GONE);
                    ll18.setVisibility(View.GONE);
                    if (radioYes.isChecked()) //so that no previous data be gone to the db
                        radioYes.setChecked(false);
                    if (radioNo.isChecked())
                        radioNo.setChecked(false);

                    spinner_vaccination.setSelection(0);
                }


            }
        }, mDOBYear, mDOBMonth, mDOBDay);


        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().

                setMaxDate(System.currentTimeMillis());
        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });

        //if patient update then age will be set
        if (patientID_edit != null) {
            //dob to be displayed based on translation...
            String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth());
            Log.v("main", "dob: " + patient1.getDate_of_birth() + "\n" + dob);

            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                String dob_text = en__bn_dob(dob); //to show text of English into bengali...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                String dob_text = en__kn_dob(dob); //to show text of English into kannada...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                String dob_text = en__mr_dob(dob); //to show text of English into kannada...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                String dob_text = en__gu_dob(dob); //to show text of English into Gujrati...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                String dob_text = en__as_dob(dob); //to show text of English into assamese...
                mDOB.setText(dob_text);
            } else {
                mDOB.setText(dob);
            }

            //  mDOB.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
            //get year month days
            String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth(), context);

            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth()).split(" ");
            mAgeYears = Integer.valueOf(ymdData[0]);
            mAgeMonths = Integer.valueOf(ymdData[1]);
            mAgeDays = Integer.valueOf(ymdData[2]);
            mAge.setText(yrMoDays);

            //vaccination if above or equal to 18 than show visibility....
            if (mAgeYears >= 18) {
                framelayout_vaccine_question.setVisibility(View.VISIBLE);
                // framelayout_vaccination.setVisibility(View.GONE);
                ll18.setVisibility(View.VISIBLE);
            } else {
                framelayout_vaccine_question.setVisibility(View.GONE);
                ll18.setVisibility(View.GONE);
                if (radioYes.isChecked())
                    radioYes.setChecked(false);
                if (radioNo.isChecked())
                    radioNo.setChecked(false);

                spinner_vaccination.setSelection(0);
            }

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

                    //vaccination if above or equal to 18 than show visibility....
                    if (mAgeYears >= 18) {
                        framelayout_vaccine_question.setVisibility(View.VISIBLE);
                        ll18.setVisibility(View.VISIBLE);
                        // framelayout_vaccination.setVisibility(View.GONE);
                    } else {
                        framelayout_vaccine_question.setVisibility(View.GONE);
                        ll18.setVisibility(View.GONE);
                        if (radioYes.isChecked())
                            radioYes.setChecked(false);
                        if (radioNo.isChecked())
                            radioNo.setChecked(false);

                        spinner_vaccination.setSelection(0);
                    }

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
                    //    Locale.setDefault(Locale.ENGLISH);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy",
                            Locale.forLanguageTag(sessionManager.getAppLanguage()));
                    dob.set(mDOBYear, mDOBMonth, mDOBDay);
                    String dobString = simpleDateFormat.format(dob.getTime());
                    mDOB.setText(dobString);
                    mDOBPicker.updateDate(mDOBYear, mDOBMonth, mDOBDay);
                    dob_indexValue = mDOBPicker.getDatePicker().getMonth(); //if user manually selects Age then...
                    Log.d("dd", "dd: " + dob_indexValue);
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
        fab.setOnClickListener(v ->

        {
            if (patientID_edit != null) {
                onPatientUpdateClicked(patient1);
            } else {
                onPatientCreateClicked();
            }
        });

        hohRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == hohYes.getId()) {
                    cardview_household.setVisibility(View.VISIBLE);
                    hohRelationshipCardView.setVisibility(View.GONE);
                    no_of_member_edittext.requestFocus();
                    no_of_member_edittext.setFocusable(true);
                    no_of_member_edittext.setFocusableInTouchMode(true);
                } else {
                    cardview_household.setVisibility(View.GONE);
                    hohRelationshipCardView.setVisibility(View.VISIBLE);
                }
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
                Toast.makeText(this, "Current Date must be greater than Date of Birth", Toast.LENGTH_LONG).show();
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
            case R.id.identification_gender_other:
                if (checked)
                    mGender = "O";
                Log.v(TAG, "gender: " + mGender);
                break;
            case R.id.identification_yes:
                if (checked)
                    framelayout_vaccination.setVisibility(View.VISIBLE);
                break;
            case R.id.identification_no:
                if (checked)
                    framelayout_vaccination.setVisibility(View.GONE);
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

        patientUuid = uuidGenerator.generateUuid();

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
                patient1.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                patient1.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
//                patient1.setBank_account(idCursor.getString(idCursor.getColumnIndexOrThrow("Bank Account")));
//                patient1.setMobile_type(idCursor.getString(idCursor.getColumnIndexOrThrow("Mobile Phone Type")));
//                patient1.setWhatsapp_mobile(idCursor.getString(idCursor.getColumnIndexOrThrow("Use WhatsApp")));
//                patient1.setNo_of_family_members(idCursor.getString(idCursor.getColumnIndexOrThrow("Total Family Members")));
//                patient1.setNo_of_family_currently_live(idCursor.getString(idCursor.getColumnIndexOrThrow("Total Family Members Staying")));
//                patient1.setSource_of_water(idCursor.getString(idCursor.getColumnIndexOrThrow("Drinking Water Source")));
//                patient1.setWater_safe(idCursor.getString(idCursor.getColumnIndexOrThrow("Safe Drinking Water")));
//                patient1.setTime_travel_water(idCursor.getString(idCursor.getColumnIndexOrThrow("Time Drinking Water Source")));
//                patient1.setWater_availability(idCursor.getString(idCursor.getColumnIndexOrThrow("Drinking Water Availability")));
//                patient1.setToilet_facility(idCursor.getString(idCursor.getColumnIndexOrThrow("Toilet Facility")));
//                patient1.setStructure_house(idCursor.getString(idCursor.getColumnIndexOrThrow("House Structure")));
//                patient1.setHectars_land(idCursor.getString(idCursor.getColumnIndexOrThrow("Family Cultivable Land")));

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
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient1.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient1.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Bank Account")) {
                    patient1.setBank_account(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Mobile Phone Type")) {
                    patient1.setMobile_type(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Use WhatsApp")) {
                    patient1.setWhatsapp_mobile(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Covid Vaccination")) {
                    patient1.setVaccination(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("martialStatus")) {
                    patient1.setMaritalStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("bpChecked")) {
                    patient1.setBpChecked(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("sugarChecked")) {
                    patient1.setSugarLevelChecked(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("hbChecked")) {
                    patient1.setHbChecked(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("bmiChecked")) {
                    patient1.setBmiChecked(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("hohRelationship")) {
                    patient1.setHeadOfHousehold(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("religion")) {
                    patient1.setReligion(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Total Family Members")) {
                    patient1.setNo_of_family_members(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Total Family Members Staying")) {
                    patient1.setNo_of_family_currently_live(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // numberOfSmartphones
                if (name.equalsIgnoreCase("numberOfSmartphones")) {
                    patient1.setNumberOfSmartphones(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // numberOfFeaturePhones
                if (name.equalsIgnoreCase("numberOfFeaturePhones")) {
                    patient1.setNumberOfFeaturePhones(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // numberOfEarningMembers
                if (name.equalsIgnoreCase("numberOfEarningMembers")) {
                    patient1.setNumberOfEarningMembers(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // electricityStatus
                if (name.equalsIgnoreCase("electricityStatus")) {
                    patient1.setElectricityStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // loadSheddingHoursPerDay
                if (name.equalsIgnoreCase("loadSheddingHoursPerDay")) {
                    patient1.setLoadSheddingHoursPerDay(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // loadSheddingDaysPerWeek
                if (name.equalsIgnoreCase("loadSheddingDaysPerWeek")) {
                    patient1.setLoadSheddingDaysPerWeek(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // runningWaterAvailability
                if (name.equalsIgnoreCase("runningWaterAvailability")) {
                    patient1.setWaterSupplyStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // waterSupplyAvailabilityHoursPerDay
                if (name.equalsIgnoreCase("waterSupplyAvailabilityHoursPerDay")) {
                    patient1.setWaterSupplyHoursPerDay(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // waterSupplyAvailabilityDaysPerWeek
                if (name.equalsIgnoreCase("waterSupplyAvailabilityDaysPerWeek")) {
                    patient1.setWaterSupplyDaysPerWeek(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Drinking Water Source")) {
                    patient1.setSource_of_water(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Safe Drinking Water")) {
                    patient1.setWater_safe(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Time Drinking Water Source")) {
                    patient1.setTime_travel_water(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Drinking Water Availability")) {
                    patient1.setWater_availability(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Toilet Facility")) {
                    patient1.setToilet_facility(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("House Structure")) {
                    patient1.setStructure_house(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Family Cultivable Land")) {
                    patient1.setHectars_land(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // averageAnnualHouseholdIncome
                if (name.equalsIgnoreCase("averageAnnualHouseholdIncome")) {
                    patient1.setAverageAnnualHouseholdIncome(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // averageExpenditureOnHealth
                if (name.equalsIgnoreCase("averageExpenditureOnHealth")) {
                    patient1.setAverageExpenditureOnHealth(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // ayushmanCardStatus
                if (name.equalsIgnoreCase("ayushmanCardStatus")) {
                    patient1.setAyushmanCardStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // mgnregaCardStatus
                if (name.equalsIgnoreCase("mgnregaCardStatus")) {
                    patient1.setMgnregaCardStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // ayushmanCardStatus
                if (name.equalsIgnoreCase("rationCardStatus")) {
                    patient1.setRationCardStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // averageExpenditureOnEducation
                if (name.equalsIgnoreCase("averageExpenditureOnEducation")) {
                    patient1.setAverageExpenditureOnEducation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // cookingFuel
                if (name.equalsIgnoreCase("cookingFuel")) {
                    patient1.setCookingFuel(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // householdLighting
                if (name.equalsIgnoreCase("householdLighting")) {
                    patient1.setHouseholdLighting(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // defecatedInOpen
                if (name.equalsIgnoreCase("defecatedInOpen")) {
                    patient1.setOpenDefecationStatus(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // reasonForOpenDefecation
                if (name.equalsIgnoreCase("reasonForOpenDefecation")) {
                    patient1.setReasonForOpenDefecation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // soapHandWashingOccasion
                if (name.equalsIgnoreCase("soapHandWashingOccasion")) {
                    patient1.setHandWashOccasion(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // ekal process - on edit set fields.
                if (name.equalsIgnoreCase("TakeOurService")) {
                    patient1.setEkalProcess(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                // foodItemsPreparedInTwentyFourHours
                if (name.equalsIgnoreCase("foodItemsPreparedInTwentyFourHours")) {
                    patient1.setFoodPreparedInTwentyFourHours(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("otherMedicalHistory")) {
                    String value = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    medicalHistoryList = new Gson().fromJson(value, new TypeToken<List<MedicalHistory>>() {
                    }.getType());
                    medicalHistoryAdapter = new MedicalHistoryAdapter(medicalHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
                    medicalHistoryViewPager.setAdapter(medicalHistoryAdapter);
                    medicalHistoryViewPager.setCurrentItem(medicalHistoryList.size() - 1);
                    medicalHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    setViewPagerOffset(medicalHistoryViewPager);
                }

                if (name.equalsIgnoreCase("smokingStatus")) {
                    String value = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    smokingHistoryList = new Gson().fromJson(value, new TypeToken<List<SmokingHistory>>() {
                    }.getType());
                    smokingHistoryAdapter = new SmokingHistoryAdapter(smokingHistoryList, sessionManager.getAppLanguage(),
                            this, updatedContext, this);
                    smokingHistoryViewPager.setAdapter(smokingHistoryAdapter);
                    smokingHistoryViewPager.setCurrentItem(smokingHistoryList.size() - 1);
                    smokingHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    setViewPagerOffset(smokingHistoryViewPager);
                }
                if (name.equalsIgnoreCase("TobaccoStatus")) {
                    String value = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    tobaccoHistoryList = new Gson().fromJson(value, new TypeToken<List<TobaccoHistory>>() {
                    }.getType());
                    tobaccoHistoryAdapter = new TobaccoHistoryAdapter(tobaccoHistoryList, sessionManager.getAppLanguage(),
                            this, updatedContext, this);
                    tobaccoHistoryViewPager.setAdapter(tobaccoHistoryAdapter);
                    tobaccoHistoryViewPager.setCurrentItem(tobaccoHistoryList.size() - 1);
                    tobaccoHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    setViewPagerOffset(tobaccoHistoryViewPager);
                }

                if (name.equalsIgnoreCase("alcoholConsumptionStatus")) {
                    String value = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                    alcoholConsumptionHistoryList = new Gson().fromJson(value, new TypeToken<List<AlcoholConsumptionHistory>>() {
                    }.getType());
                    alcoholConsumptionHistoryAdapter = new AlcoholConsumptionHistoryAdapter(alcoholConsumptionHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
                    alcoholViewPager.setAdapter(alcoholConsumptionHistoryAdapter);
                    alcoholViewPager.setCurrentItem(alcoholConsumptionHistoryList.size() - 1);
                    alcoholViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    setViewPagerOffset(alcoholViewPager);
                }

            }
            while (idCursor1.moveToNext());
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

        if (mPhoneNum.getText().toString().trim().length() > 0) {
            if (mPhoneNum.getText().toString().trim().length() < 10) {
                mPhoneNum.requestFocus();
                mPhoneNum.setError(getString(R.string.enter_10_digits));
                return;
            }

            String phoneNo = mPhoneNum.getText().toString().trim();
            if (phoneNo.equalsIgnoreCase("0000000000") || phoneNo.equalsIgnoreCase("1111111111") ||
                    phoneNo.equalsIgnoreCase("2222222222") || phoneNo.equalsIgnoreCase("3333333333") ||
                    phoneNo.equalsIgnoreCase("4444444444") || phoneNo.equalsIgnoreCase("5555555555") ||
                    phoneNo.equalsIgnoreCase("6666666666") || phoneNo.equalsIgnoreCase("7777777777") ||
                    phoneNo.equalsIgnoreCase("8888888888") || phoneNo.equalsIgnoreCase("9999999999") ||
                    phoneNo.equalsIgnoreCase("0123456789") || phoneNo.equalsIgnoreCase("1234567890")) {
              //  mPhoneNum.setText("");
                mPhoneNum.requestFocus();
                mPhoneNum.setError(getString(R.string.enter_valid_phone_number));
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

        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
                && !mPhoneNum.getText().toString().equals("") && !villageText.getText().toString().equals("")/*!mCity.getText().toString().equals("")*/ && !countryText.getText().toString().equals("") &&
                !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("")
                && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked() || mGenderO.isChecked())) {

            Log.v(TAG, "Result");

        } else {
            if (mFirstName.getText().toString().equals("")) {
                mFirstName.setError(getString(R.string.error_field_required));
            }

            if (mLastName.getText().toString().equals("")) {
                mLastName.setError(getString(R.string.error_field_required));
            }

            if (mDOB.getText().toString().equals("")) {
                mDOB.setError(getString(R.string.error_field_required));
            }

            if (mAge.getText().toString().equals("")) {
                mAge.setError(getString(R.string.error_field_required));
            }

            if (mPhoneNum.getText().toString().equals("")) {
                mPhoneNum.setError(getString(R.string.error_field_required));
            }

            /*if (mCity.getText().toString().equals("")) {
                mCity.setError(getString(R.string.error_field_required));
            }*/

            if (!mGenderF.isChecked() && !mGenderM.isChecked() && !mGenderO.isChecked()) {
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

        if (mVillage.getSelectedItemPosition() == 0) {
            villageText.setError(getString(R.string.error_field_required));
            focusView = villageText;
            cancel = true;
            return;
        } else {
            villageText.setError(null);
        }

        if (binding.ayushmanRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
            focusView = binding.ayushmanRadioGroup;
            cancel = true;
            return;
        }

        if (binding.mgnregaRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
            focusView = binding.mgnregaRadioGroup;
            cancel = true;
            return;
        }


        // TODO: Add validations for all Spinners here...
        if (occupation_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) occupation_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = occupation_spinner;
            cancel = true;
            return;
        }

        if (occupation_edittext.getVisibility() == View.VISIBLE && occupation_edittext.getText().toString().isEmpty() &&
                occupation_edittext.getText().toString().equalsIgnoreCase("") &&
                occupation_edittext.getText().toString() != null) {

            occupation_edittext.setError(getString(R.string.select));
            focusView = occupation_edittext;
            cancel = true;
            return;
        }

        if (bankaccount_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) bankaccount_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = bankaccount_spinner;
            cancel = true;
            return;
        }

        if (mobilephone_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) mobilephone_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = mobilephone_spinner;
            cancel = true;
            return;
        }

        if (whatsapp_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) whatsapp_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = whatsapp_spinner;
            cancel = true;
            return;
        }


        //vaccination
        if (framelayout_vaccine_question.getVisibility() == View.VISIBLE) {
            if (!radioYes.isChecked() && !radioNo.isChecked()) {
                MaterialAlertDialogBuilder alertDialogBuilder = new
                        MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.select_option_vaccination_dialog);
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

            if (radioYes.isChecked() && spinner_vaccination.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_vaccination.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_vaccination;
                cancel = true;
                return;
            }
        }
        //vaccination - end

        // Marital Status Validation
        if (maritalStatusSpinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) maritalStatusSpinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = maritalStatusSpinner;
            cancel = true;
            return;
        }

        if (ll18.getVisibility() == View.VISIBLE) {
            // BP Level Validation
            if (bpSpinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) bpSpinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = bpSpinner;
                cancel = true;
                return;
            }

            // Sugar Level Validation
            if (sugarLevelSpinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) sugarLevelSpinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = sugarLevelSpinner;
                cancel = true;
                return;
            }
        }

        // HB Level Validation
        if (hbLevelSpinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) hbLevelSpinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = hbLevelSpinner;
            cancel = true;
            return;
        }

        // BMI Validation
       /* if (bmiLevelSpinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) bmiLevelSpinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = bmiLevelSpinner;
            cancel = true;
            return;
        }
*/

        if (hohRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.please_state_if_you_are_the_head_of_the_family), Toast.LENGTH_SHORT).show();
            focusView = hohRadioGroup;
            cancel = true;
            return;
        }

        if (hohRadioGroup.getCheckedRadioButtonId() != -1 && hohRadioGroup.getCheckedRadioButtonId() == binding.hohYes.getId()) {

            // move Ration Card and Economic card here - Start
            if (binding.rationCardRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.rationCardRadioGroup;
                cancel = true;
                return;
            }
            // move Ration Card and Economic card here - End

            if (checkIfEmpty(this, binding.religionDropDown.getSelectedItem().toString())) {
                TextView t = (TextView) binding.religionDropDown.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = binding.religionDropDown;
                cancel = true;
                return;
            }

            if (no_of_member_edittext.getText().toString().equalsIgnoreCase("") &&
                    no_of_member_edittext.getText().toString().isEmpty()) {
                no_of_member_edittext.setError(getString(R.string.enter_number));
                // no_of_member_edittext.setTextColor(Color.RED);
                focusView = no_of_member_edittext;
                cancel = true;
                return;
            }

            if (no_of_staying_members_edittext.getText().toString().equalsIgnoreCase("") &&
                    no_of_staying_members_edittext.getText().toString().isEmpty()) {
                no_of_staying_members_edittext.setError(getString(R.string.enter_number));
                // no_of_staying_members_edittext.setTextColor(Color.RED);
                focusView = no_of_staying_members_edittext;
                cancel = true;
                return;
            }

            if (Integer.parseInt(no_of_staying_members_edittext.getText().toString()) > Integer.parseInt(no_of_member_edittext.getText().toString())) {
                no_of_staying_members_edittext.setError(getString(R.string.no_of_members_living_cannot_be_greater_than_total_number_of_members));
                focusView = no_of_staying_members_edittext;
                cancel = true;
                return;
            }

            // Validation for number of smartphones field
            if (checkIfEmpty(context, Objects.requireNonNull(binding.numberOfSmartphonesEditText.getText()).toString())) {
                binding.numberOfSmartphonesEditText.setError(getString(R.string.enter_number));
                focusView = binding.numberOfSmartphonesEditText;
                cancel = true;
                return;
            }

            // Validation for number of feature phones field
            if (checkIfEmpty(context, Objects.requireNonNull(binding.numberOfFeaturePhonesEditText.getText()).toString())) {
                binding.numberOfFeaturePhonesEditText.setError(getString(R.string.enter_number));
                focusView = binding.numberOfFeaturePhonesEditText;
                cancel = true;
                return;
            }

            // Validation for number of earning members field
            if (checkIfEmpty(context, Objects.requireNonNull(binding.noOfEarningMembersEditText.getText()).toString())) {
                binding.noOfEarningMembersEditText.setError(getString(R.string.enter_number));
                focusView = binding.noOfEarningMembersEditText;
                cancel = true;
                return;
            }

            // Validations for Electricity Status Radio Group
            if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_state_if_you_have_electricity_in_the_household), Toast.LENGTH_SHORT).show();
                focusView = binding.householdRunningWaterRadioGroup;
                cancel = true;
                return;
            }

            // Validations for Load Shedding Hours Edit Text
            if (binding.householdElectricityYes.isChecked() && checkIfEmpty(context, Objects.requireNonNull(binding.loadSheddingHoursEditText.getText()).toString())) {
                binding.loadSheddingHoursEditText.setError(getString(R.string.enter_number));
                focusView = binding.loadSheddingHoursEditText;
                cancel = true;
                return;
            }

            // Validations for Load Shedding Days Edit Text
            if (binding.householdElectricityYes.isChecked() && checkIfEmpty(context, Objects.requireNonNull(binding.loadSheddingDaysEditText.getText()).toString())) {
                binding.loadSheddingDaysEditText.setError(getString(R.string.enter_number));
                focusView = binding.loadSheddingDaysEditText;
                cancel = true;
                return;
            }

            // Validations for Running Water Radio Group
            if (binding.householdRunningWaterRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_state_if_you_have_running_water_in_the_household), Toast.LENGTH_SHORT).show();
                focusView = binding.householdRunningWaterRadioGroup;
                cancel = true;
                return;
            }

            // Validations for Running Water Hours Edit Text
            if (binding.waterSupplyYes.isChecked() &&
                    checkIfEmpty(context, Objects.requireNonNull(binding.runningWaterHoursEditText.getText()).toString())) {
                binding.runningWaterHoursEditText.setError(getString(R.string.enter_number));
                focusView = binding.runningWaterHoursEditText;
                cancel = true;
                return;
            }

            // Validations for Running Water Days Edit Text
            if (binding.waterSupplyYes.isChecked() && checkIfEmpty(context, Objects.requireNonNull(binding.runningWaterDaysEditText.getText()).toString())) {
                binding.runningWaterDaysEditText.setError(getString(R.string.enter_number));
                focusView = binding.runningWaterDaysEditText;
                cancel = true;
                return;
            }

            // Validations for Main Source of Drinking Water Linear Layout
            if (checkIfCheckboxesEmpty(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_select_the_main_source_of_drinking_water), Toast.LENGTH_SHORT).show();
                focusView = binding.mainSourceOfDrinkingWaterCheckboxLinearLayout;
                cancel = true;
                return;
            }

            if (checkIfCheckboxesEmpty(binding.householdMakeSafeWaterCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_select_the_methods_of_purifying_water), Toast.LENGTH_SHORT).show();
                focusView = binding.householdMakeSafeWaterCheckboxLinearLayout;
                cancel = true;
                return;
            }

/*
            if (!time_water_checkbox.isChecked() && time_water_editText.getText().toString().isEmpty() &&
                    time_water_editText.getText().toString().equalsIgnoreCase("")) {
                //checks if both the fields are not selected...
                time_water_checkbox.setError(getString(R.string.select));

                focusView = time_water_checkbox;
                focusView = time_water_editText;
                cancel = true;
                return;
            }
*/

//                if(time_water_checkbox.isChecked() && time_water_editText.getText().toString().isEmpty() &&
//                time_water_editText.getText().toString().equalsIgnoreCase("")) {
//                    //checks that checkbox is checked but editTExt is empty...
//                    time_water_editText.setError("Select");
//                    time_water_editText.setTextColor(Color.RED);
//                    focusView = time_water_editText;
//                    cancel = true;
//                    return;
//                }

/*
            if (water_availability_spinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) water_availability_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = water_availability_spinner;
                cancel = true;
                return;
            }
*/

            if (toilet_facility_spinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) toilet_facility_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = toilet_facility_spinner;
                cancel = true;
                return;
            }

            if (toiletfacility_edittext.getVisibility() == View.VISIBLE && toiletfacility_edittext.getText().toString().isEmpty() &&
                    toiletfacility_edittext.getText().toString().equalsIgnoreCase("") &&
                    toiletfacility_edittext.getText().toString() != null) {

                toiletfacility_edittext.setError(getString(R.string.select));
                focusView = toiletfacility_edittext;
                cancel = true;
                return;
            }

            if (structure_of_house_spinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) structure_of_house_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = structure_of_house_spinner;
                cancel = true;
                return;
            }

            if (checkIfEmpty(this, binding.unitsSpinner.getSelectedItem().toString())) {
                TextView t = (TextView) binding.unitsSpinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = binding.unitsSpinner;
                cancel = true;
                return;
            }

            if (landOwnedEditText.getVisibility() == View.VISIBLE) {
                if (!checkIfEmpty(this, binding.unitsSpinner.getSelectedItem().toString())) {
                    if (checkIfEmpty(this, binding.landOwnedEditText.getText().toString())) {
                        binding.landOwnedEditText.setError(getString(R.string.enter_number));
                        focusView = binding.landOwnedEditText;
                        cancel = true;
                        return;
                    }
                }
            }

            // Validations for Income Radio Group
            if (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.averageAnnualHouseholdIncomeRadioGroup;
                cancel = true;
                return;
            }

          /*  // Validations for Annual Health Expenditure Radio Group
            if (binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.annualHealthExpenditureRadioGroup;
                cancel = true;
                return;
            }
*/
           /* // Validations for Education Expenditure Radio Group
            if (binding.educationExpenditureRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.educationExpenditureRadioGroup;
                cancel = true;
                return;
            }
*/
            // Validations for Household Cooking Fuel Linear Layout
            if (checkIfCheckboxesEmpty(binding.householdCookingFuelCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.householdCookingFuelCheckboxLinearLayout;
                cancel = true;
                return;
            }

            // Validations for Household Cooking Fuel Linear Layout
            if (checkIfCheckboxesEmpty(binding.mainSourceOfLightingCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.mainSourceOfLightingCheckboxLinearLayout;
                cancel = true;
                return;
            }

          /*  // Validations for Open Defecation Radio Group
            if (binding.openDefecationRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.openDefecationRadioGroup;
                cancel = true;
                return;
            }
*/
            if (binding.llReasonForOpenDefecation.getVisibility() == View.VISIBLE) {
                if (checkIfCheckboxesEmpty(binding.reasonForOpenDefecationCheckboxLinearLayout)) {
                    Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                    focusView = binding.reasonForOpenDefecationCheckboxLinearLayout;
                    cancel = true;
                    return;
                }
            }

            if (checkIfCheckboxesEmpty(binding.handWashOccasionLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.handWashOccasionLinearLayout;
                cancel = true;
                return;
            }

/*
            if (checkIfCheckboxesEmpty(binding.foodCookedInTwentyFourHoursLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.householdCookingFuelCheckboxLinearLayout;
                cancel = true;
                return;
            }
*/

            // water source within 30 mints - start
            if (waterSourceWithin30minutesRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = waterSourceWithin30minutesRadioGroup;
                cancel = true;
                return;
            }
            // water source within 30 mints


            // Ekal process take up - New question added on 24th march 2023
            if (ekalProcessRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = ekalProcessRadioGroup;
                cancel = true;
                return;
            }
            // Ekal process - End

        }

        if (binding.hohRadioGroup.getCheckedRadioButtonId() != -1 && binding.hohRadioGroup.getCheckedRadioButtonId() == binding.hohNo.getId()) {
            if (binding.hohRelationshipSpinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.hohRelationshipSpinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = binding.hohRelationshipSpinner;
                cancel = true;
                return;
            }
        }

        if (medicalHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.medical_history_cannot_be_empty), Toast.LENGTH_SHORT).show();
            focusView = medicalHistoryViewPager;
            cancel = true;
            return;
        }

        if (smokingHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.smoking_history_cannot_be_empty), Toast.LENGTH_SHORT).show();
            focusView = smokingHistoryViewPager;
            cancel = true;
            return;
        }
        // tobacco valdiation
        if (tobaccoHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.tobacco_consumption_cannot_empty), Toast.LENGTH_SHORT).show();
            focusView = tobaccoHistoryViewPager;
            cancel = true;
            return;
        }

        if (alcoholConsumptionHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.alcohol_consumption_history_cannot_be_empty), Toast.LENGTH_SHORT).show();
            focusView = alcoholViewPager;
            cancel = true;
            return;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            patientdto.setFirstname(StringUtils.getValue(mFirstName.getText().toString()));
            patientdto.setMiddlename(StringUtils.getValue(mMiddleName.getText().toString()));
            patientdto.setLastname(StringUtils.getValue(mLastName.getText().toString()));
            patientdto.setPhonenumber(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientdto.setGender(StringUtils.getValue(mGender));

            // String dob = StringUtils.hi_or__en(mDOB.getText().toString(), month_index);
            String[] dob_array = mDOB.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) { //no value has been edited...ie. DOB/Age is not selected...
                String dob = StringUtils.hi_or_gu_as_en_noEdit
                        (mDOB.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or_gu_as_en_month(dob_indexValue);
                dob_array[1] = dob_array[1].replace(dob_array[1], dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];

                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob_value)));

            }

            // patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob_value)));

            patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));
            //patientdto.setCityvillage(StringUtils.getValue(mCity.getText().toString()));
            patientdto.setCityvillage(mVillage.getSelectedItem().toString());
            patientdto.setPostalcode(StringUtils.getValue(mPostal.getText().toString()));
            patientdto.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
            patientdto.setPatientPhoto(mCurrentPhotoPath);
//          patientdto.setEconomic(StringUtils.getValue(m));
            patientdto.setStateprovince(StringUtils.getValue(mState.getSelectedItem().toString()));

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            String casteVal = StringUtils.getProvided(mCaste);
            casteVal = StringUtils.second_filter(casteVal);
            patientAttributesDTO.setValue(casteVal);
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

//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
//            patientAttributesDTO.setValue(StringUtils.getValue(mOccupation.getText().toString()));
//            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ayushmanCardStatus"));
            String ayushVal = getRadioButtonStrings(
                    ((RadioButton) binding.ayushmanRadioGroup.findViewById
                            (binding.ayushmanRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    context,
                    updatedContext,
                    sessionManager.getAppLanguage()
            );
            ayushVal = StringUtils.second_filter(ayushVal);
            patientAttributesDTO.setValue(ayushVal);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("mgnregaCardStatus"));
            String mgnregaVal = getRadioButtonStrings(
                    ((RadioButton) binding.mgnregaRadioGroup.findViewById
                            (binding.mgnregaRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    context,
                    updatedContext,
                    sessionManager.getAppLanguage()
            );
            mgnregaVal = StringUtils.second_filter(mgnregaVal);
            patientAttributesDTO.setValue(mgnregaVal);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            patientAttributesDTO.setValue(getEducationStrings(StringUtils.getProvided
                    (mEducation), context, updatedContext, sessionManager.getAppLanguage()));
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
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdID"));
                patientAttributesDTO.setValue(HouseHold_UUID);
                patientAttributesDTOList.add(patientAttributesDTO);

            } else {

                String HouseHold_UUID = sessionManager.getHouseholdUuid();
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdID"));
                patientAttributesDTO.setValue(HouseHold_UUID);
                patientAttributesDTOList.add(patientAttributesDTO);

            }

            //Occupation ...
            if (occupation_edittext.getVisibility() == View.VISIBLE && !occupation_edittext.getText().toString().isEmpty() &&
                    !occupation_edittext.getText().toString().equalsIgnoreCase("")) {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
                patientAttributesDTO.setValue(StringUtils.getValue(occupation_edittext.getText().toString()));
                Log.d("HOH", "Occupation: " + occupation_edittext.getText().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
            } else {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
                //patientAttributesDTO.setValue(occupation_spinner.getSelectedItem().toString());
                patientAttributesDTO.setValue(getOccupationString(occupation_spinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                Log.d("HOH", "Occupation: " + occupation_spinner.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            //Bank Account...
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Bank Account"));
            //  patientAttributesDTO.setValue(bankaccount_spinner.getSelectedItem().toString());
            patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(bankaccount_spinner));
            Log.d("HOH", "Bankacc: " + bankaccount_spinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            //Mobile Phone own...
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Mobile Phone Type"));
            // patientAttributesDTO.setValue(mobilephone_spinner.getSelectedItem().toString());
//            patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(mobilephone_spinner));
            patientAttributesDTO.setValue(getMobilePhoneOwnership(mobilephone_spinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            Log.d("HOH", "mobile phone type: " + mobilephone_spinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            //WhatsApp Family member ...
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Use WhatsApp"));
            // patientAttributesDTO.setValue(whatsapp_spinner.getSelectedItem().toString());
            patientAttributesDTO.setValue(StringUtils.getWhatsAppStrings(whatsapp_spinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            Log.d("HOH", "Whatsapp use: " + whatsapp_spinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            if (framelayout_vaccine_question.getVisibility() == View.VISIBLE) {
                if (radioYes.isChecked() && framelayout_vaccination.getVisibility() == View.VISIBLE) {
                    //Vaccination ...
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Covid Vaccination"));
                    patientAttributesDTO.setValue(StringUtils.getVaccinationSpinnerHi_En(spinner_vaccination));
                    Log.d("HOH", "Vaccination: " + spinner_vaccination.getSelectedItem().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                } else {
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Covid Vaccination"));
                    patientAttributesDTO.setValue("No");
                    Log.d("HOH", "Vaccination: " + "No");
                    patientAttributesDTOList.add(patientAttributesDTO);
                }
            } else {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Covid Vaccination"));
                patientAttributesDTO.setValue("Age less than 18 years");
                Log.d("HOH", "Vaccination: " + spinner_vaccination.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            // Marital Status Adapter
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("martialStatus"));
            patientAttributesDTO.setValue(getMaritalStatusStrings
                    (maritalStatusSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Check if the ll18 linearlayout is visible or not
            if (ll18.getVisibility() == View.VISIBLE) {
                // BP Checked Adapter
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("bpChecked"));
                patientAttributesDTO.setValue(getTestStrings(bpSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // Sugar Level Checked
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("sugarChecked"));
                patientAttributesDTO.setValue(getTestStrings(sugarLevelSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            // HB Checked Adapter
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("hbChecked"));
            patientAttributesDTO.setValue(getTestStrings(hbLevelSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);

          /*  // BMI Checked Adapter
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("bmiChecked"));
            patientAttributesDTO.setValue(getTestStrings(bmiLevelSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);
*/
            if (hohRadioGroup.getCheckedRadioButtonId() == hohNo.getId()) {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("hohRelationship"));
                patientAttributesDTO.setValue(hohRelationship(hohRelationshipSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            //Check first if Are you Head of Household checkbox is checked or not...
            if (hohRadioGroup.getCheckedRadioButtonId() == hohYes.getId()) {

                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("hohRelationship"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);

                // move fields here - start
                // ration card
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("rationCardStatus"));
                String rationVal = getRadioButtonStrings(
                        ((RadioButton) binding.rationCardRadioGroup.findViewById(
                                binding.rationCardRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                rationVal = StringUtils.second_filter(rationVal);
                patientAttributesDTO.setValue(rationVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                // economic card
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
                patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicStatus));
                patientAttributesDTOList.add(patientAttributesDTO);
                // move fields here - end

                // religion
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("religion"));
                String religionVal_edit = StringUtils.getReligionStrings(binding.religionDropDown.getSelectedItem().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                religionVal_edit = StringUtils.second_filter(religionVal_edit); // second filtering. AEAT-298
                patientAttributesDTO.setValue(religionVal_edit);
                patientAttributesDTOList.add(patientAttributesDTO);

                //Total no of members in household  ...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Total Family Members"));
                patientAttributesDTO.setValue(StringUtils.getValue(no_of_member_edittext.getText().toString()));
                Log.d("HOH", "total family meme: " + no_of_member_edittext.getText().toString());
                patientAttributesDTOList.add(patientAttributesDTO);

                //Total no of members living in household  ...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("Total Family Members Staying"));
                patientAttributesDTO.setValue(StringUtils.getValue(no_of_staying_members_edittext.getText().toString()));
                Log.d("HOH", "Total family stay: " + no_of_staying_members_edittext.getText().toString());
                patientAttributesDTOList.add(patientAttributesDTO);

                // numberOfSmartphones
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfSmartphones"));
                patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.numberOfSmartphonesEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // numberOfFeaturePhones
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfFeaturePhones"));
                patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.numberOfFeaturePhonesEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // numberOfEarningMembers
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfEarningMembers"));
                patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.noOfEarningMembersEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // electricityStatus
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("electricityStatus"));
                String electriValue = StringUtils.getSurveyStrings(
                        ((RadioButton) binding.householdElectricityRadioGroup.
                                findViewById(binding.householdElectricityRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                electriValue = StringUtils.second_filter(electriValue);
                patientAttributesDTO.setValue(electriValue);
                patientAttributesDTOList.add(patientAttributesDTO);


                if (binding.householdElectricityYes.isChecked()) {

                    // loadSheddingHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingHoursPerDay"));
                    String[] loadSheddingHoursArray = binding.loadSheddingHoursEditText.getText().toString().split(" ");
                    String loadSheddingHours = loadSheddingHoursArray[0] + " " + getTimeStrings(loadSheddingHoursArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    loadSheddingHours = StringUtils.second_filter(loadSheddingHours);
                    patientAttributesDTO.setValue(loadSheddingHours);
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // loadSheddingDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingDaysPerWeek"));
                    String[] loadSheddingDaysArray = binding.loadSheddingDaysEditText.getText().toString().split(" ");
                    String loadSheddingDays = loadSheddingDaysArray[0] + " " + getTimeStrings(loadSheddingDaysArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    loadSheddingDays = StringUtils.second_filter(loadSheddingDays);
                    patientAttributesDTO.setValue(loadSheddingDays);
                    patientAttributesDTOList.add(patientAttributesDTO);

                } else {

                    // loadSheddingHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingHoursPerDay"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // loadSheddingDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingDaysPerWeek"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                }

                // runningWaterAvailability
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("runningWaterAvailability"));
                String runningWaterVal = StringUtils.getSurveyStrings(
                        ((RadioButton) binding.householdRunningWaterRadioGroup.findViewById
                                (binding.householdRunningWaterRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                runningWaterVal = StringUtils.second_filter(runningWaterVal);
                patientAttributesDTO.setValue(runningWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                if (binding.waterSupplyYes.isChecked()) {

                    // waterSupplyAvailabilityHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHoursPerDay"));
                    String[] runningWaterHoursArray = binding.runningWaterHoursEditText.getText().toString().split(" ");
                    String runningWaterHours = runningWaterHoursArray[0] + " " + getTimeStrings(runningWaterHoursArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    patientAttributesDTO.setValue(runningWaterHours);
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // waterSupplyAvailabilityDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysPerWeek"));
                    String[] runningWaterDaysArray = binding.runningWaterDaysEditText.getText().toString().split(" ");
                    String runningWaterDays = runningWaterDaysArray[0] + " " + getTimeStrings(runningWaterDaysArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    patientAttributesDTO.setValue(runningWaterDays);
                    patientAttributesDTOList.add(patientAttributesDTO);

                } else {

                    // waterSupplyAvailabilityHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHoursPerDay"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // waterSupplyAvailabilityDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysPerWeek"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                }

                //Main source of drinking water...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Drinking Water Source"));
                // patientAttributesDTO.setValue(source_of_water_spinner.getSelectedItem().toString());
                String drinkWaterVal = getSelectedCheckboxes(
                        binding.mainSourceOfDrinkingWaterCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                );
                drinkWaterVal = StringUtils.second_filter(drinkWaterVal);
                patientAttributesDTO.setValue(drinkWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                //How do you make Drinking water Safe?
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("Safe Drinking Water"));
                // patientAttributesDTO.setValue(howtomake_water_safe_spinner.getSelectedItem().toString());
                String safeWaterVal = getSelectedCheckboxes(
                        binding.householdMakeSafeWaterCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                );
                safeWaterVal = StringUtils.second_filter(safeWaterVal);
                patientAttributesDTO.setValue(safeWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Time taken to get water - Updated - Start
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Time Drinking Water Source"));
                String timeWaterVal = StringUtils.getSurveyStrings(
                        ((RadioButton) binding.waterSource30minutesRadioGroup.
                                findViewById(binding.waterSource30minutesRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                timeWaterVal = StringUtils.second_filter(timeWaterVal);
                patientAttributesDTO.setValue(timeWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);
                // Time taken to get water - Updated - End

              /*  // Time taken to get water...
                if (time_water_checkbox.isChecked()) {
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Time Drinking Water Source"));
                    // patientAttributesDTO.setValue(StringUtils.getValue(time_water_checkbox.getText().toString()));
                    patientAttributesDTO.setValue(StringUtils.getCheckbox_Hi_En_Or_Gu_As(time_water_checkbox.getText().toString())); //hi to en and vice-versa...
                    Log.d("HOH", "time to bring water:create " + time_water_checkbox.getText().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                } else {
                    //User enters value here...
                    String water_time = time_water_editText.getText().toString().replaceAll(getString(R.string.identification_screen_picker_hours), "hours")
                            .replaceAll(getString(R.string.identification_screen_picker_minute), "minute");
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Time Drinking Water Source"));
                    patientAttributesDTO.setValue(StringUtils.getValue(water_time));
                    Log.d("HOH", "time to bring water value entered: " + time_water_editText.getText().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                }*/

                //            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
//                    .getUuidForAttribute("Time Drinking Water Source"));
//            patientAttributesDTO.setValue(howtomake_water_safe_spinner.getSelectedItem().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);

               /* //Drinking water availability...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("Drinking Water Availability"));
                // patientAttributesDTO.setValue(water_availability_spinner.getSelectedItem().toString());
                patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(water_availability_spinner));
                Log.d("HOH", "Water availability: " + water_availability_spinner.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
*/
                //Toilet Facility...
                if (toiletfacility_edittext.getVisibility() == View.VISIBLE && !toiletfacility_edittext
                        .getText().toString().isEmpty() && !toiletfacility_edittext.getText()
                        .toString().equalsIgnoreCase("")) {

                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Toilet Facility"));
                    patientAttributesDTO.setValue(toiletfacility_edittext.getText().toString());
                    Log.d("HOH", "Toilet: " + toiletfacility_edittext.getText().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                } else {
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Toilet Facility"));
                    //  patientAttributesDTO.setValue(toilet_facility_spinner.getSelectedItem().toString());
                    String toiletVal = StringUtils.getSpinnerHi_En(toilet_facility_spinner);
                    toiletVal = StringUtils.second_filter(toiletVal);
                    patientAttributesDTO.setValue(toiletVal);
                    Log.d("HOH", "Toilet: " + toilet_facility_spinner.getSelectedItem().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                }

                //House Structure...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("House Structure"));
                //  patientAttributesDTO.setValue(structure_of_house_spinner.getSelectedItem().toString());
                String hhVal = StringUtils.getSpinnerHi_En(structure_of_house_spinner);
                hhVal = StringUtils.second_filter(hhVal);
                patientAttributesDTO.setValue(hhVal);
                Log.d("HOH", "Structure: " + structure_of_house_spinner.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);

                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Family Cultivable Land"));
                String cultivableLand = binding.landOwnedEditText.getText().toString() + " " + getLandOwnedStrings(
                        binding.unitsSpinner.getSelectedItem().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage());

                cultivableLand = StringUtils.second_filter(cultivableLand);
                patientAttributesDTO.setValue(cultivableLand);
                patientAttributesDTOList.add(patientAttributesDTO);

                // averageAnnualHouseholdIncome
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageAnnualHouseholdIncome"));
                String incomeVal = StringUtils.getRadioButtonStrings(
                        ((RadioButton) binding.averageAnnualHouseholdIncomeRadioGroup.findViewById
                                (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                incomeVal = StringUtils.second_filter(incomeVal);
                patientAttributesDTO.setValue(incomeVal);
                patientAttributesDTOList.add(patientAttributesDTO);

              /*  // averageExpenditureOnHealth
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnHealth"));
                patientAttributesDTO.setValue(StringUtils.getRadioButtonStrings(
                        ((RadioButton) binding.annualHealthExpenditureRadioGroup.findViewById(binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
            /*    // averageExpenditureOnEducation
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnEducation"));
                patientAttributesDTO.setValue(StringUtils.getRadioButtonStrings(
                        ((RadioButton) binding.educationExpenditureRadioGroup.findViewById(binding.educationExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
                // cookingFuel
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("cookingFuel"));
                String cookingVal = getSelectedCheckboxes(
                        binding.householdCookingFuelCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                );
                cookingVal = StringUtils.second_filter(cookingVal);
                patientAttributesDTO.setValue(cookingVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                // householdLighting
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdLighting"));
                String lightVal = getSelectedCheckboxes(
                        binding.mainSourceOfLightingCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                ).replace("\\", "");
                lightVal = StringUtils.second_filter(lightVal);
                patientAttributesDTO.setValue(lightVal); // replace since Lantern/Kersone/Lamp gets converted as Lantern\/Kersonene\/Lamp since they are special chars.
                patientAttributesDTOList.add(patientAttributesDTO);

             /*   // defecatedInOpen
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("defecatedInOpen"));
                patientAttributesDTO.setValue(getSurveyStrings(
                        ((RadioButton) binding.openDefecationRadioGroup.findViewById(binding.openDefecationRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
                if (binding.llReasonForOpenDefecation.getVisibility() == View.VISIBLE) {
                    // reasonForOpenDefecation
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("reasonForOpenDefecation"));
                    patientAttributesDTO.setValue(getSelectedCheckboxes(
                            binding.reasonForOpenDefecationCheckboxLinearLayout,
                            context,
                            updatedContext,
                            sessionManager.getAppLanguage(),
                            "-"
                    ));
                    patientAttributesDTOList.add(patientAttributesDTO);
                }

                // soapHandWashingOccasion
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("soapHandWashingOccasion"));
                patientAttributesDTO.setValue(getSelectedCheckboxes(
                        binding.handWashOccasionLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                ));
                patientAttributesDTOList.add(patientAttributesDTO);

                // ekal process
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("TakeOurService"));
                patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                        ((RadioButton) binding.ekalProcessRadioGroup.
                                findViewById(binding.ekalProcessRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);

              /*  // foodItemsPreparedInTwentyFourHours
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("foodItemsPreparedInTwentyFourHours"));
                patientAttributesDTO.setValue(getSelectedCheckboxes(
                        binding.foodCookedInTwentyFourHoursLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
                // sessionManager.setHOH_checkbox(true);
                // Log.d("session", "session_create: " + sessionManager.getHOH_checkbox());

            } else {
                // sessionManager.setHOH_checkbox(false);
                //  Log.d("session", "session_create: " + sessionManager.getHOH_checkbox());
            }
            //end of checking if the family head checkbox is checked or not...

            //            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
//                    .getUuidForAttribute("Family Cultivable Land"));
//            patientAttributesDTO.setValue(howtomake_water_safe_spinner.getSelectedItem().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);

            // Medical History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("otherMedicalHistory"));
            patientAttributesDTO.setValue(new Gson().toJson(medicalHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Smoking History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("smokingStatus"));
            patientAttributesDTO.setValue(new Gson().toJson(smokingHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Tobacco History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("TobaccoStatus"));
            patientAttributesDTO.setValue(new Gson().toJson(tobaccoHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Alcohol Consumption History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("alcoholConsumptionStatus"));
            patientAttributesDTO.setValue(new Gson().toJson(alcoholConsumptionHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            //patientAttributesDTOList.add(patientAttributesDTO);
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

    private void phoneNoCombinationsCheck() {

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

        if (mPhoneNum.getText().toString().trim().length() > 0) {
            if (mPhoneNum.getText().toString().trim().length() < 10) {
                mPhoneNum.requestFocus();
                mPhoneNum.setError(getString(R.string.enter_10_digits));
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

        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
                && !mPhoneNum.getText().toString().equals("") && !villageText.getText().toString().equalsIgnoreCase("")/*!mCity.getText().toString().equals("")*/ && !countryText.getText().toString().equals("") &&
                !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") &&
                !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked() || mGenderO.isChecked())) {

            Log.v(TAG, "Result");

        } else {
            if (mFirstName.getText().toString().equals("")) {
                mFirstName.setError(getString(R.string.error_field_required));
            }

            if (mLastName.getText().toString().equals("")) {
                mLastName.setError(getString(R.string.error_field_required));
            }

            if (mDOB.getText().toString().equals("")) {
                mDOB.setError(getString(R.string.error_field_required));
            }

            if (mAge.getText().toString().equals("")) {
                mAge.setError(getString(R.string.error_field_required));
            }

            if (mPhoneNum.getText().toString().equals("")) {
                mPhoneNum.setError(getString(R.string.error_field_required));
            }

            /*if (mCity.getText().toString().equals("")) {
                mCity.setError(getString(R.string.error_field_required));
            }*/

            if (!mGenderF.isChecked() && !mGenderM.isChecked() && !mGenderO.isChecked()) {
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

        if (mVillage.getSelectedItemPosition() == 0) {
            villageText.setError(getString(R.string.error_field_required));
            focusView = villageText;
            cancel = true;
            return;
        } else {
            villageText.setError(null);
        }

        if (binding.ayushmanRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
            focusView = binding.ayushmanRadioGroup;
            cancel = true;
            return;
        }

        if (binding.mgnregaRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
            focusView = binding.mgnregaRadioGroup;
            cancel = true;
            return;
        }


        // TODO: Add validations for all Spinners here...
        if (occupation_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) occupation_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = occupation_spinner;
            cancel = true;
            return;
        }

        if (occupation_edittext.getVisibility() == View.VISIBLE && occupation_edittext.getText().toString().isEmpty() &&
                occupation_edittext.getText().toString().equalsIgnoreCase("") &&
                occupation_edittext.getText().toString() != null) {

            occupation_edittext.setError(getString(R.string.select));
            focusView = occupation_edittext;
            cancel = true;
            return;
        }


        if (bankaccount_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) bankaccount_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = bankaccount_spinner;
            cancel = true;
            return;
        }

        if (mobilephone_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) mobilephone_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = mobilephone_spinner;
            cancel = true;
            return;
        }

        if (whatsapp_spinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) whatsapp_spinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = whatsapp_spinner;
            cancel = true;
            return;
        }

        //vaccination
        if (framelayout_vaccine_question.getVisibility() == View.VISIBLE) {
            if (!radioYes.isChecked() && !radioNo.isChecked()) {
                MaterialAlertDialogBuilder alertDialogBuilder = new
                        MaterialAlertDialogBuilder(IdentificationActivity.this);
                alertDialogBuilder.setTitle(R.string.error);
                alertDialogBuilder.setMessage(R.string.select_option_vaccination_dialog);
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

            if (radioYes.isChecked() && spinner_vaccination.getSelectedItemPosition() == 0) {
                TextView t = (TextView) spinner_vaccination.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = spinner_vaccination;
                cancel = true;
                return;
            }
        }
        //vaccination - end...

        // Marital Status Validation
        if (maritalStatusSpinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) maritalStatusSpinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = maritalStatusSpinner;
            cancel = true;
            return;
        }

        if (ll18.getVisibility() == View.VISIBLE) {
            // BP Level Validation
            if (bpSpinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) bpSpinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = bpSpinner;
                cancel = true;
                return;
            }

            // Sugar Level Validation
            if (sugarLevelSpinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) sugarLevelSpinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = sugarLevelSpinner;
                cancel = true;
                return;
            }
        }

        // HB Level Validation
        if (hbLevelSpinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) hbLevelSpinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = hbLevelSpinner;
            cancel = true;
            return;
        }

       /* // BMI Validation
        if (bmiLevelSpinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) bmiLevelSpinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = bmiLevelSpinner;
            cancel = true;
            return;
        }
*/
        if (hohRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.please_state_if_you_are_the_head_of_the_family), Toast.LENGTH_SHORT).show();
            focusView = hohRadioGroup;
            cancel = true;
            return;
        }

        if (hohRadioGroup.getCheckedRadioButtonId() != -1 && binding.hohRadioGroup.getCheckedRadioButtonId() == binding.hohYes.getId()) {

            // move fields here - start
            // Ration card
            if (binding.rationCardRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.rationCardRadioGroup;
                cancel = true;
                return;
            }
            // move fields here - end

            if (checkIfEmpty(this, binding.religionDropDown.getSelectedItem().toString())) {
                TextView t = (TextView) binding.religionDropDown.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = binding.religionDropDown;
                cancel = true;
                return;
            }

            if (no_of_member_edittext.getText().toString().equalsIgnoreCase("") &&
                    no_of_member_edittext.getText().toString().isEmpty()) {
                no_of_member_edittext.setError(getString(R.string.select));
                // no_of_member_edittext.setTextColor(Color.RED);
                focusView = no_of_member_edittext;
                cancel = true;
                return;
            }

            if (no_of_staying_members_edittext.getText().toString().equalsIgnoreCase("") &&
                    no_of_staying_members_edittext.getText().toString().isEmpty()) {
                no_of_staying_members_edittext.setError(getString(R.string.select));
                // no_of_staying_members_edittext.setTextColor(Color.RED);
                focusView = no_of_staying_members_edittext;
                cancel = true;
                return;
            }

            if (Integer.parseInt(no_of_staying_members_edittext.getText().toString()) > Integer.parseInt(no_of_member_edittext.getText().toString())) {
                no_of_staying_members_edittext.setError(getString(R.string.no_of_members_living_cannot_be_greater_than_total_number_of_members));
                focusView = no_of_staying_members_edittext;
                cancel = true;
                return;
            }

            // Validation for number of smartphones field
            if (checkIfEmpty(context, Objects.requireNonNull(binding.numberOfSmartphonesEditText.getText()).toString())) {
                binding.numberOfSmartphonesEditText.setError(getString(R.string.enter_number));
                focusView = binding.numberOfSmartphonesEditText;
                cancel = true;
                return;
            }

            // Validation for number of feature phones field
            if (checkIfEmpty(context, Objects.requireNonNull(binding.numberOfFeaturePhonesEditText.getText()).toString())) {
                binding.numberOfFeaturePhonesEditText.setError(getString(R.string.enter_number));
                focusView = binding.numberOfFeaturePhonesEditText;
                cancel = true;
                return;
            }

            // Validation for number of earning members field
            if (checkIfEmpty(context, Objects.requireNonNull(binding.noOfEarningMembersEditText.getText()).toString())) {
                binding.noOfEarningMembersEditText.setError(getString(R.string.enter_number));
                focusView = binding.noOfEarningMembersEditText;
                cancel = true;
                return;
            }

            // Validations for Electricity Status Radio Group
            if (binding.householdElectricityRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_state_if_you_have_electricity_in_the_household), Toast.LENGTH_SHORT).show();
                focusView = binding.householdRunningWaterRadioGroup;
                cancel = true;
                return;
            }

            // Validations for Load Shedding Hours Edit Text
            if (binding.householdElectricityYes.isChecked() && checkIfEmpty(context, Objects.requireNonNull(binding.loadSheddingHoursEditText.getText()).toString())) {
                binding.loadSheddingHoursEditText.setError(getString(R.string.enter_number));
                focusView = binding.loadSheddingHoursEditText;
                cancel = true;
                return;
            }

            // Validations for Load Shedding Days Edit Text
            if (binding.householdElectricityYes.isChecked() && checkIfEmpty(context, Objects.requireNonNull(binding.loadSheddingDaysEditText.getText()).toString())) {
                binding.loadSheddingDaysEditText.setError(getString(R.string.enter_number));
                focusView = binding.loadSheddingDaysEditText;
                cancel = true;
                return;
            }

            // Validations for Running Water Radio Group
            if (binding.householdRunningWaterRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_state_if_you_have_running_water_in_the_household), Toast.LENGTH_SHORT).show();
                focusView = binding.householdRunningWaterRadioGroup;
                cancel = true;
                return;
            }

            // Validations for Running Water Hours Edit Text
            if (binding.waterSupplyYes.isChecked() && checkIfEmpty(context, Objects.requireNonNull(binding.runningWaterHoursEditText.getText()).toString())) {
                binding.runningWaterHoursEditText.setError(getString(R.string.enter_number));
                focusView = binding.runningWaterHoursEditText;
                cancel = true;
                return;
            }

            // Validations for Running Water Days Edit Text
            if (binding.waterSupplyYes.isChecked() && checkIfEmpty(context, Objects.requireNonNull(binding.runningWaterDaysEditText.getText()).toString())) {
                binding.runningWaterDaysEditText.setError(getString(R.string.enter_number));
                focusView = binding.runningWaterDaysEditText;
                cancel = true;
                return;
            }

            // Validations for Main Source of Drinking Water Linear Layout
            if (checkIfCheckboxesEmpty(binding.mainSourceOfDrinkingWaterCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_select_the_main_source_of_drinking_water), Toast.LENGTH_SHORT).show();
                focusView = binding.mainSourceOfDrinkingWaterCheckboxLinearLayout;
                cancel = true;
                return;
            }

            if (checkIfCheckboxesEmpty(binding.householdMakeSafeWaterCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_select_the_methods_of_purifying_water), Toast.LENGTH_SHORT).show();
                focusView = binding.householdMakeSafeWaterCheckboxLinearLayout;
                cancel = true;
                return;
            }

/*
            if (!time_water_checkbox.isChecked() && time_water_editText.getText().toString().isEmpty() &&
                    time_water_editText.getText().toString().equalsIgnoreCase("")) {
                //checks if both the fields are not selected...
                time_water_checkbox.setError(getString(R.string.select));

                focusView = time_water_checkbox;
                focusView = time_water_editText;
                cancel = true;
                return;
            }
*/

//            if(time_water_checkbox.isChecked() && time_water_editText.getText().toString().isEmpty() &&
//                    time_water_editText.getText().toString().equalsIgnoreCase("")) {
//                //checks that checkbox is checked but editTExt is empty...
//                time_water_editText.setError("Select");
//                time_water_editText.setTextColor(Color.RED);
//                focusView = time_water_editText;
//                cancel = true;
//                return;
//            }

/*
            if (water_availability_spinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) water_availability_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = water_availability_spinner;
                cancel = true;
                return;
            }
*/

            if (toilet_facility_spinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) toilet_facility_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = toilet_facility_spinner;
                cancel = true;
                return;
            }

            if (toiletfacility_edittext.getVisibility() == View.VISIBLE && toiletfacility_edittext.getText().toString().isEmpty() &&
                    toiletfacility_edittext.getText().toString().equalsIgnoreCase("") &&
                    toiletfacility_edittext.getText().toString() != null) {

                toiletfacility_edittext.setError(getString(R.string.select));
                focusView = toiletfacility_edittext;
                cancel = true;
                return;
            }

            if (structure_of_house_spinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) structure_of_house_spinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = structure_of_house_spinner;
                cancel = true;
                return;
            }

            if (checkIfEmpty(this, binding.unitsSpinner.getSelectedItem().toString())) {
                TextView t = (TextView) binding.unitsSpinner.getSelectedView();
                t.setError(getString(R.string.select));
                t.setTextColor(Color.RED);
                focusView = binding.unitsSpinner;
                cancel = true;
                return;
            }

            if (landOwnedEditText.getVisibility() == View.VISIBLE) {
                if (!checkIfEmpty(this, binding.unitsSpinner.getSelectedItem().toString())) {
                    if (checkIfEmpty(this, binding.landOwnedEditText.getText().toString())) {
                        binding.landOwnedEditText.setError(getString(R.string.enter_number));
                        focusView = binding.landOwnedEditText;
                        cancel = true;
                        return;
                    }
                }
            }

            // Validations for Income Radio Group
            if (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.averageAnnualHouseholdIncomeRadioGroup;
                cancel = true;
                return;
            }

          /*  // Validations for Annual Health Expenditure Radio Group
            if (binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.annualHealthExpenditureRadioGroup;
                cancel = true;
                return;
            }
*/
          /*  // Validations for Education Expenditure Radio Group
            if (binding.educationExpenditureRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.educationExpenditureRadioGroup;
                cancel = true;
                return;
            }
*/
            // Validations for Household Cooking Fuel Linear Layout
            if (checkIfCheckboxesEmpty(binding.householdCookingFuelCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.householdCookingFuelCheckboxLinearLayout;
                cancel = true;
                return;
            }

            // Validations for Main Source of Lighting Fuel Linear Layout
            if (checkIfCheckboxesEmpty(binding.mainSourceOfLightingCheckboxLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.mainSourceOfLightingCheckboxLinearLayout;
                cancel = true;
                return;
            }

          /*  // Validations for Open Defecation Radio Group
            if (binding.openDefecationRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.openDefecationRadioGroup;
                cancel = true;
                return;
            }
*/
            if (binding.llReasonForOpenDefecation.getVisibility() == View.VISIBLE) {
                if (checkIfCheckboxesEmpty(binding.reasonForOpenDefecationCheckboxLinearLayout)) {
                    Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                    focusView = binding.reasonForOpenDefecationCheckboxLinearLayout;
                    cancel = true;
                    return;
                }
            }

            if (checkIfCheckboxesEmpty(binding.handWashOccasionLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.handWashOccasionLinearLayout;
                cancel = true;
                return;
            }

/*
            if (checkIfCheckboxesEmpty(binding.foodCookedInTwentyFourHoursLinearLayout)) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = binding.householdCookingFuelCheckboxLinearLayout;
                cancel = true;
                return;
            }
*/

            // water source within 30 mints - start
            if (waterSourceWithin30minutesRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = waterSourceWithin30minutesRadioGroup;
                cancel = true;
                return;
            }
            // water source within 30 mints

             // Ekal process take up - New question added on 24th march 2023
            if (ekalProcessRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, getString(R.string.please_fill_up_all_required_fields), Toast.LENGTH_SHORT).show();
                focusView = ekalProcessRadioGroup;
                cancel = true;
                return;
            }
            // Ekal process - End

        }

        if (binding.hohRadioGroup.getCheckedRadioButtonId() != -1 && binding.hohRadioGroup.getCheckedRadioButtonId() == binding.hohNo.getId()) {
            if (binding.hohRelationshipSpinner.getSelectedItemPosition() == 0) {
                TextView t = (TextView) binding.hohRelationshipSpinner.getSelectedView();
                try {   // AEAT-534 -> Point 6.
                    t.setError(getString(R.string.select));
                    t.setTextColor(Color.RED);
                }
                catch (Exception e) {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                focusView = binding.hohRelationshipSpinner;
                cancel = true;
                return;
            }
        }

        if (medicalHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.medical_history_cannot_be_empty), Toast.LENGTH_SHORT).show();
            focusView = medicalHistoryViewPager;
            cancel = true;
            return;
        }

        if (smokingHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.smoking_history_cannot_be_empty), Toast.LENGTH_SHORT).show();
            focusView = smokingHistoryViewPager;
            cancel = true;
            return;
        }

        // tobacco valdiation
        if (tobaccoHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.tobacco_consumption_cannot_empty), Toast.LENGTH_SHORT).show();
            focusView = tobaccoHistoryViewPager;
            cancel = true;
            return;
        }


        if (alcoholConsumptionHistoryList.isEmpty()) {
            Toast.makeText(this, getString(R.string.alcohol_consumption_history_cannot_be_empty), Toast.LENGTH_SHORT).show();
            focusView = alcoholViewPager;
            cancel = true;
            return;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (mCurrentPhotoPath == null)
                mCurrentPhotoPath = patientdto.getPatient_photo();

            patientdto.setFirst_name(StringUtils.getValue(mFirstName.getText().toString()));
            patientdto.setMiddle_name(StringUtils.getValue(mMiddleName.getText().toString()));
            patientdto.setLast_name(StringUtils.getValue(mLastName.getText().toString()));
            patientdto.setPhone_number(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientdto.setGender(StringUtils.getValue(mGender));

            //String dob = StringUtils.hi_or__en(mDOB.getText().toString());
            String[] dob_array = mDOB.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or_gu_as_en_noEdit
                        (mDOB.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or_gu_as_en_month(dob_indexValue);
                String dob_month_split = dob_array[1];
                dob_array[1] = dob_month_split.replace(dob_month_split, dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];

                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob_value)));
            }

            //  patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(dob_value)));
            patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));
            //patientdto.setCity_village(StringUtils.getValue(mCity.getText().toString()));
            patientdto.setCity_village(mVillage.getSelectedItem().toString());
            patientdto.setPostal_code(StringUtils.getValue(mPostal.getText().toString()));
            patientdto.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
            patientdto.setPatient_photo(mCurrentPhotoPath);
//                patientdto.setEconomic(StringUtils.getValue(m));
//            patientdto.setState_province(StringUtils.getValue(patientdto.getState_province()));
//
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            String casteVal_edit = StringUtils.getProvided(mCaste);
            casteVal_edit = StringUtils.second_filter(casteVal_edit);
            patientAttributesDTO.setValue(casteVal_edit);
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

//            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
//            patientAttributesDTO.setValue(StringUtils.getValue(mOccupation.getText().toString()));
//            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("ayushmanCardStatus"));
            String ayushVal = getRadioButtonStrings(
                    ((RadioButton) binding.ayushmanRadioGroup.findViewById
                            (binding.ayushmanRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    context,
                    updatedContext,
                    sessionManager.getAppLanguage()
            );
            ayushVal = StringUtils.second_filter(ayushVal);
            patientAttributesDTO.setValue(ayushVal);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("mgnregaCardStatus"));
            String mgnregaVal = getRadioButtonStrings(
                    ((RadioButton) binding.mgnregaRadioGroup.findViewById
                            (binding.mgnregaRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                    context,
                    updatedContext,
                    sessionManager.getAppLanguage()
            );
            mgnregaVal = StringUtils.second_filter(mgnregaVal);
            patientAttributesDTO.setValue(mgnregaVal);
            patientAttributesDTOList.add(patientAttributesDTO);

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Education Level"));
            patientAttributesDTO.setValue(getEducationStrings(StringUtils.getProvided
                    (mEducation), context, updatedContext, sessionManager.getAppLanguage()));
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
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdID"));
                patientAttributesDTO.setValue(HouseHold_UUID);
                patientAttributesDTOList.add(patientAttributesDTO);

            } else {

                String HouseHold_UUID = sessionManager.getHouseholdUuid();
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdID"));
                patientAttributesDTO.setValue(HouseHold_UUID);
                patientAttributesDTOList.add(patientAttributesDTO);

            }
//          patientAttributesDTOList.add(patientAttributesDTO);
            //Occupation ...
            if (occupation_edittext.getVisibility() == View.VISIBLE && !occupation_edittext.getText().toString().isEmpty() &&
                    !occupation_edittext.getText().toString().equalsIgnoreCase("")) {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
                patientAttributesDTO.setValue(StringUtils.getValue(occupation_edittext.getText().toString()));
                Log.d("HOH", "Occupation: " + occupation_edittext.getText().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
            } else {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
                //patientAttributesDTO.setValue(occupation_spinner.getSelectedItem().toString());
                patientAttributesDTO.setValue(getOccupationString(occupation_spinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                Log.d("HOH", "Occupation: " + occupation_spinner.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            //Bank Account...
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Bank Account"));
            //  patientAttributesDTO.setValue(bankaccount_spinner.getSelectedItem().toString());
            patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(bankaccount_spinner));
            Log.d("HOH", "Bankacc: " + bankaccount_spinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            //Mobile Phone own...
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Mobile Phone Type"));
            // patientAttributesDTO.setValue(mobilephone_spinner.getSelectedItem().toString());
//            patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(mobilephone_spinner));
            patientAttributesDTO.setValue(getMobilePhoneOwnership(mobilephone_spinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            Log.d("HOH", "mobile phone type: " + mobilephone_spinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            //WhatsApp Family member ...
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Use WhatsApp"));
            // patientAttributesDTO.setValue(whatsapp_spinner.getSelectedItem().toString());
            patientAttributesDTO.setValue(StringUtils.getWhatsAppStrings(whatsapp_spinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            Log.d("HOH", "Whatsapp use: " + whatsapp_spinner.getSelectedItem().toString());
            patientAttributesDTOList.add(patientAttributesDTO);

            if (framelayout_vaccine_question.getVisibility() == View.VISIBLE) {
                if (radioYes.isChecked() && framelayout_vaccination.getVisibility() == View.VISIBLE) {
                    //Vaccination ...
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Covid Vaccination"));
                    patientAttributesDTO.setValue(StringUtils.getVaccinationSpinnerHi_En(spinner_vaccination));
                    Log.d("HOH", "Vaccination: " + spinner_vaccination.getSelectedItem().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                } else {
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Covid Vaccination"));
                    patientAttributesDTO.setValue("No");
                    Log.d("HOH", "Vaccination: " + "No");
                    patientAttributesDTOList.add(patientAttributesDTO);
                }
            } else {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Covid Vaccination"));
                patientAttributesDTO.setValue("Age less than 18 years");
                Log.d("HOH", "Vaccination: " + spinner_vaccination.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            // Marital Status Adapter
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("martialStatus"));
            patientAttributesDTO.setValue(getMaritalStatusStrings
                    (maritalStatusSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Check if the ll18 linearlayout is visible or not
            if (ll18.getVisibility() == View.VISIBLE) {
                // BP Checked Adapter
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("bpChecked"));
                patientAttributesDTO.setValue(getTestStrings(bpSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // Sugar Level Checked
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("sugarChecked"));
                patientAttributesDTO.setValue(getTestStrings(sugarLevelSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            // HB Checked Adapter
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("hbChecked"));
            patientAttributesDTO.setValue(getTestStrings(hbLevelSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);

           /* // BMI Checked Adapter
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("bmiChecked"));
            patientAttributesDTO.setValue(getTestStrings(bmiLevelSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
            patientAttributesDTOList.add(patientAttributesDTO);
*/
            if (hohRadioGroup.getCheckedRadioButtonId() == hohNo.getId()) {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("hohRelationship"));
                patientAttributesDTO.setValue(hohRelationship(hohRelationshipSpinner.getSelectedItem().toString(), context, updatedContext, sessionManager.getAppLanguage()));
                patientAttributesDTOList.add(patientAttributesDTO);
            }

            //Check first if Are you Head of Household checkbox is checked or not...
            if (hohRadioGroup.getCheckedRadioButtonId() == hohYes.getId()) {
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("hohRelationship"));
                patientAttributesDTO.setValue("-");
                patientAttributesDTOList.add(patientAttributesDTO);

                // move fields here - start
                // ration card
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("rationCardStatus"));
                String rationVal = getRadioButtonStrings(
                        ((RadioButton) binding.rationCardRadioGroup.findViewById(
                                binding.rationCardRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                rationVal = StringUtils.second_filter(rationVal);
                patientAttributesDTO.setValue(rationVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                // economic status
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Economic Status"));
                patientAttributesDTO.setValue(StringUtils.getProvided(mEconomicStatus));
                patientAttributesDTOList.add(patientAttributesDTO);
                // move fields here - end

                // religion
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("religion"));
                String religionVal = StringUtils.getReligionStrings(binding.religionDropDown.getSelectedItem().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                religionVal = StringUtils.second_filter(religionVal); // second filtering. AEAT-298
                patientAttributesDTO.setValue(religionVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                //Total no of members in household  ...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Total Family Members"));
                patientAttributesDTO.setValue(StringUtils.getValue(no_of_member_edittext.getText().toString()));
                Log.d("HOH", "total family meme: " + no_of_member_edittext.getText().toString());
                patientAttributesDTOList.add(patientAttributesDTO);

                //Total no of members living in household  ...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("Total Family Members Staying"));
                patientAttributesDTO.setValue(StringUtils.getValue(no_of_staying_members_edittext.getText().toString()));
                Log.d("HOH", "Total family stay: " + no_of_staying_members_edittext.getText().toString());
                patientAttributesDTOList.add(patientAttributesDTO);

                // numberOfSmartphones
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfSmartphones"));
                patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.numberOfSmartphonesEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // numberOfFeaturePhones
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfFeaturePhones"));
                patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.numberOfFeaturePhonesEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // numberOfEarningMembers
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("numberOfEarningMembers"));
                patientAttributesDTO.setValue(StringUtils.getSurveyValue(binding.noOfEarningMembersEditText.getText().toString()));
                patientAttributesDTOList.add(patientAttributesDTO);

                // electricityStatus
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("electricityStatus"));
                String electriValue = StringUtils.getSurveyStrings(
                        ((RadioButton) binding.householdElectricityRadioGroup.
                                findViewById(binding.householdElectricityRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                electriValue = StringUtils.second_filter(electriValue);
                patientAttributesDTO.setValue(electriValue);
                patientAttributesDTOList.add(patientAttributesDTO);

                if (binding.householdElectricityYes.isChecked()) {

                    // loadSheddingHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingHoursPerDay"));
                    String[] loadSheddingHoursArray = binding.loadSheddingHoursEditText.getText().toString().split(" ");
                    String loadSheddingHours = loadSheddingHoursArray[0] + " " + getTimeStrings(loadSheddingHoursArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    loadSheddingHours = StringUtils.second_filter(loadSheddingHours);
                    patientAttributesDTO.setValue(loadSheddingHours);
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // loadSheddingDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingDaysPerWeek"));
                    String[] loadSheddingDaysArray = binding.loadSheddingDaysEditText.getText().toString().split(" ");
                    String loadSheddingDays = loadSheddingDaysArray[0] + " " + getTimeStrings(loadSheddingDaysArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    loadSheddingDays = StringUtils.second_filter(loadSheddingDays);
                    patientAttributesDTO.setValue(loadSheddingDays);
                    patientAttributesDTOList.add(patientAttributesDTO);

                } else {

                    // loadSheddingHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingHoursPerDay"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // loadSheddingDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("loadSheddingDaysPerWeek"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                }

                // runningWaterAvailability
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("runningWaterAvailability"));
                String runningWaterVal = StringUtils.getSurveyStrings(
                        ((RadioButton) binding.householdRunningWaterRadioGroup.findViewById
                                (binding.householdRunningWaterRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                runningWaterVal = StringUtils.second_filter(runningWaterVal);
                patientAttributesDTO.setValue(runningWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                //Main source of drinking water...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Drinking Water Source"));
                // patientAttributesDTO.setValue(source_of_water_spinner.getSelectedItem().toString());
                String drinkWaterVal = getSelectedCheckboxes(
                        binding.mainSourceOfDrinkingWaterCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                );
                drinkWaterVal = StringUtils.second_filter(drinkWaterVal);
                patientAttributesDTO.setValue(drinkWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                if (binding.waterSupplyYes.isChecked()) {

                    // waterSupplyAvailabilityHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHoursPerDay"));
                    String[] runningWaterHoursArray = binding.runningWaterHoursEditText.getText().toString().split(" ");
                    String runningWaterHours = runningWaterHoursArray[0] + " " + getTimeStrings(runningWaterHoursArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    patientAttributesDTO.setValue(runningWaterHours);
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // waterSupplyAvailabilityDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysPerWeek"));
                    String[] runningWaterDaysArray = binding.runningWaterDaysEditText.getText().toString().split(" ");
                    String runningWaterDays = runningWaterDaysArray[0] + " " + getTimeStrings(runningWaterDaysArray[1], context, updatedContext, sessionManager.getAppLanguage());
                    patientAttributesDTO.setValue(runningWaterDays);
                    patientAttributesDTOList.add(patientAttributesDTO);

                } else {

                    // waterSupplyAvailabilityHoursPerDay
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityHoursPerDay"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                    // waterSupplyAvailabilityDaysPerWeek
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("waterSupplyAvailabilityDaysPerWeek"));
                    patientAttributesDTO.setValue("-");
                    patientAttributesDTOList.add(patientAttributesDTO);

                }

                //Main source of drinking water...  // duplicate code was getting created here....
              /*  patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("Drinking Water Source"));
                // patientAttributesDTO.setValue(source_of_water_spinner.getSelectedItem().toString());
                String drinkWaterVal_1 = getSelectedCheckboxes(
                        binding.mainSourceOfDrinkingWaterCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                );
                drinkWaterVal_1 = StringUtils.second_filter(drinkWaterVal_1);
                patientAttributesDTO.setValue(drinkWaterVal_1);
                patientAttributesDTOList.add(patientAttributesDTO);
*/
                //How do you make Drinking water Safe?
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("Safe Drinking Water"));
                // patientAttributesDTO.setValue(howtomake_water_safe_spinner.getSelectedItem().toString());
                String safeWaterVal = getSelectedCheckboxes(
                        binding.householdMakeSafeWaterCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                );
                safeWaterVal = StringUtils.second_filter(safeWaterVal);
                patientAttributesDTO.setValue(safeWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                // Time taken to get water - Updated - Start
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Time Drinking Water Source"));
                String timeWaterVal = StringUtils.getSurveyStrings(
                        ((RadioButton) binding.waterSource30minutesRadioGroup.
                                findViewById(binding.waterSource30minutesRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                timeWaterVal = StringUtils.second_filter(timeWaterVal);
                patientAttributesDTO.setValue(timeWaterVal);
                patientAttributesDTOList.add(patientAttributesDTO);
                // Time taken to get water - Updated - End

               /* // Time taken to get water...
                if (time_water_checkbox.isChecked()) {
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Time Drinking Water Source"));
                    // patientAttributesDTO.setValue(StringUtils.getValue(time_water_checkbox.getText().toString()));
                    patientAttributesDTO.setValue(StringUtils.getCheckbox_Hi_En_Or_Gu_As(time_water_checkbox.getText().toString()));
                    Log.d("HOH", "time to bring water_edit: " + time_water_checkbox.getText().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                } else {
                    //User enters value here...
                    String water_time = time_water_editText.getText().toString().replaceAll(getString(R.string.identification_screen_picker_hours), "hours")
                            .replaceAll(getString(R.string.identification_screen_picker_minute), "minute");
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Time Drinking Water Source"));
                    patientAttributesDTO.setValue(StringUtils.getValue(water_time));
                    Log.d("HOH", "time to bring water value entered: " + time_water_editText.getText().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                }
*/

                //            patientAttributesDTO = new PatientAttributesDTO();
//            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
//            patientAttributesDTO.setPatientuuid(uuid);
//            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
//                    .getUuidForAttribute("Time Drinking Water Source"));
//            patientAttributesDTO.setValue(howtomake_water_safe_spinner.getSelectedItem().toString());
//            patientAttributesDTOList.add(patientAttributesDTO);

             /*   //Drinking water availability...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("Drinking Water Availability"));
                // patientAttributesDTO.setValue(water_availability_spinner.getSelectedItem().toString());
                patientAttributesDTO.setValue(StringUtils.getSpinnerHi_En(water_availability_spinner));
                Log.d("HOH", "Water availability: " + water_availability_spinner.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);
*/
                //Toilet Facility...
                if (toiletfacility_edittext.getVisibility() == View.VISIBLE && !toiletfacility_edittext
                        .getText().toString().isEmpty() && !toiletfacility_edittext.getText()
                        .toString().equalsIgnoreCase("")) {

                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Toilet Facility"));
                    patientAttributesDTO.setValue(toiletfacility_edittext.getText().toString());
                    Log.d("HOH", "Toilet: " + toiletfacility_edittext.getText().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                } else {
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                            .getUuidForAttribute("Toilet Facility"));
                    //  patientAttributesDTO.setValue(toilet_facility_spinner.getSelectedItem().toString());
                    String toiletVal = StringUtils.getSpinnerHi_En(toilet_facility_spinner);
                    toiletVal = StringUtils.second_filter(toiletVal);
                    patientAttributesDTO.setValue(toiletVal);
                    Log.d("HOH", "Toilet: " + toilet_facility_spinner.getSelectedItem().toString());
                    patientAttributesDTOList.add(patientAttributesDTO);
                }

                //House Structure...
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO
                        .getUuidForAttribute("House Structure"));
                //  patientAttributesDTO.setValue(structure_of_house_spinner.getSelectedItem().toString());
                String hhVal = StringUtils.getSpinnerHi_En(structure_of_house_spinner);
                hhVal = StringUtils.second_filter(hhVal);
                patientAttributesDTO.setValue(hhVal);
                Log.d("HOH", "Structure: " + structure_of_house_spinner.getSelectedItem().toString());
                patientAttributesDTOList.add(patientAttributesDTO);

                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Family Cultivable Land"));
                String cultivableLand = binding.landOwnedEditText.getText().toString() + " " + getLandOwnedStrings(
                        binding.unitsSpinner.getSelectedItem().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage());
                cultivableLand = StringUtils.second_filter(cultivableLand);
                patientAttributesDTO.setValue(cultivableLand);
                patientAttributesDTOList.add(patientAttributesDTO);

                // averageAnnualHouseholdIncome
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageAnnualHouseholdIncome"));
                String incomeVal = StringUtils.getRadioButtonStrings(
                        ((RadioButton) binding.averageAnnualHouseholdIncomeRadioGroup.findViewById
                                (binding.averageAnnualHouseholdIncomeRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                );
                incomeVal = StringUtils.second_filter(incomeVal);
                patientAttributesDTO.setValue(incomeVal);
                patientAttributesDTOList.add(patientAttributesDTO);

             /*   // averageExpenditureOnHealth
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnHealth"));
                patientAttributesDTO.setValue(StringUtils.getRadioButtonStrings(
                        ((RadioButton) binding.annualHealthExpenditureRadioGroup.findViewById(binding.annualHealthExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
              /*  // averageExpenditureOnEducation
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("averageExpenditureOnEducation"));
                patientAttributesDTO.setValue(StringUtils.getRadioButtonStrings(
                        ((RadioButton) binding.educationExpenditureRadioGroup.findViewById(binding.educationExpenditureRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
//                sessionManager.setHOH_checkbox(true);
//                Log.d("session", "session_create: " + sessionManager.getHOH_checkbox());

                // cookingFuel
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("cookingFuel"));
                String cookingVal = getSelectedCheckboxes(
                        binding.householdCookingFuelCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                );
                cookingVal = StringUtils.second_filter(cookingVal);
                patientAttributesDTO.setValue(cookingVal);
                patientAttributesDTOList.add(patientAttributesDTO);

                // householdLighting
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("householdLighting"));
                String lightVal = getSelectedCheckboxes(
                        binding.mainSourceOfLightingCheckboxLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                ).replace("\\", "");
                lightVal = StringUtils.second_filter(lightVal);
                patientAttributesDTO.setValue(lightVal); // replace since Lantern/Kersone/Lamp gets converted as Lantern\/Kersonene\/Lamp since they are special chars.
                patientAttributesDTOList.add(patientAttributesDTO);

             /*   // defecatedInOpen
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("defecatedInOpen"));
                patientAttributesDTO.setValue(getRadioButtonStrings(
                        ((RadioButton) binding.openDefecationRadioGroup.findViewById(binding.openDefecationRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
                if (binding.llReasonForOpenDefecation.getVisibility() == View.VISIBLE) {
                    // reasonForOpenDefecation
                    patientAttributesDTO = new PatientAttributesDTO();
                    patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                    patientAttributesDTO.setPatientuuid(uuid);
                    patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("reasonForOpenDefecation"));
                    patientAttributesDTO.setValue(getSelectedCheckboxes(
                            binding.reasonForOpenDefecationCheckboxLinearLayout,
                            context,
                            updatedContext,
                            sessionManager.getAppLanguage(),
                            "-"
                    ));
                    patientAttributesDTOList.add(patientAttributesDTO);
                }

                // soapHandWashingOccasion
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("soapHandWashingOccasion"));
                patientAttributesDTO.setValue(getSelectedCheckboxes(
                        binding.handWashOccasionLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                ));
                patientAttributesDTOList.add(patientAttributesDTO);

                // ekal process
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("TakeOurService"));
                patientAttributesDTO.setValue(StringUtils.getSurveyStrings(
                        ((RadioButton) binding.ekalProcessRadioGroup.
                                findViewById(binding.ekalProcessRadioGroup.getCheckedRadioButtonId())).getText().toString(),
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage()
                ));
                patientAttributesDTOList.add(patientAttributesDTO);

              /*  // foodItemsPreparedInTwentyFourHours
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(uuid);
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("foodItemsPreparedInTwentyFourHours"));
                patientAttributesDTO.setValue(getSelectedCheckboxes(
                        binding.foodCookedInTwentyFourHoursLinearLayout,
                        context,
                        updatedContext,
                        sessionManager.getAppLanguage(),
                        "-"
                ));
                patientAttributesDTOList.add(patientAttributesDTO);
*/
            } else {
//                sessionManager.setHOH_checkbox(false);
//                Log.d("session", "session_create: " + sessionManager.getHOH_checkbox());
            }
            //end of checking if the family head checkbox is checked or not...

            // Medical History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("otherMedicalHistory"));
            patientAttributesDTO.setValue(new Gson().toJson(medicalHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Smoking History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("smokingStatus"));
            patientAttributesDTO.setValue(new Gson().toJson(smokingHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Tobacco History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("TobaccoStatus"));
            patientAttributesDTO.setValue(new Gson().toJson(tobaccoHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            // Alcohol Consumption History
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("alcoholConsumptionStatus"));
            patientAttributesDTO.setValue(new Gson().toJson(alcoholConsumptionHistoryList));
            patientAttributesDTOList.add(patientAttributesDTO);

            //patientAttributesDTOList.add(patientAttributesDTO);
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

    private void setUpTranslationTools() {
        sessionManager = new SessionManager(this);
        Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
        configuration.setLocale(new Locale("en"));
        Context context = IdentificationActivity.this;
        updatedContext = context.createConfigurationContext(configuration);
    }

    @Override
    public void saveMedicalHistoryData(MedicalHistory medicalHistory) {
        medicalHistoryList.add(medicalHistory);
        medicalHistoryAdapter = new MedicalHistoryAdapter(medicalHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
        medicalHistoryViewPager.setAdapter(medicalHistoryAdapter);
        medicalHistoryViewPager.setCurrentItem(medicalHistoryList.size() - 1);
        medicalHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(medicalHistoryViewPager);
        binding.cardViewMedicalHistory.requestFocus();
    }

    @Override
    public void saveMedicalHistoryDataAtPosition(MedicalHistory medicalHistory, int position) {
        medicalHistoryList.set(position, medicalHistory);
        medicalHistoryAdapter = new MedicalHistoryAdapter(medicalHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
        medicalHistoryViewPager.setAdapter(medicalHistoryAdapter);
        medicalHistoryViewPager.setCurrentItem(medicalHistoryList.size() - 1);
        medicalHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(medicalHistoryViewPager);
        binding.cardViewMedicalHistory.requestFocus();
    }

    @Override
    public void saveSmokingHistory(SmokingHistory smokingHistory) {
        smokingHistoryList.add(smokingHistory);
        smokingHistoryAdapter = new SmokingHistoryAdapter(smokingHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
        smokingHistoryViewPager.setAdapter(smokingHistoryAdapter);
        smokingHistoryViewPager.setCurrentItem(smokingHistoryList.size() - 1);
        smokingHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(smokingHistoryViewPager);
    }

    @Override
    public void saveSmokingHistoryAtPosition(SmokingHistory smokingHistory, int position) {
        smokingHistoryList.set(position, smokingHistory);
        smokingHistoryAdapter = new SmokingHistoryAdapter(smokingHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
        smokingHistoryViewPager.setAdapter(smokingHistoryAdapter);
        smokingHistoryViewPager.setCurrentItem(smokingHistoryList.size() - 1);
        smokingHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(smokingHistoryViewPager);
    }

    @Override
    public void saveAlcoholConsumptionData(AlcoholConsumptionHistory alcoholConsumptionHistory) {
        alcoholConsumptionHistoryList.add(alcoholConsumptionHistory);
        alcoholConsumptionHistoryAdapter = new AlcoholConsumptionHistoryAdapter(alcoholConsumptionHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
        alcoholViewPager.setAdapter(alcoholConsumptionHistoryAdapter);
        alcoholViewPager.setCurrentItem(alcoholConsumptionHistoryList.size() - 1);
        alcoholViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(alcoholViewPager);
    }

    @Override
    public void saveAlcoholConsumptionDataAtPosition(AlcoholConsumptionHistory alcoholConsumptionHistory, int position) {
        alcoholConsumptionHistoryList.set(position, alcoholConsumptionHistory);
        alcoholConsumptionHistoryAdapter = new AlcoholConsumptionHistoryAdapter(alcoholConsumptionHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
        alcoholViewPager.setAdapter(alcoholConsumptionHistoryAdapter);
        alcoholViewPager.setCurrentItem(alcoholConsumptionHistoryList.size() - 1);
        alcoholViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(alcoholViewPager);
    }

    private void setViewPagerOffset(ViewPager2 viewPager2) {
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);

        int pageMarginPx = getResources().getDimensionPixelOffset(R.dimen.pageMargin);
        float offsetPx = getResources().getDimensionPixelOffset(R.dimen.offset);
        viewPager2.setPageTransformer((page, position) -> {
            ViewPager2 viewPager = (ViewPager2) page.getParent().getParent();
            float offset = position * -(2 * offsetPx + pageMarginPx);
            if (viewPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.setTranslationX(-offset);
                } else {
                    page.setTranslationX(offset);
                }
            } else {
                page.setTranslationY(offset);
            }
        });
    }

    public Context getUpdatedContext() {
        return updatedContext;
    }

    @Override
    public void getMedicalHistory(MedicalHistory medicalHistory, int position) {
        MaterialAlertDialogBuilder listDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogStyle);
        listDialog.setItems(new String[]{getString(R.string.edit_dialog_button), getString(R.string.delete_dialog_button)}, (dialog, which) -> {
            if (which == 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("hypertension", medicalHistory.getHypertension());
                bundle.putString("diabetes", medicalHistory.getDiabetes());
                bundle.putString("arthritis", medicalHistory.getArthritis());
                bundle.putString("anaemia", medicalHistory.getAnaemia());
                bundle.putString("anySurgeries", medicalHistory.getAnySurgeries());
                bundle.putString("reasonForSurgery", medicalHistory.getReasonForSurgery());

                MedicalHistoryDialog medicalHistoryDialog = new MedicalHistoryDialog();
                medicalHistoryDialog.setArguments(bundle);
                medicalHistoryDialog.show(getSupportFragmentManager(), MedicalHistoryDialog.TAG);
            }

            if (which == 1) {
                deleteSurveyData(position, medicalHistory);
            }
        });

        listDialog.show();
    }

    @Override
    public void getSmokingHistory(SmokingHistory smokingHistory, int position) {
        MaterialAlertDialogBuilder listDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogStyle);
        listDialog.setItems(new String[]{getString(R.string.edit_dialog_button), getString(R.string.delete_dialog_button)}, (dialog, which) -> {
            if (which == 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("smokingStatus", smokingHistory.getSmokingStatus());
                bundle.putString("rateOfSmoking", smokingHistory.getRateOfSmoking());
                bundle.putString("durationOfSmoking", smokingHistory.getDurationOfSmoking());
                bundle.putString("frequencyOfSmoking", smokingHistory.getFrequencyOfSmoking());

                SmokingHistoryDialog smokingHistoryDialog = new SmokingHistoryDialog();
                smokingHistoryDialog.setArguments(bundle);
                smokingHistoryDialog.show(getSupportFragmentManager(), SmokingHistoryDialog.TAG);
            }

            if (which == 1) {
                deleteSurveyData(position, smokingHistory);
            }
        });

        listDialog.show();
    }

    @Override
    public void getTobaccoHistory(TobaccoHistory tobaccoHistory, int position) {
        MaterialAlertDialogBuilder listDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogStyle);
        listDialog.setItems(new String[]{getString(R.string.edit_dialog_button), getString(R.string.delete_dialog_button)}, (dialog, which) -> {
            if (which == 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("TobaccoStatus", tobaccoHistory.getChewTobaccoStatus());

                TobaccoHistoryDialog tobaccoHistoryDialog = new TobaccoHistoryDialog();
                tobaccoHistoryDialog.setArguments(bundle);
                tobaccoHistoryDialog.show(getSupportFragmentManager(), TobaccoHistoryDialog.TAG);
            }

            if (which == 1) {
                deleteSurveyData(position, tobaccoHistory);
            }
        });

        listDialog.show();
    }

    @Override
    public void getAlcoholHistory(AlcoholConsumptionHistory alcoholConsumptionHistory,
                                  int position) {
        MaterialAlertDialogBuilder listDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogStyle);
        listDialog.setItems(new String[]{getString(R.string.edit_dialog_button), getString(R.string.delete_dialog_button)}, (dialog, which) -> {
            if (which == 0) {
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("historyOfAlcoholConsumption", alcoholConsumptionHistory.getHistoryOfAlcoholConsumption());
                bundle.putString("rateOfAlcoholConsumption", alcoholConsumptionHistory.getRateOfAlcoholConsumption());
                bundle.putString("durationOfAlcoholConsumption", alcoholConsumptionHistory.getDurationOfAlcoholConsumption());
                bundle.putString("frequencyOfAlcoholConsumption", alcoholConsumptionHistory.getFrequencyOfAlcoholConsumption());

                AlcoholConsumptionHistoryDialog alcoholConsumptionHistoryDialog = new AlcoholConsumptionHistoryDialog();
                alcoholConsumptionHistoryDialog.setArguments(bundle);
                alcoholConsumptionHistoryDialog.show(getSupportFragmentManager(), AlcoholConsumptionHistoryDialog.TAG);
            }

            if (which == 1) {
                deleteSurveyData(position, alcoholConsumptionHistory);
            }
        });

        listDialog.show();
    }

    public void deleteSurveyData(int position, Object object) {
        if (object instanceof MedicalHistory) {
            if (medicalHistoryList.size() != 0) { // Added this check to ensure  that the list isn't empty before removing the element - Added by Arpan Sircar
                medicalHistoryList.remove(position);
                medicalHistoryAdapter = new MedicalHistoryAdapter(medicalHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
                medicalHistoryViewPager.setAdapter(medicalHistoryAdapter);
                if (!medicalHistoryList.isEmpty()) {
                    medicalHistoryViewPager.setCurrentItem(medicalHistoryList.size() - 1);
                }
                medicalHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                setViewPagerOffset(medicalHistoryViewPager);
            }
        }

        if (object instanceof SmokingHistory) {
            if (smokingHistoryList.size() != 0) { // Added this check to ensure  that the list isn't empty before removing the element - Added by Arpan Sircar
                smokingHistoryList.remove(position);
                smokingHistoryAdapter = new SmokingHistoryAdapter(smokingHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
                smokingHistoryViewPager.setAdapter(smokingHistoryAdapter);
                if (!medicalHistoryList.isEmpty()) {
                    smokingHistoryViewPager.setCurrentItem(smokingHistoryList.size() - 1);
                }
                smokingHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                setViewPagerOffset(smokingHistoryViewPager);
            }
        }
        // Tobacco Consumption
        if (object instanceof TobaccoHistory) {
            if (tobaccoHistoryList.size() != 0) {
                tobaccoHistoryList.remove(position);
                tobaccoHistoryAdapter = new TobaccoHistoryAdapter(tobaccoHistoryList, sessionManager.getAppLanguage(),
                        this, updatedContext, this);
                tobaccoHistoryViewPager.setAdapter(tobaccoHistoryAdapter);
                if (!medicalHistoryList.isEmpty()) {
                    tobaccoHistoryViewPager.setCurrentItem(tobaccoHistoryList.size() - 1);
                }
                tobaccoHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                setViewPagerOffset(tobaccoHistoryViewPager);
            }
        }

        if (object instanceof AlcoholConsumptionHistory) {
            if (alcoholConsumptionHistoryList.size() != 0) { // Added this check to ensure  that the list isn't empty before removing the element - Added by Arpan Sircar
                alcoholConsumptionHistoryList.remove(position);
                alcoholConsumptionHistoryAdapter = new AlcoholConsumptionHistoryAdapter(alcoholConsumptionHistoryList, sessionManager.getAppLanguage(), this, updatedContext, this);
                alcoholViewPager.setAdapter(alcoholConsumptionHistoryAdapter);
                if (!alcoholConsumptionHistoryList.isEmpty()) {
                    alcoholViewPager.setCurrentItem(alcoholConsumptionHistoryList.size() - 1);
                }
                alcoholViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                setViewPagerOffset(alcoholViewPager);
            }
        }
    }

    private void timePicker(String unit, EditText editText, int maxValue) {
        MaterialAlertDialogBuilder timeBuilder = new MaterialAlertDialogBuilder(IdentificationActivity.this, R.style.AlertDialogStyle);
        timeBuilder.setTitle(getString(R.string.select) + " " + unit);
        final LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_1_unit_picker, null);
        timeBuilder.setView(convertView);

        NumberPicker timeQuantity = convertView.findViewById(R.id.quantity_text_view);
        TextView timeUnit = convertView.findViewById(R.id.units_text_view);
        timeQuantity.setMinValue(0);
        timeQuantity.setMaxValue(maxValue);
        timeUnit.setText(unit);

        timeBuilder.setPositiveButton(R.string.generic_ok, (dialog, which) -> {
            String hoursString = timeQuantity.getValue() + " " + timeUnit.getText().toString();
            editText.setText(hoursString);
        });

        AlertDialog alertDialog = timeBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(IdentificationActivity.this, alertDialog);
    }

    @Override
    public void saveTobaccoHistory(TobaccoHistory tobaccoHistory) {
        tobaccoHistoryList.add(tobaccoHistory);
        tobaccoHistoryAdapter = new TobaccoHistoryAdapter(tobaccoHistoryList, sessionManager.getAppLanguage(),
                this, updatedContext, this);
        tobaccoHistoryViewPager.setAdapter(tobaccoHistoryAdapter);
        tobaccoHistoryViewPager.setCurrentItem(tobaccoHistoryList.size() - 1);
        tobaccoHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(tobaccoHistoryViewPager);
    }

    @Override
    public void saveTobaccoHistoryAtPosition(TobaccoHistory tobaccoHistory, int position) {
        tobaccoHistoryList.set(position, tobaccoHistory);
        tobaccoHistoryAdapter = new TobaccoHistoryAdapter(tobaccoHistoryList, sessionManager.getAppLanguage(),
                this, updatedContext, this);
        tobaccoHistoryViewPager.setAdapter(tobaccoHistoryAdapter);
        tobaccoHistoryViewPager.setCurrentItem(tobaccoHistoryList.size() - 1);
        tobaccoHistoryViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setViewPagerOffset(tobaccoHistoryViewPager);
    }

    private void checkIfPatientIsHoHOrNot(String householduuid) throws DAOException {
        String hhUUID = sessionManager.getHouseholdUuid();
        if (!hhUUID.isEmpty() || !hhUUID.equalsIgnoreCase("")) {    // hhuuid is not empty ie. this is a fam members.
            List<String> patientUUIDList = null;
            patientUUIDList = new ArrayList<>(patientsDAO.getPatientUUIDs(householduuid));
            Log.e("HoH?", "List of patient: " + patientUUIDList);

            // now, traverse each arraylist item of patientUuid and check if the value of hohRelationShip.
            String hoh_relationship_uuid = patientsDAO.getUuidForAttribute("hohRelationship");  // 7bc0540f-6bcf-4fdd-a0c5-4068a3c922f9

            for (int i = 0; i < patientUUIDList.size(); i++) {
                String patientUUID = patientUUIDList.get(i);
                String hohRelationShip_value = "";
                hohRelationShip_value = patientsDAO.getValueFromPatientAttrbTable(patientUUID, hoh_relationship_uuid);

                if (hohRelationShip_value.equalsIgnoreCase("-")) {
                    // That means in this family already HoH is registered so break loop and set No radiobtn checked so that for
                    // each rest of the fam members we dont again ask are you hoh? question.
                    binding.hohNo.setChecked(true);

                    if (patientID_edit == null || patientID_edit.isEmpty()) {
                        binding.hohYes.setEnabled(false);
                        binding.hohNo.setEnabled(false);
                    }

                    binding.cardviewHousehold.setVisibility(View.GONE);
                    binding.cardviewHohRelationship.setVisibility(View.VISIBLE);

                    return;
                }
            }


        }
    }

}
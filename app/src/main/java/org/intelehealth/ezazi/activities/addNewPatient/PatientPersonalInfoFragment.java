package org.intelehealth.ezazi.activities.addNewPatient;

import static org.intelehealth.ezazi.utilities.StringUtils.en__as_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__bn_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__gu_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__kn_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__ml_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__mr_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__or_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__ru_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__ta_dob;
import static org.intelehealth.ezazi.utilities.StringUtils.en__te_dob;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.cameraActivity.CameraActivity;
import org.intelehealth.ezazi.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ezazi.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ezazi.activities.setupActivity.SetupActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.ImagesDAO;
import org.intelehealth.ezazi.database.dao.ImagesPushDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.models.Patient;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.models.dto.ProviderDTO;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.EditTextUtils;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.IReturnValues;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.NetworkConnection;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringUtils;
import org.intelehealth.ezazi.utilities.UuidGenerator;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 *
 */
public class PatientPersonalInfoFragment extends Fragment {
    private static final String TAG = "PatientPersonalInfoFrag";
    View view;
    SessionManager sessionManager = null;
    Context mContext;
    private List<ProviderDTO> mProviderDoctorList = new ArrayList<ProviderDTO>();
    TextInputEditText mFirstName, mMiddleName, mLastName, mDOB, mAge, mMobileNumber, mAlternateNumber;
    private String BlockCharacterSet_Others = "0123456789\\@$!=><&^*+€¥£`~";
    private String BlockCharacterSet_Name = "\\@$!=><&^*+\"\'€¥£`~";
    private boolean mIsEditMode = false;
    String patientID_edit;
    Patient patient1 = new Patient();
    private boolean hasLicense = false;
    private String patientUuid = "";
    UuidGenerator uuidGenerator = new UuidGenerator();
    private int mDOBYear;
    private int mDOBMonth;
    private int mDOBDay;
    private DatePickerDialog mDOBPicker;
    private int mAgeYears = 0;
    private int mAgeMonths = 0;
    private int mAgeDays = 0;
    Calendar today = Calendar.getInstance();
    Calendar dob = Calendar.getInstance();
    int dob_indexValue = 15;
    MaterialAlertDialogBuilder mAgePicker;
    MaterialButton btnSaveUpdate;
    String uuid = "";
    PatientDTO patientDTO = new PatientDTO();
    private String mCurrentPhotoPath;
    ImagesDAO imagesDAO = new ImagesDAO();
    Intent i_privacy;
    String privacy_value;
    private String mAlternateNumberString = "";
    PatientsDAO patientsDAO = new PatientsDAO();
    boolean fromSecondScreen = false;
    private PatientAddressInfoFragment fragment_secondScreen;
    boolean patient_detail = false;
    ImageView ivPersonal, ivAddress, ivOther;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateLocale();
        view = inflater.inflate(R.layout.fragment_patient_personal_info, container, false);
        mContext = getActivity();
        initUI();
        return view;
    }

    private void updateLocale() {
        sessionManager = new SessionManager(getActivity());
        String language = sessionManager.getAppLanguage();
        Log.d("lang", "lang: " + language);
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config,
                    getResources().getDisplayMetrics());
        }
        //  sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

    }

    private void initUI() {
        ivPersonal = getActivity().findViewById(R.id.iv_personal_info);
        ivAddress = getActivity().findViewById(R.id.iv_address_info);
        ivOther = getActivity().findViewById(R.id.iv_other_info);

        /*new*/
        ProviderDAO providerDAO = new ProviderDAO();
        try {
            mProviderDoctorList = providerDAO.getDoctorList();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        fab = view.findViewById(R.id.fab_update_photo);
        ImageView ivProfilePhoto = view.findViewById(R.id.iv_profile_photo);
        mFirstName = view.findViewById(R.id.et_first_name);
        mMiddleName = view.findViewById(R.id.et_middle_name);
        mLastName = view.findViewById(R.id.et_last_name);
        mDOB = view.findViewById(R.id.et_dob);
        mAge = view.findViewById(R.id.et_age);
        mMobileNumber = view.findViewById(R.id.et_mobile_no);
        mAlternateNumber = view.findViewById(R.id.et_alternate_mobile);
        btnSaveUpdate = view.findViewById(R.id.btn_save_update_first);
        i_privacy = getActivity().getIntent();
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.


        mFirstName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mMiddleName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mLastName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25


        //Initialize the local database to store patient information

        Intent intent = Objects.requireNonNull(getActivity()).getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                mIsEditMode = true;
                //mContext.setTitle(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
                updateUI(patient1);
            }
        }
        fragment_secondScreen = new PatientAddressInfoFragment();
        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            //   patientID_edit = getArguments().getString("patientUuid");
            patient_detail = getArguments().getBoolean("patient_detail");
            fromSecondScreen = getArguments().getBoolean("fromSecondScreen");
        }

/*
            if (patientDTO.getPatientPhoto() != null) {
                Glide.with(getActivity())
                        .load(new File(patientDTO.getPatientPhoto()))
                        .thumbnail(0.25f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(patient_imgview);
            }
*/
        // Setting up the screen when user came from Second screen.
        if (fromSecondScreen) {
            mFirstName.setText(patientDTO.getFirstname());
            mMiddleName.setText(patientDTO.getMiddlename());
            mLastName.setText(patientDTO.getLastname());
            mMobileNumber.setText(patientDTO.getPhonenumber());
            //mAlternateNumber.setText(patientDTO.getPhonenumber());

            //if patient update then age will be set
            //dob to be displayed based on translation...
            String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patientDTO.getDateofbirth());
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                mLastName.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                String dob_text = en__te_dob(dob); //to show text of English into Telugu...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                String dob_text = en__mr_dob(dob); //to show text of English into marathi...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                String dob_text = en__as_dob(dob); //to show text of English into assame...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                String dob_text = en__ml_dob(dob); //to show text of English into malyalum...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                String dob_text = en__kn_dob(dob); //to show text of English into kannada...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                String dob_text = en__ru_dob(dob); //to show text of English into kannada...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                String dob_text = en__gu_dob(dob); //to show text of English into Gujarati...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                String dob_text = en__bn_dob(dob); //to show text of English into Bengali...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                String dob_text = en__ta_dob(dob); //to show text of English into Tamil...
                mDOB.setText(dob_text);
            } else {
                mDOB.setText(dob);
            }

            // dob_edittext.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
            //get year month days
            String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patientDTO.getDateofbirth(), getActivity());

            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patientDTO.getDateofbirth()).split(" ");
            mAgeYears = Integer.valueOf(ymdData[0]);
            mAgeMonths = Integer.valueOf(ymdData[1]);
            mAgeDays = Integer.valueOf(ymdData[2]);
            String age = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " +
                    mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " +
                    mAgeDays + getResources().getString(R.string.days);
            mAge.setText(age);

/*temp
            mCountryCodePicker.setFullNumber(patientDTO.getPhonenumber()); // automatically assigns cc to spinner and number to edittext field.
*/
            //   phoneno_edittext.setText(patientDTO.getPhonenumber());

        /*
        commented as per new flow

          // Gender edit
            if (patientDTO.getGender().equals("M")) {
                mGenderMaleRadioButton.setChecked(true);
                if (mGenderFemaleRadioButton.isChecked())
                    mGenderFemaleRadioButton.setChecked(false);
                if (mGenderOthersRadioButton.isChecked())
                    mGenderOthersRadioButton.setChecked(false);
                Log.v(TAG, "yes");
            } else if (patientDTO.getGender().equals("F")) {
                mGenderFemaleRadioButton.setChecked(true);
                if (mGenderMaleRadioButton.isChecked())
                    mGenderMaleRadioButton.setChecked(false);
                if (mGenderOthersRadioButton.isChecked())
                    mGenderOthersRadioButton.setChecked(false);
                Log.v(TAG, "yes");
            } else {
                mGenderOthersRadioButton.setChecked(true);
                if (mGenderMaleRadioButton.isChecked())
                    mGenderMaleRadioButton.setChecked(false);
                if (mGenderFemaleRadioButton.isChecked())
                    mGenderFemaleRadioButton.setChecked(false);
            }

            if (mGenderMaleRadioButton.isChecked()) {
                mGender = "M";
            } else if (mGenderFemaleRadioButton.isChecked()) {
                mGender = "F";
            } else {
                mGender = "O";
            }
*/
            // profile image edit
            if (patientDTO.getPatientPhoto() != null && !patientDTO.getPatientPhoto().trim().isEmpty()) {
                //  patient_imgview.setImageBitmap(BitmapFactory.decodeFile(patientDTO.getPatientPhoto()));
                Glide.with(getActivity())
                        .load(new File(patientDTO.getPatientPhoto()))
                        .thumbnail(0.25f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(ivProfilePhoto);

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
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, mContext),
                                String.valueOf(FileUtils.encodeJSON(mContext, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(mContext, AppConstants.CONFIG_FILE_NAME)));
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
                mMobileNumber.setVisibility(View.VISIBLE);
            } else {
                mMobileNumber.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mAge")) {
                mAge.setVisibility(View.VISIBLE);
            } else {
                mAge.setVisibility(View.GONE);
            }
           /*

           temp

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
            }*/

           /* if (obj.getBoolean("mGenderM")) {
                mGenderM.setVisibility(View.VISIBLE);
            } else {
                mGenderM.setVisibility(View.GONE);
            }*/
            /*if (obj.getBoolean("mGenderF")) {
                mGenderF.setVisibility(View.VISIBLE);
            } else {
                mGenderF.setVisibility(View.GONE);
            }*/
          /*  if (obj.getBoolean("mRelationship")) {
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
            }*/

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(mContext, "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }
        handleClickListeners();


        if (patient1.getPatient_photo() != null && !patient1.getPatient_photo().trim().isEmpty())
            ivProfilePhoto.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatient_photo()));

//        ArrayAdapter<CharSequence> casteAdapter = ArrayAdapter.createFromResource(this,
//                R.array.caste, R.layout.custom_spinner);
//        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mCaste.setAdapter(casteAdapter);
       /* try {
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
        try {
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
        try {
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
        }*/


        if (null == patientID_edit || patientID_edit.isEmpty()) {
            generateUuid();

        }

        // setting radio button automatically according to the databse when user clicks edit details
     /*  In new figma design gender removed

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
*/

        gmDOBAndAge();
    }

    private void gmDOBAndAge() {
        mDOBYear = today.get(Calendar.YEAR);
        mDOBMonth = today.get(Calendar.MONTH);
        mDOBDay = today.get(Calendar.DAY_OF_MONTH);
        //DOB is set using an AlertDialog
        // Locale.setDefault(Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -10);
        mDOBPicker = new DatePickerDialog(mContext, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                mDOB.setError(null);
                mAge.setError(null);
                //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                //  mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                mDOBPicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                // Locale.setDefault(Locale.ENGLISH);
                //Formatted so that it can be read the way the user sets
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                dob.set(year, monthOfYear, dayOfMonth);
                String dobString = simpleDateFormat.format(dob.getTime());
                dob_indexValue = monthOfYear; //fetching the inex value of month selected...

                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String dob_text = en__te_dob(dobString); //to show text of English into telugu...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String dob_text = en__mr_dob(dobString); //to show text of English into telugu...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String dob_text = en__as_dob(dobString); //to show text of English into telugu...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String dob_text = en__ml_dob(dobString); //to show text of English into telugu...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String dob_text = en__kn_dob(dobString); //to show text of English into telugu...
                    mDOB.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String dob_text = en__ru_dob(dobString); //to show text of English into telugu...
                    mDOB.setText(dob_text);
                } else {
                    mDOB.setText(dobString);
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
                mAge.setText(ageS);

            }
        }, mDOBYear, mDOBMonth, mDOBDay);

        //DOB Picker is shown when clicked
        // mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDOBPicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
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
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                String dob_text = en__te_dob(dob); //to show text of English into Telugu...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                String dob_text = en__mr_dob(dob); //to show text of English into marathi...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                String dob_text = en__as_dob(dob); //to show text of English into assame...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                String dob_text = en__ml_dob(dob); //to show text of English into malyalum...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                String dob_text = en__kn_dob(dob); //to show text of English into kannada...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                String dob_text = en__ru_dob(dob); //to show text of English into kannada...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                String dob_text = en__gu_dob(dob); //to show text of English into Gujarati...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                String dob_text = en__bn_dob(dob); //to show text of English into Bengali...
                mDOB.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                String dob_text = en__ta_dob(dob); //to show text of English into Tamil...
                mDOB.setText(dob_text);
            } else {
                mDOB.setText(dob);
            }

            // mDOB.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
            //get year month days
            String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth(), mContext);

            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth()).split(" ");
            mAgeYears = Integer.valueOf(ymdData[0]);
            mAgeMonths = Integer.valueOf(ymdData[1]);
            mAgeDays = Integer.valueOf(ymdData[2]);
            String age = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " +
                    mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " +
                    mAgeDays + getResources().getString(R.string.days);
            mAge.setText(age);
        }
        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAgePicker = new MaterialAlertDialogBuilder(mContext, R.style.AlertDialogStyle);
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


                yearPicker.setMinValue(10);
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
                    calendar.add(Calendar.DAY_OF_MONTH, -mAgeDays);
                    calendar.add(Calendar.MONTH, -mAgeMonths);
                    calendar.add(Calendar.YEAR, -mAgeYears);

                    mDOBYear = calendar.get(Calendar.YEAR);
                    mDOBMonth = calendar.get(Calendar.MONTH);
                    mDOBDay = calendar.get(Calendar.DAY_OF_MONTH);

//                    int curYear = calendar.get(Calendar.YEAR);
//                    //int birthYear = curYear - yearPicker.getValue();
//                    int birthYear = curYear - mAgeYears;
//                    int curMonth = calendar.get(Calendar.MONTH);
//                    //int birthMonth = curMonth - monthPicker.getValue();
//                    int birthMonth = curMonth - mAgeMonths;
//                    //int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - dayPicker.getValue();
//                    int birthDay = calendar.get(Calendar.DAY_OF_MONTH) - mAgeDays;
//                    mDOBYear = birthYear;
//                    mDOBMonth = birthMonth;
//
//                    if (birthDay < 0) {
//                        mDOBDay = birthDay + totalDays - 1;
//                        mDOBMonth--;
//
//                    } else {
//                        mDOBDay = birthDay;
//                    }
//                    //   Locale.setDefault(Locale.ENGLISH);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy",
                            Locale.ENGLISH);
                    dob.set(mDOBYear, mDOBMonth, mDOBDay);
                    String dobString = simpleDateFormat.format(dob.getTime());
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String dob_text = en__te_dob(dobString); //to show text of English into telugu...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String dob_text = en__mr_dob(dobString); //to show text of English into marathi...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String dob_text = en__as_dob(dobString); //to show text of English into assame...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String dob_text = en__ml_dob(dobString);
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String dob_text = en__kn_dob(dobString); //to show text of English into kannada...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String dob_text = en__ru_dob(dobString); //to show text of English into kannada...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
                        mDOB.setText(dob_text);
                    } else {
                        mDOB.setText(dobString);
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
                IntelehealthApplication.setAlertDialogCustomTheme(mContext, alertDialog);
            }
        });

    }

    private void handleClickListeners() {
        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add it
                //   mDOBPicker.show();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

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

    public void showAlertDialogButtonClicked(String errorMessage) {

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        alertDialogBuilder.setTitle("Config Error");
        alertDialogBuilder.setMessage(errorMessage);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //mContext.finish();
                Intent i = new Intent(mContext, SetupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// This flag ensures all activities on top of the CloseAllViewsDemo are cleared.
                startActivity(i);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(mContext, alertDialog);
    }

    public void generateUuid() {

        patientUuid = uuidGenerator.UuidGenerator();

    }

    public String getYear(int syear, int smonth, int sday, int eyear, int emonth, int eday) {

        //three ten implementation
//        org.threeten.bp.LocalDate localDateTime1 = org.threeten.bp.LocalDate.now();
//        org.threeten.bp.LocalDate localDateTime2 = org.threeten.bp.LocalDate.now();
//        localDateTime2 = localDateTime2.withYear(syear).withMonth(smonth + 1).withDayOfMonth(sday);
//        org.threeten.bp.Period p = org.threeten.bp.Period.between(localDateTime2, localDateTime1);
//return p.getYears() + " years  - " + p.getMonths() + " month - " + p.getDays() + " days";

        LocalDate birthdate = new LocalDate(syear, smonth + 1, sday);
        LocalDate now = new LocalDate();
        Period p = new Period(birthdate, now, PeriodType.yearMonthDay());
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
//                Toast.makeText(this, "Current Date must be greater than Date of Birth", Toast.LENGTH_LONG).show();
//                mDOB.setError(getString(R.string.identification_screen_error_dob));
//                mAge.setError(getString(R.string.identification_screen_error_age));
//            } else {
//                // t1.setText("Age: " + resyear + " years /" + resmonth + " months/" + resday + " days");
//
//                calculatedAge = resyear + " years - " + resmonth + " months - " + resday + " days";
//            }
//        }
//
//        return calculatedAge != null ? calculatedAge : " ";
    }


    public void onPatientUpdateClicked(Patient patientDTO) {

      /*  mTotalBirthCount = mTotalBirthEditText.getText().toString().trim();
        mTotalMiscarriageCount = mTotalMiscarriageEditText.getText().toString().trim();
        mAlternateNumberString = mAlternateNumberEditText.getText().toString().trim();
        mWifeDaughterOfString = mWifeDaughterOfEditText.getText().toString().trim();*/

        PatientsDAO patientsDAO = new PatientsDAO();
        PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
        List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
        uuid = patientDTO.getUuid();

        patientDTO.setUuid(uuid);
        Gson gson = new Gson();

        boolean cancel = false;
        View focusView = null;


        if (dob.equals("") || dob.toString().equals("")) {
            if (dob.after(today)) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
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
                IntelehealthApplication.setAlertDialogCustomTheme(mContext, alertDialog);
                return;
            }
        }

        if (mMobileNumber.getText().toString().trim().length() > 0) {
            if (mMobileNumber.getText().toString().trim().length() < 10) {
                mMobileNumber.requestFocus();
                mMobileNumber.setError("Enter 10 digits");
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
                && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("")) {

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

            Toast.makeText(mContext, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
            return;
        }

        /*new*/
      /*
       commented as per new flow
       if (mSecondaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.seconday_doct_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }*/
        /*end*/

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (mCurrentPhotoPath == null)
                mCurrentPhotoPath = patientDTO.getPatient_photo();

            patientDTO.setFirst_name(StringUtils.getValue(mFirstName.getText().toString()));
            patientDTO.setMiddle_name(StringUtils.getValue(mMiddleName.getText().toString()));
            patientDTO.setLast_name(StringUtils.getValue(mLastName.getText().toString()));
            patientDTO.setPhone_number(StringUtils.getValue(mMobileNumber.getText().toString()));
            //  patientDTO.setGender(StringUtils.getValue(mGender));

            String[] dob_array = mDOB.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or_bn_en_noEdit
                        (mDOB.getText().toString(), sessionManager.getAppLanguage());
                patientDTO.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
                String dob_month_split = dob_array[1];
                dob_array[1] = dob_month_split.replace(dob_month_split, dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];

                patientDTO.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob_value)));
            }

          /* commented as per new flow
          // patientDTO.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(mDOB.getText().toString())));
            patientDTO.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
            patientDTO.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));
            patientDTO.setCity_village(StringUtils.getValue(mCity.getText().toString()));
            patientDTO.setPostal_code(StringUtils.getValue(mPostal.getText().toString()));
//            patientDTO.setCountry(StringUtils.getValue(mSwitch_hi_en_te_Country(mCountry.getSelectedItem().toString(),sessionManager.getAppLanguage())));
            patientDTO.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
            patientDTO.setPatient_photo(mCurrentPhotoPath);
//                patientDTO.setEconomic(StringUtils.getValue(m));
            patientDTO.setState_province(StringUtils.getValue(patientDTO.getState_province()));
//           patientDTO.setState_province(StringUtils.getValue(mSwitch_hi_en_te_State(mState.getSelectedItem().toString(),sessionManager.getAppLanguage())));*/
           /* patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mCaste));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(mMobileNumber.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            /*patientAttributesDTO = new PatientAttributesDTO();
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
            patientAttributesDTO.setValue(AppConstants.dateAndTimeUtils.currentDateTime());*/


            /*new*/
          /*
           Commented as per new flow

           //AlternateNo
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("AlternateNo"));
            patientAttributesDTO.setValue(StringUtils.getValue(mAlternateNumberString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Wife_Daughter_Of
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Wife_Daughter_Of"));
            patientAttributesDTO.setValue(StringUtils.getValue(mWifeDaughterOfString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Admission_Date
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Admission_Date"));
            patientAttributesDTO.setValue(StringUtils.getValue(mAdmissionDateString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Admission_Time
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Admission_Time"));
            patientAttributesDTO.setValue(StringUtils.getValue(mAdmissionTimeString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Parity
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Parity"));
            patientAttributesDTO.setValue(StringUtils.getValue(mTotalBirthCount + "," + mTotalMiscarriageCount));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Labor Onset
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Labor Onset"));
            patientAttributesDTO.setValue(StringUtils.getValue(mLaborOnsetString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Active Labor Diagnosed
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Active Labor Diagnosed"));
            patientAttributesDTO.setValue(StringUtils.getValue(mActiveLaborDiagnosedDate + " " + mActiveLaborDiagnosedTime));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Membrane Ruptured Timestamp
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Membrane Ruptured Timestamp"));
            patientAttributesDTO.setValue(mUnknownMembraneRupturedCheckBox.isChecked() ? "U" : StringUtils.getValue(mMembraneRupturedDate + " " + mMembraneRupturedTime));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Risk factors
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Risk factors"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRiskFactorsString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //Hospital_Maternity
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Hospital_Maternity"));
            patientAttributesDTO.setValue(StringUtils.getValue(mHospitalMaternityString));
            patientAttributesDTOList.add(patientAttributesDTO);

            //PrimaryDoctor
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("PrimaryDoctor"));
            patientAttributesDTO.setValue(StringUtils.getValue(mPrimaryDoctorUUIDString) + "@#@" + mPrimaryDoctorTextView.getText());
            patientAttributesDTOList.add(patientAttributesDTO);

            //SecondaryDoctor
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("SecondaryDoctor"));
            patientAttributesDTO.setValue(StringUtils.getValue(mSecondaryDoctorUUIDString) + "@#@" + mSecondaryDoctorTextView.getText());
            patientAttributesDTOList.add(patientAttributesDTO);

*/
            /*end*/


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
            //patientDTO.setPatientAttributesDTOList(patientAttributesDTOList);

            Logger.logD("patient json onPatientUpdateClicked : ", "Json : " + gson.toJson(patientDTO, Patient.class));

        }
        try {
            Logger.logD(TAG, "update ");
            boolean isPatientUpdated = patientsDAO.updatePatientToDB(patientDTO, uuid, patientAttributesDTOList);
            boolean isPatientImageUpdated = imagesDAO.updatePatientProfileImages(mCurrentPhotoPath, uuid);

            if (NetworkConnection.isOnline(mContext)) {
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean ispush = syncDAO.pushDataApi();
                boolean isPushImage = imagesPushDAO.patientProfileImagesPush();

//                if (ispush)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirst_name() + "" + patientDTO.getLast_name() + "'s data upload complete.", 2, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirst_name() + "" + patientDTO.getLast_name() + "'s data not uploaded.", 2, getApplication());

//                if (isPushImage)
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirst_name() + "" + patientDTO.getLast_name() + "'s Image upload complete.", 4, getApplication());
//                else
//                    AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_data_upload), "" + patientDTO.getFirst_name() + "" + patientDTO.getLast_name() + "'s Image not complete.", 4, getApplication());

            }
            if (isPatientUpdated && isPatientImageUpdated) {
                Logger.logD(TAG, "updated");
                Intent i = new Intent(mContext, PatientDetailActivity.class);
                i.putExtra("patientUuid", uuid);
                i.putExtra("patientName", patientDTO.getFirst_name() + " " + patientDTO.getLast_name());
                i.putExtra("tag", "newPatient");
                i.putExtra("hasPrescription", "false");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(i);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    private void updateUI(Patient patient) {

        //AlternateNo
        if (patient.getAlternateNo() != null) {
            mAlternateNumberString = patient.getAlternateNo();
            mAlternateNumber.setText(mAlternateNumberString);
        }
    /*
     Commented as per new flow

     //Wife_Daughter_Of
        if (patient.getWifeDaughterOf() != null) {
            mWifeDaughterOfString = patient.getWifeDaughterOf();
            mWifeDaughterOfEditText.setText(mWifeDaughterOfString);
        }

        //Admission_Date
        if (patient.getAdmissionDate() != null) {
            mAdmissionDateString = patient.getAdmissionDate();
            mAdmissionDateTextView.setText(mAdmissionDateString);
        }
        //Admission_Time
        if (patient.getAdmissionTime() != null) {
            mAdmissionTimeString = patient.getAdmissionTime();
            mAdmissionTimeTextView.setText(mAdmissionTimeString);
        }

        // parity
        if (patient.getParity() != null) {
            mTotalBirthCount = patient.getParity().split(",")[0];
            mTotalMiscarriageCount = patient.getParity().split(",")[1];
            mTotalBirthEditText.setText(mTotalBirthCount);
            mTotalMiscarriageEditText.setText(mTotalMiscarriageCount);
        }

        //Labor Onset
        if (patient.getLaborOnset() != null) {
            mLaborOnsetString = patient.getLaborOnset();
            if (mLaborOnsetString.equalsIgnoreCase("Spontaneous")) {
                mLaborOnsetRadioGroup.check(mLaborOnsetRadioGroup.getChildAt(0).getId());
            } else if (mLaborOnsetString.equalsIgnoreCase("Induced")) {
                mLaborOnsetRadioGroup.check(mLaborOnsetRadioGroup.getChildAt(1).getId());
            }
        }
        //When was active labor diagnosed?
        if (patient.getActiveLaborDiagnosed() != null) {
            mActiveLaborDiagnosedDate = patient.getActiveLaborDiagnosed().split(" ")[0];
            mActiveLaborDiagnosedTime = patient.getActiveLaborDiagnosed().split(" ")[1];
            mActiveLaborDiagnosedDateTextView.setText(mActiveLaborDiagnosedDate);
            mActiveLaborDiagnosedTimeTextView.setText(mActiveLaborDiagnosedTime);
        }

        //When was the membrane ruptured?
        if (patient.getMembraneRupturedTimestamp() != null) {
            if (patient.getMembraneRupturedTimestamp().equalsIgnoreCase("U")) {
                mUnknownMembraneRupturedCheckBox.setChecked(true);
            } else {
                mUnknownMembraneRupturedCheckBox.setChecked(false);
                mMembraneRupturedDate = patient.getMembraneRupturedTimestamp().split(" ")[0];
                mMembraneRupturedTime = patient.getMembraneRupturedTimestamp().split(" ")[1];
                mMembraneRupturedDateTextView.setText(mMembraneRupturedDate);
                mMembraneRupturedTimeTextView.setText(mMembraneRupturedTime);
            }
        }
        //Risk factors
        if (patient.getRiskFactors() != null) {
            mRiskFactorsString = patient.getRiskFactors();
            mRiskFactorsTextView.setText(mRiskFactorsString);
        }

        //Hospital/Maternity?
        if (patient.getHospitalMaternity() != null) {
            mOthersEditText.setVisibility(View.GONE);
            mHospitalMaternityString = patient.getHospitalMaternity();
            if (mHospitalMaternityString.equalsIgnoreCase("Hospital")) {
                mHospitalMaternityRadioGroup.check(mHospitalMaternityRadioGroup.getChildAt(0).getId());
            } else if (mHospitalMaternityString.equalsIgnoreCase("Maternity")) {
                mHospitalMaternityRadioGroup.check(mHospitalMaternityRadioGroup.getChildAt(1).getId());
            } else {
                mOthersEditText.setVisibility(View.VISIBLE);
                mOthersEditText.setText(mHospitalMaternityString);
                mOthersString = mHospitalMaternityString;
                mHospitalMaternityRadioGroup.check(mHospitalMaternityRadioGroup.getChildAt(2).getId());
            }
        }
        mOthersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mOthersString = s.toString();
                mHospitalMaternityString = mOthersString;
            }
        });

        //primaryDoctor
        Log.v(TAG, "getPrimaryDoctor"+patient.getPrimaryDoctor());
        Log.v(TAG, "getPrimaryDoctor"+patient.getPrimaryDoctor());
        if (patient.getPrimaryDoctor() != null) {
            mPrimaryDoctorUUIDString = patient.getPrimaryDoctor().split("@#@")[0];
            mPrimaryDoctorTextView.setText(patient.getPrimaryDoctor().split("@#@")[1]);
        }

        //secondaryDoctor
        if (patient.getPrimaryDoctor() != null) {
            mSecondaryDoctorUUIDString = patient.getSecondaryDoctor().split("@#@")[0];
            mSecondaryDoctorTextView.setText(patient.getSecondaryDoctor().split("@#@")[1]);
        }*/
    }

    private void setscreen(String patientUID) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection = "uuid=?";
        String[] patientArgs = {patientUID};
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
        String[] patientArgs1 = {patientUID};
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
                /*new*/
                if (name.equalsIgnoreCase("AlternateNo")) {
                    patient1.setAlternateNo(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Wife_Daughter_Of")) {
                    patient1.setWifeDaughterOf(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Admission_Date")) {
                    patient1.setAdmissionDate(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Admission_Time")) {
                    patient1.setAdmissionTime(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }


                if (name.equalsIgnoreCase("Parity")) {
                    patient1.setParity(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Labor Onset")) {
                    patient1.setLaborOnset(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Active Labor Diagnosed")) {
                    patient1.setActiveLaborDiagnosed(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Membrane Ruptured Timestamp")) {
                    patient1.setMembraneRupturedTimestamp(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Risk factors")) {
                    patient1.setRiskFactors(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Hospital_Maternity")) {
                    patient1.setHospitalMaternity(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("PrimaryDoctor")) {
                    patient1.setPrimaryDoctor(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase("SecondaryDoctor")) {
                    patient1.setSecondaryDoctor(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase("Ezazi Registration Number")) {
                    patient1.seteZaziRegNumber(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                /*end*/

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ivPersonal.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_info));
        ivAddress.setImageDrawable(getResources().getDrawable(R.drawable.ic_address_unselected));
        ivOther.setImageDrawable(getResources().getDrawable(R.drawable.ic_other_unselected));


        // next btn click
        btnSaveUpdate.setOnClickListener(v -> {
            onPatientCreateClicked();
        });
        // setting patient profile
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPerm();
            }
        });

    /* as per new flow commented
    // Gender - start
        if (mGenderMaleRadioButton.isChecked()) {
            mGender = "M";
        } else if (mGenderFemaleRadioButton.isChecked()) {
            mGender = "F";
        } else {
            mGender = "O";
        }

        mGenderFemaleRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mGenderMaleRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        mGenderOthersRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        // Gender - end*/

        // DOB - start
        mDOBPicker = new DatePickerDialog(getActivity(), R.style.datepicker,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        //Set the DOB calendar to the date selected by the user
                        dob.set(year, monthOfYear, dayOfMonth);
                        mDOB.setError(null);
                        mAge.setError(null);

                        // mDOBErrorTextView.setVisibility(View.GONE);
                        //  mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
//
///                        mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                        //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
                        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);

                        // Locale.setDefault(Locale.ENGLISH);
                        //Formatted so that it can be read the way the user sets
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
                        dob.set(year, monthOfYear, dayOfMonth);
                        String dobString = simpleDateFormat.format(dob.getTime());
                        dob_indexValue = monthOfYear; //fetching the inex value of month selected...

                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                            String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                            String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                            String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                            String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                            String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                            String dob_text = en__te_dob(dobString); //to show text of English into telugu...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                            String dob_text = en__mr_dob(dobString); //to show text of English into telugu...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                            String dob_text = en__as_dob(dobString); //to show text of English into telugu...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                            String dob_text = en__ml_dob(dobString); //to show text of English into telugu...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                            String dob_text = en__kn_dob(dobString); //to show text of English into telugu...
                            mDOB.setText(dob_text);
                        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                            String dob_text = en__ru_dob(dobString); //to show text of English into telugu...
                            mDOB.setText(dob_text);
                        } else {
                            mDOB.setText(dobString);
                        }

                        //  dob_edittext.setText(dobString);
                        mDOBYear = year;
                        mDOBMonth = monthOfYear;
                        mDOBDay = dayOfMonth;

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
                        String ageS = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " +
                                mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " +
                                mAgeDays + getResources().getString(R.string.days);
                        mAge.setText(ageS);

                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE)); // so that todays date as shown as default selection.

        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });
        // DOB - end

        // Age - start
        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAgePicker = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogStyle);
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

                    //mDOBErrorTextView.setVisibility(View.GONE);
                    // mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    // mAgeErrorTextView.setVisibility(View.GONE);
                    //  mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, -mAgeDays);
                    calendar.add(Calendar.MONTH, -mAgeMonths);
                    calendar.add(Calendar.YEAR, -mAgeYears);

                    mDOBYear = calendar.get(Calendar.YEAR);
                    mDOBMonth = calendar.get(Calendar.MONTH);
                    mDOBDay = calendar.get(Calendar.DAY_OF_MONTH);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy",
                            Locale.ENGLISH);
                    dob.set(mDOBYear, mDOBMonth, mDOBDay);
                    String dobString = simpleDateFormat.format(dob.getTime());
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String dob_text = en__te_dob(dobString); //to show text of English into telugu...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String dob_text = en__mr_dob(dobString); //to show text of English into marathi...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String dob_text = en__as_dob(dobString); //to show text of English into assame...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String dob_text = en__ml_dob(dobString);
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String dob_text = en__kn_dob(dobString); //to show text of English into kannada...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String dob_text = en__ru_dob(dobString); //to show text of English into kannada...
                        mDOB.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
                        mDOB.setText(dob_text);
                    } else {
                        mDOB.setText(dobString);
                    }

//                    dob_edittext.setText(dobString);
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
                IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);
            }
        });
        // Age - end
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            takePicture();
        }
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private void takePicture() {
        String patientTemp = "";
        if (patientUuid.equalsIgnoreCase("")) {
            patientTemp = patientDTO.getUuid();
        } else {
            patientTemp = patientUuid;
        }

        File filePath = new File(AppConstants.IMAGE_PATH + patientTemp);
        if (!filePath.exists()) {
            filePath.mkdir();
        }

        Intent cameraIntent = new Intent(getActivity(), CameraActivity.class);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patientTemp);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString());
        startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
    }

    private void onPatientCreateClicked() {

        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
                && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("")) {

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

            Toast.makeText(mContext, R.string.identification_screen_required_fields, Toast.LENGTH_LONG).show();
            return;
        }

        uuid = UUID.randomUUID().toString();

        Log.v(TAG, "reltion: " + patientID_edit);
        /*if (patientID_edit != null) {
            patientDTO.setUuid(patientID_edit);
        } else if (patientDTO.getUuid() != null){
          //  patientDTO.setUuid(uuid);
        }
        else {
            patientDTO.setUuid(uuid);
        }*/

        if (patient_detail) {
            //   patientDTO.setUuid(patientID_edit);
        } else {
            patientDTO.setUuid(uuid);
        }

/*

        //frag1_nxt_btn_main.setBackground(getResources().getDrawable(R.drawable.disabled_patient_reg_btn));
        if (mFirstName.getText().toString().equals("")) {
            mFirstNameErrorTextView.setVisibility(View.VISIBLE);
            mFirstNameErrorTextView.setText(getString(R.string.error_field_required));
            mFirstNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mFirstNameEditText.requestFocus();
            return;
        } else {
            mFirstNameErrorTextView.setVisibility(View.GONE);
            mFirstNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

       */
/* if (mMiddleNameEditText.getText().toString().equals("")) {
            mMiddleNameErrorTextView.setVisibility(View.VISIBLE);
            mMiddleNameErrorTextView.setText(getString(R.string.error_field_required));
            mMiddleNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mMiddleNameEditText.requestFocus();
            return;
        } else {
            mMiddleNameErrorTextView.setVisibility(View.GONE);
            mMiddleNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }*//*


        if (mLastNameEditText.getText().toString().equals("")) {
            mLastNameErrorTextView.setVisibility(View.VISIBLE);
            mLastNameErrorTextView.setText(getString(R.string.error_field_required));
            mLastNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mLastNameEditText.requestFocus();
            return;
        } else {
            mLastNameErrorTextView.setVisibility(View.GONE);
            mLastNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }
        // gender valid - start
        if (!mGenderFemaleRadioButton.isChecked() && !mGenderMaleRadioButton.isChecked() && !mGenderOthersRadioButton.isChecked()) {
            mGenderErrorTextView.setVisibility(View.VISIBLE);
            return;
        } else {
            mGenderErrorTextView.setVisibility(View.GONE);
        }
        // gender valid - end

        if (mDOBEditText.getText().toString().equals("")) {
            //  dob_edittext.setError(getString(R.string.error_field_required));
            mDOBErrorTextView.setVisibility(View.VISIBLE);
            mDOBErrorTextView.setText(getString(R.string.error_field_required));
            mDOBEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mDOBEditText.requestFocus();
            return;
        } else {
            mDOBErrorTextView.setVisibility(View.GONE);
            mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (mAgeEditText.getText().toString().equals("")) {
            //   age_edittext.setError(getString(R.string.error_field_required));
            mAgeErrorTextView.setVisibility(View.VISIBLE);
            mAgeErrorTextView.setText(getString(R.string.error_field_required));
            mAgeEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mAgeEditText.requestFocus();
            return;
        } else {
            mAgeErrorTextView.setVisibility(View.GONE);
            mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

        if (mPhoneNumberEditText.getText().toString().equals("")) {
            mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
            mPhoneNumberErrorTextView.setText(getString(R.string.error_field_required));
            mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mPhoneNumberEditText.requestFocus();
            return;
        } else {
            String s = mPhoneNumberEditText.getText().toString().replaceAll("\\s+", "");
            Log.v("phone", "phone: " + s);
            if (s.length() < 10) {
                mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                mPhoneNumberErrorTextView.setText(getString(R.string.enter_10_digits));
                mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mPhoneNumberEditText.requestFocus();
                return;
            } else {
                mPhoneNumberErrorTextView.setVisibility(View.GONE);
                mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }

            if (mCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91")
                    && s.length() > 10) {
                mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                mPhoneNumberErrorTextView.setText(R.string.invalid_mobile_no);
                mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mPhoneNumberEditText.requestFocus();
                return;
            } else {
                mPhoneNumberErrorTextView.setVisibility(View.GONE);
                mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
            // Indian mobile number max
        }

        // mobile no - start

        // mobile no - end
*/

        if (mCurrentPhotoPath != null)
            patientDTO.setPatientPhoto(mCurrentPhotoPath);
        else
            patientDTO.setPatientPhoto(patientDTO.getPatientPhoto());

        patientDTO.setFirstname(mFirstName.getText().toString());
        patientDTO.setMiddlename(mMiddleName.getText().toString());
        patientDTO.setLastname(mLastName.getText().toString());
        patientDTO.setPhonenumber(mMobileNumber.getText().toString());

        // patientDTO.setGender(StringUtils.getValue(mGender));

        //get unformatted number with prefix "+" i.e "+14696641766"
        //   patientDTO.setPhonenumber(StringUtils.getValue(countryCodePicker.getFullNumberWithPlus()));


        ///////////// patientDTO.setPhonenumber(StringUtils.getValue(mCountryCodePicker.getFullNumberWithPlus())); // automatically combines both cc and number togther.

        String[] dob_array = mDOB.getText().toString().split(" ");
        Log.d("dob_array", "0: " + dob_array[0]);
        Log.d("dob_array", "0: " + dob_array[1]);
        Log.d("dob_array", "0: " + dob_array[2]);

        //get month index and return English value for month.
        if (dob_indexValue == 15) {
            String dob = StringUtils.hi_or_bn_en_noEdit
                    (mDOB.getText().toString(), sessionManager.getAppLanguage());
            patientDTO.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                    (StringUtils.getValue(dob)));
        } else {
            String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
            dob_array[1] = dob_array[1].replace(dob_array[1], dob);
            String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];
            patientDTO.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                    (StringUtils.getValue(dob_value)));
            Log.d(TAG, "onPatientCreateClicked: dob : " + DateAndTimeUtils.getFormatedDateOfBirth
                    (StringUtils.getValue(dob_value)));

        }

        if (patientDTO != null) {
            Log.d(TAG, "onPatientCreateClicked: not null");


            //check new flow
            Log.d(TAG, "11initUI: firstname personal:  " + patientDTO.getFirstname());
            Log.d(TAG, "11initUI: lastname personal: " + patientDTO.getLastname());
            Log.d(TAG, "11initUI: middlename personal: " + patientDTO.getMiddlename());
            Log.d(TAG, "11initUI: dob personal: " + patientDTO.getDateofbirth());
            Log.d(TAG, "11initUI: phoneno: personal " + patientDTO.getPhonenumber());
            Log.d(TAG, "11initUI: patient_detail personal: " + patient_detail);

            // Bundle data
            Bundle bundle = new Bundle();
            bundle.putSerializable("patientDTO", (Serializable) patientDTO);
            bundle.putBoolean("fromFirstScreen", true);
            bundle.putBoolean("patient_detail", patient_detail);
            //   bundle.putString("patientUuid", patientID_edit);
            fragment_secondScreen.setArguments(bundle); // passing data to Fragment

            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_add_patient, fragment_secondScreen)
                    .commit();
            // end
        } else {
            Log.d(TAG, "onPatientCreateClicked: patientdao is null");
        }

    }

}

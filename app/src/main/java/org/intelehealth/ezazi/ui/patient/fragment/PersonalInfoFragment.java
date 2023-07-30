package org.intelehealth.ezazi.ui.patient.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.addNewPatient.AddNewPatientActivity;
import org.intelehealth.ezazi.activities.addNewPatient.ErrorManagerModel;
import org.intelehealth.ezazi.activities.addNewPatient.PatientAddressInfoFragment;
import org.intelehealth.ezazi.activities.addNewPatient.PatientPersonalInfoFragment;
import org.intelehealth.ezazi.activities.cameraActivity.CameraActivity;
import org.intelehealth.ezazi.activities.setupActivity.SetupActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.ImagesDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.databinding.FragmentPatientPersonalInfoBinding;
import org.intelehealth.ezazi.models.Patient;
import org.intelehealth.ezazi.models.dto.PatientAttributesModel;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.models.dto.ProviderDTO;
import org.intelehealth.ezazi.ui.InputChangeValidationListener;
import org.intelehealth.ezazi.ui.dialog.CalendarDialog;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.validation.FirstLetterUpperCaseInputFilter;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.EditTextUtils;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.IReturnValues;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidGenerator;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Vaghela Mithun R. on 28-07-2023 - 20:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class PersonalInfoFragment extends PatientBaseFragment {
    private static final String TAG = "PersonalInfoFragment";
    private FragmentPatientPersonalInfoBinding binding;
    private List<ProviderDTO> mProviderDoctorList = new ArrayList<>();
    private Patient patient1 = new Patient();
    private boolean hasLicense = false;
    private PatientDTO patientDTO = new PatientDTO();
    private SessionManager sessionManager;


    public static PersonalInfoFragment getInstance() {
        return new PersonalInfoFragment();
    }

    private boolean mIsEditMode = false;
    private String patientID_edit;
    boolean fromSummary;
    private String patientUuid = "";
    private String privacy_value;
    private String mAlternateNumberString = "";
    private PatientsDAO patientsDAO = new PatientsDAO();
    private boolean fromSecondScreen = false;
    private PatientAddressInfoFragment fragment_secondScreen;
    private boolean patient_detail = false;
    private boolean editDetails = false;
    private String mCurrentPhotoPath;
    int MY_REQUEST_CODE = 5555;

    private PatientAttributesModel patientAttributesModel;
    private NestedScrollView scrollviewPersonalInfo;

    private PersonalInfoFragment() {
        super(R.layout.fragment_patient_personal_info);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentPatientPersonalInfoBinding.bind(view);
        updateLocale();
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
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    private void initUI() {
        ProviderDAO providerDAO = new ProviderDAO();
        try {
            mProviderDoctorList = providerDAO.getDoctorList();
        } catch (DAOException e) {
            e.printStackTrace();
        }

        privacy_value = requireActivity().getIntent().getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        binding.etDob.setShowSoftInputOnFocus(false);
        binding.etAge.setShowSoftInputOnFocus(false);

        binding.etFirstName.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etLastName.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etMiddleName.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});

        setTextChangeWatcher();

        setDetailsAsPerConfigFile();
        updatePatientDetailsFromSummary();
        updatePatientDetailsFromSecondScreen();
        handleClickListeners();
    }

    private void setTextChangeWatcher() {
        new InputChangeValidationListener(binding.inputLayoutFname, text -> !TextUtils.isEmpty(text))
                .validate(getString(R.string.enter_first_name));

        new InputChangeValidationListener(binding.inputLayoutLname, text -> !TextUtils.isEmpty(text))
                .validate(getString(R.string.enter_last_name));

        new InputChangeValidationListener(binding.etLayoutDob, text -> !TextUtils.isEmpty(text))
                .validate(getString(R.string.select_dob));

        new InputChangeValidationListener(binding.etLayoutAge, text -> !TextUtils.isEmpty(text))
                .validate(getString(R.string.select_age));

        new InputChangeValidationListener(binding.inputLayoutMobile, text -> !TextUtils.isEmpty(text) && text.length() != 10)
                .validate(getString(R.string.mobile_no_length));

        new InputChangeValidationListener(binding.inputLayoutAlternateMobile, text -> !TextUtils.isEmpty(text) && text.length() != 10)
                .validate(getString(R.string.mobile_no_length));
    }

    private void setDetailsAsPerConfigFile() {
        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, requireContext()),
                        String.valueOf(FileUtils.encodeJSON(requireContext(), AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(requireContext(), AppConstants.CONFIG_FILE_NAME)));
            }

            //Display the fields on the Add Patient screen as per the config file
            binding.inputLayoutFname.setVisibility(obj.getBoolean("mFirstName") ? View.VISIBLE : View.GONE);
            binding.inputLayoutMname.setVisibility(obj.getBoolean("mMiddleName") ? View.VISIBLE : View.GONE);
            binding.inputLayoutLname.setVisibility(obj.getBoolean("mLastName") ? View.VISIBLE : View.GONE);
            binding.etLayoutDob.setVisibility(obj.getBoolean("mDOB") ? View.VISIBLE : View.GONE);
            binding.inputLayoutMobile.setVisibility(obj.getBoolean("mPhoneNum") ? View.VISIBLE : View.GONE);
            binding.inputLayoutAlternateMobile.setVisibility(obj.getBoolean("mPhoneNum") ? View.VISIBLE : View.GONE);
            binding.inputLayoutMobile.setVisibility(obj.getBoolean("mAge") ? View.VISIBLE : View.GONE);

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            //            Issue #627
            //            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(requireContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
            showAlertDialogButtonClicked(e.toString());
        }

    }

    private void handleClickListeners() {
        binding.etLayoutDob.setEndIconOnClickListener(v -> selectDob());
        binding.etLayoutDob.setOnClickListener(v -> selectDob());
        binding.etLayoutAge.setEndIconOnClickListener(v -> {
        });
    }

    private void selectDob() {
        boolean isTable = getResources().getBoolean(R.bool.isTabletSize);
        int maxHeight = getResources().getDimensionPixelOffset(R.dimen.std_430dp);
        CalendarDialog dialog = new CalendarDialog
                .Builder(requireContext())
                .title("")
                .positiveButtonLabel(R.string.ok)
                .maxHeight(!isTable ? maxHeight : 0)
                .build();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -10);
        dialog.setMaxDate(calendar.getTimeInMillis());
        Log.d(TAG, "selectDob: " + calendar.getTime());

        dialog.setListener((day, month, year, value) -> {
            Log.e(TAG, "Date = >" + value);
            //dd/mm/yyyy
            String selectedDate = value;
            String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(selectedDate);
            if (!selectedDate.isEmpty()) {
                String dobToDb = DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate);
                String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(dobToDb).split(" ");
                int ageYears = Integer.parseInt(ymdData[0]);
                int ageMonths = Integer.parseInt(ymdData[1]);
                int ageDays = Integer.parseInt(ymdData[2]);

                // String age = DateAndTimeUtils.formatAgeInYearsMonthsDate(getContext(), mAgeYears, mAgeMonths, mAgeDays);
                String[] splitedDate = selectedDate.split("/");
                binding.etDob.setText(dateToshow1 + " " + splitedDate[2]);
                patientDTO.setDateofbirth(dobToDb);
                if (ageYears < 9) {
                    binding.etAge.setText(ageYears + "");
                    binding.etAge.setText("");
                    binding.etDob.setText("");
                    binding.etLayoutDob.setErrorEnabled(true);
                    binding.etLayoutDob.setError(getString(R.string.select_age));

                } else {
                    binding.etAge.setText(ageYears + "");
                }
                Log.d(TAG, "getSelectedDate: " + dateToshow1 + ", " + splitedDate[2]);
                setSelectedDob(requireContext(), dobToDb);
            } else {
                Log.d(TAG, "onClick: date empty");
            }
        });
        dialog.show(requireFragmentManager(), "DatePicker");

    }


    private void updatePatientDetailsFromSecondScreen() {
        fragment_secondScreen = new PatientAddressInfoFragment();
        if (getArguments() != null) {
            patientDTO = (PatientDTO) getArguments().getSerializable("patientDTO");
            // patientID_edit = getArguments().getString("patientUuid");
            patient_detail = getArguments().getBoolean("patient_detail");
            fromSecondScreen = getArguments().getBoolean("fromSecondScreen");
            mAlternateNumberString = getArguments().getString("mAlternateNumberString");
            editDetails = getArguments().getBoolean("editDetails");
            patientAttributesModel = (PatientAttributesModel) getArguments().getSerializable("patientAttributes");

            patientDTO.setAlternateNo(mAlternateNumberString);

            updateUI(patient1);


            if (fromSecondScreen) {
                Log.d(TAG, "initUI: fn : " + patientDTO.getFirstname());
                binding.etFirstName.setText(patientDTO.getFirstname());
                binding.etMiddleName.setText(patientDTO.getMiddlename());
                binding.etLastName.setText(patientDTO.getLastname());
                binding.etMobileNo.setText(patientDTO.getPhonenumber());
                Log.d(TAG, "updatePatientDetailsFromSecondScreen: phone : " + patientDTO.getPhonenumber());
                binding.etAlternateMobile.setText(mAlternateNumberString);

                Log.d(TAG, "initUI: dob from dto : " + patientDTO.getDateofbirth());
                String dateOfBirth = getSelectedDob(requireContext());
                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                    ///String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patientDTO.getDateofbirth());
                    String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(dateOfBirth);

                    Log.d(TAG, "initUI: dob : " + dob);
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                        binding.etLastName.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String dob_text = en__te_dob(dob); //to show text of English into Telugu...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String dob_text = en__mr_dob(dob); //to show text of English into marathi...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String dob_text = en__as_dob(dob); //to show text of English into assame...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String dob_text = en__ml_dob(dob); //to show text of English into malyalum...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String dob_text = en__kn_dob(dob); //to show text of English into kannada...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String dob_text = en__ru_dob(dob); //to show text of English into kannada...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String dob_text = en__gu_dob(dob); //to show text of English into Gujarati...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String dob_text = en__bn_dob(dob); //to show text of English into Bengali...
                        binding.etDob.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String dob_text = en__ta_dob(dob); //to show text of English into Tamil...
                        binding.etDob.setText(dob_text);
                    } else {
                        binding.etDob.setText(dob);
                    }

                    // dob_edittext.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
                    //get year month days
                    // String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patientDTO.getDateofbirth(), getActivity());

                    // String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patientDTO.getDateofbirth()).split(" ");
                    String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(dateOfBirth).split(" ");
                    int mAgeYears = Integer.valueOf(ymdData[0]);
                    //  mAgeMonths = Integer.valueOf(ymdData[1]);
                    // mAgeDays = Integer.valueOf(ymdData[2]);
          /*  String age = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " +
                    mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " +
                    mAgeDays + getResources().getString(R.string.days);*/

                    if (mAgeYears < 9) {
                        binding.etAge.setText("");
                        binding.etDob.setText("");
                        binding.etLayoutAge.setErrorEnabled(true);
                        binding.etLayoutAge.setError(getString(R.string.select_age));
                    } else {
                        binding.etAge.setText(mAgeYears + "");
                    }
                }


                // profile image edit
                if (patientDTO.getPatientPhoto() != null && !patientDTO.getPatientPhoto().trim().isEmpty()) {
                    //  patient_imgview.setImageBitmap(BitmapFactory.decodeFile(patientDTO.getPatientPhoto()));
                    Glide.with(getActivity()).load(new File(patientDTO.getPatientPhoto()))
                            .thumbnail(0.25f)
                            .centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).into(binding.ivProfilePhoto);
                }
            }

            if (patient1.getPatient_photo() != null && !patient1.getPatient_photo().trim().isEmpty())
                binding.ivProfilePhoto.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatient_photo()));

           /*

           temp commit//one time generation of uuid
            if (null == patientID_edit || patientID_edit.isEmpty()) {
                generateUuid();
            }*/
        }
    }

    public String getSelectedDob(Context context) {
        String access = "dobPatient";
        SharedPreferences prefs = context.getSharedPreferences(access, MODE_PRIVATE);
        String accdate = prefs.getString("dobPatient", "");
        return accdate;
    }

    public void setSelectedDob(Context context, String dob) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("dobPatient", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("dobPatient", dob);
        editor.apply();
    }


    private void setClickListener() {
        binding.btnSaveUpdateFirst.setOnClickListener(v -> {
            onPatientCreateClicked();
        });
        // setting patient profile
        binding.fabUpdatePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });


        // Age - start
//        binding.etLayoutAge.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAgePicker = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogStyle);
//                mAgePicker.setTitle(R.string.identification_screen_prompt_age);
//                final LayoutInflater inflater = getLayoutInflater();
//                View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
//                mAgePicker.setView(convertView);
//                NumberPicker yearPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
//                NumberPicker monthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
//                NumberPicker dayPicker = convertView.findViewById(R.id.dialog_3_numbers_unit);
//                dayPicker.setVisibility(View.VISIBLE);
//
//                final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
//                final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
//                final TextView dayTv = convertView.findViewById(R.id.dialog_2_numbers_text_3);
//                dayPicker.setVisibility(View.VISIBLE);
//
//                int totalDays = today.getActualMaximum(Calendar.DAY_OF_MONTH);
//                dayTv.setText(getString(R.string.days));
//                middleText.setText(getString(R.string.identification_screen_picker_years));
//                endText.setText(getString(R.string.identification_screen_picker_months));
//
//
//                yearPicker.setMinValue(0);
//                yearPicker.setMaxValue(100);
//                monthPicker.setMinValue(0);
//                monthPicker.setMaxValue(12);
//
//                dayPicker.setMinValue(0);
//                dayPicker.setMaxValue(31);
//
//                EditText yearText = yearPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
//                EditText monthText = monthPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
//                EditText dayText = dayPicker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
//
//
//                yearPicker.setValue(mAgeYears);
//                monthPicker.setValue(mAgeMonths);
//                dayPicker.setValue(mAgeDays);
//
//                //year
//                EditTextUtils.returnEditextValues(new IReturnValues() {
//                    @Override
//                    public void onReturnValue(String value) {
//                        mAgeYears = Integer.valueOf(value);
//                    }
//                }, yearText);
//
//                //month
//                EditTextUtils.returnEditextValues(new IReturnValues() {
//                    @Override
//                    public void onReturnValue(String value) {
//                        mAgeMonths = Integer.valueOf(value);
//                    }
//                }, monthText);
//
//                //day
//                EditTextUtils.returnEditextValues(new IReturnValues() {
//                    @Override
//                    public void onReturnValue(String value) {
//                        mAgeDays = Integer.valueOf(value);
//                    }
//                }, dayText);
//
//
//                mAgePicker.setPositiveButton(R.string.generic_ok, (dialog, which) -> {
//                /*    String ageString = mAgeYears + getString(R.string.identification_screen_text_years) + " - " +
//                            mAgeMonths + getString(R.string.identification_screen_text_months) + " - " +
//                            mAgeDays + getString(R.string.days);*/
//                    String ageString = mAgeYears + getString(R.string.identification_screen_text_years);
//                    /// temp commit k  mAge.setText(ageString);
//
//                    //mDOBErrorTextView.setVisibility(View.GONE);
//                    // mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
//
//                    // mAgeErrorTextView.setVisibility(View.GONE);
//                    //  mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
//
//                 /*
//                   temp commit
//
//
//                   Calendar calendar = Calendar.getInstance();
//                    calendar.add(Calendar.DAY_OF_MONTH, -mAgeDays);
//                    calendar.add(Calendar.MONTH, -mAgeMonths);
//                    calendar.add(Calendar.YEAR, -mAgeYears);
//
//                    mDOBYear = calendar.get(Calendar.YEAR);
//                    mDOBMonth = calendar.get(Calendar.MONTH);
//                    mDOBDay = calendar.get(Calendar.DAY_OF_MONTH);
//
//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy",
//                            Locale.ENGLISH);
//                    dob.set(mDOBYear, mDOBMonth, mDOBDay);
//                    String dobString = simpleDateFormat.format(dob.getTime());
//                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
//                        String dob_text = en__hi_dob(dobString); //to show text of English into Hindi...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
//                        String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
//                        String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
//                        String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
//                        String dob_text = en__te_dob(dobString); //to show text of English into telugu...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
//                        String dob_text = en__mr_dob(dobString); //to show text of English into marathi...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
//                        String dob_text = en__as_dob(dobString); //to show text of English into assame...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
//                        String dob_text = en__ml_dob(dobString);
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
//                        String dob_text = en__kn_dob(dobString); //to show text of English into kannada...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
//                        String dob_text = en__ru_dob(dobString); //to show text of English into kannada...
//                        mDOB.setText(dob_text);
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
//                        String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
//                        mDOB.setText(dob_text);
//                    } else {
//                        mDOB.setText(dobString);
//                    }
//
////                    dob_edittext.setText(dobString);
//                    mDOBPicker.updateDate(mDOBYear, mDOBMonth, mDOBDay);
//                    dialog.dismiss();*/
//                });
//                mAgePicker.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
//                AlertDialog alertDialog = mAgePicker.show();
//                IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);
//            }
//        });
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
        if (!areValidFields()) {
            setScrollToFocusedItem();
            return;
        }
        patientUuid = UUID.randomUUID().toString();

        if (patient_detail) {
            //   patientDTO.setUuid(patientID_edit);
        } else {
            patientDTO.setUuid(patientUuid);
        }


        if (patientDTO != null) {

            if (mCurrentPhotoPath != null) patientDTO.setPatientPhoto(mCurrentPhotoPath);
            else patientDTO.setPatientPhoto(patientDTO.getPatientPhoto());

            patientDTO.setFirstname(binding.etFirstName.getText().toString());
            patientDTO.setMiddlename(binding.etMiddleName.getText().toString());
            patientDTO.setLastname(binding.etLastName.getText().toString());
            patientDTO.setPhonenumber(binding.etMobileNo.getText().toString());
            patientDTO.setDateofbirth(binding.etDob.getText().toString());

            // Bundle data
            Bundle bundle = new Bundle();
            bundle.putSerializable("patientDTO", (Serializable) patientDTO);
            bundle.putBoolean("fromFirstScreen", true);
            bundle.putBoolean("patient_detail", patient_detail);
            bundle.putString("patientUuidUpdate", patientID_edit);
            bundle.putString("mAlternateNumberString", binding.etAlternateMobile.getText().toString());
            bundle.putBoolean("editDetails", true);
            bundle.putBoolean("fromSummary", fromSummary);
            bundle.putSerializable("patientAttributes", (Serializable) patientAttributesModel);


            fragment_secondScreen.setArguments(bundle); // passing data to Fragment

            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_add_patient, fragment_secondScreen).commit();
            ((AddNewPatientActivity) requireActivity()).changeCurrentPage(AddNewPatientActivity.PAGE_ADDRESS);
            // end
        } else {
            Log.d(TAG, "onPatientCreateClicked: patientdao is null");
        }

    }

    private boolean areValidFields() {
        boolean valid = true;
        if (TextUtils.isEmpty(binding.etFirstName.getText())) {
            binding.inputLayoutFname.setError(getString(R.string.enter_first_name));
            binding.inputLayoutFname.setErrorEnabled(true);
            valid = false;
        } else {
            binding.inputLayoutFname.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(binding.etLastName.getText())) {
            binding.inputLayoutLname.setError(getString(R.string.enter_last_name));
            binding.inputLayoutLname.setErrorEnabled(true);
            valid = false;
        } else {
            binding.inputLayoutLname.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(binding.etDob.getText().toString())) {
            binding.etLayoutDob.setError(getString(R.string.select_dob));
            binding.etLayoutDob.setErrorEnabled(true);
            valid = false;
        } else {
            binding.etLayoutDob.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(binding.etAge.getText().toString())) {
            binding.etLayoutAge.setError(getString(R.string.select_age));
            binding.etLayoutAge.setErrorEnabled(true);
            valid = false;
        } else {
            binding.etLayoutAge.setErrorEnabled(false);
        }

        String mobileNumber = binding.etMobileNo.getText().toString();
        if (!mobileNumber.isEmpty() && mobileNumber.length() != 10) {
            binding.inputLayoutMobile.setError(getString(R.string.mobile_no_length));
            binding.inputLayoutMobile.setErrorEnabled(true);
            valid = false;
        } else {
            binding.inputLayoutMobile.setErrorEnabled(false);
        }

        String alternateMobileNumber = binding.etAlternateMobile.getText().toString();
        if (!alternateMobileNumber.isEmpty() && alternateMobileNumber.length() != 10) {
            binding.inputLayoutAlternateMobile.setError(getString(R.string.mobile_no_length));
            binding.inputLayoutAlternateMobile.setErrorEnabled(true);
            valid = false;
        } else {
            binding.inputLayoutAlternateMobile.setErrorEnabled(false);
        }

        if (binding.etFirstName.getText().toString().isEmpty()
                && binding.etLastName.getText().toString().isEmpty()
                && binding.etDob.getText().toString().isEmpty()
                && binding.etAge.getText().toString().isEmpty()) {
            valid = false;
            Toast.makeText(requireContext(), getResources().getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    private void updateUI(Patient patient) {
        if (patient.getAlternateNo() != null) {
            binding.etAlternateMobile.setText(patient.getAlternateNo());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Result Received");
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            Log.v(TAG, "Request Code " + CameraActivity.TAKE_IMAGE);
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Result OK");
                mCurrentPhotoPath = data.getStringExtra("RESULT");
                Log.v("IdentificationActivity", mCurrentPhotoPath);

                Glide.with(getActivity()).load(new File(mCurrentPhotoPath))
                        .thumbnail(0.25f).centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(binding.ivProfilePhoto);
            }
        } else if (requestCode == MY_REQUEST_CODE) {
            // getSelectedDate(data);
        }
    }

    private void updatePatientDetailsFromSummary() {
        //edit patient
        Intent intent = requireActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra("fromSummary")) {
                mIsEditMode = true;
                patientID_edit = intent.getStringExtra("patientUuid");
                Log.d(TAG, "updatePatientDetailsFromSummary: patientID_edit : " + patientID_edit);

                fromSummary = intent.getBooleanExtra("fromSummary", false);
                if (fromSummary) {
                    patient1.setUuid(patientID_edit);
                    setscreen(patientID_edit);
                    updateUI(patient1);
                }

            }
        }
    }

    private void setscreen(String patientUID) {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        String patientSelection = "uuid=?";
        String[] patientArgs = {patientUID};
        String[] patientColumns = {"uuid", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "city_village", "state_province", "postal_code", "country", "phone_number", "gender", "sdw", "occupation", "patient_photo", "economic_status", "education_status", "caste"};
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

        binding.etFirstName.setText(patientDTO.getFirstname());
        binding.etMiddleName.setText(patientDTO.getMiddlename());
        binding.etLastName.setText(patientDTO.getLastname());
        binding.etMobileNo.setText(patientDTO.getPhonenumber());
        Log.d(TAG, "updatePatientDetailsFromSecondScreen: phone : " + patientDTO.getPhonenumber());
        binding.etAlternateMobile.setText(mAlternateNumberString);

        binding.etFirstName.setText(patient1.getFirst_name());
        binding.etMiddleName.setText(patient1.getMiddle_name());
        binding.etLastName.setText(patient1.getLast_name());
        binding.etMobileNo.setText(patient1.getPhone_number());
        binding.etAlternateMobile.setText(patient1.getAlternateNo());

        String mCurrentPhotoPath = patient1.getPatient_photo();
        Log.d(TAG, "setscreen: dob :" + patient1.getDate_of_birth());

        String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth());
        binding.etDob.setText(dob);
        //for age
        String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth()).split(" ");
        binding.etAge.setText(Integer.parseInt(ymdData[0]) + "");

        if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
            Glide.with(getActivity()).load(new File(mCurrentPhotoPath))
                    .thumbnail(0.25f).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(binding.ivProfilePhoto);
        }

        patientDTO.setCityvillage(patient1.getCity_village());
        patientDTO.setStateprovince(patient1.getState_province());
        patientDTO.setCountry(patient1.getCountry());
        patientDTO.setAddress1(patient1.getAddress1());
        patientDTO.setAddress2(patient1.getAddress2());
        patientDTO.setPostalcode(patient1.getPostal_code());

    }

    public void showAlertDialogButtonClicked(String errorMessage) {

        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(requireContext())
                .title("Config Error")
                .content(errorMessage)
                .positiveButtonLabel(R.string.ok)
                .hideNegativeButton(true).build();

        dialog.setListener(() -> {
            Intent i = new Intent(requireContext(), SetupActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// This flag ensures all activities on top of the CloseAllViewsDemo are cleared.
            startActivity(i);
        });

        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
    }

    @Override
    public void onFocusedViewChanged(int y) {

    }
}

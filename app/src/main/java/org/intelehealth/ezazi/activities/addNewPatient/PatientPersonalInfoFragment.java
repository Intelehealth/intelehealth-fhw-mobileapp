package org.intelehealth.ezazi.activities.addNewPatient;

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
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.cameraActivity.CameraActivity;
import org.intelehealth.ezazi.activities.setupActivity.SetupActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.customCalendar.CustomCalendarViewUI2;
import org.intelehealth.ezazi.database.dao.ImagesDAO;
import org.intelehealth.ezazi.database.dao.ImagesPushDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.databinding.FragmentPatientPersonalInfoBinding;
import org.intelehealth.ezazi.models.Patient;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 *
 */
public class PatientPersonalInfoFragment extends Fragment {
    public PatientPersonalInfoFragment(){
        super(R.layout.fragment_patient_personal_info);
    }

    private static final String TAG = "PatientPersonalInfoFrag";

    public static PatientPersonalInfoFragment getInstance() {
        return new PatientPersonalInfoFragment();
    }

    private SessionManager sessionManager = null;
    private List<ProviderDTO> mProviderDoctorList = new ArrayList<ProviderDTO>();
    private String BlockCharacterSet_Others = "0123456789\\@$!=><&^*+€¥£`~";
    private String BlockCharacterSet_Name = "\\@$!=><&^*+\"\'€¥£`~";
    private boolean mIsEditMode = false;
    private String patientID_edit;
    boolean fromSummary;
    private Patient patient1 = new Patient();
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
    private Calendar today = Calendar.getInstance();
    private Calendar dob = Calendar.getInstance();
    private int dob_indexValue = 15;
    private MaterialAlertDialogBuilder mAgePicker;
    private PatientDTO patientDTO = new PatientDTO();
    private String mCurrentPhotoPath;
    private String privacy_value;
    private String mAlternateNumberString = "";
    private PatientsDAO patientsDAO = new PatientsDAO();
    private boolean fromSecondScreen = false;
    private PatientAddressInfoFragment fragment_secondScreen;
    private boolean patient_detail = false;
    private boolean editDetails = false;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    int MY_REQUEST_CODE = 5555;
    private String dobToDb;
    //    TextView tvPersonalInfo, tvAddressInfo, tvOtherInfo;
    private PatientAttributesModel patientAttributesModel;

    private FragmentPatientPersonalInfoBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentPatientPersonalInfoBinding.bind(view);
        updateLocale();
        initUI();
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
        //  sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

    }

    private void initUI() {
//        ivPersonal = getActivity().findViewById(R.id.iv_personal_info);
//        ivAddress = getActivity().findViewById(R.id.iv_address_info);
//        ivOther = getActivity().findViewById(R.id.iv_other_info);
//        tvPersonalInfo = getActivity().findViewById(R.id.tv_personal_info);
//        tvAddressInfo = getActivity().findViewById(R.id.tv_address_info);
//        tvOtherInfo = getActivity().findViewById(R.id.tv_other_info);


        /*new*/
        ProviderDAO providerDAO = new ProviderDAO();
        try {
            mProviderDoctorList = providerDAO.getDoctorList();
        } catch (DAOException e) {
            e.printStackTrace();
        }

        privacy_value = getActivity().getIntent().getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.etDob.setShowSoftInputOnFocus(false);
            binding.etDob.setShowSoftInputOnFocus(false);
        }

     /*   mFirstName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mMiddleName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mLastName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
*/
        binding.etFirstName.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etMiddleName.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        binding.etLastName.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});

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

    private void handleClickListeners() {
        binding.etLayoutDob.setEndIconOnClickListener(v -> selectDob());
        binding.etDob.setOnClickListener(v -> selectDob());
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
                dobToDb = DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate);
//            String age = DateAndTimeUtils.getAge_FollowUp(DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate), getActivity());
                //for age
                String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(dobToDb).split(" ");
                mAgeYears = Integer.parseInt(ymdData[0]);
                mAgeMonths = Integer.parseInt(ymdData[1]);
                mAgeDays = Integer.parseInt(ymdData[2]);

                // String age = DateAndTimeUtils.formatAgeInYearsMonthsDate(getContext(), mAgeYears, mAgeMonths, mAgeDays);
                String[] splitedDate = selectedDate.split("/");
                binding.etDob.setText(dateToshow1 + " " + splitedDate[2]);
                patientDTO.setDateofbirth(dobToDb);
                if (mAgeYears < 9) {
                    binding.etAge.setText("");
                    binding.etDob.setText("");
                    binding.etLayoutAge.setErrorEnabled(true);
                    binding.etLayoutAge.setError(getString(R.string.select_age));
                } else {
                    binding.etAge.setText(mAgeYears + "");
                    binding.etLayoutAge.setErrorEnabled(false);
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

    public void generateUuid() {
        patientUuid = uuidGenerator.UuidGenerator();
    }

    private void updateUI(Patient patient) {
        if (patient.getAlternateNo() != null) {
            binding.etAlternateMobile.setText(patient.getAlternateNo());
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        ivPersonal.setImageDrawable(getResources().getDrawable(R.drawable.ic_personal_info));
//        ivAddress.setImageDrawable(getResources().getDrawable(R.drawable.ic_address_unselected));
//        ivOther.setImageDrawable(getResources().getDrawable(R.drawable.ic_other_unselected));
//        tvPersonalInfo.setTextColor(getResources().getColor(R.color.colorPrimary));
//        tvAddressInfo.setTextColor(getResources().getColor(R.color.darkGray));
//        tvOtherInfo.setTextColor(getResources().getColor(R.color.darkGray));

        // next btn click
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
        binding.etLayoutAge.setOnClickListener(new View.OnClickListener() {
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
                /*    String ageString = mAgeYears + getString(R.string.identification_screen_text_years) + " - " +
                            mAgeMonths + getString(R.string.identification_screen_text_months) + " - " +
                            mAgeDays + getString(R.string.days);*/
                    String ageString = mAgeYears + getString(R.string.identification_screen_text_years);
                    /// temp commit k  mAge.setText(ageString);

                    //mDOBErrorTextView.setVisibility(View.GONE);
                    // mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    // mAgeErrorTextView.setVisibility(View.GONE);
                    //  mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                 /*
                   temp commit


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
                    dialog.dismiss();*/
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
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
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
//    private boolean areValidFields() {
//
//        List<ErrorManagerModel> errorDetailsList = new ArrayList<>();
//        if (TextUtils.isEmpty(binding.etFirstName.getText().toString())) {
//            errorDetailsList.add(new ErrorManagerModel(binding.etFirstName, tvErrorFirstName, getString(R.string.enter_first_name), cardFirstName));
//
//        } else {
//            tvErrorFirstName.setVisibility(View.GONE);
//            cardFirstName.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//        }
//        if (TextUtils.isEmpty(mLastName.getText().toString())) {
//       /*     mLastName.requestFocus();
//            tvErrorLastName.setVisibility(View.VISIBLE);
//            tvErrorLastName.setText(getString(R.string.enter_last_name));
//            cardLastName.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));*/
//            errorDetailsList.add(new ErrorManagerModel(mLastName, tvErrorLastName, getString(R.string.enter_last_name), cardLastName));
//
//        } else {
//            tvErrorLastName.setVisibility(View.GONE);
//            cardLastName.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//
//        }
//
//        if (TextUtils.isEmpty(mDOB.getText().toString())) {
//            /*mDOB.requestFocus();
//
//            tvErrorDob.setVisibility(View.VISIBLE);
//            tvErrorDob.setText(getString(R.string.select_dob));
//            cardDob.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));*/
//            errorDetailsList.add(new ErrorManagerModel(mDOB, tvErrorDob, getString(R.string.select_dob), cardDob));
//
//        } else {
//            tvErrorDob.setVisibility(View.GONE);
//            cardDob.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//        }
//        if (TextUtils.isEmpty(binding.etAge.getText().toString())) {
//         /*   mAge.requestFocus();
//
//            tvErrorAge.setVisibility(View.VISIBLE);
//            tvErrorAge.setText(getString(R.string.select_age));
//            cardAge.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));*/
//            errorDetailsList.add(new ErrorManagerModel(binding.etAge, tvErrorAge, getString(R.string.select_age), cardAge));
//
//        } else {
//            tvErrorAge.setVisibility(View.GONE);
//            cardAge.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//        }
//        String mobileNumber = mMobileNumber.getText().toString();
//        if (!mobileNumber.isEmpty() && mobileNumber.length() != 10) {
//          /*  mMobileNumber.requestFocus();
//
//            tvErrorMobileNo.setVisibility(View.VISIBLE);
//            tvErrorMobileNo.setText(getString(R.string.mobile_no_length));
//            cardMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));*/
//            errorDetailsList.add(new ErrorManagerModel(mMobileNumber, tvErrorMobileNo, getString(R.string.mobile_no_length), cardMobileNumber));
//
//        } else {
//            tvErrorMobileNo.setVisibility(View.GONE);
//            cardMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//        }
//
//        String alternateMobileNumber = mAlternateNumber.getText().toString();
//
//        if (!alternateMobileNumber.isEmpty() && alternateMobileNumber.length() != 10) {
//         /*   mAlternateNumber.requestFocus();
//
//            tvErrAlternateMobileNo.setVisibility(View.VISIBLE);
//            tvErrAlternateMobileNo.setText(getString(R.string.mobile_no_length));
//            cardAlternateMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//*/
//            errorDetailsList.add(new ErrorManagerModel(mAlternateNumber, tvErrAlternateMobileNo, getString(R.string.mobile_no_length), cardAlternateMobileNumber));
//
//        } else {
//            tvErrAlternateMobileNo.setVisibility(View.GONE);
//            cardAlternateMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//        }
//
//        if (binding.etFirstName.getText().toString().isEmpty() && mLastName.getText().toString().isEmpty() && mDOB.getText().toString().isEmpty() && mAge.getText().toString().isEmpty()) {
//            Toast.makeText(mContext, getResources().getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
//        }
//        if (errorDetailsList.size() > 0) {
//            for (int i = 0; i < errorDetailsList.size(); i++) {
//                ErrorManagerModel errorModel = errorDetailsList.get(i);
//                if (i == 0) {
//                    errorModel.view.requestFocus();
//                }
//                errorModel.tvError.setVisibility(View.VISIBLE);
//                errorModel.tvError.setText(errorModel.getErrorMessage());
//                errorModel.cardView.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//
//            }
//            return false;
//        } else {
//            return true;
//        }
//
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                //  checkPerm();
                takePicture();

            } else {
                showPermissionDeniedAlert(permissions);
            }

        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {

        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(requireActivity())
                .content(getString(R.string.reject_permission_results))
                .positiveButtonLabel(R.string.retry_again)
                .negativeButtonLabel(R.string.ok_close_now)
                .build();

        dialog.setListener(() -> {
            checkPerm();
        });

        dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());



        /*MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(getActivity());

        // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertdialogBuilder.setMessage(R.string.reject_permission_results);
        alertdialogBuilder.setPositiveButton(R.string.retry_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPerm();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.ok_close_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);*/
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

                Glide.with(getActivity())
                        .load(new File(mCurrentPhotoPath))
                        .thumbnail(0.25f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(binding.ivProfilePhoto);
            }
        } else if (requestCode == MY_REQUEST_CODE) {
            // getSelectedDate(data);
        }
    }

//    private void getSelectedDate(Intent data) {
//        //selectedDate  -  30/5/2023
//        if (data != null) {
//
//            Bundle bundle = data.getExtras();
//            String selectedDate = bundle.getString("selectedDate");
//            String whichDate = bundle.getString("whichDate");
//
//            if (!whichDate.isEmpty() && whichDate.equals("dobPatient")) {
//                try {
//                    Date sourceDate = new SimpleDateFormat("dd/MM/yyyy").parse(selectedDate);
//                    Date nowDate = new Date();
//                    if (sourceDate.after(nowDate)) {
//                        mAge.setText("");
//                        mDOB.setText("");
//                        // Toast.makeText(getActivity(), getString(R.string.valid_dob_msg), Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//            String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(selectedDate);
//            if (!selectedDate.isEmpty()) {
//                dobToDb = DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate);
////            String age = DateAndTimeUtils.getAge_FollowUp(DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate), getActivity());
//                //for age
//                String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(dobToDb).split(" ");
//                mAgeYears = Integer.parseInt(ymdData[0]);
//                mAgeMonths = Integer.parseInt(ymdData[1]);
//                mAgeDays = Integer.parseInt(ymdData[2]);
//
//                // String age = DateAndTimeUtils.formatAgeInYearsMonthsDate(getContext(), mAgeYears, mAgeMonths, mAgeDays);
//                String[] splitedDate = selectedDate.split("/");
//                mDOB.setText(dateToshow1 + " " + splitedDate[2]);
//                tvDobForDb.setText(dobToDb);
//                patientDTO.setDateofbirth(dobToDb);
//                if (mAgeYears < 9) {
//                    mAge.setText("");
//                    mDOB.setText("");
//                    tvErrorAge.setVisibility(View.VISIBLE);
//
//                } else {
//                    mAge.setText(mAgeYears + "");
//                }
//                Log.d(TAG, "getSelectedDate: " + dateToshow1 + ", " + splitedDate[2]);
//                setSelectedDob(mContext, dobToDb);
//            } else {
//                Log.d(TAG, "onClick: date empty");
//            }
//        }
//    }

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

//    class MyTextWatcher implements TextWatcher {
//        EditText editText;
//
//        MyTextWatcher(EditText editText) {
//            this.editText = editText;
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//            String val = editable.toString().trim();
//            if (this.editText.getId() == R.id.et_first_name) {
//                if (val.isEmpty()) {
//                    tvErrorFirstName.setVisibility(View.VISIBLE);
//                    tvErrorFirstName.setText(getString(R.string.enter_first_name));
//                    cardFirstName.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//
//                } else {
//                    tvErrorFirstName.setVisibility(View.GONE);
//                    cardFirstName.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//
//                }
//            } else if (this.editText.getId() == R.id.et_last_name) {
//                if (val.isEmpty()) {
//                    tvErrorLastName.setVisibility(View.VISIBLE);
//                    tvErrorLastName.setText(getString(R.string.enter_last_name));
//                    cardLastName.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//
//                } else {
//                    tvErrorLastName.setVisibility(View.GONE);
//                    cardLastName.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//
//                }
//            } else if (this.editText.getId() == R.id.et_dob) {
//                if (val.isEmpty()) {
//                    tvErrorDob.setVisibility(View.VISIBLE);
//                    tvErrorDob.setText(getString(R.string.select_dob));
//                    cardDob.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//
//                } else {
//                    tvErrorDob.setVisibility(View.GONE);
//                    cardDob.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//
//                }
//            } else if (this.editText.getId() == R.id.et_age) {
//                if (val.isEmpty()) {
//                    tvErrorAge.setVisibility(View.VISIBLE);
//                    tvErrorAge.setText(getString(R.string.select_age));
//                    cardAge.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//                } else {
//                    tvErrorAge.setVisibility(View.GONE);
//                    cardAge.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//                }
//            } else if (this.editText.getId() == R.id.et_mobile_no) {
//                if (!val.isEmpty()) {
//                    if (val.length() != 10) {
//                        tvErrorMobileNo.setVisibility(View.VISIBLE);
//                        tvErrorMobileNo.setText(getString(R.string.mobile_no_length));
//                        cardMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//                    } else {
//                        tvErrorMobileNo.setVisibility(View.GONE);
//                        cardMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//                    }
//                }
//            } else if (this.editText.getId() == R.id.et_alternate_mobile) {
//                if (!val.isEmpty()) {
//                    if (val.length() != 10) {
//                        tvErrAlternateMobileNo.setVisibility(View.VISIBLE);
//                        tvErrAlternateMobileNo.setText(getString(R.string.mobile_no_length));
//                        cardAlternateMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.error_red));
//                    } else {
//                        tvErrAlternateMobileNo.setVisibility(View.GONE);
//                        cardAlternateMobileNumber.setStrokeColor(ContextCompat.getColor(mContext, R.color.colorScrollbar));
//                    }
//                }
//            }
//        }
//    }

    private void setScrollToFocusedItem() {
        if (requireView().findFocus() != null) {
            Point point = getLocationOnScreen(requireView().findFocus());
            binding.scrollPersonalInfo.smoothScrollTo(0, point.y);
        }
    }

    public static Point getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

/*
    private  void updateDobAsPerLanguage(){

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
*/
}


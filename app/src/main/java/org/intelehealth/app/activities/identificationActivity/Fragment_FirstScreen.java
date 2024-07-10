package org.intelehealth.app.activities.identificationActivity;

import static android.app.Activity.RESULT_OK;
import static org.intelehealth.app.utilities.StringUtils.en_hi_dob_updated;
import static org.intelehealth.app.utilities.StringUtils.inputFilter_Others;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_contact_type_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_guardian_type_edit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.hbb20.CountryCodePicker;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.ui2.calendarviewcustom.CustomCalendarViewUI2;
import org.intelehealth.app.ui2.calendarviewcustom.SendSelectedDateInterface;
import org.intelehealth.app.utilities.AgeUtils;
import org.intelehealth.app.utilities.BundleKeys;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.EditTextUtils;
import org.intelehealth.app.utilities.IReturnValues;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.PatientRegConfigKeys;
import org.intelehealth.app.utilities.PatientRegFieldsUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.presenter.fields.data.RegFieldRepository;
import org.intelehealth.config.presenter.fields.factory.PatientViewModelFactory;
import org.intelehealth.config.presenter.fields.factory.RegFieldViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.PatientRegistrationFields;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Fragment_FirstScreen extends Fragment implements SendSelectedDateInterface {
    private View view;
    private String patientUuid = "";
    private ImageView patient_imgview;
    private Button frag1_nxt_btn_main;
    SessionManager sessionManager = null;
    private ImageView personal_icon, address_icon, other_icon;
    EditText mFirstNameEditText, mMiddleNameEditText, mLastNameEditText,
            mDOBEditText, mAgeEditText, mPhoneNumberEditText,
            mGuardianNameEditText, mEmContactNameEditText, mEmContactNumberEditText;

    Spinner mGuardianTypeSpinner, mContactTypeSpinner;
    private Fragment_SecondScreen fragment_secondScreen;
    PatientDTO patientdto = new PatientDTO();
    private String mGender;
    String uuid = "", dobToDb;
    private String mCurrentPhotoPath;

    RadioGroup genderRadioGroup;
    RadioButton mGenderMaleRadioButton, mGenderFemaleRadioButton, mGenderOthersRadioButton;
    private static final String TAG = Fragment_FirstScreen.class.getSimpleName();
    private DatePickerDialog mDOBPicker;
    private MaterialAlertDialogBuilder mAgePicker;
    Calendar dob = Calendar.getInstance();
    Calendar today = Calendar.getInstance();
    private CountryCodePicker mCountryCodePicker, mEmContactNoCountryCodePicker;
    TextView mPatientPhotoErrorTextView, mFirstNameErrorTextView, mMiddleNameErrorTextView, mLastNameErrorTextView,
            mGenderErrorTextView, mDOBErrorTextView, mAgeErrorTextView,
            mPhoneNumberErrorTextView, mGuardianNameErrorTextView, mGuardianTypeErrorTextView,
            mContactTypeErrorTextView, mEmContactNameErrorTextView, mEmContactNumErrorTextView, addAPictureTextView,
            firstNameTv, middleNameTv, lastNameTv, genderTv, dobTv, ageTv, guardianTypeTv, guardianNameTv,
            phoneNumberTv, contactTypeTv, emContactNameTv, emContactNumberTv;
    private int mDOBYear, mDOBMonth, mDOBDay, mAgeYears = 0, mAgeMonths = 0, mAgeDays = 0;
    int dob_indexValue = 15;
    //random value assigned to check while editing. If user didnt updated the dob and just clicked on fab
    //in that case, the edit() will get the dob_indexValue as 15 and we  will check if the
    //dob_indexValue == 15 then just get the dob_edittext editText value and add in the db.
    boolean fromSecondScreen = false;
    String patientID_edit;
    boolean patient_detail = false;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    private ArrayAdapter<CharSequence> guardianTypeAdapter, contactTypeAdapter;

    LinearLayout firstNameLay, middleNameLay, lastNameLay, genderLay, dobLay,
            ageLay, guardianNameLay, guardianTypeLay, phoneNumLay, contactTypeLay,
            emContactNameLay, emContactNumLay;
    RelativeLayout addPictureLay;

    RegFieldViewModel regFieldViewModel;

    List<PatientRegistrationFields> patientRegistrationFields;
    private boolean isEditMode = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_firstscreen, container, false);
        setLocale(getContext());
        return view;
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(getActivity());

        //config viewmodel initialization
        RegFieldRepository repository = new RegFieldRepository(ConfigDatabase.getInstance(requireContext()).patientRegFieldDao());
        RegFieldViewModelFactory factory = new RegFieldViewModelFactory(repository);
        regFieldViewModel = new ViewModelProvider(this, factory).get(RegFieldViewModel.class);

        initUi();

        Bundle args = getArguments();
        if (args != null) {
            patientdto = (PatientDTO) args.getSerializable("patientDTO");
            //   patientID_edit = getArguments().getString("patientUuid");
            patient_detail = args.getBoolean("patient_detail");
            fromSecondScreen = args.getBoolean("fromSecondScreen");
            isEditMode = args.getBoolean(BundleKeys.FROM_EDIT, false);
        }

        //fetching registration config
        fetchRegConfig();

        setupSpinner();

        fragment_secondScreen = new Fragment_SecondScreen();

        //updateUiFromSecondFrag();

        if (patient_detail)
            frag1_nxt_btn_main.setText(getString(R.string.save));


        personal_icon.setSelected(true);

//        personal_icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.addpatient_icon));
//        address_icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.addresslocation_icon_unselected));
//        other_icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.other_icon_unselected));


        // next btn click
        frag1_nxt_btn_main.setOnClickListener(v -> {
            onPatientCreateClicked();
        });


        // Gender - start
        if (mGenderMaleRadioButton.isChecked()) {
            mGender = "M";
        } else if (mGenderFemaleRadioButton.isChecked()) {
            mGender = "F";
        } else {
            mGender = "O";
        }

        // setting patient profile
        patient_imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPerm();
            }
        });


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

        mDOBEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mDOBPicker.show();
                CustomCalendarViewUI2 customCalendarViewUI2 = new CustomCalendarViewUI2(getActivity(), Fragment_FirstScreen.this);
                customCalendarViewUI2.showDatePicker(getActivity(), null);
            }
        });
        // DOB - end

        // Age - start
        mAgeEditText.setOnClickListener(new View.OnClickListener() {
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
                Button okButton = convertView.findViewById(R.id.button_ok_picker);
                Button cancelButton = convertView.findViewById(R.id.btn_cancel_picker);
                final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
                final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
                final TextView dayTv = convertView.findViewById(R.id.dialog_2_numbers_text_3);
                dayPicker.setVisibility(View.VISIBLE);

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

                AlertDialog alertDialog = mAgePicker.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                int width = getContext().getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
                alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

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

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String ageString = DateAndTimeUtils.formatAgeInYearsMonthsDate(getContext(), mAgeYears, mAgeMonths, mAgeDays);
                        mAgeEditText.setText(ageString);

                        mDOBErrorTextView.setVisibility(View.GONE);
                        mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                        mAgeErrorTextView.setVisibility(View.GONE);
                        mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_MONTH, -mAgeDays);
                        calendar.add(Calendar.MONTH, -mAgeMonths);
                        calendar.add(Calendar.YEAR, -mAgeYears);

                        mDOBYear = calendar.get(Calendar.YEAR);
                        mDOBMonth = calendar.get(Calendar.MONTH);
                        mDOBDay = calendar.get(Calendar.DAY_OF_MONTH);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",
                                Locale.ENGLISH);
                        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd/MM/yyyy",
                                Locale.ENGLISH);
                        dob.set(mDOBYear, mDOBMonth, mDOBDay);
                        String dobString = simpleDateFormat.format(dob.getTime());
                        dobToDb = DateAndTimeUtils.convertDateToYyyyMMddFormat(simpleDateFormat1.format(dob.getTime()));
                        mDOBEditText.setText(DateAndTimeUtils.getDisplayDateForApp(dobString));
                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                            mDOBEditText.setText(en_hi_dob_updated(DateAndTimeUtils.getDisplayDateForApp(dobString)));

                        updateGuardianVisibility();

                        alertDialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
//                IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);
            }
        });

    }


    /**
     * fetching reg config from local db
     */
    private void fetchRegConfig() {
        if (getActivity() != null) {
            regFieldViewModel.fetchEnabledPersonalRegFields()
                    .observe(getActivity(), it -> {
                                patientRegistrationFields = it;
                                updateUiFromSecondFrag();
                                configAllFields();
                            }
                    );
        }
    }

    /**
     *
     */
    private void updateUiFromSecondFrag() {
        // Setting up the screen when user came from Second screen.
        if (fromSecondScreen) {
            mFirstNameEditText.setText(patientdto.getFirstname());
            mMiddleNameEditText.setText(patientdto.getMiddlename());
            mLastNameEditText.setText(patientdto.getLastname());
            mGuardianNameEditText.setText(patientdto.getGuardianName());
            mEmContactNameEditText.setText(patientdto.getEmContactName());

            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String guardianType = switch_hi_guardian_type_edit(patientdto.getGuardianType());
                mGuardianTypeSpinner.setSelection(guardianTypeAdapter.getPosition(guardianType));

                String contactType = switch_hi_contact_type_edit(patientdto.getContactType());
                mContactTypeSpinner.setSelection(contactTypeAdapter.getPosition(contactType));
            } else {
                mGuardianTypeSpinner.setSelection(guardianTypeAdapter.getPosition(patientdto.getGuardianType()));
                mContactTypeSpinner.setSelection(contactTypeAdapter.getPosition(patientdto.getContactType()));
            }

            dobToDb = patientdto.getDateofbirth();

            mDOBEditText.setText(DateAndTimeUtils.getDisplayDateForApp(dobToDb));

            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                mDOBEditText.setText(en_hi_dob_updated(DateAndTimeUtils.getDisplayDateForApp(dobToDb)));

            // dob_edittext.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
            //get year month days
            String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patientdto.getDateofbirth(), getActivity());
            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patientdto.getDateofbirth()).split(" ");
            mAgeYears = Integer.parseInt(ymdData[0]);
            mAgeMonths = Integer.parseInt(ymdData[1]);
            mAgeDays = Integer.parseInt(ymdData[2]);
            String age = DateAndTimeUtils.formatAgeInYearsMonthsDate(getContext(), mAgeYears, mAgeMonths, mAgeDays);
            mAgeEditText.setText(age);

            mCountryCodePicker.setFullNumber(patientdto.getPhonenumber()); // automatically assigns cc to spinner and number to edittext field.
            mEmContactNoCountryCodePicker.setFullNumber(patientdto.getEmContactNumber());
            //   phoneno_edittext.setText(patientdto.getPhonenumber());

            // Gender edit
            if (patientdto.getGender().equals("M")) {
                mGenderMaleRadioButton.setChecked(true);
                if (mGenderFemaleRadioButton.isChecked())
                    mGenderFemaleRadioButton.setChecked(false);
                if (mGenderOthersRadioButton.isChecked())
                    mGenderOthersRadioButton.setChecked(false);
                Log.v(TAG, "yes");
            } else if (patientdto.getGender().equals("F")) {
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

            // profile image edit
            if (patientdto.getPatientPhoto() != null && !patientdto.getPatientPhoto().trim().isEmpty()) {
                //  patient_imgview.setImageBitmap(BitmapFactory.decodeFile(patientdto.getPatientPhoto()));
                RequestBuilder<Drawable> requestBuilder = Glide.with(getActivity())
                        .asDrawable().sizeMultiplier(0.3f);

                Glide.with(getActivity())
                        .load(new File(patientdto.getPatientPhoto()))
                        .thumbnail(requestBuilder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(patient_imgview);

            }
        }

        updateGuardianVisibility();

        setMobileNumberLimit();
    }


    /**
     * changing fields status based on config data
     */
    private void configAllFields() {
        for (PatientRegistrationFields fields : patientRegistrationFields) {
            switch (fields.getIdKey()) {
                case PatientRegConfigKeys.PROFILE_PHOTO ->
                        PatientRegFieldsUtils.Companion.configField(
                                isEditMode,
                                fields,
                                addPictureLay,
                                patient_imgview,
                                addAPictureTextView,
                                addAPictureTextView
                        );
                case PatientRegConfigKeys.FIRST_NAME -> PatientRegFieldsUtils.Companion.configField(
                        isEditMode,
                        fields,
                        firstNameLay,
                        mFirstNameEditText,
                        null,
                        firstNameTv
                );
                case PatientRegConfigKeys.MIDDLE_NAME ->
                        PatientRegFieldsUtils.Companion.configField(
                                isEditMode,
                                fields,
                                middleNameLay,
                                mMiddleNameEditText,
                                null,
                                middleNameTv
                        );
                case PatientRegConfigKeys.LAST_NAME -> PatientRegFieldsUtils.Companion.configField(
                        isEditMode,
                        fields,
                        lastNameLay,
                        mLastNameEditText,
                        null,
                        lastNameTv
                );
                case PatientRegConfigKeys.GENDER -> PatientRegFieldsUtils.Companion.configField(
                        isEditMode,
                        fields,
                        genderLay,
                        genderRadioGroup,
                        null,
                        genderTv
                );
                case PatientRegConfigKeys.DOB -> PatientRegFieldsUtils.Companion.configField(
                        isEditMode,
                        fields,
                        dobLay,
                        mDOBEditText,
                        null,
                        dobTv
                );
                case PatientRegConfigKeys.AGE -> PatientRegFieldsUtils.Companion.configField(
                        isEditMode,
                        fields,
                        ageLay,
                        mAgeEditText,
                        null,
                        ageTv
                );
                case PatientRegConfigKeys.GUARDIAN_TYPE -> {
                    if (AgeUtils.Companion.isGuardianRequired(mAgeYears,mAgeMonths,mAgeDays) && (!mDOBEditText.getText().toString().isEmpty() ||
                            !mAgeEditText.getText().toString().isEmpty())) {
                        PatientRegFieldsUtils.Companion.configField(
                                isEditMode,
                                fields,
                                guardianTypeLay,
                                mGuardianTypeSpinner,
                                null,
                                guardianTypeTv
                        );
                    }
                }
                case PatientRegConfigKeys.GUARDIAN_NAME -> {
                    if (AgeUtils.Companion.isGuardianRequired(mAgeYears,mAgeMonths,mAgeDays) && (!mDOBEditText.getText().toString().isEmpty() ||
                            !mAgeEditText.getText().toString().isEmpty())) {
                        PatientRegFieldsUtils.Companion.configField(
                                isEditMode,
                                fields,
                                guardianNameLay,
                                mGuardianNameEditText,
                                null,
                                guardianNameTv
                        );
                    }
                }
                case PatientRegConfigKeys.PHONE_NUM -> PatientRegFieldsUtils.Companion.configField(
                        isEditMode,
                        fields,
                        phoneNumLay,
                        mPhoneNumberEditText,
                        mCountryCodePicker,
                        phoneNumberTv
                );
                case PatientRegConfigKeys.EM_CONTACT_TYPE ->
                        PatientRegFieldsUtils.Companion.configField(
                                isEditMode,
                                fields,
                                contactTypeLay,
                                mContactTypeSpinner,
                                null,
                                contactTypeTv
                        );
                case PatientRegConfigKeys.EM_CONTACT_NAME ->
                        PatientRegFieldsUtils.Companion.configField(
                                isEditMode,
                                fields,
                                emContactNameLay,
                                mEmContactNameEditText,
                                null,
                                emContactNameTv
                        );
                case PatientRegConfigKeys.EM_CONTACT_NUMBER ->
                        PatientRegFieldsUtils.Companion.configField(
                                isEditMode,
                                fields,
                                emContactNumLay,
                                mEmContactNumberEditText,
                                mEmContactNoCountryCodePicker,
                                emContactNumberTv
                        );
            }
        }
    }

    /**
     * all fragment ui initialization
     */
    private void initUi() {
        //all view layout
        addPictureLay = view.findViewById(R.id.add_picture_lay);
        firstNameLay = view.findViewById(R.id.linear_firstname);
        middleNameLay = view.findViewById(R.id.linear_middlename);
        lastNameLay = view.findViewById(R.id.linear_lastname);
        genderLay = view.findViewById(R.id.linear_gender);
        guardianNameLay = view.findViewById(R.id.linear_guardian_name);
        dobLay = view.findViewById(R.id.linear_dob);
        ageLay = view.findViewById(R.id.linear_age);
        phoneNumLay = view.findViewById(R.id.linear_phone_num);
        contactTypeLay = view.findViewById(R.id.linear_contact_type);
        emContactNameLay = view.findViewById(R.id.linear_em_contact_name);
        emContactNumLay = view.findViewById(R.id.linear_emergency_contact_num);
        guardianTypeLay = view.findViewById(R.id.linear_guardian_type);

        patient_imgview = view.findViewById(R.id.patient_imgview);
        frag1_nxt_btn_main = view.findViewById(R.id.frag1_nxt_btn_main);
        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        mFirstNameEditText = view.findViewById(R.id.firstname_edittext);
        mFirstNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25  // IDA4-1344
        mMiddleNameEditText = view.findViewById(R.id.middlename_edittext);
        mMiddleNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25  // IDA4-1344
        mLastNameEditText = view.findViewById(R.id.lastname_edittext);
        mLastNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25  // IDA4-1344

        mGuardianNameEditText = view.findViewById(R.id.guardian_name_edittext);
        mGuardianNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25  // IDA4-1344
        mEmContactNameEditText = view.findViewById(R.id.em_contact_name_edittext);
        mEmContactNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25  // IDA4-1344

        genderRadioGroup = view.findViewById(R.id.gender_radio_group);
        mGenderMaleRadioButton = view.findViewById(R.id.gender_male);
        mGenderFemaleRadioButton = view.findViewById(R.id.gender_female);
        mGenderOthersRadioButton = view.findViewById(R.id.gender_other);
        mDOBEditText = view.findViewById(R.id.dob_edittext);
        mAgeEditText = view.findViewById(R.id.age_edittext);
        mCountryCodePicker = view.findViewById(R.id.countrycode_spinner);
        mPhoneNumberEditText = view.findViewById(R.id.phoneno_edittext);
        Log.v("phone", "phone value: " + mCountryCodePicker.getSelectedCountryCode());
        mCountryCodePicker.registerCarrierNumberEditText(mPhoneNumberEditText); // attaches the ccp spinner with the edittext
        mCountryCodePicker.setNumberAutoFormattingEnabled(false);

        mEmContactNoCountryCodePicker = view.findViewById(R.id.emergency_contact_countrycode_spinner);
        mEmContactNumberEditText = view.findViewById(R.id.emergency_contact_no_edittext);
        Log.v("em_phone", "phone value: " + mEmContactNoCountryCodePicker.getSelectedCountryCode());
        mEmContactNoCountryCodePicker.registerCarrierNumberEditText(mEmContactNumberEditText); // attaches the ccp spinner with the edittext
        mEmContactNoCountryCodePicker.setNumberAutoFormattingEnabled(false);

        mGuardianTypeSpinner = view.findViewById(R.id.guardian_type_spinner);
        mContactTypeSpinner = view.findViewById(R.id.contact_type_spinner);

        //all title view
        addAPictureTextView = view.findViewById(R.id.add_picture);
        firstNameTv = view.findViewById(R.id.first_name_tv);
        middleNameTv = view.findViewById(R.id.middle_name_tv);
        lastNameTv = view.findViewById(R.id.last_name_tv);
        genderTv = view.findViewById(R.id.gender_tv);
        dobTv = view.findViewById(R.id.dob_tv);
        ageTv = view.findViewById(R.id.age_tv);
        guardianTypeTv = view.findViewById(R.id.guardian_type_tv);
        guardianNameTv = view.findViewById(R.id.guardian_name_tv);
        phoneNumberTv = view.findViewById(R.id.phone_num_tv);
        contactTypeTv = view.findViewById(R.id.contact_type_tv);
        emContactNameTv = view.findViewById(R.id.em_contact_name_tv);
        emContactNumberTv = view.findViewById(R.id.em_contact_num_tv);

        mPatientPhotoErrorTextView = view.findViewById(R.id.profile_image_error);
        mFirstNameErrorTextView = view.findViewById(R.id.firstname_error);
        mMiddleNameErrorTextView = view.findViewById(R.id.middlename_error);
        mLastNameErrorTextView = view.findViewById(R.id.lastname_error);
        mGenderErrorTextView = view.findViewById(R.id.gender_error);
        mDOBErrorTextView = view.findViewById(R.id.dob_error);
        mAgeErrorTextView = view.findViewById(R.id.age_error);
        mPhoneNumberErrorTextView = view.findViewById(R.id.phone_error);

        mGuardianNameErrorTextView = view.findViewById(R.id.guardian_name_error);
        mGuardianTypeErrorTextView = view.findViewById(R.id.guardian_type_error);
        mContactTypeErrorTextView = view.findViewById(R.id.contact_type_error);
        mEmContactNameErrorTextView = view.findViewById(R.id.em_contact_name_error);
        mEmContactNumErrorTextView = view.findViewById(R.id.emergency_contact_no_error);

        mFirstNameEditText.addTextChangedListener(new MyTextWatcher(mFirstNameEditText, getActivity()));
        mMiddleNameEditText.addTextChangedListener(new MyTextWatcher(mMiddleNameEditText, getActivity()));
        mLastNameEditText.addTextChangedListener(new MyTextWatcher(mLastNameEditText, getActivity()));
        mGuardianNameEditText.addTextChangedListener(new MyTextWatcher(mGuardianNameEditText, getActivity()));
        mEmContactNameEditText.addTextChangedListener(new MyTextWatcher(mEmContactNameEditText, getActivity()));
        mDOBEditText.addTextChangedListener(new MyTextWatcher(mDOBEditText, getActivity()));
        mAgeEditText.addTextChangedListener(new MyTextWatcher(mAgeEditText, getActivity()));
        mEmContactNumberEditText.addTextChangedListener(new MyTextWatcher(mEmContactNumberEditText, getActivity()));
        //mPhoneNumberEditText.addTextChangedListener(new MyTextWatcher(mPhoneNumberEditText));
    }

    /**
     * guardian type and name will change based on age
     * if age is <=18 the two fields will show
     **/
    private void updateGuardianVisibility() {
        //guardian name view config
        if (AgeUtils.Companion.isGuardianRequired(mAgeYears,mAgeMonths,mAgeDays) && (!mDOBEditText.getText().toString().isEmpty() || !mAgeEditText.getText().toString().isEmpty())
                && PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_NAME)) {
            String guardianName = guardianNameTv.getText().toString();
            if (PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_NAME)) {
                String name = guardianName.replace("*","");
                guardianNameTv.setText(new StringBuilder().append(name).append("*"));
            }
            guardianNameLay.setVisibility(View.VISIBLE);
        } else {
            guardianNameLay.setVisibility(View.GONE);
        }

        //guardian type view config
        if (AgeUtils.Companion.isGuardianRequired(mAgeYears,mAgeMonths,mAgeDays) && (!mDOBEditText.getText().toString().isEmpty() || !mAgeEditText.getText().toString().isEmpty())
                && PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_TYPE)) {
            String guardianType = guardianTypeTv.getText().toString();
            if (PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_TYPE)) {
                String type = guardianType.replace("*","");
                guardianTypeTv.setText(new StringBuilder().append(type).append("*"));
            }
            guardianTypeLay.setVisibility(View.VISIBLE);
        } else {
            guardianTypeLay.setVisibility(View.GONE);
        }
    }

    /**
     * All spinner view populating here
     */
    private void setupSpinner() {
        Resources res = getResources();
        // guardian type
        try {
            String countriesLanguage = "guardian_type_" + sessionManager.getAppLanguage();
            int guardianTypes = res.getIdentifier(countriesLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (guardianTypes != 0) {
                guardianTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                        guardianTypes, R.layout.simple_spinner_item_1);
                guardianTypeAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
            }
            mGuardianTypeSpinner.setAdapter(guardianTypeAdapter); // keeping this is setting textcolor to white so comment this and add android:entries in xml
            mGuardianTypeSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_menu_background));
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        // contact type
        try {
            String countriesLanguage = "contact_type_" + sessionManager.getAppLanguage();
            int contactTypes = res.getIdentifier(countriesLanguage, "array", getActivity().getApplicationContext().getPackageName());
            if (contactTypes != 0) {
                contactTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                        contactTypes, R.layout.simple_spinner_item_1);
                contactTypeAdapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
            }
            mContactTypeSpinner.setAdapter(contactTypeAdapter); // keeping this is setting textcolor to white so comment this and add android:entries in xml
            mContactTypeSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.popup_menu_background));
        } catch (Exception e) {
            Logger.logE("Identification", "#648", e);
        }

        mGuardianTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mGuardianTypeErrorTextView.setVisibility(View.GONE);
                    mGuardianTypeSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mContactTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mContactTypeErrorTextView.setVisibility(View.GONE);
                    mContactTypeSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    private int mSelectedMobileNumberValidationLength = 0;
    private String mSelectedCountryCode = "";

    private void setMobileNumberLimit() {
        mSelectedCountryCode = mCountryCodePicker.getSelectedCountryCode();
        if (mSelectedCountryCode.equals("91")) {
            mSelectedMobileNumberValidationLength = 10;
        }
        mPhoneNumberEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return null;
            }
        };

        mPhoneNumberEditText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(mSelectedMobileNumberValidationLength)});
        // hide the validation fields ...
        mPhoneNumberErrorTextView.setVisibility(View.GONE);
        mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);


        //emergency contact number
        mEmContactNumberEditText.setInputType(InputType.TYPE_CLASS_PHONE);

        mEmContactNumberEditText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(mSelectedMobileNumberValidationLength)});
        // hide the validation fields ...
        mEmContactNumErrorTextView.setVisibility(View.GONE);
        mEmContactNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
    }

    @Override
    public void getSelectedDate(String selectedDate, String whichDate) {
        Log.d(TAG, "getSelectedDate: selectedDate from interface : " + selectedDate);
        if (selectedDate != null) {
            try {
                Date sourceDate = new SimpleDateFormat("dd/MM/yyyy").parse(selectedDate);
                Date nowDate = new Date();
                if (sourceDate.after(nowDate)) {
                    mAgeEditText.setText("");
                    mDOBEditText.setText("");
                    Toast.makeText(getActivity(), getString(R.string.valid_dob_msg), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(selectedDate);
        if (!selectedDate.isEmpty()) {
            dobToDb = DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate);
//            String age = DateAndTimeUtils.getAge_FollowUp(DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate), getActivity());
            //for age
            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(dobToDb).split(" ");
            mAgeYears = Integer.parseInt(ymdData[0]);
            mAgeMonths = Integer.parseInt(ymdData[1]);
            mAgeDays = Integer.parseInt(ymdData[2]);

            String age = DateAndTimeUtils.formatAgeInYearsMonthsDate(getContext(), mAgeYears, mAgeMonths, mAgeDays);
            String[] splitedDate = selectedDate.split("/");
            if (age != null && !age.isEmpty()) {
                mAgeEditText.setText(age);
                mDOBEditText.setText(dateToshow1 + ", " + splitedDate[2]);
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    mDOBEditText.setText(en_hi_dob_updated(dateToshow1) + ", " + splitedDate[2]);
                Log.d(TAG, "getSelectedDate: " + dateToshow1 + ", " + splitedDate[2]);

                updateGuardianVisibility();
            } else {
                mAgeEditText.setText("");
                mDOBEditText.setText("");
            }
        } else {
            Log.d(TAG, "onClick: date empty");
        }
    }

    class MyTextWatcher implements TextWatcher {
        EditText editText;
        WeakReference<FragmentActivity> activity;

        MyTextWatcher(EditText editText, FragmentActivity activity) {
            this.editText = editText;
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
            if (this.editText.getId() == R.id.firstname_edittext &&
                    PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.FIRST_NAME)) {
                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.FIRST_NAME)) {
                    mFirstNameErrorTextView.setVisibility(View.VISIBLE);
                    mFirstNameErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mFirstNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mFirstNameErrorTextView.setVisibility(View.GONE);
                    mFirstNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.middlename_edittext &&
                    PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.MIDDLE_NAME)) {
                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.MIDDLE_NAME)) {
                    mMiddleNameErrorTextView.setVisibility(View.VISIBLE);
                    mMiddleNameErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mMiddleNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mMiddleNameErrorTextView.setVisibility(View.GONE);
                    mMiddleNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.lastname_edittext &&
                    PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.LAST_NAME)) {
                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.LAST_NAME)) {
                    mLastNameErrorTextView.setVisibility(View.VISIBLE);
                    mLastNameErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mLastNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mLastNameErrorTextView.setVisibility(View.GONE);
                    mLastNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.dob_edittext &&
                    PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DOB)) {
                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.DOB)) {
                    mDOBErrorTextView.setVisibility(View.VISIBLE);
                    mDOBErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mDOBEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mDOBErrorTextView.setVisibility(View.GONE);
                    mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.age_edittext &&
                    PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.AGE)) {
                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.AGE)) {
                    mAgeErrorTextView.setVisibility(View.VISIBLE);
                    mAgeErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mAgeEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mAgeErrorTextView.setVisibility(View.GONE);
                    mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.phoneno_edittext
                    && PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.PHONE_NUM)) {

                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.PHONE_NUM)) {
                    mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                    mPhoneNumberErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mPhoneNumberErrorTextView.setVisibility(View.GONE);
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.guardian_name_edittext &&
                    PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_NAME)) {

                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_NAME)) {
                    mGuardianNameErrorTextView.setVisibility(View.VISIBLE);
                    mGuardianNameErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mGuardianNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mGuardianNameErrorTextView.setVisibility(View.GONE);
                    mGuardianNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.em_contact_name_edittext
                    && PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NAME)) {
                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NAME)) {
                    mEmContactNameErrorTextView.setVisibility(View.VISIBLE);
                    mEmContactNameErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mEmContactNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mEmContactNameErrorTextView.setVisibility(View.GONE);
                    mEmContactNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }

            } else if (this.editText.getId() == R.id.emergency_contact_no_edittext
                    && PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NUMBER)) {
                String phoneNumber = mPhoneNumberEditText.getText().toString().trim();
                String emContactNumber = this.editText.getText().toString().trim();

                if (val.isEmpty() && PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NUMBER)) {
                    mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                    mEmContactNumErrorTextView.setText(activity.get().getString(R.string.error_field_required));
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    if (phoneNumber.equals(emContactNumber)) {
                        mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                        mEmContactNumErrorTextView.setText(activity.get().getString(R.string.phone_number_and_emergency_number_can_not_be_the_same));
                        mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        return;
                    }
                    mEmContactNumErrorTextView.setVisibility(View.GONE);
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
        }
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            takePicture();
        }
    }

    /**
     * removed deprecated onRequestPermissionsResult
     * and added this code
     */
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    (ActivityResultCallback<Map<String, Boolean>>) result -> {
                        boolean allGranted = result.size() != 0;
                        for (boolean grantResult : result.values()) {
                            if (!grantResult) {
                                allGranted = false;
                                break;
                            }
                        }
                        if (allGranted) {
                            checkPerm();
                        } else {
                            showPermissionDeniedAlert(result.keySet().toArray(new String[result.size()]));
                        }
                    });

    private void showPermissionDeniedAlert(String[] permissions) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(getActivity());

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

        positiveButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            permissionsLauncher.launch(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]));
            return false;
        }
        return true;
    }

    private void takePicture() {
        String patientTemp = "";
        if (patientUuid.equalsIgnoreCase("")) {
            patientTemp = patientdto.getUuid();
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
        cameraActivityResult.launch(cameraIntent);
    }

    // gender
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.gender_male:
                if (checked)
                    mGender = "M";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.gender_female:
                if (checked)
                    mGender = "F";
                Log.v(TAG, "gender:" + mGender);
                break;
            case R.id.gender_other:
                if (checked)
                    mGender = "O";
                Log.v(TAG, "gender: " + mGender);
                break;
        }
        mGenderErrorTextView.setVisibility(View.GONE);
    }

    // DOB
    public String getYear(int syear, int smonth, int sday, int eyear, int emonth, int eday) {
        LocalDate birthdate = new LocalDate(syear, smonth + 1, sday);
        LocalDate now = new LocalDate();
        Period p = new Period(birthdate, now, PeriodType.yearMonthDay());
        return p.getYears() + " years  - " + p.getMonths() + " month - " + p.getDays() + " days";
    }


    private void onPatientCreateClicked() {
        uuid = UUID.randomUUID().toString();
        Log.v(TAG, "reltion: " + patientID_edit);
        if (patient_detail) {
        } else {
            patientdto.setUuid(uuid);
        }

        //profile pic
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.PROFILE_PHOTO)) {
            if ((mCurrentPhotoPath == null && patientdto.getPatientPhoto() == null) &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.PROFILE_PHOTO)) {
                mPatientPhotoErrorTextView.setVisibility(View.VISIBLE);
                mPatientPhotoErrorTextView.setText(getString(R.string.error_field_required));
                return;
            } else {
                mPatientPhotoErrorTextView.setVisibility(View.GONE);
            }
        }

        //first name
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.FIRST_NAME)) {
            if (mFirstNameEditText.getText().toString().equals("") &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.FIRST_NAME)) {
                mFirstNameErrorTextView.setVisibility(View.VISIBLE);
                mFirstNameErrorTextView.setText(getString(R.string.error_field_required));
                mFirstNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mFirstNameEditText.requestFocus();
                return;
            } else {
                mFirstNameErrorTextView.setVisibility(View.GONE);
                mFirstNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

        //middle name
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.MIDDLE_NAME)) {
            if (mMiddleNameEditText.getText().toString().equals("") &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.MIDDLE_NAME)) {
                mMiddleNameErrorTextView.setVisibility(View.VISIBLE);
                mMiddleNameErrorTextView.setText(getString(R.string.error_field_required));
                mMiddleNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mMiddleNameEditText.requestFocus();
                return;
            } else {
                mMiddleNameErrorTextView.setVisibility(View.GONE);
                mMiddleNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

        //last name
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.LAST_NAME)) {
            if (mLastNameEditText.getText().toString().equals("") &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.LAST_NAME)) {
                mLastNameErrorTextView.setVisibility(View.VISIBLE);
                mLastNameErrorTextView.setText(getString(R.string.error_field_required));
                mLastNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mLastNameEditText.requestFocus();
                return;
            } else {
                mLastNameErrorTextView.setVisibility(View.GONE);
                mLastNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

        // gender valid - start
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.GENDER)) {
            if ((!mGenderFemaleRadioButton.isChecked() && !mGenderMaleRadioButton.isChecked() && !mGenderOthersRadioButton.isChecked()) &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.GENDER)) {
                mGenderErrorTextView.setVisibility(View.VISIBLE);
                return;
            } else {
                mGenderErrorTextView.setVisibility(View.GONE);
            }
        }

        //dob
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.DOB)) {
            if (mDOBEditText.getText().toString().equals("") &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.DOB)) {
                mDOBErrorTextView.setVisibility(View.VISIBLE);
                mDOBErrorTextView.setText(getString(R.string.error_field_required));
                mDOBEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mDOBEditText.requestFocus();
                return;
            } else {
                mDOBErrorTextView.setVisibility(View.GONE);
                mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

        //age
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.AGE)) {
            if (mAgeEditText.getText().toString().equals("") &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.AGE)) {
                mAgeErrorTextView.setVisibility(View.VISIBLE);
                mAgeErrorTextView.setText(getString(R.string.error_field_required));
                mAgeEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mAgeEditText.requestFocus();
                return;
            } else {
                mAgeErrorTextView.setVisibility(View.GONE);
                mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

        if (AgeUtils.Companion.isGuardianRequired(mAgeYears,mAgeMonths,mAgeDays)) {
            //Guardian name edittext
            if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_NAME)) {
                if (mGuardianNameEditText.getText().toString().equals("") &&
                        PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_NAME)) {
                    mGuardianNameErrorTextView.setVisibility(View.VISIBLE);
                    mGuardianNameErrorTextView.setText(getString(R.string.error_field_required));
                    mGuardianNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mGuardianNameEditText.requestFocus();
                    return;
                } else {
                    mGuardianNameErrorTextView.setVisibility(View.GONE);
                    mGuardianNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }

            //Guardian type spinner
            if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_TYPE)) {
                if (mGuardianTypeSpinner.getSelectedItemPosition() == 0 &&
                        PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.GUARDIAN_TYPE)) {
                    mGuardianTypeErrorTextView.setVisibility(View.VISIBLE);
                    mGuardianTypeErrorTextView.setText(getString(R.string.error_field_required));
                    mGuardianTypeSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mGuardianTypeSpinner.requestFocus();
                    return;
                } else {
                    mGuardianTypeErrorTextView.setVisibility(View.GONE);
                    mGuardianTypeSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
                }
            }
        }

        //phone number
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.PHONE_NUM)) {
            if (PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.PHONE_NUM)) {
                String s = mPhoneNumberEditText.getText().toString().replaceAll("\\s+", "");
                Log.v("phone", "phone: " + s);
                if (s.length() < mSelectedMobileNumberValidationLength) {
                    mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                    mPhoneNumberErrorTextView.setText(getString(R.string.enter_10_digits));
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mPhoneNumberEditText.requestFocus();
                    return;
                }
                if (!mCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91") && s.length() < 15) {
                    mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                    mPhoneNumberErrorTextView.setText(getString(R.string.enter_15_digits));
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mPhoneNumberEditText.requestFocus();
                    return;
                } else {
                    mPhoneNumberErrorTextView.setVisibility(View.GONE);
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }

                if (mCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91")
                        && s.length() != mSelectedMobileNumberValidationLength) {
                    mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                    mPhoneNumberErrorTextView.setText(R.string.invalid_mobile_no);
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mPhoneNumberEditText.requestFocus();
                    return;
                } else {
                    mPhoneNumberErrorTextView.setVisibility(View.GONE);
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }else {
                String s = mPhoneNumberEditText.getText().toString().replaceAll("\\s+", "");
                if(s.length()>0){
                    if (s.length() < mSelectedMobileNumberValidationLength) {
                        mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                        mPhoneNumberErrorTextView.setText(getString(R.string.enter_10_digits));
                        mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        mPhoneNumberEditText.requestFocus();
                        return;
                    }
                    if (!mCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91") && s.length() < 15) {
                        mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                        mPhoneNumberErrorTextView.setText(getString(R.string.enter_15_digits));
                        mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        mPhoneNumberEditText.requestFocus();
                        return;
                    } else {
                        mPhoneNumberErrorTextView.setVisibility(View.GONE);
                        mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }

                    if (mCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91")
                            && s.length() != mSelectedMobileNumberValidationLength) {
                        mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                        mPhoneNumberErrorTextView.setText(R.string.invalid_mobile_no);
                        mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        mPhoneNumberEditText.requestFocus();
                        return;
                    } else {
                        mPhoneNumberErrorTextView.setVisibility(View.GONE);
                        mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
                }
            }
        }


        //Contact type spinner
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_TYPE)) {
            if (mContactTypeSpinner.getSelectedItemPosition() == 0 &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_TYPE)) {
                mContactTypeErrorTextView.setVisibility(View.VISIBLE);
                mContactTypeErrorTextView.setText(getString(R.string.error_field_required));
                mContactTypeSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mContactTypeSpinner.requestFocus();
                return;
            } else {
                mContactTypeErrorTextView.setVisibility(View.GONE);
                mContactTypeSpinner.setBackgroundResource(R.drawable.ui2_spinner_background_new);
            }
        }


        //Emergency contact name edittext
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NAME)) {
            if (mEmContactNameEditText.getText().toString().equals("") &&
                    PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NAME)) {
                mEmContactNameErrorTextView.setVisibility(View.VISIBLE);
                mEmContactNameErrorTextView.setText(getString(R.string.error_field_required));
                mEmContactNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mEmContactNameEditText.requestFocus();
                return;
            } else {
                mEmContactNameErrorTextView.setVisibility(View.GONE);
                mEmContactNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

        //Emergency contact number country code picker
        if (PatientRegFieldsUtils.Companion.getFieldEnableStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NUMBER)) {
            Log.d("ppppp"," "+PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NUMBER));
            if (PatientRegFieldsUtils.Companion.getFieldMandatoryStatus(patientRegistrationFields, PatientRegConfigKeys.EM_CONTACT_NUMBER)) {
                String s = mEmContactNumberEditText.getText().toString().replaceAll("\\s+", "");
                if (s.length() < mSelectedMobileNumberValidationLength) {
                    mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                    mEmContactNumErrorTextView.setText(getString(R.string.enter_10_digits));
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mEmContactNumberEditText.requestFocus();
                    return;
                }

                if (s.equals(mPhoneNumberEditText.getText().toString())) {
                    mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                    mEmContactNumErrorTextView.setText(getString(R.string.phone_number_and_emergency_number_can_not_be_the_same));
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mEmContactNumberEditText.requestFocus();
                    return;
                }
                if (!mCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91") && s.length() < 15) {
                    mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                    mEmContactNumErrorTextView.setText(getString(R.string.enter_15_digits));
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mEmContactNumberEditText.requestFocus();
                    return;
                } else {
                    mEmContactNumErrorTextView.setVisibility(View.GONE);
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }

                if (mEmContactNoCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91")
                        && s.length() != mSelectedMobileNumberValidationLength) {
                    mEmContactNumberEditText.setVisibility(View.VISIBLE);
                    mEmContactNumberEditText.setText(R.string.invalid_mobile_no);
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    mEmContactNumberEditText.requestFocus();
                    return;
                } else {
                    mEmContactNumErrorTextView.setVisibility(View.GONE);
                    mEmContactNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }else {
                String s = mEmContactNumberEditText.getText().toString().replaceAll("\\s+", "");
                if (s.length()>0){
                    if (s.length() < mSelectedMobileNumberValidationLength) {
                        mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                        mEmContactNumErrorTextView.setText(getString(R.string.enter_10_digits));
                        mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        mEmContactNumberEditText.requestFocus();
                        return;
                    }

                    if (s.equals(mPhoneNumberEditText.getText().toString())) {
                        mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                        mEmContactNumErrorTextView.setText(getString(R.string.phone_number_and_emergency_number_can_not_be_the_same));
                        mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        mEmContactNumberEditText.requestFocus();
                        return;
                    }
                    if (!mCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91") && s.length() < 15) {
                        mEmContactNumErrorTextView.setVisibility(View.VISIBLE);
                        mEmContactNumErrorTextView.setText(getString(R.string.enter_15_digits));
                        mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        mEmContactNumberEditText.requestFocus();
                        return;
                    } else {
                        mEmContactNumErrorTextView.setVisibility(View.GONE);
                        mEmContactNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }

                    if (mEmContactNoCountryCodePicker.getSelectedCountryCode().equalsIgnoreCase("91")
                            && s.length() != mSelectedMobileNumberValidationLength) {
                        mEmContactNumberEditText.setVisibility(View.VISIBLE);
                        mEmContactNumberEditText.setText(R.string.invalid_mobile_no);
                        mEmContactNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        mEmContactNumberEditText.requestFocus();
                        return;
                    } else {
                        mEmContactNumErrorTextView.setVisibility(View.GONE);
                        mEmContactNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
                }
            }
        }

        populateDataAndContinue();


    }

    /**
     * populating all data from fields
     */
    private void populateDataAndContinue() {

        if (mCurrentPhotoPath != null)
            patientdto.setPatientPhoto(mCurrentPhotoPath);
        else
            patientdto.setPatientPhoto(patientdto.getPatientPhoto());

        patientdto.setFirstname(mFirstNameEditText.getText().toString());
        patientdto.setMiddlename(mMiddleNameEditText.getText().toString());
        patientdto.setLastname(mLastNameEditText.getText().toString());
        patientdto.setGender(mGender==null?"":StringUtils.getValue(mGender));
        if (!mPhoneNumberEditText.getText().toString().trim().equals(""))
            patientdto.setPhonenumber(mCountryCodePicker.getFullNumberWithPlus()==null?"":StringUtils.getValue(mCountryCodePicker.getFullNumberWithPlus())); // automatically combines both cc and number togther.
        else
            patientdto.setPhonenumber("");

        patientdto.setDateofbirth(dobToDb);

        if (AgeUtils.Companion.isGuardianRequired(mAgeYears,mAgeMonths,mAgeDays)) {
            patientdto.setGuardianName(mGuardianNameEditText.getText().toString());
            patientdto.setGuardianType(StringUtils.getProvided(mGuardianTypeSpinner));
        } else {
            patientdto.setGuardianName("");
            patientdto.setGuardianType("");
        }

        patientdto.setContactType(StringUtils.getProvided(mContactTypeSpinner));
        patientdto.setEmContactName(mEmContactNameEditText.getText().toString());
        patientdto.setEmContactNumber(mEmContactNoCountryCodePicker.getFullNumberWithPlus()==null?"":StringUtils.getValue(mEmContactNoCountryCodePicker.getFullNumberWithPlus()));


        try {
            Logger.logD(TAG, "insertpatinet");
            boolean isPatientInserted = false;
            boolean isPatientImageInserted = false;
            PatientsDAO patientsDAO = new PatientsDAO();
            PatientAttributesDTO patientAttributesDTO = new PatientAttributesDTO();
            List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

            if (patientdto.getPhonenumber() != null) {
                // mobile no adding in patient attributes.
                patientAttributesDTO = new PatientAttributesDTO();
                patientAttributesDTO.setUuid(UUID.randomUUID().toString());
                patientAttributesDTO.setPatientuuid(patientdto.getUuid());
                patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
                patientAttributesDTO.setValue(patientdto.getPhonenumber()==null?"":StringUtils.getValue(patientdto.getPhonenumber()));
                patientAttributesDTOList.add(patientAttributesDTO);
            }
            patientdto.setPatientAttributesDTOList(patientAttributesDTOList);
            patientdto.setSyncd(false);


            ImagesDAO imagesDAO = new ImagesDAO();

            if (patient_detail) {
                isPatientInserted = patientsDAO.updatePatientToDB_PatientDTO(patientdto, patientdto.getUuid(), patientAttributesDTOList);
                isPatientImageInserted = imagesDAO.updatePatientProfileImages(patientdto.getPatientPhoto(), patientdto.getUuid());
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable("patientDTO", (Serializable) patientdto);
                bundle.putBoolean("fromFirstScreen", true);
                bundle.putBoolean("patient_detail", patient_detail);
                fragment_secondScreen.setArguments(bundle); // passing data to Fragment

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_firstscreen, fragment_secondScreen)
                        .commit();
            }

            if (NetworkConnection.isOnline(getActivity().getApplication())) { // todo: uncomment later jsut for testing added.
                SyncDAO syncDAO = new SyncDAO();
                ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
                boolean push = syncDAO.pushDataApi();
                boolean pushImage = imagesPushDAO.patientProfileImagesPush();
            }

            if (isPatientInserted && isPatientImageInserted) {
                Logger.logD(TAG, "inserted");
                Intent intent = new Intent(getActivity().getApplication(), PatientDetailActivity2.class);
                intent.putExtra("patientUuid", patientdto.getUuid());
                intent.putExtra("patientName", patientdto.getFirstname() + " " + patientdto.getLastname());
                intent.putExtra("tag", "newPatient");
                intent.putExtra("hasPrescription", "false");
                Bundle args = new Bundle();
                args.putSerializable("patientDTO", (Serializable) patientdto);
                intent.putExtra("BUNDLE", args);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Log.i(TAG, "Result OK");
            mCurrentPhotoPath = result.getData().getStringExtra("RESULT");
            Log.v("IdentificationActivity", mCurrentPhotoPath);
            RequestBuilder<Drawable> requestBuilder = Glide.with(getActivity())
                    .asDrawable().sizeMultiplier(0.25f);
            Glide.with(getActivity())
                    .load(new File(mCurrentPhotoPath))
                    .thumbnail(requestBuilder)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(patient_imgview);
        }
    });

}
package org.intelehealth.unicef.activities.identificationActivity;

import static android.app.Activity.RESULT_OK;
import static org.intelehealth.unicef.utilities.StringUtils.inputFilter_Name;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hbb20.CountryCodePicker;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.models.dto.PatientDTO;
import org.intelehealth.unicef.ui2.calendarviewcustom.CustomCalendarViewUI2;
import org.intelehealth.unicef.ui2.calendarviewcustom.SendSelectedDateInterface;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.EditTextUtils;
import org.intelehealth.unicef.utilities.IReturnValues;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.StringUtils;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Fragment_FirstScreen extends Fragment implements SendSelectedDateInterface {
    private View view;
    private String patientUuid = "";
    private ImageView patient_imgview;
    private Button frag1_nxt_btn_main;
    SessionManager sessionManager = null;
    private ImageView personal_icon, address_icon, other_icon;
    EditText mFirstNameEditText, mMiddleNameEditText, mLastNameEditText, mDOBEditText, mAgeEditText, mPhoneNumberEditText;
    private Fragment_SecondScreen fragment_secondScreen;
    PatientDTO patientdto = new PatientDTO();
    private String mGender;
    String uuid = "", dobToDb;
    private String mCurrentPhotoPath;
    RadioButton mGenderMaleRadioButton, mGenderFemaleRadioButton /*, mGenderOthersRadioButton*/;
    private static final String TAG = Fragment_FirstScreen.class.getSimpleName();
    private DatePickerDialog mDOBPicker;
    private MaterialAlertDialogBuilder mAgePicker;
    Calendar dob = Calendar.getInstance();
    Calendar today = Calendar.getInstance();
    private CountryCodePicker mCountryCodePicker;
    TextView mFirstNameErrorTextView, mMiddleNameErrorTextView, mLastNameErrorTextView, mGenderErrorTextView, mDOBErrorTextView, mAgeErrorTextView, mPhoneNumberErrorTextView;
    private int mDOBYear, mDOBMonth, mDOBDay, mAgeYears = 0, mAgeMonths = 0, mAgeDays = 0;
    int dob_indexValue = 15;
    //random value assigned to check while editing. If user didnt updated the dob and just clicked on fab
    //in that case, the edit() will get the dob_indexValue as 15 and we  will check if the
    //dob_indexValue == 15 then just get the dob_edittext editText value and add in the db.
    boolean fromSecondScreen = false;
    String patientID_edit;
    boolean patient_detail = false;
    private static final int GROUP_PERMISSION_REQUEST = 1000;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_firstscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(getActivity());
        patient_imgview = view.findViewById(R.id.patient_imgview);
        frag1_nxt_btn_main = view.findViewById(R.id.frag1_nxt_btn_main);
        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
        mFirstNameEditText = view.findViewById(R.id.firstname_edittext);
        mFirstNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mMiddleNameEditText = view.findViewById(R.id.middlename_edittext);
        mMiddleNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mLastNameEditText = view.findViewById(R.id.lastname_edittext);
        mLastNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Name}); //maxlength 25
        mGenderMaleRadioButton = view.findViewById(R.id.gender_male);
        mGenderFemaleRadioButton = view.findViewById(R.id.gender_female);
//        mGenderOthersRadioButton = view.findViewById(R.id.gender_other);
        mDOBEditText = view.findViewById(R.id.dob_edittext);
        mAgeEditText = view.findViewById(R.id.age_edittext);
        mCountryCodePicker = view.findViewById(R.id.countrycode_spinner);
        mPhoneNumberEditText = view.findViewById(R.id.phoneno_edittext);
        Log.v("phone", "phone value: " + mCountryCodePicker.getSelectedCountryCode());
        mCountryCodePicker.registerCarrierNumberEditText(mPhoneNumberEditText); // attaches the ccp spinner with the edittext
        mCountryCodePicker.setNumberAutoFormattingEnabled(false);

        mFirstNameErrorTextView = view.findViewById(R.id.firstname_error);
        mMiddleNameErrorTextView = view.findViewById(R.id.middlename_error);
        mLastNameErrorTextView = view.findViewById(R.id.lastname_error);
        mGenderErrorTextView = view.findViewById(R.id.gender_error);
        mDOBErrorTextView = view.findViewById(R.id.dob_error);
        mAgeErrorTextView = view.findViewById(R.id.age_error);
        mPhoneNumberErrorTextView = view.findViewById(R.id.phone_error);

        mFirstNameEditText.addTextChangedListener(new MyTextWatcher(mFirstNameEditText));
        mMiddleNameEditText.addTextChangedListener(new MyTextWatcher(mMiddleNameEditText));
        mLastNameEditText.addTextChangedListener(new MyTextWatcher(mLastNameEditText));
        mDOBEditText.addTextChangedListener(new MyTextWatcher(mDOBEditText));
        mAgeEditText.addTextChangedListener(new MyTextWatcher(mAgeEditText));
        //mPhoneNumberEditText.addTextChangedListener(new MyTextWatcher(mPhoneNumberEditText));

        fragment_secondScreen = new Fragment_SecondScreen();
        if (getArguments() != null) {
            patientdto = (PatientDTO) getArguments().getSerializable("patientDTO");
            //   patientID_edit = getArguments().getString("patientUuid");
            patient_detail = getArguments().getBoolean("patient_detail");
            fromSecondScreen = getArguments().getBoolean("fromSecondScreen");

/*
            if (patientdto.getPatientPhoto() != null) {
                Glide.with(getActivity())
                        .load(new File(patientdto.getPatientPhoto()))
                        .thumbnail(0.25f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(patient_imgview);
            }
*/
        }

        // Setting up the screen when user came from Second screen.
        if (fromSecondScreen) {
            mFirstNameEditText.setText(patientdto.getFirstname());
            mMiddleNameEditText.setText(patientdto.getMiddlename());
            mLastNameEditText.setText(patientdto.getLastname());

            dobToDb = patientdto.getDateofbirth();

            mDOBEditText.setText(DateAndTimeUtils.getDisplayDateForApp(dobToDb));

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
            //   phoneno_edittext.setText(patientdto.getPhonenumber());

            // Gender edit
            if (patientdto.getGender().equals("M")) {
                mGenderMaleRadioButton.setChecked(true);
                if (mGenderFemaleRadioButton.isChecked())
                    mGenderFemaleRadioButton.setChecked(false);
//                if (mGenderOthersRadioButton.isChecked())
//                    mGenderOthersRadioButton.setChecked(false);
                Log.v(TAG, "yes");
            } else if (patientdto.getGender().equals("F")) {
                mGenderFemaleRadioButton.setChecked(true);
                if (mGenderMaleRadioButton.isChecked())
                    mGenderMaleRadioButton.setChecked(false);
//                if (mGenderOthersRadioButton.isChecked())
//                    mGenderOthersRadioButton.setChecked(false);
                Log.v(TAG, "yes");
            } else {
//                mGenderOthersRadioButton.setChecked(true);
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
                Glide.with(getActivity())
                        .load(new File(patientdto.getPatientPhoto()))
                        .thumbnail(0.25f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(patient_imgview);

            }
        }

        setMobileNumberLimit();


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
                Log.d(TAG, "getSelectedDate: " + dateToshow1 + ", " + splitedDate[2]);
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

        MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
            if (this.editText.getId() == R.id.firstname_edittext) {
                if (val.isEmpty()) {
                    mFirstNameErrorTextView.setVisibility(View.VISIBLE);
                    mFirstNameErrorTextView.setText(getString(R.string.error_field_required));
                    mFirstNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mFirstNameErrorTextView.setVisibility(View.GONE);
                    mFirstNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.middlename_edittext) {
               /* if (val.isEmpty()) {
                    mMiddleNameErrorTextView.setVisibility(View.VISIBLE);
                    mMiddleNameErrorTextView.setText(getString(R.string.error_field_required));
                    mMiddleNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mMiddleNameErrorTextView.setVisibility(View.GONE);
                    mMiddleNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }*/
            } else if (this.editText.getId() == R.id.lastname_edittext) {
                if (val.isEmpty()) {
                    mLastNameErrorTextView.setVisibility(View.VISIBLE);
                    mLastNameErrorTextView.setText(getString(R.string.error_field_required));
                    mLastNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mLastNameErrorTextView.setVisibility(View.GONE);
                    mLastNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.dob_edittext) {
                if (val.isEmpty()) {
                    mDOBErrorTextView.setVisibility(View.VISIBLE);
                    mDOBErrorTextView.setText(getString(R.string.error_field_required));
                    mDOBEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mDOBErrorTextView.setVisibility(View.GONE);
                    mDOBEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.age_edittext) {
                if (val.isEmpty()) {
                    mAgeErrorTextView.setVisibility(View.VISIBLE);
                    mAgeErrorTextView.setText(getString(R.string.error_field_required));
                    mAgeEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mAgeErrorTextView.setVisibility(View.GONE);
                    mAgeEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (this.editText.getId() == R.id.phoneno_edittext) {
                if (val.isEmpty()) {
                    mPhoneNumberErrorTextView.setVisibility(View.VISIBLE);
                    mPhoneNumberErrorTextView.setText(getString(R.string.error_field_required));
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                } else {
                    mPhoneNumberErrorTextView.setVisibility(View.GONE);
                    mPhoneNumberEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_unselected));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon_unselected));


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
//        mGenderOthersRadioButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onRadioButtonClicked(v);
//            }
//        });

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

        // Age - end
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            takePicture();
        }
    }

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
                checkPerm();
            } else {
                showPermissionDeniedAlert(permissions);
            }

        }
    }

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

        positiveButton.setTextColor(getResources().getColor(org.intelehealth.apprtc.R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(org.intelehealth.apprtc.R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(getActivity(), alertDialog);
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
        startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
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
        /*if (patientID_edit != null) {
            patientdto.setUuid(patientID_edit);
        } else if (patientdto.getUuid() != null){
          //  patientdto.setUuid(uuid);
        }
        else {
            patientdto.setUuid(uuid);
        }*/

        if (patient_detail) {
            //   patientdto.setUuid(patientID_edit);
        } else {
            patientdto.setUuid(uuid);
        }


        //frag1_nxt_btn_main.setBackground(getResources().getDrawable(R.drawable.disabled_patient_reg_btn));
        if (mFirstNameEditText.getText().toString().equals("")) {
            mFirstNameErrorTextView.setVisibility(View.VISIBLE);
            mFirstNameErrorTextView.setText(getString(R.string.error_field_required));
            mFirstNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mFirstNameEditText.requestFocus();
            return;
        } else {
            mFirstNameErrorTextView.setVisibility(View.GONE);
            mFirstNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }

       /* if (mMiddleNameEditText.getText().toString().equals("")) {
            mMiddleNameErrorTextView.setVisibility(View.VISIBLE);
            mMiddleNameErrorTextView.setText(getString(R.string.error_field_required));
            mMiddleNameEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mMiddleNameEditText.requestFocus();
            return;
        } else {
            mMiddleNameErrorTextView.setVisibility(View.GONE);
            mMiddleNameEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }*/

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
        if (!mGenderFemaleRadioButton.isChecked() && !mGenderMaleRadioButton.isChecked() /*&& !mGenderOthersRadioButton.isChecked()*/) {
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

        if (!mPhoneNumberEditText.getText().toString().equals("")) {
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
            // Indian mobile number max
        }

        // mobile no - start

        // mobile no - end

        if (mCurrentPhotoPath != null)
            patientdto.setPatientPhoto(mCurrentPhotoPath);
        else
            patientdto.setPatientPhoto(patientdto.getPatientPhoto());

        patientdto.setFirstname(mFirstNameEditText.getText().toString());
        patientdto.setMiddlename(mMiddleNameEditText.getText().toString());
        patientdto.setLastname(mLastNameEditText.getText().toString());
        patientdto.setGender(StringUtils.getValue(mGender));

        //get unformatted number with prefix "+" i.e "+14696641766"
        //   patientdto.setPhonenumber(StringUtils.getValue(countryCodePicker.getFullNumberWithPlus()));
        if (!mPhoneNumberEditText.getText().toString().trim().equals(""))
            patientdto.setPhonenumber(StringUtils.getValue(mCountryCodePicker.getFullNumberWithPlus())); // automatically combines both cc and number togther.
        else
            patientdto.setPhonenumber("");
        patientdto.setDateofbirth(dobToDb);
        /*String[] dob_array = mDOBEditText.getText().toString().split(" ");
        Log.d("dob_array", "0: " + dob_array[0]);
        Log.d("dob_array", "0: " + dob_array[1]);
        Log.d("dob_array", "0: " + dob_array[2]);

        //get month index and return English value for month.
        if (dob_indexValue == 15) {
            String dob = StringUtils.hi_or_bn_en_noEdit
                    (mDOBEditText.getText().toString(), sessionManager.getAppLanguage());
            patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                    (StringUtils.getValue(dob)));
        } else {
            String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
            dob_array[1] = dob_array[1].replace(dob_array[1], dob);
            String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];
            patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                    (StringUtils.getValue(dob_value)));
        }*/

        // Bundle data
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientdto);
        bundle.putBoolean("fromFirstScreen", true);
        bundle.putBoolean("patient_detail", patient_detail);
        //   bundle.putString("patientUuid", patientID_edit);
        fragment_secondScreen.setArguments(bundle); // passing data to Fragment

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, fragment_secondScreen)
                .commit();
        // end
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
                        .skipMemoryCache(true)
                        .into(patient_imgview);
            }
        }
    }


}
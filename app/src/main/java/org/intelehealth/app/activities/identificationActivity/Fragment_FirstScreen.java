package org.intelehealth.app.activities.identificationActivity;

import static android.app.Activity.RESULT_OK;
import static org.intelehealth.app.utilities.StringUtils.en__as_dob;
import static org.intelehealth.app.utilities.StringUtils.en__bn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__gu_dob;
import static org.intelehealth.app.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.app.utilities.StringUtils.en__kn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ml_dob;
import static org.intelehealth.app.utilities.StringUtils.en__mr_dob;
import static org.intelehealth.app.utilities.StringUtils.en__or_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ru_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ta_dob;
import static org.intelehealth.app.utilities.StringUtils.en__te_dob;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.EditTextUtils;
import org.intelehealth.app.utilities.IReturnValues;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Fragment_FirstScreen extends Fragment {
    private View view;
    private String patientUuid = "";
    private ImageView patient_imgview;
    private Button frag1_nxt_btn_main;
    SessionManager sessionManager = null;
    private ImageView personal_icon, address_icon, other_icon;
    EditText firstname_edittext, middlename_edittext, lastname_edittext,
            dob_edittext, age_edittext, phoneno_edittext;
    private Fragment_SecondScreen fragment_secondScreen;
    PatientDTO patientdto = new PatientDTO();
    private String mGender;
    String uuid = "";
    private String mCurrentPhotoPath;
    RadioButton gender_male, gender_female, gender_other;
    private static final String TAG = Fragment_FirstScreen.class.getSimpleName();
    private DatePickerDialog mDOBPicker;
    private MaterialAlertDialogBuilder mAgePicker;
    Calendar dob = Calendar.getInstance();
    Calendar today = Calendar.getInstance();
    private CountryCodePicker countryCodePicker;
    TextView firstname_error, middlename_error, lastname_error, gender_error, dob_error, age_error, phone_error;
    private int mDOBYear, mDOBMonth, mDOBDay, mAgeYears = 0, mAgeMonths = 0, mAgeDays = 0;
    int dob_indexValue = 15;
    //random value assigned to check while editing. If user didnt updated the dob and just clicked on fab
    //in that case, the edit() will get the dob_indexValue as 15 and we  will check if the
    //dob_indexValue == 15 then just get the dob_edittext editText value and add in the db.
    boolean fromSecondScreen = false;
    String patientID_edit;


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

        firstname_edittext = view.findViewById(R.id.firstname_edittext);
        middlename_edittext = view.findViewById(R.id.middlename_edittext);
        lastname_edittext = view.findViewById(R.id.lastname_edittext);
        
        gender_male = view.findViewById(R.id.gender_male);
        gender_female = view.findViewById(R.id.gender_female);
        gender_other = view.findViewById(R.id.gender_other);
        dob_edittext = view.findViewById(R.id.dob_edittext);
        age_edittext = view.findViewById(R.id.age_edittext);
        countryCodePicker = view.findViewById(R.id.countrycode_spinner);
        phoneno_edittext = view.findViewById(R.id.phoneno_edittext);
        countryCodePicker.registerCarrierNumberEditText(phoneno_edittext); // attaches the ccp spinner with the edittext


        firstname_error = view.findViewById(R.id.firstname_error);
        middlename_error = view.findViewById(R.id.middlename_error);
        lastname_error = view.findViewById(R.id.lastname_error);
        gender_error = view.findViewById(R.id.gender_error);
        dob_error = view.findViewById(R.id.dob_error);
        age_error = view.findViewById(R.id.age_error);
        phone_error = view.findViewById(R.id.phone_error);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragment_secondScreen = new Fragment_SecondScreen();
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_unselected));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon_unselected));

        if (getArguments() != null) {
            patientdto = (PatientDTO) getArguments().getSerializable("patientDTO");
            patientID_edit = getArguments().getString("patientUuid");
            fromSecondScreen = getArguments().getBoolean("fromSecondScreen");
        }

        // Setting up the screen when user came from Second screen.
        if (fromSecondScreen) {
            firstname_edittext.setText(patientdto.getFirstname());
            middlename_edittext.setText(patientdto.getMiddlename());
            lastname_edittext.setText(patientdto.getLastname());
            
            //if patient update then age will be set
                //dob to be displayed based on translation...
                String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patientdto.getDateofbirth());
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String dob_text = en__te_dob(dob); //to show text of English into Telugu...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String dob_text = en__mr_dob(dob); //to show text of English into marathi...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String dob_text = en__as_dob(dob); //to show text of English into assame...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String dob_text = en__ml_dob(dob); //to show text of English into malyalum...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String dob_text = en__kn_dob(dob); //to show text of English into kannada...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String dob_text = en__ru_dob(dob); //to show text of English into kannada...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String dob_text = en__gu_dob(dob); //to show text of English into Gujarati...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String dob_text = en__bn_dob(dob); //to show text of English into Bengali...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String dob_text = en__ta_dob(dob); //to show text of English into Tamil...
                    dob_edittext.setText(dob_text);
                } else {
                    dob_edittext.setText(dob);
                }

                // dob_edittext.setText(DateAndTimeUtils.getFormatedDateOfBirthAsView(patient1.getDate_of_birth()));
                //get year month days
                String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patientdto.getDateofbirth(), getActivity());

                String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patientdto.getDateofbirth()).split(" ");
                mAgeYears = Integer.valueOf(ymdData[0]);
                mAgeMonths = Integer.valueOf(ymdData[1]);
                mAgeDays = Integer.valueOf(ymdData[2]);
                String age = mAgeYears + getResources().getString(R.string.identification_screen_text_years) + " - " +
                        mAgeMonths + getResources().getString(R.string.identification_screen_text_months) + " - " +
                        mAgeDays + getResources().getString(R.string.days);
                age_edittext.setText(age);

            countryCodePicker.setFullNumber(patientdto.getPhonenumber()); // automatically assigns cc to spinner and number to edittext field.
         //   phoneno_edittext.setText(patientdto.getPhonenumber());
            
            // Gender edit
                if (patientdto.getGender().equals("M")) {
                    gender_male.setChecked(true);
                    if (gender_female.isChecked())
                        gender_female.setChecked(false);
                    if (gender_other.isChecked())
                        gender_other.setChecked(false);
                    Log.v(TAG, "yes");
                } else if (patientdto.getGender().equals("F")) {
                    gender_female.setChecked(true);
                    if (gender_male.isChecked())
                        gender_male.setChecked(false);
                    if (gender_other.isChecked())
                        gender_other.setChecked(false);
                    Log.v(TAG, "yes");
                } else {
                    gender_other.setChecked(true);
                    if (gender_male.isChecked())
                        gender_male.setChecked(false);
                    if (gender_female.isChecked())
                        gender_female.setChecked(false);
                }
                
            if (gender_male.isChecked()) {
                mGender = "M";
            } else if (gender_female.isChecked()) {
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

        // next btn click
        frag1_nxt_btn_main.setOnClickListener(v -> {
            onPatientCreateClicked();
        });


        // Gender - start
        if (gender_male.isChecked()) {
            mGender = "M";
        } else if (gender_female.isChecked()) {
            mGender = "F";
        } else {
            mGender = "O";
        }

        // setting patient profile
        patient_imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientTemp = "";
                if (patientUuid.equalsIgnoreCase("")) {
                  //  patientTemp = patientID_edit; // todo: uncomment later
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
        });


        gender_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        gender_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        gender_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        // Gender - end
        
        // DOB - start
        mDOBPicker = new DatePickerDialog(getActivity(), R.style.datepicker,
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //Set the DOB calendar to the date selected by the user
                dob.set(year, monthOfYear, dayOfMonth);
                dob_edittext.setError(null);
                age_edittext.setError(null);

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
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String dob_text = en__te_dob(dobString); //to show text of English into telugu...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String dob_text = en__mr_dob(dobString); //to show text of English into telugu...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String dob_text = en__as_dob(dobString); //to show text of English into telugu...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String dob_text = en__ml_dob(dobString); //to show text of English into telugu...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String dob_text = en__kn_dob(dobString); //to show text of English into telugu...
                    dob_edittext.setText(dob_text);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String dob_text = en__ru_dob(dobString); //to show text of English into telugu...
                    dob_edittext.setText(dob_text);
                } else {
                    dob_edittext.setText(dobString);
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
                age_edittext.setText(ageS);

            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE)); // so that todays date as shown as default selection.

        //DOB Picker is shown when clicked
        mDOBPicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        dob_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDOBPicker.show();
            }
        });
        // DOB - end
        
        // Age - start
        age_edittext.setOnClickListener(new View.OnClickListener() {
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
                    age_edittext.setText(ageString);


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
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        String dob_text = en__or_dob(dobString); //to show text of English into Odiya...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                        String dob_text = en__ta_dob(dobString); //to show text of English into Tamil...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                        String dob_text = en__gu_dob(dobString); //to show text of English into Gujarati...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        String dob_text = en__te_dob(dobString); //to show text of English into telugu...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                        String dob_text = en__mr_dob(dobString); //to show text of English into marathi...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                        String dob_text = en__as_dob(dobString); //to show text of English into assame...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                        String dob_text = en__ml_dob(dobString);
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                        String dob_text = en__kn_dob(dobString); //to show text of English into kannada...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                        String dob_text = en__ru_dob(dobString); //to show text of English into kannada...
                        dob_edittext.setText(dob_text);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                        String dob_text = en__bn_dob(dobString); //to show text of English into Bengali...
                        dob_edittext.setText(dob_text);
                    } else {
                        dob_edittext.setText(dobString);
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
                    mGender = "Other";
                Log.v(TAG, "gender: " + mGender);
                break;
        }
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
        if (patientID_edit != null) {
            patientdto.setUuid(patientID_edit);
        }
        else {
            patientdto.setUuid(uuid);
        }

            frag1_nxt_btn_main.setBackground(getResources().getDrawable(R.drawable.disabled_patient_reg_btn));
            if (firstname_edittext.getText().toString().equals("")) {
                firstname_error.setVisibility(View.VISIBLE);
                return;
            }
            else {
                firstname_error.setVisibility(View.GONE);
            }

            if (middlename_edittext.getText().toString().equals("")) {
                middlename_error.setVisibility(View.VISIBLE);
                return;
            }
            else {
                middlename_error.setVisibility(View.GONE);
            }

            if (lastname_edittext.getText().toString().equals("")) {
                //   mLastName.setError(getString(R.string.error_field_required));
                lastname_error.setVisibility(View.VISIBLE);
                return;
            }
            else {
                lastname_error.setVisibility(View.GONE);
            }

            // gender valid - start
            if (!gender_female.isChecked() && !gender_male.isChecked() && !gender_other.isChecked()) {
                gender_error.setVisibility(View.VISIBLE);
                return;
            }
            else {
                gender_error.setVisibility(View.GONE);
            }
            // gender valid - end

            if (dob_edittext.getText().toString().equals("")) {
                //  dob_edittext.setError(getString(R.string.error_field_required));
                dob_error.setVisibility(View.VISIBLE);
                return;
            }
            else {
                dob_error.setVisibility(View.GONE);
            }

            if (age_edittext.getText().toString().equals("")) {
                //   age_edittext.setError(getString(R.string.error_field_required));
                age_error.setVisibility(View.VISIBLE);
                return;
            }
            else {
                age_error.setVisibility(View.GONE);
            }

            if (phoneno_edittext.getText().toString().equals("")) {
                phone_error.setVisibility(View.VISIBLE);
                return;
            }
            else {
                phone_error.setVisibility(View.GONE);
            }

            // mobile no - start
//        String s = phoneno_edittext.getText().toString().replaceAll("\\s+", "");
            if (phoneno_edittext.getText().toString().replaceAll("\\s+", "").length() > 0) {
                if (phoneno_edittext.getText().toString().replaceAll("\\s+", "").length() < 10) {
                    phoneno_edittext.requestFocus();
                    phoneno_edittext.setError(getString(R.string.enter_10_digits));
                    return;
                }
                phone_error.setVisibility(View.VISIBLE);
            }
            else {
                phone_error.setVisibility(View.GONE);
            }
            // mobile no - end

            patientdto.setPatientPhoto(mCurrentPhotoPath);
            patientdto.setFirstname(firstname_edittext.getText().toString());
            patientdto.setMiddlename(middlename_edittext.getText().toString());
            patientdto.setLastname(lastname_edittext.getText().toString());
            patientdto.setGender(StringUtils.getValue(mGender));

            //get unformatted number with prefix "+" i.e "+14696641766"
         //   patientdto.setPhonenumber(StringUtils.getValue(countryCodePicker.getFullNumberWithPlus()));
            patientdto.setPhonenumber(StringUtils.getValue(countryCodePicker.getFullNumberWithPlus())); // automatically combines both cc and number togther.

            String[] dob_array = dob_edittext.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or_bn_en_noEdit
                        (dob_edittext.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
                dob_array[1] = dob_array[1].replace(dob_array[1], dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];
                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob_value)));
            }

            // Bundle data
            Bundle bundle = new Bundle();
            bundle.putSerializable("patientDTO", (Serializable) patientdto);
            bundle.putBoolean("fromSecondScreen", true);
            bundle.putString("patientUuid", patientID_edit);
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

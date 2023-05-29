package org.intelehealth.ezazi.activities.identificationActivity;

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

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.cameraActivity.CameraActivity;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
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
import java.text.SimpleDateFormat;
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
    private int mDOBDay;
    private DatePickerDialog mDOBPicker;
    private int mAgeYears = 0;
    private int mAgeMonths = 0;
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
    EditText mAddress1;
    EditText mAddress2;
    AutoCompleteTextView mCity;
    EditText mPostal;
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
    int dob_indexValue = 15;
    //random value assigned to check while editing. If user didnt updated the dob and just clicked on fab
    //in that case, the edit() will get the dob_indexValue as 15 and we  will check if the
    //dob_indexValue == 15 then just get the mDOB editText value and add in the db.

    String OtherEditText;


    /*New*/
    private EditText mTotalBirthEditText, mTotalMiscarriageEditText;
    private RadioGroup mLaborOnsetRadioGroup;
    private RadioGroup mHospitalMaternityRadioGroup;
    private TextView mActiveLaborDiagnosedDateTextView, mActiveLaborDiagnosedTimeTextView;
    private TextView mMembraneRupturedDateTextView, mMembraneRupturedTimeTextView;
    private TextView mRiskFactorsTextView, mPrimaryDoctorTextView, mSecondaryDoctorTextView;
    private CheckBox mUnknownMembraneRupturedCheckBox;

    private EditText mOthersEditText, mAlternateNumberEditText, mWifeDaughterOfEditText;
    private TextView mAdmissionDateTextView, mAdmissionTimeTextView;

    // strings
    private String mTotalBirthCount = "0", mTotalMiscarriageCount = "0";
    private String mLaborOnsetString = "";
    private String mHospitalMaternityString = "";
    private String mActiveLaborDiagnosedDate = "", mActiveLaborDiagnosedTime = "";
    private String mMembraneRupturedDate = "", mMembraneRupturedTime = "";
    private String mRiskFactorsString = "", mPrimaryDoctorUUIDString = "", mSecondaryDoctorUUIDString = "";
    private List<String> mSelectedRiskFactorList = new ArrayList<String>();

    private String mAlternateNumberString = "", mWifeDaughterOfString = "";
    private String mAdmissionDateString = "", mAdmissionTimeString = "";
    private String mOthersString = "";

    private boolean mIsEditMode = false;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    private List<ProviderDTO> mProviderDoctorList = new ArrayList<ProviderDTO>();
    private String[] mDoctorNames;
    private List<String> mDoctorUUIDs = new ArrayList<>();
    /*end*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        Log.d("lang", "lang: " + language);
        //In case of crash still the org should hold the current lang fix.
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

        /*new*/
        ProviderDAO providerDAO = new ProviderDAO();
        try {
            mProviderDoctorList = providerDAO.getDoctorList();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        mOthersEditText = findViewById(R.id.etvOthersText);
        mAlternateNumberEditText = findViewById(R.id.identification_alt_phone_number);
        mWifeDaughterOfEditText = findViewById(R.id.etv_wife_daughter_of);
        mAdmissionDateTextView = findViewById(R.id.tvAdmissionDate);
        mAdmissionTimeTextView = findViewById(R.id.tvAdmissionTime);

        mTotalBirthEditText = findViewById(R.id.etvTotalBirth);
        mTotalMiscarriageEditText = findViewById(R.id.etvTotalMiscarriage);
        mLaborOnsetRadioGroup = findViewById(R.id.rgLaborOnset);
        mHospitalMaternityRadioGroup = findViewById(R.id.rgHospitalMaternity);
        mActiveLaborDiagnosedDateTextView = findViewById(R.id.tvActiveLaborDiagnosedDate);
        mActiveLaborDiagnosedTimeTextView = findViewById(R.id.tvActiveLaborDiagnosedTime);
        mMembraneRupturedDateTextView = findViewById(R.id.tvMembraneRupturedDate);
        mMembraneRupturedTimeTextView = findViewById(R.id.tvMembraneRupturedTime);
        mRiskFactorsTextView = findViewById(R.id.tvRiskFactors);
        mPrimaryDoctorTextView = findViewById(R.id.tvPrimaryDoctor);
        mSecondaryDoctorTextView = findViewById(R.id.tvSecondaryDoctor);
        mUnknownMembraneRupturedCheckBox = findViewById(R.id.cbUnknownMembraneRuptured);

        mAdmissionDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCalendar = Calendar.getInstance();
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(IdentificationActivity.this, R.style.DatePicker_Theme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, month, dayOfMonth);
                        Date date = cal.getTime();

                        mAdmissionDateString = simpleDateFormat.format(date);
                        mAdmissionDateTextView.setText(mAdmissionDateString);
                    }
                }, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        mAdmissionTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(IdentificationActivity.this, R.style.DatePicker_Theme,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                boolean isPM = (hourOfDay >= 12);
                                mAdmissionTimeString = String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                                mAdmissionTimeTextView.setText(mAdmissionTimeString);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        mUnknownMembraneRupturedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mMembraneRupturedDateTextView.setVisibility(View.GONE);
                    mMembraneRupturedTimeTextView.setVisibility(View.GONE);
                } else {
                    mMembraneRupturedDateTextView.setVisibility(View.VISIBLE);
                    mMembraneRupturedTimeTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        mActiveLaborDiagnosedDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCalendar = Calendar.getInstance();
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(IdentificationActivity.this, R.style.DatePicker_Theme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, month, dayOfMonth);
                        Date date = cal.getTime();

                        mActiveLaborDiagnosedDate = simpleDateFormat.format(date);
                        mActiveLaborDiagnosedDateTextView.setText(mActiveLaborDiagnosedDate);
                    }
                }, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
        mActiveLaborDiagnosedTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(IdentificationActivity.this, R.style.DatePicker_Theme,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                boolean isPM = (hourOfDay >= 12);
                                mActiveLaborDiagnosedTime = String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                                mActiveLaborDiagnosedTimeTextView.setText(mActiveLaborDiagnosedTime);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        mMembraneRupturedDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCalendar = Calendar.getInstance();
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(IdentificationActivity.this, R.style.DatePicker_Theme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, month, dayOfMonth);
                        Date date = cal.getTime();

                        mMembraneRupturedDate = simpleDateFormat.format(date);
                        mMembraneRupturedDateTextView.setText(mMembraneRupturedDate);
                    }
                }, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
        mMembraneRupturedTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(IdentificationActivity.this, R.style.DatePicker_Theme,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                boolean isPM = (hourOfDay >= 12);
                                mMembraneRupturedTime = String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM");
                                mMembraneRupturedTimeTextView.setText(mMembraneRupturedTime);

                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });
        mRiskFactorsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] items = {"None", "under age 20", "Women over age 35", "Diabetes", "Obesity", "Underweight",
                        "High blood pressure", "PCOS", "Kidney disease", "Thyroid disease", "Asthma", "Uterine fibroids"};
                boolean[] selectedItems = new boolean[items.length];
                for (int i = 0; i < items.length; i++) {
                    selectedItems[i] = mSelectedRiskFactorList.contains(items[i]);
                }
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(IdentificationActivity.this);

                builder.setTitle("Select Risk Factors")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setMultiChoiceItems(items, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int itemIndex, boolean isChecked) {
                                Log.i("Dialogos", "Opción elegida: " + items[itemIndex]);
                                if (isChecked) {
                                    if (itemIndex == 0) {
                                        mSelectedRiskFactorList.clear();
                                    } else {
                                        mSelectedRiskFactorList.remove(items[0]);
                                    }
                                    if (!mSelectedRiskFactorList.contains(items[itemIndex]))
                                        mSelectedRiskFactorList.add(items[itemIndex]);
                                } else {
                                    mSelectedRiskFactorList.remove(items[itemIndex]);
                                }
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < mSelectedRiskFactorList.size(); i++) {
                                    if (!stringBuilder.toString().isEmpty())
                                        stringBuilder.append(",");
                                    stringBuilder.append(mSelectedRiskFactorList.get(i));

                                }
                                mRiskFactorsString = stringBuilder.toString();
                                mRiskFactorsTextView.setText(mRiskFactorsString);
                            }
                        });


                builder.create().show();
            }
        });

        mPrimaryDoctorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ProviderDTO> providerDoctorList = new ArrayList<>();
                for (int i = 0; i < mProviderDoctorList.size(); i++) {
                    if (!mSecondaryDoctorUUIDString.equals(mProviderDoctorList.get(i).getUserUuid())) {
                        providerDoctorList.add(mProviderDoctorList.get(i));
                    }
                }
                mDoctorNames = new String[providerDoctorList.size()];
                mDoctorUUIDs.clear();
                int selectedId = 0;

                for (int i = 0; i < providerDoctorList.size(); i++) {
                    mDoctorNames[i] = providerDoctorList.get(i).getGivenName() + " " + providerDoctorList.get(i).getFamilyName();
                    mDoctorUUIDs.add(providerDoctorList.get(i).getUserUuid());
                    if (mPrimaryDoctorUUIDString.equals(providerDoctorList.get(i).getUserUuid()))
                        selectedId = i;
                }
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(IdentificationActivity.this);

                builder.setTitle("Select Primary Doctor")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setSingleChoiceItems(mDoctorNames, selectedId, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPrimaryDoctorUUIDString = mDoctorUUIDs.get(which);
                                mPrimaryDoctorTextView.setText(mDoctorNames[which]);
                            }
                        });
                builder.create().show();
            }
        });

        mSecondaryDoctorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrimaryDoctorUUIDString.isEmpty()) {
                    Toast.makeText(context, "Please select the primary doctor", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ProviderDTO> providerDoctorList = new ArrayList<>();
                for (int i = 0; i < mProviderDoctorList.size(); i++) {
                    if (!mPrimaryDoctorUUIDString.equals(mProviderDoctorList.get(i).getUserUuid())) {
                        providerDoctorList.add(mProviderDoctorList.get(i));
                    }
                }
                mDoctorNames = new String[providerDoctorList.size()];
                mDoctorUUIDs.clear();
                int selectedId = 0;
                for (int i = 0; i < providerDoctorList.size(); i++) {
                    mDoctorNames[i] = providerDoctorList.get(i).getGivenName() + " " + providerDoctorList.get(i).getFamilyName();
                    mDoctorUUIDs.add(providerDoctorList.get(i).getUserUuid());
                    if (mSecondaryDoctorUUIDString.equals(providerDoctorList.get(i).getUserUuid()))
                        selectedId = i;
                }
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(IdentificationActivity.this);

                builder.setTitle("Select Secondary Doctor")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setSingleChoiceItems(mDoctorNames, selectedId, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSecondaryDoctorUUIDString = mDoctorUUIDs.get(which);
                                mSecondaryDoctorTextView.setText(mDoctorNames[which]);
                            }
                        });
                builder.create().show();
            }
        });

        mLaborOnsetRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbLOSpontaneous) {
                    mLaborOnsetString = "Spontaneous";
                } else if (checkedId == R.id.rbLOInduced) {
                    mLaborOnsetString = "Induced";
                }
            }
        });

        mHospitalMaternityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mOthersEditText.setVisibility(View.GONE);
                if (checkedId == R.id.rbHospital) {
                    mHospitalMaternityString = "Hospital";
                } else if (checkedId == R.id.rbMaternity) {
                    mHospitalMaternityString = "Maternity";
                } else {
                    mHospitalMaternityString = " ";
                    mOthersEditText.setVisibility(View.VISIBLE);
//                    mOthersEditText.setText(mHospitalMaternityString);
                }
            }
        });
        /*end*/

        i_privacy = getIntent();
        context = IdentificationActivity.this;
        privacy_value = i_privacy.getStringExtra("privacy"); //privacy_accept value retrieved from previous act.

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        mCity = findViewById(R.id.identification_city);
        mCity.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_Others}); //maxlength 25

        stateText = findViewById(R.id.identification_state);
        mState = findViewById(R.id.spinner_state);
        mPostal = findViewById(R.id.identification_postal_code);
        countryText = findViewById(R.id.identification_country);
        mCountry = findViewById(R.id.spinner_country);
        mGenderM = findViewById(R.id.identification_gender_male);
        mGenderF = findViewById(R.id.identification_gender_female);
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
//Initialize the local database to store patient information

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("patientUuid")) {
                mIsEditMode = true;
                this.setTitle(R.string.update_patient_identification);
                patientID_edit = intent.getStringExtra("patientUuid");
                patient1.setUuid(patientID_edit);
                setscreen(patientID_edit);
                updateUI(patient1);
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

        if (patient1.getPatient_photo() != null && !patient1.getPatient_photo().trim().isEmpty())
            mImageView.setImageBitmap(BitmapFactory.decodeFile(patient1.getPatient_photo()));

        Resources res = getResources();
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, R.layout.custom_spinner);
        //countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountry.setAdapter(countryAdapter);
//        ArrayAdapter<CharSequence> countryAdapter = null;
//        try {
//
//            String mCountriesLanguage = "countries_" + sessionManager.getAppLanguage();
//            int country = res.getIdentifier(mCountriesLanguage, "array", getApplicationContext().getPackageName());
//            if (country != 0) {
//                countryAdapter = ArrayAdapter.createFromResource(this,
//                        country, R.layout.custom_spinner);
//
//            }
//            mCountry.setAdapter(countryAdapter);
//        } catch (Exception e) {
////            Toast.makeText(this, R.string.education_values_missing, Toast.LENGTH_SHORT).show();
//            Logger.logE("Identification", "#648", e);
//        }


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
//            mCountry.setSelection(countryAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_hi_en_te_Country_edit(patient1.getCountry(),sessionManager.getAppLanguage()))));


            /*if (patient1.getEducation_level().equals(getResources().getString(R.string.not_provided)))
                mEducation.setSelection(0);
//            else
//                mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);

            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String education = switch_hi_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String education = switch_or_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String education = switch_te_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String education = switch_mr_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String education = switch_as_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if(sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String education = switch_gu_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                }
                else if(sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String education = switch_ta_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                }
                else if(sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String education = switch_bn_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                }
                else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String education = switch_ml_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String education = switch_kn_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String education = switch_ru_education_edit(patient1.getEducation_level());
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(education) : 0);
                } else {
                    mEducation.setSelection(educationAdapter != null ? educationAdapter.getPosition(patient1.getEducation_level()) : 0);
                }
            }

            if (educationAdapter == null) {
                Toast.makeText(context, "Education Level: " + patient1.getEducation_level(), Toast.LENGTH_LONG).show();
            }*/


            /*if (patient1.getEconomic_status().equals(getResources().getString(R.string.not_provided)))
                mEconomicStatus.setSelection(0);
//            else
//                mEconomicStatus.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));

            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String economic = switch_hi_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String economic = switch_or_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String economic = switch_te_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                }  else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String economic = switch_mr_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                }else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String economic = switch_as_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                }else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String economic = switch_ml_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String economic = switch_kn_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String economic = switch_ru_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                } else if(sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String economic = switch_gu_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                }
                else if(sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String economic = switch_bn_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                }
                else if(sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String economic = switch_ta_economic_edit(patient1.getEconomic_status());
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(economic));
                }
                else {
                    mEconomicStatus.setSelection(economicStatusAdapter.getPosition(patient1.getEconomic_status()));
                }
            }

            if (patient1.getCaste().equals(getResources().getString(R.string.not_provided)))
                mCaste.setSelection(0);
//            else
//                mCaste.setSelection(casteAdapter.getPosition(patient1.getCaste()));

            else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String caste = switch_hi_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String caste = switch_or_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String caste = switch_te_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String caste = switch_mr_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String caste = switch_as_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String caste = switch_ml_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                }else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String caste = switch_kn_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String caste = switch_ru_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                } else if(sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String caste = switch_gu_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                }
                else if(sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String caste = switch_bn_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                }
                else if(sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String caste = switch_ta_caste_edit(patient1.getCaste());
                    mCaste.setSelection(casteAdapter.getPosition(caste));
                }
                else {
                    mCaste.setSelection(casteAdapter.getPosition(patient1.getCaste()));
                }

            }*/

        } else {
            mCountry.setSelection(countryAdapter.getPosition(country1));
        }

        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this, R.array.state_error, R.layout.custom_spinner);
        //  stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(stateAdapter);

        mCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    String country = adapterView.getItemAtPosition(i).toString();
//                    ArrayAdapter<CharSequence> stateAdapter = null;
                /*todo for All Language Changes Regarding...
                  if (country.matches(getResources().getString(R.string.str_check_India))) {

                        try {
                            String mStateLanguage = "states_india_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStateLanguage, "array", getApplicationContext().getPackageName());

                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                        state, R.layout.custom_spinner);
                            }
                            mState.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }

                        if (patientID_edit != null)
//                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));

                        mState.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_hi_en_te_State_edit(patient1.getState_province(),sessionManager.getAppLanguage()))));

                        else
//                            mState.setSelection(0);
                            mState.setSelection(stateAdapter.getPosition(getResources().getString(R.string.str_check_Odisha)));

                    } else if (country.matches(getResources().getString(R.string.str_check_UnitedStates))) {
                        try {
                            String mStatesLanguage = "states_us_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStatesLanguage, "array", getApplicationContext().getPackageName());
                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                        state, R.layout.custom_spinner);
                            }
                            mState.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }
                        if (patientID_edit != null) {
                            mState.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_hi_en_te_State_edit(patient1.getState_province(),sessionManager.getAppLanguage()))));

//                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        } else {
                            mState.setSelection(0);
                        }
                    } else if (country.matches(getResources().getString(R.string.str_check_Philippines))) {
                        try {
                            String mStatesLanguage = "states_philippines_" + sessionManager.getAppLanguage();
                            int state = res.getIdentifier(mStatesLanguage, "array", getApplicationContext().getPackageName());
                            if (state != 0) {
                                stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                        state, R.layout.custom_spinner);
                            }
                            mState.setAdapter(stateAdapter);
                        } catch (Exception e) {

                            Logger.logE("Identification", "#648", e);
                        }

                        if (patientID_edit != null) {
                            mState.setSelection(stateAdapter.getPosition(StringUtils.getValue(StringUtils.mSwitch_hi_en_te_State_edit(patient1.getState_province(),sessionManager.getAppLanguage()))));
                        } else {
                            mState.setSelection(stateAdapter.getPosition(getResources().getString(R.string.str_check_Bukidnon)));
                        }

                    } else {
                        stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.state_error, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);

                    }*/

                    if (country.matches("India")) {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.states_india, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);
                        // setting state according database when user clicks edit details

                        if (patientID_edit != null)
                            mState.setSelection(stateAdapter.getPosition(String.valueOf(patient1.getState_province())));
                        else

                            //for default state select state
                            mState.setSelection(0);
//                            mState.setSelection(stateAdapter.getPosition(state));


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

                    } else {
                        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                                R.array.state_error, R.layout.custom_spinner);
                        // stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mState.setAdapter(stateAdapter);
                    }
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
                if (state.matches(getResources().getString(R.string.str_check_Odisha))) {
                    //Creating the instance of ArrayAdapter containing list of fruit names
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.odisha_villages, R.layout.custom_spinner);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else if (state.matches(getResources().getString(R.string.str_check_Bukidnon))) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
                            R.array.bukidnon_villages, R.layout.custom_spinner);
                    mCity.setThreshold(1);//will start working from first character
                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } else {
                    mCity.setAdapter(null);
                }


//                if (state.matches("Odisha")) {
//                    //Creating the instance of ArrayAdapter containing list of fruit names
//                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                            R.array.odisha_villages, R.layout.custom_spinner);
//                    mCity.setThreshold(1);//will start working from first character
//                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
//                } else if (state.matches("Bukidnon")) {
//                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(IdentificationActivity.this,
//                            R.array.bukidnon_villages, R.layout.custom_spinner);
//                    mCity.setThreshold(1);//will start working from first character
//                    mCity.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
//                } else {
//                    mCity.setAdapter(null);
//                }
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
        // Locale.setDefault(Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -10);
        mDOBPicker = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new DatePickerDialog.OnDateSetListener() {
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
            String yrMoDays = DateAndTimeUtils.getAgeInYearMonth(patient1.getDate_of_birth(), context);

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

    /**
     * During edit update ui with the existing data
     *
     * @param patient
     */
    private void updateUI(Patient patient) {

        //AlternateNo
        if (patient.getAlternateNo() != null) {
            mAlternateNumberString = patient.getAlternateNo();
            mAlternateNumberEditText.setText(mAlternateNumberString);
        }
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
        Log.v(TAG, "getPrimaryDoctor" + patient.getPrimaryDoctor());
        Log.v(TAG, "getPrimaryDoctor" + patient.getPrimaryDoctor());
        if (patient.getPrimaryDoctor() != null) {
            mPrimaryDoctorUUIDString = patient.getPrimaryDoctor().split("@#@")[0];
            mPrimaryDoctorTextView.setText(patient.getPrimaryDoctor().split("@#@")[1]);
        }

        //secondaryDoctor
        if (patient.getPrimaryDoctor() != null) {
            mSecondaryDoctorUUIDString = patient.getSecondaryDoctor().split("@#@")[0];
            mSecondaryDoctorTextView.setText(patient.getSecondaryDoctor().split("@#@")[1]);
        }
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

        mTotalBirthCount = mTotalBirthEditText.getText().toString().trim();
        mTotalMiscarriageCount = mTotalMiscarriageEditText.getText().toString().trim();
        mAlternateNumberString = mAlternateNumberEditText.getText().toString().trim();
        mWifeDaughterOfString = mWifeDaughterOfEditText.getText().toString().trim();

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
                && !mCity.getText().toString().equals("") && !countryText.getText().toString().equals("") &&
                !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked())) {

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

            if (mCity.getText().toString().equals("")) {
                mCity.setError(getString(R.string.error_field_required));
            }

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
        /*new*/

        if (mWifeDaughterOfString.isEmpty()) {
            Toast.makeText(this, "Please enter wife/daughter of name", Toast.LENGTH_SHORT).show();
            mWifeDaughterOfEditText.requestFocus();
            return;
        }

        if (mAdmissionDateString.isEmpty()) {
            Toast.makeText(this, "Please select admission date", Toast.LENGTH_SHORT).show();

            return;
        }
        if (mAdmissionTimeString.isEmpty()) {
            Toast.makeText(this, "Please select admission time", Toast.LENGTH_SHORT).show();

            return;
        }

        if (mTotalBirthCount.isEmpty()) {
            Toast.makeText(this, getString(R.string.total_birth_count_val_txt), Toast.LENGTH_SHORT).show();
            mTotalBirthEditText.requestFocus();
            return;
        }
        if (mTotalMiscarriageCount.isEmpty()) {
            Toast.makeText(this, getString(R.string.total_miscarriage_count_val_txt), Toast.LENGTH_SHORT).show();
            mTotalMiscarriageEditText.requestFocus();
            return;
        }
        if (mLaborOnsetString.isEmpty()) {
            Toast.makeText(this, getString(R.string.labor_onset_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mActiveLaborDiagnosedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.active_labor_diagnosed_date_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mActiveLaborDiagnosedTime.isEmpty()) {
            Toast.makeText(this, getString(R.string.active_labor_diagnosed_time_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mUnknownMembraneRupturedCheckBox.isChecked() && mMembraneRupturedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.membrane_ruptured_date_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mUnknownMembraneRupturedCheckBox.isChecked() && mMembraneRupturedTime.isEmpty()) {
            Toast.makeText(this, getString(R.string.membrane_ruptured_time_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mRiskFactorsString.isEmpty()) {
            Toast.makeText(this, getString(R.string.risk_factors_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mHospitalMaternityString.isEmpty()) {
            Toast.makeText(this, getString(R.string.hospital_matermnity_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPrimaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(this, getString(R.string.primary_doct_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mSecondaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(this, getString(R.string.seconday_doct_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        /*end*/
        if (cancel) {
            focusView.requestFocus();
        } else {

            patientdto.setFirstname(StringUtils.getValue(mFirstName.getText().toString()));
            patientdto.setMiddlename(StringUtils.getValue(mMiddleName.getText().toString()));
            patientdto.setLastname(StringUtils.getValue(mLastName.getText().toString()));
            //  patientdto.setPhonenumber(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientdto.setGender(StringUtils.getValue(mGender));

            String[] dob_array = mDOB.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or_bn_en_noEdit
                        (mDOB.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
                dob_array[1] = dob_array[1].replace(dob_array[1], dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];
                patientdto.setDateofbirth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob_value)));
            }

            patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));
            patientdto.setCityvillage(StringUtils.getValue(mCity.getText().toString()));
            patientdto.setPostalcode(StringUtils.getValue(mPostal.getText().toString()));
            patientdto.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
//            patientdto.setCountry(StringUtils.getValue(mSwitch_hi_en_te_Country(mCountry.getSelectedItem().toString(),sessionManager.getAppLanguage())));
//
//            patientdto.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
            patientdto.setPatientPhoto(mCurrentPhotoPath);
//          patientdto.setEconomic(StringUtils.getValue(m));
            patientdto.setStateprovince(StringUtils.getValue(mState.getSelectedItem().toString()));
//            patientdto.setStateprovince(StringUtils.getValue(mSwitch_hi_en_te_State(mState.getSelectedItem().toString(),sessionManager.getAppLanguage())));

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("caste"));
            patientAttributesDTO.setValue(StringUtils.getProvided(mCaste));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Telephone Number"));
            patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNum.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);

            /*new*/
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

            //Ezazi Registration Number
            int number = (int) (Math.random() * (99999999 - 100 + 1) + 100);
            patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Ezazi Registration Number"));
            patientAttributesDTO.setValue(patientdto.getCountry().substring(0, 2) + "/" + patientdto.getStateprovince().substring(0, 2) + "/" + patientdto.getCityvillage().substring(0, 2) + "/" + String.valueOf(number));
            patientAttributesDTOList.add(patientAttributesDTO);

            /*end*/

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("Mother's Name"));
            patientAttributesDTO.setValue(StringUtils.getValue(mRelationship.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            /*patientAttributesDTO = new PatientAttributesDTO();
            patientAttributesDTO.setUuid(UUID.randomUUID().toString());
            patientAttributesDTO.setPatientuuid(uuid);
            patientAttributesDTO.setPersonAttributeTypeUuid(patientsDAO.getUuidForAttribute("occupation"));
            patientAttributesDTO.setValue(StringUtils.getValue(mOccupation.getText().toString()));
            patientAttributesDTOList.add(patientAttributesDTO);*/

            /*patientAttributesDTO = new PatientAttributesDTO();
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
*/
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

        mTotalBirthCount = mTotalBirthEditText.getText().toString().trim();
        mTotalMiscarriageCount = mTotalMiscarriageEditText.getText().toString().trim();
        mAlternateNumberString = mAlternateNumberEditText.getText().toString().trim();
        mWifeDaughterOfString = mWifeDaughterOfEditText.getText().toString().trim();

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

        if (!mFirstName.getText().toString().equals("") && !mLastName.getText().toString().equals("")
                && !mCity.getText().toString().equals("") && !countryText.getText().toString().equals("") &&
                !stateText.getText().toString().equals("") && !mDOB.getText().toString().equals("") && !mAge.getText().toString().equals("") && (mGenderF.isChecked() || mGenderM.isChecked())) {

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

            if (mCity.getText().toString().equals("")) {
                mCity.setError(getString(R.string.error_field_required));
            }

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

        /*new*/
        if (mWifeDaughterOfString.isEmpty()) {
            Toast.makeText(this, "Please enter wife/daughter of name", Toast.LENGTH_SHORT).show();
            mWifeDaughterOfEditText.requestFocus();
            return;
        }

        if (mAdmissionDateString.isEmpty()) {
            Toast.makeText(this, "Please select admission date", Toast.LENGTH_SHORT).show();

            return;
        }
        if (mAdmissionTimeString.isEmpty()) {
            Toast.makeText(this, "Please select admission time", Toast.LENGTH_SHORT).show();

            return;
        }

        if (mTotalBirthCount.isEmpty()) {
            Toast.makeText(this, getString(R.string.total_birth_count_val_txt), Toast.LENGTH_SHORT).show();
            mTotalBirthEditText.requestFocus();
            return;
        }
        if (mTotalMiscarriageCount.isEmpty()) {
            Toast.makeText(this, getString(R.string.total_miscarriage_count_val_txt), Toast.LENGTH_SHORT).show();
            mTotalMiscarriageEditText.requestFocus();
            return;
        }
        if (mLaborOnsetString.isEmpty()) {
            Toast.makeText(this, getString(R.string.labor_onset_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mActiveLaborDiagnosedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.active_labor_diagnosed_date_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mActiveLaborDiagnosedTime.isEmpty()) {
            Toast.makeText(this, getString(R.string.active_labor_diagnosed_time_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mUnknownMembraneRupturedCheckBox.isChecked() && mMembraneRupturedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.membrane_ruptured_date_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mUnknownMembraneRupturedCheckBox.isChecked() && mMembraneRupturedTime.isEmpty()) {
            Toast.makeText(this, getString(R.string.membrane_ruptured_time_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mRiskFactorsString.isEmpty()) {
            Toast.makeText(this, getString(R.string.risk_factors_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mHospitalMaternityString.isEmpty()) {
            Toast.makeText(this, getString(R.string.hospital_matermnity_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPrimaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(this, getString(R.string.primary_doct_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (mSecondaryDoctorUUIDString.isEmpty()) {
            Toast.makeText(this, getString(R.string.seconday_doct_val_txt), Toast.LENGTH_SHORT).show();
            return;
        }
        /*end*/

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

            String[] dob_array = mDOB.getText().toString().split(" ");
            Log.d("dob_array", "0: " + dob_array[0]);
            Log.d("dob_array", "0: " + dob_array[1]);
            Log.d("dob_array", "0: " + dob_array[2]);

            //get month index and return English value for month.
            if (dob_indexValue == 15) {
                String dob = StringUtils.hi_or_bn_en_noEdit
                        (mDOB.getText().toString(), sessionManager.getAppLanguage());
                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob)));
            } else {
                String dob = StringUtils.hi_or_bn_en_month(dob_indexValue);
                String dob_month_split = dob_array[1];
                dob_array[1] = dob_month_split.replace(dob_month_split, dob);
                String dob_value = dob_array[0] + " " + dob_array[1] + " " + dob_array[2];

                patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth
                        (StringUtils.getValue(dob_value)));
            }

            // patientdto.setDate_of_birth(DateAndTimeUtils.getFormatedDateOfBirth(StringUtils.getValue(mDOB.getText().toString())));
            patientdto.setAddress1(StringUtils.getValue(mAddress1.getText().toString()));
            patientdto.setAddress2(StringUtils.getValue(mAddress2.getText().toString()));
            patientdto.setCity_village(StringUtils.getValue(mCity.getText().toString()));
            patientdto.setPostal_code(StringUtils.getValue(mPostal.getText().toString()));
//            patientdto.setCountry(StringUtils.getValue(mSwitch_hi_en_te_Country(mCountry.getSelectedItem().toString(),sessionManager.getAppLanguage())));
            patientdto.setCountry(StringUtils.getValue(mCountry.getSelectedItem().toString()));
            patientdto.setPatient_photo(mCurrentPhotoPath);
//                patientdto.setEconomic(StringUtils.getValue(m));
            patientdto.setState_province(StringUtils.getValue(patientdto.getState_province()));
//           patientdto.setState_province(StringUtils.getValue(mSwitch_hi_en_te_State(mState.getSelectedItem().toString(),sessionManager.getAppLanguage())));
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
            patientAttributesDTO.setValue(StringUtils.getValue(mPhoneNum.getText().toString()));
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


}

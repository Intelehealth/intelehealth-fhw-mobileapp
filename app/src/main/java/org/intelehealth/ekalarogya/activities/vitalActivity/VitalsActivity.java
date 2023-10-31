package org.intelehealth.ekalarogya.activities.vitalActivity;

import static org.intelehealth.ekalarogya.app.AppConstants.*;
import static org.intelehealth.ekalarogya.utilities.EditTextUtils.decimalPlacesCount;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rosemaryapp.amazingspinner.AmazingSpinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.database.dao.VisitAttributeListDAO;
import org.intelehealth.ekalarogya.database.dao.VisitsDAO;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;
import org.intelehealth.ekalarogya.models.dto.EncounterDTO;
import org.intelehealth.ekalarogya.syncModule.SyncUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.complaintNodeActivity.ComplaintNodeActivity;
import org.intelehealth.ekalarogya.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.database.dao.EncounterDAO;
import org.intelehealth.ekalarogya.database.dao.ObsDAO;
import org.intelehealth.ekalarogya.models.VitalsObject;
import org.intelehealth.ekalarogya.models.dto.ObsDTO;
import org.intelehealth.ekalarogya.utilities.ConfigUtils;
import org.intelehealth.ekalarogya.utilities.FileUtils;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UuidDictionary;

import org.intelehealth.ekalarogya.utilities.exception.DAOException;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class VitalsActivity extends AppCompatActivity {
    private static final String TAG = VitalsActivity.class.getSimpleName();
    SessionManager sessionManager;
    private String patientName = "";
    private String intentTag, intentAdviceFrom;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    int flag_height = 0, flag_weight = 0, patientAge = 0;
    String heightvalue = "0";
    String weightvalue;
    ConfigUtils configUtils = new ConfigUtils(VitalsActivity.this);

    VitalsObject results = new VitalsObject();
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mtempfaren, mSpo2, mBMI, mResp,
            mHemoglobin, mSugarRandom, mSugarFasting, mSugarAfterMeal;

    TextInputLayout bmiTIL;
    Spinner mBlood_Spinner;
    ArrayAdapter<CharSequence> bloodAdapter;
    private long mLastClickTime = 0;
    Spinner mheightSpinner;
    ArrayAdapter<CharSequence> heightAdapter;
    private Context context;
    public static final int BEFORE_DECIMAL_PLACE_MAX_COUNT = 3;
    public static final int AFTER_DECIMAL_PLACE_MAX_ONE_COUNT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = VitalsActivity.this;
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientAge = intent.getIntExtra("age", 0);
            intentTag = intent.getStringExtra("tag");
            intentAdviceFrom = intent.getStringExtra("advicefrom");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            Log.v(TAG, "Patient ID: " + patientUuid);
            Log.v(TAG, "Visit ID: " + visitUuid);
            Log.v(TAG, "Patient Name: " + patientName);
            Log.v(TAG, "Intent Tag: " + intentTag);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitals);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //change done under ticket AEAT - 657
        sessionManager = new SessionManager(this);
        setTitle(getString(R.string.title_activity_vitals));
        setTitle(patientName + ": " + getTitle());
        mHeight = findViewById(R.id.table_height);
        mheightSpinner = findViewById(R.id.heightSpinner);
        mWeight = findViewById(R.id.table_weight);
        mPulse = findViewById(R.id.table_pulse);
        mBpSys = findViewById(R.id.table_bpsys);
        mBpDia = findViewById(R.id.table_bpdia);
        mTemperature = findViewById(R.id.table_temp);
        mSpo2 = findViewById(R.id.table_spo2);
        mBMI = findViewById(R.id.table_bmi);
        bmiTIL = findViewById(R.id.textInputLayout_bmi);
        if (patientAge <= 2)
            bmiTIL.setVisibility(View.GONE);
        else
            bmiTIL.setVisibility(View.VISIBLE);
        mResp = findViewById(R.id.table_respiratory);
        mBMI.setEnabled(true);
        mBMI.setClickable(false);
        String heightStr = "height_" + sessionManager.getAppLanguage();
        int heightArray = getResources().getIdentifier(heightStr, "array", getApplicationContext().getPackageName());
        if (heightArray != 0) {
            heightAdapter = ArrayAdapter.createFromResource(this, heightArray, android.R.layout.simple_spinner_dropdown_item);
        }
        mheightSpinner.setAdapter(heightAdapter);
        mHemoglobin = findViewById(R.id.table_hemoglobin);
        mSugarRandom = findViewById(R.id.table_sugar_level);
        mSugarFasting = findViewById(R.id.table_sugar_fasting);
        mSugarAfterMeal = findViewById(R.id.table_sugar_aftermeal);
        mBlood_Spinner = findViewById(R.id.spinner_blood_grp);
        String bloodStr = "blood_group_" + sessionManager.getAppLanguage();
        int bloodGrpArray = getResources().getIdentifier(bloodStr, "array", getApplicationContext().getPackageName());
        bloodAdapter = ArrayAdapter.createFromResource(this, bloodGrpArray/*R.array.blood_group*/, R.layout.blood_group_spinner);
        mBlood_Spinner.setAdapter(bloodAdapter);
        mBlood_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    TextView textView = ((TextView) parent.getChildAt(0));
                    if (textView != null)
                        textView.setTextColor(getResources().getColor(R.color.medium_gray));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
//            #633 #632
            if (!sessionManager.getLicenseKey().isEmpty()) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                                String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }//Load the config file
            //Display the fields on the Vitals screen as per the config file
            if (obj.getBoolean("mHeight")) {
                //  mHeight.setVisibility(View.VISIBLE);
                mheightSpinner.setVisibility(View.VISIBLE);
            } else {
                //  mHeight.setVisibility(View.GONE);
                mheightSpinner.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mWeight")) {
                mWeight.setVisibility(View.VISIBLE);
            } else {
                mWeight.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPulse")) {
                mPulse.setVisibility(View.VISIBLE);
            } else {
                mPulse.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mBpSys")) {
                mBpSys.setVisibility(View.VISIBLE);
            } else {
                mBpSys.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mBpDia")) {
                mBpDia.setVisibility(View.VISIBLE);
            } else {
                mBpDia.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mTemperature")) {
                if (obj.getBoolean("mCelsius")) {

                    mTemperature = findViewById(R.id.table_temp);
                    findViewById(R.id.table_temp_faren).setVisibility(View.GONE);

                } else if (obj.getBoolean("mFahrenheit")) {

                    mTemperature = findViewById(R.id.table_temp_faren);
                    findViewById(R.id.table_temp).setVisibility(View.GONE);
                }
            } else {
                mTemperature.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mSpo2")) {
                mSpo2.setVisibility(View.VISIBLE);
            } else {
                mSpo2.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mBMI")) {
                mBMI.setVisibility(View.VISIBLE);
            } else {
                mBMI.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mResp")) {
                mResp.setVisibility(View.VISIBLE);
            } else {
                mResp.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "config file error", Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (intentTag != null && intentTag.equals("edit")) {
            loadPrevious();
        }

        mHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    mBMI.getText().clear();
                    flag_height = 1;
                    heightvalue = mHeight.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_HEIGHT)) {
                        mHeight.setError(getString(R.string.height_error, AppConstants.MAXIMUM_HEIGHT));
                    } else {
                        mHeight.setError(null);
                    }

                } else {
                    flag_height = 0;
                    mBMI.getText().clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateBMI();
                if (mHeight.getText().toString().startsWith(".")) {
                    mHeight.setText("");
                } else {

                }
            }
        });

        mheightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position != 0) {
                    flag_height = 1;
                    ConvertHeightIntoCm(heightAdapter.getItem(position).toString());
                    calculateBMI();
                } else {
                    flag_height = 0;
                    heightvalue = "0";
                    mBMI.setText("");
                    calculateBMI();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


//        mheightSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//              /*  mheightSpinner.setError(null);
//                mheightSpinner.setHint("");*/
//                if (position != 0) {
//                    flag_height = 1;
//                    ConvertHeightIntoCm(heightAdapter.getItem(position).toString());
//                    calculateBMI();
//                }
//                else {
//                    flag_height = 0;
//                }
//            }
//        });

        mWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    mBMI.getText().clear();
                    flag_weight = 1;
                    weightvalue = mWeight.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(MAXIMUM_WEIGHT) ||
                            Double.valueOf(s.toString()) < Double.valueOf(MINIMUM_WEIGHT)) {
                        mWeight.setError(getString(R.string.weight_error, MINIMUM_WEIGHT, MAXIMUM_WEIGHT));
                    } else {
                        mWeight.setError(null);
                    }
                } else {
                    flag_weight = 0;
                    mBMI.getText().clear();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mWeight.getText().toString().startsWith(".")) {
                    mWeight.setText("");
                } else {
                }

                String str = mWeight.getText().toString();
                if (str.isEmpty()) return;
                String str2 = decimalPlacesCount(str, BEFORE_DECIMAL_PLACE_MAX_COUNT, AFTER_DECIMAL_PLACE_MAX_ONE_COUNT);

                if (!str2.equals(str)) {
                    mWeight.setText(str2);
                    mWeight.setSelection(str2.length());
                }

                calculateBMI();


            }
        });


        mSpo2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.table_spo2 || id == EditorInfo.IME_NULL) {
                    validateTable();
                    return true;
                }
                return false;
            }
        });

        mSpo2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_SPO2) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_SPO2)) {
                        mSpo2.setError(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                    } else {
                        mSpo2.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mSpo2.getText().toString().startsWith(".")) {
                    mSpo2.setText("");
                } else {

                }
            }
        });

        mTemperature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (configUtils.celsius()) {
                    if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                        if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS) ||
                                Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_TEMPERATURE_CELSIUS)) {
                            mTemperature.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                        } else {
                            mTemperature.setError(null);
                        }

                    }
                } else if (configUtils.fahrenheit()) {
                    if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                        if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT) ||
                                Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_TEMPERATURE_FARHENIT)) {
                            mTemperature.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                        } else {
                            mTemperature.setError(null);
                        }
                    }

                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mTemperature.getText().toString().startsWith(".")) {
                    mTemperature.setText("");
                } else {

                }

                String str = mTemperature.getText().toString();
                if (str.isEmpty()) return;
                String str2 = decimalPlacesCount(str, BEFORE_DECIMAL_PLACE_MAX_COUNT, AFTER_DECIMAL_PLACE_MAX_ONE_COUNT);

                if (!str2.equals(str)) {
                    mTemperature.setText(str2);
                    mTemperature.setSelection(str2.length());
                }

            }
        });

        mResp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_RESPIRATORY) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_RESPIRATORY)) {
                        mResp.setError(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                    } else {
                        mResp.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mResp.getText().toString().startsWith(".")) {
                    mResp.setText("");
                } else {

                }
            }
        });


        mPulse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_PULSE) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_PULSE)) {
                        mPulse.setError(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                    } else {
                        mPulse.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mPulse.getText().toString().startsWith(".")) {
                    mPulse.setText("");
                } else {

                }
            }
        });

        mBpSys.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    String diaValue = "";
                    if (mBpDia != null)
                        diaValue = mBpDia.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_BP_SYS) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_BP_SYS)) {
                        mBpSys.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                    } else if (!diaValue.trim().isEmpty() && Double.valueOf(s.toString()) <= Double.valueOf(diaValue)) {
                        mBpSys.setError(getString(R.string.bpsys_not_less_error));
                    } else {
                        mBpSys.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mBpSys.getText().toString().startsWith(".")) {
                    mBpSys.setText("");
                    return;
                }

                if (mBpSys.getText().toString().isEmpty()) {
                    mBpDia.setText("");
                    return;
                }

                // SYS - COLOR CODE - START
                bpSysColorCode(mBpSys.getText().toString().trim());
                // SYS - COLOR CODE - END
            }
        });

        mBpDia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    String sysValue = "";
                    if (mBpSys != null)
                        sysValue = mBpSys.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_BP_DSYS) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_BP_DSYS)) {
                        mBpDia.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                    } else if (!sysValue.trim().isEmpty() && Double.valueOf(s.toString()) >= Double.valueOf(sysValue)) {
                        mBpDia.setError(getString(R.string.bpdia_not_more_error));
                    } else {
                        mBpDia.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mBpDia.getText().toString().startsWith(".")) {
                    mBpDia.setText("");
                    return;
                }

                bpDiaColorCode(mBpDia.getText().toString().trim());
            }
        });

//        mHemoglobin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String[] data = new String[]{/*"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5",*/ "5.0",
//                        "5.5", "6.0", "6.5", "7.0", "7.5", "8.0", "8.5", "9.0", "9.5", "10.0", "10.5", "11.0", "11.5", "12.0",
//                        "12.5", "13.0", "13.5", "14.0", "14.5", "15.0", "15.5", "16.0", "16.5", "17.0"/*, "17.5", "18.0",
//                        "18.5", "19.0", "19.5", "20.0"*/};
//
//                setVitalInfoForHemoAndSugar(data, mHemoglobin, mHemoglobin.getText().toString().trim());
//            }
//        });

        mHemoglobin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_HEMOGLOBIN) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_HEMOGLOBIN)) {
                        mHemoglobin.setError(getString(R.string.hemoglobin_error, AppConstants.MINIMUM_HEMOGLOBIN, AppConstants.MAXIMUM_HEMOGLOBIN));
                    } else {
                        mHemoglobin.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mHemoglobin.getText().toString().startsWith(".")) {
                    mHemoglobin.setText("");
                } else {

                }

                String str = mHemoglobin.getText().toString();
                if (str.isEmpty()) return;
                String str2 = decimalPlacesCount(str, BEFORE_DECIMAL_PLACE_MAX_COUNT, AFTER_DECIMAL_PLACE_MAX_ONE_COUNT);

                if (!str2.equals(str)) {
                    mHemoglobin.setText(str2);
                    mHemoglobin.setSelection(str2.length());
                }

            }
        });

        mSugarRandom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_SUGAR) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_SUGAR)) {
                        mSugarRandom.setError(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                    } else {
                        mSugarRandom.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mSugarRandom.getText().toString().startsWith(".")) {
                    mSugarRandom.setText("");
                } else {

                }
                //   decimalPlacesCount(mSugarRandom, s, AFTER_DECIMAL_PLACE_MAX_ONE_COUNT);

                String str = mSugarRandom.getText().toString();
                if (str.isEmpty()) return;
                String str2 = decimalPlacesCount(str, BEFORE_DECIMAL_PLACE_MAX_COUNT, AFTER_DECIMAL_PLACE_MAX_ONE_COUNT);

                if (!str2.equals(str)) {
                    mSugarRandom.setText(str2);
                    mSugarRandom.setSelection(str2.length());
                }

            }
        });

        mSugarFasting.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_SUGAR) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_SUGAR)) {
                        mSugarFasting.setError(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                    } else {
                        mSugarFasting.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mSugarFasting.getText().toString().startsWith(".")) {
                    mSugarFasting.setText("");
                } else {

                }

                String str = mSugarFasting.getText().toString();
                if (str.isEmpty()) return;
                String str2 = decimalPlacesCount(str, BEFORE_DECIMAL_PLACE_MAX_COUNT, AFTER_DECIMAL_PLACE_MAX_ONE_COUNT);

                if (!str2.equals(str)) {
                    mSugarFasting.setText(str2);
                    mSugarFasting.setSelection(str2.length());
                }
            }
        });

        //commenting this validation as this field is no longer required.
        /* mSugarAfterMeal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_SUGAR) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_SUGAR)) {
                        mSugarAfterMeal.setError(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                    } else {
                        mSugarAfterMeal.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mSugarAfterMeal.getText().toString().startsWith(".")) {
                    mSugarAfterMeal.setText("");
                } else {

                }

                String str = mSugarAfterMeal.getText().toString();
                if (str.isEmpty()) return;
                String str2 = decimalPlacesCount(str, BEFORE_DECIMAL_PLACE_MAX_COUNT, AFTER_DECIMAL_PLACE_MAX_ONE_COUNT);

                if (!str2.equals(str)) {
                    mSugarAfterMeal.setText(str2);
                    mSugarAfterMeal.setSelection(str2.length());
                }
            }
        });*/

        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTable();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if(intentTag!=null && intentTag.equalsIgnoreCase("new")) {
                showConfirmationDialog();
            }
            else
                finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationDialog() {
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        final boolean[] isVisitVoid = {false};
        final boolean[] isEncounterVoid = {false};
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
        alertDialog.setTitle(getResources().getString(R.string.generic_warning));
        alertDialog.setMessage(getResources().getString(R.string.exit_vitals_warning_dialog));
        alertDialog.setPositiveButton(context.getResources().getString(R.string.vital_alert_continue_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            isVisitVoid[0] = visitsDAO.voidVisit(visitUuid);
                            isEncounterVoid[0] = encounterDAO.voidEncounter(encounterVitals);
                        } catch (DAOException e) {
                            dialog.dismiss();
                            throw new RuntimeException(e);
                        }
                        dialog.dismiss();
                        if(isVisitVoid[0] == true && isEncounterVoid[0] == true)
                        {
                            onBackPressed();
                        }

                    }
                });
        alertDialog.setNegativeButton(context.getResources().getString(R.string.survey_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    private void bmiColorCode(String finalBmiValue) {
        if (!finalBmiValue.isEmpty() && mBMI != null) {
            Double bmi = Double.valueOf(finalBmiValue);
            if (bmi < Double.valueOf(BMI_ORANGE_MAX)) {   // red
                mBMI.setText(finalBmiValue + " (" + getResources().getString(R.string.underweight) + ")");
                mBMI.setTextColor(getResources().getColor(R.color.orange));
            } else if (bmi < Double.valueOf(BMI_YELLOW_MAX) && bmi >= Double.valueOf(BMI_YELLOW_MIN)) {   // red
                mBMI.setText(finalBmiValue + " (" + getResources().getString(R.string.overweight) + ")");
                mBMI.setTextColor(getResources().getColor(R.color.dark_yellow));
            } else if (bmi >= Double.valueOf(BMI_LIGHT_RED_MIN) && (bmi < Double.valueOf(BMI_LIGHT_RED_MAX))) {
                mBMI.setText(finalBmiValue + " (" + getResources().getString(R.string.moderate_obesity) + ")");
                mBMI.setTextColor(getResources().getColor(R.color.lite_red));
            } else if (bmi >= Double.valueOf(BMI_GREEN_MIN) && (bmi < Double.valueOf(BMI_GREEN_MAX))) {
                mBMI.setText(finalBmiValue + " (" + getResources().getString(R.string.normal) + ")");
                mBMI.setTextColor(getResources().getColor(R.color.green));
            } else if (bmi >= Double.valueOf(BMI_DARK_RED_MIN)) {   // red
                mBMI.setText(finalBmiValue + " (" + getResources().getString(R.string.severe_obesity) + ")");
                mBMI.setTextColor(getResources().getColor(R.color.scale_1));
            }
            else mBMI.setTextColor(null);
        }
    }


    private void bpSysColorCode(String bpSysValue) {
        if (bpSysValue != null && !bpSysValue.isEmpty()) {
            Double bpSys = Double.valueOf(bpSysValue);
            if (bpSys < Double.valueOf(MINIMUM_BP_SYS) || bpSys > Double.valueOf(MAXIMUM_BP_SYS)) {   // red
                mBpSys.setTextColor(getResources().getColor(R.color.font_black_0));
            } else if (bpSys < Double.valueOf(SYS_RED_MIN) || bpSys >= Double.valueOf(SYS_RED_MAX)) {   // red
                mBpSys.setTextColor(getResources().getColor(R.color.scale_1));
            } else if (bpSys >= Double.valueOf(SYS_YELLOW_MIN) && (bpSys <= Double.valueOf(SYS_YELLOW_MAX))){
                    mBpSys.setTextColor(getResources().getColor(R.color.dark_yellow));
            } else if (bpSys >= Double.valueOf(SYS_GREEN_MIN) && (bpSys < Double.valueOf(SYS_GREEN_MAX))){
                    mBpSys.setTextColor(getResources().getColor(R.color.green));
            } else
                mBpSys.setTextColor(getResources().getColor(R.color.font_black_0));

        }
    }

    private void bpDiaColorCode(String bpDiaValue) {
        if (bpDiaValue != null && !bpDiaValue.isEmpty()) {
            Double bpDia = Double.valueOf(bpDiaValue);
            if(bpDia < Double.valueOf(MINIMUM_BP_DSYS) || bpDia > Double.valueOf(MAXIMUM_BP_DSYS))
                mBpDia.setTextColor(getResources().getColor(R.color.font_black_0));
            else if (bpDia > Double.valueOf(DIA_RED_MAX))  {  // red
                mBpDia.setTextColor(getResources().getColor(R.color.scale_1));
            } else if (bpDia >= Double.valueOf(DIA_YELLOW_MIN) && (bpDia < Double.valueOf(DIA_YELLOW_MAX))){
                    mBpDia.setTextColor(getResources().getColor(R.color.dark_yellow));
            } else if (bpDia < Double.valueOf(DIA_GREEN_MIN)) {   // green
                mBpDia.setTextColor(getResources().getColor(R.color.green));
            } else
                mBpDia.setTextColor(getResources().getColor(R.color.font_black_0));
        }
    }

    public void calculateBMI() {
        if (mWeight != null && heightvalue != null && !heightvalue.equalsIgnoreCase("0")) {
            if (flag_height == 1 && flag_weight == 1 ||
                    /*(mHeight.getText().toString().trim().length() > 0 && !mHeight.getText().toString().startsWith(".")*/
                    (heightvalue.trim().length() > 0 && (mWeight.getText().toString().trim().length() > 0 &&
                            !mWeight.getText().toString().startsWith(".")))) {
                mBMI.getText().clear();
                double numerator = Double.parseDouble(mWeight.getText().toString()) * 10000;
                //  double denominator = (Double.parseDouble(mHeight.getText().toString())) * (Double.parseDouble(mHeight.getText().toString()));
                double denominator = (Double.parseDouble(heightvalue)) * (Double.parseDouble(heightvalue));
                double bmi_value = numerator / denominator;
                //DecimalFormat df = new DecimalFormat("0.00");
                //mBMI.setText(df.format(bmi_value));
                mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
                if (patientAge >= 19)
                    bmiColorCode(String.format(Locale.ENGLISH, "%.2f", bmi_value));
                Log.d("BMI", "BMI: " + mBMI.getText().toString());
                //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
            } else if (flag_height == 0 || flag_weight == 0) {
                // do nothing
                mBMI.getText().clear();
            } else {
                mBMI.getText().clear();
            }
        }
    }

    public void calculateBMI_onEdit(String height, String weight) {
        if (height.toString().trim().length() > 0 && !height.toString().startsWith(".") &&
                weight.toString().trim().length() > 0 && !weight.toString().startsWith(".")) {
            mBMI.getText().clear();
            double numerator = Double.parseDouble(weight) * 10000;
            double denominator = (Double.parseDouble(height)) * (Double.parseDouble(height));
            double bmi_value = numerator / denominator;
            mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
            if (patientAge >= 19)
                bmiColorCode(String.format(Locale.ENGLISH, "%.2f", bmi_value));
            Log.d("BMI", "BMI: " + mBMI.getText().toString());
        } else {
            // do nothing
            mBMI.getText().clear();
        }
    }


    public void loadPrevious() {

        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1'";
        String[] visitArgs = {encounterVitals};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.HEIGHT: //Height
                //  mHeight.setText(value);
                if (!value.equalsIgnoreCase("0")) {
                    heightvalue = value;
                    String height = ConvertHeightIntoFeets(value);
                    int pos = heightAdapter.getPosition(height);
                    mheightSpinner.setSelection(pos);
                }
                break;
            case UuidDictionary.WEIGHT: //Weight
                mWeight.setText(value);
                break;
            case UuidDictionary.PULSE: //Pulse
                mPulse.setText(value);
                break;
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
                bpSysColorCode(value);
                mBpSys.setText(value);
                break;
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
                bpDiaColorCode(value);
                mBpDia.setText(value);
                break;
            case UuidDictionary.TEMPERATURE: //Temperature
                if (findViewById(R.id.table_temp).getVisibility() == View.GONE) {
                    //Converting Celsius to Fahrenheit
                    if (value != null && !value.isEmpty()) {
                        mTemperature.setText(convertCtoF(value));
                    }
                } else {
                    mTemperature.setText(value);
                }

                break;
            //    Respiratory added by mahiti dev team
            case UuidDictionary.RESPIRATORY: //Respiratory
                mResp.setText(value);
                break;
            case UuidDictionary.SPO2: //SpO2
                mSpo2.setText(value);
                break;

            case UuidDictionary.HEMOGLOBIN: //Hgb
                mHemoglobin.setText(value);
                break;

            case UuidDictionary.SUGARLEVELRANDOM: //sugar random
                mSugarRandom.setText(value);
                break;

            case UuidDictionary.SUGARLEVELFASTING: //sugar fasting
                mSugarFasting.setText(value);
                break;

            case UuidDictionary.SUGARLEVELAFTERMEAL: //sugar after meal
                mSugarAfterMeal.setText(value);
                break;

            case UuidDictionary.BLOODGROUP: //blood
                if (value == null || value.isEmpty() || value.length() == 0) {
                    mBlood_Spinner.setSelection(0);
                } else {
                    String[] blood_Array = getResources().getStringArray(R.array.blood_group_en);
                    int pos = Arrays.asList(blood_Array).indexOf(value);
                    mBlood_Spinner.setSelection(pos);
                    //mBlood_Spinner.setSelection(bloodAdapter.getPosition(value));
                }
                break;
            default:
                break;

        }
        //on edit on vs screen, the bmi will be set in vitals bmi edit field.
        if (mBMI.getText().toString().equalsIgnoreCase("")) {
            calculateBMI_onEdit(mHeight.getText().toString(), mWeight.getText().toString());
        }
    }

    public void validateTable() {
        boolean cancel = false;
        View focusView = null;

        boolean val = mBMI.getText().toString().trim().isEmpty();
        val = mBpSys.getText().toString().trim().isEmpty();
        val = mBpDia.getText().toString().trim().isEmpty();
        val = mSpo2.getText().toString().trim().isEmpty();
        val = mPulse.getText().toString().trim().isEmpty();
        val = mResp.getText().toString().trim().isEmpty();
        val = mTemperature.getText().toString().trim().isEmpty();
        val = mBlood_Spinner.getSelectedItemPosition() != 0;
        val = mHemoglobin.getText().toString().trim().isEmpty();
        val = mSugarRandom.getText().toString().trim().isEmpty();
        val = mSugarFasting.getText().toString().trim().isEmpty();
        val = mSugarAfterMeal.getText().toString().trim().isEmpty();

        if (mBMI.getText().toString().trim().isEmpty() && mBpSys.getText().toString().trim().isEmpty() &&
                mBpDia.getText().toString().isEmpty() && mSpo2.getText().toString().trim().isEmpty() &&
                mPulse.getText().toString().trim().isEmpty() && mResp.getText().toString().trim().isEmpty() &&
                mTemperature.getText().toString().trim().isEmpty() && mBlood_Spinner.getSelectedItemPosition() == 0 &&
                mHemoglobin.getText().toString().trim().isEmpty() && mSugarRandom.getText().toString().trim().isEmpty() &&
                mSugarFasting.getText().toString().trim().isEmpty() && mSugarAfterMeal.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.vital_alert_required_field_button), Toast.LENGTH_LONG).show();
            return;
        }

        //removing validation as asked under ticket AEAT-729
        /*if (mheightSpinner.getSelectedItemPosition() == 0) {
            TextView t = (TextView) mheightSpinner.getSelectedView();
            t.setError(getString(R.string.select));
            t.setTextColor(Color.RED);
            focusView = mheightSpinner;
            cancel = true;
            return;
        }*/

        //BP vaidations added by Prajwal.
        if (mBpSys.getText().toString().isEmpty() && !mBpDia.getText().toString().isEmpty() ||
                !mBpSys.getText().toString().isEmpty() && mBpDia.getText().toString().isEmpty()) {
            if (mBpSys.getText().toString().isEmpty()) {
                mBpSys.requestFocus();
                mBpSys.setError(getString(R.string.enter_field));
                return;
            } else if (mBpDia.getText().toString().isEmpty()) {
                mBpDia.requestFocus();
                mBpDia.setError(getString(R.string.enter_field));
                return;
            }
        }

        /*if (mSugarFasting.getText().toString().isEmpty()) {
            mSugarFasting.requestFocus();
            mSugarFasting.setError(getString(R.string.enter_field));
            return;
        }*/

        // Store values at the time of the fab is clicked.
        ArrayList<EditText> values = new ArrayList<EditText>();
        //  values.add(mHeight);
        values.add(mWeight);
        values.add(mPulse);
        values.add(mBpSys);
        values.add(mBpDia);
        values.add(mTemperature);
        values.add(mResp);
        values.add(mSpo2);
        values.add(mHemoglobin);
        values.add(mSugarRandom);
        values.add(mSugarFasting);
        values.add(mSugarAfterMeal);

        // Check to see if values were inputted.
        //   if (!intentAdviceFrom.equalsIgnoreCase("Sevika")) {

        // Validations - START
        // 1. weight
        String w_value = mWeight.getText().toString().trim();
        if (w_value != null && !w_value.isEmpty()) {
            if (Double.valueOf(w_value.toString()) > Double.valueOf(MAXIMUM_WEIGHT) ||
                    Double.valueOf(w_value.toString()) < Double.valueOf(MINIMUM_WEIGHT)) {
                mWeight.setError(getString(R.string.weight_error, MINIMUM_WEIGHT, MAXIMUM_WEIGHT));
                return;
            }
        }
        // end

        // *. BP - Systolic
        String bp_sys_value = mBpSys.getText().toString().trim();
        if (bp_sys_value != null && !bp_sys_value.isEmpty()) {
            String diaValue = "";
            if (mBpDia != null)
                diaValue = mBpDia.getText().toString();
            if ((Double.parseDouble(bp_sys_value) < Double.parseDouble(AppConstants.MINIMUM_BP_SYS)) ||
                    (Double.parseDouble(bp_sys_value) > Double.parseDouble(AppConstants.MAXIMUM_BP_SYS))) {
                mBpSys.requestFocus();
                mBpSys.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                return;
            } else if (!diaValue.trim().isEmpty() && Double.valueOf(bp_sys_value) <= Double.valueOf(diaValue)) {
                mBpSys.requestFocus();
                mBpSys.setError(getString(R.string.bpsys_not_less_error));
                return;
            }
        }
        // end

// *. BP - Diastolic
        String bp_dia_value = mBpDia.getText().toString().trim();
        if (bp_dia_value != null && !bp_dia_value.isEmpty()) {
            String sysValue = "";
            if (mBpSys != null)
                sysValue = mBpSys.getText().toString();
            if ((Double.parseDouble(bp_dia_value) < Double.parseDouble(AppConstants.MINIMUM_BP_DSYS)) ||
                    (Double.parseDouble(bp_dia_value) > Double.parseDouble(AppConstants.MAXIMUM_BP_DSYS))) {
                mBpDia.requestFocus();
                mBpDia.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                return;
            } else if (!sysValue.trim().isEmpty() && Double.valueOf(bp_dia_value) >= Double.valueOf(sysValue)) {
                mBpDia.requestFocus();
                mBpDia.setError(getString(R.string.bpdia_not_more_error));
                return;
            }
        }
        // end


        // *. spo2
        String spo2_value = mSpo2.getText().toString().trim();
        if (spo2_value != null && !spo2_value.isEmpty()) {
            if ((Double.parseDouble(spo2_value) < Double.parseDouble(AppConstants.MINIMUM_SPO2)) ||
                    (Double.parseDouble(spo2_value) > Double.parseDouble(AppConstants.MAXIMUM_SPO2))) {
                mSpo2.requestFocus();
                mSpo2.setError(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                return;
            }
        }
        // end

        // pulse - start
        String p_value = mPulse.getText().toString().trim();
        if (p_value != null && !p_value.isEmpty() && (!p_value.equals("0.0"))) {
            if ((Double.parseDouble(p_value) > Double.parseDouble(AppConstants.MAXIMUM_PULSE)) ||
                    (Double.parseDouble(p_value) < Double.parseDouble(AppConstants.MINIMUM_PULSE))) {
                mPulse.requestFocus();
                mPulse.setError(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                return;
            }
        }
        // pulse - end

        // Temp F - START
        String temp_value = mTemperature.getText().toString().trim();
        if (temp_value != null && !temp_value.isEmpty() && !temp_value.startsWith(".")) {
            if ((Double.parseDouble(temp_value) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT)) ||
                    (Double.parseDouble(temp_value) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_FARHENIT))) {
                mTemperature.requestFocus();
                mTemperature.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                return;
            }
        }
        // Temp F - END

        // *. BP - Diastolic
        String hb_value = mHemoglobin.getText().toString().trim();
        if (hb_value != null && !hb_value.isEmpty()) {
            if ((Double.parseDouble(hb_value) > Double.parseDouble(AppConstants.MAXIMUM_HEMOGLOBIN)) ||
                    (Double.parseDouble(hb_value) < Double.parseDouble(AppConstants.MINIMUM_HEMOGLOBIN))) {
                mHemoglobin.requestFocus();
                mHemoglobin.setError(getString(R.string.hemoglobin_error, AppConstants.MINIMUM_HEMOGLOBIN, AppConstants.MAXIMUM_HEMOGLOBIN));
                return;
            }
        }
        // end

        // Sugar - start
        // Sugar - Random
        String sugar_random_value = mSugarRandom.getText().toString().trim();
        if (sugar_random_value != null && !sugar_random_value.isEmpty()) {
            if ((Double.parseDouble(sugar_random_value) > Double.parseDouble(AppConstants.MAXIMUM_SUGAR)) ||
                    (Double.parseDouble(sugar_random_value) < Double.parseDouble(AppConstants.MINIMUM_SUGAR))) {
                mSugarRandom.requestFocus();
                mSugarRandom.setError(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                return;
            }
        }

        // Sugar - Fasting
        String sugar_fasting_value = mSugarFasting.getText().toString().trim();
        if (sugar_fasting_value != null && !sugar_fasting_value.isEmpty()) {
            if ((Double.parseDouble(sugar_fasting_value) > Double.parseDouble(AppConstants.MAXIMUM_SUGAR)) ||
                    (Double.parseDouble(sugar_fasting_value) < Double.parseDouble(AppConstants.MINIMUM_SUGAR))) {
                mSugarFasting.requestFocus();
                mSugarFasting.setError(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                return;
            }
        }

        // Sugar - After Meal
        String sugar_afterMeal_value = mSugarAfterMeal.getText().toString().trim();
        if (sugar_afterMeal_value != null && !sugar_afterMeal_value.isEmpty()) {
            if ((Double.parseDouble(sugar_afterMeal_value) > Double.parseDouble(AppConstants.MAXIMUM_SUGAR)) ||
                    (Double.parseDouble(sugar_afterMeal_value) < Double.parseDouble(AppConstants.MINIMUM_SUGAR))) {
                mSugarAfterMeal.requestFocus();
                mSugarAfterMeal.setError(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                return;
            }
        }
        // Sugar - end

        //for respiratory rate
        String respiratoryRate = mResp.getText().toString().trim();
        if (!respiratoryRate.isEmpty()) {
            if ((Double.parseDouble(respiratoryRate) > Double.parseDouble(MAXIMUM_RESPIRATORY)) ||
                    (Double.parseDouble(respiratoryRate) < Double.parseDouble(MINIMUM_RESPIRATORY))) {
                mResp.requestFocus();
                mResp.setError(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                return;
            }
        }

        // AEAT- 646 (Temp, BP, Pulse validaton) - START
        if (mBpSys.getText().toString().trim().isEmpty() || mBpDia.getText().toString().trim().isEmpty() ||
                mPulse.getText().toString().trim().isEmpty() || mTemperature.getText().toString().trim().isEmpty()) {

            String bpSysEmpty = "", bpDiaEmpty = "", pulseEmpty = "", tempEmpty = "";
            String emptyList = "";

            if (mBpSys.getText().toString().trim().isEmpty()) {
                bpSysEmpty = getString(R.string.table_bpsys);
                emptyList = emptyList + bpSysEmpty + "\n";
                mBpSys.requestFocus();
                mBpSys.setError(getString(R.string.enter_field));
            }
            if (mBpDia.getText().toString().trim().isEmpty()) {
                bpDiaEmpty = getString(R.string.table_bpdia);
                emptyList = emptyList + bpDiaEmpty + "\n";
                mBpDia.requestFocus();
                mBpDia.setError(getString(R.string.enter_field));
            }
            if (mPulse.getText().toString().trim().isEmpty()) {
                pulseEmpty = getString(R.string.table_pulse);
                emptyList = emptyList + pulseEmpty + "\n";
                mPulse.requestFocus();
                mPulse.setError(getString(R.string.enter_field));
            }
            if (mTemperature.getText().toString().trim().isEmpty()) {
                tempEmpty = getString(R.string.temperature_f);
                emptyList = emptyList + tempEmpty + "\n";
                mTemperature.requestFocus();
                mTemperature.setError(getString(R.string.enter_field));
            }

            if (mBpSys.getText().toString().trim().isEmpty() && mBpDia.getText().toString().trim().isEmpty() &&
                    mPulse.getText().toString().trim().isEmpty() && mTemperature.getText().toString().trim().isEmpty()) {
                MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
                alertDialog.setTitle("At least one of the following fields is mandatory to fill - ");
                alertDialog.setMessage(emptyList);
                alertDialog.setPositiveButton(context.getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = alertDialog.show();
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
                return;
            }

            Log.d(TAG, "emptyList: " + emptyList);
            emptyList = emptyList + "\n " + getString(R.string.do_you_still_want_to_continue);
            showVitalsPromptDialog(context, getString(R.string.following_fields_are_not_filled), emptyList);
        } else {
            submitVitalsIntoDB();
        }
        // AEAT- 646 - END

    }

    private void submitVitalsIntoDB() {
        try {
            //  if (mHeight.getText() != null && !mHeight.getText().toString().equals("")) {
            if (!heightvalue.equals("")) {
                results.setHeight(heightvalue/*(mHeight.getText().toString())*/);
            } else if (/*mHeight.getText().toString()*/heightvalue.equals("")) {
                results.setHeight("0");
            }
            if (mWeight.getText() != null) {
                results.setWeight((mWeight.getText().toString()));
            }
            if (mPulse.getText() != null) {
                results.setPulse((mPulse.getText().toString()));
            }
            if (mBpDia.getText() != null) {
                results.setBpdia((mBpDia.getText().toString()));
            }
            if (mBpSys.getText() != null) {
                results.setBpsys((mBpSys.getText().toString()));
            }
            if (mTemperature.getText() != null) {

                if (findViewById(R.id.table_temp).getVisibility() == View.GONE) {
                    //Converting Fahrenheit to Celsius
//                        results.setTemperature((mTemperature.getText().toString()));
                    results.setTemperature(ConvertFtoC(mTemperature.getText().toString()));
                } else {
                    results.setTemperature((mTemperature.getText().toString()));
                }

            }
            if (mResp.getText() != null) {
                results.setResp((mResp.getText().toString()));
            }
            if (mSpo2.getText() != null) {
                results.setSpo2((mSpo2.getText().toString()));
            }

            if (mHemoglobin.getText() != null) {
                results.setHsb((mHemoglobin.getText().toString()));
            }

            if (mSugarRandom.getText() != null) {
                results.setSugarrandom(mSugarRandom.getText().toString());
            }

            if (mSugarFasting.getText() != null) {
                results.setSugarfasting(mSugarFasting.getText().toString());
            }

            if (mSugarAfterMeal.getText() != null) {
                results.setSugaraftermeal(mSugarAfterMeal.getText().toString());
            }

            if (mBlood_Spinner.getSelectedItemPosition() != 0) {
                String[] blood_Array = getResources().getStringArray(R.array.blood_group_en);
                results.setBlood((blood_Array[mBlood_Spinner.getSelectedItemPosition()]));
                //results.setBlood((mBlood_Spinner.getSelectedItem().toString()));
            } else {
                results.setBlood("");
            }
        } catch (NumberFormatException e) {
            Snackbar.make(findViewById(R.id.cl_table), R.string.error_non_decimal_no_added, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }


        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        if (intentTag != null && intentTag.equals("edit")) {
            try {
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.HEIGHT);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                if (results.getHeight().equals("")) {
                    obsDTO.setValue("0");
                } else {
                    obsDTO.setValue(results.getHeight());
                }
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.HEIGHT));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.WEIGHT);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getWeight());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.WEIGHT));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.PULSE);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getPulse());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.PULSE));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.SYSTOLIC_BP);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBpsys());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SYSTOLIC_BP));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.DIASTOLIC_BP);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBpdia());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.DIASTOLIC_BP));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getTemperature());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.TEMPERATURE));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.RESPIRATORY);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getResp());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.RESPIRATORY));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.SPO2);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getSpo2());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SPO2));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.HEMOGLOBIN);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getHsb());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.HEMOGLOBIN));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.BLOODGROUP);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBlood());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.BLOODGROUP));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.SUGARLEVELRANDOM);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getSugarrandom());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SUGARLEVELRANDOM));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.SUGARLEVELFASTING);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getSugarfasting());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SUGARLEVELFASTING));

                obsDAO.updateObs(obsDTO);

                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.SUGARLEVELAFTERMEAL);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getSugaraftermeal());
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SUGARLEVELAFTERMEAL));

                obsDAO.updateObs(obsDTO);

                //making flag to false in the encounter table so it will sync again
                EncounterDAO encounterDAO = new EncounterDAO();
                try {
                    encounterDAO.updateEncounterSync("false", encounterVitals);
                    encounterDAO.updateEncounterModifiedDate(encounterVitals);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                Intent intent = new Intent(VitalsActivity.this, VisitSummaryActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("tag", intentTag);
                intent.putExtra("hasPrescription", "false");
                startActivity(intent);
            } catch (DAOException dao) {
                FirebaseCrashlytics.getInstance().recordException(dao);
            }
        } else {

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.HEIGHT);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            if (results.getHeight().equals("")) {
                obsDTO.setValue("0");
            } else {
                obsDTO.setValue(results.getHeight());
            }

            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.WEIGHT);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getWeight());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.PULSE);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getPulse());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.SYSTOLIC_BP);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBpsys());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.DIASTOLIC_BP);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBpdia());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getTemperature());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.RESPIRATORY);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getResp());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.SPO2);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getSpo2());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.HEMOGLOBIN);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getHsb());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.BLOODGROUP);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBlood());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.SUGARLEVELRANDOM);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getSugarrandom());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.SUGARLEVELFASTING);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getSugarfasting());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.SUGARLEVELAFTERMEAL);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getSugaraftermeal());

            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }


            //--------------------Doctor Advice Alert-------------------------
            if (!intentAdviceFrom.equalsIgnoreCase("Sevika")) {
                startDoctorAdvice();
            } else {
                String alertMsg = "";
                String bmiValue = mBMI.getText().toString().trim();
                if(bmiValue.length() == 3)
                    bmiValue = bmiValue.substring(0,3);
                else if(bmiValue.length() == 4)
                    bmiValue = bmiValue.substring(0,4);
                else if (bmiValue.length() >= 5)
                    bmiValue = bmiValue.substring(0,5);
                if (mBMI.getText() != null && mBMI.getText().toString().trim().length() != 0 && Double.parseDouble(bmiValue) < 18.5) {
                    alertMsg = alertMsg + getResources().getString(R.string.weight_loss_alert_msg) + "\n";
                } else if (mBMI.getText() != null && mBMI.getText().toString().trim().length() != 0 && Double.parseDouble(bmiValue) > 25.0) {
                    alertMsg = alertMsg + getResources().getString(R.string.weight_gain_alert_msg) + "\n";
                }

                if (mBpSys.getText() != null && mBpSys.getText().toString().trim().length() != 0 && (Integer.parseInt(mBpSys.getText().toString().trim()) < 60 || Integer.parseInt(mBpSys.getText().toString().trim()) > 180)) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_bp_sys_button) + "\n";
                }

                if (mBpDia.getText() != null && mBpDia.getText().toString().trim().length() != 0 && (Integer.parseInt(mBpDia.getText().toString().trim()) < 50 || Integer.parseInt(mBpDia.getText().toString().trim()) > 120)) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_bp_dia_button) + "\n";
                }

                if (mSpo2.getText() != null && mSpo2.getText().toString().trim().length() != 0 && (Integer.parseInt(mSpo2.getText().toString().trim()) < 95)) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_spo2_button) + "\n";
                }

                if (float_ageYear_Month < 35) {
                    if (mPulse.getText() != null && mPulse.getText().toString().trim().length() != 0 && (Integer.parseInt(mPulse.getText().toString().trim()) < 60 || Integer.parseInt(mPulse.getText().toString().trim()) > 200)) {
                        alertMsg = alertMsg + getResources().getString(R.string.vital_alert_pulse_button) + "\n";
                    }
                } else if (float_ageYear_Month >= 35 && float_ageYear_Month < 50) {
                    if (mPulse.getText() != null && mPulse.getText().toString().trim().length() != 0 && (Integer.parseInt(mPulse.getText().toString().trim()) < 58 || Integer.parseInt(mPulse.getText().toString().trim()) > 150)) {
                        alertMsg = alertMsg + getResources().getString(R.string.vital_alert_pulse_button) + "\n";
                    }
                } else {
                    if (mPulse.getText() != null && mPulse.getText().toString().trim().length() != 0 && (Integer.parseInt(mPulse.getText().toString().trim()) < 40 || Integer.parseInt(mPulse.getText().toString().trim()) > 140)) {
                        alertMsg = alertMsg + getResources().getString(R.string.vital_alert_pulse_button) + "\n";
                    }
                }

                if (mResp.getText() != null && mResp.getText().toString().trim().length() != 0 && (Integer.parseInt(mResp.getText().toString().trim()) < 12 || Integer.parseInt(mResp.getText().toString().trim()) > 25)) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_resp_button) + "\n";
                }

                if (float_ageYear_Month < 1) {
                    if (mTemperature.getText() != null && mTemperature.getText().toString().trim().length() != 0 && (Double.parseDouble(mTemperature.getText().toString().trim()) < 95 || Double.parseDouble(mTemperature.getText().toString().trim()) > 100.4)) {
                        alertMsg = alertMsg + getResources().getString(R.string.vital_alert_temperature_button) + "\n";
                    }
                } else {
                    if (mTemperature.getText() != null && mTemperature.getText().toString().trim().length() != 0 && (Double.parseDouble(mTemperature.getText().toString().trim()) < 95 || Double.parseDouble(mTemperature.getText().toString().trim()) > 103)) {
                        alertMsg = alertMsg + getResources().getString(R.string.vital_alert_temperature_button) + "\n";
                    }
                }

                if (mHemoglobin.getText() != null && mHemoglobin.getText().toString().trim().length() != 0 && (Double.parseDouble(mHemoglobin.getText().toString().trim()) < 7.0 || Double.parseDouble(mHemoglobin.getText().toString().trim()) > 20.0)) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_hgb_button) + "\n";
                }

                if ((mSugarRandom.getText() != null && mSugarRandom.getText().toString().trim().length() != 0 && (Double.parseDouble(mSugarRandom.getText().toString().trim()) < 80 || Double.parseDouble(mSugarRandom.getText().toString().trim()) > 130))) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_sugar_random_button) + "\n";
                }

                if ((mSugarFasting.getText() != null && mSugarFasting.getText().toString().trim().length() != 0 && (Double.parseDouble(mSugarFasting.getText().toString().trim()) < 70 || Double.parseDouble(mSugarFasting.getText().toString().trim()) > 100))) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_sugar_fasting_button) + "\n";
                }

                if ((mSugarAfterMeal.getText() != null && mSugarAfterMeal.getText().toString().trim().length() != 0 && (Integer.parseInt(mSugarAfterMeal.getText().toString().trim()) < 54 || Integer.parseInt(mSugarAfterMeal.getText().toString().trim()) > 140))) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_sugar_aftermeal_button) + "\n";
                }

                if (alertMsg.length() > 0) {
                    alertMsg = alertMsg + "\n" + getResources().getString(R.string.doctor_advice_alert_msg);
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(VitalsActivity.this);
//
                    alertDialogBuilder.setMessage(alertMsg);
                    alertDialogBuilder.setNegativeButton(getResources().getString(R.string.vital_alert_save_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                            try {
                                // avoiding multi-click by checking if click is within 1000ms than avoid it.
                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                speciality_attributes.insertVisitAttributes(visitUuid, AppConstants.DOCTOR_NOT_NEEDED);
                                // speciality_attributes.insertVisitAttributes(visitUuid, " Specialist doctor not needed");
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }

                            //-------End Visit----------
                            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
                            Date todayDate = new Date();
                            String endDate = currentDate.format(todayDate);
                            endVisit(visitUuid, patientUuid, endDate);
                        }
                    });
                    alertDialogBuilder.setPositiveButton(getResources().getString(R.string.vital_alert_continue_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startDoctorAdvice();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.show();
                    //alertDialog.show();
                    IntelehealthApplication.setAlertDialogCustomTheme(VitalsActivity.this, alertDialog);
                } else {
                    //-------End Visit----------
                    VisitAttributeListDAO speciality_attributes = new VisitAttributeListDAO();
                    try {
                        // avoiding multi-click by checking if click is within 1000ms than avoid it.
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        speciality_attributes.insertVisitAttributes(visitUuid, AppConstants.DOCTOR_NOT_NEEDED);
                        // speciality_attributes.insertVisitAttributes(visitUuid, " Specialist doctor not needed");
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }

                    SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
                    Date todayDate = new Date();
                    String endDate = currentDate.format(todayDate);
                    endVisit(visitUuid, patientUuid, endDate);
                }
            }
        }
    }

    public void showVitalsPromptDialog(Context context, String title, String message) {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(context.getResources().getString(R.string.survey_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // upon yes click move forward.
                        submitVitalsIntoDB();
                        dialog.dismiss();
                    }
                });
        alertDialog.setNegativeButton(context.getResources().getString(R.string.survey_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // upon No click do nothing.
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = alertDialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    private void endVisit(String visitUuid, String patientUuid, String endTime) {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, endTime);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        new SyncUtils().syncForeground(""); //Sync function will work in foreground of app and
        sessionManager.removeVisitSummary(patientUuid, visitUuid);
        Intent intent = new Intent(VitalsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startDoctorAdvice() {
        Intent intent = new Intent(VitalsActivity.this, ComplaintNodeActivity.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
        intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
        intent.putExtra("state", state);
        intent.putExtra("name", patientName);
        intent.putExtra("float_ageYear_Month", float_ageYear_Month);
        intent.putExtra("tag", intentTag);
        startActivity(intent);
    }

    private String ConvertFtoC(String temperature) {
        if (temperature != null && temperature.length() > 0) {
            String resultVal;
            NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
            double a = Double.parseDouble(temperature);
            double b = ((a - 32) * 5 / 9);
            resultVal = nf.format(b);
            return resultVal;
        }
        return "";
    }

    private String convertCtoF(String temperature) {
        String resultVal;
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        double a = Double.parseDouble(temperature);
        double b = (a * 9 / 5) + 32;
        nf.format(b);
        double roundOff = Math.round(b * 100.0) / 100.0;
        resultVal = nf.format(roundOff);
        return resultVal;
    }

    @Override
    public void onBackPressed() {
        if(intentTag!=null && intentTag.equalsIgnoreCase("new")) {
            Intent intent = new Intent(VitalsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    public String ConvertHeightIntoFeets(String height) {
        int val = Integer.parseInt(height);
        double centemeters = val / 2.54;
        int inche = (int) centemeters % 12;
        int feet = (int) centemeters / 12;
        String heightVal = feet + getString(R.string.ft) + " " + inche + getString(R.string.in);
        System.out.println("value of height=" + val);
        return heightVal;
    }

    public void ConvertHeightIntoCm(String height) {
        height = height.replaceAll(getString(R.string.ft), "").replaceAll(getString(R.string.in), "");
        String[] heightArr = height.split(" ");
        int feets = Integer.parseInt(heightArr[0]) * 12;
        int inches = Integer.parseInt(heightArr[1]);
        int val = (int) ((feets + inches) * 2.54) + 1;
        heightvalue = val + "";
        System.out.println("value of height=" + val);
    }

}

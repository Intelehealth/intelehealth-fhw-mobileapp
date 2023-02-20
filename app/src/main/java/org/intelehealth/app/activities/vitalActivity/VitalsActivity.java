package org.intelehealth.app.activities.vitalActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.linktop.DeviceType;
import com.linktop.MonitorDataTransmissionManager;
import com.linktop.constant.BluetoothState;
import com.linktop.constant.DeviceInfo;
import com.linktop.infs.OnBatteryListener;
import com.linktop.infs.OnBleConnectListener;
import com.linktop.infs.OnBpResultListener;
import com.linktop.infs.OnBtResultListener;
import com.linktop.infs.OnDeviceInfoListener;
import com.linktop.infs.OnDeviceVersionListener;
import com.linktop.infs.OnSpO2ResultListener;
import com.linktop.whealthService.BleDevManager;
import com.linktop.whealthService.MeasureType;
import com.linktop.whealthService.task.BpTask;
import com.linktop.whealthService.task.BtTask;
import com.linktop.whealthService.task.OxTask;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.IBinder;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ConceptAttributeListDAO;
import org.intelehealth.app.models.rhemos_device.Bp;
import org.intelehealth.app.models.rhemos_device.Bt;
import org.intelehealth.app.models.rhemos_device.SpO2;
import org.intelehealth.app.services.HcService;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.PermissionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.complaintNodeActivity.ComplaintNodeActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.utilities.exception.DAOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class VitalsActivity extends AppCompatActivity implements MonitorDataTransmissionManager.OnServiceBindListener,
        ServiceConnection, OnDeviceVersionListener, OnBleConnectListener, OnBatteryListener, OnDeviceInfoListener,
        OnSpO2ResultListener, OnBpResultListener, OnBtResultListener {
    private static final String TAG = VitalsActivity.class.getSimpleName();
    private static final int REQUEST_OPEN_BT = 0x23;
    public HcService mHcService;
    private OxTask mOxTask;
    private BpTask mBpTask;
    private BtTask mBtTask;
    private SpO2 spO2_model = new SpO2();
    private Bp bp_model = new Bp();
    private Bt bt_model = new Bt();

    private ImageButton spo2_Btn, bp_Btn, tempC_Btn, tempF_Btn;
    private boolean tempc_clicked = false, tempf_clicked = false;

    SessionManager sessionManager;
    private String patientName = "", patientFName = "", patientLName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals, encounterBill = "";
    private float float_ageYear_Month;
    int flag_height = 0, flag_weight = 0;
    String heightvalue;
    String weightvalue;
    ConfigUtils configUtils = new ConfigUtils(VitalsActivity.this);
    String appLanguage;
    VitalsObject results = new VitalsObject();
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mtempfaren, mSpo2, mBMI, mResp, mAbdominalGirth,
            bloodGlucose_editText, bloodGlucose_editText_fasting, bloodGlucoseRandom_editText, bloodGlucosePostPrandial_editText,
            haemoglobin_editText, uricAcid_editText, totalCholestrol_editText;

    ConceptAttributeListDAO conceptAttributeListDAO = new ConceptAttributeListDAO();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientFName = intent.getStringExtra("patientFirstName");
            patientLName = intent.getStringExtra("patientLastName");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        sessionManager = new SessionManager(VitalsActivity.this);
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

//        Setting the title
        setTitle(getString(R.string.title_activity_vitals));
        setTitle(patientName + ": " + getTitle());

        mHeight = findViewById(R.id.table_height);
        mWeight = findViewById(R.id.table_weight);
        mPulse = findViewById(R.id.table_pulse);
        mBpSys = findViewById(R.id.table_bpsys);
        mBpDia = findViewById(R.id.table_bpdia);
        mTemperature = findViewById(R.id.table_temp);
        tempF_Btn = findViewById(R.id.tempf_Btn);
        tempC_Btn = findViewById(R.id.tempc_Btn);

        mSpo2 = findViewById(R.id.table_spo2);

        spo2_Btn = findViewById(R.id.spo2_Btn);
        bp_Btn = findViewById(R.id.bp_Btn);

        //rhemos device fields added: By Nishita
        bloodGlucose_editText = findViewById(R.id.bloodGlucose_editText);
        bloodGlucose_editText_fasting = findViewById(R.id.bloodGlucose_editText_fasting);
        bloodGlucoseRandom_editText = findViewById(R.id.bloodGlucoseRandom_editText);
        bloodGlucosePostPrandial_editText = findViewById(R.id.bloodGlucosePostPrandial_editText);
        haemoglobin_editText = findViewById(R.id.haemoglobin_editText);
        uricAcid_editText = findViewById(R.id.uricAcid_editText);
        totalCholestrol_editText = findViewById(R.id.totalCholestrol_editText);

        mBMI = findViewById(R.id.table_bmi);
        mAbdominalGirth = findViewById(R.id.table_abdominal_girth);
//    Respiratory added by mahiti dev team

        mResp = findViewById(R.id.table_respiratory);

        mBMI.setEnabled(false);


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
                mHeight.setVisibility(View.VISIBLE);
            } else {
                mHeight.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mWeight")) {
                mWeight.setVisibility(View.VISIBLE);
            } else {
                mWeight.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mPulse")) {
                findViewById(R.id.tinput_bpm).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tinput_bpm).setVisibility(View.GONE);
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
                    findViewById(R.id.tinput_f).setVisibility(View.GONE);
                    tempF_Btn.setVisibility(View.GONE);

                } else if (obj.getBoolean("mFahrenheit")) {

                    mTemperature = findViewById(R.id.table_temp_faren);
                    findViewById(R.id.tinput_c).setVisibility(View.GONE);
                    tempC_Btn.setVisibility(View.GONE);
                }
            } else {
                mTemperature.setVisibility(View.GONE);
            }
            if (obj.getBoolean("mSpo2")) {
                findViewById(R.id.tinput_spo).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tinput_spo).setVisibility(View.GONE);
            }
            if (obj.getBoolean("mBMI")) {
                mBMI.setVisibility(View.VISIBLE);
            } else {
                mBMI.setVisibility(View.GONE);
            }

            if (obj.getBoolean("mResp")) {
                findViewById(R.id.tinput_rr).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tinput_rr).setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "config file error", Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        encounterBill = checkForOldBill();
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
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_WEIGHT)) {
                        mWeight.setError(getString(R.string.weight_error, AppConstants.MAXIMUM_WEIGHT));
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

        spo2_Btn.setOnClickListener(v -> { clickMeasure("SPO2"); });
        bp_Btn.setOnClickListener(v -> { clickMeasure("BP"); });
        tempC_Btn.setOnClickListener(v -> {
            clickMeasure("Temp");
            tempc_clicked = true;
            tempf_clicked = false;
        });
        tempF_Btn.setOnClickListener(v -> {
            clickMeasure("Temp");
            tempc_clicked = false;
            tempf_clicked = true;});

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
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_BP_SYS) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_BP_SYS)) {
                        mBpSys.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                    } else {
                        mBpSys.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mBpSys.getText().toString().startsWith(".")) {
                    mBpSys.setText("");
                } else {

                }
            }
        });

        mBpDia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_BP_DSYS) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_BP_DSYS)) {
                        mBpDia.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                    } else {
                        mBpDia.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mBpDia.getText().toString().startsWith(".")) {
                    mBpDia.setText("");
                } else {

                }
            }
        });

        //abdominal girth
        mAbdominalGirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if(patientGender.equalsIgnoreCase("M")) {
                        if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_ABDOMINAL_GIRTH_MALE)) {
                            mAbdominalGirth.setError(getString(R.string.abdominal_girth_male_error, AppConstants.MAXIMUM_ABDOMINAL_GIRTH_MALE));
                        } else {
                            mAbdominalGirth.setError(null);
                        }
                    }
                    else
                    {
                        if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_ABDOMINAL_GIRTH_FEMALE)) {
                            mAbdominalGirth.setError(getString(R.string.abdominal_girth_male_error, AppConstants.MAXIMUM_ABDOMINAL_GIRTH_FEMALE));
                        } else {
                            mAbdominalGirth.setError(null);
                        }
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (mAbdominalGirth.getText().toString().startsWith(".")) {
                    mAbdominalGirth.setText("");
                } else {
                }
            }
        });

        // glucose - non-fasting
        bloodGlucose_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_GLUCOSE_NON_FASTING) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_GLUCOSE_NON_FASTING)) {
                        bloodGlucose_editText.setError(getString(R.string.glucose_non_fasting_validation,
                                AppConstants.MINIMUM_GLUCOSE_NON_FASTING, AppConstants.MAXIMUM_GLUCOSE_NON_FASTING));
                    } else {
                        bloodGlucose_editText.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (bloodGlucose_editText.getText().toString().startsWith(".")) {
                    bloodGlucose_editText.setText("");
                } else {

                }
            }
        });
        //end

        // glucose - random - start
        bloodGlucoseRandom_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.parseDouble(s.toString()) > Double.parseDouble(AppConstants.MAXIMUM_GLUCOSE_RANDOM) ||
                            Double.parseDouble(s.toString()) < Double.parseDouble(AppConstants.MINIMUM_GLUCOSE_RANDOM)) {
                        bloodGlucoseRandom_editText.setError(getString(R.string.glucose_random_validation,
                                AppConstants.MINIMUM_GLUCOSE_RANDOM, AppConstants.MAXIMUM_GLUCOSE_RANDOM));
                    } else {
                        bloodGlucoseRandom_editText.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (bloodGlucoseRandom_editText.getText().toString().startsWith("."))
                    bloodGlucoseRandom_editText.setText("");
            }
        });
        // glucose - random - end

        // glucose - post-prandial - start
        bloodGlucosePostPrandial_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.parseDouble(s.toString()) > Double.parseDouble(AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL) ||
                            Double.parseDouble(s.toString()) < Double.parseDouble(AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL)) {
                        bloodGlucosePostPrandial_editText.setError(getString(R.string.glucose_post_prandial_validation,
                                AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL, AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL));
                    } else {
                        bloodGlucosePostPrandial_editText.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (bloodGlucosePostPrandial_editText.getText().toString().startsWith("."))
                    bloodGlucosePostPrandial_editText.setText("");
            }
        });
        // glucose - post-prandial - end

        // glucose - fasting
        bloodGlucose_editText_fasting.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_HbA1c) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_HbA1c)) {
                        bloodGlucose_editText_fasting.setError(getString(R.string.hba1c_validation,
                                AppConstants.MINIMUM_HbA1c, AppConstants.MAXIMUM_HbA1c));
                    } else {
                        bloodGlucose_editText_fasting.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (bloodGlucose_editText_fasting.getText().toString().startsWith(".")) {
                    bloodGlucose_editText_fasting.setText("");
                } else {

                }
            }
        });
        //end

        // hemoglobin
        haemoglobin_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_HEMOGLOBIN) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_HEMOGLOBIN)) {
                        haemoglobin_editText.setError(getString(R.string.hemoglobin_validation,
                                AppConstants.MINIMUM_HEMOGLOBIN, AppConstants.MAXIMUM_HEMOGLOBIN));
                    } else {
                        haemoglobin_editText.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (haemoglobin_editText.getText().toString().startsWith(".")) {
                    haemoglobin_editText.setText("");
                } else {

                }
            }
        });

        // Uric Acid
        uricAcid_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_URIC_ACID) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_URIC_ACID)) {
                        uricAcid_editText.setError(getString(R.string.uric_acid_validation,
                                AppConstants.MINIMUM_URIC_ACID, AppConstants.MAXIMUM_URIC_ACID));
                    } else {
                        uricAcid_editText.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (uricAcid_editText.getText().toString().startsWith(".")) {
                    uricAcid_editText.setText("");
                } else {

                }
            }
        });
        //end

        // Total Cholesterol
        totalCholestrol_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0 && !s.toString().startsWith(".")) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_TOTAL_CHOLSTEROL) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_TOTAL_CHOLSTEROL)) {
                        totalCholestrol_editText.setError(getString(R.string.total_cholesterol_validation,
                                AppConstants.MINIMUM_TOTAL_CHOLSTEROL, AppConstants.MAXIMUM_TOTAL_CHOLSTEROL));
                    } else {
                        totalCholestrol_editText.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (totalCholestrol_editText.getText().toString().startsWith(".")) {
                    totalCholestrol_editText.setText("");
                } else {

                }
            }
        });
        //end

        TextView fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTable();
            }
        });
    }

    public void calculateBMI() {
        if (flag_height == 1 && flag_weight == 1 ||
                (mHeight.getText().toString().trim().length() > 0 && !mHeight.getText().toString().startsWith(".") && (mWeight.getText().toString().trim().length() > 0 &&
                        !mWeight.getText().toString().startsWith(".")))) {
            mBMI.getText().clear();
            double numerator = Double.parseDouble(mWeight.getText().toString()) * 10000;
            double denominator = (Double.parseDouble(mHeight.getText().toString())) * (Double.parseDouble(mHeight.getText().toString()));
            double bmi_value = numerator / denominator;
            DecimalFormat df = new DecimalFormat("0.00");
            mBMI.setText(df.format(bmi_value));
            Log.d("BMI", "BMI: " + mBMI.getText().toString());
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
        } else if (flag_height == 0 || flag_weight == 0) {
            // do nothing
            mBMI.getText().clear();
        }
        else
        {
            mBMI.getText().clear();
        }
    }

    public void calculateBMI_onEdit(String height, String weight) {
        if (height.toString().trim().length() > 0 && !height.toString().startsWith(".") &&
                weight.toString().trim().length() > 0 && !weight.toString().startsWith(".")) {

            mBMI.getText().clear();
            double numerator = Double.parseDouble(weight) * 10000;
            double denominator = (Double.parseDouble(height)) * (Double.parseDouble(height));
            double bmi_value = numerator / denominator;
            DecimalFormat df = new DecimalFormat("0.00");
            mBMI.setText(df.format(bmi_value));
            Log.d("BMI","BMI: "+mBMI.getText().toString());
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
        } else  {
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
                mHeight.setText(value);
                break;
            case UuidDictionary.WEIGHT: //Weight
                mWeight.setText(value);
                break;
            case UuidDictionary.PULSE: //Pulse
                mPulse.setText(value);
                break;
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
                mBpSys.setText(value);
                break;
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
                mBpDia.setText(value);
                break;
            case UuidDictionary.ABDOMINAL_GIRTH: //Abdominal Girth
                mAbdominalGirth.setText(value);
                break;
            case UuidDictionary.TEMPERATURE: //Temperature
                if (findViewById(R.id.tinput_c).getVisibility() == View.GONE) {
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
            case UuidDictionary.BLOOD_GLUCOSE_ID: // Glucose // Non-Fasting
                if (!value.equalsIgnoreCase("0"))
                    bloodGlucose_editText.setText(value);
                break;
            case UuidDictionary.BLOOD_GLUCOSE_RANDOM_ID:
                if (!value.equalsIgnoreCase("0"))
                    bloodGlucoseRandom_editText.setText(value);
                break;
            case UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL_ID:
                if(!value.equalsIgnoreCase("0"))
                    bloodGlucosePostPrandial_editText.setText(value);
                break;
            case UuidDictionary.BLOOD_GLUCOSE_FASTING_ID: // Glucose // Non-Fasting
                if(!value.equalsIgnoreCase("0"))
                    bloodGlucose_editText_fasting.setText(value);
                break;
            case UuidDictionary.HEMOGLOBIN_ID: // Hemoglobin
                if(!value.equalsIgnoreCase("0"))
                    haemoglobin_editText.setText(value);
                break;
            case UuidDictionary.URIC_ACID_ID: // Uric Acid
                if(!value.equalsIgnoreCase("0"))
                    uricAcid_editText.setText(value);
                break;
            case UuidDictionary.TOTAL_CHOLESTEROL_ID: // Cholesterol
                if(!value.equalsIgnoreCase("0"))
                    totalCholestrol_editText.setText(value);
                break;
            default:
                break;

        }
        //on edit on vs screen, the bmi will be set in vitals bmi edit field.
        if(mBMI.getText().toString().equalsIgnoreCase("")) {
            calculateBMI_onEdit(mHeight.getText().toString(), mWeight.getText().toString());
        }
    }

    public void validateTable() {
        boolean cancel = false;
        View focusView = null;

        //BP vaidations added by Prajwal.
        if(mBpSys.getText().toString().isEmpty() && !mBpDia.getText().toString().isEmpty() ||
                !mBpSys.getText().toString().isEmpty() && mBpDia.getText().toString().isEmpty()) {
            if(mBpSys.getText().toString().isEmpty()) {
                mBpSys.requestFocus();
//                mBpSys.setError("Enter field");
                mBpSys.setError(getResources().getString(R.string.error_field_required));
                return;
            }
            else if(mBpDia.getText().toString().isEmpty()) {
                mBpDia.requestFocus();
//                mBpDia.setError("Enter field");
                mBpDia.setError(getResources().getString(R.string.error_field_required));
                return;
            }
        }

        // Store values at the time of the fab is clicked.
        ArrayList<EditText> values = new ArrayList<EditText>();
        values.add(mHeight);
        values.add(mWeight);
        values.add(mPulse);
        values.add(mBpSys);
        values.add(mBpDia);
        values.add(mTemperature);
        values.add(mResp);
        values.add(mSpo2);
        values.add(bloodGlucoseRandom_editText);
        values.add(bloodGlucosePostPrandial_editText);
        values.add(bloodGlucose_editText_fasting);
        values.add(haemoglobin_editText);
        values.add(uricAcid_editText);
        values.add(totalCholestrol_editText);
        values.add(mAbdominalGirth);

        // Check to see if values were inputted.
        for (int i = 0; i < values.size(); i++) {
            if (i == 0) {
                EditText et = values.get(i);
                String abc = et.getText().toString().trim();
                if (abc != null && !abc.isEmpty()) {
                    if (Double.parseDouble(abc) > Double.parseDouble(AppConstants.MAXIMUM_HEIGHT)) {
                        et.setError(getString(R.string.height_error, AppConstants.MAXIMUM_HEIGHT));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }
            } else if (i == 1) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty()) {
                    if (Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_WEIGHT)) {
                        et.setError(getString(R.string.weight_error, AppConstants.MAXIMUM_WEIGHT));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 2) {
                EditText et = values.get(i);
                String abc2 = et.getText().toString().trim();
                if (abc2 != null && !abc2.isEmpty() && (!abc2.equals("0.0"))) {
                    if ((Double.parseDouble(abc2) > Double.parseDouble(AppConstants.MAXIMUM_PULSE)) ||
                            (Double.parseDouble(abc2) < Double.parseDouble(AppConstants.MINIMUM_PULSE))) {
                        et.setError(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 3) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_SYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_SYS))) {
                        et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 4) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_DSYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_DSYS))) {
                        et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }

            } else if (i == 5) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if (configUtils.celsius()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS))) {
                            et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            focusView = et;
                            cancel = true;
                            break;
                        } else {
                            cancel = false;
                        }
                    } else if (configUtils.fahrenheit()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_FARHENIT))) {
                            et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                            focusView = et;
                            cancel = true;
                            break;
                        } else {
                            cancel = false;
                        }
                    }
                } else {
                    cancel = false;
                }
            } else if (i == 6) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_RESPIRATORY)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_RESPIRATORY))) {
                        et.setError(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }
            } else if (i == 7) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_SPO2)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_SPO2))) {
                        et.setError(getString(R.string.spo2_error,
                                AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
//       }
                } else {
                    cancel = false;
                }
            }

            // glucose - random
            else if (i == 8) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_GLUCOSE_RANDOM)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_GLUCOSE_RANDOM))) {
                        et.setError(getString(R.string.glucose_random_validation,
                                AppConstants.MAXIMUM_GLUCOSE_RANDOM, AppConstants.MINIMUM_GLUCOSE_RANDOM));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
                } else {
                    cancel = false;
                }
            }

            // glucose - post-prandial
            else if (i == 9) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL))) {
                        et.setError(getString(R.string.glucose_post_prandial_validation,
                                AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL, AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
                } else {
                    cancel = false;
                }
            }

            // glucose - fasting
            else if (i == 10) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_HbA1c)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_HbA1c))) {
                        et.setError(getString(R.string.hba1c_validation,
                                AppConstants.MINIMUM_HbA1c, AppConstants.MAXIMUM_HbA1c));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
                } else {
                    cancel = false;
                }
            }
            // hemoglobin
            else if (i == 11) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_HEMOGLOBIN)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_HEMOGLOBIN))) {
                        et.setError(getString(R.string.hemoglobin_validation,
                                AppConstants.MINIMUM_HEMOGLOBIN, AppConstants.MAXIMUM_HEMOGLOBIN));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
                } else {
                    cancel = false;
                }
            }

            // uric acid
            else if (i == 12) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_URIC_ACID)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_URIC_ACID))) {
                        et.setError(getString(R.string.uric_acid_validation,
                                AppConstants.MINIMUM_URIC_ACID, AppConstants.MAXIMUM_URIC_ACID));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
                } else {
                    cancel = false;
                }
            }

            // total cholesterol
            else if (i == 13) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TOTAL_CHOLSTEROL)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TOTAL_CHOLSTEROL))) {
                        et.setError(getString(R.string.total_cholesterol_validation,
                                AppConstants.MINIMUM_TOTAL_CHOLSTEROL, AppConstants.MAXIMUM_TOTAL_CHOLSTEROL));
                        focusView = et;
                        cancel = true;
                        break;
                    } else {
                        cancel = false;
                    }
                } else {
                    cancel = false;
                }
            }

            // abdominal girth
            else if (i == 14) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if(patientGender.equalsIgnoreCase("M")) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_ABDOMINAL_GIRTH_MALE))) {
                            et.setError(getString(R.string.abdominal_girth_male_error,
                                    AppConstants.MAXIMUM_ABDOMINAL_GIRTH_MALE));
                            focusView = et;
                            cancel = true;
                            break;
                        } else {
                            cancel = false;
                        }
                    }
                    else {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_ABDOMINAL_GIRTH_FEMALE))) {
                            et.setError(getString(R.string.abdominal_girth_male_error,
                                    AppConstants.MAXIMUM_ABDOMINAL_GIRTH_FEMALE));
                            focusView = et;
                            cancel = true;
                            break;
                        } else {
                            cancel = false;
                        }
                    }
                } else {
                    cancel = false;
                }
            }
        }


        if (cancel) {
            // There was an error - focus the first form field with an error.
            focusView.requestFocus();
            return;
        } else {
            try {
                if (mHeight.getText() != null && !mHeight.getText().toString().equals("")) {
                    results.setHeight((mHeight.getText().toString()));
                } else if (mHeight.getText().toString().equals("")) {
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
                if (mAbdominalGirth.getText() != null) {
                    results.setAbdominalGirth((mAbdominalGirth.getText().toString()));
                }
                if (mTemperature.getText() != null) {

                    if (findViewById(R.id.tinput_c).getVisibility() == View.GONE) {
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
                if (bloodGlucose_editText.getText() != null && !bloodGlucose_editText.getText().toString().equals("")) {
                    results.setBloodglucose((bloodGlucose_editText.getText().toString()));
                } else
                    results.setBloodglucose("0");
                if (bloodGlucoseRandom_editText.getText() != null && !bloodGlucoseRandom_editText.getText().toString().equals("")) {
                    results.setBloodGlucoseRandom((bloodGlucoseRandom_editText.getText().toString()));
                } else
                    results.setBloodGlucoseRandom("0");
                if (bloodGlucosePostPrandial_editText.getText() != null && !bloodGlucosePostPrandial_editText.getText().toString().equals("")) {
                    results.setBloodGlucosePostPrandial(bloodGlucosePostPrandial_editText.getText().toString());
                } else
                    results.setBloodGlucosePostPrandial("0");
                if (bloodGlucose_editText_fasting.getText() != null && !bloodGlucose_editText_fasting.getText().toString().equals("")) {
                    results.setBloodglucoseFasting((bloodGlucose_editText_fasting.getText().toString()));
                } else
                    results.setBloodglucoseFasting("0");
                if (haemoglobin_editText.getText() != null && !haemoglobin_editText.getText().toString().equals("")) {
                    results.setHemoglobin((haemoglobin_editText.getText().toString()));
                } else
                    results.setHemoglobin("0");
                if (uricAcid_editText.getText() != null && !uricAcid_editText.getText().toString().equals("")) {
                    results.setUricAcid((uricAcid_editText.getText().toString()));
                } else
                    results.setUricAcid("0");
                if (totalCholestrol_editText.getText() != null && !totalCholestrol_editText.getText().toString().equals("")) {
                    results.setTotlaCholesterol((totalCholestrol_editText.getText().toString()));
                } else
                    results.setTotlaCholesterol("0");


            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(R.id.cl_table), R.string.error_non_decimal_no_added, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

//
        }

        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        String price = "0";

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

                // Glucose
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_ID);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBloodglucose());
                price = conceptAttributeListDAO.getConceptPrice("Blood Sugar (Non-Fasting)");
                price = getPrice(price, price.indexOf('.'));
                if ((results.getBloodglucose() == null || results.getBloodglucose().equals("0") || results.getBloodglucose().equals("") || results.getBloodglucose().equals(" ")) && (encounterBill != null && !encounterBill.equals("")))
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID, "0");
                else
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID, price);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID));
                obsDAO.updateObs(obsDTO);

                // Glucose - Random
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_RANDOM_ID);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBloodGlucoseRandom());
                price = conceptAttributeListDAO.getConceptPrice("Blood Sugar (Random)");
                price = getPrice(price, price.indexOf('.'));
                if ((results.getBloodGlucoseRandom() == null || results.getBloodGlucoseRandom().equals("0") || results.getBloodGlucoseRandom().equals("") || results.getBloodGlucoseRandom().equals(" ")) && (encounterBill != null && !encounterBill.equals("")))
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_RANDOM_ID, "0");
                else
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_RANDOM_ID, price);

                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.BLOOD_GLUCOSE_RANDOM_ID));
                obsDAO.updateObs(obsDTO);

                // Glucose - Post-prandial
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL_ID);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBloodGlucosePostPrandial());
                price = conceptAttributeListDAO.getConceptPrice("Blood Sugar ( Post-prandial)");
                price = getPrice(price, price.indexOf('.'));
                if ((results.getBloodGlucosePostPrandial() == null || results.getBloodGlucosePostPrandial().equals("0") || results.getBloodGlucosePostPrandial().equals("") || results.getBloodGlucosePostPrandial().equals(" ")) && (encounterBill != null && !encounterBill.equals("")))
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_POST_PRANDIAL_ID, "0");
                else
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_POST_PRANDIAL_ID, price);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL_ID));
                obsDAO.updateObs(obsDTO);

                // Glucose - Fasting
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_FASTING_ID);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBloodglucoseFasting());
                price = conceptAttributeListDAO.getConceptPrice("Blood Glucose (Fasting)");
                price = getPrice(price, price.indexOf('.'));
                if ((results.getBloodglucoseFasting() == null || results.getBloodglucoseFasting().equals("0") || results.getBloodglucoseFasting().equals("") || results.getBloodglucoseFasting().equals(" ")) && (encounterBill != null && !encounterBill.equals("")))
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_FASTING_ID, "0");
                else
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_FASTING_ID, price);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.BLOOD_GLUCOSE_FASTING_ID));
                obsDAO.updateObs(obsDTO);

                // Hemoglobin
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.HEMOGLOBIN_ID);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getHemoglobin());
                price = conceptAttributeListDAO.getConceptPrice("Haemoglobin Test");
                price = getPrice(price, price.indexOf('.'));
                if ((results.getHemoglobin() == null || results.getHemoglobin().equals("0") || results.getHemoglobin().equals("") || results.getHemoglobin().equals(" ")) && (encounterBill != null && !encounterBill.equals("")))
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_HEMOGLOBIN_ID, "0");
                else
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_HEMOGLOBIN_ID, price);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.HEMOGLOBIN_ID));
                obsDAO.updateObs(obsDTO);

                // Uric Acid
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.URIC_ACID_ID);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getUricAcid());
                price = conceptAttributeListDAO.getConceptPrice("SERUM URIC ACID");
                price = getPrice(price, price.indexOf('.'));
                if ((results.getUricAcid() == null || results.getUricAcid().equals("0") || results.getUricAcid().equals("") || results.getUricAcid().equals(" ")) && (encounterBill != null && !encounterBill.equals("")))
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_URIC_ACID_ID, "0");
                else
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_URIC_ACID_ID, price);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.URIC_ACID_ID));
                obsDAO.updateObs(obsDTO);

                // total cholesterol
                obsDTO = new ObsDTO();
                obsDTO.setConceptuuid(UuidDictionary.TOTAL_CHOLESTEROL_ID);
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getTotlaCholesterol());
                price = conceptAttributeListDAO.getConceptPrice("TOTAL CHOLESTEROL");
                price = getPrice(price, price.indexOf('.'));
                if ((results.getTotlaCholesterol() == null || results.getTotlaCholesterol().equals("0") || results.getTotlaCholesterol().equals("") || results.getTotlaCholesterol().equals(" ")) && (encounterBill != null && !encounterBill.equals("")))
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_TOTAL_CHOLESTEROL_ID, "0");
                else
                    updateBillEncounter(encounterBill, UuidDictionary.BILL_PRICE_TOTAL_CHOLESTEROL_ID, price);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.TOTAL_CHOLESTEROL_ID));
                obsDAO.updateObs(obsDTO);

                //making flag to false in the encounter table so it will sync again
                EncounterDAO encounterDAO = new EncounterDAO();
                try {
                    encounterDAO.updateEncounterSync("false", encounterVitals);
                    encounterDAO.updateEncounterModifiedDate(encounterVitals);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                //sync has to be performed once the vitals are updated for the bill update feature
                SyncUtils syncUtils = new SyncUtils();
                boolean success = false;
                success = syncUtils.syncForeground("bill");

                if(!success) {
                    Toast.makeText(VitalsActivity.this, getString(R.string.sync_failed), Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(VitalsActivity.this, VisitSummaryActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("patientFirstName",patientFName);
                intent.putExtra("patientLastName", patientLName);
                intent.putExtra("gender", patientGender);
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

            // Glucose
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_ID);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBloodglucose());
            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            // Glucose - Random
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_RANDOM_ID);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBloodGlucoseRandom());
            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            // Glucose - Post-prandial
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL_ID);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBloodGlucosePostPrandial());
            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            // Glucose - Fasting
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_FASTING_ID);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getBloodglucoseFasting());
            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            // Hemoglobin
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.HEMOGLOBIN_ID);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getHemoglobin());
            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            // Uric Acid Test
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.URIC_ACID_ID);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getUricAcid());
            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            // total cholesterol Test
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.TOTAL_CHOLESTEROL_ID);
            obsDTO.setEncounteruuid(encounterVitals);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(results.getTotlaCholesterol());
            try {
                obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }


            Intent intent = new Intent(VitalsActivity.this, ComplaintNodeActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("patientFirstName",patientFName);
            intent.putExtra("patientLastName", patientLName);
            intent.putExtra("gender", patientGender);
            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
            intent.putExtra("tag", intentTag);
            startActivity(intent);
        }
    }

    private String ConvertFtoC(String temperature) {
        if (temperature != null && temperature.length() > 0) {
            //This new code has been added as previous throwing errors for Marathi language: By Nishita
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
    }

    private String checkForOldBill() {
        String billEncounterUuid = "";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitUuid, "0"};
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("Visit Billing Details").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    billEncounterUuid = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        //  encounterCursor.close();

        return billEncounterUuid;

    }

    private void updateBillEncounter(String encounterBill, String obsConceptID, String price) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO1 = new ObsDTO();
        obsDTO1.setConceptuuid(obsConceptID);
        obsDTO1.setEncounteruuid(encounterBill);
        obsDTO1.setCreator(sessionManager.getCreatorID());
        obsDTO1.setValue(price);
        try {
            obsDTO1.setUuid(obsDAO.getObsuuid(encounterBill, obsConceptID));
        } catch (DAOException e) {
            e.printStackTrace();
        }
        obsDAO.updateObs(obsDTO1);

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterBill);
            encounterDAO.updateEncounterModifiedDate(encounterBill);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private String getPrice(String price, int indexOf) {
        return price.substring(0, indexOf);
    }

    public void setLocale(String appLanguage) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
            VitalsActivity.this.createConfigurationContext(conf);
        }
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
    }

    @Override
    public void onServiceBind() {
        if (!IntelehealthApplication.isUseCustomBleDevService) {
            onBleState(MonitorDataTransmissionManager.getInstance().getBleState());
        }

        if (IntelehealthApplication.isUseCustomBleDevService) {
            BleDevManager bleDevManager = mHcService.getBleDevManager();
            mHcService.setOnDeviceVersionListener(this);
            bleDevManager.getBatteryTask().setBatteryStateListener(this);
            bleDevManager.getDeviceTask().setOnDeviceInfoListener(this);
        } else {
            MonitorDataTransmissionManager.getInstance().setOnBleConnectListener(this);
            MonitorDataTransmissionManager.getInstance().setOnBatteryListener(this);
            MonitorDataTransmissionManager.getInstance().setOnDevIdAndKeyListener(this);
            MonitorDataTransmissionManager.getInstance().setOnDeviceVersionListener(this);
        }
    }

    @Override
    public void onServiceUnbind() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetoothOption: {
                // Init Remos
                initRemosDevice();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initRemosDevice() {
        //Bind service about Bluetooth connection.
        if (IntelehealthApplication.isUseCustomBleDevService) {
            Intent serviceIntent = new Intent(this, HcService.class);
            bindService(serviceIntent, this, BIND_AUTO_CREATE);
        } else {
            //绑定服务，
            // 类型是 HealthMonitor（HealthMonitor健康检测仪），
            MonitorDataTransmissionManager.getInstance().bind(DeviceType.HealthMonitor, this, this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onBindingDied(ComponentName name) {
        ServiceConnection.super.onBindingDied(name);
    }

    @Override
    public void onBatteryCharging() {

    }

    @Override
    public void onBatteryQuery(int i) {

    }

    @Override
    public void onBatteryFull() {

    }

    @Override
    public void onBLENoSupported() {

    }

    @Override
    public void onOpenBLE() {
        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_OPEN_BT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OPEN_BT) {//蓝牙启动结果
            //蓝牙启动结果
            Toast.makeText(VitalsActivity.this, resultCode == Activity.RESULT_OK ? "蓝牙已打开" : "蓝牙打开失败", Toast.LENGTH_SHORT).show();
            clickConnect();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBleState(int bleState) {
        switch (bleState) {
            case BluetoothState.BLE_CLOSED:
              //  btnText.set(getString(R.string.turn_on_bluetooth));
              //  reset();
                break;
            case BluetoothState.BLE_OPENED_AND_DISCONNECT:
                try {
                 //   btnText.set(getString(R.string.connect));
                  //  reset();
                } catch (Exception ignored) {
                }
                break;
            case BluetoothState.BLE_CONNECTING_DEVICE:
                try {
                  //  btnText.set(getString(R.string.connecting));
                } catch (Exception ignored) {
                }
                break;
            case BluetoothState.BLE_CONNECTED_DEVICE:
              //  btnText.set(getString(R.string.disconnect));
                break;
        }
    }

    @Override
    public void onUpdateDialogBleList() {

    }

    @Override
    public void onDeviceInfo(DeviceInfo deviceInfo) {

    }

    @Override
    public void onReadDeviceInfoFailed() {

    }

    @Override
    public void onDeviceVersion(int i, String s) {

    }

    public void clickConnect() {
        if (IntelehealthApplication.isUseCustomBleDevService) {
            if (!PermissionManager.isObtain(this, PermissionManager.PERMISSION_LOCATION
                    , PermissionManager.requestCode_location)) {
                return;
            } else {
                if (!PermissionManager.canScanBluetoothDevice(VitalsActivity.this)) {
                    new AlertDialog.Builder(VitalsActivity.this)
                            .setTitle("提示")
                            .setMessage("Android 6.0及以上系统需要打开位置开关才能扫描蓝牙设备。")
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton("打开位置开关"
                                    , (dialog, which) -> PermissionManager.openGPS(VitalsActivity.this)).create().show();
                    return;
                }
            }
            if (mHcService.isConnected) {
                mHcService.disConnect();
            } else {
                final int bluetoothEnable = mHcService.isBluetoothEnable();
                if (bluetoothEnable == -1) {
                    onBLENoSupported();
                } else if (bluetoothEnable == 0) {
                    onOpenBLE();
                } else {
                    mHcService.quicklyConnect();
                }
            }
        } else {
            final int bleState = MonitorDataTransmissionManager.getInstance().getBleState();
            Log.e("clickConnect", "bleState:" + bleState);
            switch (bleState) {
                case BluetoothState.BLE_CLOSED:
                    MonitorDataTransmissionManager.getInstance().bleCheckOpen();
                    break;
                case BluetoothState.BLE_OPENED_AND_DISCONNECT:
                    if (MonitorDataTransmissionManager.getInstance().isScanning()) {
                        new AlertDialog.Builder(VitalsActivity.this)
                                .setTitle("提示")
                                .setMessage("正在扫描设备，请稍后...")
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton("停止扫描"
                                        , (dialogInterface, i) ->
                                                MonitorDataTransmissionManager.getInstance().scan(false)).create().show();
                    } else {
                        if (PermissionManager.isObtain(this, PermissionManager.PERMISSION_LOCATION
                                , PermissionManager.requestCode_location)) {
                            if (PermissionManager.canScanBluetoothDevice(getApplicationContext())) {
                              //  connectByDeviceList();
                                MonitorDataTransmissionManager.getInstance().scan(true);
                               /* if (showScanList) {   // todo: handle later
                                    connectByDeviceList();
                                } else {
                                    MonitorDataTransmissionManager.getInstance().scan(true);
                                }*/
                            } else {
                                new AlertDialog.Builder(VitalsActivity.this)
                                        .setTitle("提示")
                                        .setMessage("Android 6.0及以上系统需要打开位置开关才能扫描蓝牙设备。")
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .setPositiveButton("turn on location", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                PermissionManager.openGPS(VitalsActivity.this);
                                                clickConnect();
                                            }
                                        }).create().show();

                            }
                        }
                    }
                    break;
                case BluetoothState.BLE_CONNECTING_DEVICE:
//                    Toast.makeText(mActivity, "蓝牙连接中...", Toast.LENGTH_SHORT).show();
                    MonitorDataTransmissionManager.getInstance().disConnectBle();
                    break;
                case BluetoothState.BLE_CONNECTED_DEVICE:
                case BluetoothState.BLE_NOTIFICATION_DISABLED:
                case BluetoothState.BLE_NOTIFICATION_ENABLED:
                    MonitorDataTransmissionManager.getInstance().disConnectBle();
                    break;
            }
        }

    }

/*
    private void connectByDeviceList() {
        mBleDeviceListDialogFragment = new BleDeviceListDialogFragment();
        mBleDeviceListDialogFragment.show(VitalsActivity.this.getSupportFragmentManager(), "");
    }
*/

    public void clickMeasure(String testType) {
        if (IntelehealthApplication.isUseCustomBleDevService) {
            if (!mHcService.isConnected) {
              //  toast(R.string.device_disconnect);
                return;
            }
            //判断设备是否在充电，充电时不可测量
            if (mHcService.getBleDevManager().getBatteryTask().isCharging()) {
              //  toast(R.string.charging);
                return;
            }
            if (mHcService.getBleDevManager().isMeasuring()) {
                stopMeasure(testType);
                //设置ViewPager可滑动
             //   btnMeasure.setText(R.string.start_measuring);
            } else {
               // reset();
                if (startMeasure(testType)) {
                    /*
                     * 请注意了：为了代码逻辑不会混乱，每一单项在测量过程中请确保用户无法通过任何途径
                     * (当然，如果用户强制关闭页面就不管了)切换至其他测量单项的界面，直到本项一次测量结束。
                     */
                    //设置ViewPager不可滑动
                  //  btnMeasure.setText(R.string.measuring);
                }
            }
        } else {
            final MonitorDataTransmissionManager manager = MonitorDataTransmissionManager.getInstance();

            //判断手机是否和设备实现连接
            if (!manager.isConnected()) {
              //  toast(R.string.device_disconnect);
                return;
            }
            //判断设备是否在充电，充电时不可测量
            if (manager.isCharging()) {
              //  toast(R.string.charging);
                return;
            }
            //判断是否测量中...
            if (manager.isMeasuring()) {
//            if (mPosition != 2) {//体温没有停止方法，当点击停止的是非体温时才执行停止
                //停止测量
                stopMeasure(testType);
                //设置ViewPager可滑动
              //  btnMeasure.setText(getString(R.string.start_measuring));
//            }
            } else {
             //   reset();
                //开始测量
                if (startMeasure(testType)) {
                    /*
                     * 请注意了：为了代码逻辑不会混乱，每一单项在测量过程中请确保用户无法通过任何途径
                     * (当然，如果用户强制关闭页面就不管了)切换至其他测量单项的界面，直到本项一次测量结束。
                     */
                    //设置ViewPager不可滑动
                  //  btnMeasure.setText(R.string.measuring);
                }
            }

        }
    }

    public void stopMeasure(String testType) {
        if (mOxTask != null) {
            mOxTask.stop();
        } else {
            MonitorDataTransmissionManager.getInstance().stopMeasure();
        }

        if (mBpTask != null) {
            mBpTask.stop();
        } else {
            MonitorDataTransmissionManager.getInstance().stopMeasure();
        }

        //BT module is not have method stop().Because it will return result in 2~4 seconds when you click to start measure.

    }

    public boolean startMeasure(String testType) {
        switch (testType) {
            case "SPO2":
                // spo2
                spo2_test();
                return true;

            case "BP":
                bp_test();
                return true;

            case "Temp":
                temp_test();    // both methods same as remos allows only celsius for Fah convert it mathematically.
                return true;

            default:
                return true;
        }
    }

    private void temp_test() {
        if (mHcService != null) {
            mBtTask = mHcService.getBleDevManager().getBtTask();
            mBtTask.setOnBtResultListener(this);
        } else {
            MonitorDataTransmissionManager.getInstance().setOnBtResultListener(this);
        }

        if (mBtTask != null) {
            mBtTask.start();
        } else {
            MonitorDataTransmissionManager.getInstance().startMeasure(MeasureType.BT);
        }
    }

    private boolean spo2_test() {
        if (mHcService != null) {
            mOxTask = mHcService.getBleDevManager().getOxTask();
            mOxTask.setOnSpO2ResultListener(this);
        } else {
            MonitorDataTransmissionManager.getInstance().setOnSpO2ResultListener(this);
        }

        if (mOxTask != null) {
            mOxTask.start();
        } else {
            MonitorDataTransmissionManager.getInstance().startMeasure(MeasureType.SPO2);
        }
        return true;
    }

    private boolean bp_test() {
        if (mHcService != null) {
            mBpTask = mHcService.getBleDevManager().getBpTask();
            mBpTask.setOnBpResultListener(this);
        } else {
            //设置血压测量回调接口
            MonitorDataTransmissionManager.getInstance().setOnBpResultListener(this);
        }

        if (mBpTask != null) {
            if (mHcService.getBleDevManager().getBatteryTask().getPower() < 20) {
                Toast.makeText(VitalsActivity.this, "设备电量过低，请充电\nLow power.Please charge.", Toast.LENGTH_SHORT).show();
                return false;
            }
            mBpTask.start();
        } else {
            if (MonitorDataTransmissionManager.getInstance().getBatteryValue() < 20) {
                Toast.makeText(VitalsActivity.this, "设备电量过低，请充电\nLow power.Please charge.", Toast.LENGTH_SHORT).show();
                return false;
            }
            MonitorDataTransmissionManager.getInstance().startMeasure(MeasureType.BP);
        }
        return true;
    }

    @Override
    public void onSpO2Result(int spo2, int heart_rate) {
        spO2_model.setValue(spo2);
        spO2_model.setHr(heart_rate);
    }

    @Override
    public void onSpO2Wave(int i) {

    }

    @Override
    public void onSpO2End() {
        Log.e("SPO2", "SPO2: " + spO2_model.getValue() + " : " + spO2_model.getHr());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSpo2.setText(String.valueOf(spO2_model.getValue()));
                mPulse.setText(String.valueOf(spO2_model.getHr()));
            }
        });

    }

    @Override
    public void onFingerDetection(int state) {
        if (state == FINGER_NO_TOUCH) {
            stopMeasure("SPO2");
            Toast.makeText(VitalsActivity.this, "No finger was detected on the SpO₂ sensor.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBpResult(final int systolicPressure, final int diastolicPressure, final int heartRate) {
        bp_model.setTs(System.currentTimeMillis() / 1000L);
        bp_model.setSbp(systolicPressure);
        bp_model.setDbp(diastolicPressure);
        bp_model.setHr(heartRate);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBpSys.setText(String.valueOf(bp_model.getSbp()));
                mBpDia.setText(String.valueOf(bp_model.getDbp()));
            }
        });
     //   resetState();
    }

    @Override
    public void onBpResultError() {
        Toast.makeText(VitalsActivity.this, "Blood result error. Try again!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLeakError(int errorType) {
       // resetState();
        Observable.just(errorType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(error -> {
                    int textId = 0;
                    switch (error) {
                        case 0:
                            textId = R.string.leak_and_check;
                            break;
                        case 1:
                            textId = R.string.measurement_void;
                            break;
                        default:
                            break;
                    }
                    if (textId != 0)
                        Toast.makeText(VitalsActivity.this, getString(textId), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBtResult(double tempValue) {
        /*Remos Doc: The body temperature measurement only callbacks the temperature value of Celsius (℃).
        For the temperature value of Fahrenheit (℉), please convert according to the conversion formula.
         It is not provided in the SDK. Please refer to Demo for details.*/

        bt_model.setTs(System.currentTimeMillis() / 1000L);
        bt_model.setTemp(tempValue);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String value = String.valueOf(tempValue);
                if (findViewById(R.id.tinput_c).getVisibility() == View.GONE) {
                    //Converting Celsius to Fahrenheit
                    if (value != null && !value.isEmpty()) {
                        mTemperature.setText(convertCtoF(value));
                    }
                } else {
                    mTemperature.setText(value);
                }
            }
        });


      //  resetState();
    }
}

package org.intelehealth.app.activities.vitalActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

public class VitalsActivity extends AppCompatActivity {
    private static final String TAG = VitalsActivity.class.getSimpleName();
    SessionManager sessionManager;
    private String patientName = "", patientFName = "", patientLName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    int flag_height = 0, flag_weight = 0;
    String heightvalue;
    String weightvalue;
    ConfigUtils configUtils = new ConfigUtils(VitalsActivity.this);

    VitalsObject results = new VitalsObject();
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mtempfaren, mSpo2, mBMI, mResp,
            bloodGlucose_editText, bloodGlucose_editText_fasting, bloodGlucoseRandom_editText, bloodGlucosePostPrandial_editText,
            haemoglobin_editText, uricAcid_editText, totalCholestrol_editText;

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

        sessionManager = new SessionManager(this);


//        Setting the title
        setTitle(getString(R.string.title_activity_vitals));
        setTitle(patientName + ": " + getTitle());

        mHeight = findViewById(R.id.table_height);
        mWeight = findViewById(R.id.table_weight);
        mPulse = findViewById(R.id.table_pulse);
        mBpSys = findViewById(R.id.table_bpsys);
        mBpDia = findViewById(R.id.table_bpdia);
        mTemperature = findViewById(R.id.table_temp);
        mSpo2 = findViewById(R.id.table_spo2);

        //rhemos device fields added: By Nishita
        bloodGlucose_editText = findViewById(R.id.bloodGlucose_editText);
        bloodGlucose_editText_fasting = findViewById(R.id.bloodGlucose_editText_fasting);
        bloodGlucoseRandom_editText = findViewById(R.id.bloodGlucoseRandom_editText);
        bloodGlucosePostPrandial_editText = findViewById(R.id.bloodGlucosePostPrandial_editText);
        haemoglobin_editText = findViewById(R.id.haemoglobin_editText);
        uricAcid_editText = findViewById(R.id.uricAcid_editText);
        totalCholestrol_editText = findViewById(R.id.totalCholestrol_editText);

        mBMI = findViewById(R.id.table_bmi);
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

                } else if (obj.getBoolean("mFahrenheit")) {

                    mTemperature = findViewById(R.id.table_temp_faren);
                    findViewById(R.id.tinput_c).setVisibility(View.GONE);
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
                    if (Double.parseDouble(s.toString()) > Double.parseDouble(AppConstants.MAXIMUM_GLUCOSE_NON_FASTING) ||
                            Double.parseDouble(s.toString()) < Double.parseDouble(AppConstants.MINIMUM_GLUCOSE_NON_FASTING)) {
                        bloodGlucoseRandom_editText.setError(getString(R.string.glucose_non_fasting_validation,
                                AppConstants.MINIMUM_GLUCOSE_NON_FASTING, AppConstants.MAXIMUM_GLUCOSE_NON_FASTING));
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
                    if (Double.parseDouble(s.toString()) > Double.parseDouble(AppConstants.MAXIMUM_GLUCOSE_NON_FASTING) ||
                            Double.parseDouble(s.toString()) < Double.parseDouble(AppConstants.MINIMUM_GLUCOSE_NON_FASTING)) {
                        bloodGlucosePostPrandial_editText.setError(getString(R.string.glucose_non_fasting_validation,
                                AppConstants.MINIMUM_GLUCOSE_NON_FASTING, AppConstants.MAXIMUM_GLUCOSE_NON_FASTING));
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
                    if (Double.valueOf(s.toString()) > Double.valueOf(AppConstants.MAXIMUM_GLUCOSE_FASTING) ||
                            Double.valueOf(s.toString()) < Double.valueOf(AppConstants.MINIMUM_GLUCOSE_FASTING)) {
                        bloodGlucose_editText_fasting.setError(getString(R.string.glucose_fasting_validation,
                                AppConstants.MINIMUM_GLUCOSE_FASTING, AppConstants.MAXIMUM_GLUCOSE_FASTING));
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
            } else {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_SPO2)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_SPO2))) {
                        et.setError(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
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

        if(temperature != null && temperature.length() > 0) {
            String result = "";
            double fTemp = Double.parseDouble(temperature);
            double cTemp = ((fTemp - 32) * 5 / 9);
            Log.i(TAG, "uploadTemperatureInC: " + cTemp);
            DecimalFormat dtime = new DecimalFormat("#.##");
            cTemp = Double.parseDouble(dtime.format(cTemp));
            result = String.valueOf(cTemp);
            return result;
        }
        return "";

    }

    private String convertCtoF(String temperature) {

        String result = "";
        double a = Double.parseDouble(String.valueOf(temperature));
        Double b = (a * 9 / 5) + 32;

        DecimalFormat dtime = new DecimalFormat("#.##");
        b = Double.parseDouble(dtime.format(b));

        result = String.valueOf(b);
        return result;

    }

    @Override
    public void onBackPressed() {
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
}

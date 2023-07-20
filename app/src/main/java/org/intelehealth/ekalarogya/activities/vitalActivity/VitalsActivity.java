package org.intelehealth.ekalarogya.activities.vitalActivity;

import android.app.Dialog;
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
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
    int flag_height = 0, flag_weight = 0;
    String heightvalue;
    String weightvalue;
    ConfigUtils configUtils = new ConfigUtils(VitalsActivity.this);

    VitalsObject results = new VitalsObject();
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mtempfaren, mSpo2, mBMI, mResp,
            mHemoglobin, mSugarRandom, mSugarFasting, mSugarAfterMeal;
    Spinner mBlood_Spinner;
    ArrayAdapter<CharSequence> bloodAdapter;

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

        mBMI = findViewById(R.id.table_bmi);
//    Respiratory added by mahiti dev team

        mResp = findViewById(R.id.table_respiratory);

        mBMI.setEnabled(false);

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

        mHemoglobin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] data = new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0",
                        "5.5", "6.0", "6.5", "7.0", "7.5", "8.0", "8.5", "9.0", "9.5", "10.0", "10.5", "11.0", "11.5", "12.0",
                        "12.5", "13.0", "13.5", "14.0", "14.5", "15.0", "15.5", "16.0", "16.5", "17.0", "17.5", "18.0",
                        "18.5", "19.0", "19.5", "20.0"};

                setVitalInfoForHemoAndSugar(data, mHemoglobin, mHemoglobin.getText().toString().trim());
            }
        });

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
            }
        });

        mSugarAfterMeal.addTextChangedListener(new TextWatcher() {
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
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
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
            //DecimalFormat df = new DecimalFormat("0.00");
            //mBMI.setText(df.format(bmi_value));
            mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
            Log.d("BMI", "BMI: " + mBMI.getText().toString());
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
        } else if (flag_height == 0 || flag_weight == 0) {
            // do nothing
            mBMI.getText().clear();
        } else {
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
            //DecimalFormat df = new DecimalFormat("0.00");
            //mBMI.setText(df.format(bmi_value));
            mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
            Log.d("BMI", "BMI: " + mBMI.getText().toString());
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
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

        //BP vaidations added by Prajwal.
        if (mBpSys.getText().toString().isEmpty() && !mBpDia.getText().toString().isEmpty() ||
                !mBpSys.getText().toString().isEmpty() && mBpDia.getText().toString().isEmpty()) {
            if (mBpSys.getText().toString().isEmpty()) {
                mBpSys.requestFocus();
                mBpSys.setError("Enter field");
                return;
            } else if (mBpDia.getText().toString().isEmpty()) {
                mBpDia.requestFocus();
                mBpDia.setError("Enter field");
                return;
            }
        }

        //Sugar Level vaidations
        if (mSugarFasting.getText().toString().isEmpty() && !mSugarAfterMeal.getText().toString().isEmpty() ||
                !mSugarFasting.getText().toString().isEmpty() && mSugarAfterMeal.getText().toString().isEmpty()) {
            if (mSugarFasting.getText().toString().isEmpty()) {
                mSugarFasting.requestFocus();
                mSugarFasting.setError("Enter field");
                return;
            } else if (mSugarAfterMeal.getText().toString().isEmpty()) {
                mSugarAfterMeal.requestFocus();
                mSugarAfterMeal.setError("Enter field");
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
        values.add(mHemoglobin);
        values.add(mSugarRandom);
        values.add(mSugarFasting);
        values.add(mSugarAfterMeal);

        // Check to see if values were inputted.
        if (!intentAdviceFrom.equalsIgnoreCase("Sevika")) {
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
                } else if (i == 8) {
                    EditText et = values.get(i);
                    String abc1 = et.getText().toString().trim();
                    if (abc1 != null && !abc1.isEmpty()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_HEMOGLOBIN)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_HEMOGLOBIN))) {
                            et.setError(getString(R.string.hemoglobin_error, AppConstants.MINIMUM_HEMOGLOBIN, AppConstants.MAXIMUM_HEMOGLOBIN));
                            focusView = et;
                            cancel = true;
                            break;
                        } else {
                            cancel = false;
                        }
                    } else {
                        cancel = false;
                    }
                } else if (i == 9 || i == 10 || i == 11) {
                    EditText et = values.get(i);
                    String abc1 = et.getText().toString().trim();
                    if (abc1 != null && !abc1.isEmpty()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_SUGAR)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_SUGAR))) {
                            et.setError(getString(R.string.sugar_error, AppConstants.MINIMUM_SUGAR, AppConstants.MAXIMUM_SUGAR));
                            focusView = et;
                            cancel = true;
                            break;
                        } else {
                            cancel = false;
                        }
                    } else {
                        cancel = false;
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
                if (mBMI.getText() != null && mBMI.getText().toString().trim().length() != 0 && Double.parseDouble(mBMI.getText().toString().trim()) < 18.5) {
                    alertMsg = alertMsg + getResources().getString(R.string.weight_loss_alert_msg) + "\n";
                } else if (mBMI.getText() != null && mBMI.getText().toString().trim().length() != 0 && Double.parseDouble(mBMI.getText().toString().trim()) > 25.0) {
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

                if ((mSugarRandom.getText() != null && mSugarRandom.getText().toString().trim().length() != 0 && (Integer.parseInt(mSugarRandom.getText().toString().trim()) < 80 || Integer.parseInt(mSugarRandom.getText().toString().trim()) > 130))) {
                    alertMsg = alertMsg + getResources().getString(R.string.vital_alert_sugar_random_button) + "\n";
                }

                if ((mSugarFasting.getText() != null && mSugarFasting.getText().toString().trim().length() != 0 && (Integer.parseInt(mSugarFasting.getText().toString().trim()) < 70 || Integer.parseInt(mSugarFasting.getText().toString().trim()) > 100))) {
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

    private void endVisit(String visitUuid, String patientUuid, String endTime) {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, endTime);
            //Toast.makeText(this, R.string.text_advice_created, Toast.LENGTH_SHORT).show();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        new SyncUtils().syncForeground(""); //Sync function will work in foreground of app and
        sessionManager.removeVisitSummary(patientUuid, visitUuid);
        /*setResult(RESULT_OK);
        finish();*/
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
            /*String result = "";
            double fTemp = Double.parseDouble(temperature);
            double cTemp = ((fTemp - 32) * 5 / 9);
            Log.i(TAG, "uploadTemperatureInC: " + cTemp);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            DecimalFormat dtime = new DecimalFormat("#.##",symbols);
            cTemp = Double.parseDouble(dtime.format(cTemp));
            result = String.valueOf(cTemp);

            return result;*/
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
        /*String result = "";
        double a = Double.parseDouble(String.valueOf(temperature));
        Double b = (a * 9 / 5) + 32;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        DecimalFormat dtime = new DecimalFormat("#.##",symbols);
        b = Double.parseDouble(dtime.format(b));
        result = String.valueOf(b);
        return result;*/

    }

    @Override
    public void onBackPressed() {
    }

    public void setVitalInfoForHemoAndSugar(String[] data, TextView textView, String selectedValue) {
        final Dialog dialog = new Dialog(VitalsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.vitalnuberpickerdialog);

        NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(data.length - 1);
        numberPicker.setDisplayedValues(data);
        if (selectedValue != null && !selectedValue.isEmpty() && selectedValue.length() > 0) {
            /*int val=Integer.parseInt(selectedValue);
            if(data.length>20){
                numberPicker.setValue(val-10);
            }else{
                numberPicker.setValue(val-1);
            }*/
        }

        TextView okButton = (TextView) dialog.findViewById(R.id.choose_number_btn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = numberPicker.getValue();
                String val = data[index];
                textView.setText(val);
                /*if(data.length>20){
                    textView.setText((numberPicker.getValue()+10)+"");
                }else{
                    textView.setText((numberPicker.getValue()+1)+"");
                }*/
                dialog.dismiss();
            }
        });

        TextView closeButton = (TextView) dialog.findViewById(R.id.close_number_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
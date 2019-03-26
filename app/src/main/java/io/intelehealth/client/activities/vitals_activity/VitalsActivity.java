package io.intelehealth.client.activities.vitals_activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.complaint_node_activity.ComplaintNodeActivity;
import io.intelehealth.client.activities.visit_summary_activity.VisitSummaryActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.TableExam;
import io.intelehealth.client.utilities.ConceptId;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.utilities.Logger;

/**
 * Records the patient vitals in the {@link TableExam} container.
 */
public class VitalsActivity extends AppCompatActivity {


    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mtempfaren, mSpo2, mBMI, mResp;
    Long obsID;
    final String TAG = VitalsActivity.class.getSimpleName();
    int flag_height = 0, flag_weight = 0;

    Integer patientID;
    String visitID;
    String state;
    String patientName;
    String intentTag;
    String heightvalue;
    String weightvalue;
    //    Respiratory added by mahiti dev team
    String respiratory;

    String maxh = "272";
    String maxw = "150";
    String maxbpsys = "300";
    String minbpsys = "50";
    String maxbpdys = "150";
    String minbpdys = "30";
    String maxpulse = "200";
    String minpulse = "30";
    String maxte = "43";
    String minte = "25";

    String mintemf = "80";
    String maxtemf = "120";

    String maxspo2 = "100";
    String minspo2 = "1";
    String maxresp = "80";
    String minresp = "10";


    ArrayList<String> physicalExams;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    TableExam results = new TableExam();

    String mFileName = "config.json";

    boolean hasLicense = false;
    SharedPreferences.Editor e;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        //For Testing
        //patientID = Long.valueOf("1");


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getIntExtra("patientID", -1);
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            //    physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along


//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitals);
        setTitle(R.string.title_activity_vitals);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.contains("licensekey"))
            hasLicense = true;
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
//            #633 #632
//            Issue #632 added try catch exception handling
            if (hasLicense) {
                obj = new JSONObject(HelperMethods.readFileRoot(mFileName, this)); //Load the config file
            }else {
                obj = new JSONObject(String.valueOf(HelperMethods.encodeJSON(this, mFileName)));
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

                    mTemperature = (EditText) findViewById(R.id.table_temp);
                    findViewById(R.id.table_temp_faren).setVisibility(View.GONE);

                } else if (obj.getBoolean("mFahrenheit")) {

                    mTemperature = (EditText) findViewById(R.id.table_temp_faren);
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
            e.printStackTrace();
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
                if (s.toString().trim().length() > 0) {
                    mBMI.getText().clear();
                    flag_height = 1;
                    heightvalue = mHeight.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(maxh)) {
                        mHeight.setError(getString(R.string.height_error, maxh));
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
            }
        });

        mWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mBMI.getText().clear();
                    flag_weight = 1;
                    weightvalue = mWeight.getText().toString();
                    if (Double.valueOf(s.toString()) > Double.valueOf(maxw)) {
                        mWeight.setError(getString(R.string.weight_error, maxw));
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
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(maxspo2) ||
                            Double.valueOf(s.toString()) < Double.valueOf(minspo2)) {
                        mSpo2.setError(getString(R.string.spo2_error, minspo2, maxspo2));
                    } else {
                        mSpo2.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mTemperature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    JSONObject obj = null;
                    if (hasLicense)
                        obj = new JSONObject(HelperMethods.readFileRoot(mFileName, VitalsActivity.this)); //Load the config file
                    else
                        obj = new JSONObject(String.valueOf(HelperMethods.encodeJSON(VitalsActivity.this, mFileName)));

                    if (obj.getBoolean("mCelsius")) {
                        if (s.toString().trim().length() > 0) {
                            if (Double.valueOf(s.toString()) > Double.valueOf(maxte) ||
                                    Double.valueOf(s.toString()) < Double.valueOf(minte)) {
                                mTemperature.setError(getString(R.string.temp_error, minte, maxte));
                            } else {
                                mTemperature.setError(null);
                            }

                        }
                    } else if (obj.getBoolean("mFahrenheit")) {
                        if (s.toString().trim().length() > 0) {
                            if (Double.valueOf(s.toString()) > Double.valueOf(maxtemf) ||
                                    Double.valueOf(s.toString()) < Double.valueOf(mintemf)) {
                                mTemperature.setError(getString(R.string.temp_error, mintemf, maxtemf));
                            } else {
                                mTemperature.setError(null);
                            }
                        }

                    }
                } catch (
                        JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mResp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(maxresp) ||
                            Double.valueOf(s.toString()) < Double.valueOf(minresp)) {
                        mResp.setError(getString(R.string.temp_error, minresp, maxresp));
                    } else {
                        mResp.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mPulse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(maxpulse) ||
                            Double.valueOf(s.toString()) < Double.valueOf(minpulse)) {
                        mPulse.setError(getString(R.string.pulse_error, minpulse, maxpulse));
                    } else {
                        mPulse.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBpSys.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(maxbpsys) ||
                            Double.valueOf(s.toString()) < Double.valueOf(minbpsys)) {
                        mBpSys.setError(getString(R.string.bpsys_error, minbpsys, maxbpsys));
                    } else {
                        mBpSys.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBpDia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    if (Double.valueOf(s.toString()) > Double.valueOf(maxbpdys) ||
                            Double.valueOf(s.toString()) < Double.valueOf(minbpdys)) {
                        mBpDia.setError(getString(R.string.bpdia_error, minbpdys, maxbpdys));
                    } else {
                        mBpDia.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        if (flag_height == 1 && flag_weight == 1) {
            mBMI.getText().clear();
            double numerator = Double.parseDouble(weightvalue) * 10000;
            double denominator = (Double.parseDouble(heightvalue)) * (Double.parseDouble(heightvalue));
            double bmi_value = numerator / denominator;
            DecimalFormat df = new DecimalFormat("0.00");
            mBMI.setText(df.format(bmi_value));
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));
        } else if (flag_height == 0 || flag_weight == 0) {
            // do nothing
            mBMI.getText().clear();
        }
    }


    public void loadPrevious() {
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();
        String[] columns = {"value", " concept_id"};
        String orderBy = "visit_id";
        String visitSelection = "patient_id = ? AND visit_id = ?";
        String[] visitArgs = {String.valueOf(patientID), visitID};
        Cursor visitCursor = db.query("obs", columns, visitSelection, visitArgs, null, null, orderBy);
        if (visitCursor.moveToFirst()) {
            do {
                int dbConceptID = visitCursor.getInt(visitCursor.getColumnIndex("concept_id"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
//                issue #641
                try {
                    parseData(dbConceptID, dbValue);
                }catch(Exception e){
                    Logger.logE("VitalActivity","#641",e);
                }
                } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(int concept_id, String value) {
        switch (concept_id) {
            case ConceptId.HEIGHT: //Height
                mHeight.setText(value);
                break;
            case ConceptId.WEIGHT: //Weight
                mWeight.setText(value);
                break;
            case ConceptId.PULSE: //Pulse
                mPulse.setText(value);
                break;
            case ConceptId.SYSTOLIC_BP: //Systolic BP
                mBpSys.setText(value);
                break;
            case ConceptId.DIASTOLIC_BP: //Diastolic BP
                mBpDia.setText(value);
                break;
            case ConceptId.TEMPERATURE: //Temperature
                mTemperature.setText(value);
                break;
            //    Respiratory added by mahiti dev team
            case ConceptId.RESPIRATORY: //Respiratory
                mResp.setText(value);
                break;
            case ConceptId.SPO2: //SpO2
                mSpo2.setText(value);
                break;
            default:
                break;

        }
    }

    public void validateTable() {
        boolean cancel = false;
        View focusView = null;

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

        // Check to see if values were inputted.
        for (int i = 0; i < values.size(); i++) {
            if (i == 0) {
                EditText et = values.get(i);
                String abc = et.getText().toString().trim();
                if (abc != null && !abc.isEmpty()) {
                    if (Double.parseDouble(abc) > Double.parseDouble(maxh)) {
                        et.setError(getString(R.string.height_error, maxh));
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
                    if (Double.parseDouble(abc1) > Double.parseDouble(maxw)) {
                        et.setError(getString(R.string.weight_error, maxw));
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
                    if ((Double.parseDouble(abc2) > Double.parseDouble(maxpulse)) ||
                            (Double.parseDouble(abc2) < Double.parseDouble(minpulse))) {
                        et.setError(getString(R.string.pulse_error, minpulse, maxpulse));
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
                    if ((Double.parseDouble(abc1) > Double.parseDouble(maxbpsys)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(minbpsys))) {
                        et.setError(getString(R.string.bpsys_error, minbpsys, maxbpsys));
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
                    if ((Double.parseDouble(abc1) > Double.parseDouble(maxbpdys)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(minbpdys))) {
                        et.setError(getString(R.string.bpdia_error, minbpdys, maxbpdys));
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
                    try {
                        JSONObject obj = null;
                        if (hasLicense)
                            obj = new JSONObject(HelperMethods.readFileRoot(mFileName, this)); //Load the config file
                        else
                            obj = new JSONObject(String.valueOf(HelperMethods.encodeJSON(this, mFileName)));

                        if (obj.getBoolean("mCelsius")) {
                            if ((Double.parseDouble(abc1) > Double.parseDouble(maxte)) ||
                                    (Double.parseDouble(abc1) < Double.parseDouble(minte))) {
                                et.setError(getString(R.string.temp_error, minte, maxte));
                                focusView = et;
                                cancel = true;
                                break;
                            } else {
                                cancel = false;
                            }
                        } else if (obj.getBoolean("mFahrenheit")) {
                            if ((Double.parseDouble(abc1) > Double.parseDouble(maxtemf)) ||
                                    (Double.parseDouble(abc1) < Double.parseDouble(mintemf))) {
                                et.setError(getString(R.string.temp_error, mintemf, maxtemf));
                                focusView = et;
                                cancel = true;
                                break;
                            } else {
                                cancel = false;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


//       }
                } else {
                    cancel = false;
                }
            } else if (i == 6) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(maxresp)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(minresp))) {
                        et.setError(getString(R.string.resp_error, minresp, maxresp));
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
            }else {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (abc1 != null && !abc1.isEmpty() && (!abc1.equals("0.0"))) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(maxspo2)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(minspo2))) {
                        et.setError(getString(R.string.spo2_error, minspo2, maxspo2));
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
                if (mHeight.getText() != null) {
                    results.setHeight((mHeight.getText().toString()));
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
                    results.setTemperature((mTemperature.getText().toString()));
                }
                if (mResp.getText() != null) {
                    results.setResp((mResp.getText().toString()));
                }
                if (mSpo2.getText() != null) {
                    results.setSpo2((mSpo2.getText().toString()));
                }


            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(R.id.cl_table), "Error: non-decimal number entered.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

//
        }

        if (intentTag != null && intentTag.equals("edit")) {
            updateDatabase(results.getHeight(), ConceptId.HEIGHT);
            updateDatabase(results.getWeight(), ConceptId.WEIGHT);
            updateDatabase(results.getPulse(), ConceptId.PULSE);
            updateDatabase(results.getBpsys(), ConceptId.SYSTOLIC_BP);
            updateDatabase(results.getBpdia(), ConceptId.DIASTOLIC_BP);
            updateDatabase(results.getTemperature(), ConceptId.TEMPERATURE);
            //    Respiratory added by mahiti dev team
            updateDatabase(results.getResp(), ConceptId.RESPIRATORY);
            updateDatabase(results.getSpo2(), ConceptId.SPO2);
            Intent intent = new Intent(VitalsActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            startActivity(intent);
        } else {

            insertDb(results.getHeight(), ConceptId.HEIGHT);
            insertDb(results.getWeight(), ConceptId.WEIGHT);
            insertDb(results.getPulse(), ConceptId.PULSE);
            insertDb(results.getBpsys(), ConceptId.SYSTOLIC_BP);
            insertDb(results.getBpdia(), ConceptId.DIASTOLIC_BP);
            insertDb(results.getTemperature(), ConceptId.TEMPERATURE);
            //    Respiratory added by mahiti dev team
            insertDb(results.getResp(), ConceptId.RESPIRATORY);
            insertDb(results.getSpo2(), ConceptId.SPO2);
            Intent intent = new Intent(VitalsActivity.this, ComplaintNodeActivity.class);

            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }
    }


    private long insertDb(String objValue, int CONCEPT_ID) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String CREATOR_ID = prefs.getString("creatorid", null);

        String value = objValue;

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

    private void updateDatabase(String objValue, int CONCEPT_ID) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("value", objValue);

        String selection = "patient_id = ? AND visit_id = ? AND concept_id = ?";
        String[] args = {String.valueOf(patientID), visitID, String.valueOf(CONCEPT_ID)};

        int update = localdb.update(
                "obs",
                contentValues,
                selection,
                args
        );

        //If no value is not found, then update fails so insert instead.
        if (update == 0) {
            insertDb(objValue, CONCEPT_ID);
        }

    }

    @Override
    public void onBackPressed() {
    }

}

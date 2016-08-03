package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import edu.jhu.bme.cbid.healthassistantsclient.objects.TableExam;

public class TableExamActivity extends AppCompatActivity {

    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mSpo2, mBMI;
    Long obsID;
    final String LOG_TAG = "TableExamActivity";

    Long patientID = null;
    String patientStatus;
    String intentTag;

    private ArrayList<String> physExams;

    // EditText bmi = (EditText) findViewById(R.id.table_bmi);
    // bmi.setFocusable(false);
    // TODO: intent passes along patient id, gender
    // TODO: autocalculation of bmi

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //For Testing
//        patientID = Long.valueOf("1");

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getLongExtra("patientID", 1);
            patientStatus = intent.getStringExtra("status");
            intentTag = intent.getStringExtra("tag");
            Log.v(LOG_TAG, "Patient ID: " + patientID);
            Log.v(LOG_TAG, "Status: " + patientStatus);
            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_exam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mHeight = (EditText) findViewById(R.id.table_height);
        mWeight = (EditText) findViewById(R.id.table_weight);
        mPulse = (EditText) findViewById(R.id.table_pulse);
        mBpSys = (EditText) findViewById(R.id.table_bpsys);
        mBpDia = (EditText) findViewById(R.id.table_bpdia);
        mTemperature = (EditText) findViewById(R.id.table_temp);
        mSpo2 = (EditText) findViewById(R.id.table_spo2);

        mBMI = (EditText) findViewById(R.id.table_bmi);
        mBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double numerator = Double.parseDouble(mWeight.getText().toString()) * 10000;
                double denominator = (Double.parseDouble(mHeight.getText().toString())) * (Double.parseDouble(mHeight.getText().toString()));
                double bmi_value = numerator / denominator;
                mBMI.setText(String.format(Locale.ENGLISH, "%,2f", bmi_value));
                Log.d("BMI", String.valueOf(bmi_value));
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTable();
            }
        });
    }

    public void validateTable() {
        // Reset errors.


        // .getText().toString()
        boolean cancel = false;
        View focusView = null;

        // TODO: bmi, patient id should go here
        // Store values at the time of the login attempt.
        ArrayList<EditText> values = new ArrayList<EditText>();
        values.add(mHeight);
        values.add(mWeight);
        values.add(mPulse);
        values.add(mBpSys);
        values.add(mBpDia);
        values.add(mTemperature);
        values.add(mSpo2);

        // Check for a valid values.
        for (int i = 0; i < values.size(); i++) {
            EditText et = values.get(i);

            if (TextUtils.isEmpty(et.getText().toString()) && et.getTag() == null) {
                et.setError(getString(R.string.error_field_required));
                focusView = et;
                cancel = true;
                break;
            } else {
                cancel = false;
            }
        }

        if (cancel) {
            // There was an error - focus the first form field with an error.
            focusView.requestFocus();
            return;
        } else {
            TableExam results = new TableExam();
            try {
                results.setHeight(Double.parseDouble(mHeight.getText().toString()));
                results.setWeight(Double.parseDouble(mWeight.getText().toString()));
                results.setPulse(Double.parseDouble(mPulse.getText().toString()));
                results.setBpsys(Double.parseDouble(mBpSys.getText().toString()));
                results.setBpdia(Double.parseDouble(mBpDia.getText().toString()));
                results.setTemperature(Double.parseDouble(mTemperature.getText().toString()));
                results.setSpo2(Double.parseDouble(mSpo2.getText().toString()));
            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(R.id.cl_table), "Error: non-decimal number entered.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

            insertDb(results.getHeight(), 5090);
            insertDb(results.getWeight(), 5089);
            insertDb(results.getPulse(), 5087);
            insertDb(results.getBpsys(), 5085);
            insertDb(results.getBpdia(), 5086);
            insertDb(results.getTemperature(), 163202);
            insertDb(results.getSpo2(), 5092);
        }

        if (intentTag.equals("edit")){
            Intent intent = new Intent(TableExamActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("status", patientStatus);
            intent.putExtra("tag", intentTag);
            startActivity(intent);
        } else {

            Intent intent = new Intent(TableExamActivity.this, PhysicalExamActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("status", patientStatus);
            intent.putExtra("tag", intentTag);
            intent.putStringArrayListExtra("exams", physExams);
            startActivity(intent);
        }
    }


    private long insertDb(double objValue, int CONCEPT_ID) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int VISIT_ID = 100; // TODO: Connect the proper VISIT_ID
        final int CREATOR_ID = 42; // TODO: Connect the proper CREATOR_ID

        String value = String.valueOf(objValue);

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", VISIT_ID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

}

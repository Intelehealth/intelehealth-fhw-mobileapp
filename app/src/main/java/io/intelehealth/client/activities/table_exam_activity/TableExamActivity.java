package io.intelehealth.client.activities.table_exam_activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import io.intelehealth.client.activities.physical_exam_activity.PhysicalExamActivity;
import io.intelehealth.client.R;
import io.intelehealth.client.activities.visit_summary_activity.VisitSummaryActivity;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.TableExam;

public class TableExamActivity extends AppCompatActivity {

    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mSpo2, mBMI;
    Long obsID;
    final String LOG_TAG = "TableExamActivity";

    String patientID = "1";
    String visitID;
    String state;
    String patientName;
    String intentTag;

    ArrayList<String> physicalExams;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

    TableExam results = new TableExam();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //For Testing
        //patientID = Long.valueOf("1");

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            physicalExams = intent.getStringArrayListExtra("exams"); //Pass it along
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Visit ID: " + visitID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }




        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_exam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        setTitle(patientName + ": " + getTitle());

        mHeight = (EditText) findViewById(R.id.table_height);
        mWeight = (EditText) findViewById(R.id.table_weight);
        mPulse = (EditText) findViewById(R.id.table_pulse);
        mBpSys = (EditText) findViewById(R.id.table_bpsys);
        mBpDia = (EditText) findViewById(R.id.table_bpdia);
        mTemperature = (EditText) findViewById(R.id.table_temp);
        mSpo2 = (EditText) findViewById(R.id.table_spo2);

        mBMI = (EditText) findViewById(R.id.table_bmi);

        if (intentTag != null && intentTag.equals("edit")) {
            loadPrevious();
        }

        //BMI calculation is done in metric units
        mBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String heightValue = mHeight.getText().toString();
                String weightValue = mWeight.getText().toString();

                if (heightValue.matches("") || weightValue.matches("")) {
                    String message = "Please enter height and weight first.";
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TableExamActivity.this);
                    alertDialogBuilder.setMessage(message);
                    alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {

                    double numerator = Double.parseDouble(mWeight.getText().toString()) * 10000;
                    double denominator = (Double.parseDouble(mHeight.getText().toString())) * (Double.parseDouble(mHeight.getText().toString()));
                    double bmi_value = numerator / denominator;
                    mBMI.setText(String.format(Locale.ENGLISH, "%,2f", bmi_value));
                    //Log.d("BMI", String.valueOf(bmi_value));
                }



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

    public void loadPrevious(){
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();
        String[] columns = {"value", " concept_id"};
        String orderBy = "visit_id";
        String visitSelection = "patient_id = ? AND visit_id = ?";
        String[] visitArgs = {patientID, visitID};
        Cursor visitCursor = db.query("obs", columns, visitSelection, visitArgs, null, null, orderBy);
        if (visitCursor.moveToFirst()) {
            do {
                int dbConceptID = visitCursor.getInt(visitCursor.getColumnIndex("concept_id"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(int concept_id, String value) {
        switch (concept_id) {
            case 5090: //Height
                mHeight.setText(value);
                break;
            case 5089: //Weight
                mWeight.setText(value);
                break;
            case 5087: //Pulse
                mPulse.setText(value);
                break;
            case 5085: //Systolic BP
                mBpSys.setText(value);
                break;
            case 5086: //Diastolic BP
                mBpDia.setText(value);
                break;
            case 163202: //Temperature
                mTemperature.setText(value);
                break;
            case 5092: //SpO2
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
        values.add(mSpo2);

        // Check to see if values were inputted.
        for (int i = 0; i < values.size(); i++) {
            EditText et = values.get(i);

            if (TextUtils.isEmpty(et.getText().toString())) {
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


        }

        if (intentTag != null && intentTag.equals("edit")) {
            updateDatabase(results.getHeight(), 5090);
            updateDatabase(results.getWeight(), 5089);
            updateDatabase(results.getPulse(), 5087);
            updateDatabase(results.getBpsys(), 5085);
            updateDatabase(results.getBpdia(), 5086);
            updateDatabase(results.getTemperature(), 163202);
            updateDatabase(results.getSpo2(), 5092);
            Intent intent = new Intent(TableExamActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        } else {
            insertDb(results.getHeight(), 5090);
            insertDb(results.getWeight(), 5089);
            insertDb(results.getPulse(), 5087);
            insertDb(results.getBpsys(), 5085);
            insertDb(results.getBpdia(), 5086);
            insertDb(results.getTemperature(), 163202);
            insertDb(results.getSpo2(), 5092);
            Intent intent = new Intent(TableExamActivity.this, PhysicalExamActivity.class);
            intent.putExtra("patientID", patientID);
            intent.putExtra("visitID", visitID);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("tag", intentTag);
            intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }
    }


    private long insertDb(double objValue, int CONCEPT_ID) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String CREATOR_ID = prefs.getString("creatorid", null);

        String value = String.valueOf(objValue);

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("creator", CREATOR_ID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

    private void updateDatabase(double objValue, int CONCEPT_ID) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("value", objValue);

        String selection = "patient_id = ? AND visit_id = ? AND concept_id = ?";
        String[] args = {patientID, visitID, String.valueOf(CONCEPT_ID)};

        localdb.update(
                "visit",
                contentValues,
                selection,
                args
        );

    }

}

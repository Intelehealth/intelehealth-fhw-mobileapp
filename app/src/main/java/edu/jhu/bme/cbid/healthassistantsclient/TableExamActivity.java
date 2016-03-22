package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import edu.jhu.bme.cbid.healthassistantsclient.objects.TableExam;

public class TableExamActivity extends AppCompatActivity {

    EditText mHeight, mWeight, mPulse, mBpSys, mBpDia, mTemperature, mSpo2;

    private InsertTableExamDb mTask = null;


    // EditText bmi = (EditText) findViewById(R.id.table_bmi); // TODO: autocalculation - do we need this here?
    // bmi.setFocusable(false);
    // TODO: intent passes along patient id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_exam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeight = (EditText) findViewById(R.id.table_height);
        mWeight = (EditText) findViewById(R.id.table_weight);
        mPulse = (EditText) findViewById(R.id.table_pulse);
        mBpSys = (EditText) findViewById(R.id.table_bpsys);
        mBpDia = (EditText) findViewById(R.id.table_bpdia);
        mTemperature = (EditText) findViewById(R.id.table_temp);
        mSpo2 = (EditText) findViewById(R.id.table_spo2);

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                validateTable();
            }
        });
    }

    public void validateTable() {
        if (mTask != null) {
            return;
        }

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
        for(int i = 0; i < values.size(); i++) {
            EditText et = values.get(i);

            if (TextUtils.isEmpty(et.getText().toString())) {
                et.setError(getString(R.string.error_field_required));
                focusView = et;
                cancel = true;
                break;
            }
        }


        if (cancel) {
            // There was an error - focus the first form field with an error.
            focusView.requestFocus();
        } else {
            TableExam results = new TableExam();
            results.setHeight(Double.parseDouble(mHeight.getText().toString()));
            results.setWeight(Double.parseDouble(mWeight.getText().toString()));
            results.setPulse(Double.parseDouble(mPulse.getText().toString()));
            results.setBpsys(Double.parseDouble(mBpSys.getText().toString()));
            results.setBpdia(Double.parseDouble(mBpDia.getText().toString()));
            results.setTemperature(Double.parseDouble(mTemperature.getText().toString()));
            results.setSpo2(Double.parseDouble(mSpo2.getText().toString()));

            mTask = new InsertTableExamDb(results);
            mTask.execute((Void) null);
        }


    }

    public class InsertTableExamDb extends AsyncTask<Void, Void, Boolean>
            implements DialogInterface.OnCancelListener {

        int id;
        double height, weight, pulse, bpsys, bpdia, temperature, spo2;
        private ProgressDialog dialog;


        InsertTableExamDb(TableExam result) {
            id = result.getPatientId();
            height = result.getHeight();
            weight = result.getWeight();
            pulse = result.getPulse();
            bpsys = result.getBpsys();
            bpdia = result.getBpdia();
            temperature = result.getTemperature();
            spo2 = result.getSpo2();
        }

        protected void onPreExecute()
        {
            dialog = ProgressDialog
                    .show(TableExamActivity.this, "", "Loading. Please wait...", true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // TODO: where do we insert this? and how?
            return true;
        }

        protected void onPostExecute(Void unused)
        {
            dialog.dismiss();
        }

        public void onCancel(DialogInterface dialog)
        {
            cancel(true);
            dialog.dismiss();
        }
    }

}

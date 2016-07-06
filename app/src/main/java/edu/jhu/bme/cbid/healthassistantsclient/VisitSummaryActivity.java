package edu.jhu.bme.cbid.healthassistantsclient;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Obs;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

public class VisitSummaryActivity extends AppCompatActivity {

    String LOG_TAG = "Patient Summary Activity";

    Long patientID;
    Patient patient = new Patient();
    Obs complaint = new Obs();
    Obs famHistory = new Obs();
    Obs patHistory = new Obs();
    Obs physFindings = new Obs();
    Obs height = new Obs();
    Obs weight = new Obs();
    Obs pulse = new Obs();
    Obs bpSys = new Obs();
    Obs bpDias = new Obs();
    Obs temperature = new Obs();
    Obs spO2 = new Obs();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_visit_summary, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Bundle bundle = getIntent().getExtras();
        patientID = bundle.getLong("patientID", 1);
        Log.d(LOG_TAG, String.valueOf(patientID));

        //patientID = Long.valueOf("1");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }
        });

        queryData(String.valueOf(patientID));

        TextView heightView = (TextView) findViewById(R.id.textview_height_value);
        TextView weightView = (TextView) findViewById(R.id.textview_weight_value);
        TextView pulseView = (TextView) findViewById(R.id.textview_pulse_value);
        TextView bpView = (TextView) findViewById(R.id.textview_bp_value);
        TextView tempView = (TextView) findViewById(R.id.textview_temp_value);
        TextView spO2View = (TextView) findViewById(R.id.textview_pulseox_value);

        TextView complaintView = (TextView) findViewById(R.id.textview_content_complaint);
        TextView famHistView = (TextView) findViewById(R.id.textview_content_famhist);
        TextView patHistView = (TextView) findViewById(R.id.textview_content_pathist);
        TextView physFindingsView = (TextView) findViewById(R.id.textview_content_physexam);

        heightView.setText(height.getValue());
        weightView.setText(weight.getValue());
        pulseView.setText(pulse.getValue());

        //String bpSys = bpSys.getVa

        bpView.setText(bpSys.getValue() + "/"  + bpDias.getValue());

        tempView.setText(temperature.getValue());
        spO2View.setText(spO2.getValue());
        complaintView.setText(complaint.getValue());
        famHistView.setText(famHistory.getValue());
        patHistView.setText(patHistory.getValue());
        physFindingsView.setText(physFindings.getValue());

    }

    public void queryData(String dataString) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = "_id MATCH ?";
        String[] args = {dataString};

        ArrayList<String> uploadedFields = new ArrayList<>();

        String table = "patient";
        String[] columnsToReturn = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "phone_number", "patient_identifier1", "patient_identifier2"};
        final Cursor idCursor = db.query(table, columnsToReturn, selection, args, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setFirstName(idCursor.getString(idCursor.getColumnIndex("first_name")));
                patient.setMiddleName(idCursor.getString(idCursor.getColumnIndex("middle_name")));
                patient.setLastName(idCursor.getString(idCursor.getColumnIndex("last_name")));
                patient.setDateOfBirth(idCursor.getString(idCursor.getColumnIndex("date_of_birth")));
                patient.setAddress1(idCursor.getString(idCursor.getColumnIndex("address1")));
                patient.setAddress2(idCursor.getString(idCursor.getColumnIndex("address2")));
                patient.setCityVillage(idCursor.getString(idCursor.getColumnIndex("city_village")));
                patient.setStateProvince(idCursor.getString(idCursor.getColumnIndex("state_province")));
                patient.setPostalCode(idCursor.getString(idCursor.getColumnIndex("postal_code")));
                patient.setPhoneNumber(idCursor.getString(idCursor.getColumnIndex("phone_number")));
                patient.setPatientIdentifier1(idCursor.getString(idCursor.getColumnIndex("patient_identifier1")));
                patient.setPatientIdentifier2(idCursor.getString(idCursor.getColumnIndex("patient_identifier2")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        selection = "patient_id = ?";

        String[] columns = {"value", " concept_id"};
        String orderBy = "concept_id";
        Cursor visitCursor = db.query("obs", columns, selection, args, null, null, orderBy);

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
            case 163187: //Medical History
                patHistory.setValue(value);
                break;
            case 163188: //Family History
                famHistory.setValue(value);
                break;
            case 163186: //Current Complaint
                complaint.setValue(value);
                break;
            case 163189: //Physical Examination
                physFindings.setValue(value);
                break;
            case 5090: //Height
                height.setValue(value);
                break;
            case 5089: //Weight
                weight.setValue(value);
                break;
            case 5087: //Pulse
                pulse.setValue(value);
                break;
            case 5085: //Systolic BP
                bpSys.setValue(value);
                break;
            case 5086: //Diastolic BP
                bpDias.setValue(value);
                break;
            case 163202: //Temperature
                temperature.setValue(value);
                break;
            case 5092: //SpO2
                spO2.setValue(value);
                break;
            default:
                break;

        }
    }

}

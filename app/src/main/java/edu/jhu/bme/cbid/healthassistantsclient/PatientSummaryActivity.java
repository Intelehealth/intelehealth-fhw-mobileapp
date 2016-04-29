package edu.jhu.bme.cbid.healthassistantsclient;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class PatientSummaryActivity extends AppCompatActivity {

    String LOG_TAG = "Patient Summary Activity";

    Long patientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Bundle bundle = getIntent().getExtras();
//        patientID = bundle.getLong("patientID", 0);

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

        serialize(String.valueOf(1));

    }

    public void serialize(String dataString) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = "_id = ?";
        String[] args = {dataString};

        ArrayList<String> uploadedFields = new ArrayList<>();

                String[] columnsToReturn = {
                "first_name",
                "middle_name",
                "last_name",
                "date_of_birth",
                "phone_number",
                "address1",
                "address2",
                "city_village",
                "state_province",
                "postal_code",
                "country",
                "gender",
                "patient_identifier1",
                "patient_identifier2",
        };
        Cursor idCursor = db.query("patient", columnsToReturn, selection, args, null, null, null);

        idCursor.moveToFirst();

        if (idCursor.moveToFirst()){
            do{
                String data = idCursor.getString(idCursor.getColumnIndex("first_name"));
                Log.d(LOG_TAG, data);
                uploadedFields.add(data);
            }while(idCursor.moveToNext());
        }
        idCursor.close();


        selection = "patient_id = ?";

        String[] columns = {"value", " concept_id"};
        String orderBy = "concept_id";
        Cursor visitCursor = db.query("obs", columns, selection, args, null, null, orderBy);

        if (visitCursor.moveToFirst()){
            do{
                String data = visitCursor.getString(visitCursor.getColumnIndex("value"));
                Log.d(LOG_TAG, data);
                uploadedFields.add(data);
            }while(visitCursor.moveToNext());
        }
        visitCursor.close();

        Log.d(LOG_TAG, uploadedFields.toString());

        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(uploadedFields);

        Log.d(LOG_TAG, json);


    }
}

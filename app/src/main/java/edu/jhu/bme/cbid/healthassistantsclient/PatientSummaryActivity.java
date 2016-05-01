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

        Bundle bundle = getIntent().getExtras();
        patientID = bundle.getLong("patientID", 0);

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

        serialize(String.valueOf(2));

    }

    public void serialize(String dataString) {
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

        //TODO: query and grab each column of the cursor, and create a patient obj and use that to display the card

        //TODO: the different queries should become different container objects

        //TODO: modify the cards so that they adapt to the objects



//        idCursor.moveToFirst();
//        Log.d(LOG_TAG, String.valueOf(idCursor.moveToFirst()) );
//        Log.d(LOG_TAG, String.valueOf(idCursor.getColumnIndex("first_name")));
//        Log.d(LOG_TAG, data1);

        if (idCursor.moveToFirst()){
            do{
        String data1 = idCursor.getString(idCursor.getColumnIndex("first_name"));
                Log.d(LOG_TAG, data1);
                uploadedFields.add(data1);
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
                //Log.d(LOG_TAG, data);
                uploadedFields.add(data);
            }while(visitCursor.moveToNext());
        }
        visitCursor.close();

        //Log.d(LOG_TAG, uploadedFields.toString());

        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(uploadedFields);

        //Log.d(LOG_TAG, json);


    }
}

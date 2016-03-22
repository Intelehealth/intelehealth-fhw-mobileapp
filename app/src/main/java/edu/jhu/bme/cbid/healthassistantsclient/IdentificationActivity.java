package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

public class IdentificationActivity extends AppCompatActivity {

    //Demographic acquisition screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        Button identificationButton = (Button) findViewById(R.id.identificationSubmitButton);
        identificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitIdentifiers();
            }
        });

    }

    public void submitIdentifiers() {

        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localDb = mDbHelper.getWritableDatabase();
        ContentValues patientEntries = new ContentValues();

        ViewGroup identifiersLayout = (ViewGroup) findViewById(R.id.identificationTable);
        for (int i = 0; i < identifiersLayout.getChildCount(); i++) {
            View view = identifiersLayout.getChildAt(i);
            if (view instanceof EditText) {
                String storageColumn = (view).getTag().toString();
                String storageValue = ((EditText) view).getText().toString();

                patientEntries.put(storageColumn, storageValue);

                //TODO: Check if DB statements are correct
            }
        }

        CheckBox maleCheckBox = (CheckBox) findViewById(R.id.maleCheckBox);
        CheckBox femaleCheckBox = (CheckBox) findViewById(R.id.femaleCheckBox);

        if (maleCheckBox.isChecked()) {

            String storageColumn = maleCheckBox.getTag().toString();
            String storageValue = "male";

            patientEntries.put(storageColumn, storageValue);
        } else if (femaleCheckBox.isChecked()) {
            String storageColumn = maleCheckBox.getTag().toString();
            String storageValue = "female";

            patientEntries.put(storageColumn, storageValue);
        }

        long newRowID; //TODO: Can we make this the local identifier/registration number?
        newRowID = localDb.insert(
                "patient",
                "null",
                patientEntries);

        // DEBUG ONLY
        Log.d("Patient ID Row", debug("" + newRowID));
        Intent intent = new Intent(this, TableExamActivity.class);
        startActivity(intent);


        //TODO: upload identifiers to OpenMRS using service
    }

    public String debug(String dataString) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        /* String[] columnsToReturn = {
                "_id",
                "openmrs_id",
                "first_name",
                "middle_name",
                "last_name",
                "date_of_birth", // ISO 8601
                "phone_number",
                "address1",
                "address2",
                "city_village",
                "state_province",
                "postal_code",
                "country", // ISO 3166-1 alpha-2
                "gender",
                "patient_identifier1",
                "patient_identifier2",
                "patient_identifier3"
        }; */

        String selection = "_id = ?";
        String[] args = new String[1];
        args[0] = dataString;

        Cursor patientCursor = db.query("patient", null, selection, args, null, null, null);

        Gson gson = new GsonBuilder().serializeNulls().create();
        Patient patient = new Patient();
        patientCursor.moveToFirst();
        patient.setFirstName(patientCursor.getString(2));
        String json = gson.toJson(patient);
        Log.d("IdSerial" + "/Gson", json);

        return json;
    }
}

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

import java.util.Random;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

public class IdentificationActivity extends AppCompatActivity {
    LocalRecordsDatabaseHelper mDbHelper;

    //Demographic acquisition screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());

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
                null,
                patientEntries);

        // DEBUG ONLY
        Log.d("Patient ID Row", String.valueOf(addDummyPatient()));
        Intent intent = new Intent(this, TableExamActivity.class);
        startActivity(intent);


        //TODO: upload identifiers to OpenMRS using service
    }

    /** DEBUG ONLY */
    public long addDummyPatient() {
        SQLiteDatabase localDb = mDbHelper.getWritableDatabase();
        ContentValues patientEntries = new ContentValues();

        Random rand = new Random();

        String[] fnames = {"Molly",
                "Amy",
                "Claire",
                "Emily",
                "Katie",
                "Madeline",
                "Katelyn",
                "Emma",
                "Abigail",
                "Carly",
                "Jenna",
                "Heather",
                "Katherine",
                "Caitlin",
                "Kaitlin",
                "Holly",
                "Allison",
                "Kaitlyn",
                "Hannah",
                "Kathryn"};

        String[] lnames = { "Dawson",
                "Reed",
                "White",
                "Smith",
                "Adam",
                "Wesley",
                "Ambrose",
                "Schnieder",
                "Lamody",
                "Knudtson",
                "Kundert",
                "Kohlstedt",
                "Gilbert",
                "Willson",
                "Williams",
                "Mathews",
                "Young",
                "Hale",
                "Cullen",
                "Steffan",
                "Stevens",
                "StClaire",
                "Bryson",
                "Hammer",
                "Stegar",
                "Miles",
                "Bryant",
                "Davis",
                "Jones",
                "Miller",
                "Moore",
                "Anderson",
                "Martin",
                "Thompson",
                "Lewis",
                "Allen",
                "Wright",
                "Hill",
                "Wood",
                "Baker",
                "Sampson",
                "Nelson",
                "Mason",
                "Parker",
                "Stewart",
                "Murphy",
                "Brooks",
                "Kelly"};

        String fName = fnames[rand.nextInt(fnames.length)];
        String lName = lnames[rand.nextInt(lnames.length)];
        String mName = "Michael";
        String dob = String.format("%d-%02d-%02d", rand.nextInt(60) + 1950,
                rand.nextInt(11) + 1, rand.nextInt(26)+1);
        String gender = "female";

        patientEntries.put("first_name", fName);
        patientEntries.put("middle_name", mName);
        patientEntries.put("last_name", lName);
        patientEntries.put("date_of_birth", dob);
        patientEntries.put("gender", gender);

        long newRowID = localDb.insert(
                "patient",
                null,
                patientEntries);

        return newRowID;
    }
}

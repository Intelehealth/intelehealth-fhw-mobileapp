package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

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


        //TODO: upload identifiers to OpenMRS using service
    }
}

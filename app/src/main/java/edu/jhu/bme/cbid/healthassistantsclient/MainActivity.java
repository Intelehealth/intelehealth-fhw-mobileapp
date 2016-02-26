package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Home screen with the four buttons
    // TODO: write code lol

    // ANDROID UI METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseIO localDb = new DatabaseIO(openOrCreateDatabase("localDb", MODE_PRIVATE, null));

        // The INSERT and UPDATE/DELETE commands require SQL statements to be passed as parameters
        // TODO: Use SQL statements from schema
    }

    public void newPatientWorkflow(View view) {
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
        /*
        Ideally, we would start this class with the fragment to do a new patient
        See below for the reasoning behind this
         */
    }

    public void fuPatientWorkflow(View view) {
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);

        /*
        So this should definitely start a fragment
        Basically, this will allow the HA to search for the patient
        If the patient is not found in the local database, give the HA the option to create a new PT
        Then swap out the "findPatient" fragment with createPatient
        */
    }


    public void diagnosticTests(View view) {
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
        // No fragment, just go straight into the screen
    }


    public void activePatients(View view) {
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
        // This can be a fragment, can be fragment-less as well

    }


    // ANDROID ASYNC TASKS

}

package edu.jhu.bme.cbid.healthassistantsclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Amal Afroz Alam on 3/25/16.
 */

public class IdentificationActivity extends AppCompatActivity {

    EditText mFirstName, mMiddleName, mLastName, mPhoneNum, mDOB, mAge, mAddress1, mAddress2, mCity, mState, mPostal, mCountry, mRelationship, mOccupation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mFirstName = (EditText) findViewById(R.id.identification_first_name);
        mMiddleName = (EditText) findViewById(R.id.identification_middle_name);
        mLastName = (EditText) findViewById(R.id.identification_last_name);
        mPhoneNum = (EditText) findViewById(R.id.identification_phone_number);
        mDOB = (EditText) findViewById(R.id.identification_birthdate);
        mAge = (EditText) findViewById(R.id.identification_age);
        mAddress1 = (EditText) findViewById(R.id.identification_address1);
        mAddress2 = (EditText) findViewById(R.id.identification_address2);
        mCity = (EditText) findViewById(R.id.identification_city);
        mState = (EditText) findViewById(R.id.identification_state);
        mPostal = (EditText) findViewById(R.id.identification_postal_code);
        mCountry = (EditText) findViewById(R.id.identification_country);
        mRelationship = (EditText) findViewById(R.id.identification_relationship);
        mOccupation = (EditText) findViewById(R.id.identification_occupation);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}

package edu.jhu.bme.cbid.healthassistantsclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ComplaintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);
    }


    //This activity is going to build the fragments.
    //We're going to have the complaintcategory fragment first, on the left half of the screen.
    //The right half of the screen will be complaintfragment

}

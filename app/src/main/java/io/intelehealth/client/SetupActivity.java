package io.intelehealth.client;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.intelehealth.telemedicine.R;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        //Maybe get rid of FAB

        /*
        so have a textbox that allows to input a URL
        preset URL for the demo version of this app

        Then have them insert a prefix
        preset prefix can be JHU, and then they can test it

        maybe sure it is changed
        at least prefix MUST be changed before pressing submit

        once submit is clicked, do a progress bar that says checking

        if check works, say yes, and move on to home screen
        also save URL and Prefix to sharedprefs

        if check fails, then you say please check the URL
        thats if you couldnt even connect

        if you could connect, but the prefix returns ANYTHING then you need to do it again

        if you could connect, and prefix returns NOTHING, then you save it, and then home screen it
         */

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

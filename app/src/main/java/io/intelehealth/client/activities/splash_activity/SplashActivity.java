package io.intelehealth.client.activities.splash_activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.login_activity.LoginActivity;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.activities.setup_activity.SetupActivity;


public class SplashActivity extends AppCompatActivity {


    int PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    int PERMISSIONS_REQUEST_READ_CONTACTS = 3;
    int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4;
    int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    int PERMISSIONS_REQUEST_CAMERA = 6;
    int PERMISSIONS_REQUEST_INTERNET = 7;
    int PERMISSIONS_REQUEST_MANAGE_ACCOUNTS = 9;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //whenever re-open the app language can be set which we set before closing the app
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        String ln = prefs.getString("Language","");

        if(!ln.equalsIgnoreCase("")){
            Locale locale = new Locale(ln);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }


        /*
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    PERMISSIONS_REQUEST_GET_ACCOUNTS);

        }
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);

        }
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        }
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);

        }
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.INTERNET},
                    PERMISSIONS_REQUEST_INTERNET);

        }
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.ACCOUNT_MANAGER)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.ACCOUNT_MANAGER},
                    PERMISSIONS_REQUEST_MANAGE_ACCOUNTS);

        }*/


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
        boolean setup = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SETUP_COMPLETE, false);

        String LOG_TAG = "SplashActivity";
        Log.d(LOG_TAG, String.valueOf(setup));
        if (setup) {
            Log.d(LOG_TAG, "Starting login");

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d(LOG_TAG, "Starting setup");
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
            finish();
        }
    }


}

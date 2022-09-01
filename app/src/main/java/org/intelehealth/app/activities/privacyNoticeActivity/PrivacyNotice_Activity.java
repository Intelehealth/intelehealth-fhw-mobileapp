package org.intelehealth.app.activities.privacyNoticeActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;

import org.intelehealth.app.activities.identificationActivity.IdentificationActivity;

public class PrivacyNotice_Activity extends AppCompatActivity implements View.OnClickListener {
    TextView privacy_textview;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    Button accept, reject;
    MaterialCheckBox checkBox_cho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_notice_2);
        setTitle(getString(R.string.privacy_notice_title));

        /*
         * Toolbar which displays back arrow on action bar
         * Add the below lines for every activity*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        sessionManager = new SessionManager(this);
        privacy_textview = findViewById(R.id.privacy_text);
        privacy_textview.setAutoLinkMask(Linkify.ALL);
        accept = findViewById(R.id.button_accept);
        reject = findViewById(R.id.button_reject);
        checkBox_cho = findViewById(R.id.checkbox_CHO);


        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(
                        FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                        String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

//            SharedPreferences sharedPreferences = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
//            if(sharedPreferences.getAll().values().contains("cb"))
            Locale current = getResources().getConfiguration().locale;
            if (current.toString().equals("or")) { //Privacy notice support for Odiya
                String privacy_string = obj.getString("privacyNoticeText_Odiya");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            }else if (current.toString().equals("ar")) { //Privacy notice support for Odiya
                String privacy_string = obj.getString("privacyNoticeText_ar");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            } else if (current.toString().equals("gu")) {
//                String privacy_string = obj.getString("privacyNoticeText_Gujarati");
                String privacy_string = obj.getString("privacyNoticeText");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            } else if (current.toString().equals("bn")) { //Privacy notice support for Bengali
                String privacy_string = obj.getString("privacyNoticeText_Bengali");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }
            }
            else if (current.toString().equals("hi")) { //Privacy notice support for Hindi
                String privacy_string = obj.getString("privacyNoticeText_Hindi");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            } else if (current.toString().equals("ta")) {
                String privacy_string = obj.getString("privacyNoticeText");
//                String privacy_string = obj.getString("privacyNoticeText_Tamil");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            } else if (current.toString().equals("kn")) {          //Privacy text support for Kannada
//                String privacy_string = obj.getString("privacyNoticeText");
                String privacy_string = obj.getString("privacyNoticeText_Kannada");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            }
            else if (current.toString().equals("te")) {
                String privacy_string = obj.getString("privacyNoticeText");
//                String privacy_string = obj.getString("privacyNoticeText_Telugu");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }
            }
            else if (current.toString().equals("mr")) {
               // String privacy_string = obj.getString("privacyNoticeText");
                String privacy_string = obj.getString("privacyNoticeText_Marathi");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            }
            //privacy notice support for Assamese language...
            else if (current.toString().equals("as")) {
                String privacy_string = obj.getString("privacyNoticeText");
//                String privacy_string = obj.getString("privacyNoticeText_Assamese");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }


            } else if (current.toString().equals("ml")) {
                //  todo
                String privacy_string = obj.getString("privacyNoticeText");
//                String privacy_string = obj.getString("privacyNoticeText_Hindi");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                } else {
                    privacy_textview.setText(privacy_string);
                }

            } else {
                String privacy_string = obj.getString("privacyNoticeText");
                privacy_textview.setText(privacy_string);
            }

            accept.setOnClickListener(this);
            reject.setOnClickListener(this);

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (checkBox_cho.isChecked() && v.getId() == R.id.button_accept) {

            //Clear HouseHold UUID from Session for new registration
            sessionManager.setHouseholdUuid("");

            Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
            intent.putExtra("privacy", accept.getText().toString()); //privacy value send to identificationActivity
            Log.d("Privacy", "selected radio: " + accept.getText().toString());
            startActivity(intent);
        } else if (checkBox_cho.isChecked() && v.getId() == R.id.button_reject) {
            Toast.makeText(PrivacyNotice_Activity.this,
                    getString(R.string.privacy_reject_toast), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(PrivacyNotice_Activity.this,
                    getString(R.string.please_read_out_privacy_consent_first), Toast.LENGTH_SHORT).show();
        }

    }
}

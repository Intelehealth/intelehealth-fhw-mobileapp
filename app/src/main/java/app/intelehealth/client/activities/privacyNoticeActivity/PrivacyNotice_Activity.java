package app.intelehealth.client.activities.privacyNoticeActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.util.Linkify;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import app.intelehealth.client.R;
import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.utilities.FileUtils;
import app.intelehealth.client.utilities.SessionManager;

import app.intelehealth.client.activities.identificationActivity.IdentificationActivity;

public class PrivacyNotice_Activity extends AppCompatActivity {
    TextView privacy_textview;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    RadioGroup radiogrp;
    RadioButton radiobtn;
    RadioButton radio_acc;
    RadioButton radio_rej;
    TextView txt_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        radiogrp = findViewById(R.id.radio_privacy_grp);
        radio_acc = findViewById(R.id.radio_accept);
        radio_rej = findViewById(R.id.radio_reject);

        txt_next = findViewById(R.id.txt_privacy);


        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this)); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

//            SharedPreferences sharedPreferences = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
//            if(sharedPreferences.getAll().values().contains("cb"))
            Locale current = getResources().getConfiguration().locale;
//            if(current.toString().equals("cb"))
//            {
//                String privacy_string = obj.getString("privacyNoticeText_Cebuano");
//                if(privacy_string.isEmpty())
//                {
//                    privacy_string = obj.getString("privacyNoticeText");
//                    privacy_textview.setText(privacy_string);
//                }
//                else
//                {
//                    privacy_textview.setText(privacy_string);
//                }
//
//            }
//            else
            if (current.toString().equals("or")) {
                String privacy_string = obj.getString("privacyNoticeText_Odiya");
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


            txt_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int selected_radio = radiogrp.getCheckedRadioButtonId();
                    radiobtn = findViewById(selected_radio);

                    if (radio_acc.isChecked() || radio_rej.isChecked()) {
                        if (radio_acc.isChecked()) {
//                            sessionManager.setOfllineOpenMRSID("");
                            Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
                            intent.putExtra("privacy", radiobtn.getText()); //privacy value send to identificationActivity
                            startActivity(intent);
                        } else {
                            Toast.makeText(PrivacyNotice_Activity.this, getString(R.string.privacy_reject_toast), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.privacy_toast), Toast.LENGTH_SHORT).show();
                    }


                }
            });


        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
        }


    }
}

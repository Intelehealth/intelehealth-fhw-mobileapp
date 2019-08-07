package io.intelehealth.client.activities.privacy_notice_activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.identification_activity.IdentificationActivity;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.utilities.SessionManager;

public class PrivacyNotice_Activity extends AppCompatActivity {
    TextView privacy_textview;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    RadioGroup radiogrp;
    RadioButton radiobtn;
    RadioButton radio_acc;
    RadioButton radio_rej;
    TextView txt_next;
    String mFileName = "config.json";
    private static final String TAG = "Privacy" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_notice_);

        sessionManager = new SessionManager(this);
        privacy_textview = (TextView) findViewById(R.id.privacy_text);

        radiogrp = (RadioGroup) findViewById(R.id.radio_privacy_grp);
        radio_acc = (RadioButton) findViewById(R.id.radio_accept);
        radio_rej = (RadioButton) findViewById(R.id.radio_reject);

        txt_next = (TextView) findViewById(R.id.txt_privacy);


        if (sessionManager.valueContains("licensekey"))
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense)
            {
                obj = new JSONObject(FileUtils.readFileRoot(mFileName, this)); //Load the config file

            } else
            {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }

            SharedPreferences sharedPreferences = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
            if(sharedPreferences.getAll().values().contains("cb"))
            {
                String privacy_string = obj.getString("privacyNoticeText_Cebuano");
                if(privacy_string.isEmpty())
                {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                }
                else
                {
                    privacy_textview.setText(privacy_string);
                }

            }
            else if(sharedPreferences.getAll().values().contains("or"))
            {
                String privacy_string = obj.getString("privacyNoticeText_Odiya");
                if(privacy_string.isEmpty())
                {
                    privacy_string = obj.getString("privacyNoticeText");
                    privacy_textview.setText(privacy_string);
                }
                else
                {
                    privacy_textview.setText(privacy_string);
                }

            }
            else
            {
                String privacy_string = obj.getString("privacyNoticeText");
                privacy_textview.setText(privacy_string);
            }



            txt_next.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {

                    int selected_radio = radiogrp.getCheckedRadioButtonId();
                    radiobtn = (RadioButton) findViewById(selected_radio);

                    if (radio_acc.isChecked() || radio_rej.isChecked())
                    {
                        if(radiobtn.getText().equals("Accept"))
                        {
                            Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
                            intent.putExtra("privacy",radiobtn.getText()); //privacy value send to identificationActivity
                            Log.d(TAG, "Privacy Value on (Privacy): "+radiobtn.getText());
                            startActivity(intent);
                        }
                        else
                        {
                            finish();
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),getString(R.string.privacy_toast), Toast.LENGTH_SHORT).show();
                    }


                }
            });


        }
        catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
        }




    }
}
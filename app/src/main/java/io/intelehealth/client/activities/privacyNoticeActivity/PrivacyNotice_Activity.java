package io.intelehealth.client.activities.privacyNoticeActivity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.identificationActivity.IdentificationActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.utilities.ConfigUtils;
import io.intelehealth.client.utilities.FileUtils;
import io.intelehealth.client.utilities.SessionManager;

public class PrivacyNotice_Activity extends AppCompatActivity {
    TextView privacy_textview;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    RadioGroup radiogrp;
    RadioButton radiobtn;
    TextView btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_notice_2);

        sessionManager = new SessionManager(this);
        privacy_textview = (TextView) findViewById(R.id.privacy_text);

        radiogrp = (RadioGroup) findViewById(R.id.radio_privacy_grp);

        btn = (TextView) findViewById(R.id.txt_privacy);


        if (sessionManager.valueContains("licensekey"))
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense)
            {
                obj = new JSONObject(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this)); //Load the config file

            } else
                {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
                }

            String privacy_string = obj.getString("privacyNoticeText");
            privacy_textview.setText(privacy_string);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int selected_radio = radiogrp.getCheckedRadioButtonId();
                    radiobtn = (RadioButton) findViewById(selected_radio);

                    if(radiobtn.getText().equals("Accept"))
                    {
                        Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
                        intent.putExtra("privacy",radiobtn.getText()); //privacy value send to identificationActivity
                        startActivity(intent);
                    }
                    else
                    {
                        finish();
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

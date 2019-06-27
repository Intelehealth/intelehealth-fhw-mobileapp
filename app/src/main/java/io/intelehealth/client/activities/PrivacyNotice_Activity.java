package io.intelehealth.client.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.identificationActivity.IdentificationActivity;

public class PrivacyNotice_Activity extends AppCompatActivity {

    RadioGroup radiogrp;
    RadioButton radiobtn;
    TextView btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_notice_);

        radiogrp = (RadioGroup) findViewById(R.id.radio_privacy_grp);

        btn = (TextView) findViewById(R.id.txt_privacy);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selected_radio = radiogrp.getCheckedRadioButtonId();
                radiobtn = (RadioButton) findViewById(selected_radio);

                if(radiobtn.getText().equals("Accept"))
                {
                    Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
                    startActivity(intent);
                }
                else
                {
                    finish();
                }

            }
        });
    }
}

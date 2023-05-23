package org.intelehealth.ezazi.activities.addNewPatient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import org.intelehealth.ezazi.R;

import java.io.Serializable;

public class AddNewPatientActivity extends AppCompatActivity {
    private static final String TAG = "AddNewPatientActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_patient);

        initUI();


    }

    private void initUI() {
        ImageView ivPersonalInfo = findViewById(R.id.iv_personal_info);
        ImageView ivAddressInfo = findViewById(R.id.iv_address_info);
        ImageView ivOtherInfo = findViewById(R.id.iv_other_info);

        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        tvTitle.setText(getResources().getString(R.string.add_patient));

        ivPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setScreen(new PatientPersonalInfoFragment());
            }
        });
        ivAddressInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setScreen(new PatientAddressInfoFragment());
            }
        });
        ivOtherInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setScreen(new PatientOtherInfoFragment());
            }
        });
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, new PatientPersonalInfoFragment())
                .commit();


       /* Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {

              String  patient_detail = intent.getStringExtra("ScreenEdit");

                Bundle args = intent.getBundleExtra("BUNDLE");
                if (patient_detail.equalsIgnoreCase("personal_edit")) {
                    setScreen(new PatientPersonalInfoFragment());
                } else if (patient_detail.equalsIgnoreCase("address_edit")) {
                    setScreen(new PatientAddressInfoFragment());
                } else if (patient_detail.equalsIgnoreCase("others_edit")) {
                    setScreen(new PatientOtherInfoFragment());
                }

            }*/
        }


    private void setScreen(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_add_patient, fragment)
                .commit();
    }
/*
    private void setscreen(Fragment fragment) {
        // Bundle data
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientDTO", (Serializable) patientdto);
        Log.v(TAG, "reltion: " + patientID_edit);
        if (patientID_edit != null) {
            bundle.putString("patientUuid", patientID_edit);
        } else {
            bundle.putString("patientUuid", patientdto.getUuid());
        }
        bundle.putBoolean("fromFirstScreen", true);
        bundle.putBoolean("fromSecondScreen", true);
        bundle.putBoolean("fromThirdScreen", true);
        bundle.putBoolean("patient_detail", true);
        fragment.setArguments(bundle); // passing data to Fragment

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_firstscreen, fragment)
                .commit();
    }
*/

}
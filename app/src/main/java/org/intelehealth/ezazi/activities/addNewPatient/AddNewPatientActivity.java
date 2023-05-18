package org.intelehealth.ezazi.activities.addNewPatient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
package org.intelehealth.ezazi.activities.addNewPatient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.IntelehealthApplication;

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
        ImageView ibBackArrow = toolbar.findViewById(R.id.iv_back_arrow_common);


        ibBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.are_you_want_go_back);
        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent i_back = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i_back);
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.generic_no, null);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

}
package org.intelehealth.app.activities.visit;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import org.intelehealth.app.R;

public class PrescriptionActivity extends AppCompatActivity {
    private ImageButton btn_up_header, btnup_drdetails_header, btnup_diagnosis_header, btnup_medication_header,
            btnup_test_header, btnup_speciality_header, btnup_followup_header;
    private RelativeLayout vs_header_expandview, vs_drdetails_header_expandview,
            vs_diagnosis_header_expandview, vs_medication_header_expandview, vs_testheader_expandview,
            vs_speciality_header_expandview, vs_followup_header_expandview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription2);

        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initUI();
        expandableCardVisibilityHandling();


    }

    private void initUI() {
        btn_up_header = findViewById(R.id.btn_up_header);
        btnup_drdetails_header = findViewById(R.id.btnup_drdetails_header);
        btnup_diagnosis_header = findViewById(R.id.btnup_diagnosis_header);
        btnup_medication_header = findViewById(R.id.btnup_medication_header);
        btnup_test_header = findViewById(R.id.btnup_test_header);
        btnup_speciality_header = findViewById(R.id.btnup_speciality_header);
        btnup_followup_header = findViewById(R.id.btnup_followup_header);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_drdetails_header_expandview = findViewById(R.id.vs_drdetails_header_expandview);
        vs_diagnosis_header_expandview = findViewById(R.id.vs_diagnosis_header_expandview);
        vs_medication_header_expandview = findViewById(R.id.vs_medication_header_expandview);
        vs_testheader_expandview = findViewById(R.id.vs_testheader_expandview);
        vs_speciality_header_expandview = findViewById(R.id.vs_speciality_header_expandview);
        vs_followup_header_expandview = findViewById(R.id.vs_followup_header_expandview);
    }

    private void expandableCardVisibilityHandling() {
        btn_up_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else
                vs_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_drdetails_header.setOnClickListener(v -> {
            if (vs_drdetails_header_expandview.getVisibility() == View.VISIBLE)
                vs_drdetails_header_expandview.setVisibility(View.GONE);
            else
                vs_drdetails_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_diagnosis_header.setOnClickListener(v -> {
            if (vs_diagnosis_header_expandview.getVisibility() == View.VISIBLE)
                vs_diagnosis_header_expandview.setVisibility(View.GONE);
            else
                vs_diagnosis_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_medication_header.setOnClickListener(v -> {
            if (vs_medication_header_expandview.getVisibility() == View.VISIBLE)
                vs_medication_header_expandview.setVisibility(View.GONE);
            else
                vs_medication_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_test_header.setOnClickListener(v -> {
            if (vs_testheader_expandview.getVisibility() == View.VISIBLE)
                vs_testheader_expandview.setVisibility(View.GONE);
            else
                vs_testheader_expandview.setVisibility(View.VISIBLE);
        });

        btnup_speciality_header.setOnClickListener(v -> {
            if (vs_speciality_header_expandview.getVisibility() == View.VISIBLE)
                vs_speciality_header_expandview.setVisibility(View.GONE);
            else
                vs_speciality_header_expandview.setVisibility(View.VISIBLE);
        });

        btnup_followup_header.setOnClickListener(v -> {
            if (vs_followup_header_expandview.getVisibility() == View.VISIBLE)
                vs_followup_header_expandview.setVisibility(View.GONE);
            else
                vs_followup_header_expandview.setVisibility(View.VISIBLE);
        });
    }
}
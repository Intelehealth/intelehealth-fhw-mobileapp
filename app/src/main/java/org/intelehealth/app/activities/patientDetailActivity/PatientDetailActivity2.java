package org.intelehealth.app.activities.patientDetailActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.models.dto.PatientDTO;

public class PatientDetailActivity2 extends AppCompatActivity {
    TextView name, district;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail2);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        Intent intent = getIntent();
        if (intent != null) {
            //  category_value = intent.getStringExtra("category");
            Bundle args = intent.getBundleExtra("BUNDLE");
            PatientDTO patientDTO = (PatientDTO) args.getSerializable("patientDTO");
        }

        name = findViewById(R.id.name);
        district = findViewById(R.id.district);

        name.setText("Prajwal Waingankar");
        district.setText("Thane");
    }
}
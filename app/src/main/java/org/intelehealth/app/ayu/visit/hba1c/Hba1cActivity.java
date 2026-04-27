package org.intelehealth.app.ayu.visit.hba1c;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

public class Hba1cActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hba1c);
        Button btnBleSearch = findViewById(R.id.btnBleSearch);
        Button btnMeasurement = findViewById(R.id.btnMeasurement);
        btnBleSearch.setOnClickListener(v -> {
            Intent intent = new Intent(Hba1cActivity.this, BleSearchActivity.class);
            startActivity(intent);
        });

        btnMeasurement.setOnClickListener(v -> {
            Intent intent = new Intent(Hba1cActivity.this, MeasurementActivity.class);
            startActivity(intent);
        });
    }
}
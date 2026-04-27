package org.intelehealth.app.ayu.visit.pocdevice;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import org.intelehealth.app.R;

public class HeartAndLungRecordedValuesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_heart_and_lung_recorded_values);

    }
}
package org.intelehealth.app.activities.patientSurveyActivity;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;

import org.intelehealth.app.R;

public class PatientSurveyActivity_New extends AppCompatActivity {
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_survey_new);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingbar);


    }
}
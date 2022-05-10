package org.intelehealth.ekalarogya.activities.surveyActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.surveyActivity.fragments.FirstScreenFragment;

public class SurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        startFragmentTransactions();
    }

    private void startFragmentTransactions() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_container, new FirstScreenFragment())
                .commit();
    }
}
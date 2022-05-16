package org.intelehealth.ekalarogya.activities.surveyActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.surveyActivity.fragments.FirstScreenFragment;
import org.intelehealth.ekalarogya.models.dto.PatientAttributesDTO;

import java.util.ArrayList;
import java.util.List;

public class SurveyActivity extends AppCompatActivity {

    public static List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();


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
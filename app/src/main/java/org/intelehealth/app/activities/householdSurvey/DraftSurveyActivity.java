package org.intelehealth.app.activities.householdSurvey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import org.intelehealth.app.R;
import org.intelehealth.app.models.dto.PatientAttributesDTO;

import java.util.ArrayList;
import java.util.List;

public class DraftSurveyActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DraftSurveyAdapter draftSurveyAdapter;
    private Context context = DraftSurveyActivity.this;
    private List<PatientAttributesDTO> patientAttributesDTOList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_survey);

        recyclerView = findViewById(R.id.recycler_draftSurvey);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false));
        draftSurveyAdapter = new DraftSurveyAdapter(patientAttributesDTOList, context);
        recyclerView.setAdapter(draftSurveyAdapter);
    }
}
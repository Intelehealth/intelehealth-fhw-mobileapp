package org.intelehealth.app.activities.householdSurvey;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.householdSurvey.Fragments.FirstScreenFragment;
import org.intelehealth.app.models.dto.PatientAttributesDTO;

import java.util.ArrayList;
import java.util.List;

public class HouseholdSurveyActivity extends AppCompatActivity {
    public static List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household_survey);
        setTitle(getString(R.string.household_survey));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.framelayout_container, new FirstScreenFragment())
                .commit();

       /* Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
        }*/

    }


}
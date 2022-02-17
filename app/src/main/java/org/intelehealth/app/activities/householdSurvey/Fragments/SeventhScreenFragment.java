package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.householdSurvey.HouseholdSurveyActivity;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;

public class SeventhScreenFragment extends Fragment {

    public SeventhScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_seventh_screen, container, false);
        Button next_button = rootView.findViewById(R.id.submit_button);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HouseholdSurveyActivity.class);
                intent.putExtra("hasPrescription", "false");
                startActivity(intent);
            }
        });

        return rootView;
    }
}
package org.intelehealth.app.activities.householdSurvey.Fragments;

/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstScreenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstScreenFragment extends Fragment {
    EditText nameInvestigator, villageSurvey, blockSurvey, districtSurvey, dateofVisit, namePerson, householdNumber;
    RadioButton kuchaRadioButton, puccaRadioButton;
    RadioButton availableAccepted, availableDeferred, notavailableSurvey, notavailableSecondVisit, notavailableThirdVisit, RefusedParticipate;
    ImageButton next_button;


    public FirstScreenFragment() {
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
        View rootView =  inflater.inflate(R.layout.fragment_first_screen, container, false);

        nameInvestigator = rootView.findViewById(R.id.investigator_name_edit_text);
        villageSurvey = rootView.findViewById(R.id.villageSurvey);
        blockSurvey = rootView.findViewById(R.id.block_dropdown);
        districtSurvey = rootView.findViewById(R.id.districtSurvey);
        dateofVisit = rootView.findViewById(R.id.dateVisit);
        namePerson = rootView.findViewById(R.id.primary_respondent_name_edit_text);
        householdNumber = rootView.findViewById(R.id.household_number_edit_text);
        kuchaRadioButton = rootView.findViewById(R.id.kuchaRadioButton);
        puccaRadioButton = rootView.findViewById(R.id.puccaRadioButton);
        availableAccepted = rootView.findViewById(R.id.next_button);
        availableDeferred = rootView.findViewById(R.id.availableDeferred);
        notavailableSurvey = rootView.findViewById(R.id.next_button);
        notavailableSecondVisit = rootView.findViewById(R.id.next_button);
        notavailableThirdVisit = rootView.findViewById(R.id.next_button);
        RefusedParticipate = rootView.findViewById(R.id.next_button);
        next_button = rootView.findViewById(R.id.next_button);

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.framelayout_container, new SecondScreenFragment())
                        .commit();
            }
        });
        return rootView;
    }

}
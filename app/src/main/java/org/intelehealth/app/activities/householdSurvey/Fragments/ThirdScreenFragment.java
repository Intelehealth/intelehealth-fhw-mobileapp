package org.intelehealth.app.activities.householdSurvey.Fragments;
/**
 * Created by Prajwal Maruti Waingankar on 14-02-2022
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.intelehealth.app.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ThirdScreenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

// TODO: Refer the implementation of FirstScreen and SecondScreen for data interoperability and flow and
//  follow same for all other screens -- Prajwal 17-02-2022.


public class ThirdScreenFragment extends Fragment {

    public ThirdScreenFragment() {
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
        View rootView =  inflater.inflate(R.layout.fragment_third_screen, container, false);
        ImageButton next_button = rootView.findViewById(R.id.next_button);

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.framelayout_container, new FourthScreenFragment())
                        .commit();
            }
        });
        return rootView;
    }
}
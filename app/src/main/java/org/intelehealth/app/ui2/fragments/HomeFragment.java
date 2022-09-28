package org.intelehealth.app.ui2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientActivity_New;

public class HomeFragment extends Fragment {
    CardView followup_cardview;
    TextView textlayout_find_patient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_ui2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        followup_cardview = view.findViewById(R.id.followup_cardview);
        textlayout_find_patient = view.findViewById(R.id.textlayout_find_patient);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textlayout_find_patient.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchPatientActivity_New.class);
            startActivity(intent);
        });

        followup_cardview.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), FollowUpPatientActivity_New.class);
            startActivity(intent);
        });
    }
}

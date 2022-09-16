package org.intelehealth.app.ui2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New;

public class HomeFragment extends Fragment {
    CardView followup_cardview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_home_ui2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        followup_cardview = view.findViewById(R.id.followup_cardview);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        followup_cardview.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), FollowUpPatientActivity_New.class);
            startActivity(intent);
        });
    }
}

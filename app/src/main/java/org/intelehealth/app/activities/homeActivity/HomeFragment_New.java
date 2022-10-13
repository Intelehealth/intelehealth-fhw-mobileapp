package org.intelehealth.app.activities.homeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.appointment.MyAppointmentActivity;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientActivity_New;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Objects;

public class HomeFragment_New extends Fragment {
    private static final String TAG = "HomeFragment_New";
    View view;
    SessionManager sessionManager;
    CardView followup_cardview, addpatient_cardview;
    TextView textlayout_find_patient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_ui2, container, false);
        return view;
    }

    private void initUI() {
        sessionManager = new SessionManager(getActivity());

        ImageView viewHamburger = Objects.requireNonNull(getActivity()).findViewById(R.id.iv_hamburger);
        viewHamburger.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_hamburger));
        TextView tvLocation = Objects.requireNonNull(getActivity()).findViewById(R.id.tv_user_location_home);
        tvLocation.setText(sessionManager.getLocationName());
        TextView tvLastSyncApp = Objects.requireNonNull(getActivity()).findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = Objects.requireNonNull(getActivity()).findViewById(R.id.imageview_notifications_home);
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.VISIBLE);
        ivNotification.setVisibility(View.VISIBLE);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.VISIBLE);

        CardView cardAppointment = view.findViewById(R.id.cardView4_appointment);
        cardAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyAppointmentActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: homefragen");
        initUI();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        followup_cardview = view.findViewById(R.id.followup_cardview);
        addpatient_cardview = view.findViewById(R.id.addpatient_cardview);
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

        addpatient_cardview.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PrivacyPolicyActivity_New.class);
            intent.putExtra("add_patient", "add_patient");
            startActivity(intent);
        });
    }
}


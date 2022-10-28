package org.intelehealth.app.appointmentNew;

import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.Objects;

public class TodaysMyAppointmentsFragment extends Fragment {
    View view;
    LinearLayout cardUpcomingAppointments, cardCancelledAppointments, cardCompletedAppointments, layoutMainAppOptions,
            layoutUpcoming, layoutCancelled, layoutCompleted;
    RecyclerView rvUpcomingApp, rvCancelledApp, rvCompletedApp;
    LinearLayout layoutParentAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_todays_appointments_ui2, container, false);
        initUI();
        clickListeners();
        return view;
    }

    private void initUI() {

        rvUpcomingApp = view.findViewById(R.id.rv_upcoming_appointments);
        rvCancelledApp = view.findViewById(R.id.rv_cancelled_appointments);
        rvCompletedApp = view.findViewById(R.id.rv_completed_appointments);
        cardUpcomingAppointments = view.findViewById(R.id.card_upcoming_appointments);
        cardCancelledAppointments = view.findViewById(R.id.card_cancelled_appointments);
        cardCompletedAppointments = view.findViewById(R.id.card_completed_appointments);
        layoutMainAppOptions = view.findViewById(R.id.layout_main_app_options);
        layoutUpcoming = view.findViewById(R.id.layout_upcoming);
        layoutCancelled = view.findViewById(R.id.layout_cancelled);
        layoutCompleted = view.findViewById(R.id.layout_completed);
        layoutParentAll = view.findViewById(R.id.layout_parent_all);


        //recyclerview for upcoming appointments
        TodaysMyAppointmentsAdapter todaysUpcomingAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity());
        rvUpcomingApp.setAdapter(todaysUpcomingAppointmentsAdapter);

        //recyclerview for cancelled appointments
        TodaysMyAppointmentsAdapter todaysMyAppointmentsAdapter = new TodaysMyAppointmentsAdapter(getActivity());
        rvCancelledApp.setAdapter(todaysMyAppointmentsAdapter);

        //recyclerview for completed appointments
        TodaysMyAppointmentsAdapter todaysMyAppointmentsAdapter1 = new TodaysMyAppointmentsAdapter(getActivity());
        rvCompletedApp.setAdapter(todaysMyAppointmentsAdapter1);

    }

    private void clickListeners() {
        cardUpcomingAppointments.setOnClickListener(v -> {
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_ui2);
            cardUpcomingAppointments.startAnimation(fadeOut);

            layoutUpcoming.setVisibility(View.VISIBLE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutUpcoming.setLayoutParams(params);
        });
        cardCancelledAppointments.setOnClickListener(v -> {

            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));

            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_ui2);
            cardCancelledAppointments.startAnimation(fadeOut);

            layoutUpcoming.setVisibility(View.GONE);
            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCancelled.setLayoutParams(params);

        });
        cardCompletedAppointments.setOnClickListener(v -> {
            cardCancelledAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardUpcomingAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));

            layoutMainAppOptions.setBackground(getResources().getDrawable(R.drawable.ui2_ic_bg_options_appointment));
            cardCompletedAppointments.setBackground(getResources().getDrawable(R.drawable.ui2_bg_selcted_card));
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_ui2);
            cardCompletedAppointments.startAnimation(fadeOut);

            layoutCompleted.setVisibility(View.VISIBLE);
            layoutCancelled.setVisibility(View.GONE);
            layoutUpcoming.setVisibility(View.GONE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
            params.weight = 1.0f;
            params.gravity = Gravity.TOP;

            layoutCompleted.setLayoutParams(params);

        });


    }



}

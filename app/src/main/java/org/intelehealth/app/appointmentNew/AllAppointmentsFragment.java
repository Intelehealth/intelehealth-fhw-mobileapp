package org.intelehealth.app.appointmentNew;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class AllAppointmentsFragment extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_all_appointments_ui2, container, false);
        initUI();
        return view;
    }

    private void initUI() {

        RecyclerView rvUpcomingApp = view.findViewById(R.id.rv_all_upcoming_appointments);
        RecyclerView rvCancelledApp = view.findViewById(R.id.rv_all_cancelled_appointments);
        RecyclerView rvCompletedApp = view.findViewById(R.id.rv_all_completed_appointments);
        FrameLayout frameLayoutFilter = view.findViewById(R.id.filter_frame_all_appointments);
        ImageView ivFilterAllApp = view.findViewById(R.id.iv_filter_all_app);
        FrameLayout frameLayoutDateFilter = view.findViewById(R.id.filter_frame_date_appointments);
        ImageView ivDateFilter = view.findViewById(R.id.iv_calendar_all_app);

        //recyclerview for upcoming appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter = new MyAllAppointmentsAdapter(getActivity());
        rvUpcomingApp.setAdapter(myAllAppointmentsAdapter);

        //recyclerview for cancelled appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter1 = new MyAllAppointmentsAdapter(getActivity());
        rvCancelledApp.setAdapter(myAllAppointmentsAdapter1);

        //recyclerview for completed appointments
        MyAllAppointmentsAdapter myAllAppointmentsAdapter2 = new MyAllAppointmentsAdapter(getActivity());
        rvCompletedApp.setAdapter(myAllAppointmentsAdapter2);

        //click listeners for filters

        ivFilterAllApp.setOnClickListener(v -> {
            // filter options
            if (frameLayoutFilter.getVisibility() == View.VISIBLE)
                frameLayoutFilter.setVisibility(View.GONE);
            else
                frameLayoutFilter.setVisibility(View.VISIBLE);
        });

        ivDateFilter.setOnClickListener(v -> {
            // filter options
            if (frameLayoutDateFilter.getVisibility() == View.VISIBLE)
                frameLayoutDateFilter.setVisibility(View.GONE);
            else
                frameLayoutDateFilter.setVisibility(View.VISIBLE);
        });


    }


}

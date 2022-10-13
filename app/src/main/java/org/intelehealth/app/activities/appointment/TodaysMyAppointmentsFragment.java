package org.intelehealth.app.activities.appointment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class TodaysMyAppointmentsFragment extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_todays_appointments_ui2, container, false);
        initUI();
        return view;
    }

    private void initUI() {

        RecyclerView rvUpcomingApp = view.findViewById(R.id.rv_upcoming_appointments);
        RecyclerView rvCancelledApp = view.findViewById(R.id.rv_cancelled_appointments);
        RecyclerView rvCompletedApp = view.findViewById(R.id.rv_completed_appointments);

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


}

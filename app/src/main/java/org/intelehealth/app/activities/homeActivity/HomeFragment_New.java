package org.intelehealth.app.activities.homeActivity;

import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.followuppatients.FollowUpPatientActivity_New;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientActivity_New;
import org.intelehealth.app.activities.visit.EndVisitActivity;
import org.intelehealth.app.activities.visit.VisitActivity;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointmentNew.MyAppointmentActivity;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment_New extends Fragment implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "HomeFragment_New";
    View view;
    SessionManager sessionManager;
    CardView followup_cardview, addpatient_cardview;
    TextView textlayout_find_patient;
    NetworkUtils networkUtils;
    ImageView ivInternet;
    private TextView mUpcomingAppointmentCountTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_ui2, container, false);
        networkUtils = new NetworkUtils(getActivity(), this);

        return view;
    }

    private void initUI() {
        sessionManager = new SessionManager(getActivity());
        View layoutToolbar = requireActivity().findViewById(R.id.toolbar_home);
        layoutToolbar.setVisibility(View.VISIBLE);
        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        }

        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        ImageView viewHamburger = requireActivity().findViewById(R.id.iv_hamburger);
        viewHamburger.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_hamburger));
        DrawerLayout mDrawerLayout = requireActivity().findViewById(R.id.drawer_layout);

        /*if (viewHamburger != null) {
            viewHamburger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(Gravity.LEFT);

                }
            });
        } else {
            Log.d(TAG, "clickListeners: iv_hamburger null");
        }*/
        mUpcomingAppointmentCountTextView = requireActivity().findViewById(R.id.textView5);
        TextView tvLocation = requireActivity().findViewById(R.id.tv_user_location_home);
        tvLocation.setText(sessionManager.getLocationName());
        TextView tvLastSyncApp = requireActivity().findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = requireActivity().findViewById(R.id.imageview_notifications_home);
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.VISIBLE);
        ivNotification.setVisibility(View.VISIBLE);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.VISIBLE);
        ivInternet = requireActivity().findViewById(R.id.imageview_is_internet);


        CardView cardAppointment = view.findViewById(R.id.cardView4_appointment);
        CardView closedVisitsCardView = view.findViewById(R.id.closedVisitsCardView);
        CardView card_prescription = view.findViewById(R.id.card_prescription);

        cardAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyAppointmentActivity.class);
                startActivity(intent);
               /* Intent intent = new Intent(getActivity(), ScheduleAppointmentActivity_New.class);
                startActivity(intent);*/
            }
        });

        closedVisitsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), VisitSummaryActivity_New.class);
                startActivity(intent);*/
                Intent intent = new Intent(getActivity(), EndVisitActivity.class);
                startActivity(intent);
            }
        });

        card_prescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VisitActivity.class);
                startActivity(intent);
            }
        });

        getUpcomingAppointments();
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

    @Override
    public void onStart() {
        super.onStart();

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }
    }

    private void getUpcomingAppointments() {
        //recyclerview for upcoming appointments
        int totalUpcomingApps = 0;
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat1.format(new Date());

        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointmentsWithFiltersForToday("", currentDate);
        List<AppointmentInfo> upcomingAppointmentsList = new ArrayList<>();

        try {
            if (appointmentInfoList.size() > 0) {
                for (int i = 0; i < appointmentInfoList.size(); i++) {
                    AppointmentInfo appointmentInfo = appointmentInfoList.get(i);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                    String currentDateTime = dateFormat.format(new Date());
                    String slottime = appointmentInfo.getSlotDate() + " " + appointmentInfo.getSlotTime();

                    long diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();

                    long second = diff / 1000;
                    long minutes = second / 60;
                    if (appointmentInfo.getStatus().equalsIgnoreCase("booked") && minutes >= 0) {
                        upcomingAppointmentsList.add(appointmentInfo);
                    }
                }
                totalUpcomingApps = upcomingAppointmentsList.size();


            } else {
                totalUpcomingApps = 0;
            }
            mUpcomingAppointmentCountTextView.setText(totalUpcomingApps + " upcoming");

        } catch (
                Exception e) {
            e.printStackTrace();
        }


    }
}


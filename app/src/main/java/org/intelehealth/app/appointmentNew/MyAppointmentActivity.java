package org.intelehealth.app.appointmentNew;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import org.intelehealth.app.utilities.CustomLog;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NavigationUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class MyAppointmentActivity extends BaseActivity implements UpdateAppointmentsCount, NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "MyAppointmentActivity";
    BottomNavigationView bottomNav;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    String fromFragment = "";
    int totalCount;
    NetworkUtils networkUtils;
    ImageView ivIsInternet;
    private ObjectAnimator syncAnimator;
    private boolean mIsInternetAvailable;
    private HashMap<Integer, UpdateFragmentOnEvent> mUpdateFragmentOnEventHashMap = new HashMap<>();

    public void initUpdateFragmentOnEvent(int tab, UpdateFragmentOnEvent listener) {
        CustomLog.v(TAG, "initUpdateFragmentOnEvent");
        mUpdateFragmentOnEventHashMap.put(tab, listener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointment_new_ui2);
        networkUtils = new NetworkUtils(MyAppointmentActivity.this, this);

        initUI();


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllAppointments();
    }

    private void loadAllAppointments() {
        CustomLog.v(TAG, "loadAllAppointments");
        String baseurl = BuildConfig.SERVER_URL + ":3004";
        int tabIndex = tabLayout.getSelectedTabPosition();
        if (mUpdateFragmentOnEventHashMap.containsKey(tabIndex))
            Objects.requireNonNull(mUpdateFragmentOnEventHashMap.get(tabIndex)).onFinished(AppConstants.EVENT_FLAG_START);
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlotsAll(DateAndTimeUtils.getCurrentDateInDDMMYYYYFormat(),
                        DateAndTimeUtils.getOneMonthAheadDateInDDMMYYYYFormat(),
                        new SessionManager(this).getLocationUuid())

                .enqueue(new Callback<AppointmentListingResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentListingResponse> call, retrofit2.Response<AppointmentListingResponse> response) {
                        if (response.body() == null) return;
                        CustomLog.v(TAG, "onResponse - " + new Gson().toJson(response.body()));
                        AppointmentListingResponse slotInfoResponse = response.body();
                        AppointmentDAO appointmentDAO = new AppointmentDAO();
                        appointmentDAO.deleteAllAppointments();


                        if (slotInfoResponse.getData().size() > 0) {
                            for (int i = 0; i < slotInfoResponse.getData().size(); i++) {

                                try {
                                    appointmentDAO.insert(slotInfoResponse.getData().get(i));

                                } catch (DAOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        /*if (slotInfoResponse.getCancelledAppointments() != null) {
                            if (slotInfoResponse.getCancelledAppointments().size() > 0) {

                                for (int i = 0; i < slotInfoResponse.getCancelledAppointments().size(); i++) {

                                    try {
                                        appointmentDAO.insert(slotInfoResponse.getCancelledAppointments().get(i));

                                    } catch (DAOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }*/

                        //getAppointments();
                        CustomLog.v(TAG, "onFinished - " + new Gson().toJson(slotInfoResponse));
                        Objects.requireNonNull(mUpdateFragmentOnEventHashMap.get(tabIndex)).onFinished(AppConstants.EVENT_FLAG_SUCCESS);
                    }

                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        CustomLog.v("onFailure", t.getMessage());
                        Objects.requireNonNull(mUpdateFragmentOnEventHashMap.get(tabIndex)).onFinished(AppConstants.EVENT_FLAG_FAILED);
                        //log out operation if response code is 401
                        new NavigationUtils().logoutOperation(MyAppointmentActivity.this,t);
                    }
                });


    }


    private void initUI() {
        View toolbar = findViewById(R.id.toolbar_my_appointments);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        ImageView ivBackArrow = toolbar.findViewById(R.id.iv_back_arrow_common);

        tvTitle.setText(getResources().getString(R.string.my_appointments));

        ivIsInternet.setOnClickListener(v -> {
            SyncUtils.syncNow(MyAppointmentActivity.this, ivIsInternet, syncAnimator);
        });

        ivBackArrow.setOnClickListener(v -> {
            Intent intent = new Intent(MyAppointmentActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });

        configureTabLayout();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        bottomNav = findViewById(R.id.bottom_nav_my_appointments);
        bottomNav.setOnItemSelectedListener(navigationItemSelectedListener);
        bottomNav.setItemIconTintList(null);
        bottomNav.getMenu().findItem(R.id.bottom_nav_home_menu).setChecked(false);

    }

    public void configureTabLayout() {
        tabLayout = findViewById(R.id.tablayout_appointments);

       /* tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.todays)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all_appointments)));*/

        viewPager = findViewById(R.id.pager_appointments);
        MyAppointmentsPagerAdapter adapter = new MyAppointmentsPagerAdapter
                (getSupportFragmentManager(), 2, MyAppointmentActivity.this);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(adapter.getItemCount() - 1);

        // int limit = (adapter.getCount() > 1 ? adapter.getCount() - 1 : 1);

        //viewPager.setOffscreenPageLimit(limit);

        /*viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));*/
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setText(getString(R.string.todays));
                } else {
                    tab.setText(getString(R.string.all_appointments));
                }
            }
        }).attach();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
              /*  CustomLog.d(TAG, "onTabSelected:position : : " + tab.getPosition());
                if (fromFragment != null && !fromFragment.isEmpty() && fromFragment.equals("today")) {
                    if (tab.getPosition() == 0) {
                        tab.setText("Today's (" + totalCount + ")");

                    }
                } else if (fromFragment != null && !fromFragment.isEmpty() && fromFragment.equals("all")) {
                    if (tab.getPosition() == 1) {
                        tab.setText("All appointments (" + totalCount + ")");

                    }

                }*/
                loadAllAppointments();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();

    }

    NavigationBarView.OnItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.bottom_nav_home_menu:
                           /* CustomLog.d(TAG, "onNavigationItemSelected: bottom_nav_home_menu");
                            tvTitleHomeScreenCommon.setText(getResources().getString(R.string.title_home_screen));
                            fragment = new HomeFragment_New();
                            loadFragment(fragment);*/
                            return true;
                        case R.id.bottom_nav_achievements:
                         /*   tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_achievements));
                            fragment = new MyAchievementsFragment();
                            loadFragmentForBottomNav(fragment);*/

                            return true;
                        case R.id.bottom_nav_help:
                       /*     tvTitleHomeScreenCommon.setText(getResources().getString(R.string.help));
                            fragment = new HelpFragment_New();
                            loadFragmentForBottomNav(fragment);*/

                            return true;
                        case R.id.bottom_nav_add_patient:

                            return true;
                    }

                    return false;
                }
            };


    @Override
    public void updateCount(String whichFrag, int count) {
        //  CustomLog.d(TAG, "updateCount:selected tab : " + tabLayout.getSelectedTabPosition());

        //  CustomLog.d(TAG, "updateCount: count : " + count);

     /*   fromFragment = whichFrag;
        totalCount = count;*/

/*        new TabLayoutMediator(tabLayout, viewPager,
                (TabLayout.Tab tab, int position) -> {
                    if (position == 0)
                        tab.setText("Received (" + count + ")").setIcon(R.drawable.presc_tablayout_icon);
                    else
                        tab.setText("Pending (" + count + ")").setIcon(R.drawable.presc_tablayout_icon);

                }
        ).attach();*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();

    }

    //update ui as per internet availability
    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        mIsInternetAvailable = isInternetAvailable;
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));

        }
    }
}
package org.intelehealth.app.appointmentNew;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkUtils;

import java.util.Objects;

public class MyAppointmentActivity extends AppCompatActivity implements UpdateAppointmentsCount, NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "MyAppointmentActivity";
    BottomNavigationView bottomNav;
    TabLayout tabLayout;
    ViewPager viewPager;
    String fromFragment = "";
    int totalCount;
    NetworkUtils networkUtils;
    ImageView ivIsInternet;
    private ObjectAnimator syncAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointment_new_ui2);
        networkUtils = new NetworkUtils(MyAppointmentActivity.this, this);

        initUI();


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        bottomNav = findViewById(R.id.bottom_nav_my_appointments);
        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        bottomNav.setItemIconTintList(null);
        bottomNav.getMenu().findItem(R.id.bottom_nav_home_menu).setChecked(false);

    }

    public void configureTabLayout() {
        tabLayout = findViewById(R.id.tablayout_appointments);

        tabLayout.addTab(tabLayout.newTab().setText("Today's"));
        tabLayout.addTab(tabLayout.newTab().setText("All appointments"));

        viewPager = findViewById(R.id.pager_appointments);
        PagerAdapter adapter = new MyAppointmentsPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), MyAppointmentActivity.this);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);

        // int limit = (adapter.getCount() > 1 ? adapter.getCount() - 1 : 1);

        //viewPager.setOffscreenPageLimit(limit);

        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
              /*  Log.d(TAG, "onTabSelected:position : : " + tab.getPosition());
                if (fromFragment != null && !fromFragment.isEmpty() && fromFragment.equals("today")) {
                    if (tab.getPosition() == 0) {
                        tab.setText("Today's (" + totalCount + ")");

                    }
                } else if (fromFragment != null && !fromFragment.isEmpty() && fromFragment.equals("all")) {
                    if (tab.getPosition() == 1) {
                        tab.setText("All appointments (" + totalCount + ")");

                    }

                }*/
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();

    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.bottom_nav_home_menu:
                           /* Log.d(TAG, "onNavigationItemSelected: bottom_nav_home_menu");
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
        //  Log.d(TAG, "updateCount:selected tab : " + tabLayout.getSelectedTabPosition());

        //  Log.d(TAG, "updateCount: count : " + count);

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
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }
    }
}
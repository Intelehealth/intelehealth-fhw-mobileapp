package org.intelehealth.app.activities.visit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.appointment.MyAppointmentActivity;
import org.intelehealth.app.activities.appointment.MyAppointmentsPagerAdapter;
/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        configureTabLayout();


    }


    public void configureTabLayout() {
        TabLayout tabLayout = findViewById(R.id.tablayout_appointments);
        tabLayout.addTab(tabLayout.newTab().setText("Received (0)"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending (0)"));

        ViewPager viewPager = findViewById(R.id.pager_appointments);
        PagerAdapter adapter = new VisitPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), VisitActivity.this);
        viewPager.setAdapter(adapter);
        int limit = (adapter.getCount() > 1 ? adapter.getCount() - 1 : 1);
        viewPager.setOffscreenPageLimit(limit);

        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }

}
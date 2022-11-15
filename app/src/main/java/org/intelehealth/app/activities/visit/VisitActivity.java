package org.intelehealth.app.activities.visit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.intelehealth.app.R;
/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitActivity extends FragmentActivity {
    private int receivedTotal = 0;
    private int pendingTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);

        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        configureTabLayout();


    }


    public void configureTabLayout() {
        TabLayout tabLayout = findViewById(R.id.tablayout_appointments);
        ViewPager2 viewPager = findViewById(R.id.pager_appointments);
        VisitPagerAdapter adapter = new VisitPagerAdapter
                (VisitActivity.this);
        viewPager.setAdapter(adapter);

        EndVisitCountsInterface countsInterface = new VisitReceivedFragment();
        receivedTotal = countsInterface.getPrescCount();
        pendingTotal = countsInterface.getPrescCount();

        new TabLayoutMediator(tabLayout, viewPager,
                (TabLayout.Tab tab, int position) -> {
                    if (position == 0)
                        tab.setText("Received (" + receivedTotal + ")").setIcon(R.drawable.presc_tablayout_icon);
                    else
                        tab.setText("Pending (" + pendingTotal + ")").setIcon(R.drawable.presc_tablayout_icon);

                }
        ).attach();

        int limit = (adapter.getItemCount() > 1 ? adapter.getItemCount() - 1 : 1);
        viewPager.setOffscreenPageLimit(limit);

//        viewPager.addOnPageChangeListener(new
//                TabLayout.TabLayoutOnPageChangeListener(tabLayout));


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
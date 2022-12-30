package org.intelehealth.app.activities.visit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.VisitCountInterface;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitActivity extends FragmentActivity implements
        NetworkUtils.InternetCheckUpdateInterface, VisitCountInterface {
    private ImageButton ibBack, refresh;
    private NetworkUtils networkUtils;
    TabLayout tabLayout;
    ViewPager2 viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);

        networkUtils = new NetworkUtils(this, this);
        ibBack = findViewById(R.id.vector);
        refresh = findViewById(R.id.refresh);

        ibBack.setOnClickListener(v -> {
            Intent intent = new Intent(VisitActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });
        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        configureTabLayout();


    }


    public void configureTabLayout() {
        tabLayout = findViewById(R.id.tablayout_appointments);
        viewPager = findViewById(R.id.pager_appointments);
        VisitPagerAdapter adapter = new VisitPagerAdapter
                (VisitActivity.this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (TabLayout.Tab tab, int position) -> {
                    if (position == 0)
                        tab.setText("Received (" + 0 + ")").setIcon(R.drawable.presc_tablayout_icon);
                    else
                        tab.setText("Pending (" + 0 + ")").setIcon(R.drawable.presc_tablayout_icon);

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

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        }
        else {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));
        }
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
    public void receivedCount(int count) {
        Log.v("ccount", "received_count: " + count);
        tabLayout.getTabAt(0).setText("Received (" + count + ")");
    }

    @Override
    public void pendingCount(int count) {
        tabLayout.getTabAt(1).setText("Pending (" + count + ")");
    }
}
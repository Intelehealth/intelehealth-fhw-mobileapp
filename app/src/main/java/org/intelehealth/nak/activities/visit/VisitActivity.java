package org.intelehealth.nak.activities.visit;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.intelehealth.nak.R;
import org.intelehealth.nak.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.nak.app.AppConstants;
import org.intelehealth.nak.syncModule.SyncUtils;
import org.intelehealth.nak.utilities.NetworkConnection;
import org.intelehealth.nak.utilities.NetworkUtils;
import org.intelehealth.nak.utilities.SessionManager;
import org.intelehealth.nak.utilities.VisitCountInterface;
import org.intelehealth.nak.webrtc.activity.BaseActivity;

import java.util.Locale;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitActivity extends BaseActivity implements
        NetworkUtils.InternetCheckUpdateInterface, VisitCountInterface {
    private static final String TAG = VisitActivity.class.getName();
    private ImageButton ibBack, refresh;
    private NetworkUtils networkUtils;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    SessionManager sessionManager;
    private BroadcastReceiver mBroadcastReceiver;
    private ObjectAnimator syncAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        sessionManager = new SessionManager(this);
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
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context, getString(R.string.sync_completed), Toast.LENGTH_SHORT).show();
                Log.v(TAG, "Sync Done!");
                refresh.clearAnimation();
                syncAnimator.cancel();
                recreate();
            }
        };
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
        //registerReceiver(mBroadcastReceiver, filterSend);
        ContextCompat.registerReceiver(this, mBroadcastReceiver, filterSend, ContextCompat.RECEIVER_EXPORTED); //changed because previous code not working on android 14 and above


        syncAnimator = ObjectAnimator.ofFloat(refresh, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setRepeatCount(ValueAnimator.INFINITE);
        syncAnimator.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void configureTabLayout() {
        tabLayout = findViewById(R.id.tablayout_appointments);
        viewPager = findViewById(R.id.pager_appointments);
        VisitPagerAdapter adapter = new VisitPagerAdapter(VisitActivity.this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (TabLayout.Tab tab, int position) -> {
                    if (position == 0)
                        tab.setText(getResources().getString(R.string.received)).setIcon(R.drawable.presc_tablayout_icon);
                    else
                        tab.setText(getResources().getString(R.string.pending)).setIcon(R.drawable.presc_tablayout_icon);

                }
        ).attach();

        int limit = (adapter.getItemCount() > 1 ? adapter.getItemCount() - 1 : 1);
        viewPager.setOffscreenPageLimit(limit);
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

        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config,getResources().getDisplayMetrics());
        }
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
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
        Log.v(TAG, "receivedCount: " + count);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.received));
    }

    @Override
    public void pendingCount(int count) {
        Log.v(TAG, "pendingCount: " + count);
        tabLayout.getTabAt(1).setText(getResources().getString(R.string.pending));
    }

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            new SyncUtils().syncBackground();
            refresh.clearAnimation();
            syncAnimator.start();
        }
    }
}
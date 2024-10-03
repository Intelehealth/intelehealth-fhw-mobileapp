package org.intelehealth.app.activities.visit;

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
import org.intelehealth.app.utilities.CustomLog;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.VisitCountInterface;
import org.intelehealth.fcm.utils.NotificationBroadCast;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

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

    //this variable to detect sync click
    private boolean syncClicked = false;

    //sometimes multiple event came on broadcaster receiver
    //to detect multiple call added the field
    private int refreshCount = 0;
    private AlertDialog loadingDialog;
    private int currentTabPos = 0;
    private NotificationReceiver notificationReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        sessionManager = new SessionManager(this);
        networkUtils = new NetworkUtils(this, this);
        ibBack = findViewById(R.id.vector);
        refresh = findViewById(R.id.refresh);
        notificationReceiver =new  NotificationReceiver();
        notificationReceiver.registerNotificationReceiver(this);
        ibBack.setOnClickListener(v -> {
            Intent intent = new Intent(VisitActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });
        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);
        configureTabLayout();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("JOB")) {
                    int flagType = intent.getIntExtra("JOB", AppConstants.SYNC_PULL_DATA_DONE);
                    if (flagType == AppConstants.SYNC_PULL_DATA_DONE ||
                            flagType == AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE) {
                            CustomLog.v(TAG, "Sync Done!");
                            if (!isFinishing()) {
                                refresh.clearAnimation();
                                syncAnimator.cancel();
                            }
                            configureTabLayout();
                    }
                }

                //just stopping the progressbar here if sync is failed
                if (intent.hasExtra(AppConstants.SYNC_INTENT_DATA_KEY)) {
                    int flagType = intent.getIntExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED);
                    if (flagType == AppConstants.SYNC_FAILED) {
                        refresh.clearAnimation();
                        syncAnimator.cancel();
                        hideProgressbar();
                    }
                }
            }
        };
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
        ContextCompat.registerReceiver(
                this,
                mBroadcastReceiver,
                filterSend,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

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
        notificationReceiver.unregisterNotificationReceiver(this);
    }

    public void configureTabLayout() {
        if(refreshCount > 0) return;
        tabLayout = findViewById(R.id.tablayout_appointments);
        viewPager = findViewById(R.id.pager_appointments);
        VisitPagerAdapter adapter = new VisitPagerAdapter(VisitActivity.this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentTabPos,false);

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
                currentTabPos = tab.getPosition();
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
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }

        hideProgressbar();
        refreshCount++;


    }

    private void updateCounts(boolean isForReceivedPrescription) {
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = new VisitsDAO().getVisitCountsByStatus(isForReceivedPrescription);
            runOnUiThread(() -> {
                if (isForReceivedPrescription)
                    Objects.requireNonNull(tabLayout.getTabAt(0)).setText(getResources().getString(R.string.received) + "\t(" + count + ")");
                else
                    Objects.requireNonNull(tabLayout.getTabAt(1)).setText(getResources().getString(R.string.pending) + "\t(" + count + ")");

            });

        });
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    private void hideProgressbar() {
        if(syncClicked && !this.isFinishing()){
            loadingDialog.dismiss();
        }
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
        CustomLog.v(TAG, "receivedCount: " + count);
        //tabLayout.getTabAt(0).setText(getResources().getString(R.string.received));
        updateCounts(true);
    }

    @Override
    public void pendingCount(int count) {
        CustomLog.v(TAG, "pendingCount: " + count);
        //tabLayout.getTabAt(1).setText(getResources().getString(R.string.pending));
        updateCounts(false);
    }

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {

            if (!this.isFinishing()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog = new DialogUtils().showCommonLoadingDialog(
                                VisitActivity.this,
                                getString(R.string.loading),
                                getString(R.string.please_wait)
                        );
                    }
                });

                refresh.clearAnimation();
                syncAnimator.start();
            }
            syncClicked = true;
            refreshCount = 0;
            new SyncUtils().syncBackground();
        }
    }

    public class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NotificationBroadCast.CUSTOM_ACTION)) {
                // FCM A added action received
                String moduleName = intent.getStringExtra(NotificationBroadCast.FCM_MODULE);
                syncNow(refresh);
            }
        }

        public void registerNotificationReceiver(Context context) {
            IntentFilter filter = new IntentFilter(NotificationBroadCast.CUSTOM_ACTION);
            LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        }

        public void unregisterNotificationReceiver(Context context) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }


}
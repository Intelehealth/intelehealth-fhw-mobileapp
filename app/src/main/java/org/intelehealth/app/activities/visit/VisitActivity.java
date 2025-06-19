package org.intelehealth.app.activities.visit;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.util.DisplayMetrics;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ui.home.HomeScreenQueriesRepository;
import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
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
import org.intelehealth.app.utilities.PrescriptionLoadingListeners;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.ThreadingUtils;
import org.intelehealth.app.utilities.VisitCountInterface;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.fcm.utils.NotificationBroadCast;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
public class VisitActivity extends BaseActivity implements
        NetworkUtils.InternetCheckUpdateInterface, VisitCountInterface, PrescriptionLoadingListeners {
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
    private boolean isReceivedOldLoaded = false;
    private boolean isReceivedRecentLoaded = false;
    private boolean isPendingRecentLoaded = false;
    private boolean isPendingOldLoaded = false;
    AlertDialog commonLoadingDialog;
    // private NotificationReceiver notificationReceiver;
    public FeatureActiveStatus mFeatureActiveStatus;

    @Override
    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        super.onFeatureActiveStatusLoaded(activeStatus);
        Log.d(TAG, "onFeatureActiveStatusLoaded: activeStatus : "+activeStatus);
        Log.d(TAG, "onFeatureActiveStatusLoaded: activeStatus : "+activeStatus.getActiveStatusPrescriptionWithOtp());

        if (activeStatus != null) {
            mFeatureActiveStatus = activeStatus;
            featureStatusListener.onFeatureStatusReady(activeStatus);
        }
    }
    public FeatureActiveStatus getFeatureActiveStatus() {
        return mFeatureActiveStatus;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        sessionManager = new SessionManager(this);
     //   networkUtils = new NetworkUtils(this, this);  // TODO: Commented as it was taking heavy load on app and checking network here is not requried.
        ibBack = findViewById(R.id.vector);
        refresh = findViewById(R.id.refresh);
      //  notificationReceiver =new  NotificationReceiver(); // TODO: Commented as it was taking heavy load on app and checking network here is not requried.
       // notificationReceiver.registerNotificationReceiver(this);
        ibBack.setOnClickListener(v -> {
            Intent intent = new Intent(VisitActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });
        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);
        configureTabLayout();

//        mBroadcastReceiver = new BroadcastReceiver() {    // TODO: Commented this code as it is taking heavy load when moving from home to this screen of around 10secs.
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.hasExtra("JOB")) {
//                    int flagType = intent.getIntExtra("JOB", AppConstants.SYNC_PULL_DATA_DONE);
//                    if (flagType == AppConstants.SYNC_PULL_DATA_DONE ||
//                            flagType == AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE) {
//                        if (!isFinishing()) {
//                            refresh.clearAnimation();
//                            if (syncAnimator != null) syncAnimator.cancel();
//                        }
//                        // Delay tab layout update slightly
//                        new Handler(Looper.getMainLooper()).postDelayed(() -> configureTabLayout(), 300);
//                    }
//                }
//
//           /* @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.hasExtra("JOB")) {
//                    int flagType = intent.getIntExtra("JOB", AppConstants.SYNC_PULL_DATA_DONE);
//                    if (flagType == AppConstants.SYNC_PULL_DATA_DONE ||
//                            flagType == AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE) {
//                            CustomLog.v(TAG, "Sync Done!");
//                            if (!isFinishing()) {
//                                refresh.clearAnimation();
//                                syncAnimator.cancel();
//                            }
//                            configureTabLayout();
//                    }
//                }*/
//
//                //just stopping the progressbar here if sync is failed
//                if (intent.hasExtra(AppConstants.SYNC_INTENT_DATA_KEY)) {
//                    int flagType = intent.getIntExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED);
//                    if (flagType == AppConstants.SYNC_FAILED) {
//                        refresh.clearAnimation();
//                        syncAnimator.cancel();
//                        hideProgressbar();
//                    }
//                }
//            }
//        };
//        IntentFilter filterSend = new IntentFilter();
//        filterSend.addAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
//        ContextCompat.registerReceiver(
//                this,
//                mBroadcastReceiver,
//                filterSend,
//                ContextCompat.RECEIVER_NOT_EXPORTED
//        );

        syncAnimator = ObjectAnimator.ofFloat(refresh, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setRepeatCount(ValueAnimator.INFINITE);
        syncAnimator.setInterpolator(new LinearInterpolator());

        if (commonLoadingDialog == null) {
            commonLoadingDialog = new DialogUtils().showCommonLoadingDialog(this, getString(R.string.loading), "");
            commonLoadingDialog.setCancelable(false);
        }
        if (!commonLoadingDialog.isShowing()) {
            commonLoadingDialog.show();
        }
    }

   /* @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }*/

/*
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
*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  unregisterReceiver(mBroadcastReceiver);
      //  notificationReceiver.unregisterNotificationReceiver(this);
    }

    public void configureTabLayout() {
        if (refreshCount > 0) return; // Prevent unnecessary updates

        if (tabLayout == null) tabLayout = findViewById(R.id.tablayout_appointments);
        if (viewPager == null) viewPager = findViewById(R.id.pager_appointments);

        if (viewPager.getAdapter() == null) {
            VisitPagerAdapter adapter = new VisitPagerAdapter(this,this);
            viewPager.setAdapter(adapter);

            new TabLayoutMediator(tabLayout, viewPager,
                    (tab, position) -> tab.setText(getResources().getString(
                                    position == 0 ? R.string.received : R.string.pending))
                            .setIcon(R.drawable.presc_tablayout_icon)
            ).attach();

            viewPager.setOffscreenPageLimit(1); // Optimize memory usage
        }
          /*String language = sessionManager.getAppLanguage();
      if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }*/

        hideProgressbar();
        refreshCount++;


    }

   /* private void updateCounts(boolean isForReceivedPrescription) {
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
*/
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
      //  networkUtils.callBroadcastReceiver();
    }

    private void hideProgressbar() {
        if(syncClicked && !this.isFinishing()){
            loadingDialog.dismiss();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        /*try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void receivedCount(int count) {
        CustomLog.v(TAG, "receivedCount: " + count);
        //tabLayout.getTabAt(0).setText(getResources().getString(R.string.received));
//        ThreadingUtils.executeInBackground(updateCounts(true));
        updateCounts(true);
    }

    @Override
    public void pendingCount(int count) {
        CustomLog.v(TAG, "pendingCount: " + count);
        //tabLayout.getTabAt(1).setText(getResources().getString(R.string.pending));
//        ThreadingUtils.executeInBackground(updateCounts(false));
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

   /* public class NotificationReceiver extends BroadcastReceiver { // // TODO: Commented as it was taking heavy load on app and checking network here is not requried.

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
*/
   private void updateCounts(boolean isForReceivedPrescription) {
       new Thread(() -> {
           int count;
           SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
           if (isForReceivedPrescription) {
               count = new HomeScreenQueriesRepository().getReceivedPrescriptionVisitsCount(db);
           } else {
               count = new VisitsDAO().getVisitCountsByStatus(false);
           }
           int finalCount = count;
           runOnUiThread(() -> {
               if (isForReceivedPrescription)
                   Objects.requireNonNull(tabLayout.getTabAt(0)).setText(
                           getResources().getString(R.string.received) + "\t(" + finalCount + ")");
               else
                   Objects.requireNonNull(tabLayout.getTabAt(1)).setText(
                           getResources().getString(R.string.pending) + "\t(" + finalCount + ")");
           });
       }).start();
   }

    @Override
    public void isReceivedRecentLoaded(boolean status) {
        Log.d("CCC","isReceivedRecentLoaded");
        isReceivedRecentLoaded = status;
        if(isReceivedRecentLoaded && isReceivedOldLoaded && isPendingRecentLoaded && isPendingOldLoaded){

            if (commonLoadingDialog.isShowing()) {
                commonLoadingDialog.dismiss();
            }
        }
    }

    @Override
    public void isReceivedOldLoaded(boolean status) {
        isReceivedOldLoaded = status;
        if(isReceivedRecentLoaded && isReceivedOldLoaded && isPendingRecentLoaded && isPendingOldLoaded){

            if (commonLoadingDialog.isShowing()) {
                commonLoadingDialog.dismiss();
            }
        }
    }

    @Override
    public void isPendingRecentLoaded(boolean status) {
        isPendingRecentLoaded = status;
        if(isReceivedRecentLoaded && isReceivedOldLoaded && isPendingRecentLoaded && isPendingOldLoaded){

            if (commonLoadingDialog.isShowing()) {
                commonLoadingDialog.dismiss();
            }
        }
    }

    @Override
    public void isPendingOldLoaded(boolean status) {
        isPendingOldLoaded = status;
        if(isReceivedRecentLoaded && isReceivedOldLoaded && isPendingRecentLoaded && isPendingOldLoaded){

            if (commonLoadingDialog.isShowing()) {
                commonLoadingDialog.dismiss();
            }
        }
    }
    public interface OnFeatureStatusReadyListener {
        void onFeatureStatusReady(FeatureActiveStatus status);
    }
    private OnFeatureStatusReadyListener featureStatusListener;

    public void setFeatureStatusListener(OnFeatureStatusReadyListener listener) {
        this.featureStatusListener = listener;
        if (mFeatureActiveStatus != null) {
            listener.onFeatureStatusReady(mFeatureActiveStatus);
        }
    }
}
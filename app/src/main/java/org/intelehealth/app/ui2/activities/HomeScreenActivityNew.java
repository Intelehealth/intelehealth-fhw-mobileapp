package org.intelehealth.app.ui2.activities;

import static org.intelehealth.app.utilities.StringUtils.en__as_dob;
import static org.intelehealth.app.utilities.StringUtils.en__bn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__gu_dob;
import static org.intelehealth.app.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.app.utilities.StringUtils.en__kn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ml_dob;
import static org.intelehealth.app.utilities.StringUtils.en__mr_dob;
import static org.intelehealth.app.utilities.StringUtils.en__or_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ru_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ta_dob;
import static org.intelehealth.app.utilities.StringUtils.en__te_dob;
import static org.intelehealth.app.utilities.StringUtils.getFullMonthName;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import com.google.android.material.navigation.NavigationView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.CheckAppUpdateRes;
import org.intelehealth.app.services.firebase_services.CallListenerBackgroundService;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.ui2.fragments.HomeFragment;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.apprtc.data.Manager;
import org.intelehealth.apprtc.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;
import me.piruin.quickaction.QuickIntentAction;

public class HomeScreenActivityNew extends AppCompatActivity {
    private static final String TAG = "HomeScreenActivity";
    ImageView imageViewIsInternet;
    private boolean isConnected = false;
    private static final int ID_DOWN = 2;
    private QuickAction quickAction;
    private QuickAction quickIntent;
    private DrawerLayout mDrawerLayout;
    SessionManager sessionManager;
    Dialog dialogLoginSuccess;
    NavigationView mNavigationView;
    private int versionCode = 0;
    private ProgressDialog mSyncProgressDialog, mRefreshProgressDialog, mResetSyncDialog;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ObjectAnimator syncAnimator;
    SyncUtils syncUtils = new SyncUtils();
    int i = 5;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_ui2);
        context = HomeScreenActivityNew.this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }
        sessionManager = new SessionManager(this);

        initUI();



    }

    private void initUI() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        TextView tvAppVersion = findViewById(R.id.tv_app_version);
        LinearLayout menuResetApp = findViewById(R.id.layout_reset_app);

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            //drawer is open
            //  getWindow().setStatusBarColor(Color.CYAN);

        }


         TextView tvLocation = findViewById(R.id.tv_user_location_home);
        tvLocation.setText(sessionManager.getLocationName());

        imageViewIsInternet = findViewById(R.id.imageview_is_internet);
        ImageView ivHamburger = findViewById(R.id.iv_hamburger);
        ivHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);

            }
        });
        isNetworkAvailable(this);

        imageViewIsInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction.show(v);
            }
        });

        //nav header
        mNavigationView = findViewById(R.id.navigationview);
        View headerView = mNavigationView.getHeaderView(0);
        ImageView ivCloseDrawer = headerView.findViewById(R.id.iv_close_drawer);
        ImageView ivProfileIcon = headerView.findViewById(R.id.iv_profile_icon);
        TextView tvUsername = headerView.findViewById(R.id.tv_loggedin_username);
        TextView tvUserId = headerView.findViewById(R.id.tv_userid);
        TextView tvEditProfile = headerView.findViewById(R.id.tv_edit_profile);


        ivCloseDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);

            }
        });
        setupDrawerContent(mNavigationView);

        //code from old home activity

        syncAnimator = ObjectAnimator.ofFloat(imageViewIsInternet, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setRepeatCount(ValueAnimator.INFINITE);
        syncAnimator.setInterpolator(new LinearInterpolator());
        imageViewIsInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AppConstants.notificationUtils.showNotifications(getString(R.string.sync), getString(R.string.syncInProgress), 1, context);

                if (isNetworkConnected()) {
                    Toast.makeText(context, getString(R.string.syncInProgress), Toast.LENGTH_LONG).show();
                    imageViewIsInternet.clearAnimation();
                    syncAnimator.start();
                    syncUtils.syncForeground("home");
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
                }
//                if (!sessionManager.getLastSyncDateTime().equalsIgnoreCase("- - - -")
//                        && Locale.getDefault().toString().equalsIgnoreCase("en")) {
//                    lastSyncAgo.setText(sessionManager.getLastTimeAgo());
//                }
            }
        });
        //WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
        if (sessionManager.isFirstTimeLaunched()) {
            mSyncProgressDialog = new ProgressDialog(HomeScreenActivityNew.this, R.style.AlertDialogStyle); //thats how to add a style!
            mSyncProgressDialog.setTitle(R.string.syncInProgress);
            mSyncProgressDialog.setCancelable(false);
            mSyncProgressDialog.setProgress(i);
            mSyncProgressDialog.show();

            syncUtils.initialSync("home");
        } else {
            // if initial setup done then we can directly set the periodic background sync job
            WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
            saveToken();
            // requestPermission();
        }
        /*sessionManager.setMigration(true);

        if (sessionManager.isReturningUser()) {
            syncUtils.syncForeground("");
        }*/

    }

    private void checkForInternet() {
        boolean result = NetworkConnection.isOnline(this);
        Log.d(TAG, "checkForInternet: result : " + result);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void isNetworkAvailable(Context context) {
        int flag = 0;

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {

                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            if (imageViewIsInternet != null) {
                                imageViewIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
                                flag = 1;
                                //  setTooltipForInternet("Good internet.\nRefresh");

                            }
                        }
                    }
                }
            }
        }

        if (flag == 0) {
            if (imageViewIsInternet != null) {
                imageViewIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

                //  setTooltipForInternet("No internet");
            }

        }

    }

    private void setTooltipForInternet(String message) {
        QuickAction.setDefaultColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
        QuickAction.setDefaultTextColor(Color.BLACK);

        ActionItem nextItem = new ActionItem(ID_DOWN, message);
        quickAction = new QuickAction(this, QuickAction.HORIZONTAL);
        quickAction.setColorRes(R.color.white);
        quickAction.setTextColorRes(R.color.textColorBlack);
        quickAction.addActionItem(nextItem);
        quickAction.setTextColor(Color.BLACK);


        //Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(ActionItem item) {
                //here we can filter which action item was clicked with pos or actionId parameter
                String title = item.getTitle();
                Toast.makeText(HomeScreenActivityNew.this, title + " selected", Toast.LENGTH_SHORT).show();
                if (!item.isSticky()) quickAction.remove(item);
            }
        });

        quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
                // Toast.makeText(HomeScreenActivity.this, "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        quickIntent = new QuickIntentAction(this)
                .setActivityIntent(sendIntent)
                .create();
        quickIntent.setAnimStyle(QuickAction.Animation.REFLECT);
    }


    private void loadFragment(Fragment fragment) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showLoggingInDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(HomeScreenActivityNew.this);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(HomeScreenActivityNew.this);
        View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_login_success, null);
        builder.setView(customLayout);

        dialogLoginSuccess = builder.create();
        dialogLoginSuccess.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialogLoginSuccess.show();
        int width = getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        dialogLoginSuccess.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogLoginSuccess.dismiss();
            }
        }, 2000);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass = null;
        switch (menuItem.getItemId()) {
            case R.id.menu_my_achievements:
                break;
            case R.id.menu_video_lib:

                break;
            case R.id.menu_change_language:

                break;
            case R.id.menu_about_us:

                break;
            case R.id.menu_logout:

                break;
            default:
        }

      /*  try {

            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flContent, fragment).commit();
        String backStateName = fragment.getClass().getName();
        fragmentTransaction.addToBackStack(backStateName);*/

        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onResume() {
        Fragment fragment = new HomeFragment();
        loadFragment(fragment);

        showLoggingInDialog();
        //registerReceiver(reMyreceive, filter);
        checkAppVer();  //auto-update feature.
//        lastSyncTextView.setText(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
        if (!sessionManager.getLastSyncDateTime().equalsIgnoreCase("- - - -")
                && Locale.getDefault().toString().equals("en")) {
//            lastSyncAgo.setText(CalculateAgoTime());
        }


        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(AppConstants.SYNC_INTENT_ACTION);
        registerReceiver(syncBroadcastReceiver, filter);
        //showBadge();
    }

    private void checkAppVer() {

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        disposable.add((Disposable) AppConstants.apiInterface.checkAppUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<CheckAppUpdateRes>() {
                    @Override
                    public void onSuccess(CheckAppUpdateRes res) {
                        int latestVersionCode = 0;
                        if (!res.getLatestVersionCode().isEmpty()) {
                            latestVersionCode = Integer.parseInt(res.getLatestVersionCode());
                        }

                        if (latestVersionCode > versionCode) {
                            android.app.AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new android.app.AlertDialog.Builder(HomeScreenActivityNew.this, android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new android.app.AlertDialog.Builder(HomeScreenActivityNew.this);
                            }


                            builder.setTitle(getResources().getString(R.string.new_update_available))
                                    .setCancelable(false)
                                    .setMessage(getResources().getString(R.string.update_app_note))
                                    .setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }

                                        }
                                    })

                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setCancelable(false);

                            Dialog dialog = builder.show();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                                TextView tv = (TextView) dialog.findViewById(textViewId);
                                tv.setTextColor(getResources().getColor(R.color.white));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Error", "" + e);
                    }
                })
        );

    }

    private List<Integer> mTempSyncHelperList = new ArrayList<Integer>();

    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD("syncBroadcastReceiver", "onReceive! " + intent);

            if (intent != null && intent.hasExtra(AppConstants.SYNC_INTENT_DATA_KEY)) {
                int flagType = intent.getIntExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED);
                if (sessionManager.isFirstTimeLaunched()) {
                    if (flagType == AppConstants.SYNC_FAILED) {
                        hideSyncProgressBar(false);
                        /*Toast.makeText(context, R.string.failed_synced, Toast.LENGTH_SHORT).show();
                        finish();*/
                        new AlertDialog.Builder(HomeScreenActivityNew.this)
                                .setMessage(R.string.failed_initial_synced)
                                .setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }

                                }).setCancelable(false)

                                .show();
                    } else {
                        mTempSyncHelperList.add(flagType);
                        if (mTempSyncHelperList.contains(AppConstants.SYNC_PULL_DATA_DONE)
//                                && mTempSyncHelperList.contains(AppConstants.SYNC_PUSH_DATA_DONE)
                                /*&& mTempSyncHelperList.contains(AppConstants.SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE)
                                && mTempSyncHelperList.contains(AppConstants.SYNC_OBS_IMAGE_PUSH_DONE)*/) {
                            hideSyncProgressBar(true);
                        }
                    }
                    // showBadge();
                }
            }

            setLastSyncTime(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
//            lastSyncTextView.setText(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
//          lastSyncAgo.setText(sessionManager.getLastTimeAgo());

            if (syncAnimator != null && syncAnimator.getCurrentPlayTime() > 200) {
                syncAnimator.cancel();
                syncAnimator.end();
            }
        }
    };

    private void hideSyncProgressBar(boolean isSuccess) {
        saveToken();
        // requestPermission();
        if (mTempSyncHelperList != null) mTempSyncHelperList.clear();
        if (mSyncProgressDialog != null && mSyncProgressDialog.isShowing()) {
            mSyncProgressDialog.dismiss();
            if (isSuccess) {
                saveToken();
                sessionManager.setFirstTimeLaunched(false);
                sessionManager.setMigration(true);
                // initial setup/sync done and now we can set the periodic background sync job
                // given some delay after initial sync
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
                    }
                }, 10000);
            }
        }

    }

    private void saveToken() {
        Manager.getInstance().setBaseUrl("https://" + sessionManager.getServerUrl());
        // save fcm reg. token for chat (Video)
        FirebaseUtils.saveToken(this, sessionManager.getProviderID(), IntelehealthApplication.getInstance().refreshedFCMTokenID, sessionManager.getAppLanguage());
    }

    private void setLastSyncTime(String dob) {
        String convertedString = getFullMonthName(dob);

        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            String sync_text = en__hi_dob(convertedString); //to show text of English into Hindi...
            //lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            String sync_text = en__or_dob(convertedString); //to show text of English into Odiya...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            String sync_text = en__bn_dob(convertedString); //to show text of English into Odiya...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            String sync_text = en__gu_dob(convertedString); //to show text of English into Gujarati...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            String sync_text = en__te_dob(convertedString); //to show text of English into telugu...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            String sync_text = en__mr_dob(convertedString); //to show text of English into telugu...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            String sync_text = en__as_dob(convertedString); //to show text of English into telugu...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            String sync_text = en__ml_dob(convertedString); //to show text of English into telugu...
            //   lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            String sync_text = en__kn_dob(convertedString); //to show text of English into telugu...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
            String sync_text = en__ru_dob(convertedString); //to show text of English into Russian...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            String sync_text = en__ta_dob(convertedString); //to show text of English into Tamil...
            // lastSyncTextView.setText(sync_text);
        } else {
            // lastSyncTextView.setText(dob);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(syncBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Is BG Service On - " + CallListenerBackgroundService.isInstanceCreated());
        if (!CallListenerBackgroundService.isInstanceCreated()) {
            Intent serviceIntent = new Intent(this, CallListenerBackgroundService.class);
            context.startService(serviceIntent);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}


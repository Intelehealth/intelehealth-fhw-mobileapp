package org.intelehealth.app.activities.homeActivity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;
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
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.BuildConfig;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.aboutus.AboutUsActivity;
import org.intelehealth.app.activities.achievements.fragments.MyAchievementsFragment;
import org.intelehealth.app.activities.help.activities.HelpFragment_New;
import org.intelehealth.app.activities.informativeVideos.fragments.InformativeVideosFragment_New;
import org.intelehealth.app.activities.loginActivity.LoginActivityNew;
import org.intelehealth.app.activities.notification.NotificationActivity;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.activities.settingsActivity.Language_ProtocolsActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointmentNew.UpdateFragmentOnEvent;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ProviderAttributeDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.models.CheckAppUpdateRes;
import org.intelehealth.app.models.dto.ProviderAttributeDTO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.profile.MyProfileActivity;
import org.intelehealth.app.services.firebase_services.CallListenerBackgroundService;
import org.intelehealth.app.services.firebase_services.DeviceInfoUtils;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.OfflineLogin;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.TooltipWindow;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.apprtc.ChatActivity;
import org.intelehealth.apprtc.CompleteActivity;
import org.intelehealth.apprtc.data.Manager;
import org.intelehealth.apprtc.utils.FirebaseUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class HomeScreenActivity_New extends AppCompatActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "HomeScreenActivity";
    ImageView imageViewIsInternet, ivHamburger, imageview_notifications_home;
    private boolean isConnected = false;
    private static final int ID_DOWN = 2;
    private DrawerLayout mDrawerLayout;
    SessionManager sessionManager;
    Dialog dialogLoginSuccess, dialogRefreshInProgress;
    NavigationView mNavigationView;
    private int versionCode = 0;
    private ProgressDialog mSyncProgressDialog, mRefreshProgressDialog, mResetSyncDialog;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ObjectAnimator syncAnimator;
    SyncUtils syncUtils = new SyncUtils();
    int i = 5;
    Context context;
    TextView tvTitleHomeScreenCommon, tvAppLastSync;
    BottomNavigationView bottomNav;
    private CardView survey_snackbar_cv;
    ImageView imageViewIsNotification, ivCloseDrawer, ivProfileIcon;
    TextView tvEditProfile, tvAppVersion, tvUsername, tvUserId;
    LinearLayout menuResetApp;
    private static final String ACTION_NAME = "org.intelehealth.app.RTC_MESSAGING_EVENT";
    String firstLogin = "";
    View toolbarHome;
    NetworkUtils networkUtils;
    String currentFragment;
    private AlertDialog resetDialog;
    TooltipWindow tipWindow;
    private MaterialAlertDialogBuilder resetAlertDialogBuilder;

    private static final String TAG_HOME = "TAG_HOME";
    private static final String TAG_ACHIEVEMENT = "TAG_ACHIEVEMENT";
    private static final String TAG_HELP = "TAG_HELP";

    private void saveToken() {
        Manager.getInstance().setBaseUrl("https://" + sessionManager.getServerUrl());
        // save fcm reg. token for chat (Video)
        FirebaseUtils.saveToken(this, sessionManager.getProviderID(), IntelehealthApplication.getInstance().refreshedFCMTokenID, sessionManager.getAppLanguage());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "onNewIntent");
        catchFCMMessageData();
    }

    private void catchFCMMessageData() {
        // get the chat notification click info
        if (getIntent().getExtras() != null) {
            Logger.logV(TAG, " getIntent - " + getIntent().getExtras().getString("actionType"));
            Bundle remoteMessage = getIntent().getExtras();
            try {
                if (remoteMessage.containsKey("actionType") && remoteMessage.getString("actionType").equals("TEXT_CHAT")) {
                    //Log.d(TAG, "actionType : TEXT_CHAT");
                    String fromUUId = remoteMessage.getString("toUser");
                    String toUUId = remoteMessage.getString("fromUser");
                    String patientUUid = remoteMessage.getString("patientId");
                    String visitUUID = remoteMessage.getString("visitId");
                    String patientName = remoteMessage.getString("patientName");
                    JSONObject connectionInfoObject = new JSONObject();
                    connectionInfoObject.put("fromUUID", fromUUId);
                    connectionInfoObject.put("toUUID", toUUId);
                    connectionInfoObject.put("patientUUID", patientUUid);

                    Intent intent = new Intent(ACTION_NAME);
                    intent.putExtra("visit_uuid", visitUUID);
                    intent.putExtra("connection_info", connectionInfoObject.toString());
                    intent.setComponent(new ComponentName("org.intelehealth.unicef", "org.intelehealth.unicef.utilities.RTCMessageReceiver"));
                    getApplicationContext().sendBroadcast(intent);

                    Intent chatIntent = new Intent(this, ChatActivity.class);
                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    chatIntent.putExtra("patientName", patientName);
                    chatIntent.putExtra("visitUuid", visitUUID);
                    chatIntent.putExtra("patientUuid", patientUUid);
                    chatIntent.putExtra("fromUuid", fromUUId);
                    chatIntent.putExtra("toUuid", toUUId);
                    startActivity(chatIntent);

                } else if (remoteMessage.containsKey("actionType") && remoteMessage.getString("actionType").equals("VIDEO_CALL")) {
                    //Log.d(TAG, "actionType : VIDEO_CALL");
                    Intent in = new Intent(this, CompleteActivity.class);
                    String roomId = remoteMessage.getString("roomId");
                    String doctorName = remoteMessage.getString("doctorName");
                    String nurseId = remoteMessage.getString("nurseId");
                    String visitId = remoteMessage.getString("visitId");
                    String doctorId = remoteMessage.getString("doctorId");
                    boolean isOldNotification = false;
                    if (remoteMessage.containsKey("timestamp")) {
                        String timestamp = remoteMessage.getString("timestamp");

                        Date date = new Date();
                        if (timestamp != null) {
                            date.setTime(Long.parseLong(timestamp));
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss"); //this format changeable
                            dateFormatter.setTimeZone(TimeZone.getDefault());

                            try {
                                Date ourDate = dateFormatter.parse(dateFormatter.format(date));
                                long seconds = 0;
                                if (ourDate != null) {
                                    seconds = Math.abs(new Date().getTime() - ourDate.getTime()) / 1000;
                                }
                                Log.v(TAG, "Current time - " + new Date());
                                Log.v(TAG, "Notification time - " + ourDate);
                                Log.v(TAG, "seconds - " + seconds);
                                if (seconds >= 30) {
                                    isOldNotification = true;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    in.putExtra("roomId", roomId);
                    in.putExtra("isInComingRequest", true);
                    in.putExtra("doctorname", doctorName);
                    in.putExtra("nurseId", nurseId);
                    in.putExtra("visitId", visitId);
                    in.putExtra("doctorId", doctorId);

                    int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                    if (callState == TelephonyManager.CALL_STATE_IDLE && !isOldNotification) {
                        startActivity(in);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private UpdateFragmentOnEvent mUpdateFragmentOnEvent;

    public void initUpdateFragmentOnEvent(UpdateFragmentOnEvent listener) {
        Log.v(TAG, "initUpdateFragmentOnEvent");
        mUpdateFragmentOnEvent = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_ui2);
        context = HomeScreenActivity_New.this;
        networkUtils = new NetworkUtils(context, this);
        DeviceInfoUtils.saveDeviceInfo(this);
        catchFCMMessageData();


        loadFragment(new HomeFragment_New(), TAG_HOME);
        // FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        // tx.replace(R.id.fragment_container, new HomeFragment_New());
        // tx.commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }
        sessionManager = new SessionManager(this);
        initUI();
        //}
        clickListeners();
//        awsAuth();

    }

    private void clickListeners() {
        Intent intent_exit = getIntent();
        if (intent_exit != null) {
            String intentTag = intent_exit.getStringExtra("intentTag");
            if (intentTag != null) {
                if (intentTag.equalsIgnoreCase("Feedback screen")) showSnackBarAndRemoveLater();
                else survey_snackbar_cv.setVisibility(View.GONE);
            }
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            //drawer is open
            //  getWindow().setStatusBarColor(Color.CYAN);
        }


        /*tvTitleHomeScreenCommon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity_New.this, MyAppointmentActivity.class);
                startActivity(intent);
            }
        });*/

        imageViewIsNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity_New.this, NotificationActivity.class);
                startActivity(intent);
            }
        });


        imageview_notifications_home.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
        });

        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);

               /* tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_profile));
                Fragment fragment = new MyProfileFragment_New();
                loadFragment(fragment);*/
                Intent intent = new Intent(HomeScreenActivity_New.this, MyProfileActivity.class);
                startActivity(intent);
            }
        });


        ivCloseDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);

            }
        });

        // Reset's App...
        menuResetApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                resetApp();
            }
        });
    }

    private void resetApp() {
        // to insert time spent by user into the db
        insertTimeSpentByUserIntoDb();
        showResetConfirmationDialog();
    }

    private void showResetConfirmationDialog() {
        patientRegistrationDialog(context, getResources().getDrawable(R.drawable.ui2_ic_warning_internet), getString(R.string.reset_app_new_title), getString(R.string.sure_to_reset_app), getString(R.string.generic_yes), getString(R.string.no), action -> {
            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                checkNetworkConnectionAndPerformSync();
                //  showSimpleDialog(getString(R.string.resetting_app_dialog), getString(R.string.please_wait_app_reset));
//                            deleteCache(getApplicationContext());
/*
                        new Handler(Looper.getMainLooper())
                                .postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        resetDialog.dismiss();
                                        deleteCache(getApplicationContext());
                                    }
                                }, 2000);
*/
            }
        });
    }

    private void checkNetworkConnectionAndPerformSync() {
        if ((isNetworkConnected())) {

            // first we're showing the sync in progress dialog - Added by Arpan Sircar
            showSimpleDialog(resetAlertDialogBuilder, getString(R.string.app_sync_dialog_title), getString(R.string.please_wait_sync_progress), getResources().getDrawable(R.drawable.ui2_icon_logging_in));

            boolean isSynced = syncUtils.syncForeground("home");
            if (isSynced) {

                new Handler().postDelayed(() -> {

                    // next we're displaying the sync successful message - Added by Arpan Sircar
                    updateSimpleDialog(resetDialog, getString(R.string.sync_successful), getString(R.string.please_wait_app_reset), getResources().getDrawable(R.drawable.ui2_icon_login_success));

                    new Handler().postDelayed(() -> {

                        // finally, we'll be displaying the reset dialog - Added by Arpan Sircar
                        showResetProgressbar();

                        new Handler().postDelayed(() -> { //Do something after 100ms
                            Toast.makeText(getApplicationContext(), getString(R.string.app_reset_successfully), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), getString(R.string.please_open_the_app_again), Toast.LENGTH_SHORT).show();
                            deleteCache(getApplicationContext());
                        }, 1000);

                    }, 2000);
                }, 4000);

            } else {
                // mResetSyncDialog.dismiss();
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showOkDialog(this, getString(R.string.error), getString(R.string.sync_failed), getString(R.string.generic_ok));
            }
        } else {
            MaterialAlertDialogBuilder builder = new DialogUtils().showErrorDialogWithTryAgainButton(this, getDrawable(R.drawable.ui2_icon_logging_in), getString(R.string.network_failure), getString(R.string.reset_app_requires_internet_message), getString(R.string.try_again));
            AlertDialog networkFailureDialog = builder.show();

            networkFailureDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
            networkFailureDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
            int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
            networkFailureDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

            Button tryAgainButton = networkFailureDialog.findViewById(R.id.positive_btn);
            if (tryAgainButton != null) tryAgainButton.setOnClickListener(v -> {
                networkFailureDialog.dismiss();
                checkNetworkConnectionAndPerformSync();
            });
        }
    }

    private void showResetProgressbar() {
        resetDialog.dismiss();
        MaterialAlertDialogBuilder resetDialogBuilder = new MaterialAlertDialogBuilder(context);
        showSimpleDialog(resetDialogBuilder, getString(R.string.resetting_app_dialog), getString(R.string.please_wait_app_reset), getResources().getDrawable(R.drawable.ui2_icon_logging_in));
    }

    public void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            boolean success = deleteDir(dir);
            if (success) {
                clearAppData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void clearAppData() {
        resetDialog.dismiss();
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSimpleDialog(MaterialAlertDialogBuilder alertDialog, String title, String subtitle, Drawable dialogIcon) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
        alertDialog.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);

        icon.setImageDrawable(dialogIcon);
        dialog_title.setText(title);
        dialog_subtitle.setText(subtitle);
        positive_btn.setVisibility(View.GONE);
        negative_btn.setVisibility(View.GONE);

        resetDialog = alertDialog.create();
        resetDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        resetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        resetDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        resetDialog.setCancelable(false);
        resetDialog.show();
    }

    private void updateSimpleDialog(Dialog dialog, String title, String subtitle, Drawable dialogIcon) {
        ImageView icon = dialog.findViewById(R.id.dialog_icon);
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        TextView dialogSubtitle = dialog.findViewById(R.id.dialog_subtitle);

        icon.setImageDrawable(dialogIcon);
        dialogTitle.setText(title);
        dialogSubtitle.setText(subtitle);
    }

    private void initUI() {
        tipWindow = new TooltipWindow(HomeScreenActivity_New.this);
        resetAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        survey_snackbar_cv = findViewById(R.id.survey_snackbar_cv);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        tvAppVersion = findViewById(R.id.tv_app_version);
        menuResetApp = findViewById(R.id.layout_reset_app);
        imageview_notifications_home = findViewById(R.id.imageview_notifications_home);

        toolbarHome = findViewById(R.id.toolbar_home);

        tvTitleHomeScreenCommon = toolbarHome.findViewById(R.id.tv_user_location_home);
        tvAppLastSync = toolbarHome.findViewById(R.id.tv_app_sync_time);

        imageViewIsInternet = toolbarHome.findViewById(R.id.imageview_is_internet);
        imageViewIsNotification = toolbarHome.findViewById(R.id.imageview_notifications_home);

        ivHamburger = toolbarHome.findViewById(R.id.iv_hamburger);
        ivHamburger.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_hamburger));

        ivHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: icon clicked");
                mDrawerLayout.openDrawer(GravityCompat.START);

            }
        });

        //isNetworkAvailable(this);

        //nav header
        mNavigationView = findViewById(R.id.navigationview);
        View headerView = mNavigationView.getHeaderView(0);
        ivCloseDrawer = headerView.findViewById(R.id.iv_close_drawer);
        ivProfileIcon = headerView.findViewById(R.id.iv_profile_icon);
        tvUsername = headerView.findViewById(R.id.tv_loggedin_username);
        tvUserId = headerView.findViewById(R.id.tv_userid);
        tvEditProfile = headerView.findViewById(R.id.tv_edit_profile);

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
//                    Toast.makeText(context, getString(R.string.syncInProgress), Toast.LENGTH_LONG).show();
                    imageViewIsInternet.clearAnimation();
                    syncAnimator.start();
                    syncUtils.syncForeground("home");
                } else {
//                    Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
                    if (!tipWindow.isTooltipShown())
                        tipWindow.showToolTip(imageViewIsInternet, getResources().getString(R.string.no_network_tooltip));
//                    showRefreshFailedDialog();
                }
//                if (!sessionManager.getLastSyncDateTime().equalsIgnoreCase("- - - -")
//                        && Locale.getDefault().toString().equalsIgnoreCase("en")) {
//                    lastSyncAgo.setText(sessionManager.getLastTimeAgo());
//                }
            }
        });
        //WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
        if (sessionManager.isFirstTimeLaunched()) {
            /*mSyncProgressDialog = new ProgressDialog(HomeScreenActivity_New.this, R.style.AlertDialogStyle); //thats how to add a style!
            mSyncProgressDialog.setTitle(R.string.syncInProgress);
            mSyncProgressDialog.setCancelable(false);
            mSyncProgressDialog.setProgress(i);
            mSyncProgressDialog.show();*/
            showRefreshInProgressDialog();
            syncUtils.initialSync("home");
        } else {
            // if initial setup done then we can directly set the periodic background sync job
            WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
            saveToken();
            requestPermission();
        }
        /*sessionManager.setMigration(true);

        if (sessionManager.isReturningUser()) {
            syncUtils.syncForeground("");
        }*/

        //bottom nav
        bottomNav = findViewById(R.id.bottom_nav_home);
        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        bottomNav.setItemIconTintList(null);
        bottomNav.getMenu().findItem(R.id.bottom_nav_home_menu).setChecked(true);


       /* String sync_text = setLastSyncTime(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
        tvAppLastSync.setText(sync_text);
        Log.d(TAG, "onReceive: sync_text initui : " + sessionManager.getLastSyncDateTime());
*/

        tvAppVersion.setText(getString(R.string.app_version_string, BuildConfig.VERSION_NAME));


    }

    private void showSnackBarAndRemoveLater() {
        survey_snackbar_cv.setVisibility(View.VISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                survey_snackbar_cv.setVisibility(View.GONE);
            }
        }, 4000);
    }

    private void checkForInternet() {
        boolean result = NetworkConnection.isOnline(this);
        Log.d(TAG, "checkForInternet: result : " + result);
    }

    @Override
    public void onBackPressed() {
        //HomeFragment_New
        //MyAchievementsFragmentNew

        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        Log.v(TAG, "backStackEntryCount - " + backStackEntryCount);
        String topFragmentTag = getTopFragmentTag();
        if (topFragmentTag.equalsIgnoreCase(TAG_HOME)) {
            // finish();
            wantToExitApp(this, getResources().getString(R.string.exit_app), getResources().getString(R.string.sure_to_exit), getResources().getString(R.string.yes), getResources().getString(R.string.no));

        } else {
            //super.onBackPressed();
            getSupportFragmentManager().popBackStackImmediate(topFragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadLastSelectedFragment();
        }
    }

    private Fragment getTopFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        return getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    private String getTopFragmentTag() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return "";
        }
        String topFragment = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        Log.v(TAG, topFragment);
        return topFragment;

    }

    public void wantToExitApp(Context context, String title, String subTitle, String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.iv_dialog_image);
        TextView dialog_title = convertView.findViewById(R.id.tv_title_book_app);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ui2_ic_exit_app));

        dialog_title.setText(title);
        tvInfo.setText(Html.fromHtml(subTitle));
        yesButton.setText(positiveBtnTxt);
        noButton.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            moveTaskToBack(true);


        });

        alertDialog.show();
    }

    public void wantToLogoutFromApp(Context context, String title, String subTitle, String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.iv_dialog_image);
        TextView dialog_title = convertView.findViewById(R.id.tv_title_book_app);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ui2_ic_exit_app));

        dialog_title.setText(title);
        tvInfo.setText(Html.fromHtml(subTitle));
        yesButton.setText(positiveBtnTxt);
        noButton.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            logout();

            if (CallListenerBackgroundService.isInstanceCreated()) {
                Intent serviceIntent = new Intent(this, CallListenerBackgroundService.class);
                context.stopService(serviceIntent);
            }

        });

        alertDialog.show();
    }

    public void showRefreshInProgressDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(HomeScreenActivity_New.this);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(HomeScreenActivity_New.this);
        View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_refresh, null);
        builder.setView(customLayout);
        dialogRefreshInProgress = builder.create();
        dialogRefreshInProgress.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialogRefreshInProgress.show();
        int width = getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        dialogRefreshInProgress.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogRefreshInProgress.dismiss();
            }
        }, 3000);
    }

    public void showRefreshFailedDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(HomeScreenActivity_New.this);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(HomeScreenActivity_New.this);
        View customLayout = inflater.inflate(R.layout.ui2_layout_dialog_failed_refresh, null);
        builder.setView(customLayout);
        dialogRefreshInProgress = builder.create();
        dialogRefreshInProgress.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        dialogRefreshInProgress.show();
        int width = getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        dialogRefreshInProgress.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogRefreshInProgress.dismiss();
            }
        }, 3000);
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

    private String mLastTag = "";

    private void loadFragment(Fragment fragment, String tag) {

        if (fragment != null) {
            if (mLastTag.equalsIgnoreCase(tag)) {
                getSupportFragmentManager().popBackStackImmediate(tag, 0);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
            mLastTag = tag;

        }
    }

    /*private void loadFragmentForBottomNav(Fragment fragment) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
        *//*boolean fragmentPopped = getSupportFragmentManager().popBackStackImmediate(tag, 0);

        if (!fragmentPopped) {
            transaction.addToBackStack(null);
            transaction.commit();
        }*//*
    }*/

    public void showLoggingInDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity_New.this);
        builder.setCancelable(false);
        LayoutInflater inflater = LayoutInflater.from(HomeScreenActivity_New.this);
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
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return false;
            }
        });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass = null;
        String tag = "";
        switch (menuItem.getItemId()) {
            case R.id.menu_my_achievements:
                tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_achievements));
                tvTitleHomeScreenCommon.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tvAppLastSync.setVisibility(View.GONE);
                ivHamburger.setVisibility(View.GONE);
                imageViewIsInternet.setVisibility(View.VISIBLE);
                imageViewIsNotification.setVisibility(View.GONE);
                fragment = new MyAchievementsFragment();
                tag = TAG_ACHIEVEMENT;
                break;
            case R.id.menu_video_lib:
                tvTitleHomeScreenCommon.setText(getResources().getString(R.string.videos));
                fragment = new InformativeVideosFragment_New();
                break;
            case R.id.menu_change_language:
                Intent intent = new Intent(HomeScreenActivity_New.this, Language_ProtocolsActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_about_us:
                Intent i = new Intent(HomeScreenActivity_New.this, AboutUsActivity.class);
                startActivity(i);
                break;
            case R.id.menu_logout:
                wantToLogoutFromApp(this, getResources().getString(R.string.menu_option_logout), getResources().getString(R.string.sure_to_logout), getResources().getString(R.string.yes), getResources().getString(R.string.no));
                break;
            default:
        }

        mDrawerLayout.closeDrawers();

        if (fragment != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadFragment(fragment, tag);
            menuItem.setChecked(true);
            // setTitle(menuItem.getTitle());
        }

    }

    @Override
    protected void onResume() {
        if (mIsFirstTimeSyncDone && dialogRefreshInProgress != null && dialogRefreshInProgress.isShowing()) {
            dialogRefreshInProgress.dismiss();
        }
        Log.d(TAG, "check11onResume: home");
        loadLastSelectedFragment();
        //toolbarHome.setVisibility(View.VISIBLE);
        String lastSync = getResources().getString(R.string.last_sync) + ": " + sessionManager.getLastSyncDateTime();
        tvAppLastSync.setText(lastSync);

        //ui2.0 update user details in  nav header
        updateNavHeaderUserDetails();
        firstLogin = getIntent().getStringExtra("firstLogin");
        Log.d(TAG, "onCreate: firstLogin : " + firstLogin);
        if (sessionManager.getIsLoggedIn() && firstLogin != null && !firstLogin.isEmpty() && firstLogin.equalsIgnoreCase("firstLogin")) {
            firstLogin = "";
            getIntent().putExtra("firstLogin", "");

            showLoggingInDialog();

        }


        //registerReceiver(reMyreceive, filter);
        checkAppVer();  //auto-update feature.
        bottomNav.getMenu().findItem(R.id.bottom_nav_home_menu).setChecked(true);

        super.onResume();
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(AppConstants.SYNC_INTENT_ACTION);
        registerReceiver(syncBroadcastReceiver, filter);
        requestPermission();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();

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

        disposable.add((Disposable) AppConstants.apiInterface.checkAppUpdate().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<CheckAppUpdateRes>() {
            @Override
            public void onSuccess(CheckAppUpdateRes res) {
                int latestVersionCode = 0;
                if (!res.getLatestVersionCode().isEmpty()) {
                    latestVersionCode = Integer.parseInt(res.getLatestVersionCode());
                }

                if (latestVersionCode > versionCode) {
                    android.app.AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new android.app.AlertDialog.Builder(HomeScreenActivity_New.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new android.app.AlertDialog.Builder(HomeScreenActivity_New.this);
                    }


                    builder.setTitle(getResources().getString(R.string.new_update_available)).setCancelable(false).setMessage(getResources().getString(R.string.update_app_note)).setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }

                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false);

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
        }));

    }

    private List<Integer> mTempSyncHelperList = new ArrayList<Integer>();
    private boolean mIsFirstTimeSyncDone = false;
    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD("syncBroadcastReceiver", "onReceive! " + intent.hasExtra(AppConstants.SYNC_INTENT_DATA_KEY));

            if (intent.hasExtra(AppConstants.SYNC_INTENT_DATA_KEY)) {
                int flagType = intent.getIntExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED);
                mTempSyncHelperList.add(flagType);
                if (flagType == AppConstants.SYNC_FAILED) {
                    if (sessionManager.isFirstTimeLaunched()) {
                        hideSyncProgressBar(false);
                        showRefreshFailedDialog();
                        //finish();
                    }
                }

                Log.v("syncBroadcastReceiver", new Gson().toJson(mTempSyncHelperList));
                if (mTempSyncHelperList.contains(AppConstants.SYNC_PULL_DATA_DONE) &&
                        mTempSyncHelperList.contains(AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE)) {
                    hideSyncProgressBar(true);
                }
            }

            String sync_text = setLastSyncTime(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
            String lastSync = getResources().getString(R.string.last_sync) + ": " + sessionManager.getLastSyncDateTime();
            tvAppLastSync.setText(lastSync);

            //ui2.0 update user details in  nav header
            updateNavHeaderUserDetails();

//            lastSyncTextView.setText(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
//          lastSyncAgo.setText(sessionManager.getLastTimeAgo());

            if (syncAnimator != null && syncAnimator.getCurrentPlayTime() > 200) {
                syncAnimator.cancel();
                syncAnimator.end();
            }
        }
    };

    private void hideSyncProgressBar(boolean isSuccess) {
        mIsFirstTimeSyncDone = true;
        saveToken();
        requestPermission();
        if (mTempSyncHelperList != null) mTempSyncHelperList.clear();
        if (dialogRefreshInProgress != null && dialogRefreshInProgress.isShowing()) {
            dialogRefreshInProgress.dismiss();
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
        Intent serviceIntent = new Intent(this, CallListenerBackgroundService.class);
        if (!CallListenerBackgroundService.isInstanceCreated()) {
            stopService(serviceIntent);
        }
        startService(serviceIntent);

        mUpdateFragmentOnEvent.onFinished(AppConstants.EVENT_FLAG_SUCCESS);
    }


    private String setLastSyncTime(String dob) {
        String convertedString = getFullMonthName(dob);
        String sync_text = "";

        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            sync_text = en__hi_dob(convertedString); //to show text of English into Hindi...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            sync_text = en__or_dob(convertedString); //to show text of English into Odiya...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            sync_text = en__bn_dob(convertedString); //to show text of English into Odiya...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            sync_text = en__gu_dob(convertedString); //to show text of English into Gujarati...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            sync_text = en__te_dob(convertedString); //to show text of English into telugu...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            sync_text = en__mr_dob(convertedString); //to show text of English into telugu...
            // lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            sync_text = en__as_dob(convertedString); //to show text of English into telugu...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            sync_text = en__ml_dob(convertedString); //to show text of English into telugu...
            //   lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            sync_text = en__kn_dob(convertedString); //to show text of English into telugu...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
            sync_text = en__ru_dob(convertedString); //to show text of English into Russian...
            //  lastSyncTextView.setText(sync_text);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            sync_text = en__ta_dob(convertedString); //to show text of English into Tamil...
            // lastSyncTextView.setText(sync_text);
        } else {
            // lastSyncTextView.setText(dob);
        }
        return sync_text;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(syncBroadcastReceiver);
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 10021;

    private void requestPermission() {
        Intent serviceIntent = new Intent(this, CallListenerBackgroundService.class);
        if (!CallListenerBackgroundService.isInstanceCreated()) {
            //CallListenerBackgroundService.getInstance().stopForegroundService();
            context.startService(serviceIntent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                if (dialogRefreshInProgress != null && dialogRefreshInProgress.isShowing()) {
                    dialogRefreshInProgress.dismiss();
                }
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                //Permission Granted-System will work
            }
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;

            switch (item.getItemId()) {
                case R.id.bottom_nav_home_menu:
                    Log.d(TAG, "onNavigationItemSelected: bottom_nav_home_menu");
                    tvTitleHomeScreenCommon.setText(getResources().getString(R.string.title_home_screen));
                    fragment = new HomeFragment_New();
                    ivHamburger.setVisibility(View.VISIBLE);
                    loadFragment(fragment, TAG_HOME);
                    return true;
                case R.id.bottom_nav_achievements:
                    tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_achievements));
                    tvTitleHomeScreenCommon.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    tvAppLastSync.setVisibility(View.GONE);
                    ivHamburger.setVisibility(View.GONE);
                    imageViewIsInternet.setVisibility(View.VISIBLE);
                    imageViewIsNotification.setVisibility(View.GONE);
                    fragment = new MyAchievementsFragment();
                    //loadFragmentForBottomNav(fragment);
                    loadFragment(fragment, TAG_ACHIEVEMENT);
                    return true;
                case R.id.bottom_nav_help:
                    tvTitleHomeScreenCommon.setText(getResources().getString(R.string.help_center));
                    tvTitleHomeScreenCommon.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    tvAppLastSync.setVisibility(View.GONE);
                    imageViewIsInternet.setVisibility(View.VISIBLE);
                    imageViewIsNotification.setVisibility(View.GONE);
                    ivHamburger.setVisibility(View.GONE);
                    fragment = new HelpFragment_New();
                    //loadFragmentForBottomNav(fragment);
                    loadFragment(fragment, TAG_HELP);
                    return true;
                case R.id.bottom_nav_add_patient:
                    Intent intent = new Intent(HomeScreenActivity_New.this, PrivacyPolicyActivity_New.class);
                    intent.putExtra("add_patient", "add_patient");
                    startActivity(intent);
                    return false;
            }

            return false;
        }
    };


    private void updateNavHeaderUserDetails() {
        try {
            ProviderDAO providerDAO = new ProviderDAO();
            ProviderDTO providerDTO = providerDAO.getLoginUserDetails(sessionManager.getProviderID());
            if (providerDTO != null) {
                boolean firstname = isValidField(providerDTO.getFamilyName());
                boolean lastname = isValidField(providerDTO.getGivenName());
                String userFullName = "";
                if (firstname && lastname) {
                    userFullName = providerDTO.getGivenName() + " " + providerDTO.getFamilyName();
                } else if (firstname) {
                    userFullName = providerDTO.getGivenName();
                } else if (lastname) {
                    userFullName = providerDTO.getFamilyName();

                }
                tvUsername.setText(userFullName);
                tvUserId.setText(getString(R.string.chw_id).concat(" ").concat(sessionManager.getChwname()));

                if (providerDTO.getImagePath() != null && !providerDTO.getImagePath().isEmpty()) {

                    Glide.with(HomeScreenActivity_New.this).load(providerDTO.getImagePath()).thumbnail(0.3f).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileIcon);


                }

                // if imagepath is not available in local db

                if (providerDTO.getImagePath() == null || providerDTO.getImagePath().equalsIgnoreCase("")) {
                    if (NetworkConnection.isOnline(this)) {
                        profilePicDownloaded(providerDTO);
                    }
                }

            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidField(String fieldName) {
        if (fieldName != null && !fieldName.isEmpty() && !fieldName.equals("null")) {
            return true;
        } else {
            return false;
        }
    }

    public void logout() {
        // to insert time spent by user into the db
        insertTimeSpentByUserIntoDb();

        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);

//        parseLogOut();

       /* AccountManager manager = AccountManager.get(HomeActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
     /*   Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.removeAccount(accountList[0], HomeActivity.this, null, null);
            } else {
                manager.removeAccount(accountList[0], null, null); // Legacy implementation
            }
        }
*/
        Intent intent = new Intent(HomeScreenActivity_New.this, LoginActivityNew.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        syncUtils.syncBackground();
        sessionManager.setReturningUser(false);
        sessionManager.setLogout(true);
    }


    public void profilePicDownloaded(ProviderDTO providerDTO) throws DAOException {
        Log.d(TAG, "profilePicDownloaded: ");
        SessionManager sessionManager = new SessionManager(HomeScreenActivity_New.this);
        UrlModifiers urlModifiers = new UrlModifiers();
        String uuid = sessionManager.getProviderID();
        String url = urlModifiers.getProviderProfileImageUrl(uuid);
        Log.d(TAG, "profilePicDownloaded:: url : " + url);


        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PROVIDER_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());

        profilePicDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody file) {
                Log.d(TAG, "onNext: ");
                DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                downloadFilesUtils.saveToDisk(file, uuid);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                ProviderDAO providerDAO = new ProviderDAO();
                boolean updated = false;
                try {
                    updated = providerDAO.updateLoggedInUserProfileImage(AppConstants.IMAGE_PATH + uuid + ".jpg", sessionManager.getProviderID());

                } catch (DAOException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (updated) {
                    Glide.with(HomeScreenActivity_New.this).load(AppConstants.IMAGE_PATH + uuid + ".jpg").thumbnail(0.3f).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileIcon);
                }
                ImagesDAO imagesDAO = new ImagesDAO();
                boolean isImageDownloaded = false;
                try {
                    isImageDownloaded = imagesDAO.updateLoggedInUserProfileImage(AppConstants.IMAGE_PATH + uuid + ".jpg", sessionManager.getProviderID());

                } catch (DAOException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
    }

    //update ui as per internet availability
    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            imageViewIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            imageViewIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // put string value
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        assert currentFragment != null;
        outState.putString("currentFragment", currentFragment.getTag());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // get values from saved state
        currentFragment = savedInstanceState.getString("currentFragment");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loadLastSelectedFragment() {
        Fragment fragment = null;
        String tag = getTopFragmentTag();
        //if (currentFragment != null && !currentFragment.isEmpty()) {
        if (tag.equalsIgnoreCase(TAG_HOME)) {
            fragment = new HomeFragment_New();
            ivHamburger.setVisibility(View.VISIBLE);
            bottomNav.getMenu().findItem(R.id.bottom_nav_home_menu).setChecked(true);
        } else if (tag.equalsIgnoreCase(TAG_HELP)) {
            fragment = new HelpFragment_New();
            bottomNav.getMenu().findItem(R.id.bottom_nav_help).setChecked(true);
            tvTitleHomeScreenCommon.setText(getResources().getString(R.string.help_center));
            ivHamburger.setVisibility(View.GONE);
            imageview_notifications_home.setVisibility(View.GONE);
            imageViewIsInternet.setVisibility(View.VISIBLE);
        } else if (tag.equalsIgnoreCase(TAG_ACHIEVEMENT)) {
            fragment = new MyAchievementsFragment();
            bottomNav.getMenu().findItem(R.id.bottom_nav_achievements).setChecked(true);
            ivHamburger.setVisibility(View.GONE);
            imageview_notifications_home.setVisibility(View.GONE);
            imageViewIsInternet.setVisibility(View.VISIBLE);
            tvTitleHomeScreenCommon.setText(getString(R.string.my_achievements));
            tag = TAG_ACHIEVEMENT;
        }
        // }
        loadFragment(fragment, tag);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            /*if (mSyncProgressDialog != null && mSyncProgressDialog.isShowing()) {
                mSyncProgressDialog.dismiss();
            }
            mSyncProgressDialog = new ProgressDialog(HomeScreenActivity_New.this, R.style.AlertDialogStyle); //thats how to add a style!
            mSyncProgressDialog.setTitle(R.string.syncInProgress);
            mSyncProgressDialog.setCancelable(false);
            mSyncProgressDialog.setProgress(i);
            mSyncProgressDialog.show();*/

            if (dialogRefreshInProgress != null && dialogRefreshInProgress.isShowing())
                dialogRefreshInProgress.dismiss();

            showRefreshInProgressDialog();

            syncUtils.initialSync("home");
        }
    }

    private void insertTimeSpentByUserIntoDb() {
        long firstLoginTimeInMilliseconds = DateAndTimeUtils.convertStringDateToMilliseconds(sessionManager.getFirstProviderLoginTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long todaysDateInMilliseconds = DateAndTimeUtils.getTodaysDateInMilliseconds();
        long startTimeInMilliseconds = Math.max(todaysDateInMilliseconds, firstLoginTimeInMilliseconds);
        long endTimeInMilliseconds = System.currentTimeMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> aggregateStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTimeInMilliseconds, endTimeInMilliseconds);
        UsageStats overallUsageStats = aggregateStatsMap.get("org.intelehealth.app");

        if (overallUsageStats != null) {
            long totalTimeSpent = overallUsageStats.getTotalTimeInForeground();
            ProviderAttributeDTO providerAttributeDTO = new ProviderAttributeDTO();
            providerAttributeDTO.setUuid(UUID.randomUUID().toString());
            providerAttributeDTO.setProvider_uuid(sessionManager.getProviderID());
            providerAttributeDTO.setValue(String.valueOf(totalTimeSpent));
            providerAttributeDTO.setProvider_attribute_type_uuid("");

            ProviderAttributeDAO providerAttributeDAO = new ProviderAttributeDAO();
            try {
                providerAttributeDAO.createProviderAttribute(providerAttributeDTO);
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
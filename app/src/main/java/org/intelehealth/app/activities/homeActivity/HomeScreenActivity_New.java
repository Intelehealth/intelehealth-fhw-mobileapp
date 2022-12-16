package org.intelehealth.app.activities.homeActivity;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.aboutus.AboutUsActivity;
import org.intelehealth.app.activities.settingsActivity.Language_ProtocolsActivity;
import org.intelehealth.app.activities.loginActivity.LoginActivity;
import org.intelehealth.app.activities.loginActivity.LoginActivityNew;
import org.intelehealth.app.appointmentNew.AppointmentDetailsActivity;
import org.intelehealth.app.appointmentNew.MyAppointmentActivity;
import org.intelehealth.app.activities.informativeVideos.fragments.InformativeVideosFragment_New;
import org.intelehealth.app.activities.notification.NotificationActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.activities.help.activities.HelpFragment_New;
import org.intelehealth.app.database.dao.ProviderProfileDao;
import org.intelehealth.app.models.CheckAppUpdateRes;
import org.intelehealth.app.models.dto.ProviderProfileDTO;
import org.intelehealth.app.profile.MyProfileFragment_New;
import org.intelehealth.app.services.firebase_services.CallListenerBackgroundService;
import org.intelehealth.app.services.firebase_services.DeviceInfoUtils;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.ui2.customToolip.ActionItemCustom;
import org.intelehealth.app.ui2.customToolip.QuickActionCustom;
import org.intelehealth.app.ui2.customToolip.QuickIntentActionCustom;
import org.intelehealth.app.activities.achievements.fragments.MyAchievementsFragment;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.OfflineLogin;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.apprtc.ChatActivity;
import org.intelehealth.apprtc.CompleteActivity;
import org.intelehealth.apprtc.data.Manager;
import org.intelehealth.apprtc.utils.FirebaseUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class HomeScreenActivity_New extends AppCompatActivity {
    private static final String TAG = "HomeScreenActivity";
    ImageView imageViewIsInternet, ivHamburger, imageview_notifications_home;
    private boolean isConnected = false;
    private static final int ID_DOWN = 2;
    private QuickActionCustom quickAction;
    private QuickActionCustom quickIntent;
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
    TextView tvTitleHomeScreenCommon, tvAppLastSync;
    BottomNavigationView bottomNav;
    private CardView survey_snackbar_cv;
    ImageView imageViewIsNotification, ivCloseDrawer, ivProfileIcon;
    TextView tvEditProfile, tvAppVersion, tvUsername, tvUserId;
    LinearLayout menuResetApp;
    private static final String ACTION_NAME = "org.intelehealth.app.RTC_MESSAGING_EVENT";
    String firstLogin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_ui2);
        context = HomeScreenActivity_New.this;
        DeviceInfoUtils.saveDeviceInfo(this);
        catchFCMMessageData();


        loadFragment(new HomeFragment_New());
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

        //currently user details are in local db
        updateNavHeaderUserDetails();

    }

    private void clickListeners() {
        Intent intent_exit = getIntent();
        if (intent_exit != null) {
            String intentTag = intent_exit.getStringExtra("intentTag");
            if (intentTag != null) {
                if (intentTag.equalsIgnoreCase("Feedback screen"))
                    showSnackBarAndRemoveLater();
                else
                    survey_snackbar_cv.setVisibility(View.GONE);
            }
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            //drawer is open
            //  getWindow().setStatusBarColor(Color.CYAN);
        }


        tvTitleHomeScreenCommon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity_New.this, MyAppointmentActivity.class);
                startActivity(intent);
            }
        });

        imageViewIsNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreenActivity_New.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        if (ivHamburger != null) {
            ivHamburger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(Gravity.LEFT);

                }
            });
        }

        imageViewIsInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quickAction.show(v);
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

                tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_profile));
                Fragment fragment = new MyProfileFragment_New();
                loadFragment(fragment);
            }
        });


        ivCloseDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);

            }
        });

        menuResetApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initUI() {
        survey_snackbar_cv = findViewById(R.id.survey_snackbar_cv);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        tvAppVersion = findViewById(R.id.tv_app_version);
        menuResetApp = findViewById(R.id.layout_reset_app);
        imageview_notifications_home = findViewById(R.id.imageview_notifications_home);

        View toolbarHome = findViewById(R.id.toolbar_home);

        tvTitleHomeScreenCommon = toolbarHome.findViewById(R.id.tv_user_location_home);
        tvAppLastSync = toolbarHome.findViewById(R.id.tv_app_sync_time);

        imageViewIsInternet = toolbarHome.findViewById(R.id.imageview_is_internet);
        imageViewIsNotification = toolbarHome.findViewById(R.id.imageview_notifications_home);

        ivHamburger = findViewById(R.id.iv_hamburger);
        isNetworkAvailable(this);

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
            mSyncProgressDialog = new ProgressDialog(HomeScreenActivity_New.this, R.style.AlertDialogStyle); //thats how to add a style!
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

        //bottom nav
        bottomNav = findViewById(R.id.bottom_nav_home);
        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        bottomNav.setItemIconTintList(null);
        bottomNav.getMenu().findItem(R.id.bottom_nav_home_menu).setChecked(true);


        String sync_text = setLastSyncTime(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
        tvAppLastSync.setText(sync_text);
        Log.d(TAG, "onReceive: sync_text : " + sync_text);


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
        if (backStackEntryCount == 1) {
            // finish();
            wantToExitApp(this, "Exit App", getResources().getString(R.string.sure_to_exit),
                    getResources().getString(R.string.yes), getResources().getString(R.string.no));

        } else {
            super.onBackPressed();


        }
    }

    public void wantToExitApp(Context context, String title, String subTitle,
                              String positiveBtnTxt, String negativeBtnTxt) {
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

    public void wantToLogoutFromApp(Context context, String title, String subTitle,
                                    String positiveBtnTxt, String negativeBtnTxt) {
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

        });

        alertDialog.show();
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
                                setTooltipForInternet("Good internet.\nRefresh");

                            }
                        }
                    }
                }
            }
        }

        if (flag == 0) {
            if (imageViewIsInternet != null) {
                imageViewIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

                setTooltipForInternet("No internet");
            }

        }

    }

    private void setTooltipForInternet(String message) {
        QuickActionCustom.setDefaultColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
        QuickActionCustom.setDefaultTextColor(Color.BLACK);

        ActionItemCustom nextItem = new ActionItemCustom(ID_DOWN, message);
        quickAction = new QuickActionCustom(this, QuickActionCustom.HORIZONTAL);
        quickAction.setColorRes(R.color.white);
        quickAction.setTextColorRes(R.color.textColorBlack);
        quickAction.addActionItem(nextItem);
        quickAction.setTextColor(Color.BLACK);


        //Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickActionCustom.OnActionItemClickListener() {
            @Override
            public void onItemClick(ActionItemCustom item) {
                //here we can filter which action item was clicked with pos or actionId parameter
                String title = item.getTitle();
                Toast.makeText(HomeScreenActivity_New.this, title + " selected", Toast.LENGTH_SHORT).show();
                if (!item.isSticky()) quickAction.remove(item);
            }
        });

        quickAction.setOnDismissListener(new QuickActionCustom.OnDismissListener() {
            @Override
            public void onDismiss() {
                // Toast.makeText(HomeScreenActivity.this, "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        quickIntent = new QuickIntentActionCustom(this)
                .setActivityIntent(sendIntent)
                .create();
        quickIntent.setAnimStyle(QuickActionCustom.Animation.REFLECT);
    }


    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            String tag = fragment.getClass().getSimpleName();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment, tag);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void loadFragmentForBottomNav(Fragment fragment) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        // transaction.addToBackStack(null);
        // transaction.commit();
        boolean fragmentPopped = getSupportFragmentManager().popBackStackImmediate(tag, 0);

        if (!fragmentPopped) {
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    public void showLoggingInDialog() {

        AlertDialog.Builder builder
                = new AlertDialog.Builder(HomeScreenActivity_New.this);
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
                tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_achievements));
                fragment = new MyAchievementsFragment();

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
                wantToLogoutFromApp(this, "Logout", getResources().getString(R.string.sure_to_logout),
                        getResources().getString(R.string.yes), getResources().getString(R.string.no));

                break;
            default:
        }

        if (fragment != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadFragment(fragment);
            menuItem.setChecked(true);
            setTitle(menuItem.getTitle());
            mDrawerLayout.closeDrawers();
        }

    }

    @Override
    protected void onResume() {

        firstLogin = getIntent().getStringExtra("firstLogin");
        Log.d(TAG, "onCreate: firstLogin : " + firstLogin);
        if (sessionManager.getIsLoggedIn() && firstLogin != null && !firstLogin.isEmpty() && firstLogin.equalsIgnoreCase("firstLogin")) {
            firstLogin = "";
            getIntent().putExtra("firstLogin", "");

            showLoggingInDialog();

        }
        loadFragment(new HomeFragment_New());
        ivHamburger.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_hamburger));

        //registerReceiver(reMyreceive, filter);
        checkAppVer();  //auto-update feature.
//        lastSyncTextView.setText(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
        if (!sessionManager.getLastSyncDateTime().equalsIgnoreCase("- - - -")
                && Locale.getDefault().toString().equals("en")) {
//            lastSyncAgo.setText(CalculateAgoTime());
        }
      /*  //UI2.0 if first time login then only show popup
        if (sessionManager.getIsLoggedIn()) {
            //sessionManager.setIsLoggedIn(true);
            showLoggingInDialog();

        }*/


        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: 11");
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
                                builder = new android.app.AlertDialog.Builder(HomeScreenActivity_New.this, android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new android.app.AlertDialog.Builder(HomeScreenActivity_New.this);
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
                        new AlertDialog.Builder(HomeScreenActivity_New.this)
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

            String sync_text = setLastSyncTime(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
            tvAppLastSync.setText(sync_text);
            Log.d(TAG, "onReceive: sync_text : " + sync_text);
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

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment;

                    switch (item.getItemId()) {
                        case R.id.bottom_nav_home_menu:
                            Log.d(TAG, "onNavigationItemSelected: bottom_nav_home_menu");
                            tvTitleHomeScreenCommon.setText(getResources().getString(R.string.title_home_screen));
                            fragment = new HomeFragment_New();
                            loadFragment(fragment);
                            return true;
                        case R.id.bottom_nav_achievements:
                            tvTitleHomeScreenCommon.setText(getResources().getString(R.string.my_achievements));
                            fragment = new MyAchievementsFragment();
                            loadFragmentForBottomNav(fragment);

                            return true;
                        case R.id.bottom_nav_help:
                            tvTitleHomeScreenCommon.setText(getResources().getString(R.string.help));
                            fragment = new HelpFragment_New();
                            loadFragmentForBottomNav(fragment);

                            return true;
                        case R.id.bottom_nav_add_patient:

                            return true;
                    }

                    return false;
                }
            };


    private void logoutFromApp() {
        //code from old homeactivity


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

        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();
        sessionManager.setReturningUser(false);
        sessionManager.setLogout(true);


    }

    private void updateNavHeaderUserDetails() {
        ProviderProfileDao providerProfileDao = new ProviderProfileDao();
        try {
            ProviderProfileDTO providerProfileDTO = providerProfileDao.getProvidersDetails();

            boolean firstname = isValidField(providerProfileDTO.getFirstName());
            boolean lastname = isValidField(providerProfileDTO.getLastName());
            String userFullName = "";
            if (firstname && lastname) {
                userFullName = providerProfileDTO.getFirstName() + " " + providerProfileDTO.getLastName();
            } else if (firstname) {
                userFullName = providerProfileDTO.getFirstName();
            } else if (lastname) {
                userFullName = providerProfileDTO.getLastName();

            }
            tvUsername.setText(userFullName);

            tvUserId.setText("CHW ID : " + sessionManager.getChwname());
            Log.d(TAG, "initUI: path : " + providerProfileDTO.getImagePath());

            if (providerProfileDTO.getImagePath() != null && !providerProfileDTO.getImagePath().isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(providerProfileDTO.getImagePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                byte[] imageArray = stream.toByteArray();
                ivProfileIcon.setImageBitmap(bitmap);
              /*  Glide.with(getActivity())
                        .load(bitmap)
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(ivProfileImage);*/

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

        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();
        sessionManager.setReturningUser(false);
        sessionManager.setLogout(true);
    }

    private void catchFCMMessageData() {
        // get the chat notification click info
        if (getIntent().getExtras() != null) {
            //Logger.logV(TAG, " getIntent - " + getIntent().getExtras().getString("actionType"));
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
                                if (seconds >= 10) {
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

}


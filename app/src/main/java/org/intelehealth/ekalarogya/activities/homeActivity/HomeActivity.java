package org.intelehealth.ekalarogya.activities.homeActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ekalarogya.BuildConfig;
import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.activePatientsActivity.ActivePatientActivity;
import org.intelehealth.ekalarogya.activities.chmProfileActivity.HwProfileActivity;
import org.intelehealth.ekalarogya.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ekalarogya.activities.loginActivity.LoginActivity;
import org.intelehealth.ekalarogya.activities.privacyNoticeActivity.PrivacyNotice_Activity;
import org.intelehealth.ekalarogya.activities.searchPatientActivity.SearchPatientActivity;
import org.intelehealth.ekalarogya.activities.settingsActivity.SettingsActivity;
import org.intelehealth.ekalarogya.activities.todayPatientActivity.TodayPatientActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.appointment.AppointmentListingActivity;
import org.intelehealth.ekalarogya.database.dao.SyncDAO;
import org.intelehealth.ekalarogya.database.dao.VisitsDAO;
import org.intelehealth.ekalarogya.models.CheckAppUpdateRes;
import org.intelehealth.ekalarogya.models.DownloadMindMapRes;
import org.intelehealth.ekalarogya.models.dto.VisitDTO;
import org.intelehealth.ekalarogya.networkApiCalls.ApiClient;
import org.intelehealth.ekalarogya.networkApiCalls.ApiInterface;
import org.intelehealth.ekalarogya.services.firebase_services.DeviceInfoUtils;
import org.intelehealth.ekalarogya.shared.BaseActivity;
import org.intelehealth.ekalarogya.syncModule.SyncUtils;
import org.intelehealth.ekalarogya.utilities.ConfigUtils;
import org.intelehealth.ekalarogya.utilities.DialogUtils;
import org.intelehealth.ekalarogya.utilities.DownloadMindMaps;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.NavigationUtils;
import org.intelehealth.ekalarogya.utilities.NetworkConnection;
import org.intelehealth.ekalarogya.utilities.OfflineLogin;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.UnUploadedVisitsNotificationWorker;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;
import org.intelehealth.ekalarogya.webrtc.activity.EkalCallLogActivity;
import org.intelehealth.ekalarogya.webrtc.activity.EkalChatActivity;
import org.intelehealth.ekalarogya.webrtc.activity.EkalVideoActivity;
import org.intelehealth.ekalarogya.widget.materialprogressbar.CustomProgressDialog;
import org.intelehealth.fcm.utils.FcmConstants;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.utils.FirebaseUtils;
import org.intelehealth.klivekit.utils.Manager;
import org.intelehealth.klivekit.utils.RtcUtilsKt;
import org.intelehealth.videolibrary.constants.Constants;
import org.intelehealth.videolibrary.utils.VideoLibraryManager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Home Screen
 */

public class HomeActivity extends BaseActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    private ProgressDialog mSyncProgressDialog;
    private boolean hasLicense = false;
    int i = 5;
    CardView manualSyncButton;
    SyncUtils syncUtils = new SyncUtils();
    CardView c1, c2, c3, c4, c5, c6, unUploadedVisitNotificationCV;
    Context context;
    CustomProgressDialog customProgressDialog;
    private String mindmapURL = "";
    private DownloadMindMaps mTask;
    ProgressDialog mProgressDialog, mSyncDialog;
    private int versionCode = 0;
    private CompositeDisposable disposable = new CompositeDisposable();
    TextView lastSyncTextView, lastSyncAgo, newPatient_textview, findPatients_textview, todaysVisits_textview, activeVisits_textview, videoLibrary_textview, help_textview, unUploadedVisitNotificationTV;
    Toolbar toolbar;

    ActivityResultLauncher<Intent> result = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == Constants.JWT_TOKEN_EXPIRED) {
                sessionManager.setJwtAuthToken(null);
                NavigationUtils navigationUtils = new NavigationUtils();
                navigationUtils.triggerSignOutOn401Response(context);
            }
        }
    });

    private void saveToken() {
        Manager.getInstance().setBaseUrl("https://" + sessionManager.getServerUrl());
        VideoLibraryManager.setBaseUrl(sessionManager.getServerUrl());
        FirebaseUtils.saveToken(this, sessionManager.getProviderID(), IntelehealthApplication.getInstance().refreshedFCMTokenID, sessionManager.getAppLanguage(), sessionManager.getJwtAuthToken());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(HomeActivity.this);
        setContentView(R.layout.activity_home);
        sessionManager = new SessionManager(this);
        checkIfFCMNotificationClicked();
        Log.e(TAG, "onCreate: server url=>" + sessionManager.getServerUrl());
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        DeviceInfoUtils.saveDeviceInfo(this);
        catchFCMMessageData();
        setTitle(R.string.title_activity_login);
        context = HomeActivity.this;
        customProgressDialog = new CustomProgressDialog(context);
        mSyncDialog = new ProgressDialog(HomeActivity.this, R.style.AlertDialogStyle);
        mSyncDialog.setTitle(R.string.app_sync);
        mSyncDialog.setCancelable(false);
        mSyncDialog.setProgress(i);
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        Logger.logD(TAG, "onCreate: " + getFilesDir().toString());
        lastSyncTextView = findViewById(R.id.lastsynctextview);
        lastSyncAgo = findViewById(R.id.lastsyncago);
        manualSyncButton = findViewById(R.id.manualsyncbutton);
        c1 = findViewById(R.id.cardview_newpat);
        c2 = findViewById(R.id.cardview_find_patient);
        c3 = findViewById(R.id.cardview_today_patient);
        c4 = findViewById(R.id.cardview_active_patients);
        c5 = findViewById(R.id.cardview_video_libraby);
        c6 = findViewById(R.id.cardview_help_whatsapp);
        unUploadedVisitNotificationCV = findViewById(R.id.unUploadedVisitsNotificationCardView);
        findViewById(R.id.cardview_appointment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AppointmentListingActivity.class);
                startActivity(intent);
            }
        });

        newPatient_textview = findViewById(R.id.newPatient_textview);
        newPatient_textview.setText(R.string.new_patient);
        findPatients_textview = findViewById(R.id.findPatients_textview);
        findPatients_textview.setText(R.string.find_patient);
        todaysVisits_textview = findViewById(R.id.todaysVisits_textview);
        todaysVisits_textview.setText(R.string.today_visits);
        activeVisits_textview = findViewById(R.id.activeVisits_textview);
        activeVisits_textview.setText(R.string.active_visits);
        videoLibrary_textview = findViewById(R.id.videoLibrary_textview);
        videoLibrary_textview.setText(R.string.video_library);
        help_textview = findViewById(R.id.help_textview);
        help_textview.setText(R.string.Whatsapp_Help_Cardview);
        unUploadedVisitNotificationTV = findViewById(R.id.unUploadedVisitsNotificationTextView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            } else {
                UnUploadedVisitsNotificationWorker.schedule(getApplicationContext());
                Toast.makeText(HomeActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            UnUploadedVisitsNotificationWorker.schedule(getApplicationContext());
        }

        //Help section of watsapp...
        c6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumberWithCountryCode = "+919503692181";
                String message = getString(R.string.hello_my_name_is) + " " + sessionManager.getChwname() + " " + getString(R.string.i_need_assistance);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s&text=%s", phoneNumberWithCountryCode, message))));
            }
        });

        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Loads the config file values and check for the boolean value of privacy key.
                ConfigUtils configUtils = new ConfigUtils(HomeActivity.this);
                if (configUtils.privacy_notice()) {
                    Intent intent = new Intent(HomeActivity.this, PrivacyNotice_Activity.class);
                    startActivity(intent);
                } else {
                    //Clear HouseHold UUID from Session for new registration
                    sessionManager.setHouseholdUuid("");
                    Intent intent = new Intent(HomeActivity.this, IdentificationActivity.class);
                    startActivity(intent);
                }
            }
        });
        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SearchPatientActivity.class);
                startActivity(intent);
            }
        });
        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, TodayPatientActivity.class);
                startActivity(intent);
            }
        });
        c5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoLibrary();
            }
        });

        lastSyncTextView.setText(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());
        manualSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    Toast.makeText(context, getString(R.string.syncInProgress), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
                }
                syncUtils.syncForeground("home");
            }
        });

        if (sessionManager.isFirstTimeLaunched()) {
            mSyncProgressDialog = new ProgressDialog(HomeActivity.this, R.style.AlertDialogStyle); //thats how to add a style!
            mSyncProgressDialog.setTitle(R.string.syncInProgress);
            mSyncProgressDialog.setCancelable(false);
            mSyncProgressDialog.setProgress(i);
            mSyncProgressDialog.show();
            syncUtils.initialSync("home");
            mSyncProgressDialog.dismiss();
        } else {
            WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
            saveToken();
        }

        Logger.logD("Yojana", sessionManager.getJalJeevanYojanaScheme());
        showProgressbar();
        HeartBitApi();
        showAppInfo();
        try {
            voidUnSyncedOldVisits();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        setLocale(HomeActivity.this);

        getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    private void checkIfFCMNotificationClicked() {
        Intent intent = getIntent();
        String clickAction = intent.getStringExtra(FcmConstants.INTENT_CLICK_ACTION);
        if (clickAction != null && clickAction.equalsIgnoreCase(FcmConstants.FCM_PLUGIN_HOME_ACTIVITY)) {
            switchLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(HomeActivity.this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();
                UnUploadedVisitsNotificationWorker.schedule(getApplicationContext());
            } else {
                Toast.makeText(HomeActivity.this, "Notification Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startActiveVisits(View view) {
        Intent intent = new Intent(HomeActivity.this, ActivePatientActivity.class);
        startActivity(intent);
    }

    private void voidUnSyncedOldVisits() throws ParseException {
        VisitsDAO visitsDAO = new VisitsDAO();
        List<VisitDTO> unSyncedOldVisitList = visitsDAO.getAllUnSyncedOldVisits();
        final boolean[] isVisitVoid = {false};
        if (unSyncedOldVisitList.size() > 0) {
            for (int i = 0; i < unSyncedOldVisitList.size(); i++) {
                try {
                    isVisitVoid[0] = visitsDAO.voidVisit(unSyncedOldVisitList.get(i).getUuid());
                } catch (DAOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (isVisitVoid[0] = true) {
            int count = getUnUploadedVisitCount();
            unUploadedVisitNotificationTV.setText(getResources().getString(R.string.you_have_unuploaded_visits, String.valueOf(count)));
            if (count == 0)
                unUploadedVisitNotificationCV.setVisibility(View.GONE);
        }
    }

    public void HeartBitApi() {
        try {
            Timer timer = new Timer();
            TimerTask minuteTask = new TimerTask() {
                @Override
                public void run() {
                    SyncDAO syncDAO = new SyncDAO();
                    syncDAO.syncUserStatus(context);
                }
            };
            //timer will be call after each 5minute
            timer.schedule(minuteTask, 0l, 1000 * 5 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //function for handling the video library feature...
//    private void videoLibrary() {
//        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;
//        //Check for license key and load the correct config file
//        try {
//            JSONObject obj = null;
//            if (hasLicense) {
//                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context), String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
//            } else {
//                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
//            }
//            if (obj.has("video_library")) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri uri = Uri.parse(obj.getString("video_library"));
//                intent.setData(uri);
//                startActivity(intent);
//            } else {
//                Toast.makeText(context, "No config attribute found", Toast.LENGTH_SHORT).show();
//            }
//        } catch (JSONException e) {
//            FirebaseCrashlytics.getInstance().recordException(e);
//            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
//        }
//    }

    private void videoLibrary() {
        try {
            Intent intent = new Intent(this, Class.forName("org.intelehealth.videolibrary.listing.activity.YoutubeListingActivity"));
            result.launch(intent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int getUnUploadedVisitCount() {
        int unUploadedVisitCount = 0;
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender " +
                "FROM tbl_visit a, tbl_patient b " +
                "WHERE a.patientuuid = b.uuid " +
                "AND a.voided = 0 AND a.enddate is NULL OR a.enddate='' GROUP BY a.uuid ORDER BY a.sync ASC ";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        final Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndexOrThrow("sync")).equalsIgnoreCase("0"))
                        unUploadedVisitCount++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return unUploadedVisitCount;
    }

    private void showProgressbar() {
        mProgressDialog = new ProgressDialog(HomeActivity.this);
        mProgressDialog.setMessage(getString(R.string.download_protocols));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.actionCallLog) {
            startActivity(new Intent(this, EkalCallLogActivity.class));
            return true;
        } else if (itemId == R.id.userProfileOption) {
            Hw_Profile();
            return true;
        } else if (itemId == R.id.settingsOption) {
            settings();
            return true;
        } else if (itemId == R.id.switchLocation) {
            if ((isNetworkConnected())) {
                mSyncDialog.show();
                boolean isSynced = syncUtils.syncForeground("home");
                if (isSynced) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() { //Do something after 100ms
                            showConfirmationDialog();
                        }
                    }, 3000);
                } else {
                    mSyncDialog.dismiss();
                    DialogUtils dialogUtils = new DialogUtils();
                    dialogUtils.showOkDialog(this, getString(R.string.error), getString(R.string.sync_failed), getString(R.string.generic_ok));
                }
                return true;
            } else {
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showOkDialog(this, getString(R.string.error_network), getString(R.string.no_network_sync), getString(R.string.generic_ok));
            }
            return true;
        } else if (itemId == R.id.updateProtocolsOption) {
            if (NetworkConnection.isOnline(this)) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                dialog.setTitle(getString(R.string.enter_license_key)).setView(promptsView).setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;
                        EditText etURL = d.findViewById(R.id.licenseurl);
                        EditText etKey = d.findViewById(R.id.licensekey);
                        String url = etURL.getText().toString().trim();
                        String key = etKey.getText().toString().trim();
                        if (url.isEmpty()) {
                            etURL.setError(getResources().getString(R.string.enter_server_url));
                            etURL.requestFocus();
                            return;
                        }
                        if (url.contains(":")) {
                            etURL.setError(getResources().getString(R.string.invalid_url));
                            etURL.requestFocus();
                            return;
                        }
                        if (key.isEmpty()) {
                            etKey.setError(getResources().getString(R.string.enter_license_key));
                            etKey.requestFocus();
                            return;
                        }
                        sessionManager.setMindMapServerUrl(url);
                        getMindmapDownloadURL("https://" + url + ":3004/", key);
                        // as per new jwt implementation -> changing port from 3004 -> 3030 - Prajwal.
                    }
                }).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog builderDialog = dialog.show();
                IntelehealthApplication.setAlertDialogCustomTheme(this, builderDialog);
            } else {
                Toast.makeText(context, getString(R.string.mindmap_internect_connection), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.logoutOption) {
            MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
            alertdialogBuilder.setMessage(R.string.sure_to_logout);
            alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    logout();
                }
            });
            alertdialogBuilder.setNegativeButton(R.string.generic_no, null);
            AlertDialog alertDialog = alertdialogBuilder.create();
            alertDialog.show();
            Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationDialog() {
        mSyncDialog.dismiss();
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(getString(R.string.confirm_switch_location,
                sessionManager.getCurrentLocationName(),
                sessionManager.getSecondaryLocationName()));
        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switchLocation();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.generic_no, null);
        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();
        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    public void settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void Hw_Profile() {
        Intent intent = new Intent(this, HwProfileActivity.class);
        startActivity(intent);
    }

    public void logout() {
        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);
        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();
        sessionManager.setReturningUser(false);
        sessionManager.setUserProfileDetail("");
        sessionManager.setLogout(true);
        IntelehealthApplication.getInstance().stopRealTimeObserverAndSocket();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        checkAppVer();  //auto-update feature.
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(AppConstants.SYNC_INTENT_ACTION);
        ContextCompat.registerReceiver(this, syncBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(syncBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            backPressEvent();
        }
    };

    public void backPressEvent() {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.sure_to_exit);
        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveTaskToBack(true);
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.generic_no, null);
        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();
        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
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
                        new AlertDialog.Builder(HomeActivity.this).setMessage(R.string.failed_initial_synced).setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }

                                }).setCancelable(false)

                                .show();
                    } else {
                        mTempSyncHelperList.add(flagType);
                        if (mTempSyncHelperList.contains(AppConstants.SYNC_PULL_DATA_DONE)) {
                            hideSyncProgressBar(true);
                        }
                    }
                }
            }
            lastSyncTextView.setText(getString(R.string.last_synced) + " \n" + sessionManager.getLastSyncDateTime());

            try {
                voidUnSyncedOldVisits();
            } catch (ParseException exception) {
                throw new RuntimeException(exception);
            }
        }
    };

    private void hideSyncProgressBar(boolean isSuccess) {
        saveToken();
        if (mTempSyncHelperList != null) mTempSyncHelperList.clear();
        if (mSyncProgressDialog != null && mSyncProgressDialog.isShowing()) {
            mSyncProgressDialog.dismiss();
            if (isSuccess) {
                saveToken();
                sessionManager.setFirstTimeLaunched(false);
                sessionManager.setMigration(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
                    }
                }, 10000);
            }
        }
    }

    private void getMindmapDownloadURL(String url, String key) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url, context);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key);
            resultsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
                        @Override
                        public void onNext(DownloadMindMapRes res) {
                            customProgressDialog.dismiss();
                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {
                                Log.e("MindMapURL", "Successfully get MindMap URL");
                                mTask = new DownloadMindMaps(context, mProgressDialog);
                                mindmapURL = res.getMindmap().trim();
                                sessionManager.setLicenseKey(key);
                                checkExistingMindMaps();
                            } else {
                                Toast.makeText(context, getResources().getString(R.string.no_protocols_found), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            customProgressDialog.dismiss();
                            Toast.makeText(context, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_SHORT).show();
                            Log.v(TAG, "jwt_response: " + e.toString());
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
        }
    }

    private void checkExistingMindMaps() {
        //Check is there any existing mindmaps are present, if yes then delete.
        File engines = new File(context.getFilesDir().getAbsolutePath(), "/Engines");
        Log.e(TAG, "Engines folder=" + engines.exists());
        if (engines.exists()) {
            engines.delete();
        }
        File logo = new File(context.getFilesDir().getAbsolutePath(), "/logo");
        Log.e(TAG, "Logo folder=" + logo.exists());
        if (logo.exists()) {
            logo.delete();
        }
        File physicalExam = new File(context.getFilesDir().getAbsolutePath() + "/physExam.json");
        Log.e(TAG, "physExam.json=" + physicalExam.exists());
        if (physicalExam.exists()) {
            physicalExam.delete();
        }
        File familyHistory = new File(context.getFilesDir().getAbsolutePath() + "/famHist.json");
        Log.e(TAG, "famHist.json=" + familyHistory.exists());
        if (familyHistory.exists()) {
            familyHistory.delete();
        }
        File pastMedicalHistory = new File(context.getFilesDir().getAbsolutePath() + "/patHist.json");
        Log.e(TAG, "patHist.json=" + pastMedicalHistory.exists());
        if (pastMedicalHistory.exists()) {
            pastMedicalHistory.delete();
        }
        File config = new File(context.getFilesDir().getAbsolutePath() + "/config.json");
        Log.e(TAG, "config.json=" + config.exists());
        if (config.exists()) {
            config.delete();
        }
        //Start downloading mindmaps
        mTask.execute(mindmapURL, context.getFilesDir().getAbsolutePath() + "/mindmaps.zip");
        Log.e("DOWNLOAD", "isSTARTED");

    }

    private void checkAppVer() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
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
                        builder = new android.app.AlertDialog.Builder(HomeActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new android.app.AlertDialog.Builder(HomeActivity.this);
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
                System.out.println(e.toString());
            }
        }));

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "onNewIntent");
        catchFCMMessageData();
    }

    private void catchFCMMessageData() {
        if (getIntent().getExtras() != null) {
            Bundle remoteMessage = getIntent().getExtras();
            try {
                if (remoteMessage.containsKey("actionType") && remoteMessage.getString("actionType").equals("TEXT_CHAT")) {
                    String fromUUId = remoteMessage.getString("toUser");
                    String toUUId = remoteMessage.getString("fromUser");
                    String patientUUid = remoteMessage.getString("patientId");
                    String visitUUID = remoteMessage.getString("visitId");
                    String patientName = remoteMessage.getString("patientName");
                    RtcArgs args = new RtcArgs();
                    args.setPatientName(patientName);
                    args.setPatientId(patientUUid);
                    args.setVisitId(visitUUID);
                    args.setNurseId(fromUUId);
                    args.setDoctorUuid(toUUId);
                    EkalChatActivity.startChatActivity(this, args);
                } else if (remoteMessage.containsKey("actionType") && remoteMessage.getString("actionType").equals("VIDEO_CALL")) {
                    Intent in = new Intent(this, EkalVideoActivity.class);
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
                    RtcArgs args = remoteMessage.getParcelable(RtcUtilsKt.RTC_ARGS);
                    in.putExtra(RtcUtilsKt.RTC_ARGS, args);
                    int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                    if (callState == TelephonyManager.CALL_STATE_IDLE && !isOldNotification) {
                        startActivity(in);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAttachedToWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showAppInfo() {
        TextView tvSetupLocation = findViewById(R.id.tvAppUserLocation);
        tvSetupLocation.setText(getString(R.string.location_setup, sessionManager.getCurrentLocationName()));
        TextView tvUserAppInfo = findViewById(R.id.tvAppVersionName);
        tvUserAppInfo.setText(getString(R.string.app_version_string, sessionManager.getChwname(), BuildConfig.VERSION_NAME));
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

    private void switchLocation() {
        HashMap<String, String> hashMap4 = new HashMap<>();
        Map.Entry<String, String> village_name = null;
        hashMap4.put(sessionManager.getSecondaryLocationUuid(), sessionManager.getSecondaryLocationName());
        for (Map.Entry<String, String> entry : hashMap4.entrySet()) {
            village_name = entry;
        }
        if (village_name != null) {
            switchLocationSetup(village_name);
            Log.d(TAG, "attempting setup");
        }
    }

    private void switchLocationSetup(Map.Entry<String, String> villageName) {
        ProgressDialog progress;
        progress = new ProgressDialog(HomeActivity.this, R.style.AlertDialogStyle);
        progress.setTitle(getString(R.string.please_wait_progress));
        progress.setMessage(getString(R.string.logging_in));
        progress.show();
        sessionManager.setSecondaryLocationName(sessionManager.getCurrentLocationName());
        sessionManager.setSecondaryLocationUuid(sessionManager.getCurrentLocationUuid());
        sessionManager.setCurrentLocationName(villageName.getValue());
        sessionManager.setCurrentLocationUuid(villageName.getKey());
        sessionManager.setFirstTimeLaunched(true);
        sessionManager.setPullExcutedTime("2006-08-22 22:21:48 ");
        clearDatabase();
//        getApplicationContext().deleteDatabase(AppConstants.DATABASE_NAME);
        progress.dismiss();
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
        intent.putExtra("intentType", "switchLocation");
        startActivity(intent);
        finish();
    }

    private void clearDatabase() {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete("tbl_appointments", null, null);
        db.delete("tbl_encounter", null, null);
        db.delete("tbl_dr_speciality", null, null);
        db.delete("tbl_visit_attribute", null, null);
        db.delete("tbl_patient", null, null);
        db.delete("tbl_patient_attribute", null, null);
        db.delete("tbl_visit", null, null);
        db.delete("tbl_obs", null, null);
        db.delete("tbl_image_records", null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}

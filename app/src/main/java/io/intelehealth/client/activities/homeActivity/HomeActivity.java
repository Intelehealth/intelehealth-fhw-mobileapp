package io.intelehealth.client.activities.homeActivity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.activePatientsActivity.ActivePatientActivity;
import io.intelehealth.client.activities.identificationActivity.IdentificationActivity;
import io.intelehealth.client.activities.loginActivity.LoginActivity;
import io.intelehealth.client.activities.privacyNoticeActivity.PrivacyNotice_Activity;
import io.intelehealth.client.activities.searchPatientActivity.SearchPatientActivity;
import io.intelehealth.client.activities.settingsActivity.SettingsActivity;
import io.intelehealth.client.activities.todayPatientActivity.TodayPatientActivity;
import io.intelehealth.client.activities.videoLibraryActivity.VideoLibraryActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.services.DownloadProtocolsTask;
import io.intelehealth.client.syncModule.SyncUtils;
import io.intelehealth.client.utilities.ConfigUtils;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.OfflineLogin;
import io.intelehealth.client.utilities.SessionManager;

import static io.intelehealth.client.app.AppConstants.UNIQUE_WORK_NAME;

/**
 * Home Screen
 */

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    ProgressDialog TempDialog;
    CountDownTimer CDT;
    int i = 5;

    TextView lastSyncTextView;
    Button manualSyncButton;
    IntentFilter filter;
    Myreceiver reMyreceive;
    SyncUtils syncUtils = new SyncUtils();
    CardView c1, c2, c3, c4, c5;
    private String key = null;
    private String licenseUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sessionManager = new SessionManager(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        setTitle(R.string.title_activity_login);
        reMyreceive = new Myreceiver();
        filter = new IntentFilter("lasysync");

        Logger.logD(TAG, "onCreate: " + getFilesDir().toString());
        lastSyncTextView = findViewById(R.id.lastsynctextview);
        manualSyncButton = findViewById(R.id.manualsyncbutton);
        manualSyncButton.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        c1 = findViewById(R.id.cardview_newpat);
        c2 = findViewById(R.id.cardview_find_patient);
        c3 = findViewById(R.id.cardview_today_patient);
        c4 = findViewById(R.id.cardview_active_patients);
        c5 = findViewById(R.id.cardview_video_libraby);
        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Loads the config file values and check for the boolean value of privacy key.
                ConfigUtils configUtils = new ConfigUtils(HomeActivity.this);
                if (configUtils.privacy_notice()) {
                    Intent intent = new Intent(HomeActivity.this, PrivacyNotice_Activity.class);
                    startActivity(intent);
                } else {
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
        c4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ActivePatientActivity.class);
                startActivity(intent);
            }
        });
        c5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, VideoLibraryActivity.class);
                startActivity(intent);
            }
        });

//        manualSyncButton.setPaintFlags(manualSyncButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        lastSyncTextView.setText("Last Synced:- " + sessionManager.getLastPulledDateTime());
        manualSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                syncUtils.syncForeground();
//                pullDataDAO.pushDataApi();
//                imagesPushDAO.patientProfileImagesPush();
//                imagesPushDAO.obsImagesPush();
//                pullDataDAO.pullData(HomeActivity.this);
            }
        });
//        final RecyclerView recyclerView = findViewById(R.id.recyclerview_home);
//        recyclerView.setHasFixedSize(true);
//
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 1);
//        recyclerView.setLayoutManager(gridLayoutManager);
//
//        recyclerView.setAdapter(new HomeAdapter());

//        String date = sessionManager.getDate();
//        String time = sessionManager.getTime();
//        final Calendar startDate = Calendar.getInstance();
//        startDate.set(Calendar.HOUR, 10);
//        startDate.set(Calendar.MINUTE, 00);
//        startDate.set(Calendar.AM_PM, Calendar.PM);
//
//
//        final Calendar endDate = Calendar.getInstance();
//        endDate.set(Calendar.HOUR, 10);
//        endDate.set(Calendar.MINUTE, 15);
//        endDate.set(Calendar.AM_PM, Calendar.PM);
//        if (getIntent().hasExtra("setup") && getIntent().getBooleanExtra("setup", false)) {
//
//            Log.d("newfilepath", dbfilepath);
//            final File db_file = new File(dbfilepath);

//            if (db_file.exists()) {
//                new AlertDialog.Builder(this)
//                        .setIcon(R.drawable.ic_file_download_black_48px)
//                        .setTitle(R.string.local_restore_alert_title)
//                        .setMessage(R.string.local_restore_alert_message)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.generic_yes,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int whichButton) {
//                                        //Restore from local backup
//                                        manageBackup(false, false); // to restore app data if db is empty
//                                    }
//                                }
//                        )
//                        .setNegativeButton(R.string.generic_no,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int whichButton) {
//                                        //Do Nothing!
//                                        // db_file.delete();
//                                    }
//                                }
//                        )
//                        .create().show();
//            }
//        }
//        if (sessionManager.isFirstTimeSyncExcuted()) {

//        AppConstants.notificationUtils.showNotifications("syncBackground","syncBackground Compledted",this);
//        pullDataDAO.pushDataApi();
//            sessionManager.setFirstTimeSyncExecute(false);
//        }


        WorkManager.getInstance().enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
        if (sessionManager.isFirstTimeLaunched()) {
            TempDialog = new ProgressDialog(HomeActivity.this);
            TempDialog.setMessage("Please wait...");
            TempDialog.setCancelable(false);
            TempDialog.setProgress(i);

            TempDialog.show();

            CDT = new CountDownTimer(7000, 1000) {
                public void onTick(long millisUntilFinished) {
                    TempDialog.setMessage("Please wait.. Syncing");
                    i--;
                }

                public void onFinish() {
                    TempDialog.dismiss();
                    //Your Code ...
                    sessionManager.setFirstTimeLaunched(false);
                }
            }.start();

        }

        if (sessionManager.isReturningUser()) {
            syncUtils.syncForeground();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.syncOption:
//                refreshDatabases();
//                return true;
            case R.id.settingsOption:
                settings();
                return true;
            case R.id.updateProtocolsOption: {

                if (sessionManager.getLicenseKey() != null && sessionManager.getLicenseKey().equalsIgnoreCase("http://mindmaps.intelehealth.io:4040")) {
                    String license = sessionManager.getLicenseKey();
                    if (license != null) {
                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(this);
                        downloadProtocolsTask.execute(license);
                    } else {
                        Toast.makeText(this, "License invalid", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    LayoutInflater li = LayoutInflater.from(this);
                    View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                    dialog.setTitle(getString(R.string.enter_license_key))
                            .setView(promptsView)
                            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Dialog d = (Dialog) dialog;

                                    EditText text = d.findViewById(R.id.licensekey);
                                    // text.setText(sessionManager.getLicenseKey());
                                    EditText url = d.findViewById(R.id.licenseurl);
                                    // url.setText(sessionManager.getMindMapServerUrl());
                                    if (text.getText().toString().isEmpty() && text.getText() == null || url.getText().toString().isEmpty() && url.getText() == null) {
                                        text.setFocusable(true);
                                        text.setError("Enter license key");
                                    }
                                    if (sessionManager.getLicenseKey() != null && sessionManager.getLicenseKey().equalsIgnoreCase("http://mindmaps.intelehealth.io:4040")) {
                                        text.setText(sessionManager.getLicenseKey());
                                        url.setText(sessionManager.getMindMapServerUrl());
                                    }
                                    key = text.getText().toString();
                                    licenseUrl = url.getText().toString();

                                    sessionManager.setMindMapServerUrl(licenseUrl);

                                    if (keyVerified(key)) {
                                        DownloadProtocolsTask downloadProtocolsTask = new DownloadProtocolsTask(HomeActivity.this);
                                        downloadProtocolsTask.execute(key);


                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.create().show();

                }
                return true;
            }
            case R.id.sync:
//                pullDataDAO.pullData(this);
//                pullDataDAO.pushDataApi();
                boolean isSynced = syncUtils.syncForeground();
                AppConstants.notificationUtils.showNotifications("sync", "syncBackground Completed", 1, this);
//                boolean i = imagesPushDAO.patientProfileImagesPush();
//                boolean o = imagesPushDAO.obsImagesPush();
                if (isSynced)
                    AppConstants.notificationUtils.showNotifications("ImageUpload", "ImageUpload Completed", 4, this);
                else
                    AppConstants.notificationUtils.showNotifications("ImageUpload", "ImageUpload failed", 4, this);
                return true;
//            case R.id.backupOption:
//                manageBackup(true, false);  // to backup app data at any time of the day
//                return true;
//
//            case R.id.restoreOption:
//                manageBackup(false, false); // to restore app data if db is empty
//                return true;

            case R.id.logoutOption:
//                manageBackup(true, false);
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method starts intent to another activity to change settings
     *
     * @return void
     */
    public void settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Logs out the user. It removes user account using AccountManager.
     *
     * @return void
     */
    public void logout() {

        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);

//        parseLogOut();

        AccountManager manager = AccountManager.get(HomeActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.removeAccount(accountList[0], HomeActivity.this, null, null);
            } else {
                manager.removeAccount(accountList[0], null, null); // Legacy implementation
            }
        }

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();

        sessionManager.setReturningUser(false);
    }

//    public void manageBackup(boolean isBackup, boolean isForced) {
//        BackupCloud b = new BackupCloud(this);
//        if (isBackup)
//            b.startCloudBackup(null, false);
//        if (!isBackup) {
//            if (isForced) b.cloudRestoreForced();
//            if (!isForced) b.startCloudRestore();
//        }
//    }

//    private void parseLogOut() {
//        if (NetworkConnection.isOnline(this)) {
//            ParseQuery<ParseObject> getLogin = ParseQuery.getQuery("Login");
//            getLogin.whereEqualTo("userId", sessionManager.getCreatorID());
//            try {
//                List<ParseObject> loginList = getLogin.find();
//                if (loginList != null && !loginList.isEmpty()) {
//                    for (ParseObject login : loginList)
//                        login.delete();
//                }
//            } catch (ParseException e1) {
//                Log.e(TAG, "parseLogOut: ", e1);
//            }
//        }
//    }

    @Override
    protected void onResume() {
        registerReceiver(reMyreceive, filter);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(reMyreceive);
        super.onDestroy();
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to EXIT ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        finish();

                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public class Myreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastSyncTextView.setText("Last Synced:- " + sessionManager.getLastPulledDateTime());
        }
    }

}

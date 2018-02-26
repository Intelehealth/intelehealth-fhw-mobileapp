package io.intelehealth.client.activities.home_activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.acra.ACRA;

import java.io.IOException;
import java.util.Calendar;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.login_activity.LoginActivity;
import io.intelehealth.client.activities.login_activity.OfflineLogin;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.services.DownloadMindmapsTask;


/**
 * Home Screen
 */

public class HomeActivity extends AppCompatActivity {

    String value = "";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor e;
    String backupdate, backuptime;
    Calendar calendar;
    Handler handler;

    private static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.i(TAG, "onCreate: " + getFilesDir().toString());

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_home);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(new HomeAdapter());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        e = sharedPreferences.edit();
        backupdate = sharedPreferences.getString("date", "");
        backuptime = sharedPreferences.getString("time", "");

        final Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.HOUR, 10);
        startDate.set(Calendar.MINUTE, 00);
        startDate.set(Calendar.AM_PM, Calendar.PM);


        final Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR, 10);
        endDate.set(Calendar.MINUTE, 15);
        endDate.set(Calendar.AM_PM, Calendar.PM);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                long start = startDate.getTimeInMillis();
                long end = endDate.getTimeInMillis();
                calendar = Calendar.getInstance();

                if (start < calendar.getTimeInMillis() &&
                        calendar.getTimeInMillis() < end) {
                    // Toast.makeText(HomeActivity.this,"backup started",Toast.LENGTH_SHORT).show();
                    manageBackup();
                }
                handler.postDelayed(this, 1000 * 60);
            }
        }, 1000 * 60);

        handler = new Handler();

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
            case R.id.updateMindmapsOption: {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences.contains("licensekey")) {
                    String license = sharedPreferences.getString("licensekey", null);
                    if (license != null) {
                        DownloadMindmapsTask downloadMindmapsTask = new DownloadMindmapsTask(this);
                        downloadMindmapsTask.execute(license);
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

                                    EditText text = (EditText) d.findViewById(R.id.licensekey);
                                    String key = text.getText().toString();
                                    if (key != null && !key.trim().isEmpty()) {
                                        DownloadMindmapsTask downloadMindmapsTask = new DownloadMindmapsTask(HomeActivity.this);
                                        downloadMindmapsTask.execute(key);
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
            case R.id.backupOption:
                manageBackup();  // to restore app data at any time of the day
                return true;

            case R.id.logoutOption:
                manageBackup();
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
     * This method sync all the data recorded to server and sync back locally.
     *
     * @return void
     */

    public void endOfDay() {
        // TODO: sync all the data recorded to EHR, and sync back locally
        // Information to sync includes credentials and patient info
        // Bandwidth heavy task
        try {
            throw new NullPointerException();
        } catch (NullPointerException caughtException) {
            ACRA.getErrorReporter().handleException(caughtException);
        }
    }

    /**
     * Logs out the user. It removes user account using AccountManager.
     *
     * @return void
     */
    public void logout() {

        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);

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
    }

    public void manageBackup() {
        Backup b = new Backup();
        boolean exists = b.checkDatabaseForData(HomeActivity.this);
        Log.d("data:", String.valueOf(exists));

        if (exists == true) {
            value = "yes";
            e.putString("value", value); //copy to file
        } else if (exists == false) {
            value = "no";
            e.putString("value", value);
        }
        e.apply();

        try {
            b.createFileInMemory(this);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Toast.makeText(this, getString(R.string.backup_completed), Toast.LENGTH_SHORT).show();
    }


}

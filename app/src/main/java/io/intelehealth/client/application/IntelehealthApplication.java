package io.intelehealth.client.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;


import com.parse.Parse;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.utilities.HelperMethods;

/**
 * Created by tusharjois on 9/20/16.
 */
@ReportsCrashes(
        formUri = "https://intelehealth.cloudant.com/acra-intelehealth/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "thisheyetheentmornevessh",
        formUriBasicAuthPassword = "2bf554e018d200e27788367cd2b8ebc259cb80a7",
        //formKey = "", // This is required for backward compatibility but not used
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash
)

public class IntelehealthApplication extends Application implements Application.ActivityLifecycleCallbacks{

    private static Context mContext;
    private Activity currentActivity;

    private static final String TAG = IntelehealthApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        // The following line triggers the initialization of ACRA
        this.mContext = getApplicationContext();
        ACRA.init(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_URL, null);
        if(url==null){
            Log.i(TAG, "onCreate: Parse not init");
        }
        else {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(HelperMethods.IMAGE_APP_ID)
                    .server("http://"+url+":1337/parse/")
                    .build()
            );
            Log.i(TAG, "onCreate: Parse init");
        }

        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();

        localdb.execSQL("UPDATE "+DelayedJobQueueProvider.DELAYED_JOBS_TABLE_NAME +" SET "+ DelayedJobQueueProvider.SYNC_STATUS+"=0");




        registerActivityLifecycleCallbacks(this);
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}

package io.intelehealth.client.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

//Extend Application class with MultiDexApplication for multidex support
public class IntelehealthApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = IntelehealthApplication.class.getSimpleName();
    private static Context mContext;
    private static String androidId;
    private Activity currentActivity;

    public static Context getAppContext() {
        return mContext;
    }

    public static String getAndroidId() {
        return androidId;
    }

//        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);
//        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
//
//        localdb.execSQL("UPDATE "+DelayedJobQueueProvider.DELAYED_JOBS_TABLE_NAME +" SET "+ DelayedJobQueueProvider.SYNC_STATUS+"=0");


//        registerActivityLifecycleCallbacks(this);
//    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //For Vector Drawables Backward Compatibility(<API 21)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mContext = getApplicationContext();

        androidId = String
                .format("%16s", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .replace(' ', '0');

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        String url = sharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_URL, null);
//        if(url==null){
//            Log.i(TAG, "onCreate: Parse not init");
//        }
//        else {
//            Dispatcher dispatcher = new Dispatcher();
//            dispatcher.setMaxRequestsPerHost(1);
//            dispatcher.setMaxRequests(4);
//            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            builder.dispatcher(dispatcher);

//            Parse.initialize(new Parse.Configuration.Builder(this)
//                    .clientBuilder(builder)
//                    .applicationId(HelperMethods.IMAGE_APP_ID)
//                    .server("http://"+url+":1337/parse/")
//                    .build()
//            );
//        Log.i(TAG, "onCreate: Parse init");
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

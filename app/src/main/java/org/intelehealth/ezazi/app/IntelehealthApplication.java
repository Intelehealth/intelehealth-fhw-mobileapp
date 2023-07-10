package org.intelehealth.ezazi.app;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.Parse;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.database.InteleHealthDatabaseHelper;
import org.intelehealth.ezazi.firebase.RealTimeDataChangedObserver;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.klivekit.socket.SocketManager;

import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

//Extend Application class with MultiDexApplication for multidex support
public class IntelehealthApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = IntelehealthApplication.class.getSimpleName();
    private static Context mContext;
    private static String androidId;
    private Activity currentActivity;
    SessionManager sessionManager;

    public static Context getAppContext() {
        return mContext;
    }

    public static String getAndroidId() {
        return androidId;
    }

    private static IntelehealthApplication sIntelehealthApplication;
    public String refreshedFCMTokenID = "";

    private RealTimeDataChangedObserver dataChangedObserver;

    public static IntelehealthApplication getInstance() {
        return sIntelehealthApplication;
    }

    private SocketManager socketManager = SocketManager.getInstance();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIntelehealthApplication = this;
        //For Vector Drawables Backward Compatibility(<API 21)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mContext = getApplicationContext();
        sessionManager = new SessionManager(this);
        // keeping the base url in one singleton object for using in apprtc module

        configureCrashReporting();

        RxJavaPlugins.setErrorHandler(throwable -> {
            // FirebaseCrashlytics.getInstance().recordException(throwable);
        });
        androidId = String
                .format("%16s", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .replace(' ', '0');

        String url = sessionManager.getServerUrl();
        if (url == null) {
            Log.i(TAG, "onCreate: Parse not init");
        } else {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequestsPerHost(1);
            dispatcher.setMaxRequests(4);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.dispatcher(dispatcher);

            Parse.initialize(new Parse.Configuration.Builder(this)
                    .clientBuilder(builder)
                    .applicationId(AppConstants.IMAGE_APP_ID)
                    .server("https://" + url + ":1337/parse/")
                    .build()
            );
            Log.i(TAG, "onCreate: Parse init");

            InteleHealthDatabaseHelper mDbHelper = new InteleHealthDatabaseHelper(this);
            SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
            mDbHelper.onCreate(localdb);
        }
        registerActivityLifecycleCallbacks(this);
        dataChangedObserver = new RealTimeDataChangedObserver(this);
        dataChangedObserver.startObserver();

        initSocketConnection();
    }

    /**
     * Socket should be open and close app level,
     * so when app create open it and close on app terminate
     */
    public void initSocketConnection() {
        Log.d(TAG, "initSocketConnection: ");
        if (sessionManager.getCreatorID() != null && !sessionManager.getCreatorID().isEmpty()) {
            String socketUrl = BuildConfig.SOCKET_URL + "?userId="
                    + sessionManager.getCreatorID()
                    + "&name=" + sessionManager.getChwname();
            if (!socketManager.isConnected()) socketManager.connect(socketUrl);
        }
    }

    private void configureCrashReporting() {
//        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
//                //.disabled(BuildConfig.DEBUG) // comment by Venu as per intelesafe
//                .build();
//        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());

        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Test 1 G
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


    /**
     * for setting the Alert Dialog Custom Font.
     *
     * @param context
     * @param builderDialog
     */
    public static void setAlertDialogCustomTheme(Context context, Dialog builderDialog) {
        // Getting the view elements
        TextView textView = (TextView) builderDialog.getWindow().findViewById(android.R.id.message);
        TextView alertTitle = (TextView) builderDialog.getWindow().findViewById(R.id.alertTitle);
        Button button1 = (Button) builderDialog.getWindow().findViewById(android.R.id.button1);
        Button button2 = (Button) builderDialog.getWindow().findViewById(android.R.id.button2);
        if (textView != null)
            textView.setTypeface(ResourcesCompat.getFont(context, R.font.lato_regular));
        alertTitle.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button1.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button2.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (dataChangedObserver != null) dataChangedObserver.stopObserver();
        if (socketManager.isConnected()) socketManager.disconnect();
    }
}

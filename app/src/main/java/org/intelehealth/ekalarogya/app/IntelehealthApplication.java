package org.intelehealth.ekalarogya.app;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.appcompat.app.AppCompatDelegate;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.Parse;

import org.intelehealth.ekalarogya.BuildConfig;
import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.database.InteleHealthDatabaseHelper;
import org.intelehealth.ekalarogya.firebase.RealTimeDataChangedObserver;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.klivekit.socket.SocketManager;
import org.intelehealth.klivekit.utils.Constants;


import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

//Extend Application class with MultiDexApplication for multidex support
public class IntelehealthApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private static final String TAG = IntelehealthApplication.class.getSimpleName();
    private static Context mContext;
    private static String androidId;
    private Activity currentActivity;
    SessionManager sessionManager;
    public static boolean isInBackground;

    public static Context getAppContext() {
        return mContext;
    }

    public static String getAndroidId() {
        return androidId;
    }

    private static IntelehealthApplication sIntelehealthApplication;
    public String refreshedFCMTokenID = "";
    public String webrtcTempCallId = "";

    public static IntelehealthApplication getInstance() {
        return sIntelehealthApplication;
    }

    private RealTimeDataChangedObserver dataChangedObserver;

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

        configureCrashReporting();

        RxJavaPlugins.setErrorHandler(throwable -> {
            FirebaseCrashlytics.getInstance().recordException(throwable);
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

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        startRealTimeObserverAndSocket();

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree());
        }
    }

    private void configureCrashReporting() {
//        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
//                //.disabled(BuildConfig.DEBUG) // comment by Venu as per intelesafe
//                .build();
//        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);

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
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.lato_regular));
        alertTitle.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button1.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button2.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        isInBackground = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        isInBackground = true;
    }

    public void startRealTimeObserverAndSocket() {
        startRealTimeObserver();
        initSocketConnection();
    }

    private void startRealTimeObserver() {
        if (sessionManager.getProviderID() != null && !sessionManager.getProviderID().isEmpty()) {
            dataChangedObserver = new RealTimeDataChangedObserver(this);
            dataChangedObserver.startObserver();
        }
    }

    /**
     * Socket should be open and close app level,
     * so when app create open it and close on app terminate
     */
    private void initSocketConnection() {
        Log.d(TAG, "initSocketConnection: ");
        if (sessionManager.getCreatorID() != null && !sessionManager.getCreatorID().isEmpty()) {
            String socketUrl = Constants.BASE_URL + "?userId="
                    + sessionManager.getProviderID()
                    + "&name=" + sessionManager.getChwname();
            if (!socketManager.isConnected()) socketManager.connect(socketUrl);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopRealTimeObserverAndSocket();
    }

    public void stopRealTimeObserverAndSocket() {
        if (dataChangedObserver != null) dataChangedObserver.stopObserver();
        if (socketManager.isConnected()) socketManager.disconnect();
    }
}

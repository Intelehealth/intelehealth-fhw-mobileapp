package org.intelehealth.app.activities.homeActivity.heartbeatApi;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.HeartbeatApiRequest;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NavigationUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.videolibrary.utils.ResponseChecker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class HeartbeatApi {

    private final Context context;
    private final SessionManager sessionManager;
    private static final String TAG = "HeartbeatApi";

    public HeartbeatApi(Context context, SessionManager sessionManager) {
        this.context = context;
        this.sessionManager = sessionManager;
    }

    public void pushHeartbeatApiData() {
        HeartbeatApiRequest request = getApiData();
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();
        String url = sessionManager.getServerUrl() + ":3004/api/user/createUpdateStatus";

        Single<Response<ResponseBody>> heartbeatApiCallObservable = AppConstants.apiInterface.heartbeatApiRequest(url, authHeader, request);
        heartbeatApiCallObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> responseBodyResponse) {
                        ResponseChecker<ResponseBody> responseChecker = new ResponseChecker<>(responseBodyResponse);
                        if (responseChecker.isNotAuthorized()) {
                            sessionManager.setJwtAuthToken(null);
                            NavigationUtils navigationUtils = new NavigationUtils();
                            navigationUtils.triggerSignOutOn401Response(context);
                            return;
                        }
                        Logger.logD(TAG, "Success: " + responseBodyResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Error: " + e.getMessage());
                    }
                });
    }

    private HeartbeatApiRequest getApiData() {
        String appVersion = BuildConfig.VERSION_NAME;
        String currentDeviceVersion = Build.VERSION.RELEASE;
        String deviceName = Settings.Global.getString(context.getContentResolver(), "device_name");
        String deviceModel = Build.MODEL;
        long lastSyncTime = getLastSyncTime();

        HeartbeatApiRequest request = new HeartbeatApiRequest();
        request.setUserUuid(sessionManager.getProviderID());
        request.setAndroidVersion(currentDeviceVersion);
        request.setVersion(appVersion);
        request.setCurrentTimestamp(Calendar.getInstance().getTimeInMillis());
        request.setDevice(deviceName);
        request.setDeviceModel(deviceModel);
        request.setLastActivity(getCurrentActivity());
        request.setLastSyncTimestamp(lastSyncTime);
        request.setName(sessionManager.getChwname());
        request.setStatus("Online");
        /*Commenting below code to resolve location type
         * mismatch issue in monitoring sheet and village reports
         * as sending location while sync updating location type at server
         * (AEAT-2322) */
//        request.setVillage(sessionManager.getCurrentLocationName());
//        request.setSecondaryVillage(sessionManager.getSecondaryLocationName());
        request.setSanch(sessionManager.getSanchName());

        return request;
    }

    private long getLastSyncTime() {
        Calendar calendar = Calendar.getInstance();
        long lastSyncTime = 0L;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            if (!sessionManager.getPullExcutedTime().isEmpty()) {
                String dateTime = sessionManager.getPullExcutedTime();
                Date date = format.parse(dateTime);
                lastSyncTime = date != null ? date.getTime() : 0L;
            }
        } catch (Exception e) {
            Timber.d("Heartbeat Api: %s", e.getMessage());
        }
        return lastSyncTime;
    }

    private String getCurrentActivity() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null || manager.getRunningTasks(1) == null || manager.getRunningTasks(1).isEmpty()) {
            return "";
        }

        String currentActivity = "";
        ComponentName componentName = manager.getRunningTasks(1).get(0).topActivity;
        if (componentName != null) {
            String[] activityName = componentName.getClassName().split("\\.");
            if (activityName.length > 1) {
                currentActivity = activityName[activityName.length - 1];
            }
        }
        return currentActivity;
    }
}

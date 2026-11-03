package org.intelehealth.app.reactnative;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.klivekit.data.PreferenceHelper;

import java.util.Map;

/**
 * Bridges an incoming doctor status FCM notification ("Queue Update" with event
 * "doctor:status") to the React Native {@code StatusBannerModule} shown on the
 * home screen — the counterpart of {@link QueueCardUpdater} for the status banner.
 *
 * <p>A {@code doctorStatus} of "away" shows the "Doctor is on Break" banner; any
 * other status removes it. The banner is persisted so it survives the home
 * screen remounting, pushed live via a {@code StatusBannerUpdate} device event
 * when mounted, and announced through {@link #getBannerReceived()} so the home
 * screen can mount or remove it.
 *
 * @see FCMNotificationReceiver
 * @see #EVENT_STATUS_BANNER_UPDATE
 */
public final class StatusBannerUpdater {

    private static final String TAG = "StatusBannerUpdater";

    /** JS event name the {@code HomeStatusBanner} component subscribes to. */
    public static final String EVENT_STATUS_BANNER_UPDATE = "StatusBannerUpdate";

    /** JS event telling every mounted status banner (home and queue screen) to hide. */
    public static final String EVENT_STATUS_BANNER_DISMISS = "StatusBannerDismiss";

    /** Fallback variant when the payload omits one (keeps a valid banner style). */
    private static final String DEFAULT_VARIANT = "alert";

    /** FCM data key holding the doctor's current status, e.g. "away" or "online". */
    private static final String KEY_DOCTOR_STATUS = "doctorStatus";

    /** The only doctor status that shows the "Doctor is on Break" banner. */
    private static final String DOCTOR_STATUS_AWAY = "away";

    /**
     * Posted with the receive time on every banner notification, so a home screen
     * that has no banner mounted yet (none received before it opened) can mount one.
     */
    private static final MutableLiveData<Long> bannerReceived = new MutableLiveData<>();

    private StatusBannerUpdater() {
    }

    public static LiveData<Long> getBannerReceived() {
        return bannerReceived;
    }

    /**
     * Parse a "queue_status" FCM data payload, persist it, and push it to the
     * live banner if the RN context is running.
     */
    public static void handleBannerNotification(Context context, Map<String, String> data) {
        if (context == null || data == null) {
            return;
        }
        if (DOCTOR_STATUS_AWAY.equalsIgnoreCase(data.get(KEY_DOCTOR_STATUS))) {
            StatusBannerData banner = new StatusBannerData(
                    DEFAULT_VARIANT,
                    context.getString(R.string.doctor_on_break),
                    context.getString(R.string.queue_paused),
                    context.getString(R.string.view_queue));
            persist(context, banner);
            RnEventEmitter.emit(EVENT_STATUS_BANNER_UPDATE, toWritableMap(banner));
            bannerReceived.postValue(System.currentTimeMillis());
        } else {
            // Any other doctor status (online, busy, ...) takes the banner down.
            dismiss(context);
        }
    }

    /**
     * Take the banner down on every screen: clear the persisted copy so neither the
     * home nor the queue screen shows it on its next mount, tell any mounted RN
     * banner to hide, and let the home screen collapse its container. Used when the
     * doctor is no longer away and when the user closes (×) the banner on either
     * screen. The next "away" notification shows it again.
     */
    public static void dismiss(Context context) {
        if (context == null) {
            return;
        }
        try {
            new PreferenceHelper(context).clear(PreferenceHelper.STATUS_BANNER_DATA);
        } catch (Exception e) {
            Log.e(TAG, "dismiss failed: " + e.getMessage());
        }
        RnEventEmitter.emit(EVENT_STATUS_BANNER_DISMISS, Arguments.createMap());
        bannerReceived.postValue(System.currentTimeMillis());
    }

    /**
     * The most recently persisted banner payload, or {@code null} if no queue
     * status notification has been received yet. Used by the home fragment to
     * seed the banner on mount.
     */
    @Nullable
    public static StatusBannerData getPersisted(Context context) {
        try {
            String json = new PreferenceHelper(context).getString(PreferenceHelper.STATUS_BANNER_DATA);
            if (TextUtils.isEmpty(json)) {
                return null;
            }
            return new Gson().fromJson(json, StatusBannerData.class);
        } catch (Exception e) {
            Log.e(TAG, "getPersisted failed: " + e.getMessage());
            return null;
        }
    }


    private static void persist(Context context, StatusBannerData banner) {
        try {
            new PreferenceHelper(context)
                    .save(PreferenceHelper.STATUS_BANNER_DATA, new Gson().toJson(banner));
        } catch (Exception e) {
            Log.e(TAG, "persist failed: " + e.getMessage());
        }
    }

    /** Build the JS props map that mirrors the HomeStatusBanner props shape. */
    private static WritableMap toWritableMap(StatusBannerData banner) {
        WritableMap map = Arguments.createMap();
        map.putString("variant", banner.getVariant());
        map.putString("title", banner.getTitle());
        map.putString("subtitle", banner.getSubtitle());
        map.putString("actionLabel", banner.getActionLabel());
        return map;
    }

    /**
     * Build the initial-properties {@link Bundle} for the {@code ReactFragment}
     * that hosts the banner, mirroring {@link #toWritableMap(StatusBannerData)}.
     */
    public static Bundle toBundle(StatusBannerData banner) {
        Bundle bundle = new Bundle();
        bundle.putString("variant", banner.getVariant());
        bundle.putString("title", banner.getTitle());
        bundle.putString("subtitle", banner.getSubtitle());
        bundle.putString("actionLabel", banner.getActionLabel());
        return bundle;
    }
}

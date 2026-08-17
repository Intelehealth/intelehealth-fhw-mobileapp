package org.intelehealth.app.reactnative;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;

import org.intelehealth.klivekit.data.PreferenceHelper;+

import java.util.Map;

/**
 * Bridges an incoming "queue_status" FCM notification to the React Native
 * {@code StatusBannerModule} shown on the home screen — the counterpart of
 * {@link QueueCardUpdater} for the status banner.
 *
 * <p>Each notification carries a complete banner (variant + title + subtitle +
 * action). The payload is persisted so the banner reflects the latest queue
 * status the next time the home screen mounts, and — when the app is in the
 * foreground — pushed live via a {@code StatusBannerUpdate} device event.
 *
 * @see FCMNotificationReceiver
 * @see #EVENT_STATUS_BANNER_UPDATE
 */
public final class StatusBannerUpdater {

    private static final String TAG = "StatusBannerUpdater";

    /** JS event name the {@code HomeStatusBanner} component subscribes to. */
    public static final String EVENT_STATUS_BANNER_UPDATE = "StatusBannerUpdate";

    /** Fallback variant when the payload omits one (keeps a valid banner style). */
    private static final String DEFAULT_VARIANT = "alert";

    private StatusBannerUpdater() {
    }

    /**
     * Parse a "queue_status" FCM data payload, persist it, and push it to the
     * live banner if the RN context is running.
     */
    public static void handleBannerNotification(Context context, Map<String, String> data) {
        if (context == null || data == null) {
            return;
        }
        StatusBannerData banner = parse(data);
        persist(context, banner);
        RnEventEmitter.emit(EVENT_STATUS_BANNER_UPDATE, toWritableMap(banner));
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

    /** Map the FCM string payload onto the banner's fields. */
    private static StatusBannerData parse(Map<String, String> data) {
        String variant = emptyToNull(data.get("variant"));
        if (variant == null) {
            variant = DEFAULT_VARIANT;
        }
        return new StatusBannerData(
                variant,
                emptyToNull(data.get("title")),
                emptyToNull(data.get("subtitle")),
                emptyToNull(data.get("actionLabel"))
        );
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

    @Nullable
    private static String emptyToNull(@Nullable String value) {
        return TextUtils.isEmpty(value) ? null : value;
    }
}

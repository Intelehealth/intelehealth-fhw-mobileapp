package org.intelehealth.app.reactnative;

import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.app.IntelehealthApplication;

/**
 * Small helper for pushing {@code DeviceEventEmitter} events from native to JS.
 *
 * <p>Events are only delivered when a React context is currently alive (app in
 * foreground / RN started). When it isn't, the emit is a no-op — callers that
 * need the data to survive should persist it separately and re-seed on mount.
 *
 * @see QueueCardUpdater
 * @see StatusBannerUpdater
 */
public final class RnEventEmitter {

    private static final String TAG = "RnEventEmitter";

    private RnEventEmitter() {
    }

    /** Emit {@code eventName} with {@code payload} to JS, if a context exists. */
    public static void emit(String eventName, WritableMap payload) {
        try {
            ReactContext reactContext = getCurrentReactContext();
            if (reactContext == null) {
                Log.d(TAG, "emit skipped (" + eventName + "): no active React context");
                return;
            }
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, payload);
        } catch (Exception e) {
            Log.e(TAG, "emit failed (" + eventName + "): " + e.getMessage());
        }
    }

    /** The running React context, or {@code null} if RN isn't started yet. */
    @Nullable
    public static ReactContext getCurrentReactContext() {
        IntelehealthApplication app = IntelehealthApplication.getInstance();
        if (app == null) {
            return null;
        }
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            return app.getReactHost().getCurrentReactContext();
        }
        ReactInstanceManager manager = app.getReactNativeHost().getReactInstanceManager();
        return manager.getCurrentReactContext();
    }
}

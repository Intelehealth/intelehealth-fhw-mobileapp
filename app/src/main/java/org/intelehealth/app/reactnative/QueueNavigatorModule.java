package org.intelehealth.app.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

/**
 * Native bridge that lets the RN queue list open and close the standalone
 * {@link QueueDetailsActivity}. Exposed to JS as "QueueNavigator"
 * (see react-native/src/native/QueueNavigator.ts).
 */
public class QueueNavigatorModule extends ReactContextBaseJavaModule {

    public static final String NAME = "QueueNavigator";

    QueueNavigatorModule(ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Launch Queue Details, forwarding the tapped row (a plain JS object) as the
     * Activity's initial properties.
     */
    @ReactMethod
    public void openQueueDetails(ReadableMap item) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        Bundle props = item != null ? Arguments.toBundle(item) : new Bundle();
        Intent intent = new Intent(activity, QueueDetailsActivity.class);
        intent.putExtra(QueueDetailsActivity.EXTRA_INITIAL_PROPS, props);
        activity.startActivity(intent);
    }

    /** Finish the current Queue Details Activity (back navigation). */
    @ReactMethod
    public void close() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}

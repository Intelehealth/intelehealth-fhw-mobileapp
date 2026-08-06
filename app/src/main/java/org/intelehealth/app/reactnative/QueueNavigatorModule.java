package org.intelehealth.app.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.intelehealth.app.R;

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

    /**
     * Open the Patient's Queue screen from the home screen (e.g. the "View
     * Queue" banner button or the "Open Queue" card button). Selects the Queue
     * bottom-nav item, which reuses the host activity's navigation listener to
     * swap in {@link PatientQueueFragment} with the correct toolbar setup.
     */
    @ReactMethod
    public void openPatientQueue() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_nav_home);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.bottom_nav_queue);
            }
        });
    }

    /**
     * Collapse the home status-banner container after the RN banner is dismissed.
     * The banner is hosted in a native FrameLayout whose margin would otherwise
     * leave a gap once the RN view renders nothing; setting it GONE lets the
     * Add Patient card move up to its original position (see the
     * layout_goneMarginTop on addpatient_cardview).
     */
    @ReactMethod
    public void dismissStatusBanner() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            View container = activity.findViewById(R.id.status_banner_container);
            if (container != null) {
                container.setVisibility(View.GONE);
            }
        });
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

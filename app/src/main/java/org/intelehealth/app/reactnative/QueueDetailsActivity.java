package org.intelehealth.app.reactnative;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.facebook.react.ReactFragment;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import org.intelehealth.app.R;
import org.intelehealth.app.shared.BaseActivity;

/**
 * Standalone Activity host for the React Native "Queue Details" screen
 * (component "QueueDetailsModule", see react-native/index.js).
 *
 * The toolbar is a native layout copied from the Visit Details screen (see
 * activity_queue_details.xml) so the two screens share the same style; the React
 * component is embedded below the toolbar via a {@link ReactFragment}, mirroring
 * {@link PatientQueueFragment}. The tapped queue row arrives from
 * {@link QueueNavigatorModule} as a Bundle under {@link #EXTRA_INITIAL_PROPS}
 * and is forwarded to React as the component's initial properties.
 */
public class QueueDetailsActivity extends BaseActivity
        implements DefaultHardwareBackBtnHandler {

    public static final String EXTRA_INITIAL_PROPS = "initialProperties";

    private static final String RN_COMPONENT_NAME = "QueueDetailsModule";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_details);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        handleBackPress();

        ImageButton backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> finish());

        // Attach the RN component once; the FragmentManager restores it on recreation.
        if (savedInstanceState == null) {
            Bundle props = getIntent().getBundleExtra(EXTRA_INITIAL_PROPS);

            ReactFragment reactFragment = new ReactFragment.Builder()
                    .setComponentName(RN_COMPONENT_NAME)
                    .setLaunchOptions(props != null ? props : new Bundle())
                    .build();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.queue_details_container, reactFragment)
                    .commit();
        }
    }


    private void handleBackPress () {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    /**
     * Required by {@link ReactFragment}'s host contract: React calls this to let
     * the hardware back button fall through to the Activity's default handling.
     */
    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }
}

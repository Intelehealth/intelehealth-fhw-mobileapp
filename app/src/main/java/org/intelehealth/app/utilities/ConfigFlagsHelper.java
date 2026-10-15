package org.intelehealth.app.utilities;

import android.content.Context;

import androidx.annotation.WorkerThread;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.klivekit.data.PreferenceHelper;

/**
 * Keeps the server-config feature flags the app reads from prefs (QMS, edit visit) in step with
 * the stored config, and tracks for which app version the config was last fetched.
 * <p>
 * After an app update the stored config may have been parsed with the previous version's keys, so
 * until a fresh fetch succeeds those flags are not taken from it (see {@link #isRefreshPending}).
 */
public final class ConfigFlagsHelper {

    private ConfigFlagsHelper() {
    }

    /** True until a config fetch has succeeded on this app version. */
    public static boolean isRefreshPending(PreferenceHelper preferenceHelper) {
        int syncedVersion = preferenceHelper.get(PreferenceHelper.CONFIG_SYNCED_APP_VERSION, 0);
        return syncedVersion != BuildConfig.VERSION_CODE;
    }

    /** Saves the QMS / edit-visit flags from the stored config row. */
    public static void applyFlags(PreferenceHelper preferenceHelper, FeatureActiveStatus status) {
        preferenceHelper.save(PreferenceHelper.IS_QMS_CONFIGURE, status.getActiveQms());
        preferenceHelper.save(PreferenceHelper.IS_EDIT_VISIT_CONFIGURE, true/*status.getActiveEditVisit()*/);
    }

    /**
     * Call after a config fetch succeeded (splash, pull sync, post-update refresh). Reads the
     * freshly written row directly instead of waiting for the LiveData observer, which may already
     * have fired (and been skipped while the refresh was pending), then marks this app version as
     * synced.
     */
    @WorkerThread
    public static void onConfigSynced(Context context) {
        Context appContext = context.getApplicationContext();
        PreferenceHelper preferenceHelper = new PreferenceHelper(appContext);
        FeatureActiveStatus status = ConfigDatabase.getInstance(appContext).featureActiveStatusDao().getRecordNow();
        if (status != null) {
            applyFlags(preferenceHelper, status);
        }
        preferenceHelper.save(PreferenceHelper.CONFIG_SYNCED_APP_VERSION, BuildConfig.VERSION_CODE);
    }
}

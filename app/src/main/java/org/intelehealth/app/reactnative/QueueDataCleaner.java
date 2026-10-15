package org.intelehealth.app.reactnative;

import android.content.Context;

import org.intelehealth.app.database.dao.QueueDAO;
import org.intelehealth.klivekit.data.PreferenceHelper;

/**
 * Clears the locally kept queue (QMS) data on logout. The queue, the "Next In Queue" card and the
 * status banner all belong to the signed-in health worker; without this, the next user to log in
 * on the device sees the previous user's patients and status until new data arrives.
 */
public final class QueueDataCleaner {

    private QueueDataCleaner() {
    }

    public static void clearOnLogout(Context context) {
        PreferenceHelper preferenceHelper = new PreferenceHelper(context.getApplicationContext());
        preferenceHelper.clear(PreferenceHelper.QUEUE_CARD_DATA);
        preferenceHelper.clear(PreferenceHelper.STATUS_BANNER_DATA);
        new QueueDAO().deleteAll();
    }
}

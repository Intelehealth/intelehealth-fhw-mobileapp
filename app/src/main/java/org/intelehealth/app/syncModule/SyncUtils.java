package org.intelehealth.app.syncModule;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.sync.AppointmentSync;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.optimized_sync.OptimizedSyncWorker;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;

public class SyncUtils {


    private static final String TAG = SyncUtils.class.getSimpleName();

    /**
     * This method will be responsible for initial sync/setup
     *
     * @param fromActivity
     */
    public void initialSync(String fromActivity,Context context) {

        SyncDAO syncDAO = new SyncDAO();
        Logger.logD(TAG, "Pull Started");
        syncDAO.pullDataBackgroundService(IntelehealthApplication.getAppContext(), fromActivity,0);
        Logger.logD(TAG, "Pull ended");
        // sync data
        AppointmentSync.getAppointments(context);
    }

    /**
     * Requests a sync.
     *
     * The sequence itself lives in OptimizedSyncDao and blocks from end to end, so it cannot be run on
     * the caller's thread: most of the callers here are click handlers and menu actions on the main
     * thread, which the previous implementation got away with only because every step inside it was
     * asynchronous. Enqueuing a worker keeps those callers non-blocking and puts every sync in the app
     * on one path.
     *
     * Requests made in quick succession queue behind one another rather than overlapping, which is the
     * point of the sequence being blocking in the first place.
     */
    public void syncInBackground() {
        Logger.logD(TAG, "Sync requested");
        OptimizedSyncWorker.enqueueOneTimeWork(IntelehealthApplication.getAppContext());
    }

    public void syncBackground() {
        syncInBackground();
    }

    /**
     * Requests a sync and reports that it was accepted.
     *
     * The boolean this returns has never described the outcome of a sync. It was the return of
     * pushDataApi, which subscribes its request on a background scheduler and returns before the call
     * is made, so it was true in every case that reached a caller. The screens gating on it are
     * therefore unaffected, and the completion they actually respond to arrives, as it did before, on
     * the sync broadcast they already listen for.
     *
     * @param fromActivity retained so the existing call sites need no change; the pull no longer varies
     *                     by caller.
     */
    public boolean syncForeground(String fromActivity) {
        syncInBackground();
        return true;
    }

    /**
     * Clicking on this btn will start Sync.
     *
     * @param view Refresh button view.
     */
    public static boolean syncNow(Context context, View view, ObjectAnimator syncAnimator) {
        boolean isSynced = false;

        syncAnimator = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setInterpolator(new LinearInterpolator());

        if (NetworkConnection.isOnline(context)) {
            //Toast.makeText(context, context.getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
            view.clearAnimation();
            syncAnimator.start();
            new SyncUtils().syncBackground();

            isSynced = true;
            new Handler(Looper.getMainLooper())
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(context, context.getString(R.string.successfully_synced), Toast.LENGTH_SHORT).show();
                        }
                    }, 1200);

        } else {
            isSynced = false;
            Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
        }

        return isSynced;
    }

}

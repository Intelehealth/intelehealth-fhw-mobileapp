package app.intelehealth.client.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;

import app.intelehealth.client.activities.visitSummaryActivity.VisitSummaryActivity;

/**
 * Created by Prajwal Waingankar
 * on 24-Aug-20.
 * Github: prajwalmw
 */


public class OnClearFromRecentService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearFromRecentService", "Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearFromRecentService", "END");
        //Code here
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
        File dir = new File(path);
        VisitSummaryActivity.deleteRecursive(dir);
    }
}

package io.intelehealth.client.syncModule;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import io.intelehealth.client.R;
import io.intelehealth.client.database.dao.ImagesPushDAO;
import io.intelehealth.client.database.dao.PullDataDAO;
import io.intelehealth.client.utilities.Logger;

public class SyncWorkManager extends Worker {


    String TAG = SyncWorkManager.class.getSimpleName();

    public SyncWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        Logger.logD(TAG, "result job");
        PullDataDAO pullDataDAO = new PullDataDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();

//        if (pull)
//            sendNotification("Sync", "Synced Data");
//        else
//            sendNotification("Sync", "failed to Sync");

        pullDataDAO.pushDataApi();

//        if (push)
//            sendNotification("Sync", "Synced Data");
//        else
//            sendNotification("Sync", "failed to Sync");
//

        imagesPushDAO.patientProfileImagesPush();
        imagesPushDAO.obsImagesPush();

        pullDataDAO.pullData(applicationContext);

        return Result.success();
    }

    public void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher);

        notificationManager.notify(1, notification.build());
    }
}


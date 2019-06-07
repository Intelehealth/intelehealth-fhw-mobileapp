package io.intelehealth.client.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.dao.ObsDAO;
import io.intelehealth.client.models.download.Download;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UrlModifiers;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DownloadService extends IntentService {
    static String TAG = DownloadService.class.getSimpleName();
    UrlModifiers urlModifiers = new UrlModifiers();
    ObsDAO obsDAO = new ObsDAO();
    SessionManager sessionManager = null;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials;
    private int totalFileSize;

    public DownloadService() {
        super("Download Service");
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_download)
//                .setContentTitle("Download")
//                .setContentText("Downloading File")
//                .setAutoCancel(true);
//        notificationManager.notify(0, notificationBuilder.build());
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
        }
        AppConstants.notificationUtils.showNotificationProgress("Download", "Downloading File", IntelehealthApplication.getAppContext(), 0);
        initDownload();

    }

    private void initDownload() {

        String url = "";
        List<String> imageObsList = new ArrayList<>();
        imageObsList = obsDAO.getImageStrings("07a816ce-ffc0-49b9-ad92-a1bf9bf5e2ba", encounterAdultIntials);
        for (int i = 0; i < imageObsList.size(); i++) {
            url = urlModifiers.obsImageUrl(imageObsList.get(i));
            Observable<ResponseBody> downloadobs = AppConstants.apiInterface.OBS_IMAGE_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
            int finalI = i;
            downloadobs.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ResponseBody>() {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                downloadFile(responseBody, finalI);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "onerror" + e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "oncomplete");
                        }
                    });
        }

    }

    private void downloadFile(ResponseBody body, int i) throws IOException {

        int count;
        byte[] data = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file" + i + ".jpg");
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                sendNotification(download);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete();
        output.flush();
        output.close();
        bis.close();

    }

    private void sendNotification(Download download) {

        sendIntent(download);
//        notificationBuilder.setProgress(100,download.getProgress(),false);
//        notificationBuilder.setContentText("Downloading file "+ download.getCurrentFileSize() +"/"+totalFileSize +" MB");
//        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendIntent(Download download) {

        Intent intent = new Intent(AppConstants.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete() {

        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);

        AppConstants.notificationUtils.showNotificationProgress("Download", "File Downloaded", IntelehealthApplication.getAppContext(), 100);

//        notificationManager.cancel(0);
//        notificationBuilder.setProgress(0,0,false);
//        notificationBuilder.setContentText("File Downloaded");
//        notificationManager.notify(0, notificationBuilder.build());

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
//        notificationManager.cancel(0);
    }

}

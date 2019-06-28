package io.intelehealth.client.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import com.crashlytics.android.Crashlytics;

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
import io.intelehealth.client.database.dao.ImagesDAO;
import io.intelehealth.client.database.dao.ObsDAO;
import io.intelehealth.client.models.download.Download;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UrlModifiers;
import io.intelehealth.client.utilities.UuidDictionary;
import io.intelehealth.client.utilities.exception.DAOException;
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
    private String imgPrefix = "AD";
    final private String imageAD = "Additional Documents";
    final private String imagePE = "Physical Exam";
    public String baseDir = "";
    public String ImageType = "";
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
            ImageType = intent.getStringExtra("ImageType");
        }
        AppConstants.notificationUtils.showNotificationProgress("Download", "Downloading File", IntelehealthApplication.getAppContext(), 0);
        if (ImageType.equalsIgnoreCase(UuidDictionary.COMPLEX_IMAGE_AD))
            baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "Patient Images" + File.separator + patientUuid + File.separator + visitUuid + File.separator + imageAD + File.separator;
        else
            baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "Patient Images" + File.separator + patientUuid + File.separator + visitUuid + File.separator + imagePE + File.separator;

        initDownload(ImageType);

    }

    private void initDownload(String ImageType) {

        String url = "";
        List<String> imageObsList = new ArrayList<>();
        imageObsList = obsDAO.getImageStrings(ImageType, encounterAdultIntials);
        String imageType = "";
        if (ImageType.equalsIgnoreCase(UuidDictionary.COMPLEX_IMAGE_AD))
            imageType = "AD";
        else
            imageType = "PE";
        for (int i = 0; i < imageObsList.size(); i++) {
            url = urlModifiers.obsImageUrl(imageObsList.get(i));
            Observable<ResponseBody> downloadobs = AppConstants.apiInterface.OBS_IMAGE_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
            int finalI = i;
            String finalImageType = imageType;
            List<String> finalImageObsList = imageObsList;
            int finalI1 = i;
            downloadobs.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ResponseBody>() {
                        @Override
                        public void onNext(ResponseBody responseBody) {

                            try {
                                downloadFile(responseBody, baseDir + finalImageObsList.get(finalI1) + "_" + finalImageType, finalImageType);
                            } catch (IOException e) {
                                Crashlytics.getInstance().core.logException(e);
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

    private void downloadFile(ResponseBody body, String mImageName, String imageType) throws IOException {
        String imagepath = mImageName + "_" + System.currentTimeMillis() + ".jpg";
        int count;
        byte[] data = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(imagepath);
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

        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(patientUuid, visitUuid, encounterAdultIntials, imagepath, imageType);
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }

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


        Intent i = new Intent();
        i.setAction("MY_BROADCAST_IMAGE_DOWNLAOD");
        sendBroadcast(i);


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

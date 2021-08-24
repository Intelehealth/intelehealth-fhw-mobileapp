package org.intelehealth.msfarogyabharat.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.intelehealth.msfarogyabharat.models.ObsImageModel.AddImageDownloadResponse;
import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.dao.ImagesDAO;
import org.intelehealth.msfarogyabharat.database.dao.ObsDAO;
import org.intelehealth.msfarogyabharat.models.download.Download;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DownloadService extends IntentService {
    static String TAG = DownloadService.class.getSimpleName();
    UrlModifiers urlModifiers = new UrlModifiers();
    ObsDAO obsDAO = new ObsDAO();
    SessionManager sessionManager = null;
    private String encounterAdultIntials;
    private int totalFileSize;
    public String baseDir = "";
    public String ImageType = "";
    private String patientUuid = "";
    List<String> mfilename = new ArrayList<>();
    String url = "";

    public DownloadService() {
        super("Download Service");
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            ImageType = intent.getStringExtra("ImageType");
            patientUuid = intent.getStringExtra("patientUuid");
        }
//        AppConstants.notificationUtils.showNotificationProgress("Download", "Downloading File", 4, IntelehealthApplication.getAppContext(), 0);

        baseDir = AppConstants.IMAGE_PATH;

        try {
            initDownload(ImageType);
        } catch (DAOException e) {
            e.printStackTrace();
        }
      //  initDownload(ImageType);

    }

/*
    private void initDownload(String ImageType) {

        String url = "";
        List<String> imageObsList = new ArrayList<>();
        imageObsList = obsDAO.getImageStrings(ImageType, encounterAdultIntials);
        if (imageObsList.size() == 0) {
//            AppConstants.notificationUtils.DownloadDone("Download", "No Images to Download", 4, IntelehealthApplication.getAppContext());
        }
        for (int i = 0; i < imageObsList.size(); i++) {
            url = urlModifiers.obsImageUrl(imageObsList.get(i));
            Observable<ResponseBody> downloadobs = AppConstants.apiInterface.OBS_IMAGE_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
            int finalI1 = i;
            List<String> finalImageObsList1 = imageObsList;
            downloadobs.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ResponseBody>() {
                        @Override
                        public void onNext(ResponseBody responseBody) {

                            try {
                                downloadFile(responseBody, finalImageObsList1.get(finalI1));
                            } catch (IOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
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
*/


    private void initDownload(String ImageType) throws DAOException {
        List<String> imageObsList = new ArrayList<>();
        ImagesDAO imagesDAO = new ImagesDAO();
        imageObsList = obsDAO.getImageStrings(ImageType, encounterAdultIntials);

        if (imageObsList.size() == 0) {
            //do something...
        }

        for (int i = 0; i < imageObsList.size(); i++) {
            String downloadurl = "";
            downloadurl = urlModifiers.obsImageFilenameDownlaodUrl(patientUuid, imageObsList.get(i));

            Observable<AddImageDownloadResponse> responseObservable =
                    AppConstants.apiInterface.OBS_IMAGE_FILENAME_DOWNLOAD(downloadurl,
                            "Basic " + sessionManager.getEncoded());

            int finalI = i;
            List<String> finalImageObsList = imageObsList;
            responseObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<AddImageDownloadResponse>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(AddImageDownloadResponse addImageDownloadResponse) {
                            mfilename.add(addImageDownloadResponse.getData().get(0).getImageName());
                            Log.v("main", "download_filename: "+ mfilename);

                            url = urlModifiers.obsImageUrl(finalImageObsList.get(finalI));
                            List<String> final_mfilename = mfilename; //TODO: check if this obsuuid and the above one is correct or not...

                            //Image Pull API.....
                            Observable<ResponseBody> downloadobs = AppConstants.apiInterface.OBS_IMAGE_DOWNLOAD
                                    (url, "Basic " + sessionManager.getEncoded());
                            downloadobs.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new DisposableObserver<ResponseBody>() {
                                        @Override
                                        public void onNext(ResponseBody responseBody) {
                                            try {
                                                //  downloadFile(responseBody, finalImageObsList1.get(finalI1));
                                                downloadFile(responseBody, final_mfilename.get(finalI), finalImageObsList.get(finalI));
                                            } catch (IOException e) {
                                                FirebaseCrashlytics.getInstance().recordException(e);
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

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(DownloadService.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });

            //TODO: This will return the filename for that obs uuid...
        }

    }

   /* private String download_ImageFile(String patientUuid, String obsUuid) {

    }*/

    private void downloadFile(ResponseBody body, String image_filename, String obsUuid) throws IOException {
        String imagepath = image_filename + ".jpg";
        int count;
        byte[] data = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(baseDir + imagepath);
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
            imagesDAO.updateObs(obsUuid);
            imagesDAO.insertInto_tbl_additional_doc(UUID.randomUUID().toString(), patientUuid, obsUuid, image_filename, "TRUE");
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    private void sendNotification(Download download) {

        sendIntent(download);
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

//        AppConstants.notificationUtils.showNotificationProgress("Download", "File Downloaded", 4, IntelehealthApplication.getAppContext(), 100);


    }


}

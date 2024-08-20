package org.intelehealth.vikalphelpline.services;

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
import java.util.concurrent.Executors;

import org.intelehealth.vikalphelpline.models.ObsImageModel.AddImageDownloadResponse;
import org.intelehealth.vikalphelpline.models.ObsImageModel.Add_Doc_DataModel;
import org.intelehealth.vikalphelpline.utilities.Base64Utils;
import org.intelehealth.vikalphelpline.utilities.Logger;
import org.intelehealth.vikalphelpline.utilities.SessionManager;
import org.intelehealth.vikalphelpline.utilities.UrlModifiers;
import org.intelehealth.vikalphelpline.app.AppConstants;
import org.intelehealth.vikalphelpline.app.IntelehealthApplication;
import org.intelehealth.vikalphelpline.database.dao.ImagesDAO;
import org.intelehealth.vikalphelpline.database.dao.ObsDAO;
import org.intelehealth.vikalphelpline.models.download.Download;
import org.intelehealth.vikalphelpline.utilities.exception.DAOException;
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
    List<Add_Doc_DataModel> dataModels = new ArrayList<>();
    String url = "";
    Observable<AddImageDownloadResponse> responseObservable;
    Observable<ResponseBody> downloadobs;
    String encoded = "";
    Base64Utils base64Utils = new Base64Utils();

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


    private void initDownload(String ImageType) throws DAOException {
        encoded = base64Utils.encoded("intelehealthUser", "IHUser#1");

        List<String> imageObsList = new ArrayList<>();
        ImagesDAO imagesDAO = new ImagesDAO();
        imageObsList = obsDAO.getImageStrings(ImageType, encounterAdultIntials);

        if (imageObsList.size() == 0) {
            //do something...
        }

        for (int i = 0; i < imageObsList.size(); i++) {
            Log.v(TAG, "image_list: "+imageObsList.get(i)); //image list
            String downloadurl = "";
            downloadurl = urlModifiers.obsImageFilenameDownlaodUrl(patientUuid, imageObsList.get(i));

            responseObservable =
                    AppConstants.apiInterface.OBS_IMAGE_FILENAME_DOWNLOAD(downloadurl,
                            "Basic " + encoded);

            int finalI = i;
            List<String> finalImageObsList = imageObsList;
          //  List<String> finalImageObsList1 = imageObsList;
            responseObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<AddImageDownloadResponse>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(AddImageDownloadResponse addImageDownloadResponse) {
                            Log.v(TAG, "download_filename: "+ addImageDownloadResponse.getData().get(0).getImageName() +
                                    addImageDownloadResponse.getData().get(0).getObsId());

                            dataModels.add(new Add_Doc_DataModel
                                    (addImageDownloadResponse.getData().get(0).getObsId(),
                                            addImageDownloadResponse.getData().get(0).getImageName()));

                            if(finalImageObsList.size() == dataModels.size()) {

                                for (int j = 0; j < dataModels.size(); j++) {
                                    url = urlModifiers.obsImageUrl(dataModels.get(j).getObsuuid());
                                    Log.v(TAG, "url_downlaodimage: "+url);
                                    //Image Pull API.....
                                    downloadobs = AppConstants.apiInterface.OBS_IMAGE_DOWNLOAD
                                            (url, "Basic " + encoded);
                                    int finalJ = j;
                                    downloadobs.subscribeOn(Schedulers.from(Executors.newFixedThreadPool(1))) //TODO: scehduler.from....
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new DisposableObserver<ResponseBody>() {
                                                @Override
                                                public void onNext(ResponseBody responseBody) {
                                                    try {
                                                        downloadFile(responseBody, dataModels.get(finalJ).getFilename(), dataModels.get(finalJ).getObsuuid());
                                                        Log.v(TAG, dataModels.get(finalJ).getFilename() + ":" + dataModels.get(finalJ).getObsuuid());

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


                           // url = urlModifiers.obsImageUrl(finalImageObsList.get(finalI));
                           // List<String> final_mfilename = mfilename; //TODO: check if this obsuuid and the above one is correct or not...



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
            imagesDAO.insertInto_tbl_additional_doc(UUID.randomUUID().toString(), patientUuid, encounterAdultIntials, obsUuid, image_filename, "0", "TRUE");
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
        intent.setPackage(IntelehealthApplication.getAppContext().getPackageName());
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);


        Intent i = new Intent();
        i.setAction("MY_BROADCAST_IMAGE_DOWNLAOD");
        i.setPackage(IntelehealthApplication.getAppContext().getPackageName());
        sendBroadcast(i);


    }


    private void onDownloadComplete() {

        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);

//        AppConstants.notificationUtils.showNotificationProgress("Download", "File Downloaded", 4, IntelehealthApplication.getAppContext(), 100);


    }


}

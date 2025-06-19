package org.intelehealth.app.database.dao;

import android.content.Intent;
import org.intelehealth.app.utilities.CustomLog;

import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.app.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.app.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.app.models.providerImageRequestModel.ProviderProfile;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class ImagesPushDAO {
    String TAG = ImagesPushDAO.class.getSimpleName();
    SessionManager sessionManager = null;
    ProviderProfile providerProfile = null;


    public boolean patientProfileImagesPush() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        String url = urlModifiers.setPatientProfileImageUrl();
        List<PatientProfile> patientProfiles = new ArrayList<>();
        try {
            patientProfiles = imagesDAO.getPatientProfileUnsyncedImages();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        // CustomLog.d(TAG, "patientProfileImagesPush: getBase64EncodedImage "+ patientProfile.getBase64EncodedImage());
        for (PatientProfile p : patientProfiles) {
            Single<ResponseBody> personProfilePicUpload = AppConstants.apiInterface.PERSON_PROFILE_PIC_UPLOAD(url, "Basic " + encoded, p);
            personProfilePicUpload.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody responseBody) {
                            Logger.logD(TAG, "success" + responseBody);
                            try {
                                imagesDAO.updateUnsyncedPatientProfile(p.getPerson(), "PP");
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Uploaded Patient Profile", 4, IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                            e.printStackTrace();
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
                        }
                    });
        }
        sessionManager.setPullSyncFinished(true);
        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE)
                .setPackage(IntelehealthApplication.getAppContext().getPackageName()));
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;
    }

    public boolean obsImagesPush() {

        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        String url = urlModifiers.setObsImageUrl();
        List<ObsPushDTO> obsImageJsons = new ArrayList<>();
        try {
            obsImageJsons = imagesDAO.getObsUnsyncedImages();
            CustomLog.e(TAG, "image request model" + gson.toJson(obsImageJsons));
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        int i = 0;
        for (ObsPushDTO p : obsImageJsons) {
            //pass it like this
            File file = null;
            file = new File(AppConstants.IMAGE_PATH + p.getUuid() + ".jpg");
            CustomLog.e(TAG, "File size:" + file.length());
            RequestBody requestFile = RequestBody.create(file,MediaType.parse("application/json"));
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Observable<ObsJsonResponse> obsJsonResponseObservable = AppConstants.apiInterface.OBS_JSON_RESPONSE_OBSERVABLE(url, "Basic " + encoded, body, p);
            obsJsonResponseObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ObsJsonResponse>() {
                        @Override
                        public void onNext(ObsJsonResponse obsJsonResponse) {
                            Logger.logD(TAG, "success" + obsJsonResponse);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "success");
                            try {
                                imagesDAO.updateUnsyncedObsImages(p.getUuid());
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                    });
        }
        sessionManager.setPushSyncFinished(true);
        //sometimes syncing happening while logout
        //added the checking to prevent any broadcaster receiver event while logout
        if(!sessionManager.isLogout()){
            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_OBS_IMAGE_PUSH_DONE)
                    .setPackage(IntelehealthApplication.getAppContext().getPackageName()));
        }
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;
    }

    public boolean deleteObsImage() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        List<String> voidedObsImageList = new ArrayList<>();
        try {
            voidedObsImageList = imagesDAO.getVoidedImageObs();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        for (String voidedObsImage : voidedObsImageList) {
            String url = urlModifiers.obsImageDeleteUrl(voidedObsImage);
            Observable<Void> deleteObsImage = AppConstants.apiInterface.DELETE_OBS_IMAGE(url, "Basic " + encoded);
            deleteObsImage.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Void>() {
                        @Override
                        public void onNext(Void aVoid) {
                            Logger.logD(TAG, "success" + aVoid);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "successfully Deleted the images from server");
                        }
                    });
        }
        return true;
    }

    //newly added for profile picture upload- ui2.0
    public boolean loggedInUserProfileImagesPush() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        String url = urlModifiers.setProviderProfileImageUrl();
        try {
            providerProfile = imagesDAO.getUserProfileUnsyncedImages(sessionManager.getProviderID());


        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (providerProfile != null && providerProfile.getFile() != null && !providerProfile.getFile().isEmpty()) {
            CustomLog.d(TAG, "loggedInUserProfileImagesPush: in conditions : ");

            Single<ResponseBody> personProfilePicUpload = AppConstants.apiInterface.PROVIDER_PROFILE_PIC_UPLOAD(url, providerProfile, "Basic " + encoded);
            personProfilePicUpload.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody responseBody) {
                            try {
                                Timber.tag(TAG).d("Image upload success =>%s", new Gson().toJson(responseBody));
                                imagesDAO.updateUnsyncedUserProfile(providerProfile.getProviderid());

                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.tag(TAG).e("Image upload fail =>%s", e.getMessage());
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                        }
                    });
        }
        sessionManager.setPullSyncFinished(true);
        //sometimes syncing happening while logout
        //added the checking to prevent any broadcaster receiver event while logout
        if(!sessionManager.isLogout()){
            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE)
                    .setPackage(IntelehealthApplication.getAppContext().getPackageName()));
        }
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;
    }


}

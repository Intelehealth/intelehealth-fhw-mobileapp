package io.intelehealth.client.database.dao;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.models.ObsImageModel.ObsJsonRequest;
import io.intelehealth.client.models.ObsImageModel.ObsJsonResponse;
import io.intelehealth.client.models.patientImageModelRequest.PatientProfile;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.UrlModifiers;
import io.intelehealth.client.utilities.exception.DAOException;
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

//    public PatientProfile PatientProfileFrameJson(String uuid) {
//
//
//        PatientProfile patientProfile = new PatientProfile();
//        patientProfile.setBase64EncodedImage("");
//        patientProfile.setPerson("");
//
//
//        return patientProfile;
//    }


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
            Crashlytics.getInstance().core.logException(e);
        }
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
                                Crashlytics.getInstance().core.logException(e);
                            }
                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Uploaded Patient Profile", IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                        }
                    });
        }
        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", IntelehealthApplication.getAppContext());
        return true;
    }

    public boolean obsImagesPush() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        String url = urlModifiers.setObsImageUrl();
        List<ObsJsonRequest> obsImageJsons = new ArrayList<>();
        try {
            obsImageJsons = imagesDAO.getObsUnsyncedImages();
        } catch (DAOException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        int i = 0;
        for (ObsJsonRequest p : obsImageJsons) {
            //pass it like this
            File file = null;
            try {
                file = new File(imagesDAO.getobsImagePath(p.getUuid()));
            } catch (DAOException e) {
                Crashlytics.getInstance().core.logException(e);
            }
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            Observable<ObsJsonResponse> obsJsonResponseObservable = AppConstants.apiInterface.OBS_JSON_RESPONSE_OBSERVABLE(url, "Basic " + encoded, body, p);
            obsJsonResponseObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ObsJsonResponse>() {
//                        @Override
//                        public void onSuccess(ResponseBody responseBody) {
//                            Logger.logD(TAG, "success" + responseBody);
//                            try {
//                                imagesDAO.updateUnsyncedPatientProfile(p.getPerson(), "PP");
//                            } catch (DAOException e) {
//                                  Crashlytics.getInstance().core.logException(e);
//                            }
//                            AppConstants.notificationUtils.showNotificationProgress("Patient Profile", "Uploading Patient Profile", IntelehealthApplication.getAppContext(), finalI);
//                        }

                        @Override
                        public void onNext(ObsJsonResponse obsJsonResponse) {
                            Logger.logD(TAG, "success" + obsJsonResponse);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "success");
                            try {
                                imagesDAO.updateUnsyncedObsImages(p.getUuid());
                            } catch (DAOException e) {
                                Crashlytics.getInstance().core.logException(e);
                            }
                        }
                    });
        }
        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", IntelehealthApplication.getAppContext());
        return true;
    }


}

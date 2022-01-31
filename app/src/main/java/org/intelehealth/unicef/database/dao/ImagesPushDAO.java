package org.intelehealth.unicef.database.dao;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.apprtc.utils.SendMessageUtils;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.unicef.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.unicef.models.ObsImageModel.ObsPushDTOMain;
import org.intelehealth.unicef.models.dto.RTCConnectionDTO;
import org.intelehealth.unicef.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.UrlModifiers;
import org.intelehealth.unicef.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

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
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
                        }
                    });
        }
        sessionManager.setPullSyncFinished(true);
        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE));
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
        Logger.logD(TAG, "url - " + url);
        List<ObsPushDTOMain> obsImageJsons = new ArrayList<ObsPushDTOMain>();
        try {
            obsImageJsons = imagesDAO.getObsUnsyncedImages();
            Log.e(TAG, "image request model" + gson.toJson(obsImageJsons));
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        int i = 0;
        for (ObsPushDTOMain obsPushDTOMain : obsImageJsons) {
            ObsPushDTO obsPushDTO = new ObsPushDTO();
            obsPushDTO.setConcept(obsPushDTOMain.getConcept());
            obsPushDTO.setEncounter(obsPushDTOMain.getEncounter());
            obsPushDTO.setObsDatetime(obsPushDTOMain.getObsDatetime());
            obsPushDTO.setUuid(obsPushDTOMain.getUuid());
            obsPushDTO.setPerson(obsPushDTOMain.getPerson());
            //pass it like this
            File file = null;
            file = new File(AppConstants.IMAGE_PATH + obsPushDTO.getUuid() + ".jpg");
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/json"), file);
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Observable<ObsJsonResponse> obsJsonResponseObservable = AppConstants.apiInterface.OBS_JSON_RESPONSE_OBSERVABLE(url, "Basic " + encoded, body, obsPushDTO);
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
                                imagesDAO.updateUnsyncedObsImages(obsPushDTOMain.getUuid());
                                // send messages after image upload
                                RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
                                RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(obsPushDTOMain.getVisitUUID());
                                if (rtcConnectionDTO != null) {
                                    // send message in chat that new images uploaded for visit
                                    try {
                                        JSONObject jsonObject = new JSONObject(rtcConnectionDTO.getConnectionInfo());
                                        SendMessageUtils.postMessages(IntelehealthApplication.getAppContext(),
                                                rtcConnectionDTO.getVisitUUID(),
                                                obsPushDTOMain.getPatientName(),
                                                jsonObject.getString("fromUUID"),
                                                jsonObject.getString("toUUID"),
                                                jsonObject.getString("patientUUID"), "New images uploaded for this visit."
                                        );
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (DAOException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                    });
        }
        sessionManager.setPushSyncFinished(true);
        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_OBS_IMAGE_PUSH_DONE));
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

}

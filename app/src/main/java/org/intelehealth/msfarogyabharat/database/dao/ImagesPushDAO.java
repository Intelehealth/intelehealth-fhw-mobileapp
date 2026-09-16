package org.intelehealth.msfarogyabharat.database.dao;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.models.ObsImageModel.Add_Image_Push_Body;
import org.intelehealth.msfarogyabharat.models.ObsImageModel.Add_Img_Filename_PushImageResponse;
import org.intelehealth.msfarogyabharat.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.msfarogyabharat.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.msfarogyabharat.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.msfarogyabharat.utilities.Base64Utils;
import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UrlModifiers;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImagesPushDAO {
    String TAG = ImagesPushDAO.class.getSimpleName();
    SessionManager sessionManager = null;
    String encoded_filename = "";
    Base64Utils base64Utils_filename = new Base64Utils();


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
        Logger.logD("url", url);
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
        IntelehealthApplication.getAppContext()
                .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE));
//        AppConstants.notificationUtils.DownloadDone("Patient Profile", "Completed Uploading Patient Profile", 4, IntelehealthApplication.getAppContext());
        return true;
    }

    public boolean obsImagesPush() {

        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();

        encoded_filename = base64Utils_filename.encoded("intelehealthUser", "IHUser#1");

        Gson gson = new Gson();
        UrlModifiers urlModifiers = new UrlModifiers();
        ImagesDAO imagesDAO = new ImagesDAO();
        String url = urlModifiers.setObsImageUrl();
        Logger.logD("url", url);
        List<ObsPushDTO> obsImageJsons = new ArrayList<>();
        try {
            obsImageJsons = imagesDAO.getObsUnsyncedImages();
            Log.e(TAG, "image request model" + gson.toJson(obsImageJsons));
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        int i = 0;
        for (ObsPushDTO p : obsImageJsons) {

            //pass it like this
            File file = null;
            file = new File(AppConstants.IMAGE_PATH + p.getValue() + ".jpg");
            RequestBody requestFile = RequestBody.create(MediaType.parse("application/json"), file);
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Observable<ObsJsonResponse> obsJsonResponseObservable = AppConstants.apiInterface.
                    OBS_JSON_RESPONSE_OBSERVABLE(url, "Basic " + encoded, body, p);
            Log.v("main", "obsurl" + url);
            Log.v("main", "obsdata: " + p.toString());

            obsJsonResponseObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ObsJsonResponse>() {
                        @Override
                        public void onNext(ObsJsonResponse obsJsonResponse) {
                            Logger.logD(TAG, "success" + obsJsonResponse);

                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "onError " + e.getMessage());
//                            AppConstants.notificationUtils.DownloadDone("Patient Profile", "Error Uploading Patient Profile", IntelehealthApplication.getAppContext());
                        }

                        @Override
                        public void onComplete() {
                            Logger.logD(TAG, "success");

                            //TODO: Add api for image filenaME push api
                            Add_Image_Push_Body add_image_push_body = new Add_Image_Push_Body();
                            add_image_push_body.setPatientId(p.getPerson());
                            add_image_push_body.setObsId(p.getUuid());
                            add_image_push_body.setEncounteruuid(p.getEncounter());
                            add_image_push_body.setImageName(p.getValue());

                            Log.v("main", "image file model" + gson.toJson(add_image_push_body));

                            UrlModifiers urlModifiers = new UrlModifiers();
                            ImagesDAO imagesDAO = new ImagesDAO();
                            String url = urlModifiers.additional_image_filename_url();

                            Call<Add_Img_Filename_PushImageResponse> responseCall = AppConstants.apiInterface
                                    .ADDITIONAL_DOC_IMAGE_FILENAME(url, "Basic " + encoded_filename, add_image_push_body);

                            responseCall.enqueue(new Callback<Add_Img_Filename_PushImageResponse>() {
                                @Override
                                public void onResponse(Call<Add_Img_Filename_PushImageResponse> call,
                                                       Response<Add_Img_Filename_PushImageResponse> response) {

                                    //TODO: now add this value in tbl_additional_doc table...
                                    imagesDAO.insertInto_tbl_additional_doc(UUID.randomUUID().toString(), p.getPerson(), p.getEncounter(),
                                            p.getUuid(), p.getValue(), "0", "TRUE");

                                    try {
                                        imagesDAO.updateUnsyncedObsImages(p.getUuid());
                                    } catch (DAOException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Add_Img_Filename_PushImageResponse> call, Throwable t) {
                                    t.printStackTrace();
                                }
                            });
                        }
                    });
        }
        sessionManager.setPushSyncFinished(true);
        IntelehealthApplication.getAppContext().sendBroadcast(
                new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
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

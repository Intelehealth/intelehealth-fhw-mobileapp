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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * Obs uuids of additional documents attached while editing a sent visit that the user has not
     * explicitly uploaded yet. Background pushes ({@link #obsImagesPush()}) skip them, so a draft the
     * user may still remove or discard never reaches the server behind their back.
     */
    private static final Set<String> HELD_DRAFT_OBS_UUIDS = ConcurrentHashMap.newKeySet();

    public static void holdDraftObsImage(String obsUuid) {
        if (obsUuid != null) HELD_DRAFT_OBS_UUIDS.add(obsUuid);
    }

    public static void releaseDraftObsImages(Collection<String> obsUuids) {
        HELD_DRAFT_OBS_UUIDS.removeAll(obsUuids);
    }

    /**
     * Result of a targeted {@link #obsImagesPush(Collection, ObsImagesPushListener)}, delivered on
     * the main thread once every requested image has either uploaded or failed.
     */
    public interface ObsImagesPushListener {
        void onFinished(Set<String> uploadedUuids, Set<String> failedUuids);
    }


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

        for (ObsPushDTO p : obsImageJsons) {
            // Drafts are uploaded only through the explicit (targeted) push.
            if (HELD_DRAFT_OBS_UUIDS.contains(p.getUuid())) continue;
            pushObsImage(p, url, encoded, imagesDAO, null, null);
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

    /**
     * Uploads only the given obs images (e.g. the additional-document drafts the user tapped Upload
     * for) and reports which ones the server accepted. A requested uuid that is not currently
     * uploadable (already synced, voided, or its visit is not synced yet) is reported as failed.
     * Held drafts are uploaded here regardless of the hold; the caller releases them on success.
     */
    public void obsImagesPush(Collection<String> obsUuids, ObsImagesPushListener listener) {
        // Called from a click handler: run the unsynced-images query off the main thread.
        Set<String> requested = new HashSet<>(obsUuids);
        Schedulers.io().scheduleDirect(() -> pushRequestedObsImages(requested, listener));
    }

    private void pushRequestedObsImages(Set<String> obsUuids, ObsImagesPushListener listener) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String encoded = sessionManager.getEncoded();
        String url = new UrlModifiers().setObsImageUrl();
        ImagesDAO imagesDAO = new ImagesDAO();

        Set<String> pending = new HashSet<>(obsUuids);
        Set<String> uploaded = new HashSet<>();
        Set<String> failed = new HashSet<>();

        List<ObsPushDTO> toPush = new ArrayList<>();
        try {
            for (ObsPushDTO p : imagesDAO.getObsUnsyncedImages()) {
                if (pending.contains(p.getUuid())) toPush.add(p);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        for (ObsPushDTO p : toPush) pending.remove(p.getUuid());
        failed.addAll(pending);

        if (toPush.isEmpty()) {
            AndroidSchedulers.mainThread().scheduleDirect(() -> listener.onFinished(uploaded, failed));
            return;
        }

        // All completion callbacks run on the main thread, so the counter needs no synchronisation.
        int[] remaining = {toPush.size()};
        for (ObsPushDTO p : toPush) {
            Runnable done = () -> {
                if (--remaining[0] == 0) listener.onFinished(uploaded, failed);
            };
            pushObsImage(p, url, encoded, imagesDAO,
                    () -> {
                        uploaded.add(p.getUuid());
                        done.run();
                    },
                    () -> {
                        failed.add(p.getUuid());
                        done.run();
                    });
        }
    }

    /**
     * Uploads one obs image and marks it synced on success. {@code onSuccess}/{@code onFailure} are
     * optional and invoked on the main thread.
     */
    private void pushObsImage(ObsPushDTO p, String url, String encoded, ImagesDAO imagesDAO,
                              Runnable onSuccess, Runnable onFailure) {
        File file = new File(AppConstants.IMAGE_PATH + p.getUuid() + ".jpg");
        CustomLog.e(TAG, "File size:" + file.length());
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("application/json"));
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
                        if (onFailure != null) onFailure.run();
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD(TAG, "success");
                        try {
                            imagesDAO.updateUnsyncedObsImages(p.getUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (onSuccess != null) onSuccess.run();
                    }
                });
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
        deleteVoidedObsOnServer(voidedObsImageList, urlModifiers, encoded);
        // The push payload can't carry a void, so vitals/diagnostics cleared while editing (and
        // duplicate copies voided on edit) are deleted here alongside the image obs on every sync.
        deleteVoidedObsOnServer(ObsDAO.getVoidedUnsyncedVitalAndDiagnosticsObs(), urlModifiers, encoded);
        return true;
    }

    /**
     * Sends DELETE /obs/{uuid} for obs voided locally. A row is marked synced once the server
     * confirms it is gone (2xx, or 404 when it never reached the server), so the voided-obs queries
     * stop returning it; other failures leave it for the next sync.
     */
    private void deleteVoidedObsOnServer(List<String> obsUuids, UrlModifiers urlModifiers, String encoded) {
        for (String obsUuid : obsUuids) {
            AppConstants.apiInterface.DELETE_OBS(urlModifiers.obsImageDeleteUrl(obsUuid), "Basic " + encoded)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DisposableSingleObserver<retrofit2.Response<Void>>() {
                        @Override
                        public void onSuccess(retrofit2.Response<Void> response) {
                            if (response.isSuccessful() || response.code() == 404) {
                                ObsDAO.markObsSynced(obsUuid);
                            } else {
                                Logger.logD(TAG, "obs delete failed: " + response.code());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "obs delete error " + e.getMessage());
                        }
                    });
        }
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

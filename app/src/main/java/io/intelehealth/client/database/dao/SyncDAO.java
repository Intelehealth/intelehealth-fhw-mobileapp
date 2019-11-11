package io.intelehealth.client.database.dao;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.models.dto.ResponseDTO;
import io.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;
import io.intelehealth.client.models.pushResponseApiCall.PushResponseApiCall;
import io.intelehealth.client.services.LastSyncIntentService;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.PatientsFrameJson;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.exception.DAOException;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncDAO {
    public static String TAG = "SyncDAO";
    SessionManager sessionManager = null;

    public boolean SyncData(ResponseDTO responseDTO) throws DAOException {
        boolean isSynced = true;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ObsDAO obsDAO = new ObsDAO();
        LocationDAO locationDAO = new LocationDAO();
        ProviderDAO providerDAO = new ProviderDAO();
        try {
            Logger.logD(TAG, "pull sync started");

            patientsDAO.insertPatients(responseDTO.getData().getPatientDTO());
            patientsDAO.patientAttributes(responseDTO.getData().getPatientAttributesDTO());
            patientsDAO.patinetAttributeMaster(responseDTO.getData().getPatientAttributeTypeMasterDTO());
            visitsDAO.insertVisit(responseDTO.getData().getVisitDTO());
            encounterDAO.insertEncounter(responseDTO.getData().getEncounterDTO());
            obsDAO.insertObsTemp(responseDTO.getData().getObsDTO());
            locationDAO.insertLocations(responseDTO.getData().getLocationDTO());
            providerDAO.insertProviders(responseDTO.getData().getProviderlist());

            Logger.logD(TAG, "Pull ENCOUNTER: "+responseDTO.getData().getEncounterDTO());
            Logger.logD(TAG, "Pull sync ended");
            sessionManager.setPullExcutedTime(sessionManager.isPulled());
            sessionManager.setFirstTimeSyncExecute(false);
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
            Logger.logE(TAG, "Exception", e);
            throw new DAOException(e.getMessage(), e);
        }

        return isSynced;

    }

    public boolean pullData(final Context context) {
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String oldDate = sessionManager.getPullExcutedTime();
        String url = "http://" + sessionManager.getServerUrl() + ":8080/EMR-Middleware/webapi/pull/pulldata/" + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime();
        Logger.logD("PULL", "PULL_DATA: "+url);
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started");
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                AppConstants.notificationUtils.showNotifications("syncBackground", "Syncing", 1, IntelehealthApplication.getAppContext());
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {

                    // SyncDAO syncDAO = new SyncDAO();
                    boolean sync = false;
                    try {
                        sync = SyncData(response.body());
                    } catch (DAOException e) {
                        Crashlytics.getInstance().core.logException(e);
                    }
                    if (sync)
                        AppConstants.notificationUtils.DownloadDone("sync", "Successfully synced", 1, IntelehealthApplication.getAppContext());
                    else
                        AppConstants.notificationUtils.DownloadDone("sync", "failed synced,You can try again", 1, IntelehealthApplication.getAppContext());

                }

                Logger.logD("End Pull request", "Ended");
                sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome());

                Intent intent = new Intent(IntelehealthApplication.getAppContext(), LastSyncIntentService.class);
                IntelehealthApplication.getAppContext().startService(intent);
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }

    public boolean pushDataApi() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();


        PushRequestApiCall pushRequestApiCall;
        PatientsFrameJson patientsFrameJson = new PatientsFrameJson();
        pushRequestApiCall = patientsFrameJson.frameJson();
        final boolean[] isSucess = {true};
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        Logger.logD(TAG, "push request model" + gson.toJson(pushRequestApiCall));
        String url = "http://" + sessionManager.getServerUrl() + ":8080/EMR-Middleware/webapi/push/pushdata";
//        push only happen if any one data exists.
        if (!pushRequestApiCall.getVisits().isEmpty() || !pushRequestApiCall.getPersons().isEmpty() || !pushRequestApiCall.getPatients().isEmpty() || !pushRequestApiCall.getEncounters().isEmpty()) {
            Single<PushResponseApiCall> pushResponseApiCallObservable = AppConstants.apiInterface.PUSH_RESPONSE_API_CALL_OBSERVABLE(url, "Basic " + encoded, pushRequestApiCall);
            pushResponseApiCallObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<PushResponseApiCall>() {
                        @Override
                        public void onSuccess(PushResponseApiCall pushResponseApiCall) {
                            Logger.logD(TAG, "success" + pushResponseApiCall);
                            for (int i = 0; i < pushResponseApiCall.getData().getPatientlist().size(); i++) {
                                try {
                                    patientsDAO.updateOpemmrsId(pushResponseApiCall.getData().getPatientlist().get(i).getOpenmrsId(), pushResponseApiCall.getData().getPatientlist().get(i).getSyncd().toString(), pushResponseApiCall.getData().getPatientlist().get(i).getUuid());
                                    Log.d("SYNC","ProvUUDI"+pushResponseApiCall.getData().getPatientlist().get(i).getUuid());
                                } catch (DAOException e) {
                                    Crashlytics.getInstance().core.logException(e);
                                }
                            }

                            for (int i = 0; i < pushResponseApiCall.getData().getVisitlist().size(); i++) {
                                try {
                                    visitsDAO.updateVisitSync(pushResponseApiCall.getData().getVisitlist().get(i).getUuid(), pushResponseApiCall.getData().getVisitlist().get(i).getSyncd().toString());
                                } catch (DAOException e) {
                                    Crashlytics.getInstance().core.logException(e);
                                }
                            }

                            for (int i = 0; i < pushResponseApiCall.getData().getEncounterlist().size(); i++) {
                                try {
                                    encounterDAO.updateEncounterSync(pushResponseApiCall.getData().getEncounterlist().get(i).getSyncd().toString(), pushResponseApiCall.getData().getEncounterlist().get(i).getUuid());
                                    Log.d("SYNC","Encounter Data: "+pushResponseApiCall.getData().getEncounterlist().get(i).toString());
                                } catch (DAOException e) {
                                    Crashlytics.getInstance().core.logException(e);
                                }
                            }
                            isSucess[0] = true;
                            sessionManager.setSyncFinished(true);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                            isSucess[0] = false;
                        }
                    });
            sessionManager.setPullSyncFinished(true);
        }

        return isSucess[0];
    }


}

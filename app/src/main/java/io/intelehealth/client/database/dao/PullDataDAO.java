package io.intelehealth.client.database.dao;

import android.content.Context;
import android.content.Intent;

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


public class PullDataDAO {

    private SessionManager sessionManager = null;

    public static String TAG = PullDataDAO.class.getSimpleName();

    public boolean pullData(final Context context) {
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
//        1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a
        String url = "http://" + sessionManager.getServerUrl() + ":8080/EMR-Middleware/webapi/pull/pulldata/" + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime();
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started");
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                AppConstants.notificationUtils.showNotifications("Sync", "Syncing", IntelehealthApplication.getAppContext());
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
//                    sessionManager.setPullExcutedTime(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {

//                    pullDataExecutedTime(response.body(), context);
                    SyncDAO syncDAO = new SyncDAO();
                    boolean sync = false;
                    try {
                        sync = syncDAO.SyncData(response.body());
                    } catch (DAOException e) {
                        Crashlytics.getInstance().core.logException(e);
                    }
                    if (sync)
                        AppConstants.notificationUtils.DownloadDone("Sync", "Successfully synced", IntelehealthApplication.getAppContext());
                    else
                        AppConstants.notificationUtils.DownloadDone("Sync", "failed synced,You can try again", IntelehealthApplication.getAppContext());

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

//    public void pullDataExecutedTime(final ResponseDTO responseDTO, final Context context) {
//        class dataInserted extends AsyncTask<Void, Void, Void> {
////            private ProgressDialog dialog = new ProgressDialog(context);
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                SyncDAO syncDAO = new SyncDAO();
//                try {
//                    syncDAO.SyncData(responseDTO);
//                } catch (DAOException e) {
//                    Logger.logE("Dao exception", "exception", e);
//                      Crashlytics.getInstance().core.logException(e);
//                }
//                return null;
//            }
//
//
//            @Override
//            protected void onPreExecute() {
////                this.dialog.setMessage("Please wait");
////                this.dialog.setCancelable(false);
////                this.dialog.show();
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
////                if (dialog.isShowing()) {
////                    dialog.dismiss();
////                }
//            }
//        }
//        dataInserted dataInserted = new dataInserted();
//        dataInserted.execute();
//
//    }

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
        if (pushRequestApiCall.getPatients().isEmpty() || pushRequestApiCall.getEncounters().isEmpty() || pushRequestApiCall.getPersons().isEmpty() || pushRequestApiCall.getVisits().isEmpty()) {
        } else {
            String url = "http://" + sessionManager.getServerUrl() + ":8080/EMR-Middleware/webapi/push/pushdata";
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
        }
        sessionManager.setPullSyncFinished(true);
        return isSucess[0];
    }

}





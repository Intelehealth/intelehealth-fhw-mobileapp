package io.intelehealth.client.dao;

import android.content.Context;

import com.google.gson.Gson;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.dto.ResponseDTO;
import io.intelehealth.client.exception.DAOException;
import io.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;
import io.intelehealth.client.models.pushResponseApiCall.PushResponseApiCall;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.PatientsFrameJson;
import io.intelehealth.client.utilities.SessionManager;
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

                if (response.isSuccessful()) {

//                    pullDataExecutedTime(response.body(), context);
                    SyncDAO syncDAO = new SyncDAO();
                    try {
                        syncDAO.SyncData(response.body());
                    } catch (DAOException e) {
                        e.printStackTrace();
                    }

                }
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
//                    sessionManager.setPullExcutedTime(response.body().getData().getPullexecutedtime());
                }
                Logger.logD("End Pull request", "Ended");
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
            }
        });

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
//                    e.printStackTrace();
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
        EncounterDAO encounterDAO=new EncounterDAO();


        PushRequestApiCall pushRequestApiCall;
        PatientsFrameJson patientsFrameJson = new PatientsFrameJson();
        pushRequestApiCall = patientsFrameJson.frameJson();
        final boolean[] isSucess = {true};
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        Logger.logD(TAG, "push request model" + gson.toJson(pushRequestApiCall));
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
                                e.printStackTrace();
                            }
                        }

                        for (int i = 0; i < pushResponseApiCall.getData().getVisitlist().size(); i++) {
                            try {
                                visitsDAO.updateVisitSync(pushResponseApiCall.getData().getVisitlist().get(i).getUuid(), pushResponseApiCall.getData().getVisitlist().get(i).getSyncd().toString());
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }

                        for(int i=0;i<pushResponseApiCall.getData().getEncounterlist().size();i++){
                            try {
                                encounterDAO.updateEncounterSync(pushResponseApiCall.getData().getEncounterlist().get(i).getSyncd().toString(),pushResponseApiCall.getData().getEncounterlist().get(i).getUuid());
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }
                        isSucess[0] = true;

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Onerror " + e.getMessage());
                        isSucess[0] = false;
                    }
                });
        return isSucess[0];
    }
}





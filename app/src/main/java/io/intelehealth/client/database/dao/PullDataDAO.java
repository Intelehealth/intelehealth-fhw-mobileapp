package io.intelehealth.client.database.dao;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.activePatientsActivity.ActivePatientActivity;
import io.intelehealth.client.activities.activePatientsActivity.ActivePatientAdapter;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.models.ActivePatientModel;
import io.intelehealth.client.models.Patient;
import io.intelehealth.client.models.dto.PatientDTO;
import io.intelehealth.client.models.dto.ResponseDTO;
import io.intelehealth.client.models.dto.VisitDTO;
import io.intelehealth.client.models.pushRequestApiCall.PushRequestApiCall;
import io.intelehealth.client.models.pushResponseApiCall.PushResponseApiCall;
import io.intelehealth.client.services.LastSyncIntentService;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.PatientsFrameJson;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.utilities.StringUtils;
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
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;

    public boolean pullData(final Context context) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        db = mDbHelper.getWritableDatabase();

        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
//        1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a
        String url = "http://" + sessionManager.getServerUrl() + "/EMR-Middleware/webapi/pull/pulldata/" + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime();
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started");
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                AppConstants.notificationUtils.showNotifications("syncBackground", "Syncing", IntelehealthApplication.getAppContext());
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

                    if (response.body().getData() != null) {
                        triggerVisitNotification(response.body().getData().getVisitDTO());
                    }
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

    private void triggerVisitNotification(List<VisitDTO> listVisitDTO) {

        List<ActivePatientModel> activePatientList = new ArrayList<>();
        getPatients(activePatientList);

        List<VisitDTO> visitDTO = new ArrayList<>();
        visitDTO.addAll(listVisitDTO);

        if (visitDTO != null) {
            for (int i = 0; i < visitDTO.size(); i++) {
                for (int j = 0; j < activePatientList.size(); j++) {
                    if (visitDTO.get(i).getPatientuuid().equalsIgnoreCase(activePatientList.get(j).getPatientuuid())) {
                        AppConstants.notificationUtils.DownloadDone(IntelehealthApplication.getAppContext().getResources().getString(R.string.patient) + " " + activePatientList.get(j).getFirst_name() + " " + activePatientList.get(j).getLast_name(), IntelehealthApplication.getAppContext().getResources().getString(R.string.has_a_new_prescription), IntelehealthApplication.getAppContext());
                    }
                }
            }
        }
    }

    private void getPatients(List<ActivePatientModel> activePatientList) {

        String query =
                "SELECT   a.uuid, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id  " +
                        "FROM tbl_visit a, tbl_patient b " +
                        "WHERE a.patientuuid = b.uuid " +
                        "AND a.enddate is NULL OR a.enddate='' GROUP BY a.uuid ORDER BY a.startdate ASC";
        final Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    activePatientList.add(new ActivePatientModel(
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                            ""
                    ));
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
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
        String url = "http://" + sessionManager.getServerUrl() + "/EMR-Middleware/webapi/push/pushdata";
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
            sessionManager.setPullSyncFinished(true);
        }

        return isSucess[0];
    }

}





package org.intelehealth.msfarogyabharat.database.dao;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.InteleHealthDatabaseHelper;
import org.intelehealth.msfarogyabharat.models.ActivePatientModel;
import org.intelehealth.msfarogyabharat.models.dto.ResponseDTO;
import org.intelehealth.msfarogyabharat.models.dto.VisitDTO;
import org.intelehealth.msfarogyabharat.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.msfarogyabharat.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.msfarogyabharat.syncModule.SyncProgress;
import org.intelehealth.msfarogyabharat.utilities.FollowUpNotificationWorker;
import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.NotificationID;
import org.intelehealth.msfarogyabharat.utilities.PatientsFrameJson;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

;

public class SyncDAO {
    public static String TAG = "SyncDAO";
    public static final String MSF_PULL_ISSUE = "MSF_PULL_ISSUE";
    SessionManager sessionManager = null;
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;
    String appLanguage;
    private static final SyncProgress liveDataSync = new SyncProgress();

    public boolean SyncData(ResponseDTO responseDTO) throws DAOException {
        boolean isSynced = true;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ObsDAO obsDAO = new ObsDAO();
        LocationDAO locationDAO = new LocationDAO();
        ProviderDAO providerDAO = new ProviderDAO();
        VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();
        ProviderAttributeLIstDAO providerAttributeLIstDAO = new ProviderAttributeLIstDAO();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.logD(TAG, "pull sync started");

                    if (responseDTO.getData().getPatientDTO() != null)
                        patientsDAO.insertPatients(responseDTO.getData().getPatientDTO());

                    if (responseDTO.getData().getPatientAttributesDTO() != null)
                        patientsDAO.patientAttributes(responseDTO.getData().getPatientAttributesDTO());

                    if (responseDTO.getData().getPatientAttributeTypeMasterDTO() != null)
                        patientsDAO.patinetAttributeMaster(responseDTO.getData().getPatientAttributeTypeMasterDTO());

                    if (responseDTO.getData().getVisitDTO() != null)
                        visitsDAO.insertVisit(responseDTO.getData().getVisitDTO());

                    if (responseDTO.getData().getEncounterDTO() != null)
                        encounterDAO.insertEncounter(responseDTO.getData().getEncounterDTO());

                    if (responseDTO.getData().getObsDTO() != null)
                        obsDAO.insertObsTemp(responseDTO.getData().getObsDTO());

                    if (responseDTO.getData().getLocationDTO() != null)
                        locationDAO.insertLocations(responseDTO.getData().getLocationDTO());

                    if (responseDTO.getData().getProviderlist() != null)
                        providerDAO.insertProviders(responseDTO.getData().getProviderlist());

                    if (responseDTO.getData().getProviderAttributeList() != null)
                        providerAttributeLIstDAO.insertProvidersAttributeList(responseDTO.getData().getProviderAttributeList());

                    if (responseDTO.getData().getVisitAttributeList() != null)
                        visitAttributeListDAO.insertProvidersAttributeList(responseDTO.getData().getVisitAttributeList());


                    // Todo: This was the main cause of app hang issue: MHM-219
                   /* long value = FollowUpNotificationWorker.getFollowUpCount();
                    sessionManager.setFollowUpVisit(String.valueOf(value));*/

//            visitsDAO.insertVisitAttribToDB(responseDTO.getData().getVisitAttributeList())

                    //Logger.logD(TAG, "Pull ENCOUNTER: " + responseDTO.getData().getEncounterDTO());
                    Logger.logD(TAG, "Pull sync ended");
                  //  sessionManager.setPullExcutedTime(sessionManager.isPulled()); // todo : msf issue
                    sessionManager.setFirstTimeSyncExecute(false);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Logger.logE(TAG, "Exception", e);
                  //  throw new DAOException(e.getMessage(), e);
                }
            }
        });


        return isSynced;

    }


    public boolean pullData_Background(final Context context, int pageNo) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        db = mDbHelper.getReadableDatabase();

        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String oldDate = sessionManager.getPullExcutedTime();
        String url = "https://" + sessionManager.getServerUrl() + "/EMR-Middleware/webapi/pull/pulldata/" +
                sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime() +
                "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;

        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started ");
        Logger.logD(MSF_PULL_ISSUE, "background " + url);
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {

                    // msf sync issue - start
                    boolean sync = false;
                    try {
                        // step 1. insert data into local db.
                        sync = SyncData(response.body());
                        Logger.logD("sync", "sync pullbackground" + sync);

                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        e.printStackTrace();
                    }

                    if (sync) {
                        // Step 2. once inserted successsfully, call the presc notification code from below.
                        triggerNotificationForPrescription(response);


                        // Step 3. on insert done and notifi call from this packet of page0 and limit 100 again call the pull().
                        int nextPageNo = response.body().getData().getPageNo();
                        int totalCount = response.body().getData().getTotalCount();
                        Logger.logD(MSF_PULL_ISSUE, "background pageno: " + nextPageNo + " totalCount: " + totalCount);
                        if (nextPageNo != -1) {
                            pullData_Background(context, nextPageNo);
                            return;
                        }
                        else {
                            // do nothing - move ahead.
                            sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.getcurrentDateTime());

                            Logger.logD("End Pull request", "Ended");
                            sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome());
                            sessionManager.setPullExcutedTime(sessionManager.isPulled());

                            //Workmanager request is used in ForeGround sync in place of this as per Intele_safe
                            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PULL_DATA_DONE));
                        }
                        // msf sync issue - end


                    }
                    else {
                        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
                    }

                    /*if (sessionManager.getTriggerNoti().equals("yes")) {
                        if (response.body().getData() != null) {
                            ArrayList<String> listPatientUUID = new ArrayList<String>();
                            List<VisitDTO> listVisitDTO = new ArrayList<>();
                            ArrayList<String> encounterVisitUUID = new ArrayList<String>();
                            for (int i = 0; i < response.body().getData().getEncounterDTO().size(); i++) {
                                if (response.body().getData().getEncounterDTO().get(i)
                                        .getEncounterTypeUuid().equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                                    encounterVisitUUID.add(response.body().getData().getEncounterDTO().get(i).getVisituuid());
                                }
                            }
                            listVisitDTO.addAll(response.body().getData().getVisitDTO());
                            for (int i = 0; i < encounterVisitUUID.size(); i++) {
                                for (int j = 0; j < listVisitDTO.size(); j++) {
                                    if (encounterVisitUUID.get(i).equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
                                        listPatientUUID.add(listVisitDTO.get(j).getPatientuuid());
                                    }
                                }
                            }

                            if (listPatientUUID.size() > 0) {
                                triggerVisitNotification(listPatientUUID);
                            }
                        }
                    } else {
                        sessionManager.setTriggerNoti("yes");
                    }*/
                }


            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }


    public boolean pullData(final Context context, String fromActivity, int pageNo) {
        final Handler handler = new Handler(context.getMainLooper());

        mDbHelper = new InteleHealthDatabaseHelper(context);
        db = mDbHelper.getReadableDatabase();
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String oldDate = sessionManager.getPullExcutedTime();
        String url = "https://" + sessionManager.getServerUrl() + "/EMR-Middleware/webapi/pull/pulldata/" +
                sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime() +
                "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;
        Logger.logD(MSF_PULL_ISSUE, url);
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started");

        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                    Logger.logD(MSF_PULL_ISSUE, "last syc time: " + response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {

                    // msf sync issue - start
                    boolean sync = false;
                    try {
                        // step 1. insert data into local db.
                        sync = SyncData(response.body());
                        Logger.logD("sync", "sync pull" + sync);

                    } catch (DAOException e) {
                        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                    if (sync) {
                        // Step 2. once inserted successsfully, call the presc notification code from below.
                        triggerNotificationForPrescription(response);

                        // Step 3. on insert done and notifi call from this packet of page0 and limit 100 again call the pull().
                        int nextPageNo = response.body().getData().getPageNo();
                        int totalCount = response.body().getData().getTotalCount();
                        int percentage = 0; // this should be only in initialSync....
                        Logger.logD(MSF_PULL_ISSUE, "pageno: " + nextPageNo + " totalCount: " + totalCount);
                        if (nextPageNo != -1) {
                            percentage = (int) Math.round(nextPageNo * AppConstants.PAGE_LIMIT * 100.0/totalCount);
                            Logger.logD(MSF_PULL_ISSUE, "percentage: " + percentage);
                            setProgress(percentage);
                            pullData(context, fromActivity, nextPageNo);
                            return;
                        }
                        else {
                            percentage = 100;
                            Logger.logD(MSF_PULL_ISSUE, "percentage page -1: " + percentage);
                            setProgress(percentage);
                            sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.getcurrentDateTime());

                            // Adding handlers here so that we can show these toasts on the main thread - Added by Arpan Sircar
                            if (fromActivity.equalsIgnoreCase("home")) {
                                handler.post(() -> Toast.makeText(context, context.getResources().getString(R.string.successfully_synced), Toast.LENGTH_LONG).show());
                            } else if (fromActivity.equalsIgnoreCase("visitSummary")) {
                                handler.post(() -> Toast.makeText(context, context.getResources().getString(R.string.visit_uploaded_successfully), Toast.LENGTH_LONG).show());
                            } else if (fromActivity.equalsIgnoreCase("downloadPrescription")) {
                            }

                            //
                            Logger.logD("End Pull request", "Ended");
                            sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome());
                            sessionManager.setPullExcutedTime(sessionManager.isPulled());

                            //Workmanager request is used in ForeGround sync in place of this as per the intele_Safe
                            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PULL_DATA_DONE));

                        }
                        // msf sync issue - end

                    } else {
                        if (fromActivity.equalsIgnoreCase("home")) {
                            handler.post(() -> Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show());
                        } else if (fromActivity.equalsIgnoreCase("visitSummary")) {
                            handler.post(() -> Toast.makeText(context, context.getString(R.string.visit_not_uploaded), Toast.LENGTH_LONG).show());
                        } else if (fromActivity.equalsIgnoreCase("downloadPrescription")) {
                            handler.post(() -> Toast.makeText(context, context.getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show());
                        }
                        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
                    }

                }


            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }

    private void triggerNotificationForPrescription(Response<ResponseDTO> response) {
        if (sessionManager.getTriggerNoti().equals("yes")) {
            if (response.body().getData() != null) {
                ArrayList<String> listPatientUUID = new ArrayList<String>();
                List<VisitDTO> listVisitDTO = new ArrayList<>();
                ArrayList<String> encounterVisitUUID = new ArrayList<String>();
                for (int i = 0; i < response.body().getData().getEncounterDTO().size(); i++) {
                    if (response.body().getData().getEncounterDTO().get(i)
                            .getEncounterTypeUuid().equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                        encounterVisitUUID.add(response.body().getData().getEncounterDTO().get(i).getVisituuid());
                    }
                }
                listVisitDTO.addAll(response.body().getData().getVisitDTO());
                for (int i = 0; i < encounterVisitUUID.size(); i++) {
                    for (int j = 0; j < listVisitDTO.size(); j++) {
                        if (encounterVisitUUID.get(i).equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
                            listPatientUUID.add(listVisitDTO.get(j).getPatientuuid());
                        }
                    }
                }

                if (listPatientUUID.size() > 0) {
                    triggerVisitNotification(listPatientUUID);
                }
            }
        } else {
            sessionManager.setTriggerNoti("yes");
        }
    }

    public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        IntelehealthApplication.getAppContext().getResources().updateConfiguration(config, IntelehealthApplication.getAppContext().getResources().getDisplayMetrics());
    }

    private void triggerVisitNotification(ArrayList<String> listPatientUUID) {

        List<ActivePatientModel> activePatientList = new ArrayList<>();
        getPatients(activePatientList);

        if (listPatientUUID != null) {
            for (int i = 0; i < listPatientUUID.size(); i++) {
                for (int j = 0; j < activePatientList.size(); j++) {
                    if (listPatientUUID.get(i).equalsIgnoreCase(activePatientList.get(j).getPatientuuid())) {
                        Log.e("GET-ID", "" + NotificationID.getID());
                        AppConstants.notificationUtils.DownloadDone
                                (IntelehealthApplication.getAppContext().getResources().getString(R.string.patient) + " " + activePatientList.get(j).getFirst_name() + " " + activePatientList.get(j).getLast_name(),
                                IntelehealthApplication.getAppContext().getString(R.string.has_a_new_prescription), NotificationID.getID(), IntelehealthApplication.getAppContext());
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
                    boolean hasPrescription = getHasPrescription(cursor);
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
                            "",
                            "",
                            hasPrescription
                    ));
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
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
        Log.e(TAG, "push request model" + gson.toJson(pushRequestApiCall));
        String url = "https://" + sessionManager.getServerUrl() + "/EMR-Middleware/webapi/push/pushdata";
//        String url = "https://" + sessionManager.getServerUrl() + "/pushdata";
//        push only happen if any one data exists.
        Logger.logD("url", url);
        if (!pushRequestApiCall.getVisits().isEmpty() || !pushRequestApiCall.getPersons().isEmpty() || !pushRequestApiCall.getPatients().isEmpty() || !pushRequestApiCall.getEncounters().isEmpty()) {
            Single<PushResponseApiCall> pushResponseApiCallObservable = AppConstants.apiInterface.PUSH_RESPONSE_API_CALL_OBSERVABLE(url, "Basic " + encoded, pushRequestApiCall);
            pushResponseApiCallObservable.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableSingleObserver<PushResponseApiCall>() {
                        @Override
                        public void onSuccess(PushResponseApiCall pushResponseApiCall) {
                            Logger.logD(TAG, "success" + pushResponseApiCall);
                            for (int i = 0; i < pushResponseApiCall.getData().getPatientlist().size(); i++) {
                                try {
                                    patientsDAO.updateOpemmrsId(pushResponseApiCall.getData().getPatientlist().get(i).getOpenmrsId(), pushResponseApiCall.getData().getPatientlist().get(i).getSyncd().toString(), pushResponseApiCall.getData().getPatientlist().get(i).getUuid());
                                    Log.d("SYNC", "ProvUUDI" + pushResponseApiCall.getData().getPatientlist().get(i).getUuid());
                                } catch (DAOException e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }

                            for (int i = 0; i < pushResponseApiCall.getData().getVisitlist().size(); i++) {
                                try {
                                    visitsDAO.updateVisitSync(pushResponseApiCall.getData().getVisitlist().get(i).getUuid(), pushResponseApiCall.getData().getVisitlist().get(i).getSyncd().toString());
                                } catch (DAOException e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }

                            for (int i = 0; i < pushResponseApiCall.getData().getEncounterlist().size(); i++) {
                                try {
                                    encounterDAO.updateEncounterSync(pushResponseApiCall.getData().getEncounterlist().get(i).getSyncd().toString(), pushResponseApiCall.getData().getEncounterlist().get(i).getUuid());
                                    Log.d("SYNC", "Encounter Data: " + pushResponseApiCall.getData().getEncounterlist().get(i).toString());
                                } catch (DAOException e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }
                            isSucess[0] = true;
                            sessionManager.setSyncFinished(true);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                            isSucess[0] = false;
                            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
                        }
                    });
            sessionManager.setPullSyncFinished(true);
            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PUSH_DATA_DONE));
        }

        return isSucess[0];
    }

    private void CalculateAgoTime(Context context) {
        String finalTime = "";

        String syncTime = sessionManager.getLastSyncDateTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        ParsePosition pos = new ParsePosition(0);
        long then = formatter.parse(syncTime, pos).getTime();
        long now = new Date().getTime();

        long seconds = (now - then) / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String time = "";
        long num = 0;
        if (days > 0) {
            num = days;
            time = days + " " + context.getString(R.string.day);
        } else if (hours > 0) {
            num = hours;
            time = hours + " " + context.getString(R.string.hour);
        } else if (minutes >= 0) {
            num = minutes;
            time = minutes + " " + context.getString(R.string.minute);
        }
//      <For seconds>
//      else {
//            num = seconds;
//            time = seconds + " second";
//      }
        if (num > 1) {
            time += context.getString(R.string.s);
        }
        finalTime = time + " " + context.getString(R.string.ago);

        sessionManager.setLastTimeAgo(finalTime);
    }

    private Boolean getHasPrescription(Cursor cursor) {
        boolean hasPrescription = false;
        String query1 = "Select count(*) from tbl_encounter where encounter_type_uuid = 'bd1fbfaa-f5fb-4ebd-b75c-564506fc309e' AND visituuid = ?";
        Cursor mCount = db.rawQuery(query1, new String[]{cursor.getString(cursor.getColumnIndexOrThrow("uuid"))});
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        if (count == 1)
            hasPrescription = true;

        return hasPrescription;
    }

    public static void setProgress(int progress) {
        liveDataSync.updateProgress(progress);
    }

    public static SyncProgress getSyncProgress_LiveData() {
        return liveDataSync;
    }
}

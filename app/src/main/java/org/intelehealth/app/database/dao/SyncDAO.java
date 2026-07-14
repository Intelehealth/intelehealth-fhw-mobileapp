package org.intelehealth.app.database.dao;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.LocationDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientAttributeTypeMasterDTO;
import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.ProviderAttributeListDTO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.dto.VisitAttributeDTO;
import org.intelehealth.app.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.app.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.app.models.dto.ResponseDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.PatientsFrameJson;
import org.intelehealth.app.services.InitialSyncIntentService;
import org.intelehealth.app.syncModule.SyncProgress;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.network.response.ConfigResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Intelehealth on 17/04/17.
 */

public class SyncDAO {
    private static final String TAG = "SyncDAO";
    public static final String PULL_ISSUE = "PULL_ISSUE";
    SessionManager sessionManager = null;
    InteleHealthDatabaseHelper mDbHelper;
    SQLiteDatabase db;
    String appLanguage;

    static SyncProgress liveDataSync = new SyncProgress();
    static boolean isTheConfigUpdated = false;


    public boolean SyncData(ResponseDTO responseDTO, boolean isAppSetupDone) throws DAOException {
        boolean isSyncedValue = true;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        appLanguage = sessionManager.getAppLanguage();
        if (appLanguage != null && !appLanguage.equalsIgnoreCase("")) {
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
        PatientAttributesMasterDaoNew patientAttributesMasterDaoNew = new PatientAttributesMasterDaoNew();
        PatientAttributesDaoNew patientAttributesDaoNew = new PatientAttributesDaoNew();
        try {
            Logger.logD(TAG, "pull sync started");

            if (responseDTO != null && responseDTO.getData() != null) {
                if (responseDTO.getData().getPatientAttributeTypeMasterDTO() != null) {
                    patientAttributesMasterDaoNew.patinetAttributeMaster(
                            responseDTO.getData().getPatientAttributeTypeMasterDTO());
                }

                if (responseDTO.getData().getPatientDTO() != null) {
                    patientsDAO.insertPatients(responseDTO.getData().getPatientDTO());
                }

                if (responseDTO.getData().getPatientAttributesDTO() != null) {
                    patientAttributesDaoNew.patientAttributes(responseDTO.getData().getPatientAttributesDTO());
                }

                if (responseDTO.getData().getVisitDTO() != null) {
                    visitsDAO.insertVisit(responseDTO.getData().getVisitDTO());
                }

                if (responseDTO.getData().getEncounterDTO() != null) {
                    encounterDAO.insertEncounter(responseDTO.getData().getEncounterDTO());
                }

                if (responseDTO.getData().getObsDTO() != null) {
                    obsDAO.insertObsTemp(responseDTO.getData().getObsDTO());
                }

                if (responseDTO.getData().getLocationDTO() != null) {
                    locationDAO.insertLocations(responseDTO.getData().getLocationDTO());
                }

                if (responseDTO.getData().getProviderlist() != null) {
                    providerDAO.insertProviders(responseDTO.getData().getProviderlist());
                }

                if (responseDTO.getData().getVisitAttributeList() != null) {
                    visitAttributeListDAO.insertProvidersAttributeList(responseDTO.getData().getVisitAttributeList());
                }

                if (responseDTO.getData().getProviderAttributeList() != null) {
                    providerAttributeLIstDAO.insertProvidersAttributeList(
                            responseDTO.getData().getProviderAttributeList());
                }
            }

            Logger.logD(TAG, "pull sync Ended");

            if (!isAppSetupDone) {
                sessionManager.setSetupComplete(true);
            }
        } catch (Exception e) {
            isSyncedValue = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE(TAG, "SyncData Exception: ", e);
            throw new DAOException(e.getMessage());
        }
        return isSyncedValue;
    }

    public void downloadPatientImages(List<PatientDTO> patientDTOList) {
    }

    public void profilePicDownloaded(PatientDTO patientDTO) {
    }

    public void saveConfig(ConfigResponse configResponse) {
    }

    public boolean pullData_Background(final Context context, int pageNo) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        db = mDbHelper.getWriteDb();

        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/pull/pulldata/" +
                     sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime() +
                     "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;
        
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(
                url, "Basic " + encoded);

        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {
                    Single.fromCallable(() -> populatePullSuccessBackground(response, context))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                            }, throwable -> {
                                Log.e("RxJavaError",
                                        "Error occurred in populatePullSuccessBackground",
                                        throwable);
                            });
                }

                sessionManager.setLastPulledDateTime(
                        AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.getAppLanguage()));

                IntelehealthApplication.getAppContext()
                        .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .setPackage(
                                        IntelehealthApplication.getAppContext().getPackageName())
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                        AppConstants.SYNC_PULL_DATA_DONE));
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                IntelehealthApplication.getAppContext()
                        .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .setPackage(
                                        IntelehealthApplication.getAppContext().getPackageName())
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                        AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }

    public Object populatePullSuccessBackground(Response<ResponseDTO> response, Context context) {

        try {
            if (!isTheConfigUpdated)
                loadConfig();
            SyncData(response.body(), true);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (sessionManager.getTriggerNoti().equals("yes")) {
            if (response.body().getData() != null) {
                ArrayList<String> listPatientUUID = new ArrayList<String>();
                List<VisitDTO> listVisitDTO = new ArrayList<>();
                ArrayList<String> encounterVisitUUID = new ArrayList<String>();

                for (int i = 0;
                     i < response.body().getData().getEncounterDTO().size(); i++) {
                    if (response.body().getData().getEncounterDTO().get(i)
                            .getEncounterTypeUuid()
                            .equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                        encounterVisitUUID.add(
                                response.body().getData().getEncounterDTO().get(i)
                                        .getVisituuid());
                    }
                }
                listVisitDTO.addAll(response.body().getData().getVisitDTO());
                for (int i = 0; i < encounterVisitUUID.size(); i++) {
                    for (int j = 0; j < listVisitDTO.size(); j++) {
                        if (encounterVisitUUID.get(i)
                                .equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
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
        return true;
    }


    public boolean pullData(final Context context, String fromActivity, int pageNo) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        if (db == null) {
            db = mDbHelper.getWriteDb();
        }
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/pull/pulldata/"
                     + sessionManager.getLocationUuid() + "/" +
                     sessionManager.getPullExcutedTime() +
                     "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;
        
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(
                url, "Basic " + encoded);
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {
                    Single.fromCallable(() -> {
                        boolean sync = false;
                        if (!isTheConfigUpdated)
                            loadConfig();
                        sync = SyncData(response.body(), true);
                        return sync;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(sync -> {
                        if (sync) {
                            int nextPageNo = response.body().getData().getPageNo();
                            int totalCount = response.body().getData().getTotalCount();
                            int percentage = 0;

                            if (nextPageNo != -1) {
                                percentage = (int) Math.round(
                                        nextPageNo * AppConstants.PAGE_LIMIT * 100.0 / totalCount);
                                setProgress(percentage);
                                pullData(context, fromActivity, nextPageNo);
                            } else {
                                percentage = 100;
                                sessionManager.setPullExcutedTime(sessionManager.isPulled());
                                setProgress(percentage);
                                Intent broadcast = new Intent();
                                broadcast.putExtra("JOB", AppConstants.SYNC_PULL_DATA_DONE);
                                broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
                                broadcast.setPackage(IntelehealthApplication.getAppContext().getPackageName());
                                context.sendBroadcast(broadcast);
                                sessionManager.setLastSyncDateTime(
                                        AppConstants.dateAndTimeUtils.getcurrentDateTime(
                                                sessionManager.getAppLanguage()));
                            }
                        }

                        if (sessionManager.getTriggerNoti().equals("yes")) {
                            if (response.body().getData() != null) {
                                ArrayList<String> listPatientUUID = new ArrayList<String>();
                                List<VisitDTO> listVisitDTO = new ArrayList<>();
                                ArrayList<String> encounterVisitUUID = new ArrayList<String>();

                                for (int i = 0;
                                     i < response.body().getData().getEncounterDTO().size(); i++) {
                                    if (response.body().getData().getEncounterDTO().get(i)
                                            .getEncounterTypeUuid()
                                            .equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                                        encounterVisitUUID.add(
                                                response.body().getData().getEncounterDTO().get(i)
                                                        .getVisituuid());
                                    }
                                }
                                listVisitDTO.addAll(response.body().getData().getVisitDTO());
                                for (int i = 0; i < encounterVisitUUID.size(); i++) {
                                    for (int j = 0; j < listVisitDTO.size(); j++) {
                                        if (encounterVisitUUID.get(i)
                                                .equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
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
                    }, throwable -> {
                        FirebaseCrashlytics.getInstance().recordException(throwable);
                        IntelehealthApplication.getAppContext()
                                .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                        .setPackage(IntelehealthApplication.getAppContext()
                                                .getPackageName())
                                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                                AppConstants.SYNC_FAILED));
                    });
                } else {
                    IntelehealthApplication.getAppContext()
                            .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                    .setPackage(IntelehealthApplication.getAppContext()
                                            .getPackageName())
                                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                            AppConstants.SYNC_FAILED));
                }

                sessionManager.setLastPulledDateTime(
                        AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.getAppLanguage()));

                IntelehealthApplication.getAppContext()
                        .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .setPackage(
                                        IntelehealthApplication.getAppContext().getPackageName())
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                        AppConstants.SYNC_PULL_DATA_DONE));
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                IntelehealthApplication.getAppContext()
                        .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .setPackage(
                                        IntelehealthApplication.getAppContext().getPackageName())
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                        AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }


    public boolean pullDataBackgroundService(final Context context, String fromActivity,
                                             int pageNo) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        if (db == null) {
            db = mDbHelper.getWriteDb();
        }
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/pull/pulldata/" +
                     sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime() +
                     "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;
        
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(
                url, "Basic " + encoded);
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {
                    ResponseDTO responseDTO = response.body();
                    InitialSyncIntentService.setData(responseDTO);

                    Intent intent = new Intent(context, InitialSyncIntentService.class);
                    intent.putExtra("from", fromActivity);
                    context.startService(intent);


                    if (sessionManager.getTriggerNoti().equals("yes")) {
                        if (response.body().getData() != null) {
                            ArrayList<String> listPatientUUID = new ArrayList<String>();
                            List<VisitDTO> listVisitDTO = new ArrayList<>();
                            ArrayList<String> encounterVisitUUID = new ArrayList<String>();

                            for (int i = 0;
                                 i < response.body().getData().getEncounterDTO().size(); i++) {
                                if (response.body().getData().getEncounterDTO().get(i)
                                        .getEncounterTypeUuid()
                                        .equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                                    encounterVisitUUID.add(
                                            response.body().getData().getEncounterDTO().get(i)
                                                    .getVisituuid());
                                }
                            }
                            listVisitDTO.addAll(response.body().getData().getVisitDTO());
                            for (int i = 0; i < encounterVisitUUID.size(); i++) {
                                for (int j = 0; j < listVisitDTO.size(); j++) {
                                    if (encounterVisitUUID.get(i)
                                            .equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
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

                sessionManager.setLastPulledDateTime(
                        AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.getAppLanguage()));

                IntelehealthApplication.getAppContext()
                        .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .setPackage(
                                        IntelehealthApplication.getAppContext().getPackageName())
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                        AppConstants.SYNC_PULL_DATA_DONE));
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                IntelehealthApplication.getAppContext()
                        .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .setPackage(
                                        IntelehealthApplication.getAppContext().getPackageName())
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                        AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }

    public void setLocale(String lang) {
    }

    public void triggerVisitNotification(ArrayList<String> patientList) {
        for (int i = 0; i < patientList.size(); i++) {
            PatientsDAO patientsDAO = new PatientsDAO();
            try {
                String name = patientsDAO.getPatientName(patientList.get(i)).get(0).getName();
                AppConstants.notificationUtils.DownloadDone(
                        IntelehealthApplication.getAppContext().getString(R.string.visit),
                        name + " " + IntelehealthApplication.getAppContext()
                                .getString(R.string.visit_data_failed), 4,
                        IntelehealthApplication.getAppContext());
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

    }


    /**
     * Callback invoked once the encounter/obs/visit metadata push to the server has
     * actually completed (success or error), instead of guessing with a fixed delay.
     */
    public interface PushDataCallback {
        void onPushComplete(boolean success);
    }

    public boolean pushDataApi() {
        return pushDataApi(null);
    }

    public boolean pushDataApi(PushDataCallback callback) {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ProviderDAO providerDAO = new ProviderDAO();
        AppointmentDAO appointmentDAO = new AppointmentDAO();

        PushRequestApiCall pushRequestApiCall;
        PatientsFrameJson patientsFrameJson = new PatientsFrameJson();
        pushRequestApiCall = patientsFrameJson.frameJson();
        final boolean[] isSucess = {true};
        String encoded = sessionManager.getEncoded();
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/push/pushdata";
        if (!pushRequestApiCall.getVisits().isEmpty()
            || !pushRequestApiCall.getPersons().isEmpty()
            || !pushRequestApiCall.getPatients().isEmpty()
            || !pushRequestApiCall.getEncounters().isEmpty()
            || !pushRequestApiCall.getProviders().isEmpty()
            || !pushRequestApiCall.getAppointments().isEmpty()) {
            Single<PushResponseApiCall> pushResponseApiCallObservable
                    = AppConstants.apiInterface.PUSH_RESPONSE_API_CALL_OBSERVABLE(url,
                    "Basic " + encoded, pushRequestApiCall);
            pushResponseApiCallObservable.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io()) 
                    .subscribe(new DisposableSingleObserver<PushResponseApiCall>() {
                        @Override
                        public void onSuccess(PushResponseApiCall pushResponseApiCall) {
                            try {
                                if (pushResponseApiCall.getData().getPatientlist() != null) {
                                    for (int i = 0; i < pushResponseApiCall.getData().getPatientlist()
                                            .size(); i++) {
                                        try {
                                            patientsDAO.updateOpemmrsId(
                                                    pushResponseApiCall.getData().getPatientlist()
                                                            .get(i).getOpenmrsId(),
                                                    pushResponseApiCall.getData().getPatientlist()
                                                            .get(i).getSyncd().toString(),
                                                    pushResponseApiCall.getData().getPatientlist()
                                                            .get(i).getUuid());
                                        } catch (DAOException e) {
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        }
                                    }
                                }

                                if (pushResponseApiCall.getData().getVisitlist() != null) {
                                    for (int i = 0;
                                         i < pushResponseApiCall.getData().getVisitlist().size(); i++) {
                                        try {
                                            visitsDAO.updateVisitSync(
                                                    pushResponseApiCall.getData().getVisitlist().get(i)
                                                            .getUuid(),
                                                    pushResponseApiCall.getData().getVisitlist().get(i)
                                                            .getSyncd().toString());
                                        } catch (DAOException e) {
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        }
                                    }
                                }

                                if (pushResponseApiCall.getData().getEncounterlist() != null) {
                                    for (int i = 0;
                                         i < pushResponseApiCall.getData().getEncounterlist().size();
                                         i++) {
                                        try {
                                            encounterDAO.updateEncounterSync(
                                                    pushResponseApiCall.getData().getEncounterlist()
                                                            .get(i).getSyncd().toString(),
                                                    pushResponseApiCall.getData().getEncounterlist()
                                                            .get(i).getUuid());
                                        } catch (DAOException e) {
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        }
                                    }
                                }

                                if (pushResponseApiCall.getData().getProviderlist() != null) {
                                    for (int i = 0;
                                         i < pushResponseApiCall.getData().getProviderlist().size();
                                         i++) {
                                        try {
                                            providerDAO.updateProviderProfileSync(
                                                    pushResponseApiCall.getData().getProviderlist()
                                                            .get(i).getUuid(),
                                                    "1");
                                        } catch (Exception e) {
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        }
                                    }
                                }

                                if (pushResponseApiCall.getData().getAppointmentList() != null) {
                                    for (int i = 0; i < pushResponseApiCall.getData()
                                            .getAppointmentList().size(); i++) {
                                        try {
                                            appointmentDAO.updateAppointmentSync(
                                                    pushResponseApiCall.getData().getAppointmentList()
                                                            .get(i).getUuid(),
                                                    pushResponseApiCall.getData().getAppointmentList()
                                                            .get(i).getSync());
                                        } catch (Exception e) {
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                        }
                                    }
                                }

                                IntelehealthApplication.getAppContext()
                                        .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                                .setPackage(IntelehealthApplication.getAppContext()
                                                        .getPackageName())
                                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                                        AppConstants.SYNC_PUSH_DATA_DONE));
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                            if (callback != null) {
                                callback.onPushComplete(true);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            isSucess[0] = false;
                            IntelehealthApplication.getAppContext()
                                    .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                            .setPackage(IntelehealthApplication.getAppContext()
                                                    .getPackageName())
                                            .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                                    AppConstants.SYNC_FAILED));
                            if (callback != null) {
                                callback.onPushComplete(false);
                            }
                        }
                    });
            sessionManager.setPullSyncFinished(true);
            IntelehealthApplication.getAppContext()
                    .sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                            .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                            .putExtra(AppConstants.SYNC_INTENT_DATA_KEY,
                                    AppConstants.SYNC_PUSH_DATA_DONE));
        } else if (callback != null) {
            // Nothing to push (no pending visits/encounters/etc) - there is no
            // metadata push to wait on, so any pending obs images can be pushed now.
            callback.onPushComplete(true);
        }

        return isSucess[0];
    }

    public static void setProgress(int progress) {
        liveDataSync.updateProgress(progress);
    }

    public static SyncProgress getSyncProgress_LiveData() {
        return liveDataSync;
    }
    public void loadConfig() {
        isTheConfigUpdated = true;
    }
}

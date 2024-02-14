package org.intelehealth.ezazi.partogram;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.VisitAttributeListDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.models.dto.VisitDTO;
import org.intelehealth.ezazi.services.firebase_services.FirebaseRealTimeDBUtils;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.utilities.NotificationUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CardGenerationEngine {
    private static final String TAG = CardGenerationEngine.class.getName();
    private static ArrayList<EncounterDTO> encounters;
    private static ArrayList<ObsDTO> observations;

    public static void scanForNewCardEligibility() {
        encounters = new ArrayList<>();
        observations = new ArrayList<>();
        // all active visit list
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        List<VisitDTO> visitDTOList = visitsDAO.getAllActiveVisitByProviderId(new SessionManager(IntelehealthApplication.getAppContext()).getProviderID());
        Log.v(TAG, "visitDTOList count - " + visitDTOList.size());


        for (int i = 0; i < visitDTOList.size(); i++) {
            String visitUid = visitDTOList.get(i).getUuid();
            Log.v(TAG, "visitUid - " + new Gson().toJson(visitDTOList.get(i)));
            Log.v(TAG, "visitUid - " + visitUid);
            EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUid);
            if (encounterDTO != null && encounterDTO.getVisituuid().equals(visitUid)) {
                String latestEncounterTime = encounterDTO.getEncounterTime(); //eg. 2022-06-30T19:58:05.935+0530
                if (latestEncounterTime == null) continue;
                String latestEncounterName = encounterDAO.getEncounterTypeNameByUUID(encounterDTO.getEncounterTypeUuid()); //eg. Stage1_Hour1_1
                Log.v(TAG, "latestEncounterTime - " + latestEncounterTime);
                Log.v(TAG, "latestEncounterName - " + latestEncounterName);
                if (latestEncounterName != null && latestEncounterName.length() > 0) {
//                    Date encounterDate = DateTimeUtils.parseUTCDate(latestEncounterTime, DateAndTimeUtils.FORMAT_UTC);
//                    Date currentDate = DateTimeUtils.getCurrentDate(DateTimeUtils.getUTCTimeZone());

//                        SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
//                        SimpleDateFormat f3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//                        Date latestEncounterDate = latestEncounterTime.contains("T") || latestEncounterTime.contains("+") ? f2.parse(latestEncounterTime) : f3.parse(latestEncounterTime);
//
//                        Calendar c2 = Calendar.getInstance();
//                        c2.setTime(latestEncounterDate);
//                        c2.set(Calendar.SECOND, 0);
//                        c2.set(Calendar.MILLISECOND, 0);
//                        Calendar now = Calendar.getInstance();
//                        now.setTime(new Date());
//                        now.set(Calendar.SECOND, 0);
//                        now.set(Calendar.MILLISECOND, 0);

//                    long diff = currentDate.getTime() - encounterDate.getTime();//as given

//                    long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                    long minutes = DateTimeUtils.getMinDiffWithCurrentDate(latestEncounterTime, AppConstants.UTC_FORMAT);
                    Log.v(TAG, "minutes - " + minutes);
//                    Log.v(TAG, "seconds - " + seconds);

                    Map<String, String> log = new HashMap<>();
                    log.put("TAG", TAG);
                    log.put("action", "scanForNewCardEligibility");
                    log.put("visitUid", visitUid);
                    log.put("latestEncounterTime", latestEncounterTime);
                    log.put("latestEncounterName", latestEncounterName);
                    log.put("minutes", String.valueOf(minutes));
                    FirebaseRealTimeDBUtils.logData(log);

                    if (latestEncounterName.toLowerCase().contains("stage1")) {
                        if (minutes >= 30) {
                           /* //out of time -decision pending*/
                            if (checkVisitEncounterReachToLimitForStage1(latestEncounterName)) {
                                closeReachToLimitVisit(visitUid);
                            } else {
                                // get next encounter name
                                String nextEncounterTypeName = getNextEncounterTypeName(latestEncounterName);
                                if (nextEncounterTypeName != null) {
                                    Log.v(TAG, "nextEncounterTypeName - " + nextEncounterTypeName);
                                    createNewEncounter(visitUid, nextEncounterTypeName);
                                }
                            }
                        } else if (minutes == 29) {
                            SyncUtils syncUtils = new SyncUtils();
                            syncUtils.syncBackground();
                        }
                    } else if (latestEncounterName.toLowerCase().contains("stage2")) {
                        //Log.d(TAG, "scanForNewCardEligibility: minutes : " + minutes);

                        if (minutes >= 15) {
                            //out of time -decision pending
                            if (checkVisitEncounterReachToLimit(latestEncounterName)) {
                                closeReachToLimitVisit(visitUid);
                            } else {
                                // get next encounter name
                                String nextEncounterTypeName = getNextEncounterTypeName(latestEncounterName);
                                if (nextEncounterTypeName != null) {
                                    //Log.v(TAG, "nextEncounterTypeName - " + nextEncounterTypeName);
                                    createNewEncounter(visitUid, nextEncounterTypeName);
                                }
                            }
                        } else if (minutes == 14) {
                            SyncUtils syncUtils = new SyncUtils();
                            syncUtils.syncBackground();
                        }
                    }
                }
            }
        }

        boolean isInserted = false;

        try {
            isInserted = insertEncounters();
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        if (isInserted) {
//                SyncUtils syncUtils = new SyncUtils();
//                syncUtils.syncBackground();
            alertToCollectHistoryData();
            isNewEncounterCreated();
        }
    }

    private static void isNewEncounterCreated() {
        Intent intent = new Intent(AppConstants.CURRENT_ENC_EDIT_INTENT_ACTION);
        intent.putExtra("newEncounter", true);
        IntelehealthApplication.getAppContext().sendBroadcast(intent);
    }

    private static boolean insertEncounters() throws DAOException {
        Log.e(TAG, "New Encounters => " + encounters.size());
        Log.e(TAG, "New Obs => " + observations.size());
        if (encounters.size() > 0) {
            EncounterDAO encounterDAO = new EncounterDAO();
            encounterDAO.createBulkEncountersToDB(encounters);
            ObsDAO obsDAO = new ObsDAO();
            return obsDAO.insertObsToDb(observations, TAG);
        } else return false;
    }

    private static void playSound() {
        try {
            int callState = ((TelephonyManager) IntelehealthApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
            if (callState == TelephonyManager.CALL_STATE_IDLE) {
                MediaPlayer mediaPlayer = MediaPlayer.create(IntelehealthApplication.getAppContext(), R.raw.al_1);
                mediaPlayer.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createNewEncounter(String visit_UUID, String nextEncounterTypeName) {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        String encounterTypeUuid = encounterDAO.getEncounterTypeUuid(nextEncounterTypeName);
        if (encounterTypeUuid != null && encounterTypeUuid.length() > 0) {
            encounterDTO.setUuid(UUID.randomUUID().toString());
            encounterDTO.setVisituuid(visit_UUID);
            encounterDTO.setEncounterTime(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
            encounterDTO.setProvideruuid(new SessionManager(IntelehealthApplication.getAppContext()).getProviderID());
            encounterDTO.setEncounterTypeUuid(encounterTypeUuid);
            encounterDTO.setSyncd(false); // false as this is the one that is started and would be pushed in the payload...
            encounterDTO.setVoided(0);
            encounterDTO.setPrivacynotice_value("true");

            Map<String, String> log = new HashMap<>();
            log.put("TAG", TAG);
            log.put("action", "createNewEncounter");
            log.put("value", new Gson().toJson(encounterDAO));
            log.put("nextEncounterTypeName", nextEncounterTypeName);
            FirebaseRealTimeDBUtils.logData(log);
            SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
            encounters.add(encounterDTO);
            observations.add(new ObsDAO().createObs(encounterDTO.getUuid(), EncounterDTO.Type.NORMAL.name(), sessionManager.getCreatorID(), TAG));
//            try {
//                boolean status = encounterDAO.createEncountersToDB(encounterDTO);
//                if (status) {
//                    SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
//
//                    new ObsDAO().createEncounterType(encounterDTO.getUuid(), EncounterDTO.Type.NORMAL.name(), sessionManager.getCreatorID(), TAG);
////                    new VisitsDAO().updateVisitSync(encounterDTO.getVisituuid(), "false");
//                    Intent intent = new Intent(AppConstants.NEW_CARD_INTENT_ACTION);
//                    IntelehealthApplication.getAppContext().sendBroadcast(intent);
//                    sendNotification("Alert!", "Time to collect the History data!", null);
//                    int callState = ((TelephonyManager) IntelehealthApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
//                    if (callState == TelephonyManager.CALL_STATE_IDLE) {
//                        playSound();
//                    }
//
//                }
//            } catch (DAOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private static void alertToCollectHistoryData() {
        Intent intent = new Intent(AppConstants.NEW_CARD_INTENT_ACTION);
        IntelehealthApplication.getAppContext().sendBroadcast(intent);
        sendNotification("Alert!", "Time to collect the History data!", null);
        playSound();
    }

    private static boolean checkVisitEncounterReachToLimit(String encounterTypeName) {
        if (!encounterTypeName.toLowerCase().contains("stage") && !encounterTypeName.toLowerCase().contains("hour"))
            return false;
        String[] parts = encounterTypeName.toLowerCase().replaceAll("stage", "").replaceAll("hour", "").split("_");
        if (parts.length != 3) return false;
        int stageNumber = Integer.parseInt(parts[0]);
        int hourNumber = Integer.parseInt(parts[1]);
        int cardNumber = Integer.parseInt(parts[2]);

        return stageNumber == 2 && hourNumber == 5 && cardNumber == 4;
    }

    private static void closeReachToLimitVisit(String visitId) {
        Log.d(TAG, "kz11closeReachToLimitVisit: visitid :: " + visitId);
        Log.d(TAG, "kz11closeReachToLimitVisit: check insert value :");
        long updated = new VisitAttributeListDAO().updateVisitAttribute(visitId, UuidDictionary.DECISION_PENDING, "true");
        if (updated > 0) {
            try {
                VisitsDAO visitsDAO = new VisitsDAO();
                visitsDAO.updateVisitSync(visitId, "false");
                Intent intent = new Intent(AppConstants.VISIT_DECISION_PENDING_ACTION);
                IntelehealthApplication.getAppContext().sendBroadcast(intent);
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }
        }
//        ObsDAO obsDAO = new ObsDAO();
//        try {
//            String encounterUuid = new EncounterDAO().insertVisitCompleteEncounterToDb(visitId, sessionManager.getProviderID());
//            if (encounterUuid != null && encounterUuid.length() > 0) {
//                boolean isInserted = obsDAO.insert_Obs(encounterUuid, sessionManager.getCreatorID(), CompletedVisitStatus.OutOfTime.OUT_OF_TIME.value(), CompletedVisitStatus.OutOfTime.OUT_OF_TIME.uuid());
//                if (isInserted) {
//                    VisitsDAO visitsDAO = new VisitsDAO();
//                    visitsDAO.updateVisitEnddate(visitId, AppConstants.dateAndTimeUtils.currentDateTime());
//                    new VisitAttributeListDAO().markVisitAsRead(visitId);
//                    Intent intent = new Intent(AppConstants.VISIT_OUT_OF_TIME_ACTION);
//                    IntelehealthApplication.getAppContext().sendBroadcast(intent);
//                }
//            }
//        } catch (DAOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static String getNextEncounterTypeName(String encounterTypeName) {
        if (!encounterTypeName.toLowerCase().contains("stage") && !encounterTypeName.toLowerCase().contains("hour"))
            return null;
        String[] parts = encounterTypeName.toLowerCase().replaceAll("stage", "").replaceAll("hour", "").split("_");
        if (parts.length != 3) return null;
        int stageNumber = Integer.parseInt(parts[0]);
        int hourNumber = Integer.parseInt(parts[1]);
        int cardNumber = Integer.parseInt(parts[2]);
        if (stageNumber == 1) {
            if (cardNumber == 1) {
                cardNumber = 2;
            } else {
                hourNumber += 1;
                cardNumber = 1;
                if (hourNumber >= 21) {
                    stageNumber = 2;
                    hourNumber = 1;
                }
            }
        } else {
            if (cardNumber < 4) {
                cardNumber += 1;
            } else {
                hourNumber += 1;
                cardNumber = 1;

            }
        }
        return "Stage" + stageNumber + "_" + "Hour" + hourNumber + "_" + cardNumber;

    }

    private static void sendNotification(String title, String message, PendingIntent pendingIntent) {

        if (pendingIntent == null) {
            Intent intent = new Intent(IntelehealthApplication.getAppContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(IntelehealthApplication.getAppContext(), 0, intent, NotificationUtils.getPendingIntentFlag());
        }
        String channelId = "CHANNEL_ID_1";

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + IntelehealthApplication.getAppContext().getPackageName() + "/" + R.raw.al_1);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(IntelehealthApplication.getAppContext(), channelId).setSmallIcon(R.mipmap.ic_launcher)
                //.setContentTitle("Firebase Push Notification")
                .setContentTitle(title).setContentText(message).setAutoCancel(true).setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_HIGH);

        /*NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);*/

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(IntelehealthApplication.getAppContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Default Channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(100, notificationBuilder.build());

    }

    private static boolean checkVisitEncounterReachToLimitForStage1(String encounterTypeName) {
        if (!encounterTypeName.toLowerCase().contains("stage") && !encounterTypeName.toLowerCase().contains("hour"))
            return false;
        String[] parts = encounterTypeName.toLowerCase().replaceAll("stage", "").replaceAll("hour", "").split("_");
        if (parts.length != 3) return false;
        int stageNumber = Integer.parseInt(parts[0]);
        int hourNumber = Integer.parseInt(parts[1]);
        int cardNumber = Integer.parseInt(parts[2]);

        return stageNumber == 1 && hourNumber == 15 && cardNumber == 2;
    }

}

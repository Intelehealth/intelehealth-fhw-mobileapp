package org.intelehealth.ezazi.partogram;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.VisitDTO;
import org.intelehealth.ezazi.services.firebase_services.FirebaseRealTimeDBUtils;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.utilities.NotificationUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CardGenerationEngine {
    private static final String TAG = CardGenerationEngine.class.getName();

    public static void scanForNewCardEligibility() {
        try {

            // all active visit list
            VisitsDAO visitsDAO = new VisitsDAO();
            EncounterDAO encounterDAO = new EncounterDAO();
            List<VisitDTO> visitDTOList = visitsDAO.getAllActiveVisitsForMe(new SessionManager(IntelehealthApplication.getAppContext()).getCreatorID());
            Log.v(TAG, "visitDTOList count - " + visitDTOList.size());


            for (int i = 0; i < visitDTOList.size(); i++) {
                String visitUid = visitDTOList.get(i).getUuid();
                Log.v(TAG, "visitUid - " + new Gson().toJson(visitDTOList.get(i)));
                Log.v(TAG, "visitUid - " + visitUid);
                EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUid);
                String latestEncounterTime = encounterDTO.getEncounterTime(); //eg. 2022-06-30T19:58:05.935+0530
                if (latestEncounterTime == null) continue;
                String latestEncounterName = encounterDAO.getEncounterTypeNameByUUID(encounterDTO.getEncounterTypeUuid()); //eg. Stage1_Hour1_1

                SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
                SimpleDateFormat f3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                Date latestEncounterDate = latestEncounterTime.contains("T") || latestEncounterTime.contains("+") ? f2.parse(latestEncounterTime) : f3.parse(latestEncounterTime);

                Calendar c2 = Calendar.getInstance();
                c2.setTime(latestEncounterDate);
                c2.set(Calendar.SECOND, 0);
                c2.set(Calendar.MILLISECOND, 0);
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);


                Log.v(TAG, "latestEncounterTime - " + latestEncounterTime);
                Log.v(TAG, "latestEncounterName - " + latestEncounterName);

                long diff = now.getTimeInMillis() - c2.getTimeInMillis();//as given

                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                Log.v(TAG, "minutes - " + minutes);
                Log.v(TAG, "seconds - " + seconds);

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
                        // get next encounter name
                        String nextEncounterTypeName = getNextEncounterTypeName(latestEncounterName);
                        if (nextEncounterTypeName != null) {
                            Log.v(TAG, "nextEncounterTypeName - " + nextEncounterTypeName);
                            createNewEncounter(visitUid, nextEncounterTypeName);
                        }
                    } else if (minutes == 29) {
                        SyncUtils syncUtils = new SyncUtils();
                        syncUtils.syncBackground();
                    }
                } else if (latestEncounterName.toLowerCase().contains("stage2")) {
                    if (minutes >= 15) {
                        // get next encounter name
                        String nextEncounterTypeName = getNextEncounterTypeName(latestEncounterName);
                        if (nextEncounterTypeName != null) {
                            Log.v(TAG, "nextEncounterTypeName - " + nextEncounterTypeName);
                            createNewEncounter(visitUid, nextEncounterTypeName);
                        }
                    } else if (minutes == 14) {
                        SyncUtils syncUtils = new SyncUtils();
                        syncUtils.syncBackground();
                    }
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void playSound() {

        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(IntelehealthApplication.getAppContext(), R.raw.al_1);
            mediaPlayer.start();
            mediaPlayer.setOnSeekCompleteListener(mp -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createNewEncounter(String visit_UUID, String nextEncounterTypeName) {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();

        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setVisituuid(visit_UUID);
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setProvideruuid(new SessionManager(IntelehealthApplication.getAppContext()).getProviderID());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid(nextEncounterTypeName));
        encounterDTO.setSyncd(false); // false as this is the one that is started and would be pushed in the payload...
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value("true");

        Map<String, String> log = new HashMap<>();
        log.put("TAG", TAG);
        log.put("action", "createNewEncounter");
        log.put("value", new Gson().toJson(encounterDAO));
        log.put("nextEncounterTypeName", nextEncounterTypeName);
        FirebaseRealTimeDBUtils.logData(log);

        try {
            boolean status = encounterDAO.createEncountersToDB(encounterDTO);
            if (status) {
                Intent intent = new Intent(AppConstants.NEW_CARD_INTENT_ACTION);
                IntelehealthApplication.getAppContext().sendBroadcast(intent);

                sendNotification("Alert!", "Time to collect the History data!", null);
                int callState = ((TelephonyManager) IntelehealthApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                if (callState == TelephonyManager.CALL_STATE_IDLE) {
                    playSound();
                }

            }
        } catch (DAOException e) {
            e.printStackTrace();
        }

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
            pendingIntent = PendingIntent.getActivity(IntelehealthApplication.getAppContext(), 0, intent,
                    NotificationUtils.getPendingIntentFlag());
        }
        String channelId = "CHANNEL_ID_1";

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + IntelehealthApplication.getAppContext().getPackageName() + "/" + R.raw.al_1);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(IntelehealthApplication.getAppContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentTitle("Firebase Push Notification")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

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
}

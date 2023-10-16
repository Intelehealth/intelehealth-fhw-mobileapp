package org.intelehealth.ezazi.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.util.UUID;

/**
* Created by Prajwal Maruti Waingankar on 12-05-2022, 11:29
* Copyright (c) 2021 . All rights reserved.
* Email: prajwalwaingankar@gmail.com
* Github: prajwalmw
*/

public class NotificationReceiver extends BroadcastReceiver {
    PowerManager.WakeLock wl;
    String visitUuid, providerID, nextIntervalEncounterTypeUuid_Name;
    int timeTag = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("NotificationReceiver", "onReceive");

//        NotificationUtils notificationUtils = new NotificationUtils();
//        notificationUtils.createTimelineNotification(context, intent);

        if(intent != null) {
            visitUuid = intent.getStringExtra("visitUuid");
            providerID = intent.getStringExtra("providerID");
            timeTag = intent.getIntExtra("timeTag", 0);
            fetchLatestEncounterTypeUuid_DisplayText(visitUuid);
        }


      /*  PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        if(isScreenOn==false)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE,"myApp:notificationLock");
            wl.acquire(1000);

            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myApp:mycpu_notificationLock");
            wl_cpu.acquire(1000);

            // Official Doc: acquire() -> Ensures that the device is on at the level requested when the wake lock was created.
            // The lock will be released after the given timeout expires.
        }*/

    }

    private void fetchLatestEncounterTypeUuid_DisplayText(String visitUuid) {
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterTypeName = "";

        encounterTypeName = encounterDAO.fetchLatestEncounterTypeUuid(visitUuid);

        if(encounterTypeName != null && !encounterTypeName.equalsIgnoreCase("")) {
            if(timeTag == 15) {
                nextIntervalEncounterTypeUuid_Name = compareAndProvideStage2EncounterUuid(encounterTypeName);
            }
            else if (timeTag == 30) {
                nextIntervalEncounterTypeUuid_Name = compareAndProvideNextIntervalEncounterTypeUuid(encounterTypeName);
            }
            else {

            }

            if(nextIntervalEncounterTypeUuid_Name != null &&
                    !nextIntervalEncounterTypeUuid_Name.equalsIgnoreCase("")) {
                createNewEncounter(visitUuid, providerID, nextIntervalEncounterTypeUuid_Name);
            }
        }
    }

    private String compareAndProvideStage2EncounterUuid(String encounterTypeName) {
        String result = "";

        switch (encounterTypeName) {
            case "Stage2_Hour1_1":
                result = "Stage2_Hour1_2";
                break;

            case "Stage2_Hour1_2":
                result = "Stage2_Hour1_3";
                break;

            case "Stage2_Hour1_3":
                result = "Stage2_Hour1_4";
                break;

            case "Stage2_Hour1_4":
                result = "Stage2_Hour2_1";
                break;

            case "Stage2_Hour2_1":
                result = "Stage2_Hour2_2";
                break;

            case "Stage2_Hour2_2":
                result = "Stage2_Hour2_3";
                break;

            case "Stage2_Hour2_3":
                result = "Stage2_Hour2_4";
                break;

            case "Stage2_Hour2_4":
                result = "Stage2_Hour3_1";
                break;

            case "Stage2_Hour3_1":
                result = "Stage2_Hour3_2";
                break;

            case "Stage2_Hour3_2":
                result = "Stage2_Hour3_3";
                break;

            case "Stage2_Hour3_3":
                result = "Stage2_Hour3_4";
                break;

            case "Stage2_Hour3_4":
                result = "Stage2_Hour4_1";
                break;

            case "Stage2_Hour4_1":
                result = "Stage2_Hour4_2";
                break;

            case "Stage2_Hour4_2":
                result = "Stage2_Hour4_3";
                break;

            case "Stage2_Hour4_3":
                result = "Stage2_Hour4_4";
                break;

            case "Stage2_Hour4_4":
                result = "Stage2_Hour5_1";
                break;

            case "Stage2_Hour5_1":
                result = "Stage2_Hour5_2";
                break;

            case "Stage2_Hour5_2":
                result = "Stage2_Hour5_3";
                break;

            case "Stage2_Hour5_3":
                result = "Stage2_Hour5_4";
                break;

            default:
                result = "Stage2_Hour1_1";
                // ie. If any other encounterName is passed and it will be passed from Stage1 obvious
                // it doesnt match above than it will return this value that we need to start the stage2 encounters list.
        }
        return result;
    }


    private String compareAndProvideNextIntervalEncounterTypeUuid(String encounterTypeName) {
        String result = "";
        switch (encounterTypeName) {
            case "Stage1_Hour1_1":
                result = "Stage1_Hour1_2";
                break;

            case "Stage1_Hour1_2":
                result = "Stage1_Hour2_1";
                break;

            case "Stage1_Hour2_1":
                result = "Stage1_Hour2_2";
                break;

            case "Stage1_Hour2_2":
                result = "Stage1_Hour3_1";
                break;

            case "Stage1_Hour3_1":
                result = "Stage1_Hour3_2";
                break;

            case "Stage1_Hour3_2":
                result = "Stage1_Hour4_1";
                break;

            case "Stage1_Hour4_1":
                result = "Stage1_Hour4_2";
                break;

            case "Stage1_Hour4_2":
                result = "Stage1_Hour5_1";
                break;

            case "Stage1_Hour5_1":
                result = "Stage1_Hour5_2";
                break;

            case "Stage1_Hour5_2":
                result = "Stage1_Hour6_1";
                break;

            case "Stage1_Hour6_1":
                result = "Stage1_Hour6_2";
                break;

            case "Stage1_Hour6_2":
                result = "Stage1_Hour7_1";
                break;

            case "Stage1_Hour7_1":
                result = "Stage1_Hour7_2";
                break;

            case "Stage1_Hour7_2":
                result = "Stage1_Hour8_1";
                break;

            case "Stage1_Hour8_1":
                result = "Stage1_Hour8_2";
                break;

            case "Stage1_Hour8_2":
                result = "Stage1_Hour9_1";
                break;

            case "Stage1_Hour9_1":
                result = "Stage1_Hour9_2";
                break;

            case "Stage1_Hour9_2":
                result = "Stage1_Hour10_1";
                break;

            case "Stage1_Hour10_1":
                result = "Stage1_Hour10_2";
                break;

            case "Stage1_Hour10_2":
                result = "Stage1_Hour11_1";
                break;

            case "Stage1_Hour11_1":
                result = "Stage1_Hour11_2";
                break;

            case "Stage1_Hour11_2":
                result = "Stage1_Hour12_1";
                break;

            case "Stage1_Hour12_1":
                result = "Stage1_Hour12_2";
                break;

            case "Stage1_Hour12_2":
                result = "Stage1_Hour13_1";
                break;

            case "Stage1_Hour13_1":
                result = "Stage1_Hour13_2";
                break;

            default:
                result = "";
        }

        return result;
    }

    private void createNewEncounter(String visit_UUID, String provider_ID, String nextEncounterTypeUuid) {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();

        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setVisituuid(visit_UUID);
        encounterDTO.setEncounterTime(DateTimeUtils.getCurrentDateInUTC(AppConstants.UTC_FORMAT));
        encounterDTO.setProvideruuid(provider_ID);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid(nextEncounterTypeUuid));
        encounterDTO.setSyncd(false); // false as this is the one that is started and would be pushed in the payload...
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value("true");

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

}

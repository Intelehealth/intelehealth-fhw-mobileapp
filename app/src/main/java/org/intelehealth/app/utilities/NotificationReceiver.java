package org.intelehealth.app.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.UUID;

/**
* Created by Prajwal Maruti Waingankar on 12-05-2022, 11:29
* Copyright (c) 2021 . All rights reserved.
* Email: prajwalwaingankar@gmail.com
* Github: prajwalmw
*/

public class NotificationReceiver extends BroadcastReceiver {
    PowerManager.WakeLock wl;
    String visitUuid, providerID;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.createTimelineNotification(context, intent);

        createNewEncounter(intent);

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
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
        }

    }

    private void createNewEncounter(Intent intent) {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();

        visitUuid = intent.getStringExtra("visitUuid");
        providerID = intent.getStringExtra("providerID");

        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setProvideruuid(providerID);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("Stage1_Hour1_2"));
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

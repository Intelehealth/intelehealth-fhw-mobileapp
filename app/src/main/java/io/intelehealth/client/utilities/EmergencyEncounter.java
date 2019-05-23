package io.intelehealth.client.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.intelehealth.client.dao.EncounterDAO;
import io.intelehealth.client.dto.EncounterDTO;
import io.intelehealth.client.exception.DAOException;

public class EmergencyEncounter {

    public boolean uploadEncounterEmergency(String visitUuid) {
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);

        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("EMERGENCY"));
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void removeEncounterEmergency() {


    }
}

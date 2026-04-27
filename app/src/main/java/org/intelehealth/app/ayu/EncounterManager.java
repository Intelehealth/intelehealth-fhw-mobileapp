package org.intelehealth.app.ayu;

import android.util.Log;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import java.util.UUID;

public class EncounterManager {
    private ObsDAO obsDAO;
    private EncounterDAO encounterDAO;
    private SessionManager sessionManager;

    // TODO: Replace with real OpenMRS concept UUIDs for stethoscope data
    public static final String STETHOSCOPE_AUDIO_CONCEPT_UUID = "5307a514-6d9b-4394-a902-8a9d16416962";
    public static final String STETHOSCOPE_RESULT_CONCEPT_UUID = "e2d5854b-7009-4822-9114-1e03a95c9602";

    public EncounterManager() {
        this.obsDAO = new ObsDAO();
        this.encounterDAO = new EncounterDAO();
        this.sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
    }

    public interface EncounterCallback {
        void onCreated(String encounterUuid);
    }

    public void createEncounter(String patientUuid, String visitUuid, EncounterCallback cb) {
        try {
            String encounterUuid = UUID.randomUUID().toString();
            EncounterDTO encounterDTO = new EncounterDTO();
            encounterDTO.setUuid(encounterUuid);
            encounterDTO.setVisituuid(visitUuid);
            encounterDTO.setEncounterTypeUuid(UuidDictionary.ENCOUNTER_VITALS);
            encounterDTO.setProvideruuid(sessionManager.getProviderID());
            
            // AppConstants.dateAndTimeUtils is static and initialized in AppConstants.java
            if (AppConstants.dateAndTimeUtils != null) {
                encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
            }

            encounterDTO.setVoided(0);
            encounterDTO.setSyncd(false);

            boolean success = encounterDAO.createEncountersToDB(encounterDTO);
            if (success && cb != null) {
                cb.onCreated(encounterUuid);
            }
        } catch (Exception e) {
            Log.e("EncounterManager", "Error creating encounter: " + e.getMessage());
        }
    }

    public void sendAudioObs(String encounterUuid, String audioUrl) {
        try {
            ObsDTO obsDTO = new ObsDTO();
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setConceptuuid(STETHOSCOPE_AUDIO_CONCEPT_UUID);
            obsDTO.setValue(audioUrl);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setVoided(0);
            
            obsDAO.insertObs(obsDTO);
            Log.d("EncounterManager", "Audio Obs saved locally: " + audioUrl);
        } catch (Exception e) {
            Log.e("EncounterManager", "Error saving Audio Obs: " + e.getMessage());
        }
    }

    public void sendResultObs(String encounterUuid, String result) {
        try {
            ObsDTO obsDTO = new ObsDTO();
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setConceptuuid(STETHOSCOPE_RESULT_CONCEPT_UUID);
            obsDTO.setValue(result);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setVoided(0);

            obsDAO.insertObs(obsDTO);
            Log.d("EncounterManager", "AI Result Obs saved locally: " + result);
        } catch (Exception e) {
            Log.e("EncounterManager", "Error saving Result Obs: " + e.getMessage());
        }
    }
}

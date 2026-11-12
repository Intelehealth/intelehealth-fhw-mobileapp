package org.intelehealth.app.ayu.visit.ncd;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.UUID;

/**
 * Saves the NCD consent, eligibility and CBAC answers, and the medicine photos, as obs of one NCD Screening
 * encounter per visit. They upload with the rest of the visit through the usual push, once the visit is sent.
 */
public class NcdScreeningRepository {

    private final String mProviderUuid;
    private final String mCreatorUuid;
    private final EncounterDAO mEncounterDAO = new EncounterDAO();
    private final ObsDAO mObsDAO = new ObsDAO();
    private final ImagesDAO mImagesDAO = new ImagesDAO();

    public NcdScreeningRepository(String providerUuid, String creatorUuid) {
        mProviderUuid = providerUuid;
        mCreatorUuid = creatorUuid;
    }

    public void saveConsent(String visitUuid, boolean accepted) {
        save(visitUuid, UuidDictionary.NCD_CONSENT, NcdScreeningValues.consent(accepted));
    }

    public void saveEligibility(String visitUuid, NcdEligibilityState state) {
        save(visitUuid, UuidDictionary.NCD_ELIGIBILITY, NcdScreeningValues.eligibility(state));
        saveMedicinePhotos(visitUuid, state);
    }

    // Image obs like Physical Exam photos, so the usual image upload sends them; the file name is the obs uuid
    private void saveMedicinePhotos(String visitUuid, NcdEligibilityState state) {
        if (state.getMedicinePhotoPaths().isEmpty()) return;
        String comment = NcdScreeningValues.medicinePhotoComment(state.getKnownCaseText());
        try {
            String encounterUuid = encounterFor(visitUuid);
            for (String path : state.getMedicinePhotoPaths()) {
                String obsUuid = path.substring(path.lastIndexOf('/') + 1).split("\\.")[0];
                mImagesDAO.insertObsImageDatabase(obsUuid, encounterUuid, UuidDictionary.COMPLEX_IMAGE_PE, comment);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void saveCbac(String visitUuid, CbacAssessmentState state) {
        save(visitUuid, UuidDictionary.CBAC_QUESTIONNAIRE, NcdScreeningValues.cbacAnswers(state));
        save(visitUuid, UuidDictionary.CBAC_SCORE, NcdScreeningValues.cbacScore(state.getTotalScore()));
    }

    // One obs per concept: saving again updates it instead of adding another
    private void save(String visitUuid, String conceptUuid, String value) {
        try {
            String encounterUuid = encounterFor(visitUuid);
            ObsDTO obs = new ObsDTO();
            obs.setEncounteruuid(encounterUuid);
            obs.setConceptuuid(conceptUuid);
            obs.setCreator(mCreatorUuid);
            obs.setValue(value);
            String existingUuid = mObsDAO.getObsuuid(encounterUuid, conceptUuid);
            if (existingUuid != null) {
                obs.setUuid(existingUuid);
                mObsDAO.updateObs(obs);
            } else {
                mObsDAO.insertObs(obs);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    // The visit's NCD Screening encounter, created on first use (getEmergencyEncounters finds any type)
    private String encounterFor(String visitUuid) throws DAOException {
        String uuid = mEncounterDAO.getEmergencyEncounters(visitUuid, UuidDictionary.ENCOUNTER_NCD_SCREENING);
        if (uuid != null && !uuid.isEmpty()) return uuid;
        EncounterDTO encounter = new EncounterDTO();
        encounter.setUuid(UUID.randomUUID().toString());
        encounter.setEncounterTypeUuid(UuidDictionary.ENCOUNTER_NCD_SCREENING);
        encounter.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounter.setVisituuid(visitUuid);
        encounter.setSyncd(false);
        encounter.setProvideruuid(mProviderUuid);
        encounter.setVoided(0);
        mEncounterDAO.createEncountersToDB(encounter);
        return encounter.getUuid();
    }
}

package org.intelehealth.app.ayu.visit.ncd;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONArray;
import org.json.JSONObject;

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

    // The visit's screening plan from what was saved, so new and edited visits follow the same rules;
    // null when the visit had no NCD screening or its answers can't be read
    public NcdScreeningPlan loadPlan(Context context, String visitUuid) {
        try {
            String encounterUuid = mEncounterDAO.getEmergencyEncounters(visitUuid, UuidDictionary.ENCOUNTER_NCD_SCREENING);
            if (encounterUuid == null || encounterUuid.isEmpty()) return null;
            String consent = mObsDAO.getObsValue(encounterUuid, UuidDictionary.NCD_CONSENT);
            if (NcdScreeningValues.consent(false).equals(consent)) return NcdScreeningPlan.consentDeclined();
            String eligibility = mObsDAO.getObsValue(encounterUuid, UuidDictionary.NCD_ELIGIBILITY);
            NcdEligibilityRules.Result result = NcdScreeningValues.savedResult(eligibility);
            if (result == null) return null;
            String knownCaseId = knownCaseOptionId(context, NcdScreeningValues.savedKnownCaseText(eligibility));
            Integer score = NcdScreeningValues.savedScore(mObsDAO.getObsValue(encounterUuid, UuidDictionary.CBAC_SCORE));
            return NcdScreeningPlan.of(result, knownCaseId, score);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    // The saved known-case text back to its option id, from the same question file
    private static String knownCaseOptionId(Context context, String text) {
        if (text == null) return null;
        try {
            JSONObject json = FileUtils.encodeJSON(context, NcdEligibilityFragment.NCD_ELIGIBILITY_FILE);
            JSONArray questions = json == null ? null : json.optJSONArray("options");
            for (int i = 0; questions != null && i < questions.length(); i++) {
                JSONObject question = questions.getJSONObject(i);
                if (!NcdEligibilityFragment.KNOWN_CASE.equals(question.optString("id"))) continue;
                JSONArray options = question.optJSONArray("options");
                for (int j = 0; options != null && j < options.length(); j++) {
                    if (text.equals(options.getJSONObject(j).optString("text"))) return options.getJSONObject(j).optString("id");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return null;
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

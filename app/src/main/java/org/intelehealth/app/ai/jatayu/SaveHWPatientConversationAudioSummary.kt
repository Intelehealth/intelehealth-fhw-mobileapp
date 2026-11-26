package org.intelehealth.app.ai.jatayu

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.ObsDAO
import org.intelehealth.app.models.dto.ObsDTO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.jatayu.audiorecording.VisitVoiceRecorder
import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.app.utilities.exception.DAOException
import org.json.JSONObject
import org.json.JSONTokener
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Saves the Jatayu VoiceDoc output for a visit's health worker - patient conversation as one
 * [UuidDictionary.PATIENT_HW_TALK_AUDIO_SUMMARY_CONCEPT_UUID] obs on the visit's adult-initial
 * encounter (the same encounter as the AI visit summary obs):
 *  1. [saveResult]: the VoiceDoc result (transcript + section summary) into the obs value;
 *  2. [savePatientSummary]: the /api/patient-visit-summary response into the obs comments.
 *
 * Writes run on one background thread, so they stay in arrival order (value before comments)
 * and keep the DB off the main thread. Each write marks the obs and its encounter unsynced so
 * the next sync pushes them.
 */
object SaveHWPatientConversationAudioSummary {

    private const val TAG = "SaveHWPatientConversationAudioSummary"
    private const val CONCEPT_UUID = UuidDictionary.PATIENT_HW_TALK_AUDIO_SUMMARY_CONCEPT_UUID

    private val executor = Executors.newSingleThreadExecutor()

    /** Step 1: stores the VoiceDoc result as the obs value (inserted, or replaced on re-record). */
    @JvmStatic
    fun saveResult(visitUuid: String?, sessionName: String?, transcript: String?, summaryJson: String?) {
        Timber.tag(TAG).d("saveResult: visit=$visitUuid session=$sessionName transcript=${transcript?.length} chars summary=${summaryJson?.length} chars")
        executor.execute {
            try {
                saveResultToDb(visitUuid, sessionName, transcript, summaryJson)
            } finally {
                // Saved or not, stop the visit upload from waiting for it (see VisitSummaryActivity_New).
                VisitVoiceRecorder.onResultSaved(visitUuid)
            }
        }
    }

    private fun saveResultToDb(visitUuid: String?, sessionName: String?, transcript: String?, summaryJson: String?) {
        val encounterUuid = adultInitialEncounter(visitUuid) ?: return
        val value = JSONObject()
            .put("sessionName", sessionName)
            .put("transcript", transcript.orEmpty())
            .put("summary", parse(summaryJson))
            .toString()
        try {
            val obsDAO = ObsDAO()
            val obsDTO = ObsDTO().apply {
                conceptuuid = CONCEPT_UUID
                encounteruuid = encounterUuid
                creator = SessionManager(IntelehealthApplication.getAppContext()).creatorID
                this.value = value
            }
            val existingObsUuid = obsDAO.getObsuuid(encounterUuid, CONCEPT_UUID)
            val isSaved = if (existingObsUuid != null) {
                obsDTO.uuid = existingObsUuid
                obsDAO.updateObs(obsDTO)
            } else {
                obsDAO.insertObs(obsDTO)
            }
            markEncounterUnsynced(encounterUuid)
            Timber.tag(TAG).i("saveResult: visit=$visitUuid encounter=$encounterUuid isSaved=$isSaved")
        } catch (e: DAOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Timber.tag(TAG).e("saveResult failed for visit $visitUuid: ${e.message}")
        }
    }

    /** Step 2: stores the patient summary in the comments of the obs saved by [saveResult]. */
    @JvmStatic
    fun savePatientSummary(visitUuid: String?, patientSummaryJson: String?) {
        Timber.tag(TAG).d("savePatientSummary: visit=$visitUuid summary=${patientSummaryJson?.length} chars")
        if (!hasPatientSummary(patientSummaryJson)) {
            Timber.tag(TAG).w("savePatientSummary: no patient summary for visit $visitUuid yet, not saved: $patientSummaryJson")
            return
        }
        executor.execute {
            val encounterUuid = adultInitialEncounter(visitUuid) ?: return@execute
            try {
                val obsDAO = ObsDAO()
                val obsUuid = obsDAO.getObsuuid(encounterUuid, CONCEPT_UUID)
                if (obsUuid == null) {
                    Timber.tag(TAG).e("savePatientSummary: no conversation obs for visit $visitUuid, result not saved first")
                    return@execute
                }
                val isSaved = obsDAO.updateObsComments(obsUuid, patientSummaryJson.orEmpty())
                markEncounterUnsynced(encounterUuid)
                Timber.tag(TAG).i("savePatientSummary: visit=$visitUuid obs=$obsUuid isSaved=$isSaved")
            } catch (e: DAOException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Timber.tag(TAG).e("savePatientSummary failed for visit $visitUuid: ${e.message}")
            }
        }
    }

    /**
     * The visit's adult-initial encounter, synced or not. (EncounterDAO's
     * fetchEncounterUuidForEncounterAdultInitials only returns synced ones, and the result can
     * arrive before the visit has synced.) Null if the visit is gone, e.g. discarded.
     */
    private fun adultInitialEncounter(visitUuid: String?): String? {
        if (visitUuid.isNullOrBlank()) {
            Timber.tag(TAG).e("No visit UUID on VoiceDoc result, can't save it")
            return null
        }
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.readableDatabase
        db.rawQuery(
            "SELECT uuid FROM tbl_encounter WHERE visituuid = ? AND encounter_type_uuid = ? AND voided = 0 LIMIT 1",
            arrayOf(visitUuid, UuidDictionary.ENCOUNTER_ADULTINITIAL)
        ).use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0)
        }
        Timber.tag(TAG).e("No adult-initial encounter for visit $visitUuid, VoiceDoc output not saved")
        return null
    }

    /**
     * False for the server's "nothing yet" answer, e.g.
     * {"overall_summary":"No visit records found for this patient.","visits_found":0,"visits":[]},
     * and for empty bodies - those must never land in the obs comments.
     */
    private fun hasPatientSummary(json: String?): Boolean {
        if (json.isNullOrBlank()) return false
        val obj = runCatching { JSONObject(json) }.getOrNull() ?: return json.trim() !in setOf("[]", "null", "\"\"")
        if (obj.length() == 0) return false
        if (obj.has("visits_found") && obj.optInt("visits_found") == 0) return false
        val visits = obj.optJSONArray("visits")
        return visits == null || visits.length() > 0
    }

    /** Sync only pushes obs of unsynced encounters, and the visit may have synced already. */
    private fun markEncounterUnsynced(encounterUuid: String) {
        EncounterDAO().updateEncounterSync("false", encounterUuid)
    }

    private fun parse(json: String?): Any =
        runCatching { JSONTokener(json.orEmpty()).nextValue() }.getOrNull() ?: JSONObject.NULL
}

package org.intelehealth.app.ui.baseline_survey.helper

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.intelehealth.app.R
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.ncd.model.MedicalHistory
import org.intelehealth.ncd.utils.CategorySegregationUtils
import org.json.JSONArray

class MissingLineListingQuestionsHelper(private val context: Context) {

    private val resources = context.resources
    private val yesString = resources.getString(R.string.medical_history_yes)
    private val noString = resources.getString(R.string.medical_history_no)

    fun evaluateMedicalHistory(patientId: String?, patientAge: Int?): MissingLineListingResult {
        val medicalHistoryJson: String? = PatientsDAO().fetchBaselineMedicalHistory(patientId)
        val history: MedicalHistory? = convertJsonToMedicalHistory(medicalHistoryJson)
        Log.e("MedicalHistoryParser", "medicalHistoryJson: ${medicalHistoryJson}")
        Log.e("MedicalHistoryParser", "history: ${history}")
        val age = patientAge ?: 0
        Log.d("TAG", "evaluateMedicalHistory: age : "+age)
        val isApplicableForAnemia = age > 11
        val isApplicableForBP = age >= 18
        val isApplicableForDiabetes = age >= 20

        if (history == null) {
            val emptyStatus = ComplaintStatus(false, false)
            return MissingLineListingResult(
                anemia = emptyStatus,
                bp = emptyStatus,
                diabetes = emptyStatus,
                hasAnyHistoryWithoutMedication = false
            )
        }

        val anemiaStatus = ComplaintStatus(
            hasHistory = isApplicableForAnemia &&  isHistoryPresent(history.anemia),
            onMedication = isApplicableForAnemia && isMedicationPresent(history.medicationForAnemia),
          /*  isHwForComplaintPresent = isHwForComplaintPresent(history.healthWorkerForAnemia),
            isReasonForNoMedicationPresent = isReasonForNoMedicationPresent(history.healthWorkerForAnemia)*/
        )

        val bpStatus = ComplaintStatus(
            hasHistory = isApplicableForBP && isHistoryPresent(history.hypertension),
            onMedication = isApplicableForBP && isMedicationPresent(history.medicationForBP)
        )

        val diabetesStatus = ComplaintStatus(
            hasHistory = isApplicableForDiabetes && isHistoryPresent(history.diabetes),
            onMedication = isApplicableForDiabetes && isMedicationPresent(history.medicationForDiabetes)
        )

        // ---------- MISSING MEDICATION CHECK (AGE-SAFE) ----------
        val hasAnyHistoryWithoutMedication = listOf(
            Triple(anemiaStatus, history.anemia, isApplicableForAnemia),
            Triple(bpStatus, history.hypertension, isApplicableForBP),
            Triple(diabetesStatus, history.diabetes, isApplicableForDiabetes)
        ).any { (status, historyValue, isAgeEligible) ->
            isAgeEligible &&
                    historyValue.equals(yesString, ignoreCase = true) &&
                    !status.onMedication
        }
       /* val hasAnyHistoryWithoutMedication = listOf(
            anemiaStatus to history.anemia,
            bpStatus to history.hypertension,
            diabetesStatus to history.diabetes
        ).any { (status, histValue) ->
            histValue.equals(resources.getString(R.string.medical_history_yes), ignoreCase = true)
                    && !status.onMedication
        }*/


        return MissingLineListingResult(
            anemia = anemiaStatus,
            bp = bpStatus,
            diabetes = diabetesStatus,
            hasAnyHistoryWithoutMedication = hasAnyHistoryWithoutMedication
        )
    }

    // ---- Check if history = Yes ----
    private fun isHistoryPresent(value: String?): Boolean {
        return !value.isNullOrBlank() && value.trim() == yesString
    }

    // ---- Check if medication is explicitly present ("Yes" or "No") ----
    private fun isMedicationPresent(value: String?): Boolean {
        if (value.isNullOrBlank() || value.trim() == "-") return false
        val trimmedValue = value.trim()
        return trimmedValue.equals(yesString, true) || trimmedValue.equals(noString, true)
    }
    private fun convertJsonToMedicalHistory(medicalHistoryJson: String?): MedicalHistory? {
        if (medicalHistoryJson.isNullOrBlank()) return null

        return try {
            val gson = Gson()
            val jsonElement = JsonParser.parseString(medicalHistoryJson)

            val history: MedicalHistory? = when {
                jsonElement.isJsonArray -> {
                    val list: List<MedicalHistory> =
                        gson.fromJson(jsonElement, object : TypeToken<List<MedicalHistory>>() {}.type)
                    list.firstOrNull()
                }
                jsonElement.isJsonObject -> gson.fromJson(jsonElement, MedicalHistory::class.java)
                else -> null
            }
            history
        } catch (e: Exception) {
            Log.e("MedicalHistoryParser", "Failed to parse: ${e.message}")
            null
        }
    }
}

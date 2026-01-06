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

    fun evaluateMedicalHistory(patientId: String?): MissingLineListingResult {
        val medicalHistoryJson: String? = PatientsDAO().fetchBaselineMedicalHistory(patientId)
        val history: MedicalHistory? = convertJsonToMedicalHistory(medicalHistoryJson)
        Log.e("MedicalHistoryParser", "medicalHistoryJson: ${medicalHistoryJson}")
        Log.e("MedicalHistoryParser", "history: ${history}")

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
            hasHistory = isHistoryPresent(history.anemia),
            onMedication = isMedicationPresent(history.medicationForAnemia),
          /*  isHwForComplaintPresent = isHwForComplaintPresent(history.healthWorkerForAnemia),
            isReasonForNoMedicationPresent = isReasonForNoMedicationPresent(history.healthWorkerForAnemia)*/
        )

        val bpStatus = ComplaintStatus(
            hasHistory = isHistoryPresent(history.hypertension),
            onMedication = isMedicationPresent(history.medicationForBP)
        )

        val diabetesStatus = ComplaintStatus(
            hasHistory = isHistoryPresent(history.diabetes),
            onMedication = isMedicationPresent(history.medicationForDiabetes)
        )

        val hasAnyHistoryWithoutMedication = listOf(
            anemiaStatus to history.anemia,
            bpStatus to history.hypertension,
            diabetesStatus to history.diabetes
        ).any { (status, histValue) ->
            histValue.equals(resources.getString(R.string.medical_history_yes), ignoreCase = true)
                    && !status.onMedication
        }


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
    fun convertJsonToMedicalHistory(medicalHistoryJson: String?): MedicalHistory? {
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

           /* // Merge hypertension into bp if bp is empty
            if (history != null && (history.bp.isNullOrBlank() || history.bp.trim() == "-") && !history.hypertension.isNullOrBlank()) {
                history.bp = history.hypertension
            }
*/
            history // return the object
        } catch (e: Exception) {
            Log.e("MedicalHistoryParser", "Failed to parse: ${e.message}")
            null
        }
    }
    private fun isHwForComplaintPresent(value: String?): Boolean {
        if (value.isNullOrBlank() || value.trim() == "-") return false
        val trimmedValue = value.trim()
        return trimmedValue.equals(yesString, true) || trimmedValue.equals(noString, true)
    }
    private fun isReasonForNoMedicationPresent(value: String?): Boolean {
        if (value.isNullOrBlank() || value.trim() == "-") return false
        val trimmedValue = value.trim()
        return trimmedValue.equals(yesString, true) || trimmedValue.equals(noString, true)
    }
}

package org.intelehealth.ncd.utils

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intelehealth.ncd.R
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.MedicalHistory
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes

class CategorySegregationUtils(private val resources: Resources) {

    fun segregateAndFetchData(
        patientList: MutableList<Patient>,
        patientAttributeList: MutableList<PatientAttributes>,
        category: String
    ): MutableList<Patient> {
        when (category) {

            Constants.ANEMIA_SCREENING -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfAnemiaPresent(attribute.value)) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.DIABETES_SCREENING -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfDiabetes(attribute.value)) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.HYPERTENSION_SCREENING -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfHypertensionPresent(attribute.value)) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.HYPERTENSION_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfHypertensionPresent(attribute.value)) {
                    removePatientsFromList(patientList, attribute)
                }
            }
        }

        return patientList
    }

    private fun isHistoryOfAnemiaPresent(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].anaemia == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isHistoryOfHypertensionPresent(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].hypertension == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isHistoryOfDiabetes(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].diabetes == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun removePatientsFromList(
        patientList: MutableList<Patient>,
        attributes: PatientAttributes
    ): List<Patient> {
        val iterator = patientList.iterator();
        while (iterator.hasNext()) {
            val patient = iterator.next()
            if (patient.uuid == attributes.patientUuid) {
                iterator.remove()
            }
        }
        return patientList
    }

    private fun convertJsonToList(medicalHistoryJson: String?): List<MedicalHistory> {
        medicalHistoryJson?.let {
            return Gson().fromJson(
                medicalHistoryJson,
                object : TypeToken<List<MedicalHistory>>() {}.type
            )
        }
        return emptyList()
    }
}
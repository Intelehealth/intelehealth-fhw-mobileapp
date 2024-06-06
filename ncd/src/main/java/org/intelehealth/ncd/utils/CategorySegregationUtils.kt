package org.intelehealth.ncd.utils

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intelehealth.ncd.R
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.MedicalHistory
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientWithAttribute

class CategorySegregationUtils(private val resources: Resources) {

    fun segregateAndFetchData(
        patientList: MutableList<Patient>,
        patientAttributeList: MutableList<PatientAttributes>,
        category: String
    ): MutableList<Patient> {
        when (category) {

            Constants.ANEMIA_SCREENING -> patientAttributeList.forEach { attribute ->
                if (isHistoryOfAnemiaPresent(attribute.value) && (isCurrentlyTakingAnemiaMedication(
                        attribute.value
                    ) || isThereAFollowUpWithAnemiaPHC(attribute.value))
                ) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.ANEMIA_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfAnemiaPresent(attribute.value) || !isCurrentlyTakingAnemiaMedication(
                        attribute.value
                    ) && !isThereAFollowUpWithAnemiaPHC(attribute.value)
                ) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.DIABETES_SCREENING -> patientAttributeList.forEach { attribute ->
                if (isHistoryOfDiabetesPresent(attribute.value) && (isCurrentlyTakingDiabetesMedication(
                        attribute.value
                    ) || isThereAFollowUpWithDiabetesPHC(attribute.value))
                ) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.DIABETES_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfDiabetesPresent(attribute.value) || !isCurrentlyTakingDiabetesMedication(
                        attribute.value
                    ) && !isThereAFollowUpWithDiabetesPHC(attribute.value)
                ) {
                    removePatientsFromList(patientList, attribute)

                }
            }

            Constants.HYPERTENSION_SCREENING -> patientAttributeList.forEach { attribute ->
                if (isHistoryOfHypertensionPresent(attribute.value) && (isCurrentlyTakingHypertensionMedication(
                        attribute.value
                    ) || isThereAFollowUpWithHypertensionPHC(attribute.value))
                ) {
                    removePatientsFromList(patientList, attribute)

                }
            }

            Constants.HYPERTENSION_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfHypertensionPresent(attribute.value) || !isCurrentlyTakingHypertensionMedication(
                        attribute.value
                    ) && !isThereAFollowUpWithHypertensionPHC(attribute.value)
                ) {
                    removePatientsFromList(patientList, attribute)
                }
            }
        }

        return patientList
    }

    fun populatePatientDiseaseAttributes(patientList: MutableList<PatientWithAttribute>): List<PatientWithAttribute> {
        patientList.forEach {
            val patientAge: Int = DateAndTimeUtils.calculateAgeInYears(it.dateOfBirth)
            val diseaseList: List<String> =
                populateDiseaseListBasedOnAgeAndHistory(patientAge, it.value)
            it.attributeList = diseaseList.toMutableList()
        }
        return patientList
    }

    fun populateDiseaseListBasedOnAgeAndHistory(
        patientAge: Int,
        medicalHistoryJson: String?
    ): List<String> {
        val diseaseList: MutableList<String> = mutableListOf()

        // For general patients
        if (patientAge < Constants.GENERAL_EXCLUSION_AGE) {
            diseaseList.add(resources.getString(R.string.tab_general))
            return diseaseList
        }

        if (patientAge >= Constants.ANEMIA_EXCLUSION_AGE) {
            if (!isHistoryOfAnemiaPresent(medicalHistoryJson) || !isCurrentlyTakingAnemiaMedication(medicalHistoryJson) && !isThereAFollowUpWithAnemiaPHC(medicalHistoryJson)) {
                diseaseList.add(resources.getString(R.string.tab_anemia_screening))
            }

            if (isHistoryOfAnemiaPresent(medicalHistoryJson) && (isCurrentlyTakingAnemiaMedication(medicalHistoryJson) || isThereAFollowUpWithAnemiaPHC(medicalHistoryJson))) {
                diseaseList.add(resources.getString(R.string.tab_anemia_follow_up))
            }
        }

        if (patientAge >= Constants.HYPERTENSION_EXCLUSION_AGE) {
            if (!isHistoryOfHypertensionPresent(medicalHistoryJson) || !isCurrentlyTakingHypertensionMedication(medicalHistoryJson) && !isThereAFollowUpWithHypertensionPHC(medicalHistoryJson)) {
                diseaseList.add(resources.getString(R.string.tab_hypertension_screening))
            }

            if (isHistoryOfHypertensionPresent(medicalHistoryJson) && (isCurrentlyTakingHypertensionMedication(medicalHistoryJson) || isThereAFollowUpWithHypertensionPHC(medicalHistoryJson))) {
                diseaseList.add(resources.getString(R.string.tab_hypertension_follow_up))
            }
        }

        if (patientAge >= Constants.DIABETES_EXCLUSION_AGE) {
            if (!isHistoryOfDiabetesPresent(medicalHistoryJson) || !isCurrentlyTakingDiabetesMedication(medicalHistoryJson) && !isCurrentlyTakingDiabetesMedication(medicalHistoryJson)) {
                diseaseList.add(resources.getString(R.string.tab_diabetes_screening))
            }

            if (isHistoryOfDiabetesPresent(medicalHistoryJson) && (isCurrentlyTakingDiabetesMedication(medicalHistoryJson) || isThereAFollowUpWithDiabetesPHC(medicalHistoryJson))) {
                diseaseList.add(resources.getString(R.string.tab_diabetes_follow_up))
            }
        }

        return diseaseList
    }

    private fun isHistoryOfAnemiaPresent(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].anaemia == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isCurrentlyTakingAnemiaMedication(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].medicationForAnemia == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isThereAFollowUpWithAnemiaPHC(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].healthWorkerForAnemia == resources.getString(R.string.medical_history_yes)
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

    private fun isCurrentlyTakingHypertensionMedication(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].medicationForBP == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isThereAFollowUpWithHypertensionPHC(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].healthWorkerForBP == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isHistoryOfDiabetesPresent(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].diabetes == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isCurrentlyTakingDiabetesMedication(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].medicationForDiabetes == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isThereAFollowUpWithDiabetesPHC(medicalHistoryJson: String?): Boolean {
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].healthWorkerForDiabetes == resources.getString(R.string.medical_history_yes)
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
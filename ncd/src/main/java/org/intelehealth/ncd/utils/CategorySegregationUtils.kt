package org.intelehealth.ncd.utils

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import org.intelehealth.ncd.R
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryDataSource
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.model.MedicalHistory
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.model.PatientWithAttribute
import org.intelehealth.ncd.room.CategoryDatabase
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.room.dao.VisitDao

class CategorySegregationUtils(private val resources: Resources) {
    private  val TAG = "CategorySegregationUtil"
    fun segregateAndFetchData(
        patientList: MutableList<Patient>,
        patientAttributeList: MutableList<PatientAttributes>,
        category: String
    ): MutableList<Patient> {
        Log.d("HypertensionDebug", "Full Patient Attribute List:\n${patientAttributeList.joinToString("\n")}")
        Log.d("HypertensionDebug", "Full Patient Attribute List size:\n${patientAttributeList.size}")

        when (category) {

            Constants.ANEMIA_SCREENING -> patientAttributeList.forEach { attribute ->
                // check attribute it self null
                if (attribute == null) {
                    return@forEach
                }
                if (attribute.value == null) {
                    return@forEach
                }
                if (isHistoryOfAnemiaPresent(attribute.value) && (isCurrentlyTakingAnemiaMedication(attribute.value) || isThereAFollowUpWithAnemiaPHC(attribute.value))
                ) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.ANEMIA_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfAnemiaPresent(attribute.value)) {
                    removePatientsFromList(patientList, attribute)
                }
            }


          /*  Constants.ANEMIA_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfAnemiaPresent(attribute.value) || !isCurrentlyTakingAnemiaMedication(
                        attribute.value
                    ) && !isThereAFollowUpWithAnemiaPHC(attribute.value)
                ) {
                    removePatientsFromList(patientList, attribute)
                }
            }*/

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

           /* Constants.HYPERTENSION_SCREENING -> patientAttributeList.forEach { attribute ->
                if (isHistoryOfHypertensionPresent(attribute.value) && (isCurrentlyTakingHypertensionMedication(
                        attribute.value
                    ) || isThereAFollowUpWithHypertensionPHC(attribute.value))
                ) {
                    removePatientsFromList(patientList, attribute)

                }
            }*/
            Constants.HYPERTENSION_SCREENING -> patientAttributeList.forEach { attribute ->
                //1.  History of hypertension - no  ||  2. History of hypertension - yes and currently taking medication  - no -but logic is of remove the entry from list
                val hasHypertensionHistory = isHistoryOfHypertensionPresent(attribute.value)
                val currentlyOnMedication = isCurrentlyTakingHypertensionMedication(attribute.value)

                val includePatient = when {
                    !hasHypertensionHistory -> true                 // No history → include
                    hasHypertensionHistory && !currentlyOnMedication -> true // History but not on medication → include
                    else -> false                                   // History and on medication → exclude
                }
                // Remove if inclusion criteria NOT met
                if (!includePatient) {
                    removePatientsFromList(patientList, attribute)
                }
            }

            Constants.HYPERTENSION_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                val hasHypertensionHistory = isHistoryOfHypertensionPresent(attribute.value)
                val currentlyOnMedication = isCurrentlyTakingHypertensionMedication(attribute.value)

                // Inclusion criteria flags
                val includePatient =  hasHypertensionHistory && currentlyOnMedication


                // Remove if patient does not meet inclusion
                if (!includePatient) {
                    removePatientsFromList(patientList, attribute)
                }
            }

         /*   Constants.HYPERTENSION_FOLLOW_UP -> patientAttributeList.forEach { attribute ->
                if (!isHistoryOfHypertensionPresent(attribute.value) || !isCurrentlyTakingHypertensionMedication(
                        attribute.value
                    ) && !isThereAFollowUpWithHypertensionPHC(attribute.value)
                ) {
                    removePatientsFromList(patientList, attribute)
                }
            }*/
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
            if (!isHistoryOfAnemiaPresent(medicalHistoryJson) || !isCurrentlyTakingAnemiaMedication(
                    medicalHistoryJson
                ) && !isThereAFollowUpWithAnemiaPHC(medicalHistoryJson)
            ) {
                diseaseList.add(resources.getString(R.string.tab_anemia_screening))
            }

            if (isHistoryOfAnemiaPresent(medicalHistoryJson) && (isCurrentlyTakingAnemiaMedication(
                    medicalHistoryJson
                ) || isThereAFollowUpWithAnemiaPHC(medicalHistoryJson))
            ) {
                diseaseList.add(resources.getString(R.string.tab_anemia_follow_up))
            }
        }

        if (patientAge >= Constants.HYPERTENSION_EXCLUSION_AGE) {
            if (!isHistoryOfHypertensionPresent(medicalHistoryJson) || !isCurrentlyTakingHypertensionMedication(
                    medicalHistoryJson
                ) && !isThereAFollowUpWithHypertensionPHC(medicalHistoryJson)
            ) {
                diseaseList.add(resources.getString(R.string.tab_hypertension_screening))
            }

            if (isHistoryOfHypertensionPresent(medicalHistoryJson) && (isCurrentlyTakingHypertensionMedication(
                    medicalHistoryJson
                ) || isThereAFollowUpWithHypertensionPHC(medicalHistoryJson))
            ) {
                diseaseList.add(resources.getString(R.string.tab_hypertension_follow_up))
            }
        }

        if (patientAge >= Constants.DIABETES_EXCLUSION_AGE) {
            if (!isHistoryOfDiabetesPresent(medicalHistoryJson) || !isCurrentlyTakingDiabetesMedication(
                    medicalHistoryJson
                ) && !isCurrentlyTakingDiabetesMedication(medicalHistoryJson)
            ) {
                diseaseList.add(resources.getString(R.string.tab_diabetes_screening))
            }

            if (isHistoryOfDiabetesPresent(medicalHistoryJson) && (isCurrentlyTakingDiabetesMedication(
                    medicalHistoryJson
                ) || isThereAFollowUpWithDiabetesPHC(medicalHistoryJson))
            ) {
                diseaseList.add(resources.getString(R.string.tab_diabetes_follow_up))
            }
        }

        return diseaseList
    }

    private fun isHistoryOfAnemiaPresent(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].anemia == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isCurrentlyTakingAnemiaMedication(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].medicationForAnemia == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isThereAFollowUpWithAnemiaPHC(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].healthWorkerForAnemia == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isHistoryOfHypertensionPresent(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].bp == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isCurrentlyTakingHypertensionMedication(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].medicationForBP == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isThereAFollowUpWithHypertensionPHC(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].healthWorkerForBP == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isHistoryOfDiabetesPresent(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].diabetes == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isCurrentlyTakingDiabetesMedication(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
        val medicalHistoryList: List<MedicalHistory> = convertJsonToList(medicalHistoryJson)
        return if (medicalHistoryList.isEmpty()) {
            false
        } else {
            medicalHistoryList[0].medicationForDiabetes == resources.getString(R.string.medical_history_yes)
        }
    }

    private fun isThereAFollowUpWithDiabetesPHC(medicalHistoryJson: String?): Boolean {
        if (medicalHistoryJson.isNullOrEmpty()) {
            return false
        }
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

    /*private fun convertJsonToList(medicalHistoryJson: String?): List<MedicalHistory> {
        medicalHistoryJson?.let {
            return Gson().fromJson(
                medicalHistoryJson,
                object : TypeToken<List<MedicalHistory>>() {}.type
            )
        }
        return emptyList()
    }*/
    //due to crash changed this method
    private fun convertJsonToList(medicalHistoryJson: String?): List<MedicalHistory> {
        if (medicalHistoryJson.isNullOrBlank()) return emptyList()

        return try {
            val gson = Gson()
            val jsonElement = JsonParser().parse(medicalHistoryJson)
            when {
                jsonElement.isJsonArray -> {
                    gson.fromJson(jsonElement, object : TypeToken<List<MedicalHistory>>() {}.type)
                }
                jsonElement.isJsonObject -> {
                    listOf(gson.fromJson(jsonElement, MedicalHistory::class.java))
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.e("MedicalHistoryParser", "Failed to parse: ${e.message}")
            emptyList()
        }
    }

     fun getEligibleMMsForPatients(patientVisitDetailsList: List<PatientVisitDetails>): Map<String, Any> {
        val mmCategories = listOf(
            Constants.HYPERTENSION_SCREENING,
            Constants.HYPERTENSION_FOLLOW_UP,
            Constants.ANEMIA_SCREENING,
            Constants.ANEMIA_FOLLOW_UP,
            Constants.DIABETES_SCREENING
        )
        val eligibleMms = mutableListOf<String>()
        val patientId = patientVisitDetailsList.firstOrNull()?.patientId ?: ""
         Log.d(TAG, "kk getEligibleMMsForPatients: patientVisitDetailsList : ${patientVisitDetailsList}")

         Log.d(TAG, "kk getEligibleMMsForPatients: patientId : "+patientId)
        for (category in mmCategories) {
            val eligiblePatients = segregateAndFetchPatientVisitDetails(patientVisitDetailsList, category)
            Log.d(TAG, "kk getEligibleMMsForPatients: eligiblePatients : "+eligiblePatients)

            if (eligiblePatients.isNotEmpty()) {
                eligibleMms.add(category)
            }
        }
        return mapOf(
            "patient_id" to patientId,
            "eligible_mms" to eligibleMms
        )
    }

     private suspend fun checkForAllEligibleProtocols(patientUuid: String, context: Context): Map<String, Any> {
        val database = CategoryDatabase.getInstance(context)

        val patientDao: PatientDao = database.patientDao()
        val patientAttributeDao: PatientAttributeDao = database.patientAttributeDao()
        val visitsDao: VisitDao = database.visitDao()

        val dataSource = CategoryDataSource(patientDao, patientAttributeDao, visitsDao)
        val repository = CategoryRepository(dataSource)
        val utils = CategorySegregationUtils(resources)

        val result = repository.getPatientVisitDetailsForFollowup(
            age = Constants.HYPERTENSION_EXCLUSION_AGE,
            attributeTypeUuid = Constants.OTHER_MEDICAL_HISTORY,
            visitNoteEncounterUuid = Constants.ENCOUNTER_VISIT_COMPLETE,
            patientUuid
        )
         return utils.getEligibleMMsForPatients(patientVisitDetailsList = result)
    }

    fun segregateAndFetchPatientVisitDetails(
        patientVisitDetailsList: List<PatientVisitDetails>,
        category: String
    ): List<PatientVisitDetails> {
        Log.d(TAG, "testmulti segregateAndFetchPatientVisitDetails: patientVisitDetailsList : "+Gson().toJson(patientVisitDetailsList))
        Log.d(TAG, "testmulti segregateAndFetchPatientVisitDetails: category : "+category)
        val gson = GsonBuilder().serializeNulls().create()
        Log.d("testmulti", "newlist : " +gson.toJson(patientVisitDetailsList))


        patientVisitDetailsList.forEachIndexed { index, item ->
            val json = Gson().toJson(item)
           // logLong(TAG, "testmulti Patient #$index : $json")
        }
        val result = when (category) {
            Constants.HYPERTENSION_SCREENING -> filterHypertensionScreeningPatients(patientVisitDetailsList)
            Constants.HYPERTENSION_FOLLOW_UP -> filterHypertensionFollowUpPatients(patientVisitDetailsList)
            Constants.ANEMIA_SCREENING -> filterAnemiaScreeningPatients(patientVisitDetailsList)
            Constants.ANEMIA_FOLLOW_UP -> filterAnemiaFollowUpPatients(patientVisitDetailsList)

            else -> emptyList()
        }

        return result
    }

    private fun filterHypertensionScreeningPatients(
        patientVisitDetailsList: List<PatientVisitDetails>
    ): List<PatientVisitDetails> {
        return patientVisitDetailsList.filter { detail ->
            val age = detail.age ?: return@filter false
            val followupGiven = detail.isHypertensionFollowupGiven ?: false
            Log.d(TAG, "linelist hyperten screen name : "+detail.firstName + " "+detail.lastName)
            Log.d(TAG, "linelist hyperten screen age : "+detail.openmrsId)
            Log.d(TAG, "linelist hyperten screen patientid : "+detail.patientId)
            Log.d(TAG, "linelist hyperten screen visit id : "+detail.visitId)
            Log.d(TAG, "linelist hyperten screen isHypertensionFollowupGiven : "+followupGiven)
            // Exclude if follow-up is already given
            if (followupGiven) return@filter false

            // Exclude if no attribute value (medical history JSON)
            val medicalHistoryJson = detail.value
            Log.d(TAG, "linelist hyperten screen medicalHistoryJson : "+medicalHistoryJson)
            if (medicalHistoryJson.isNullOrEmpty()) return@filter false

            val hasHistory = isHistoryOfHypertensionPresent(medicalHistoryJson)
            val onMedication = isCurrentlyTakingHypertensionMedication(medicalHistoryJson)

            Log.d(TAG, "linelist hyperten screen hasHistory : "+hasHistory)
            Log.d(TAG, "linelist hyperten screen onMedication : "+onMedication)

            val meetsAgeCriteria = age >= Constants.HYPERTENSION_EXCLUSION_AGE
            Log.d(TAG, "linelist hyperten screen meetsAgeCriteria : "+meetsAgeCriteria)
            Log.d(TAG, "linelist hyperten screen ***************************************************\n")

            meetsAgeCriteria && (!hasHistory || (hasHistory && !onMedication))
        }
    }


    private fun filterHypertensionFollowUpPatients(
        patientVisitDetailsList: List<PatientVisitDetails>
    ): List<PatientVisitDetails> {
        return patientVisitDetailsList.filter { detail ->
            val age = detail.age ?: return@filter false
            val meetsAgeCriteria = age >= Constants.HYPERTENSION_EXCLUSION_AGE

            val followupGiven = detail.isHypertensionFollowupGiven
            Log.d(TAG, "linelist hyperten followup name : "+detail.firstName + " "+detail.lastName)
            Log.d(TAG, "linelist hyperten followup age : "+detail.openmrsId)
            Log.d(TAG, "linelist hyperten followup patientid : "+detail.patientId)
            Log.d(TAG, "linelist hyperten followup visit id : "+detail.visitId)
            Log.d(TAG, "linelist hyperten followup isHypertensionFollowupGiven : "+followupGiven)
            Log.d(TAG, "linelist hyperten followup medicalHistoryJson : "+detail.value)
            Log.d(TAG, "linelist hyperten followup hasHistory : "+isHistoryOfHypertensionPresent(detail.value))
            Log.d(TAG, "linelist hyperten followup onMedication : "+isCurrentlyTakingHypertensionMedication(detail.value))
            Log.d(TAG, "linelist hyperten followup isHypertensionFollowupTodayOrLater : "+detail.isHypertensionFollowupTodayOrLater)
            Log.d(TAG, "linelist hyperten followup ***************************************************\n")

            return@filter when {
                // Case 1: Follow-up not given or null →  check baseline criteria
                followupGiven != true -> {
                    val medicalHistoryJson = detail.value
                    if (medicalHistoryJson.isNullOrEmpty()) return@filter false
                    val hasHistory = isHistoryOfHypertensionPresent(detail.value)
                    val onMedication = isCurrentlyTakingHypertensionMedication(detail.value)
                    meetsAgeCriteria && hasHistory && onMedication
                }

                // Case 2: Follow-up given → check only the protocol flag
                else -> {
                    detail.isHypertensionFollowupTodayOrLater == true
                    /*    val followUpFlag = detail.followUpFromProtocol ?: false
                        followUpFlag*/
                }
            }
        }
    }
    private fun filterAnemiaScreeningPatients(
        patientVisitDetailsList: List<PatientVisitDetails>
    ): List<PatientVisitDetails> {
        return patientVisitDetailsList.filter { detail ->
            val age = detail.age ?: return@filter false
            val followupGiven = detail.isAnemiaFollowupGiven ?: false
            Log.d(TAG, "linelist Anemia screen name : "+detail.firstName + " "+detail.lastName)
            Log.d(TAG, "linelist Anemia screen age : "+detail.openmrsId)
            Log.d(TAG, "linelist Anemia screen patientid : "+detail.patientId)
            Log.d(TAG, "linelist Anemia screen visit id : "+detail.visitId)
            Log.d(TAG, "linelist Anemia screen isAnemiaFollowupGiven : "+followupGiven)
            // Exclude if follow-up is already given
            if (followupGiven) return@filter false

            // Exclude if no attribute value (medical history JSON)
            val medicalHistoryJson = detail.value
            if (medicalHistoryJson.isNullOrEmpty()) return@filter false
            Log.d(TAG, "linelist Anemia screen medicalHistoryJson : "+medicalHistoryJson)

            val hasHistory = isHistoryOfAnemiaPresent(medicalHistoryJson)
            val onMedication = isCurrentlyTakingAnemiaMedication(medicalHistoryJson)

            val meetsAgeCriteria = age > Constants.ANEMIA_EXCLUSION_AGE
            Log.d(TAG, "linelist Anemia screen hasHistory : "+hasHistory)
            Log.d(TAG, "linelist Anemia screen onMedication : "+onMedication)
            Log.d(TAG, "linelist Anemia screen meetsAgeCriteria : "+meetsAgeCriteria)
            Log.d(TAG, "linelist Anemia screen ***************************************************\n")

            meetsAgeCriteria && (!hasHistory || (hasHistory && !onMedication))
        }
    }
    // 🔥 Add this: Wrapper that Java can call
    fun checkForAllEligibleProtocolsBlocking(
        patientUuid: String,
        context: Context
    ): Map<String, Any> = runBlocking {
        checkForAllEligibleProtocols(patientUuid, context)
    }
    private fun filterAnemiaFollowUpPatients(
        patientVisitDetailsList: List<PatientVisitDetails>
    ): List<PatientVisitDetails> {
        return patientVisitDetailsList.filter { detail ->
            val age = detail.age ?: return@filter false
            val meetsAgeCriteria = age >= Constants.ANEMIA_EXCLUSION_AGE

            val followupGiven = detail.isAnemiaFollowupGiven
            Log.d(TAG, "linelist Anemia followup name : "+detail.firstName + " "+detail.lastName)
            Log.d(TAG, "linelist Anemia followup age : "+detail.openmrsId)
            Log.d(TAG, "linelist Anemia followup patientid : "+detail.patientId)
            Log.d(TAG, "linelist Anemia followup visit id : "+detail.visitId)
            Log.d(TAG, "linelist Anemia followup isAnemiaFollowupGiven : "+followupGiven)
            Log.d(TAG, "linelist Anemia followup medicalHistoryJson : "+detail.value)
            Log.d(TAG, "linelist Anemia followup hasHistory : "+isHistoryOfAnemiaPresent(detail.value))
            Log.d(TAG, "linelist Anemia followup onMedication : "+isCurrentlyTakingAnemiaMedication(detail.value))
            Log.d(TAG, "linelist Anemia followup isAnemiaFollowupTodayOrLater : "+detail.isAnemiaFollowupTodayOrLater)
            Log.d(TAG, "linelist Anemia followup ***************************************************\n")

            return@filter when {
                // Case 1: Follow-up not given or null →  check baseline criteria
                followupGiven != true -> {
                    val medicalHistoryJson = detail.value
                    if (medicalHistoryJson.isNullOrEmpty()) return@filter false
                    val hasHistory = isHistoryOfAnemiaPresent(detail.value)
                    val onMedication = isCurrentlyTakingAnemiaMedication(detail.value)
                    meetsAgeCriteria && hasHistory && onMedication
                }

                // Case 2: Follow-up given → check only the protocol flag
                else -> {
                    detail.isAnemiaFollowupTodayOrLater == true
                    /*    val followUpFlag = detail.followUpFromProtocol ?: false
                        followUpFlag*/
                }
            }
        }
    }

}
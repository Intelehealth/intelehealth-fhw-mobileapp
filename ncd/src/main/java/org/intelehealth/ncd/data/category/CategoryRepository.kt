package org.intelehealth.ncd.data.category

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.pagination.PatientVisitPagingSource
import org.intelehealth.ncd.utils.DateAndTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CategoryRepository(private val dataSource: CategoryDataSource) {

    suspend fun getPatientsBasedOnAge(age: Int): List<Patient> =
        dataSource.getPatientsBasedOnAge(age)

    suspend fun getPatientsBelowAge(age: Int): List<Patient> = dataSource.getPatientsBelowAge(age)

    suspend fun getPatientsBasedOnUuids(
        patientsList: List<Patient>,
        attributeUuid: String
    ): List<PatientAttributes> {
        val resultList: MutableList<PatientAttributes> = mutableListOf()
        patientsList.forEach {
            resultList.add(dataSource.getPatientsBasedOnAttributesUuids(it.uuid.toString(), attributeUuid))
        }
        return resultList
    }
    suspend fun getStartVisitNoteEncounterByVisitUUID(visitUuid: String, encounterTypeUuid: String):String = dataSource.getStartVisitNoteEncounterByVisitUUID(visitUuid, encounterTypeUuid)
    suspend fun getPatientVisitDetails(age: Int, attributeTypeUuid: String, visitNoteEncounterUuid: String): List<PatientVisitDetails> {
        val rawDataList = dataSource.getPatientVisitRawData(age, attributeTypeUuid, visitNoteEncounterUuid)
        val result = buildPatientVisitDetails(rawDataList)
        result.forEachIndexed { index, item ->
        }
        return result
    }
    private fun buildPatientVisitDetails(
        rawDataList: List<PatientVisitDetails>
    ): List<PatientVisitDetails> {
        return rawDataList.map { data ->
            val formattedStartDate = data.startDate?.let {
                DateAndTimeUtils.formatStartVisitDate(
                    it,
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                    "dd MMM 'at' hh:mm a"
                )
            }

            val isFollowupFromObs = data.chiefComplaintData?.let {
                checkFollowUpFlag(it)}
            val isFollowUpGivenToPatient = data.chiefComplaintData?.let { checkIfFollowupDateGivenToPatient(it)}
            // Check follow-up flags
            var isHypertensionFollowupGiven: Boolean? = null
            var isAnemiaFollowupGiven: Boolean? = null
            var isDiabetesFollowupGiven: Boolean? = null

            data.chiefComplaintData?.let { complaintData ->
                val tempModel = PatientVisitDetails()
                setFollowUpFlags(complaintData, tempModel, data)
                isHypertensionFollowupGiven = tempModel.isHypertensionFollowupGiven
                isAnemiaFollowupGiven = tempModel.isAnemiaFollowupGiven
                isDiabetesFollowupGiven = tempModel.isDiabetesFollowupGiven
            }

            PatientVisitDetails(
                patientId = data.patientId,
                visitId = data.visitId,
                firstName = data.firstName,
                middleName = data.middleName,
                lastName = data.lastName,
                dateOfBirth = data.dateOfBirth,
                gender = data.gender,
                openmrsId = data.openmrsId,
                value = data.value,
                personAttributeTypeUuid = data.personAttributeTypeUuid,
                startDate = formattedStartDate,
                patientPhoto = data.patientPhoto,
                isPrescriptionExist = data.isPrescriptionExist,
                isNcdVisit = data.isNcdVisit,
                chiefComplaintData = data.chiefComplaintData,
                followUpFromProtocol = isFollowupFromObs,
                visitEndDate = data.visitEndDate,
                isFollowUpDateGivenToPatient = isFollowUpGivenToPatient,
                isHypertensionFollowupGiven = isHypertensionFollowupGiven,
                isAnemiaFollowupGiven = isAnemiaFollowupGiven,
                isDiabetesFollowupGiven = isDiabetesFollowupGiven
            )
        }
    }

    suspend fun getPatientVisitDetailsBelowAgeForGeneral(visitNoteEncounterUuid: String): List<PatientVisitDetails> {
        val rawDataList = dataSource.getPatientVisitRawDataBelowAgeForGeneral(visitNoteEncounterUuid)

        val result = buildPatientVisitDetailsForGeneral(rawDataList)

        return result
    }
    private fun buildPatientVisitDetailsForGeneral(
        rawDataList: List<PatientVisitDetails>
    ): List<PatientVisitDetails> {
        return rawDataList.map { data ->
            val formattedStartDate = data.startDate?.let {
                DateAndTimeUtils.formatStartVisitDate(it, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM 'at' hh:mm a") }

            PatientVisitDetails(
                patientId = data.patientId,
                visitId = data.visitId,
                firstName = data.firstName,
                middleName = data.middleName,
                lastName = data.lastName,
                dateOfBirth = data.dateOfBirth,
                gender = data.gender,
                openmrsId = data.openmrsId,
                value = data.value,
                personAttributeTypeUuid = data.personAttributeTypeUuid,
                startDate = formattedStartDate,
                patientPhoto = data.patientPhoto,
                isPrescriptionExist = data.isPrescriptionExist,
            )
        }
    }
    suspend fun getPatientVisitDetailsForFollowup(age: Int, attributeTypeUuid: String, visitNoteEncounterUuid: String, patientUuid: String): List<PatientVisitDetails> {
        val rawDataList = dataSource.getPatientVisitRawDataForFollowup(age, attributeTypeUuid, visitNoteEncounterUuid, patientUuid)
        rawDataList.forEach {
        }
        val result = buildPatientVisitDetails(rawDataList)
        result.forEachIndexed { index, item ->
        }
        return result
    }

    private fun checkFollowUpFlag(chiefComplaintData: String?): Boolean {
        if (chiefComplaintData.isNullOrBlank()) return false

        return try {
            // 1. Extract the date after "Next Follow Up Date - "
            val regex = "Next Follow Up Date - ([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4})".toRegex()
            val match = regex.find(chiefComplaintData) ?: return false
            val dateStr = match.groupValues[1] // e.g., "30/Oct/2025"
            val inputFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH)
            val followUpDate = inputFormat.parse(dateStr) ?: return false

            // 3. Check if today's date is equal to or after follow-up date
            val today = Calendar.getInstance().time
            !today.before(followUpDate) // true if today >= follow-up date, false otherwise
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun checkIfFollowupDateGivenToPatient(chiefComplaintData: String?): Boolean {
        if (chiefComplaintData.isNullOrBlank()) return false
        val regex = "Next Follow Up Date -\\s*(.+)".toRegex()
        val match = regex.find(chiefComplaintData)
        val dateStr = match?.groupValues?.getOrNull(1)
        return !dateStr.isNullOrBlank()
    }


   fun getPagedPatients(visitEncounterNoteAttr: String, query: String): Flow<PagingData<PatientVisitDetails>> {
       return Pager(
           config = PagingConfig(
               pageSize = 5,
               enablePlaceholders = false
           ),
           pagingSourceFactory = { PatientVisitPagingSource(dataSource, visitEncounterNoteAttr,query) }
       ).flow
   }

    private fun setFollowUpFlags(
        chiefComplaintData: String?,
        model: PatientVisitDetails,
        data: PatientVisitDetails
    ) {
        if (chiefComplaintData.isNullOrBlank()) return

        // 1. Extract the complaint name from <b>...</b>
        val complaintRegex = "<b>(.*?)</b>".toRegex()
        val complaintMatch = complaintRegex.find(chiefComplaintData)
        val complaintNameRaw = complaintMatch?.groupValues?.getOrNull(1)?.trim() ?: return
        val complaintName = complaintNameRaw
            .lowercase()
            .replace("followup", "follow_up")
            .replace("[^a-z0-9]+".toRegex(), "_")
            .trim('_')

        // 2. Check if a "Next Follow Up Date" exists
        val followUpRegex = "Next Follow Up Date -\\s*(.+)".toRegex()
        val match = followUpRegex.find(chiefComplaintData)
        val isFollowUpGiven = !match?.groupValues?.getOrNull(1).isNullOrBlank()

        // 3. Set the appropriate flag using constants
        when (complaintName) {
            Constants.HYPERTENSION_SCREENING, Constants.HYPERTENSION_FOLLOW_UP -> model.isHypertensionFollowupGiven = isFollowUpGiven
            Constants.ANEMIA_SCREENING, Constants.ANEMIA_FOLLOW_UP -> model.isAnemiaFollowupGiven = isFollowUpGiven
            Constants.DIABETES_SCREENING, Constants.DIABETES_FOLLOW_UP -> model.isDiabetesFollowupGiven = isFollowUpGiven
        }
    }

}
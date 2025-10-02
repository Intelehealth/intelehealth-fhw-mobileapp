package org.intelehealth.ncd.data.category

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.model.Visit
import org.intelehealth.ncd.room.dao.VisitDao
import org.intelehealth.ncd.utils.DateAndTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
        rawDataList.forEach {
        }
        Log.d("TAG", "getPatientVisitDetails: attributeTypeUuid : $attributeTypeUuid")
        Log.d("TAG", "getPatientVisitDetails: visitNoteEncounterUuid : $visitNoteEncounterUuid")

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
            Log.d("TAG", "buildPatientVisitDetailsForGeneral: isFollowupFromObs : "+isFollowupFromObs)

            Log.d("TAG", "buildPatientVisitDetailsForGeneral: data.chiefComplaintData : "+data.chiefComplaintData)


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
                visitSpeciality = data.visitSpeciality
            )
        }
    }

    suspend fun getPatientVisitDetailsBelowAgeForGeneral(
        age: Int,
        visitNoteEncounterUuid: String
    ): List<PatientVisitDetails> {
        val rawDataList = dataSource.getPatientVisitRawDataBelowAgeForGeneral(age, visitNoteEncounterUuid)

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
    suspend fun getPatientVisitDetailsForFollowup(age: Int, attributeTypeUuid: String, visitNoteEncounterUuid: String): List<PatientVisitDetails> {
        val rawDataList = dataSource.getPatientVisitRawDataForFollowup(age, attributeTypeUuid, visitNoteEncounterUuid)
        rawDataList.forEach {
        }
        Log.d("TAG", "getPatientVisitDetailsForFollowup: attributeTypeUuid : $attributeTypeUuid")
        Log.d("TAG", "getPatientVisitDetailsForFollowup: visitNoteEncounterUuid : $visitNoteEncounterUuid")

        val result = buildPatientVisitDetails(rawDataList)
        result.forEachIndexed { index, item ->
        }
        return result
    }

    fun checkFollowUpFlag(chiefComplaintData: String?): Boolean {
        if (chiefComplaintData.isNullOrBlank()) return false

        return try {
            // 1. Extract the date after "Next Follow Up Date - "
            val regex = "Next Follow Up Date - ([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4})".toRegex()
            val match = regex.find(chiefComplaintData) ?: return false
            val dateStr = match.groupValues[1] // e.g., "30/Oct/2025"
            // 2. Parse the date in the format dd/MMM/yyyy
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


}
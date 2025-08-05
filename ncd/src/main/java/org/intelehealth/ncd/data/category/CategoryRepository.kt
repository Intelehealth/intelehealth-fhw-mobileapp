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
                isPrescriptionExist = data.isPrescriptionExist == true
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
                isPrescriptionExist = data.isPrescriptionExist == true
            )
        }
    }


}
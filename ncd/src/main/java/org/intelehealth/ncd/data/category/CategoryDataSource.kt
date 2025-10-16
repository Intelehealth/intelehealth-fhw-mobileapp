package org.intelehealth.ncd.data.category

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.model.Visit
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.room.dao.VisitDao

class CategoryDataSource(
    private val patientDao: PatientDao,
    private val patientAttributeDao: PatientAttributeDao,
    private val visitDao: VisitDao

) {

    suspend fun getPatientsBasedOnAge(age: Int): List<Patient> =
        patientDao.getPatientsBasedOnAge(age)

    suspend fun getPatientsBelowAge(age: Int): List<Patient> = patientDao.getPatientsBelowAge(age)

    suspend fun getPatientsBasedOnAttributesUuids(
        patientUuid: String,
        attributeUuid: String
    ): PatientAttributes =
        patientAttributeDao.getPatientsBasedOnAttributeUuids(patientUuid, attributeUuid)

    suspend fun getStartVisitNoteEncounterByVisitUUID(visitUuid: String, encounterTypeUuid: String): String = visitDao.getStartVisitNoteEncounterByVisitUUID(visitUuid, encounterTypeUuid)
    suspend fun getPatientVisitRawData(age: Int,  attributeTypeUuid: String, visitNoteEncounterUuid: String): List<PatientVisitDetails> = visitDao.getPatientVisitRawData(age, attributeTypeUuid,visitNoteEncounterUuid)
    suspend fun getPatientVisitRawDataBelowAgeForGeneral(visitNoteEncounterUuid: String): List<PatientVisitDetails> = visitDao.getPatientVisitRawDataGeneral(visitNoteEncounterUuid)
    suspend fun getPatientVisitRawDataForFollowup(age: Int,  attributeTypeUuid: String, visitNoteEncounterUuid: String): List<PatientVisitDetails> = visitDao.getPatientVisitRawDataForFollowup(age, attributeTypeUuid,visitNoteEncounterUuid)
    fun getPatientVisitRawDataBelowAgeForGeneralNew(visitNoteEncounterUuid: String): PagingSource<Int, PatientVisitDetails> = visitDao.getPatientVisitPagingSource(visitNoteEncounterUuid)

}
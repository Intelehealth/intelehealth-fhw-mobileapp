package org.intelehealth.ncd.data

import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao

class SearchDataSource(
    private val patientDao: PatientDao,
    private val patientAttributeDao: PatientAttributeDao
) {

    suspend fun getPatientsBasedOnAge(age: Int): List<Patient> =
        patientDao.getPatientsBasedOnAge(age)

    suspend fun getPatientsBelowAge(age: Int): List<Patient> = patientDao.getPatientsBelowAge(age)

    suspend fun getPatientsBasedOnAttributesUuids(
        patientUuid: String,
        attributeUuid: String
    ): PatientAttributes =
        patientAttributeDao.getPatientsBasedOnAttributeUuids(patientUuid, attributeUuid)

}
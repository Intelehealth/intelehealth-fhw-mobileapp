package org.intelehealth.ncd.data.category

import android.util.Log
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao

class CategoryDataSource(
    private val patientDao: PatientDao,
    private val patientAttributeDao: PatientAttributeDao
) {

    suspend fun getPatientsBasedOnAge(age: Int): List<Patient> =
        patientDao.getPatientsBasedOnAge(age)

    suspend fun getPatientsBelowAge(age: Int): List<Patient> = patientDao.getPatientsBelowAge(age)

    suspend fun getPatientsBasedOnAttributesUuids(
        patientUuid: String,
        attributeUuid: String
    ): PatientAttributes {
        Log.d("PatientRepo", "getPatientsBasedOnAttributesUuids called with patientUuid=$patientUuid, attributeUuid=$attributeUuid")

        return patientAttributeDao.getPatientsBasedOnAttributeUuids(patientUuid, attributeUuid)
    }

}
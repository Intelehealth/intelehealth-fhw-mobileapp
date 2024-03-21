package org.intelehealth.ncd.data

import org.intelehealth.ncd.model.Patient

class SearchRepository(private val dataSource: SearchDataSource) {

    suspend fun getPatientsBasedOnAge(age: Int): List<Patient> = dataSource.getPatientsBasedOnAge(age)

    suspend fun getPatientsBasedOnUuids(
        patientUuid: String,
        attributeUuid: String
    ) = dataSource.getPatientsBasedOnAttributesUuids(patientUuid, attributeUuid)

}
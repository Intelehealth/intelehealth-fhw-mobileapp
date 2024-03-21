package org.intelehealth.ncd.data

import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes

class SearchRepository(private val dataSource: SearchDataSource) {

    suspend fun getPatientsBasedOnAge(age: Int): List<Patient> =
        dataSource.getPatientsBasedOnAge(age)

    suspend fun getAllPatients(): List<Patient> = dataSource.getAllPatients()

    suspend fun getPatientsBasedOnUuids(
        patientsList: List<Patient>,
        attributeUuid: String
    ): List<PatientAttributes> {
        val resultList: MutableList<PatientAttributes> = mutableListOf()
        patientsList.forEach {
            resultList.add(dataSource.getPatientsBasedOnAttributesUuids(it.uuid, attributeUuid))
        }
        return resultList
    }
}
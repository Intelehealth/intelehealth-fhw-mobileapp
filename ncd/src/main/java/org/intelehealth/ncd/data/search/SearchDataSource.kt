package org.intelehealth.ncd.data.search

import org.intelehealth.ncd.model.PatientWithAttribute
import org.intelehealth.ncd.room.dao.PatientDao

class SearchDataSource(private val patientDao: PatientDao) {
    suspend fun queryPatientsAndAttributesForSearchString(
        attribute: String,
        name: String,
        phoneNumberAttribute: String
    ): List<PatientWithAttribute> =
        patientDao.queryPatientsAndAttributesForSearchString(attribute, name, phoneNumberAttribute)
}
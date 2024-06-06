package org.intelehealth.ncd.data.search

import org.intelehealth.ncd.model.PatientWithAttribute

class SearchRepository(private val dataSource: SearchDataSource) {

    suspend fun queryPatientsAndAttributesForSearchString(
        attribute: String,
        name: String,
        phoneNumberAttribute: String
    ): List<PatientWithAttribute> =
        dataSource.queryPatientsAndAttributesForSearchString(attribute, name, phoneNumberAttribute)

}
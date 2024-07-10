package org.intelehealth.app.ui.patient.data

import org.intelehealth.app.shared.builder.QueryBuilder

/**
 * Created by Vaghela Mithun R. on 09-07-2024 - 17:52.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientQueryBuilder : QueryBuilder() {
    fun buildPatientDetailsQuery(patientId: String): String {
        return select(
            " P.uuid, P.openmrs_id, P.first_name, P.middle_name, P.last_name, P.gender, P.date_of_birth, P.address1, P.address2, " +
                    "P.city_village, P.state_province, P.postal_code, P.country,P.phone_number, P.patient_photo, P.guardian_name, P.guardian_type," +
                    "P.contact_type,P.em_contact_name,P.em_contact_num,"
                    + buildPatientAttributesQuery("Telephone Number") + " telephone,"
                    + buildPatientAttributesQuery("Economic Status") + " economicStatus,"
                    + buildPatientAttributesQuery("Education Level") + " educationLevel,"
                    + buildPatientAttributesQuery("providerUUID") + " provider,"
                    + buildPatientAttributesQuery("occupation") + " occupation,"
                    + buildPatientAttributesQuery("Son/wife/daughter") + " sdw,"
                    + buildPatientAttributesQuery("NationalID") + " nationalId,"
                    + buildPatientAttributesQuery("ProfileImageTimestamp") + " profileImageTimestamp,"
                    + buildPatientAttributesQuery("Caste") + " caste,"
                    + buildPatientAttributesQuery("createdDate") + " createdDate "
        )
            .from("tbl_patient P")
            .where("P.uuid =  '$patientId' AND P.voided  = '0' ")
            .groupBy(" P.uuid ")
            .build()
    }

    private fun buildPatientAttributesQuery(attrName: String): String {
        return "(SELECT value FROM tbl_patient_attribute WHERE patientuuid = P.uuid " +
                "AND person_attribute_type_uuid = (SELECT uuid FROM tbl_patient_attribute_master WHERE name = '" + attrName + "')) "
    }
}
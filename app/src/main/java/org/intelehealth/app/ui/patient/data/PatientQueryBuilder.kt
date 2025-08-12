package org.intelehealth.app.ui.patient.data

import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.shared.builder.QueryBuilder

/**
 * Created by Vaghela Mithun R. on 09-07-2024 - 17:52.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientQueryBuilder : QueryBuilder() {
    fun buildPatientDetailsQuery(patientId: String): String {
        return select(
            "P.uuid, P.openmrs_id, P.first_name, P.middle_name, P.last_name, P.gender, P.date_of_birth, P.address1, P.address2, " +
                    "P.city_village, P.state_province, P.postal_code, P.country,P.phone_number, P.patient_photo, P.guardian_name, P.guardian_type," +
                    "P.address3,address6,countyDistrict,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.TELEPHONE.value) + " telephone,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.ECONOMIC_STATUS.value) + " economicStatus,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.EDUCATION.value) + " educationLevel,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.PROVIDER_ID.value) + " provider,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.OCCUPATION.value) + " occupation,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.SWD.value) + " sdw,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.NATIONAL_ID.value) + " nationalId,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.PROFILE_IMG_TIMESTAMP.value) + " profileImageTimestamp,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.CAST.value) + " caste,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.CREATED_DATE.value) + " createdDate,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.TMH_CASE_NUMBER.value) + " tmhCaseNumber,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.REQUEST_ID.value) + " requestId,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.DISCIPLINE.value) + " discipline,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.RELATIVE_PHONE_NUMBER.value) + " relativePhoneNumber,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.CREATED_DATE.value) + " createdDate,"

                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.PROVINCES.value) + " provinces,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.CITIES.value) + " cities,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.REGISTRATION_ADDRESS_OF_HF.value) + " registrationAddressOfHf,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.INN.value) + " inn,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.CODE_OF_HEALTH_FACILITY.value) + " codeOfHealthFacility,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.HEALTH_FACILITY_NAME.value) + " healthFacilityName,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.CODE_OF_DEPARTMENT.value) + " codeOfDepartment,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.DEPARTMENT.value) + " department, "
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.CREATED_DATE.value) + " createdDate, "
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.BLOCK.value) + " blockSurvey, "
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.HOUSEHOLD_UUID_LINKING.value) + " HouseHold,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.EMERGENCY_CONTACT_TYPE.value) + " contact_type,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.EMERGENCY_CONTACT_NAME.value) + " em_contact_name,"
                    + buildPatientAttributesQuery(PatientAttributesDTO.Column.EMERGENCY_CONTACT_NUMBER.value) + " em_contact_num"
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
package org.intelehealth.app.utilities

import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.model.AbhaProfileResponse
import org.intelehealth.abdm.model.OTPVerificationResponse
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object AbhaUtils {
    fun getPatientPersonalDetailsFromOtpResponse(
        patient: PatientDTO,
        response: OTPVerificationResponse
    ) {
        patient.patientPhoto = response.abhaProfile.photo
        patient.firstname = response.abhaProfile.firstName
        patient.middlename = response.abhaProfile.middleName
        patient.lastname = response.abhaProfile.lastName
        patient.gender = response.abhaProfile.gender
        patient.phonenumber = "91${response.abhaProfile.mobile}"
        patient.dateofbirth = DateTimeUtils.formatToLocalDate(
            formatPatientDobFromCreation(response.abhaProfile.dob),
            DateTimeUtils.YYYY_MM_DD_HYPHEN
        )
    }

    fun getPatientAddressDetailsFromOtpResponse(
        patient: PatientDTO,
        response: OTPVerificationResponse
    ) {
        patient.postalcode = response.abhaProfile.pinCode
        bifurcateAddress(response.abhaProfile.address, patient)
    }

    fun getPatientAbhaDetailsFromOtpResponse(
        patient: PatientDTO,
        response: OTPVerificationResponse
    ) {
        patient.abhaNumber = response.abhaProfile.abhaNumber
        patient.abhaAddress = "${response.abhaProfile.phrAddress[0]}"
    }

    fun getPatientAbhaDetailsFromAbhaResponse(
        patient: PatientDTO,
        response: AbhaProfileResponse
    ) {
        patient.patientPhoto = response.profilePhoto
        patient.firstname = response.firstName
        patient.middlename = response.middleName
        patient.lastname = response.lastName
        patient.gender = response.gender
        patient.phonenumber = "91${response.mobile}"
//        patient.dateofbirth =
    }

//    private fun formatPatientDobFromVerification(): Date {
//
//    }

    private fun formatPatientDobFromCreation(date: String): Date {
        val pattern = AbdmConstant.ABHA_DOB_FORMAT
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern))
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private fun bifurcateAddress(address: String, patientDTO: PatientDTO) {
        val parts = address.split(",").map { it.trim() }
        if (parts.size < 3) {
            patientDTO.address1 = address
            return
        }

        patientDTO.stateprovince = parts[parts.size - 1]
        patientDTO.district = parts[parts.size - 2]
        patientDTO.cityvillage = parts[parts.size - 3]
        patientDTO.address1 = parts.dropLast(3).joinToString(", ")
    }
}
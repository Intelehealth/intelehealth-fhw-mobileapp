package org.intelehealth.app.utilities

import android.content.Context
import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.model.AbhaProfileResponse
import org.intelehealth.abdm.model.OTPVerificationResponse
import org.intelehealth.abdm.utils.AbhaImageUtils
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object AbhaUtils {
    fun getPatientPersonalDetailsFromOtpResponse(
        context: Context,
        patient: PatientDTO,
        response: OTPVerificationResponse
    ) {
        patient.patientPhoto = AbhaImageUtils.convertEncodedToFile(
            context,
            response.abhaProfile.photo,
            patient.uuid
        )
        patient.firstname = response.abhaProfile.firstName
        patient.middlename = response.abhaProfile.middleName
        patient.lastname = response.abhaProfile.lastName
        patient.gender = response.abhaProfile.gender
        patient.phonenumber = "91${response.abhaProfile.mobile}"
        patient.dateofbirth = DateTimeUtils.formatToLocalDate(
            formatPatientDob(response.abhaProfile.dob),
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
        patient.abhaAddress =
            if (response.abhaProfile.phrAddress[0].endsWith("@abdm")) {
                response.abhaProfile.phrAddress[0]
            } else {
                "${response.abhaProfile.phrAddress[0]}@abdm"
            }
    }

    fun getPatientPersonalDetailsFromAbhaResponse(
        context: Context,
        patient: PatientDTO,
        response: AbhaProfileResponse
    ) {
        patient.patientPhoto = AbhaImageUtils.convertEncodedToFile(
            context,
            response.profilePhoto,
            patient.uuid
        )
        patient.firstname = response.firstName
        patient.middlename = response.middleName
        patient.lastname = response.lastName
        patient.gender = response.gender
        patient.phonenumber = "91${response.mobile}"
        patient.dateofbirth = DateTimeUtils.formatToLocalDate(
            formatPatientDobFromVerification(
                response.dayOfBirth,
                response.monthOfBirth,
                response.yearOfBirth
            ),
            DateTimeUtils.YYYY_MM_DD_HYPHEN
        )
    }

    fun getPatientAddressDetailsFromAbhaResponse(
        patient: PatientDTO,
        response: AbhaProfileResponse
    ) {
        patient.postalcode = response.pincode
        bifurcateAddress(response.address, patient)
    }

    fun getPatientAbhaDetailsFromAbhaResponse(
        patient: PatientDTO,
        response: AbhaProfileResponse
    ) {
        patient.abhaNumber = response.abhaNumber
        patient.abhaAddress = if (response.preferredAbhaAddress.endsWith("@abdm")) {
            response.preferredAbhaAddress
        } else {
            "${response.preferredAbhaAddress}@abdm"
        }
    }

    private fun formatPatientDobFromVerification(
        dayOfBirth: String,
        monthOfBirth: String,
        yearOfBirth: String
    ): Date {
        val abhaDate = "$dayOfBirth-$monthOfBirth-$yearOfBirth"
        return formatPatientDob(abhaDate)
    }

    private fun formatPatientDob(date: String): Date {
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
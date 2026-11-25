package org.intelehealth.abdm.utils

import org.intelehealth.abdm.api.AbhaPatientDetails
import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.model.AbhaProfileResponse
import org.intelehealth.abdm.model.OTPVerificationResponse
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Converts ABDM API responses into the app-facing [AbhaPatientDetails]. */
internal object AbhaPatientMapper {
    private val abhaDobFormat = DateTimeFormatter.ofPattern(AbdmConstant.ABHA_DOB_FORMAT)

    /** Create-ABHA flow (enrol by Aadhaar). */
    fun fromOtpResponse(response: OTPVerificationResponse): AbhaPatientDetails {
        val profile = response.abhaProfile
        val address = splitAddress(profile.address)
        return AbhaPatientDetails(
            firstName = profile.firstName,
            middleName = profile.middleName,
            lastName = profile.lastName,
            gender = profile.gender,
            dateOfBirth = toAppDate(profile.dob),
            phoneNumber = profile.mobile?.let { "91$it" },
            address1 = address.address1,
            cityVillage = address.cityVillage,
            district = address.district,
            stateProvince = address.state,
            postalCode = profile.pinCode,
            abhaNumber = profile.abhaNumber,
            abhaAddress = profile.phrAddress?.firstOrNull()?.let(::withAbdmSuffix),
            photoBase64 = profile.photo
        )
    }

    /** Verify-ABHA flow (login + profile fetch). */
    fun fromAbhaProfile(response: AbhaProfileResponse): AbhaPatientDetails {
        val address = splitAddress(response.address)
        val dob = "${response.dayOfBirth}-${response.monthOfBirth}-${response.yearOfBirth}"
        return AbhaPatientDetails(
            firstName = response.firstName,
            middleName = response.middleName,
            lastName = response.lastName,
            gender = response.gender,
            dateOfBirth = toAppDate(dob),
            phoneNumber = response.mobile?.let { "91$it" },
            address1 = address.address1,
            cityVillage = address.cityVillage,
            district = address.district,
            stateProvince = address.state,
            postalCode = response.pincode,
            abhaNumber = response.abhaNumber,
            abhaAddress = response.preferredAbhaAddress?.let(::withAbdmSuffix),
            photoBase64 = response.profilePhoto
        )
    }

    private fun withAbdmSuffix(address: String): String {
        val suffix = AbdmManager.abhaAddressSuffix
        return if (address.endsWith(suffix)) address else "$address$suffix"
    }

    /** ABHA "dd-MM-yyyy" -> app "yyyy-MM-dd". */
    private fun toAppDate(abhaDate: String?): String? {
        abhaDate ?: return null
        return try {
            LocalDate.parse(abhaDate, abhaDobFormat).toString()
        } catch (e: Exception) {
            Timber.tag("AbhaPatientMapper").e(e, "Invalid ABHA date of birth")
            null
        }
    }

    private data class AddressParts(
        val address1: String?,
        val cityVillage: String? = null,
        val district: String? = null,
        val state: String? = null
    )

    /** "line, line, city, district, state": the last three parts are city, district, state. */
    private fun splitAddress(address: String?): AddressParts {
        address ?: return AddressParts(null)
        val parts = address.split(",").map { it.trim() }
        if (parts.size < 3) return AddressParts(address)
        return AddressParts(
            address1 = parts.dropLast(3).joinToString(", "),
            cityVillage = parts[parts.size - 3],
            district = parts[parts.size - 2],
            state = parts[parts.size - 1]
        )
    }
}

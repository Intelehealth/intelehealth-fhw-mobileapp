package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.remote.dto.VerifyOtpResponseDto
import org.intelehealth.abdm.domain.model.EnrolledAbhaProfile
import org.intelehealth.abdm.domain.model.EnrolledAbhaToken
import org.intelehealth.abdm.domain.model.AbhaCreateSession

internal fun VerifyOtpResponseDto.toDomain(): AbhaCreateSession? {
    val enrolledAbhaToken = with(tokens ?: return null) {
        EnrolledAbhaToken(
            token = token ?: return null,
            expiresIn = expiresIn ?: return null,
        )
    }
    val profile = with(abhaProfile ?: return null) {
        EnrolledAbhaProfile(
            firstName = firstName.orEmpty(),
            lastName = lastName.orEmpty(),
            middleName = middleName,
            dateOfBirth = dob.orEmpty(),
            gender = gender.orEmpty(),
            profilePhoto = photo,
            mobile = mobile.orEmpty(),
            address = address.orEmpty(),
            phrAddresses = phrAddress ?: emptyList(),
            pinCode = pinCode.orEmpty(),
            abhaNumber = abhaNumber ?: return null,
            preferredAbhaAddress = preferredAddress,
        )
    }
    return AbhaCreateSession(
        txnId = txnId ?: return null,
        isNew = isNew ?: false,
        profile = profile,
        enrolledAbhaToken = enrolledAbhaToken,
    )
}
package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.remote.dto.AbhaCardResponseDto
import org.intelehealth.abdm.data.remote.dto.FetchAbhaProfileResponseDto
import org.intelehealth.abdm.domain.model.AbhaCard
import org.intelehealth.abdm.domain.model.AbhaProfile

internal fun FetchAbhaProfileResponseDto.toDomain(): AbhaProfile? {
    val abhaNumber: String = abhaNumber ?: return null
    val preferredAbhaAddress: String = preferredAbhaAddress ?: abhaAddress.orEmpty()
    val mobile: String = mobile.orEmpty()
    val firstName: String = firstName.orEmpty()
    val lastName: String = lastName.orEmpty()
    val yearOfBirth: String = yearOfBirth.orEmpty()
    val dayOfBirth: String = dayOfBirth.orEmpty()
    val monthOfBirth: String = monthOfBirth.orEmpty()
    val gender: String = gender.orEmpty()
    val address: String = address.orEmpty()
    val pinCode: String = pinCode.orEmpty()
    val stateName: String = stateName.orEmpty()

    return AbhaProfile(
        abhaNumber = abhaNumber,
        preferredAbhaAddress = preferredAbhaAddress,
        mobile = mobile,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        yearOfBirth = yearOfBirth,
        dayOfBirth = dayOfBirth,
        monthOfBirth = monthOfBirth,
        gender = gender,
        profilePhoto = profilePhoto,
        pinCode = pinCode,
        address = address,
        stateName = stateName,
    )
}

internal fun AbhaCardResponseDto.toDomain(): AbhaCard? {
    if (image == null) return null
    return AbhaCard(image = image)
}
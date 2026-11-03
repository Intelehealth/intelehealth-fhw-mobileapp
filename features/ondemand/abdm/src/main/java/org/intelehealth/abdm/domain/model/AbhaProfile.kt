package org.intelehealth.abdm.domain.model

internal data class AbhaProfile(
    val abhaNumber: String,
    val preferredAbhaAddress: String,
    val mobile: String,

    val firstName: String,
    val middleName: String?,
    val lastName: String,

    val yearOfBirth: String,
    val monthOfBirth: String,
    val dayOfBirth: String,

    val gender: String,
    val profilePhoto: String?,

    val address: String,
    val pinCode: String,
    val stateName: String,
)
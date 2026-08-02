package org.intelehealth.abdm.domain.model

internal data class AbhaCreateSession(
    val txnId: String,
    val profile: EnrolledAbhaProfile,
    val isNew: Boolean,
    val enrolledAbhaToken: EnrolledAbhaToken,
)

internal data class EnrolledAbhaToken(
    val token: String,
    val expiresIn: Int,
)

internal data class EnrolledAbhaProfile(
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val dateOfBirth: String,
    val gender: String,
    val address: String,
    val profilePhoto: String?,
    val mobile: String,
    val phrAddresses: List<String>,
    val pinCode: String,
    val abhaNumber: String,
    val preferredAbhaAddress: String?,
)
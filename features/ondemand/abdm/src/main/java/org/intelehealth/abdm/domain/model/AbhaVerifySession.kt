package org.intelehealth.abdm.domain.model

internal data class AbhaVerifySession(
    val txnId: String,
    val authResult: String,
    val message: String,
    val token: String,
    val accounts: List<AbhaSearchResult>,
    val user: VerifiedAbhaUser?,
    val expiresIn: Int,
    val refreshToken: String,
)

internal data class VerifiedAbhaUser(
    val kycStatus: String,
)
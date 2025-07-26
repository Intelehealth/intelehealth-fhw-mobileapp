package org.intelehealth.app.abdm.model

data class FetchAuthModesResponse(
    val healthIdNumber: String,
    val abhaAddress: String,
    val authMethods: List<String>,
    val blockedAuthMethods: List<String>,
    val status: String,
    val message: String?,
    val fullName: String,
    val mobile: String
)

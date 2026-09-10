package org.intelehealth.abdm.domain.model

internal data class RequestedOtp(
    val txnId: String,
    val message: String,
)

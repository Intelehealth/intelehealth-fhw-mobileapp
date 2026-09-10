package org.intelehealth.abdm.domain.repository

import org.intelehealth.abdm.domain.model.RequestedOtp
import org.intelehealth.abdm.domain.model.AbhaCreateSession

internal interface AbhaCreateRepository {
    suspend fun verifyMobileForEnrollment(
        otp: String,
        txnId: String,
        mobileNumber: String,
    ): Result<RequestedOtp>

    suspend fun requestAadhaarOtp(
        value: String,
        scope: String,
    ): Result<RequestedOtp>

    /** Sends an OTP to a newly-entered mobile number during enrollment (scope = "mobile"). */
    suspend fun requestMobileOtp(
        mobileNumber: String,
        txnId: String,
    ): Result<RequestedOtp>

    suspend fun verifyAadhaarOtp(
        otp: String,
        txnId: String,
        mobileNumber: String,
    ): Result<AbhaCreateSession>
}
package org.intelehealth.abdm.data.repository

import org.intelehealth.abdm.data.mapper.toDomain
import org.intelehealth.abdm.data.remote.api.AbhaCreateApi
import org.intelehealth.abdm.data.remote.auth.TokenManager
import org.intelehealth.abdm.data.remote.dto.AadhaarRequestDto
import org.intelehealth.abdm.data.remote.dto.RegisterNumberWithAbdmRequestDto
import org.intelehealth.abdm.data.remote.dto.VerifyOtpRequestDto
import org.intelehealth.abdm.data.remote.extensions.MalformedResponseException
import org.intelehealth.abdm.data.remote.extensions.OtpVerificationFailedException
import org.intelehealth.abdm.data.remote.extensions.requireBody
import org.intelehealth.abdm.domain.model.RequestedOtp
import org.intelehealth.abdm.domain.model.AbhaCreateSession
import org.intelehealth.abdm.domain.repository.AbhaCreateRepository
import javax.inject.Inject

internal class AbhaCreateRepositoryImpl @Inject constructor(
    private val api: AbhaCreateApi,
    private val tokenManager: TokenManager,
) : AbhaCreateRepository {
    override suspend fun verifyMobileForEnrollment(
        otp: String,
        txnId: String,
        mobileNumber: String,
    ): Result<RequestedOtp> = runCatching {
        tokenManager.ensureValidToken()
        val response = api.verifyMobileForEnrollment(
            RegisterNumberWithAbdmRequestDto(
                otp = otp,
                txnId = txnId,
                mobileNumber = mobileNumber,
            )
        ).requireBody()
        if (response.authResult.equals(AUTH_RESULT_FAILED, ignoreCase = true)) {
            throw OtpVerificationFailedException(response.message)
        }
        response.toDomain()
            ?: throw MalformedResponseException("Enroll mobile response missing required fields")
    }

    override suspend fun requestAadhaarOtp(
        value: String,
        scope: String,
    ): Result<RequestedOtp> = runCatching {
        tokenManager.ensureValidToken()
        api.requestAadhaarOtp(
            AadhaarRequestDto(
                value = value,
                scope = scope,
            )
        )
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Request aadhaar otp missing required fields")
    }

    override suspend fun requestMobileOtp(
        mobileNumber: String,
        txnId: String,
    ): Result<RequestedOtp> = runCatching {
        tokenManager.ensureValidToken()
        api.requestAadhaarOtp(
            AadhaarRequestDto(
                value = mobileNumber,
                scope = SCOPE_MOBILE,
                txnId = txnId,
            )
        )
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Request mobile otp missing required fields")
    }

    override suspend fun verifyAadhaarOtp(
        otp: String,
        txnId: String,
        mobileNumber: String,
    ): Result<AbhaCreateSession> = runCatching {
        tokenManager.ensureValidToken()
        api.verifyAadhaarOtp(
            VerifyOtpRequestDto(
                otp = otp,
                txnId = txnId,
                mobileNumber = mobileNumber,
            )
        )
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Verify aadhaar otp missing required fields")
    }

    private companion object {
        const val SCOPE_MOBILE = "mobile"
        const val AUTH_RESULT_FAILED = "failed"
    }
}
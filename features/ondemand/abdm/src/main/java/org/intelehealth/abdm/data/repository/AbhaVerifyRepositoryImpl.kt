package org.intelehealth.abdm.data.repository

import org.intelehealth.abdm.data.mapper.toDomain
import org.intelehealth.abdm.data.remote.api.AbhaVerifyApi
import org.intelehealth.abdm.data.remote.auth.TokenManager
import org.intelehealth.abdm.data.remote.dto.FetchAuthModesRequestDto
import org.intelehealth.abdm.data.remote.dto.MobileOtpRequestDto
import org.intelehealth.abdm.data.remote.dto.SearchProfileRequestDto
import org.intelehealth.abdm.data.remote.dto.VerifyOtpRequestDto
import org.intelehealth.abdm.data.remote.extensions.MalformedResponseException
import org.intelehealth.abdm.data.remote.extensions.requireBody
import org.intelehealth.abdm.domain.model.AbhaVerifySession
import org.intelehealth.abdm.domain.model.AuthModes
import org.intelehealth.abdm.domain.model.RequestedOtp
import org.intelehealth.abdm.domain.model.SearchedAbhaProfiles
import org.intelehealth.abdm.domain.repository.AbhaVerifyRepository
import javax.inject.Inject

internal class AbhaVerifyRepositoryImpl @Inject constructor(
    private val api: AbhaVerifyApi,
    private val tokenManager: TokenManager,
) : AbhaVerifyRepository {
    override suspend fun fetchAbhaProfiles(
        value: String,
    ): Result<SearchedAbhaProfiles> = runCatching {
        tokenManager.ensureValidToken()
        val profilesByIndex = api.fetchAbhaProfiles(
            SearchProfileRequestDto(
                value = value,
            )
        ).requireBody()
        val firstProfile = profilesByIndex["0"]
            ?: throw MalformedResponseException("Search abha profiles response was empty")
        firstProfile.toDomain()
            ?: throw MalformedResponseException("Fetch abha profiles missing required fields")
    }

    override suspend fun requestMobileOtp(
        value: String,
        scope: String,
        authMethod: String?,
        txnId: String?,
    ): Result<RequestedOtp> = runCatching {
        tokenManager.ensureValidToken()
        api.requestMobileOtp(
            MobileOtpRequestDto(
                value = value,
                scope = scope,
                authMethod = authMethod,
                txnId = txnId,
            )
        )
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Request mobile otp missing required fields")
    }

    override suspend fun verifyLoginOtp(
        otp: String,
        txnId: String,
        scope: String,
        authMethod: String?,
    ): Result<AbhaVerifySession> = runCatching {
        tokenManager.ensureValidToken()
        api.verifyLoginOtp(
            VerifyOtpRequestDto(
                otp = otp,
                txnId = txnId,
                mobileNumber = null,
                scope = scope,
                authMethod = authMethod,
            )
        )
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Verify login otp missing required fields")
    }

    override suspend fun fetchAuthModes(
        xToken: String,
        abhaAddress: String,
    ): Result<AuthModes> = runCatching {
        tokenManager.ensureValidToken()
        api.fetchAuthModes(
            xToken = xToken,
            body = FetchAuthModesRequestDto(
                abhaAddress = abhaAddress,
            )
        )
            .requireBody()
            .toDomain()
    }
}
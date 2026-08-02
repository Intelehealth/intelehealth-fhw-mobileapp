package org.intelehealth.abdm.domain.repository

import org.intelehealth.abdm.domain.model.AbhaVerifySession
import org.intelehealth.abdm.domain.model.AuthModes
import org.intelehealth.abdm.domain.model.RequestedOtp
import org.intelehealth.abdm.domain.model.SearchedAbhaProfiles

internal interface AbhaVerifyRepository {
    suspend fun fetchAbhaProfiles(
        value: String,
    ): Result<SearchedAbhaProfiles>

    suspend fun requestMobileOtp(
        value: String,
        scope: String,
        authMethod: String? = null,
        txnId: String? = null,
    ): Result<RequestedOtp>

    suspend fun verifyLoginOtp(
        otp: String,
        txnId: String,
        scope: String,
        authMethod: String? = null,
    ): Result<AbhaVerifySession>

    suspend fun fetchAuthModes(
        xToken: String,
        abhaAddress: String,
    ): Result<AuthModes>
}
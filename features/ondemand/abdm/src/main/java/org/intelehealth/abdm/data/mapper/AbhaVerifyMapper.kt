package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.remote.dto.AbhaAccountDto
import org.intelehealth.abdm.data.remote.dto.AbhaUserDto
import org.intelehealth.abdm.data.remote.dto.FetchAuthModesResponseDto
import org.intelehealth.abdm.data.remote.dto.SearchProfileResponseDto
import org.intelehealth.abdm.data.remote.dto.VerifyMobileOtpResponseDto
import org.intelehealth.abdm.domain.model.AbhaSearchResult
import org.intelehealth.abdm.domain.model.AbhaVerifySession
import org.intelehealth.abdm.domain.model.AuthModes
import org.intelehealth.abdm.domain.model.SearchedAbhaProfiles
import org.intelehealth.abdm.domain.model.VerifiedAbhaUser

internal fun SearchProfileResponseDto.toDomain(): SearchedAbhaProfiles? {
    val resolvedTxnId = txnId ?: return null
    val profiles = abhaAccount?.mapNotNull { it.toDomain() } ?: emptyList()
    return SearchedAbhaProfiles(
        txnId = resolvedTxnId,
        abhaSearchResults = profiles,
    )
}

internal fun AbhaAccountDto.toDomain(): AbhaSearchResult? {
    return AbhaSearchResult(
        abhaNumber = abhaNumber ?: return null,
        name = name.orEmpty(),
        gender = gender,
        index = index,
    )
}

internal fun AbhaUserDto.toDomain(): VerifiedAbhaUser {
    return VerifiedAbhaUser(
        kycStatus = kycStatus.orEmpty(),
    )
}

internal fun VerifyMobileOtpResponseDto.toDomain(): AbhaVerifySession? {
    return AbhaVerifySession(
        txnId = txnId.orEmpty(),
        authResult = authResult ?: return null,
        message = message.orEmpty(),
        token = token.orEmpty(),
        accounts = accounts?.mapNotNull { it.toDomain() } ?: emptyList(),
        user = users?.firstOrNull()?.toDomain(),
        expiresIn = expiresIn ?: 0,
        refreshToken = refreshToken.orEmpty(),
    )
}

internal fun FetchAuthModesResponseDto.toDomain(): AuthModes {
    return AuthModes(
        authMethods = authMethods ?: emptyList(),
    )
}
package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.remote.dto.TokenDto
import org.intelehealth.abdm.domain.model.AuthToken

internal fun TokenDto.toDomain(): AuthToken? {
    val token = accessToken ?: return null
    val expiresIn = expiresIn ?: return null

    return AuthToken(
        accessToken = token,
        expiresInSeconds = expiresIn,
        refreshToken = refreshToken
    )
}
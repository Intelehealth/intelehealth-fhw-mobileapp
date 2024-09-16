package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.model.AuthTokenResponseDto
import org.intelehealth.abdm.domain.model.AuthToken
import javax.inject.Inject

class AuthTokenMapper @Inject constructor(){

     fun mapToDomain(authTokenResponseDto: AuthTokenResponseDto): AuthToken {
        return AuthToken(
            accessToken = authTokenResponseDto.accessToken,
            expiresIn = authTokenResponseDto.expiresIn,
            refreshExpiresIn = authTokenResponseDto.refreshExpiresIn,
            refreshToken = authTokenResponseDto.refreshToken,
            tokenType = authTokenResponseDto.tokenType
        )
    }
}

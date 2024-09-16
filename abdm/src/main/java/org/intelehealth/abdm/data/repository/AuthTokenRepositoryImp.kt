package org.intelehealth.abdm.data.repository

import org.intelehealth.abdm.data.mapper.AuthTokenMapper
import org.intelehealth.abdm.data.network.SafeApiCall
import org.intelehealth.abdm.data.services.AbdmService
import org.intelehealth.abdm.domain.model.AuthToken
import org.intelehealth.abdm.domain.repository.AuthTokenRepository
import org.intelehealth.abdm.domain.result.ApiResult
import javax.inject.Inject

class AuthTokenRepositoryImp @Inject constructor(
    private val abdmService: AbdmService,
    private val authTokenMapper: AuthTokenMapper
) : AuthTokenRepository {
    override suspend fun getAuthToken(): ApiResult<AuthToken> {
        return SafeApiCall.call(
            { abdmService.getAuthTokenApi() },
            { abdmServiceDto -> authTokenMapper.mapToDomain(abdmServiceDto) })
    }
}
package org.intelehealth.abdm.domain.repository

import org.intelehealth.abdm.domain.model.AuthToken
import org.intelehealth.abdm.domain.result.ApiResult

interface AuthTokenRepository {
    suspend fun getAuthToken() : ApiResult<AuthToken>
}
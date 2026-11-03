package org.intelehealth.abdm.data.repository

import org.intelehealth.abdm.data.mapper.toDomain
import org.intelehealth.abdm.data.remote.api.AbdmAuthApi
import org.intelehealth.abdm.data.remote.auth.TokenStore
import org.intelehealth.abdm.data.remote.extensions.MalformedResponseException
import org.intelehealth.abdm.data.remote.extensions.requireBody
import org.intelehealth.abdm.domain.model.AuthToken
import org.intelehealth.abdm.domain.repository.AbdmAuthRepository
import javax.inject.Inject

internal class AbdmAuthRepositoryImpl @Inject constructor(
    private val authApi: AbdmAuthApi,
    private val tokenStore: TokenStore,
) : AbdmAuthRepository {
    override suspend fun fetchAndStoreToken(): Result<AuthToken> = runCatching {
        val token = authApi.getToken()
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("getToken response missing required fields (accessToken or expiresIn)")

        token.also { tokenStore.set(it.accessToken, it.expiresInSeconds) }
    }
}
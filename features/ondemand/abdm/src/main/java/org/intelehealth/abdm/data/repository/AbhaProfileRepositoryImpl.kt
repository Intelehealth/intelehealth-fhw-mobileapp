package org.intelehealth.abdm.data.repository

import org.intelehealth.abdm.data.mapper.toDomain
import org.intelehealth.abdm.data.remote.api.AbhaProfileApi
import org.intelehealth.abdm.data.remote.auth.TokenManager
import org.intelehealth.abdm.data.remote.dto.FetchAbhaProfileRequestDto
import org.intelehealth.abdm.data.remote.extensions.MalformedResponseException
import org.intelehealth.abdm.data.remote.extensions.requireBody
import org.intelehealth.abdm.domain.model.AbhaCard
import org.intelehealth.abdm.domain.model.AbhaProfile
import org.intelehealth.abdm.domain.repository.AbhaProfileRepository
import javax.inject.Inject

internal class AbhaProfileRepositoryImpl @Inject constructor(
    private val api: AbhaProfileApi,
    private val tokenManager: TokenManager,
) : AbhaProfileRepository {
    override suspend fun fetchAbhaProfile(
        xToken: String,
        txnId: String,
        abhaNumber: String?,
        scope: String,
    ): Result<AbhaProfile> = runCatching {
        tokenManager.ensureValidToken()
        api.fetchAbhaProfile(
            xToken = xToken,
            body = FetchAbhaProfileRequestDto(
                txnId = txnId,
                abhaNumber = abhaNumber,
                scope = scope,
            ),
        ).requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Abha profile response missing required fields")
    }

    override suspend fun fetchAbhaCard(
        xToken: String,
        scope: String,
    ): Result<AbhaCard> = runCatching {
        tokenManager.ensureValidToken()
        api.fetchAbhaCard(scope = scope, xToken = xToken)
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Abha card response missing required fields")
    }
}
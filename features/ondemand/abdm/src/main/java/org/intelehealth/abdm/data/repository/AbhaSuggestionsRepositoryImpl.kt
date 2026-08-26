package org.intelehealth.abdm.data.repository

import org.intelehealth.abdm.data.mapper.toDomain
import org.intelehealth.abdm.data.remote.api.AbhaSuggestionsApi
import org.intelehealth.abdm.data.remote.auth.TokenManager
import org.intelehealth.abdm.data.remote.dto.AbhaAddressSuggestionsRequestDto
import org.intelehealth.abdm.data.remote.dto.RegisterAbhaAddressRequestDto
import org.intelehealth.abdm.data.remote.extensions.MalformedResponseException
import org.intelehealth.abdm.data.remote.extensions.requireBody
import org.intelehealth.abdm.domain.model.AbhaSuggestions
import org.intelehealth.abdm.domain.model.RegisteredAbhaAddress
import org.intelehealth.abdm.domain.repository.AbhaSuggestionsRepository
import javax.inject.Inject

internal class AbhaSuggestionsRepositoryImpl @Inject constructor(
    private val api: AbhaSuggestionsApi,
    private val tokenManager: TokenManager,
) : AbhaSuggestionsRepository {
    override suspend fun fetchSuggestions(txnId: String): Result<AbhaSuggestions> = runCatching {
        tokenManager.ensureValidToken()
        api.fetchAbhaAddressSuggestions(AbhaAddressSuggestionsRequestDto(txnId = txnId))
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("Suggestions response missing required fields")
    }

    override suspend fun registerPreferredAddress(
        txnId: String,
        abhaAddress: String,
    ): Result<RegisteredAbhaAddress> = runCatching {
        tokenManager.ensureValidToken()
        api.registerPreferredAbhaAddress(
            RegisterAbhaAddressRequestDto(
                txnId = txnId,
                abhaAddress = abhaAddress,
            )
        )
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("RegisterAddress response missing required fields")
    }
}
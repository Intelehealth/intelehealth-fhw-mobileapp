package org.intelehealth.abdm.domain.repository

import org.intelehealth.abdm.domain.model.AbhaSuggestions
import org.intelehealth.abdm.domain.model.RegisteredAbhaAddress

internal interface AbhaSuggestionsRepository {
    suspend fun fetchSuggestions(txnId: String): Result<AbhaSuggestions>
    suspend fun registerPreferredAddress(
        txnId: String,
        abhaAddress: String,
    ): Result<RegisteredAbhaAddress>
}
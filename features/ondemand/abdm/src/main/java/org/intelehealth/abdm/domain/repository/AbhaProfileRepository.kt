package org.intelehealth.abdm.domain.repository

import org.intelehealth.abdm.domain.model.AbhaCard
import org.intelehealth.abdm.domain.model.AbhaProfile

internal interface AbhaProfileRepository {
    suspend fun fetchAbhaProfile(
        xToken: String,
        txnId: String,
        abhaNumber: String?,
        scope: String,
    ): Result<AbhaProfile>

    suspend fun fetchAbhaCard(
        xToken: String,
        scope: String,
    ): Result<AbhaCard>
}
package org.intelehealth.abdm.domain.repository

import org.intelehealth.abdm.domain.model.AuthToken

internal interface AbdmAuthRepository {
    suspend fun fetchAndStoreToken(): Result<AuthToken>
}
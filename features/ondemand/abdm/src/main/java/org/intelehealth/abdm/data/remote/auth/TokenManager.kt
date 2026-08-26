package org.intelehealth.abdm.data.remote.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.intelehealth.abdm.domain.repository.AbdmAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TokenManager @Inject constructor(
    private val authRepository: AbdmAuthRepository,
    private val tokenStore: TokenStore,
) {
    private val refreshMutex = Mutex()

    /**
     * Guarantees TokenStore has a valid (non-expired) token before returning.
     * If the current token is missing or expired, fetches a new one.
     * Safe to call concurrently; only one refresh happens at a time.
     */
    suspend fun ensureValidToken() {
        if (tokenStore.get() != null) return

        refreshMutex.withLock {
            if (tokenStore.get() != null) return@withLock
            authRepository.fetchAndStoreToken().getOrThrow()
        }
    }
}
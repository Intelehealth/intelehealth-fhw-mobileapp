package org.intelehealth.abdm.data.remote.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor() {
    @Volatile
    private var token: String? = null

    @Volatile
    private var expiresAtMillis: Long = 0L

    fun get(): String? = if (isValid()) token else null

    fun set(value: String, expiresInSeconds: Int) {
        token = value
        expiresAtMillis =
            System.currentTimeMillis() + (expiresInSeconds - SAFETY_BUFFER_SECONDS) * 1000L
    }

    fun clear() {
        token = null
        expiresAtMillis = 0L
    }

    private fun isValid(): Boolean = token != null && System.currentTimeMillis() < expiresAtMillis

    private companion object {
        const val SAFETY_BUFFER_SECONDS = 60
    }
}
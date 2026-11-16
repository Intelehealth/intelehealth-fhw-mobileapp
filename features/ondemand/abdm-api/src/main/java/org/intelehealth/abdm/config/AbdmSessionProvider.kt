package org.intelehealth.abdm.config

/**
 * Provides current session state to abdm. Implementations MUST read fresh data
 * on each call — return values can change over the app's lifetime (login, logout,
 * token refresh, account switch).
 */
interface AbdmSessionProvider {
    fun getEncodedCredentials(): String?

    /** The active facility/location UUID, used when linking an ABHA address to a patient. */
    fun getLocationUuid(): String
}
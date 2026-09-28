package org.intelehealth.abdm.data.remote.auth

import org.intelehealth.abdm.config.AbdmSessionProvider
import org.intelehealth.abdm.di.network.BASIC_PREFIX

internal fun AbdmSessionProvider.basicAuthHeader(): String {
    val encoded = getEncodedCredentials()
        ?: throw IllegalStateException("No session — patient endpoint called without credentials")
    return "$BASIC_PREFIX$encoded"
}
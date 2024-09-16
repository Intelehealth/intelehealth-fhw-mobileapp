package org.intelehealth.abdm.domain.repository.registration

import org.intelehealth.abdm.domain.model.RegistrationConsent

interface RegistrationConsentRepository {
    suspend fun getConsentList(): List<RegistrationConsent>
}
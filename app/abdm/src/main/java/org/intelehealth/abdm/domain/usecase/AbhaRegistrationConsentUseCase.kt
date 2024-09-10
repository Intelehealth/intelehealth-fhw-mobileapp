package org.intelehealth.abdm.domain.usecase

import org.intelehealth.abdm.domain.registration.RegistrationConsentRepository
import javax.inject.Inject

class AbhaRegistrationConsentUseCase @Inject constructor(
    private val repository: RegistrationConsentRepository,
) {
    suspend operator fun invoke() = repository.getConsentList()
}
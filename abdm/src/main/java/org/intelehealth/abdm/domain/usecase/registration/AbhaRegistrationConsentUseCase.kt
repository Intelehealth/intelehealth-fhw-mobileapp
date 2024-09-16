package org.intelehealth.abdm.domain.usecase.registration

import org.intelehealth.abdm.domain.repository.registration.RegistrationConsentRepository
import javax.inject.Inject

class AbhaRegistrationConsentUseCase @Inject constructor(
    private val repository: RegistrationConsentRepository,
) {
    suspend operator fun invoke() = repository.getConsentList()
}
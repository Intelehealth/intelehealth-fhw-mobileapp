package org.intelehealth.abdm.domain.usecase.registration

import org.intelehealth.abdm.domain.model.request.EnrollAbhaAddressRequest
import org.intelehealth.abdm.domain.repository.registration.EnrollAbhaAddressRepository
import javax.inject.Inject

class EnrollAbhaAddressUseCase @Inject constructor(private val enrollAbhaAddressRepository: EnrollAbhaAddressRepository) {

    suspend operator fun invoke(
        authHeader: String,
        enrollAbhaAddressRequest: EnrollAbhaAddressRequest
    ) = enrollAbhaAddressRepository.enrollAbhaAddress(authHeader, enrollAbhaAddressRequest)
}
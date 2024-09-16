package org.intelehealth.abdm.domain.usecase.registration

import org.intelehealth.abdm.domain.model.request.AbhaAddressSuggestionRequest
import org.intelehealth.abdm.domain.repository.registration.EnrollAbhaAddressRepository
import javax.inject.Inject

class AbhaSuggestionListUseCase @Inject constructor(private val enrollAbhaAddressRepository: EnrollAbhaAddressRepository) {
    suspend operator fun invoke(
        authHeader: String,
        abhaAddressSuggestionRequest: AbhaAddressSuggestionRequest
    ) = enrollAbhaAddressRepository.getAbhaAddressSuggestionList(
        authHeader,
        abhaAddressSuggestionRequest
    )
}
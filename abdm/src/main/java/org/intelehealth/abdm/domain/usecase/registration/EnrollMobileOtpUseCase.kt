package org.intelehealth.abdm.domain.usecase.registration

import org.intelehealth.abdm.domain.model.request.EnrollMobileOtpRequest
import org.intelehealth.abdm.domain.repository.registration.EnrollAbhaAddressRepository
import javax.inject.Inject

class EnrollMobileOtpUseCase @Inject constructor(private val enrollAbhaAddressRepository: EnrollAbhaAddressRepository) {
    suspend operator fun invoke(
        authHeader: String,
        enrollMobileOtpRequest: EnrollMobileOtpRequest
    ) = enrollAbhaAddressRepository.verifyMobileOtp(authHeader, enrollMobileOtpRequest)
}
package org.intelehealth.abdm.domain.usecase.registration

import org.intelehealth.abdm.domain.model.request.SendMobileOtpRequest
import org.intelehealth.abdm.domain.repository.registration.EnrollAbhaAddressRepository
import javax.inject.Inject

class SendMobileOtpToEnrollUseCase @Inject constructor(private val enrollAbhaAddressRepository: EnrollAbhaAddressRepository) {
    suspend operator fun invoke(
        authHeader: String,
        sendMobileOtpRequest: SendMobileOtpRequest
    ) = enrollAbhaAddressRepository.sendMobileOtp(authHeader, sendMobileOtpRequest)
}
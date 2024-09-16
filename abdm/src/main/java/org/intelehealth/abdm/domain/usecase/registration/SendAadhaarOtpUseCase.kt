package org.intelehealth.abdm.domain.usecase.registration

import org.intelehealth.abdm.domain.repository.registration.RegisterAbdmRepository
import org.intelehealth.abdm.domain.model.request.SendAadhaarOtpApiRequest
import javax.inject.Inject

class SendAadhaarOtpUseCase @Inject constructor(private val registerAbdmRepository: RegisterAbdmRepository) {
    suspend operator fun invoke(authHeader: String, sendOtpApiRequest: SendAadhaarOtpApiRequest) =
        registerAbdmRepository.sendAadhaarCardOtp(authHeader, sendOtpApiRequest)
}
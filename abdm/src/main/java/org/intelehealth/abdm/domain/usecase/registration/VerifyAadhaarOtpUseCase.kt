package org.intelehealth.abdm.domain.usecase.registration

import org.intelehealth.abdm.domain.repository.registration.RegisterAbdmRepository
import org.intelehealth.abdm.domain.model.request.AadhaarOtpVerificationRequest
import javax.inject.Inject

class VerifyAadhaarOtpUseCase @Inject constructor(private val registerAbdmRepository: RegisterAbdmRepository) {
    suspend operator fun invoke(
        authHeader: String,
        aadhaarOtpVerificationRequest: AadhaarOtpVerificationRequest
    ) = registerAbdmRepository.verifyAadhaarCardOtp(authHeader, aadhaarOtpVerificationRequest)
}
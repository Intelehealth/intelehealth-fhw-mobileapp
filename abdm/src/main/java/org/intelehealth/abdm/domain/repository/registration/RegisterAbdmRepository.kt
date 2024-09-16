package org.intelehealth.abdm.domain.repository.registration

import org.intelehealth.abdm.domain.model.AadhaarOTP
import org.intelehealth.abdm.domain.model.AadhaarOtpVerification
import org.intelehealth.abdm.domain.repository.registration.request.SendAadhaarOtpApiRequest
import org.intelehealth.abdm.domain.repository.registration.request.AadhaarOtpVerificationRequest
import org.intelehealth.abdm.domain.result.ApiResult

interface RegisterAbdmRepository {
    suspend fun sendAadhaarCardOtp(authHeader : String, sendSendAadhaarOtpApiRequest: SendAadhaarOtpApiRequest): ApiResult<AadhaarOTP>
    suspend fun verifyAadhaarCardOtp(authHeader : String,aadhaarOtpVerificationRequest: AadhaarOtpVerificationRequest): ApiResult<AadhaarOtpVerification>
}
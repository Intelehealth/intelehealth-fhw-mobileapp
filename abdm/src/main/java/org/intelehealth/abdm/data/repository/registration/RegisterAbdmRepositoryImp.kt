package org.intelehealth.abdm.data.repository.registration

import org.intelehealth.abdm.data.mapper.AadhaarOTPMapper
import org.intelehealth.abdm.data.mapper.AadhaarOtpVerificationMapper
import org.intelehealth.abdm.data.network.SafeApiCall
import org.intelehealth.abdm.data.services.AbdmService
import org.intelehealth.abdm.domain.model.AadhaarOTP
import org.intelehealth.abdm.domain.model.AadhaarOtpVerification
import org.intelehealth.abdm.domain.repository.registration.RegisterAbdmRepository
import org.intelehealth.abdm.domain.repository.registration.request.SendAadhaarOtpApiRequest
import org.intelehealth.abdm.domain.repository.registration.request.AadhaarOtpVerificationRequest
import org.intelehealth.abdm.domain.result.ApiResult
import javax.inject.Inject

class RegisterAbdmRepositoryImp @Inject constructor(
    private val abdmService: AbdmService,
    private val aadhaarOTPMapper: AadhaarOTPMapper,
    private val aadhaarOtpVerificationMapper: AadhaarOtpVerificationMapper
) : RegisterAbdmRepository {

    override suspend fun sendAadhaarCardOtp(
        authHeader: String,
        sendSendAadhaarOtpApiRequest: SendAadhaarOtpApiRequest
    ): ApiResult<AadhaarOTP> {
        return SafeApiCall.call(
            { abdmService.sendAadhaarOtpRequestApi(authHeader, sendSendAadhaarOtpApiRequest) },
            { aadhaarOtpDto -> aadhaarOTPMapper.mapToDomain(aadhaarOtpDto) })

    }

    override suspend fun verifyAadhaarCardOtp(
        authHeader: String,
        aadhaarOtpVerificationRequest: AadhaarOtpVerificationRequest
    ): ApiResult<AadhaarOtpVerification> {
        return SafeApiCall.call(
            { abdmService.verifyAadhaarOtpRequestApi(authHeader, aadhaarOtpVerificationRequest) },
            { dto -> aadhaarOtpVerificationMapper.mapToDomain(dto) })

    }
}
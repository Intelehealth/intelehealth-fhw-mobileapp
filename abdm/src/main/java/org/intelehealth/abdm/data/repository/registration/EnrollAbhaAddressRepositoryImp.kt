package org.intelehealth.abdm.data.repository.registration

import org.intelehealth.abdm.data.mapper.AbhaAddressSuggestionListMapper
import org.intelehealth.abdm.data.mapper.EnrolledAbhaAddressMapper
import org.intelehealth.abdm.data.mapper.SendMobileOtpMapper
import org.intelehealth.abdm.data.mapper.VerifyMobileOtpMapper
import org.intelehealth.abdm.data.network.SafeApiCall
import org.intelehealth.abdm.data.services.AbdmService
import org.intelehealth.abdm.domain.model.AbhaAddressSuggestionList
import org.intelehealth.abdm.domain.model.EnrolledAbhaAddressDetails
import org.intelehealth.abdm.domain.model.SendMobileOtpToEnrollResponseData
import org.intelehealth.abdm.domain.model.EnrollMobileOtpResponseData
import org.intelehealth.abdm.domain.model.request.AbhaAddressSuggestionRequest
import org.intelehealth.abdm.domain.model.request.EnrollAbhaAddressRequest
import org.intelehealth.abdm.domain.model.request.SendMobileOtpRequest
import org.intelehealth.abdm.domain.model.request.EnrollMobileOtpRequest
import org.intelehealth.abdm.domain.repository.registration.EnrollAbhaAddressRepository
import org.intelehealth.abdm.domain.result.ApiResult
import javax.inject.Inject

class EnrollAbhaAddressRepositoryImp @Inject constructor(
    private val abdmService: AbdmService,
    private val abhaAddressSuggestionListMapper: AbhaAddressSuggestionListMapper,
    private val enrolledAbhaAddressMapper: EnrolledAbhaAddressMapper,
    private val sendMobileOtpMapper: SendMobileOtpMapper,
    private val verifyMobileOtpMapper: VerifyMobileOtpMapper,
) : EnrollAbhaAddressRepository {
    override suspend fun getAbhaAddressSuggestionList(
        authHeader: String, abhaAddressSuggestionRequest: AbhaAddressSuggestionRequest
    ): ApiResult<AbhaAddressSuggestionList> {
        return SafeApiCall.call({
            abdmService.getAbhaAddressSuggestionListApi(
                authHeader, abhaAddressSuggestionRequest
            )
        }, { response -> abhaAddressSuggestionListMapper.toDomain(response) })

    }

    override suspend fun enrollAbhaAddress(
        authHeader: String, enrollAbhaAddressRequest: EnrollAbhaAddressRequest
    ): ApiResult<EnrolledAbhaAddressDetails> {
        return SafeApiCall.call({
            abdmService.setPreferredAbhaAddressApi(
                authHeader,
                enrollAbhaAddressRequest
            )
        },
            { response -> enrolledAbhaAddressMapper.toDomain(response) })
    }

    override suspend fun sendMobileOtp(
        authHeader: String,
        sendMobileOtpRequest: SendMobileOtpRequest
    ): ApiResult<SendMobileOtpToEnrollResponseData> {
        return SafeApiCall.call({
            abdmService.getMobileOtp(
                authHeader,
                sendMobileOtpRequest
            )
        },
            { response -> sendMobileOtpMapper.toDomain(response) })

    }

    override suspend fun verifyMobileOtp(
        authHeader: String,
        enrollMobileOtpRequest: EnrollMobileOtpRequest
    ): ApiResult<EnrollMobileOtpResponseData> {
        return SafeApiCall.call({
            abdmService.verifyMobileOtp(
                authHeader,
                enrollMobileOtpRequest
            )
        },
            { response -> verifyMobileOtpMapper.toDomain(response) })
    }
}
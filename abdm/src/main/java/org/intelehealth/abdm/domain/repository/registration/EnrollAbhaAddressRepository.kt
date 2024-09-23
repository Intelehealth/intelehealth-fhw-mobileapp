package org.intelehealth.abdm.domain.repository.registration

import org.intelehealth.abdm.domain.model.AbhaAddressSuggestionList
import org.intelehealth.abdm.domain.model.EnrolledAbhaAddressDetails
import org.intelehealth.abdm.domain.model.SendMobileOtpToEnrollResponseData
import org.intelehealth.abdm.domain.model.EnrollMobileOtpResponseData
import org.intelehealth.abdm.domain.model.request.AbhaAddressSuggestionRequest
import org.intelehealth.abdm.domain.model.request.EnrollAbhaAddressRequest
import org.intelehealth.abdm.domain.model.request.SendMobileOtpRequest
import org.intelehealth.abdm.domain.model.request.EnrollMobileOtpRequest
import org.intelehealth.abdm.domain.result.ApiResult

interface EnrollAbhaAddressRepository {
    suspend fun getAbhaAddressSuggestionList(
        authHeader: String,
        abhaAddressSuggestionRequest: AbhaAddressSuggestionRequest
    ): ApiResult<AbhaAddressSuggestionList>

    suspend fun enrollAbhaAddress(
        authHeader: String,
        enrollAbhaAddressRequest: EnrollAbhaAddressRequest
    ): ApiResult<EnrolledAbhaAddressDetails>

    suspend fun sendMobileOtp(
        authHeader: String,
        sendMobileOtpRequest: SendMobileOtpRequest
    ): ApiResult<SendMobileOtpToEnrollResponseData>

    suspend fun verifyMobileOtp(
        authHeader: String,
        enrollMobileOtpRequest: EnrollMobileOtpRequest
    ): ApiResult<EnrollMobileOtpResponseData>

}
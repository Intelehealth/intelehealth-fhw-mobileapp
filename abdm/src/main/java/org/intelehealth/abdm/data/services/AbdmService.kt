package org.intelehealth.abdm.data.services

import org.intelehealth.abdm.domain.model.request.SendAadhaarOtpApiRequest
import org.intelehealth.abdm.data.model.AadhaarOTPResponseDto
import org.intelehealth.abdm.domain.model.request.AadhaarOtpVerificationRequest
import org.intelehealth.abdm.data.model.AadhaarOtpVerificationResponseDto
import org.intelehealth.abdm.data.model.AbhaAddressSuggestionListDto
import org.intelehealth.abdm.data.model.AuthTokenResponseDto
import org.intelehealth.abdm.data.model.EnrolledAbhaAddressDto
import org.intelehealth.abdm.data.model.SendMobileOtpDto
import org.intelehealth.abdm.data.model.VerifyMobileOtpResponseDto
import org.intelehealth.abdm.domain.model.request.AbhaAddressSuggestionRequest
import org.intelehealth.abdm.domain.model.request.EnrollAbhaAddressRequest
import org.intelehealth.abdm.domain.model.request.SendMobileOtpRequest
import org.intelehealth.abdm.domain.model.request.EnrollMobileOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AbdmService {
    @GET("/abha/getToken")
    suspend fun getAuthTokenApi(): Response<AuthTokenResponseDto>

    @POST("/abha/enrollOTPReq")
    suspend fun sendAadhaarOtpRequestApi(
        @Header("Authorization") accessToken: String,
        @Body sendAadhaarOtpApiRequest: SendAadhaarOtpApiRequest
    ): Response<AadhaarOTPResponseDto>

    @POST("/abha/enrollByAadhar")
    suspend fun verifyAadhaarOtpRequestApi(
        @Header("Authorization") accessToken: String,
        @Body aadhaarOtpVerificationRequest: AadhaarOtpVerificationRequest
    ): Response<AadhaarOtpVerificationResponseDto>

    @POST("/abha/enrollOTPReq")
    suspend fun getMobileOtp(
        @Header("Authorization") accessToken: String,
        @Body sendMobileOtpRequest: SendMobileOtpRequest
    ): Response<SendMobileOtpDto>

    @POST("/abha/enrollByAbdm")
    suspend fun verifyMobileOtp(
        @Header("Authorization") accessToken: String,
        @Body enrollMobileOtpRequest: EnrollMobileOtpRequest
    ): Response<VerifyMobileOtpResponseDto>

    @POST("/abha/enrollSuggestion")
    suspend fun getAbhaAddressSuggestionListApi(
        @Header("Authorization") accessToken: String,
        @Body abhaAddressSuggestionRequest: AbhaAddressSuggestionRequest
    ): Response<AbhaAddressSuggestionListDto>

    @POST("/abha/setPreferredAddress")
    suspend fun setPreferredAbhaAddressApi(
        @Header("Authorization") accessToken: String,
        @Body enrollAbhaAddressRequest: EnrollAbhaAddressRequest
    ): Response<EnrolledAbhaAddressDto>


}
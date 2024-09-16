package org.intelehealth.abdm.data.services

import org.intelehealth.abdm.domain.repository.registration.request.SendAadhaarOtpApiRequest
import org.intelehealth.abdm.data.model.AadhaarOTPResponseDto
import org.intelehealth.abdm.domain.repository.registration.request.AadhaarOtpVerificationRequest
import org.intelehealth.abdm.data.model.AadhaarOtpVerificationResponseDto
import org.intelehealth.abdm.data.model.AuthTokenResponseDto
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

}
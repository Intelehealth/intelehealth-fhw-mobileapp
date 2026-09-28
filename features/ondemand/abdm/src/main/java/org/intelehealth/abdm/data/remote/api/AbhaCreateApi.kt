package org.intelehealth.abdm.data.remote.api

import org.intelehealth.abdm.data.remote.dto.AadhaarRequestDto
import org.intelehealth.abdm.data.remote.dto.OtpResponseDto
import org.intelehealth.abdm.data.remote.dto.RegisterNumberWithAbdmRequestDto
import org.intelehealth.abdm.data.remote.dto.VerifyOtpRequestDto
import org.intelehealth.abdm.data.remote.dto.VerifyOtpResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AbhaCreateApi {
    @POST("abha/enrollByAbdm")
    suspend fun verifyMobileForEnrollment(
        @Body apiRequest: RegisterNumberWithAbdmRequestDto,
    ): Response<OtpResponseDto>

    @POST("abha/enrollOTPReq")
    suspend fun requestAadhaarOtp(
        @Body body: AadhaarRequestDto,
    ): Response<OtpResponseDto>

    @POST("abha/enrollByAadhar")
    suspend fun verifyAadhaarOtp(
        @Body body: VerifyOtpRequestDto,
    ): Response<VerifyOtpResponseDto>
}
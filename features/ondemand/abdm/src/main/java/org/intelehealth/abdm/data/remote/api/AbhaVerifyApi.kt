package org.intelehealth.abdm.data.remote.api

import org.intelehealth.abdm.data.remote.dto.FetchAuthModesRequestDto
import org.intelehealth.abdm.data.remote.dto.FetchAuthModesResponseDto
import org.intelehealth.abdm.data.remote.dto.MobileOtpRequestDto
import org.intelehealth.abdm.data.remote.dto.OtpResponseDto
import org.intelehealth.abdm.data.remote.dto.SearchProfileRequestDto
import org.intelehealth.abdm.data.remote.dto.SearchProfileResponseDto
import org.intelehealth.abdm.data.remote.dto.VerifyMobileOtpResponseDto
import org.intelehealth.abdm.data.remote.dto.VerifyOtpRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AbhaVerifyApi {
    @POST("abha/searchAbhaProfiles")
    suspend fun fetchAbhaProfiles(
        @Body request: SearchProfileRequestDto,
    ): Response<Map<String, SearchProfileResponseDto>>

    @POST("abha/loginOTPReq")
    suspend fun requestMobileOtp(
        @Body body: MobileOtpRequestDto,
    ): Response<OtpResponseDto>

    @POST("abha/loginOTPVerify")
    suspend fun verifyLoginOtp(
        @Body body: VerifyOtpRequestDto,
    ): Response<VerifyMobileOtpResponseDto>

    @POST("abha/fetchAuthModes")
    suspend fun fetchAuthModes(
        @Header("X-TOKEN") xToken: String,
        @Body body: FetchAuthModesRequestDto,
    ): Response<FetchAuthModesResponseDto>
}
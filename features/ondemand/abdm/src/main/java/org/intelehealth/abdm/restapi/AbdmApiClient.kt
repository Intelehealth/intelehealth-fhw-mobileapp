package org.intelehealth.abdm.restapi

import io.reactivex.Single
import okhttp3.ResponseBody
import org.intelehealth.abdm.model.AadharApiBody
import org.intelehealth.abdm.model.EnrollNumberWithABDMRequest
import org.intelehealth.abdm.model.EnrollSuggestionRequestBody
import org.intelehealth.abdm.model.EnrollSuggestionResponse
import org.intelehealth.abdm.model.OTPResponse
import org.intelehealth.abdm.model.OTPVerificationRequestBody
import org.intelehealth.abdm.model.OTPVerificationResponse
import org.intelehealth.abdm.model.TokenResponse
import org.intelehealth.abdm.model.UpdateIdentifierReqBody
import org.intelehealth.app.abdm.model.ExistUserStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url


interface AbdmApiClient {

    @GET("/abha/getToken")
    fun getToken(): Single<TokenResponse>

    @POST("/abha/enrollOTPReq")
    fun getOtpForAadhar(
        @Header("Authorization") accessToken: String,
        @Body body: AadharApiBody
    ): Single<Response<OTPResponse>>

    @POST("/abha/enrollByAadhar")
    fun pushOtpForVerification(
        @Header("Authorization") accessToken: String,
        @Body body: OTPVerificationRequestBody
    ): Single<Response<OTPVerificationResponse>>

    @POST("/abha/enrollSuggestion")
    fun enrollAbhaAddressSuggestion(
        @Header("Authorization") accessToken: String,
        @Body body: EnrollSuggestionRequestBody
    ): Single<EnrollSuggestionResponse>

    @POST("/abha/enrollByAbdm")
    fun enrolNumberWithAbdm(
        @Header("Authorization") accessToken: String,
        @Body apiRequest: EnrollNumberWithABDMRequest
    ): Single<Response<OTPResponse>>

    @GET
    fun checkExistingUser(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Single<ExistUserStatusResponse>

    @POST("/openmrs/ws/rest/v1/patient//{mExistingPatientUuid}/identifier")
    fun updatePatientIdentifier(
        @Path("mExistingPatientUuid") mExistingPatientUuid: String,
        @Header("Authorization") authHeader: String,
        @Body body: UpdateIdentifierReqBody
    ): Single<Response<ResponseBody>>
}
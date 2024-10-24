package org.intelehealth.core.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.intelehealth.core.network.model.Location
import org.intelehealth.core.network.model.LoginModel
import org.intelehealth.core.network.model.PushRequestApiCall
import org.intelehealth.core.network.model.Resource
import org.intelehealth.core.network.model.ResponseModel
import org.intelehealth.core.network.model.Results
import org.intelehealth.coreroomdb.entity.ObsJsonResponse
import org.intelehealth.coreroomdb.entity.Observation
import org.intelehealth.coreroomdb.entity.PatientProfile
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Created by - Prajwal W. on 10/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

interface CoreApiClient {

    @GET("location?tag=Login%20Location")
    suspend fun fetchLocationList(
        @Query("v") representation: String
    ): Response<Results<Location>>

    //EMR-Middleware/webapi/pull/pulldata/
    @GET
    suspend fun pullApiDetails(
        @Url url: String, @Header("Authorization") authHeader: String
    ): Response<ResponseModel>

    @GET
    suspend fun fetchLoginDetails(
        @Url url: String, @Header("Authorization") authHeader: String
    ): Response<LoginModel>

    @GET
    suspend fun fetchLoginProviderDetails(
        @Url url: String, @Header("Authorization") authHeader: String
    ): Response<List<Resource>>

    @POST
    @Headers("Accept: application/json")
    suspend fun pushApiDetails(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body pushRequestApiCall: PushRequestApiCall
    ): Response<PushRequestApiCall>

    @GET
    suspend fun downloadPersonProfilePicture(
        @Url url: String, @Header("Authorization") authHeader: String
    ): Response<ResponseBody>

    @POST
    suspend fun uploadPersonProfilePicture(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body patientProfile: PatientProfile
    ): Response<ResponseBody>

    // Obs Image Download
    @GET
    suspend fun obsImageDownload(
        @Url url: String, @Header("Authorization") authHeader: String
    ): Response<ResponseBody>


    // Fetch obs response in json format.
    @POST
    @Multipart
    @Headers("Accept: application/json")
    suspend fun fetchObsJsonResponse(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Part image: MultipartBody.Part,
        @Part("json") obsJsonRequest: Observation
    ): Response<ObsJsonResponse>

    @DELETE
    suspend fun deleteObsImage(@Url url: String, @Header("Authorization") authHeader: String)

    @GET("/api/mindmap/download")
    suspend fun downloadMindMap(@Query("key") licenseKey: String): Response<DownloadMindMapRes>

    @GET("/intelehealth/app_update.json")
    suspend fun checkAppUpdate(): Response<CheckAppUpdateRes>

    @POST("/openmrs/ws/rest/v1/password")
    suspend fun changePassword(
        @Body modelChangePassword: ChangePasswordModel_New,
        @Header("Authorization") authHeader: String
    ): Response<ResponseBody>

    @POST("/api/auth/requestOtp")
    @Headers("Accept: application/json")
    suspend fun requestOTP(
        @Body modelRequestOTPParams: RequestOTPParamsModel_New
    ): Response<ForgotPasswordApiResponseModel_New>

    @POST("/api/auth/verifyOtp")
    @Headers("Accept: application/json")
    suspend fun verifyOTP(
        @Body modelOtpVerificationParams: OTPVerificationParamsModel_New
    ): Response<ForgotPasswordApiResponseModel_New>


    @POST("/api/auth/resetPassword/{userUuid}")
    suspend fun resetPassword(
        @Path("userUuid") userUuid: String, @Body modelChangePassword: ChangePasswordParamsModel_New
    ): Response<ResetPasswordResModel_New>


    @POST
    suspend fun uploadProviderProfilePicture(
        @Url url: String,
        @Body providerProfile: ProviderProfile,
        @Header("Authorization") authHeader: String
    ): Response<ResponseBody>


    @GET
    suspend fun downloadProviderProfilePicture(
        @Url url: String, @Header("Authorization") authHeader: String
    ): Response<ResponseBody>

    /*@GET
    Observable<Profile> PROVIDER_PROFILE_DETAILS_DOWNLOAD(@Url String url,
    @Header("Authorization") String authHeader);*/

    @GET
    suspend fun downloadProviderProfileDetails(
        @Url url: String, @Header("Authorization") authHeader: String
    ): Response<Profile>


    @POST("/openmrs/ws/rest/v1/person/{userUuid}")
    suspend fun updateProfileAge(
        @Path("userUuid") userUuid: String,
        @Body profileUpdateAge: ProfileUpdateAge,
        @Header("Authorization") authHeader: String
    ): Response<ResponseBody>


    @POST("/openmrs/ws/rest/v1/provider/{userUuid}/attribute")
    suspend fun createProfileAttribute(
        @Path("userUuid") userUuid: String,
        @Body profileCreateAttribute: ProfileCreateAttribute,
        @Header("Authorization") authHeader: String
    ): Response<ResponseBody>


    @POST("attribute/{attributeUuid}")
    suspend fun updateProfileAttribute(
        @Path("attributeUuid") attributeUuid: String,
        @Body profileUpdateAttribute: ProfileUpdateAttribute,
        @Header("Authorization") authHeader: String
    ): Response<ResponseBody>


    @POST
    @Headers("Accept: application/json")
    suspend fun authLoginJwtApi(
        @Url url: String, @Body authJWTBody: AuthJWTBody
    ): Response<AuthJWTResponse>


    /**
     * getting html here like privacy policy, terms of service
     *
     * @param url
     * @return
     */
    @GET
    suspend fun getHtml(@Url url: String): Response<ResponseBody>


    @DELETE("/api/mindmap/clearAll/{id}")
    suspend fun clearAllNotifications(
        @Header("Authorization") authHeader: String, @Path("id") id: String
    ): Response<ResponseBody>


    @PUT("/api/mindmap/acknowledge/{id}")
    suspend fun notificationsAcknowledge(
        @Header("Authorization") authHeader: String, @Path("id") id: String
    ): Response<ResponseBody>


    @GET("/api/mindmap/notifications")
    suspend fun fetchAllNotifications(
        @Header("Authorization") authHeader: String,
        @Query("userId") userId: String,
        @Query("page") page: String,
        @Query("size") size: String
    ): Response<NotificationResponse>

}
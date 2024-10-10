package org.intelehealth.core.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
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

//District-wise location
@GET("location/{state_uuid}?&v=custom:(uuid,display,childLocations:(uuid,display))")
fun DISTRICT_SANCH_VILLAGE_OBSERVABLE(
    @Path("state_uuid") state_uuid: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<District_Sanch_Village?>?

@GET("location?tag=Login%20Location")
fun LOCATION_OBSERVABLE(@Query("v") representation: String?): io.reactivex.Observable<Results<org.intelehealth.app.models.Location?>?>?


@DELETE
fun DELETE_ENCOUNTER(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): Call<Void?>?

//EMR-Middleware/webapi/pull/pulldata/
@GET
fun RESPONSE_DTO_CALL(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): Call<ResponseDTO?>?

@GET
fun LOGIN_MODEL_OBSERVABLE(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<LoginModel?>?


@GET
fun LOGIN_PROVIDER_MODEL_OBSERVABLE(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<LoginProviderModel?>?

@Headers("Accept: application/json")
@POST
fun PUSH_RESPONSE_API_CALL_OBSERVABLE(
    @Url url: String?,
    @Header("Authorization") authHeader: String?,
    @Body pushRequestApiCall: PushRequestApiCall?
): io.reactivex.Single<PushResponseApiCall?>?

@GET
fun PERSON_PROFILE_PIC_DOWNLOAD(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@POST
fun PERSON_PROFILE_PIC_UPLOAD(
    @Url url: String?,
    @Header("Authorization") authHeader: String?,
    @Body patientProfile: PatientProfile?
): io.reactivex.Single<ResponseBody?>?

@GET
fun OBS_IMAGE_DOWNLOAD(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@Headers("Accept: application/json")
@POST
@Multipart
fun OBS_JSON_RESPONSE_OBSERVABLE(
    @Url url: String?,
    @Header("Authorization") authHeader: String?,
    @Part image: MultipartBody.Part?,
    @Part("json") obsJsonRequest: ObsPushDTO?
): io.reactivex.Observable<ObsJsonResponse?>?

@DELETE
fun DELETE_OBS_IMAGE(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<Void?>?


@GET("/api/mindmap/download")
fun DOWNLOAD_MIND_MAP_RES_OBSERVABLE(@Query("key") licenseKey: String?): io.reactivex.Observable<DownloadMindMapRes?>?

@GET("/intelehealth/app_update.json")
fun checkAppUpdate(): io.reactivex.Single<CheckAppUpdateRes?>?

@Headers("Accept: application/json")
@POST
fun END_VISIT_RESPONSE_BODY_OBSERVABLE(
    @Url url: String?,
    @Body endVisitEncounterPrescription: EndVisitEncounterPrescription?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<EndVisitResponseBody?>?

@POST
fun OBS_PRESCRIPTION_UPLOAD(
    @Url url: String?,
    @Body prescription: ObsPrescription?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ObsPrescResponse?>?

@Headers("Accept: application/json")
@POST
fun OBS_SIGNANDSUBMIT_STATUS(
    @Url url: String?,
    @Body prescription: EndVisitEncounterPrescription?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@DELETE
fun DELETE_PRESCOBS_ITEM(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<Response<Void?>?>?


@POST("/openmrs/ws/rest/v1/password")
fun CHANGE_PASSWORD_OBSERVABLE(
    @Body changePasswordParamsModel_new: ChangePasswordModel_New?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@Headers("Accept: application/json")
@POST("/api/auth/requestOtp")
fun REQUEST_OTP_OBSERVABLE(@Body requestOTPParamsModel_new: RequestOTPParamsModel_New?): io.reactivex.Observable<ForgotPasswordApiResponseModel_New?>?

@Headers("Accept: application/json")
@POST("/api/auth/verifyOtp")
fun VERFIY_OTP_OBSERVABLE(@Body OTPVerificationParamsModel_new: OTPVerificationParamsModel_New?): io.reactivex.Observable<ForgotPasswordApiResponseModel_New?>?

@POST("api/openmrs/forgetPassword/resetPassword/{userUuid}")
fun resetPassword(
    @Path("userUuid") userUuid: String?,
    @Body changePasswordParamsModel_new: ChangePasswordParamsModel_New?,
    @Header("Authorization") authHeader: String?
): Call<ResetPasswordResModel_New?>?

@POST("/api/auth/resetPassword/{userUuid}")
fun RESET_PASSWORD_OBSERVABLE(
    @Path("userUuid") userUuid: String?,
    @Body changePasswordParamsModel_new: ChangePasswordParamsModel_New?
): io.reactivex.Observable<ResetPasswordResModel_New?>?


@POST
fun PROVIDER_PROFILE_PIC_UPLOAD(
    @Url url: String?,
    @Body patientProfile: ProviderProfile?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Single<ResponseBody?>?


@GET
fun PROVIDER_PROFILE_PIC_DOWNLOAD(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@GET
fun PROVIDER_PROFILE_DETAILS_DOWNLOAD(
    @Url url: String?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<org.intelehealth.app.models.hwprofile.Profile?>?

@POST("/openmrs/ws/rest/v1/person/{userUuid}")
fun PROFILE_AGE_UPDATE(
    @Path("userUuid") userUuid: String?,
    @Body profileUpdateAge: ProfileUpdateAge?, @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@POST("/openmrs/ws/rest/v1/provider/{userUuid}/attribute")
fun PROFILE_ATTRIBUTE_CREATE(
    @Path("userUuid") userUuid: String?,
    @Body profileCreateAttribute: ProfileCreateAttribute?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@POST("attribute/{attributeUuid}")
fun PROFILE_ATTRIBUTE_UPDATE(
    @Path("attributeUuid") attributeUuid: String?,
    @Body profileUpdateAttribute: ProfileUpdateAttribute?,
    @Header("Authorization") authHeader: String?
): io.reactivex.Observable<ResponseBody?>?

@Headers("Accept: application/json")
@POST
fun AUTH_LOGIN_JWT_API(
    @Url url: String?,
    @Body authJWTBody: AuthJWTBody?
): io.reactivex.Observable<AuthJWTResponse?>?

/**
 * getting html here like privacy policy, terms of service
 *
 * @param url
 * @return
 */
@GET
fun GET_HTML(@Url url: String?): io.reactivex.Observable<ResponseBody?>?

@DELETE("/api/mindmap/clearAll/{id}")
fun clearAllNotifications(
    @Header("Authorization") authHeader: String?,
    @Path("id") id: String?
): io.reactivex.Single<ResponseBody?>?

@PUT("/api/mindmap/acknowledge/{id}")
fun notificationsAcknowledge(
    @Header("Authorization") authHeader: String?,
    @Path("id") id: String?
): io.reactivex.Single<ResponseBody?>?

@GET("/api/mindmap/notifications")
fun fetchAllNotifications(
    @Header("Authorization") authHeader: String?,
    @Query("userId") userId: String?,
    @Query("page") page: String?,
    @Query("size") size: String?
): io.reactivex.Single<NotificationResponse?>?
}
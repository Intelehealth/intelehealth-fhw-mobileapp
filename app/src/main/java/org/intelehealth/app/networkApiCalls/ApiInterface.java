package org.intelehealth.app.networkApiCalls;


import com.google.gson.JsonObject;

import org.intelehealth.app.activities.notification.NotificationResponse;
import org.intelehealth.app.activities.notification.result.NotificationResult;
import org.intelehealth.app.models.ChangePasswordModel_New;
import org.intelehealth.app.models.ChangePasswordParamsModel_New;
import org.intelehealth.app.models.CheckAppUpdateRes;
import org.intelehealth.app.models.DownloadMindMapRes;
import org.intelehealth.app.models.ForgotPasswordApiResponseModel_New;
import org.intelehealth.app.models.Location;
import org.intelehealth.app.models.OTPVerificationParamsModel_New;
import org.intelehealth.app.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.app.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.app.models.RequestOTPParamsModel_New;
import org.intelehealth.app.models.ResetPasswordResModel_New;
import org.intelehealth.app.models.Results;
import org.intelehealth.app.models.dto.ResponseDTO;
import org.intelehealth.app.models.hwprofile.Profile;
import org.intelehealth.app.models.hwprofile.ProfileCreateAttribute;
import org.intelehealth.app.models.hwprofile.ProfileUpdateAge;
import org.intelehealth.app.models.hwprofile.ProfileUpdateAttribute;
import org.intelehealth.app.models.loginModel.LoginModel;
import org.intelehealth.app.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.app.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.app.models.prescriptionUpload.EndVisitEncounterPrescription;
import org.intelehealth.app.models.prescriptionUpload.EndVisitResponseBody;
import org.intelehealth.app.models.prescriptionUpload.ObsPrescResponse;
import org.intelehealth.app.models.prescriptionUpload.ObsPrescription;
import org.intelehealth.app.models.providerImageRequestModel.ProviderProfile;
import org.intelehealth.app.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.app.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.app.models.statewise_location.District_Sanch_Village;
import org.intelehealth.app.models.statewise_location.State;
import org.intelehealth.app.utilities.authJWT_API.AuthJWTBody;
import org.intelehealth.app.utilities.authJWT_API.AuthJWTResponse;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {


    //State-wise location
    @GET("location?tag=State&v=custom:(uuid,display)")
    Observable<State> STATE_OBSERVABLE();

    //District-wise location
    @GET("location/{state_uuid}?&v=custom:(uuid,display,childLocations:(uuid,display))")
    Observable<District_Sanch_Village> DISTRICT_SANCH_VILLAGE_OBSERVABLE(@Path("state_uuid") String state_uuid, @Header("Authorization") String authHeader);

    @GET("location?tag=Login%20Location")
    Observable<Results<Location>> LOCATION_OBSERVABLE(@Query("v") String representation);


    @DELETE
    Call<Void> DELETE_ENCOUNTER(@Url String url,
                                @Header("Authorization") String authHeader);

    //EMR-Middleware/webapi/pull/pulldata/
    @GET
    Call<ResponseDTO> RESPONSE_DTO_CALL(@Url String url,
                                        @Header("Authorization") String authHeader);

    @GET
    Observable<LoginModel> LOGIN_MODEL_OBSERVABLE(@Url String url,
                                                  @Header("Authorization") String authHeader);


    @GET
    Observable<LoginProviderModel> LOGIN_PROVIDER_MODEL_OBSERVABLE(@Url String url,
                                                                   @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    Single<PushResponseApiCall> PUSH_RESPONSE_API_CALL_OBSERVABLE(@Url String url,
                                                                  @Header("Authorization") String authHeader,
                                                                  @Body PushRequestApiCall pushRequestApiCall);

    @GET
    Observable<ResponseBody> PERSON_PROFILE_PIC_DOWNLOAD(@Url String url,
                                                         @Header("Authorization") String authHeader);

    @POST
    Single<ResponseBody> PERSON_PROFILE_PIC_UPLOAD(@Url String url,
                                                   @Header("Authorization") String authHeader,
                                                   @Body PatientProfile patientProfile);

    @GET
    Observable<ResponseBody> OBS_IMAGE_DOWNLOAD(@Url String url,
                                                @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    @Multipart
    Observable<ObsJsonResponse> OBS_JSON_RESPONSE_OBSERVABLE(@Url String url,
                                                             @Header("Authorization") String authHeader,
                                                             @Part MultipartBody.Part image,
                                                             @Part("json") ObsPushDTO obsJsonRequest);

    @DELETE
    Observable<Void> DELETE_OBS_IMAGE(@Url String url, @Header("Authorization") String authHeader);


    @GET("/api/mindmap/download")
    Observable<DownloadMindMapRes> DOWNLOAD_MIND_MAP_RES_OBSERVABLE(@Query("key") String licenseKey,@Header("Authorization") String authHeader);

    @GET("/intelehealth/app_update.json")
    Single<CheckAppUpdateRes> checkAppUpdate();

    @Headers({"Accept: application/json"})
    @POST
    Observable<EndVisitResponseBody> END_VISIT_RESPONSE_BODY_OBSERVABLE(
            @Url String url,
            @Body EndVisitEncounterPrescription endVisitEncounterPrescription,
            @Header("Authorization") String authHeader);

    @POST
    Observable<ObsPrescResponse> OBS_PRESCRIPTION_UPLOAD
            (@Url String url,
             @Body ObsPrescription prescription,
             @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    Observable<ResponseBody> OBS_SIGNANDSUBMIT_STATUS(
            @Url String url,
            @Body EndVisitEncounterPrescription prescription,
            @Header("Authorization") String authHeader);

    @DELETE
    Observable<Response<Void>> DELETE_PRESCOBS_ITEM(
            @Url String url,
            @Header("Authorization") String authHeader);


    @POST("/openmrs/ws/rest/v1/password")
    Observable<ResponseBody> CHANGE_PASSWORD_OBSERVABLE(@Body ChangePasswordModel_New changePasswordParamsModel_new,
                                                        @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST("/api/auth/requestOtp")
    Observable<ForgotPasswordApiResponseModel_New> REQUEST_OTP_OBSERVABLE(@Body RequestOTPParamsModel_New requestOTPParamsModel_new);

    @Headers({"Accept: application/json"})
    @POST("/api/auth/verifyOtp")
    Observable<ForgotPasswordApiResponseModel_New> VERFIY_OTP_OBSERVABLE(@Body OTPVerificationParamsModel_New OTPVerificationParamsModel_new);

    @POST("api/openmrs/forgetPassword/resetPassword/{userUuid}")
    Call<ResetPasswordResModel_New> resetPassword(@Path("userUuid") String userUuid,
                                                  @Body ChangePasswordParamsModel_New changePasswordParamsModel_new,
                                                  @Header("Authorization") String authHeader);

    @POST("/api/auth/resetPassword/{userUuid}")
    Observable<ResetPasswordResModel_New> RESET_PASSWORD_OBSERVABLE(@Path("userUuid") String userUuid,
                                                                    @Body ChangePasswordParamsModel_New changePasswordParamsModel_new);


    @POST
    Single<ResponseBody> PROVIDER_PROFILE_PIC_UPLOAD(@Url String url,
                                                     @Body ProviderProfile patientProfile,
                                                     @Header("Authorization") String authHeader);


    @GET
    Observable<ResponseBody> PROVIDER_PROFILE_PIC_DOWNLOAD(@Url String url,
                                                           @Header("Authorization") String authHeader);

    @GET
    Observable<Profile> PROVIDER_PROFILE_DETAILS_DOWNLOAD(@Url String url,
                                                          @Header("Authorization") String authHeader);

    @POST("/openmrs/ws/rest/v1/person/{userUuid}")
    Observable<ResponseBody> PROFILE_AGE_UPDATE(@Path("userUuid") String userUuid,
                                                @Body ProfileUpdateAge profileUpdateAge, @Header("Authorization") String authHeader);

    @POST("/openmrs/ws/rest/v1/provider/{userUuid}/attribute")
    Observable<ResponseBody> PROFILE_ATTRIBUTE_CREATE(@Path("userUuid") String userUuid,
                                                      @Body ProfileCreateAttribute profileCreateAttribute, @Header("Authorization") String authHeader);

    @POST("attribute/{attributeUuid}")
    Observable<ResponseBody> PROFILE_ATTRIBUTE_UPDATE(@Path("attributeUuid") String attributeUuid,
                                                      @Body ProfileUpdateAttribute profileUpdateAttribute, @Header("Authorization") String authHeader);

    @Headers({"Accept: application/json"})
    @POST
    Observable<AuthJWTResponse> AUTH_LOGIN_JWT_API(
            @Url String url,
            @Body AuthJWTBody authJWTBody
    );

    /**
     * getting html here like privacy policy, terms of service
     *
     * @param url
     * @return
     */

    @GET
    Observable<ResponseBody> GET_HTML(@Url String url);

    @DELETE("/api/mindmap/clearAll/{id}")
    Single<ResponseBody> clearAllNotifications(@Header("Authorization") String authHeader, @Path("id") String id);

    @PUT("/api/mindmap/acknowledge/{id}")
    Single<ResponseBody> notificationsAcknowledge(@Header("Authorization") String authHeader, @Path("id") String id);

    @GET("/api/mindmap/notifications")
    Single<NotificationResponse> fetchAllNotifications(@Header("Authorization") String authHeader, @Query("userId") String userId, @Query("page") String page, @Query("size") String size);
}

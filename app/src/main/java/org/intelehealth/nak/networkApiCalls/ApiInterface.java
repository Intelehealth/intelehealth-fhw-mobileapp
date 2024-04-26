package org.intelehealth.nak.networkApiCalls;


import org.intelehealth.nak.models.ChangePasswordModel_New;
import org.intelehealth.nak.models.ChangePasswordParamsModel_New;
import org.intelehealth.nak.models.CheckAppUpdateRes;
import org.intelehealth.nak.models.DownloadMindMapRes;
import org.intelehealth.nak.models.ForgotPasswordApiResponseModel_New;
import org.intelehealth.nak.models.Location;
import org.intelehealth.nak.models.OTPVerificationParamsModel_New;
import org.intelehealth.nak.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.nak.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.nak.models.RequestOTPParamsModel_New;
import org.intelehealth.nak.models.ResetPasswordResModel_New;
import org.intelehealth.nak.models.Results;
import org.intelehealth.nak.models.dto.ResponseDTO;
import org.intelehealth.nak.models.hwprofile.Profile;
import org.intelehealth.nak.models.hwprofile.ProfileCreateAttribute;
import org.intelehealth.nak.models.hwprofile.ProfileUpdateAge;
import org.intelehealth.nak.models.hwprofile.ProfileUpdateAttribute;
import org.intelehealth.nak.models.loginModel.LoginModel;
import org.intelehealth.nak.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.nak.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.nak.models.prescriptionUpload.EndVisitEncounterPrescription;
import org.intelehealth.nak.models.prescriptionUpload.EndVisitResponseBody;
import org.intelehealth.nak.models.prescriptionUpload.ObsPrescResponse;
import org.intelehealth.nak.models.prescriptionUpload.ObsPrescription;
import org.intelehealth.nak.models.providerImageRequestModel.ProviderProfile;
import org.intelehealth.nak.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.nak.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.nak.models.statewise_location.District_Sanch_Village;
import org.intelehealth.nak.models.statewise_location.State;

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
    Observable<DownloadMindMapRes> DOWNLOAD_MIND_MAP_RES_OBSERVABLE(@Query("key") String licenseKey);

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
}

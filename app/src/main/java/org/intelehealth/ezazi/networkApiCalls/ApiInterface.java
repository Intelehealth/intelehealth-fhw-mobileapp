package org.intelehealth.ezazi.networkApiCalls;


import org.intelehealth.ezazi.core.ApiResponse;
import org.intelehealth.ezazi.models.CheckAppUpdateRes;
import org.intelehealth.ezazi.models.DownloadMindMapRes;
import org.intelehealth.ezazi.models.Location;
import org.intelehealth.ezazi.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.ezazi.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.ezazi.models.OxytocinResponseModel;
import org.intelehealth.ezazi.models.Results;
import org.intelehealth.ezazi.models.dto.ResponseDTO;
import org.intelehealth.ezazi.models.loginModel.LoginModel;
import org.intelehealth.ezazi.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.ezazi.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.ezazi.models.prescriptionUpload.EndVisitEncounterPrescription;
import org.intelehealth.ezazi.models.prescriptionUpload.EndVisitResponseBody;
import org.intelehealth.ezazi.models.prescriptionUpload.ObsPrescResponse;
import org.intelehealth.ezazi.models.prescriptionUpload.ObsPrescription;
import org.intelehealth.ezazi.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.ezazi.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.ezazi.models.statewise_location.District_Sanch_Village;
import org.intelehealth.ezazi.models.statewise_location.State;
import org.intelehealth.ezazi.ui.rtc.model.UserToken;

import org.intelehealth.ezazi.ui.password.model.ChangePasswordRequestModel;
import org.intelehealth.ezazi.ui.password.model.PasswordResponseModel;
import org.intelehealth.ezazi.ui.password.model.RequestOTPModel;
import org.intelehealth.ezazi.ui.password.model.VerifyOtpRequestModel;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
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
    Call<PushResponseApiCall> PUSH_RESPONSE_API_CALL_OBSERVABLE(@Url String url,
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

    @GET
    Call<OxytocinResponseModel> GET_OXYTOCIN_UNIT(@Url String url);

    @POST("/api/auth/requestOtp")
    Call<ApiResponse<PasswordResponseModel>> requestOTP(@Body RequestOTPModel requestOTPModel);

    @POST("/api/auth/verifyOtp")
    Call<ApiResponse<PasswordResponseModel>> verifyOtp(@Body VerifyOtpRequestModel verifyOtpRequestModel);

    @POST("/api/auth/resetPassword/{userUuid}")
    Call<ApiResponse<PasswordResponseModel>> resetPassword(@Path("userUuid") String userUuid,
                                                           @Body ChangePasswordRequestModel changePasswordRequestModel);

   /* @POST("/api/auth/resetPassword/{userUuid}")
    @FormUrlEncoded
    Call<ApiResponse<PasswordResponseModel>> resetPassword(@Path("userUuid") String userUuid,
                                                           @Field("newPassword") String newPassword
    );*/

    @GET("api/getToken")
    Call<UserToken> getVideoAppToken(
            @Query("name") String doctorUuid,
            @Query("roomId") String patientUuid,
            @Query("nurseName") String nurseUuid
    );
}

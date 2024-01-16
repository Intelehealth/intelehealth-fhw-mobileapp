package org.intelehealth.ekalarogya.networkApiCalls;


import androidx.annotation.ContentView;

import org.intelehealth.ekalarogya.models.CheckAppUpdateRes;
import org.intelehealth.ekalarogya.models.DownloadMindMapRes;
import org.intelehealth.ekalarogya.models.Location;
import org.intelehealth.ekalarogya.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.ekalarogya.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.ekalarogya.models.Results;
import org.intelehealth.ekalarogya.models.UserProfileModel.MainProfileModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.UserAttributeModel;
import org.intelehealth.ekalarogya.models.UserProfileModel.UserInfoUpdateModel;
import org.intelehealth.ekalarogya.models.UserStatusUpdateApiCall;
import org.intelehealth.ekalarogya.models.dto.ResponseDTO;
import org.intelehealth.ekalarogya.models.loginModel.LoginModel;
import org.intelehealth.ekalarogya.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.ekalarogya.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.ekalarogya.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.ekalarogya.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.ekalarogya.models.statewise_location.District_Sanch_Village;
import org.intelehealth.ekalarogya.models.statewise_location.Setup_LocationModel;
import org.intelehealth.ekalarogya.models.statewise_location.State;
import org.intelehealth.ekalarogya.utilities.authJWT_API.AuthJWTBody;
import org.intelehealth.ekalarogya.utilities.authJWT_API.AuthJWTResponse;

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

    //-SetupLocation
    @GET("getLocations")
    Observable<Setup_LocationModel> SETUP_LOCATIONOBSERVABLE();

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


    /*  @GET("/v2/node/api/mindmap/download")
      Observable<DownloadMindMapRes> DOWNLOAD_MIND_MAP_RES_OBSERVABLE(@Query("key") String licenseKey, @Header("Authorization") String authHeader);
  */
    @GET("/api/mindmap/download")
    Observable<DownloadMindMapRes> DOWNLOAD_MIND_MAP_RES_OBSERVABLE(@Query("key") String licenseKey);

    @GET("/intelehealth/app_update.json")
    Single<CheckAppUpdateRes> checkAppUpdate();

    @GET
    Observable<ResponseBody> PERSON_PROFILE_INFO1(@Url String url,
                                                  @Header("Authorization") String authHeader);

    @GET
    Observable<Response<MainProfileModel>> PERSON_PROFILE_INFO(
            @Url String url,
            @Header("Authorization") String authHeader
    );

    @Headers({"Accept: application/json"})
    @POST
    Single<Response<ResponseBody>> UserStatus_API_CALL_OBSERVABLE(
            @Url String url,
            @Header("Authorization") String authHeader,
            @Body UserStatusUpdateApiCall userStatusUpdateApiCall
    );

    @Headers({"Accept: application/json"})
    @PUT
    Single<Response<UserInfoUpdateModel>> HwUpdateInfo_API_CALL_OBSERVABLE(
            @Url String url,
            @Header("Authorization") String authHeader,
            @Body UserAttributeModel obj
    );

    @POST
    Observable<AuthJWTResponse> AUTH_LOGIN_JWT_API(
            @Url String url,
            @Body AuthJWTBody authJWTBody
    );
}

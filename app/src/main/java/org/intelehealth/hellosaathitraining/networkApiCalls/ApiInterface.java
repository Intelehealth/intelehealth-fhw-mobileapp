package org.intelehealth.hellosaathitraining.networkApiCalls;


import org.intelehealth.hellosaathitraining.models.BucketResponse;
import org.intelehealth.hellosaathitraining.models.CheckAppUpdateRes;
import org.intelehealth.hellosaathitraining.models.DownloadMindMapRes;
import org.intelehealth.hellosaathitraining.models.IVR_Call_Models.Call_Details_Response;
import org.intelehealth.hellosaathitraining.models.Location;
import org.intelehealth.hellosaathitraining.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.hellosaathitraining.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.hellosaathitraining.models.PrescriptionBody;
import org.intelehealth.hellosaathitraining.models.PrescriptionUrl;
import org.intelehealth.hellosaathitraining.models.Results;
import org.intelehealth.hellosaathitraining.models.SubscriptionData;
import org.intelehealth.hellosaathitraining.models.SubscriptionStatus;
import org.intelehealth.hellosaathitraining.models.dto.ResponseDTO;
import org.intelehealth.hellosaathitraining.models.loginModel.LoginModel;
import org.intelehealth.hellosaathitraining.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.hellosaathitraining.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.hellosaathitraining.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.hellosaathitraining.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.hellosaathitraining.models.statewise_location.District_Sanch_Village;
import org.intelehealth.hellosaathitraining.models.statewise_location.State;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
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

    @GET
    Single<String> CALL_PATIENT_IVR(@Url String url);

    @GET
    Call<BucketResponse> getBucketList(@Url String url,
                                       @Header("Authorization") String authHeader);

    @GET
    Call<SubscriptionStatus> getSubscriptionStatus(@Url String url,
                                                   @Header("Authorization") String authHeader);

    @POST
    Call<SubscriptionStatus> subscribe(@Url String url,
                                     @Header("Authorization") String authHeader,
                                     @Body SubscriptionData data);

    @GET
    Observable<Call_Details_Response> IVR_CALL_RESPONSE(@Url String url);

//    @POST
//    Single<ResponseBody> SEND_PRESC_SMS(@Url String url, @Body PrescriptionSms prescriptionSms);

    @POST
    @FormUrlEncoded
    Call<ResponseBody> SEND_WELCOME_SMS(@Url String url,
                                        @Field("api-key") String apiKey,
                                        @Field("to") String to,
                                        @Field("sender") String sender,
                                        @Field("source") String source,
                                        @Field("type") String type,
                                        @Field("template_id") String template_id,
                                        @Field("body") String body);

    @POST
    Call<PrescriptionUrl> getPrescriptionShortUrl(@Url String url, @Body PrescriptionBody data);

}


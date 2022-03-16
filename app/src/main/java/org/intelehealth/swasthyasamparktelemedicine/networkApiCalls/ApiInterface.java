package org.intelehealth.swasthyasamparktelemedicine.networkApiCalls;


import org.intelehealth.swasthyasamparktelemedicine.models.CheckAppUpdateRes;
import org.intelehealth.swasthyasamparktelemedicine.models.ClsUserGetResponse;
import org.intelehealth.swasthyasamparktelemedicine.models.DownloadMindMapRes;
import org.intelehealth.swasthyasamparktelemedicine.models.GetPassword;
import org.intelehealth.swasthyasamparktelemedicine.models.GetUserCallRes.UserCallRes;
import org.intelehealth.swasthyasamparktelemedicine.models.Location;
import org.intelehealth.swasthyasamparktelemedicine.models.NewUserCreationCall.UserCreationData;
import org.intelehealth.swasthyasamparktelemedicine.models.ObsImageModel.ObsJsonResponse;
import org.intelehealth.swasthyasamparktelemedicine.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.swasthyasamparktelemedicine.models.Results;
import org.intelehealth.swasthyasamparktelemedicine.models.SendCallData;
import org.intelehealth.swasthyasamparktelemedicine.models.dto.ResponseDTO;
import org.intelehealth.swasthyasamparktelemedicine.models.loginModel.LoginModel;
import org.intelehealth.swasthyasamparktelemedicine.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.swasthyasamparktelemedicine.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.swasthyasamparktelemedicine.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.swasthyasamparktelemedicine.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.swasthyasamparktelemedicine.models.statewise_location.District_Sanch_Village;
import org.intelehealth.swasthyasamparktelemedicine.models.statewise_location.State;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
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

    @POST
    Observable<UserCallRes> REGISTER_USER(@Url String url,
                                          @Header("Authorization") String authHeader,
                                          @Body UserCreationData userCreationData);

    @GET
    Single<String> CALL_PATIENT_IVR(@Url String url);

    @GET
    Observable<ClsUserGetResponse> getUsersFromServer(@Url String url, @Header("Authorization") String authHeader, @Query("username")  String userName);

    @POST
    Observable<GetPassword> getUserMapping(@Url String url, @Header("Authorization") String authHeader, @Body GetPassword getPassword);


    @POST
    Call<ResponseBody> callPatientData(@Url String url, @Body SendCallData sendCallData);
}

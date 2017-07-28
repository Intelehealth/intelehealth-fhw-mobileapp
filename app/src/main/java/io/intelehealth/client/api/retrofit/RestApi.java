package io.intelehealth.client.api.retrofit;


import io.intelehealth.client.models.Results;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.models.PatientPhoto;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * List of the apis used.
 *
 * @see <a href="http://square.github.io/retrofit/">http://square.github.io/retrofit</a>de
 * <p>
 * Created by Dexter Barretto on 6/9/17.
 * Github : @dbarretto
 */

public interface RestApi {

    @GET("location?tag=Login%20Location")
    Call<Results<Location>> getLocations(@Query("v") String representation);

    @GET("session")
    Call<ResponseBody> loginTask(@Header("Authorization") String authHeader);

    @POST("personimage/{uuid}")
    Call<PatientPhoto> uploadPatientPhoto(@Path("uuid") String uuid,
                                          @Body PatientPhoto patientPhoto);

    @GET("personimage/{uuid}")
    Call<ResponseBody> downloadPatientPhoto(@Path("uuid") String uuid);

}

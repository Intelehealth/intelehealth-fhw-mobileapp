package io.intelehealth.client.retrofit;



import io.intelehealth.client.objects.Response;
import io.intelehealth.client.retrofit.models.Results;
import io.intelehealth.client.retrofit.models.resource.Location;
import io.intelehealth.client.retrofit.models.resource.PatientPhoto;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Calls the respective apis.
 * @see <a href="http://square.github.io/retrofit/">http://square.github.io/retrofit</a>
 *
 * Created by Dexter Barretto on 6/9/17.
 * Github : @dbarretto
 */

public interface RestApi {

    @GET("location?tag=Login%20Location")
    Call<Results<Location>> getLocations(@Query("v") String representation);

    @POST("personimage/{uuid}")
    Call<PatientPhoto> uploadPatientPhoto(@Path("uuid") String uuid,
                                          @Body PatientPhoto patientPhoto);

    @GET("personimage/{uuid}")
    Call<ResponseBody> downloadPatientPhoto(@Path("uuid") String uuid);

}

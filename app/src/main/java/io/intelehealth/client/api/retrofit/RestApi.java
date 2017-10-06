package io.intelehealth.client.api.retrofit;


import io.intelehealth.client.models.Identifier;
import io.intelehealth.client.models.Results;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.models.PatientPhoto;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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


    @GET("module/idgen/generateIdentifier.form?source=1")
    Call<Identifier> getIdentifier(@Query("username") String username,
                                   @Query("password") String password);

}

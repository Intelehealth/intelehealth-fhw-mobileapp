package io.intelehealth.client.api.retrofit;




import com.google.gson.JsonNull;

import io.intelehealth.client.models.Identifier;
import io.intelehealth.client.models.Results;
import io.intelehealth.client.models.Location;
import io.intelehealth.client.models.PatientPhoto;
import io.intelehealth.client.network.models.PatientUUIDResponsemodel;
import io.intelehealth.client.network.visitModels.VisitResponsemodel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

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

    @GET("visit")
    Call<PatientUUIDResponsemodel>   GETPATIENT(@Query("patient") String patientUUID, @Header("Authorization") String authHeader);


    @GET
    Call<VisitResponsemodel> VISIT_RESPONSEMODEL_CALL(@Url String url, @Header("Authorization") String authHeader);

    @DELETE("encounter/{uuid}?purge=true")
    Call<Void> DELETE_ENCOUNTER(@Path("uuid") String uuid,@Header("Authorization") String authHeader);

}

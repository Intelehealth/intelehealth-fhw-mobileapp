package io.intelehealth.client.network;

import android.location.Location;

import io.intelehealth.client.dto.ResponseDTO;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {


    @GET("location?tag=Login%20Location")
    Call<Results<Location>> getLocations(@Query("v") String representation);

    @GET("visit")
    Call<PatientUUIDResponsemodel> GETPATIENT(@Query("patient") String patientUUID, @Header("Authorization") String authHeader);


    @GET
    Call<VisitResponsemodel> VISIT_RESPONSEMODEL_CALL(@Url String url, @Header("Authorization") String authHeader);

    @DELETE("encounter/{uuid}?purge=true")
    Call<Void> DELETE_ENCOUNTER(@Path("uuid") String uuid, @Header("Authorization") String authHeader);

    //EMR-Middleware/webapi/pull/pulldata/
    @GET
    Call<ResponseDTO> RESPONSE_DTO_CALL(@Url String url);
}

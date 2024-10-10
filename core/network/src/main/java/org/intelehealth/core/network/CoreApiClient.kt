package org.intelehealth.core.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Created by - Prajwal W. on 10/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

interface CoreApiClient {

    @GET("location?tag=Login%20Location")
    suspend fun fetchLocationList(
        @Query("v") representation: String)
    : Response<Results<Location>>

    //EMR-Middleware/webapi/pull/pulldata/
    @GET
    suspend fun pullApiDetails(
        @Url url: String,
        @Header("Authorization") authHeader: String)
    : Response<ResponseDTO>

    @GET
    suspend fun fetchLoginDetails(
        @Url url: String,
        @Header("Authorization") authHeader: String)
    : Response<LoginModel>

    @GET
    suspend fun fetchLoginProviderDetails(
        @Url url: String,
        @Header("Authorization") authHeader: String)
    : Response<LoginProviderModel>

    @POST
    @Headers("Accept: application/json")
    suspend fun pushApiDetails(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body pushRequestApiCall: PushRequestApiCall
    ): Response<PushResponseApiCall>

    @GET
    suspend fun downloadPersonProfilePicture(
        @Url url: String,
        @Header("Authorization") authHeader: String)
    : Response<ResponseBody>

    @POST
    suspend fun uploadPersonProfilePicture(@Url url: String,
                                           @Header("Authorization") authHeader: String,
                                           @Body patientProfile: PatientProfile)
    : Response<ResponseBody>

    // Obs Image Download
    @GET
    suspend fun obsImageDownload(@Url url: String,
                                 @Header("Authorization") authHeader: String) : Response<ResponseBody>


    // Fetch obs response in json format.
    @POST
    @Multipart
    @Headers("Accept: application/json")
    suspend fun fetchObsJsonResponse(@Url url: String,
                                     @Header("Authorization") authHeader: String,
                                     @Part image: MultipartBody.Part,
                                     @Part("json") obsJsonRequest: ObsPushDTO
                                     )
    : Response<ObsJsonResponse>






















}
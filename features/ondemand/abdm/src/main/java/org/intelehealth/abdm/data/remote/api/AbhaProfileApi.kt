package org.intelehealth.abdm.data.remote.api

import org.intelehealth.abdm.data.remote.dto.AbhaCardResponseDto
import org.intelehealth.abdm.data.remote.dto.FetchAbhaProfileRequestDto
import org.intelehealth.abdm.data.remote.dto.FetchAbhaProfileResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AbhaProfileApi {
    @POST("abha/profile")
    suspend fun fetchAbhaProfile(
        @Header("X-TOKEN") xToken: String,
        @Body body: FetchAbhaProfileRequestDto,
    ): Response<FetchAbhaProfileResponseDto>

    @GET("abha/getCard")
    suspend fun fetchAbhaCard(
        @Query("scope") scope: String,
        @Header("X-TOKEN") xToken: String,
    ): Response<AbhaCardResponseDto>
}
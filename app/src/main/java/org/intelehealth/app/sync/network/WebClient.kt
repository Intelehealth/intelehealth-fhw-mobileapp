package org.intelehealth.app.sync.network

import org.intelehealth.app.models.dto.ResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface WebClient {

    @GET("/EMR-Middleware/webapi/pull/pulldata/{location_uuid}/{pull_executed_time}")
    suspend fun pullData(
        @Path("location_uuid") locationUuid: String,
        @Path("pull_executed_time") pullExecutedTime: String
    ): Response<ResponseDTO>

}
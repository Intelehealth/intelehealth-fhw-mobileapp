package org.intelehealth.config.network

import org.intelehealth.config.network.response.ConfigResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Created by Vaghela Mithun R. on 30-08-2023 - 15:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface WebClient {
    @GET("/api/config/getPublishedConfig")
    suspend fun getPublishedConfig(): Response<ConfigResponse>
}
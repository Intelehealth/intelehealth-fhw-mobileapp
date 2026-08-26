package org.intelehealth.abdm.data.remote.api

import org.intelehealth.abdm.data.remote.dto.TokenDto
import retrofit2.Response
import retrofit2.http.GET

interface AbdmAuthApi {
    @GET("abha/getToken")
    suspend fun getToken(): Response<TokenDto>
}
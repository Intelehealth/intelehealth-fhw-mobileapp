package org.intelehealth.abdm.data.remote.api

import org.intelehealth.abdm.data.remote.dto.AbhaAddressSuggestionsRequestDto
import org.intelehealth.abdm.data.remote.dto.AbhaAddressSuggestionsResponseDto
import org.intelehealth.abdm.data.remote.dto.RegisterAbhaAddressRequestDto
import org.intelehealth.abdm.data.remote.dto.RegisterAbhaAddressResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AbhaSuggestionsApi {
    @POST("abha/enrollSuggestion")
    suspend fun fetchAbhaAddressSuggestions(
        @Body request: AbhaAddressSuggestionsRequestDto,
    ): Response<AbhaAddressSuggestionsResponseDto>

    @POST("abha/setPreferredAddress")
    suspend fun registerPreferredAbhaAddress(
        @Body request: RegisterAbhaAddressRequestDto,
    ): Response<RegisterAbhaAddressResponseDto>
}
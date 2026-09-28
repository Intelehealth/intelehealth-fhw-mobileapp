package org.intelehealth.abdm.data.remote.api

import org.intelehealth.abdm.data.remote.dto.UpdateIdentifierRequestDto
import org.intelehealth.abdm.data.remote.dto.UserStatusResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface PatientApi {
    @GET
    suspend fun checkExistingUser(
        @Url url: String,
        @Header("Authorization") authHeader: String,
    ): Response<UserStatusResponseDto>

    @POST("openmrs/ws/rest/v1/patient/{patientUuid}/identifier")
    suspend fun updatePatientIdentifier(
        @Path("patientUuid") patientUuid: String,
        @Header("Authorization") authHeader: String,
        @Body body: UpdateIdentifierRequestDto,
    ): Response<Unit>
}
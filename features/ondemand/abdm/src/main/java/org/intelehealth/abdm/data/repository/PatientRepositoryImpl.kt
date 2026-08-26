package org.intelehealth.abdm.data.repository

import org.intelehealth.abdm.config.AbdmConfig
import org.intelehealth.abdm.config.AbdmSessionProvider
import org.intelehealth.abdm.data.mapper.toDomain
import org.intelehealth.abdm.data.remote.api.PatientApi
import org.intelehealth.abdm.data.remote.auth.basicAuthHeader
import org.intelehealth.abdm.data.remote.dto.UpdateIdentifierRequestDto
import org.intelehealth.abdm.data.remote.extensions.MalformedResponseException
import org.intelehealth.abdm.data.remote.extensions.requireBody
import org.intelehealth.abdm.data.remote.extensions.requireSuccess
import org.intelehealth.abdm.domain.model.PatientServerData
import org.intelehealth.abdm.domain.repository.PatientRepository
import javax.inject.Inject

internal class PatientRepositoryImpl @Inject constructor(
    private val api: PatientApi,
    private val sessionProvider: AbdmSessionProvider,
    private val config: AbdmConfig,
) : PatientRepository {
    override suspend fun checkExistingUser(
        abhaNumber: String,
    ): Result<PatientServerData> = runCatching {
        val url = "${config.baseUrl}EMR-Middleware/webapi/check/id/$abhaNumber"
        val authHeader = sessionProvider.basicAuthHeader()
        api.checkExistingUser(
            url = url,
            authHeader = authHeader
        )
            .requireBody()
            .toDomain()
            ?: throw MalformedResponseException("check existing user response missing required fields")
    }

    override suspend fun updatePatientIdentifier(
        patientUuid: String,
        identifier: String,
        identifierType: String,
        location: String,
    ): Result<Unit> = runCatching {
        api.updatePatientIdentifier(
            patientUuid = patientUuid,
            authHeader = sessionProvider.basicAuthHeader(),
            body = UpdateIdentifierRequestDto(
                identifier = identifier,
                identifierType = identifierType,
                location = location
            )
        ).requireSuccess()
    }
}
package org.intelehealth.abdm.domain.repository

import org.intelehealth.abdm.domain.model.PatientServerData

internal interface PatientRepository {
    suspend fun checkExistingUser(abhaNumber: String): Result<PatientServerData>
    suspend fun updatePatientIdentifier(
        patientUuid: String,
        identifier: String,
        identifierType: String,
        location: String,
    ): Result<Unit>
}
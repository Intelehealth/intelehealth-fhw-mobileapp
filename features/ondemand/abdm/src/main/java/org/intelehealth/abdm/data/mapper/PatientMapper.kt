package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.remote.dto.UserStatusResponseDto
import org.intelehealth.abdm.domain.model.PatientServerData

internal fun UserStatusResponseDto.toDomain(): PatientServerData? {
    val data = data ?: return PatientServerData(uuid = null, openMrsId = null)
    return PatientServerData(
        uuid = data.uuid,
        openMrsId = data.openMrsId,
    )
}
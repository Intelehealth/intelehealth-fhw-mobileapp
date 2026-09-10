package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.remote.dto.OtpResponseDto
import org.intelehealth.abdm.domain.model.RequestedOtp

internal fun OtpResponseDto.toDomain(): RequestedOtp? {
    return RequestedOtp(
        txnId = txnId ?: return null,
        message = message.orEmpty(),
    )
}
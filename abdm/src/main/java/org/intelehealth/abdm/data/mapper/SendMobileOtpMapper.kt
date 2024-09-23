package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.model.SendMobileOtpDto
import org.intelehealth.abdm.domain.model.SendMobileOtpToEnrollResponseData
import javax.inject.Inject

class SendMobileOtpMapper @Inject constructor() {

    // Map from Data to domain model
    fun toDomain(dto: SendMobileOtpDto): SendMobileOtpToEnrollResponseData {
        return SendMobileOtpToEnrollResponseData(
            txnId = dto.txnId,
            message = dto.message,
            authResult = dto.authResult
        )
    }
}

package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.model.VerifyMobileOtpResponseDto
import org.intelehealth.abdm.domain.model.EnrollMobileOtpResponseData
import javax.inject.Inject

class VerifyMobileOtpMapper @Inject constructor(){

    // Map from Data to domain model
    fun toDomain(dto: VerifyMobileOtpResponseDto): EnrollMobileOtpResponseData {
        return EnrollMobileOtpResponseData(
            txnId = dto.txnId,
            message = dto.message,
            authResult = dto.authResult
        )
    }


}

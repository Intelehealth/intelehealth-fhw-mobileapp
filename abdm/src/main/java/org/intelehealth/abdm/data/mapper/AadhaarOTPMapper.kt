package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.model.AadhaarOTPResponseDto
import org.intelehealth.abdm.domain.model.AadhaarOTP
import javax.inject.Inject

class AadhaarOTPMapper @Inject constructor(){

    // Method to map AadhaarOTPResponseDto to AadhaarOTP
    fun mapToDomain(aadhaarOTPResponseDto: AadhaarOTPResponseDto): AadhaarOTP {
        return AadhaarOTP(
            txnId = aadhaarOTPResponseDto.txnId,
            message = aadhaarOTPResponseDto.message,
            authResult = aadhaarOTPResponseDto.authResult
        )
    }
}
package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.model.ABHAProfileDto
import org.intelehealth.abdm.data.model.AadhaarOtpVerificationResponseDto
import org.intelehealth.abdm.data.model.TokensDto
import org.intelehealth.abdm.domain.model.ABHAProfile
import org.intelehealth.abdm.domain.model.AadhaarOtpVerification
import org.intelehealth.abdm.domain.model.Tokens
import javax.inject.Inject

class AadhaarOtpVerificationMapper @Inject constructor(){

    // Method to map AadhaarOtpVerificationResponseDto to AadhaarOtpVerification
    fun mapToDomain(dto: AadhaarOtpVerificationResponseDto): AadhaarOtpVerification {
        return AadhaarOtpVerification().apply {
            message = dto.message
            txnId = dto.txnId
            tokens = dto.tokens?.let { mapToTokens(it) }
            abhaProfile = dto.abhaProfile?.let { mapToAbhaProfile(it) }
            isNew = dto.isNew!!
            uuID = dto.uuID
            openMrsId = dto.openMrsId
        }
    }

    // Method to map AadhaarOtpVerification to AadhaarOtpVerificationResponseDto


    // Helper method to map TokensDto to Tokens
    private fun mapToTokens(tokensDto: TokensDto): Tokens {
        return Tokens(
            token = tokensDto.token,
            expiresIn = tokensDto.expiresIn,
            refreshToken = tokensDto.refreshToken,
            refreshExpiresIn = tokensDto.refreshExpiresIn
        )
    }

    // Helper method to map ABHAProfileDto to ABHAProfile
    private fun mapToAbhaProfile(abhaProfileDto: ABHAProfileDto): ABHAProfile {
        return ABHAProfile(
            firstName = abhaProfileDto.firstName,
            middleName = abhaProfileDto.middleName,
            lastName = abhaProfileDto.lastName,
            dob = abhaProfileDto.dob,
            gender = abhaProfileDto.gender,
            photo = abhaProfileDto.photo,
            mobile = abhaProfileDto.mobile,
            email = abhaProfileDto.email,
            phrAddress = abhaProfileDto.phrAddress,
            address = abhaProfileDto.address,
            districtCode = abhaProfileDto.districtCode,
            stateCode = abhaProfileDto.stateCode,
            pinCode = abhaProfileDto.pinCode,
            abhaType = abhaProfileDto.abhaType,
            stateName = abhaProfileDto.stateName,
            districtName = abhaProfileDto.districtName,
            aBHANumber = abhaProfileDto.aBHANumber,
            abhaStatus = abhaProfileDto.abhaStatus
        )
    }

}

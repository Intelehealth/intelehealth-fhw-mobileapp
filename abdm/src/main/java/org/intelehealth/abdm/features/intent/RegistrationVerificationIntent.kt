package org.intelehealth.abdm.features.intent

sealed interface RegistrationVerificationIntent {
    data class OnClickSendOtp(val aadhaarNo: String,val mobileNo : String) : RegistrationVerificationIntent
    data class ResendAadhaarOtp(val aadhaarNo: String,val mobileNo : String) : RegistrationVerificationIntent
    data class OnClickVerifyAadhaarOtp(val aadhaarOtp: String) : RegistrationVerificationIntent
    data class OnClickSendMobileOtp(val mobileNo: String) : RegistrationVerificationIntent
    data class ResendMobileOtp(val mobileNo: String) : RegistrationVerificationIntent
    data class VerifyMobileOtp(val mobileOtp: String) : RegistrationVerificationIntent
}
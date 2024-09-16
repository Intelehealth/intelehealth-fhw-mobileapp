package org.intelehealth.abdm.features.intent

sealed interface RegistrationVerificationIntent {
    data class OnClickSendOtp(val aadhaarNo: String,val mobileNo : String) : RegistrationVerificationIntent
    data class OnClickVerifyAadhaarOtp(val aadhaarOtp: String) : RegistrationVerificationIntent
    data class ResendAadhaarOtp(val aadhaarNo: String,val mobileNo : String) : RegistrationVerificationIntent
    data class OnClickMobileVerifyOtp(val mobileNo: String) : RegistrationVerificationIntent
    data class ResendMobileVerifyOtp(val mobileNo: String) : RegistrationVerificationIntent
}
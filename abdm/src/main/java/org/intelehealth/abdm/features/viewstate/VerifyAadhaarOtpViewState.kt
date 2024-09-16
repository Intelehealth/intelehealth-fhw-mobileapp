package org.intelehealth.abdm.features.viewstate

import org.intelehealth.abdm.domain.model.AadhaarOtpVerification


sealed interface VerifyAadhaarOtpViewState {
    data object Idle : VerifyAadhaarOtpViewState
    data object Loading : VerifyAadhaarOtpViewState
    data class Error(val message: String) : VerifyAadhaarOtpViewState
    data class OpenMobileVerificationScreen(val data: AadhaarOtpVerification) : VerifyAadhaarOtpViewState
    data class OpenSelectAbhaScreen(val data: AadhaarOtpVerification) : VerifyAadhaarOtpViewState

}
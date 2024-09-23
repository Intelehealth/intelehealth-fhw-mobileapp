package org.intelehealth.abdm.features.viewstate

import org.intelehealth.abdm.domain.model.AadhaarOTP

sealed interface SendAadhaarOtpViewState {
    data object Loading : SendAadhaarOtpViewState
    data object Idle : SendAadhaarOtpViewState
    data class Error(val message: String) : SendAadhaarOtpViewState
    data class Success(val data: AadhaarOTP) : SendAadhaarOtpViewState
}
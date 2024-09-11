package org.intelehealth.abdm.features.viewstate

import java.util.Objects


sealed interface SendAadhaarOtpViewState {
    data object Loading : SendAadhaarOtpViewState
    data object Idle : SendAadhaarOtpViewState
    data class Error(val message: String) : SendAadhaarOtpViewState
    data class Success(val data: Objects) : SendAadhaarOtpViewState

}
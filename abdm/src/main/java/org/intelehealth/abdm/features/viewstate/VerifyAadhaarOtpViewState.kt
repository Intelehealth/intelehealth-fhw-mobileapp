package org.intelehealth.abdm.features.viewstate

import java.util.Objects


sealed interface VerifyAadhaarOtpViewState {
    data object Loading : VerifyAadhaarOtpViewState
    data class Error(val message: String) : VerifyAadhaarOtpViewState
    data class Success(val data: Objects) : VerifyAadhaarOtpViewState

}
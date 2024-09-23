package org.intelehealth.abdm.features.viewstate

import org.intelehealth.abdm.domain.model.EnrollMobileOtpResponseData


sealed interface EnrollMobileOtpViewState {
    data object Idle : EnrollMobileOtpViewState
    data object Loading : EnrollMobileOtpViewState
    data class Error(val message: String) : EnrollMobileOtpViewState
    data class OpenSelectAbhaScreen(val data: EnrollMobileOtpResponseData) : EnrollMobileOtpViewState
}
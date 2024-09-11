package org.intelehealth.abdm.features.viewstate

import java.util.Objects


sealed interface SendMobileOtpViewState {
    data object Loading : SendMobileOtpViewState
    data class Error(val message: String) : SendMobileOtpViewState
    data class Success(val data: Objects) : SendMobileOtpViewState

}
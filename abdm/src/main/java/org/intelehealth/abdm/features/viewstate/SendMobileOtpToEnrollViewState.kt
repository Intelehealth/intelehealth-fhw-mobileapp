package org.intelehealth.abdm.features.viewstate

import org.intelehealth.abdm.domain.model.SendMobileOtpToEnrollResponseData


sealed interface SendMobileOtpToEnrollViewState {
    data object Idle : SendMobileOtpToEnrollViewState
    data object Loading : SendMobileOtpToEnrollViewState
    data class Error(val message: String) : SendMobileOtpToEnrollViewState
    data class Success(val data: SendMobileOtpToEnrollResponseData) : SendMobileOtpToEnrollViewState
}

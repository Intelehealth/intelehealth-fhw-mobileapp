package org.intelehealth.abdm.features.viewstate

import org.intelehealth.abdm.domain.model.AuthToken

sealed interface GetAuthTokenViewState {
    data object Loading : GetAuthTokenViewState
    data object Idle : GetAuthTokenViewState
    data class Success(val data: AuthToken) : GetAuthTokenViewState
    data class Error(val message: String) :GetAuthTokenViewState
}
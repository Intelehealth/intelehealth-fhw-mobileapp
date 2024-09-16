package org.intelehealth.abdm.data.viewstate

import org.intelehealth.abdm.domain.model.RegistrationConsent


sealed interface RegistrationConsentListViewState {
    data object Idle : RegistrationConsentListViewState
    data object Loading : RegistrationConsentListViewState
    data class Error(val message: String) : RegistrationConsentListViewState
    data class Success(val data: List<RegistrationConsent>) : RegistrationConsentListViewState

}
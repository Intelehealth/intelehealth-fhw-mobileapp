package org.intelehealth.abdm.features.viewstate

import org.intelehealth.abdm.domain.model.EnrolledAbhaAddressDetails

sealed interface EnrollAbhaAddressViewState {
    data object Loading : EnrollAbhaAddressViewState
    data object Idle : EnrollAbhaAddressViewState
    data class Success(val data: EnrolledAbhaAddressDetails) : EnrollAbhaAddressViewState
    data class Error(val message: String) :EnrollAbhaAddressViewState
}
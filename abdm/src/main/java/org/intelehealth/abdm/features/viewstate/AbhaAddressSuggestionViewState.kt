package org.intelehealth.abdm.features.viewstate

import org.intelehealth.abdm.domain.model.AbhaAddressSuggestionList

sealed interface AbhaAddressSuggestionViewState {
    data object Loading : AbhaAddressSuggestionViewState
    data object Idle : AbhaAddressSuggestionViewState
    data class Success(val data: AbhaAddressSuggestionList) : AbhaAddressSuggestionViewState
    data class Error(val message: String) : AbhaAddressSuggestionViewState
}
package org.intelehealth.abdm.presentation.abha_suggestions

import androidx.annotation.StringRes
import org.intelehealth.abdm.presentation.common.UiState

internal data class AbhaSuggestionsUiState(
    val addresses: List<String> = emptyList(),
    val operation: UiState<Unit> = UiState.Idle,
    @StringRes val loadingMessageRes: Int? = null,
    @StringRes val addressError: Int? = null,
)

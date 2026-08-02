package org.intelehealth.abdm.presentation.abha_suggestions

import androidx.annotation.StringRes
import org.intelehealth.abdm.presentation.common.UiState

internal data class AbhaSuggestionsUiState(
    val addresses: List<String> = emptyList(),
    val operation: UiState<Unit> = UiState.Idle,
    @StringRes val loadingMessageRes: Int? = null,
    @StringRes val addressError: Int? = null,

    /**
     * The screen normally suppresses the back press to force a Submit. Set when the address was
     * registered with ABDM but linking it to the patient failed, so the user has a way out instead
     * of being stuck on a screen whose only action has already half-succeeded.
     */
    val allowExit: Boolean = false,
)

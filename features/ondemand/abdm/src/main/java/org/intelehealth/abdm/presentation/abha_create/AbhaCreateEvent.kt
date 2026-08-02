package org.intelehealth.abdm.presentation.abha_create

import androidx.annotation.StringRes
import org.intelehealth.abdm.domain.model.AbhaCreateSession
import org.intelehealth.abdm.result.AbdmResult

internal sealed interface AbhaCreateEvent {
    data class NavigateToSuggestions(val session: AbhaCreateSession) : AbhaCreateEvent

    /**
     * One-shot coloured snackbar. Prefers the server-supplied [message]; falls back to [messageRes].
     * [isSuccess] picks the green/red tone.
     */
    data class ShowSnackbar(
        val message: String? = null,
        @StringRes val messageRes: Int? = null,
        val isSuccess: Boolean = false,
    ) : AbhaCreateEvent

    /** Show the existing-addresses picker so the user can choose one or request a new one. */
    data class ShowAddressChecklist(
        val preferredAbhaAddress: String,
        val abhaAddresses: List<String>,
    ) : AbhaCreateEvent

    /** Hand the finished outcome back to the host. */
    data class CompleteWithResult(val result: AbdmResult) : AbhaCreateEvent
}
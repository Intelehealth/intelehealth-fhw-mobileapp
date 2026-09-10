package org.intelehealth.abdm.presentation.abha_suggestions

internal sealed interface AbhaSuggestionsEvent {
    /** The user's preferred ABHA address was registered successfully. */
    data class AddressRegistered(val preferredAbhaAddress: String) : AbhaSuggestionsEvent
}

package org.intelehealth.abdm.features.intent

sealed interface EnrollAbhaAddressIntent {
    data class GetSuggestionList(val txnId : String) : EnrollAbhaAddressIntent
    data class EnrollAbhaAddress(val txnId : String,val abhaAddress: String) : EnrollAbhaAddressIntent
}
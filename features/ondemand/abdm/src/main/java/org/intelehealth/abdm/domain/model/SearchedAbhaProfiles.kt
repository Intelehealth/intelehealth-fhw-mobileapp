package org.intelehealth.abdm.domain.model

internal data class SearchedAbhaProfiles(
    val txnId: String,
    val abhaSearchResults: List<AbhaSearchResult>,
)

internal data class AbhaSearchResult(
    val abhaNumber: String,
    val name: String,
    val gender: String?,
    val index: Int?,
)
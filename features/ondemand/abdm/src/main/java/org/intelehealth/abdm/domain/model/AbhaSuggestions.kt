package org.intelehealth.abdm.domain.model

/**The wrapper model for AbhaAddressSuggestionsResponseDto.
 *  It uses only those properties from TokenDto which are actually used in the app*/
internal data class AbhaSuggestions(
    val txnId: String,
    val addresses: List<String>,
)
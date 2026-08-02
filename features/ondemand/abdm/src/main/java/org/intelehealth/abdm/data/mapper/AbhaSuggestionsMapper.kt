package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.remote.dto.AbhaAddressSuggestionsResponseDto
import org.intelehealth.abdm.data.remote.dto.RegisterAbhaAddressResponseDto
import org.intelehealth.abdm.domain.model.AbhaSuggestions
import org.intelehealth.abdm.domain.model.RegisteredAbhaAddress

internal fun AbhaAddressSuggestionsResponseDto.toDomain(): AbhaSuggestions? {
    val txnId: String = txnId ?: return null
    val addresses: List<String> = abhaAddressList ?: emptyList()

    return AbhaSuggestions(
        txnId = txnId,
        addresses = addresses,
    )
}

internal fun RegisterAbhaAddressResponseDto.toDomain(): RegisteredAbhaAddress? {
    val preferredAbhaAddress: String = preferredAbhaAddress.orEmpty()
    return RegisteredAbhaAddress(preferredAbhaAddress = preferredAbhaAddress)
}
package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.model.AbhaAddressSuggestionListDto
import org.intelehealth.abdm.domain.model.AbhaAddressSuggestionList
import javax.inject.Inject

class AbhaAddressSuggestionListMapper @Inject constructor() {

    fun toDomain(dto: AbhaAddressSuggestionListDto): AbhaAddressSuggestionList {
        return AbhaAddressSuggestionList(
            txnId = dto.txnId,
            abhaAddressList = dto.abhaAddressList
        )
    }
}
package org.intelehealth.abdm.data.mapper

import org.intelehealth.abdm.data.model.EnrolledAbhaAddressDto
import org.intelehealth.abdm.domain.model.EnrolledAbhaAddressDetails
import javax.inject.Inject

class EnrolledAbhaAddressMapper @Inject constructor() {

    // Map from Data to domain model
    fun toDomain(dto: EnrolledAbhaAddressDto): EnrolledAbhaAddressDetails {
        return EnrolledAbhaAddressDetails(
            message = dto.message,
            txnId = dto.txnId,
            healthIdNumber = dto.healthIdNumber,
            preferredAbhaAddress = dto.preferredAbhaAddress
        )
    }

}

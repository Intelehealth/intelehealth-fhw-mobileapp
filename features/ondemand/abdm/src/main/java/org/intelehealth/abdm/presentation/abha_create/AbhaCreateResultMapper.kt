package org.intelehealth.abdm.presentation.abha_create

import org.intelehealth.abdm.domain.model.AbhaCreateSession
import org.intelehealth.abdm.result.AbdmAbhaProfile
import org.intelehealth.abdm.result.AbdmOutcomes
import org.intelehealth.abdm.result.AbdmResult

/** Maps the internal verified session into the public [AbdmResult] handed back to the host. */
internal fun AbhaCreateSession.toAbdmResult(
    outcome: AbdmOutcomes,
    preferredAbhaAddress: String? = null,
    uuid: String? = null,
    openMrsId: String? = null,
    phrAddresses: List<String> = profile.phrAddresses,
): AbdmResult =
    AbdmResult(
        outcome = outcome,
        accessToken = null,
        xToken = enrolledAbhaToken.token,
        txnId = txnId,
        isNew = isNew,
        uuid = uuid,
        openMrsId = openMrsId,
        cardScope = AbdmResult.CARD_SCOPE_CREATE,
        profile = AbdmAbhaProfile(
            abhaNumber = profile.abhaNumber,
            firstName = profile.firstName,
            middleName = profile.middleName,
            lastName = profile.lastName,
            dateOfBirth = profile.dateOfBirth,
            gender = profile.gender,
            mobile = profile.mobile,
            address = profile.address,
            pinCode = profile.pinCode,
            profilePhoto = profile.profilePhoto,
            phrAddresses = phrAddresses,
            preferredAbhaAddress = preferredAbhaAddress,
        ),
    )

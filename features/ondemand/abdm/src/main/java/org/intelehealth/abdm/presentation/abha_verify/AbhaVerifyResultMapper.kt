package org.intelehealth.abdm.presentation.abha_verify

import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.abdm.domain.model.AbhaProfile
import org.intelehealth.abdm.result.AbdmAbhaProfile
import org.intelehealth.abdm.result.AbdmOutcomes
import org.intelehealth.abdm.result.AbdmResult

/**
 * Projects the verified ABHA profile into the same record shape as the local side, so the compare
 * screen can lay them out field-for-field. Carries the local [uuid]/[openMrsId] so a merge saves
 * back to the existing patient row.
 */
internal fun AbhaProfile.toComparisonRecord(uuid: String, openMrsId: String): LocalPatientRecord =
    LocalPatientRecord(
        uuid = uuid,
        openMrsId = openMrsId,
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = "$yearOfBirth-$monthOfBirth-$dayOfBirth",
        gender = gender,
        address = address,
        pinCode = pinCode,
        phoneNumber = mobile,
        abhaNumber = abhaNumber,
        abhaAddress = preferredAbhaAddress,
    )

/**
 * Maps a verified [AbhaProfile] into the [AbdmResult] handed back to the host when the verified
 * ABHA belongs to a brand-new (not-on-HMIS) patient.
 */
internal fun AbhaProfile.toNewPatientVerifyResult(
    xToken: String,
    txnId: String,
): AbdmResult =
    AbdmResult(
        outcome = AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_NEW_PATIENT_FOR_VERIFICATION,
        accessToken = null,
        xToken = xToken,
        txnId = txnId,
        isNew = false,
        cardScope = AbdmResult.CARD_SCOPE_VERIFY,
        profile = AbdmAbhaProfile(
            abhaNumber = abhaNumber,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            dateOfBirth = "$yearOfBirth-$monthOfBirth-$dayOfBirth",
            gender = gender,
            mobile = mobile,
            address = address,
            pinCode = pinCode,
            profilePhoto = profilePhoto,
            phrAddresses = listOf(preferredAbhaAddress),
            preferredAbhaAddress = preferredAbhaAddress,
        ),
    )

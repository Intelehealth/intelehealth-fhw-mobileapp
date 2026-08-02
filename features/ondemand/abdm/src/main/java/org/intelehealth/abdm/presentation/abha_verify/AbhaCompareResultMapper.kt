package org.intelehealth.abdm.presentation.abha_verify

import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.abdm.result.AbdmAbhaProfile
import org.intelehealth.abdm.result.AbdmOutcomes
import org.intelehealth.abdm.result.AbdmResult

/**
 * Builds the [AbdmResult] returned after a successful compare-and-save. The saved record is the
 * ABHA side (locked selection), carrying the existing patient's [uuid]/[openMrsId].
 */
internal fun LocalPatientRecord.toAfterComparisonResult(
    xToken: String,
    txnId: String,
): AbdmResult =
    AbdmResult(
        outcome = AbdmOutcomes.NAVIGATE_TO_PATIENT_DETAILS_SCREEN_WITH_EXISTING_PATIENT_AFTER_COMPARISON,
        accessToken = null,
        xToken = xToken,
        txnId = txnId,
        isNew = false,
        uuid = uuid,
        openMrsId = openMrsId,
        profile = AbdmAbhaProfile(
            abhaNumber = abhaNumber,
            firstName = firstName,
            middleName = null,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            gender = gender,
            mobile = phoneNumber,
            address = address,
            pinCode = pinCode,
            profilePhoto = null,
            phrAddresses = listOf(abhaAddress),
            preferredAbhaAddress = abhaAddress,
        ),
    )

package org.intelehealth.abdm.presentation.abha_create

import org.intelehealth.abdm.domain.model.AbhaCreateSession
import org.intelehealth.abdm.domain.model.EnrolledAbhaProfile
import org.intelehealth.abdm.result.AbdmAbhaProfile
import org.intelehealth.abdm.result.AbdmOutcomes
import org.intelehealth.abdm.result.AbdmResult

/**
 * The enrolment profile's date of birth in the yyyy-MM-dd that the local patient lookup matches on.
 *
 * Enrolment returns the date as a single day-first string, where verification returns year, month and
 * day as three separate fields — so the verify normaliser is not reusable here: it takes a different
 * profile type and reads fields this one does not have. Without the flip the date is still well formed
 * and so passes every guard, but matches no local row, and the lookup that depends on it quietly finds
 * nobody.
 *
 * Blank on anything unexpected, which the local store reads as "no usable date" and answers by skipping
 * the phone fallback rather than matching on a number a household may share.
 */
internal fun EnrolledAbhaProfile.normalisedDateOfBirth(): String {
    val parts = dateOfBirth.trim().split("-")
    if (parts.size != 3) return ""
    val (day, month, year) = parts
    if (year.length != 4 || day.isEmpty() || month.isEmpty()) return ""
    if (!year.all { it.isDigit() } || !month.all { it.isDigit() } || !day.all { it.isDigit() }) {
        return ""
    }
    return "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
}

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

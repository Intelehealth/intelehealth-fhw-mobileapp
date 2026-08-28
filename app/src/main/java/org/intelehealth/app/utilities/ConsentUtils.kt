package org.intelehealth.app.utilities

import org.intelehealth.core.utility.DateTimeUtils
import java.util.Date
import java.util.TimeZone

/**
 * NAS-1752 - builds the pipe-separated consent value shared by Patient, Teleconsultation and
 * ABDM consent:
 *
 * `Date & Time | HW User_UUID | language | status | application`
 *
 * e.g. `25-08-2026 19:45:45 | 5f2c1b3a-... | mr | active | NAS-mobile`
 *
 * Centralised here so the format is defined once instead of being hand-built at each of the
 * three call sites (PersonalConsentActivity, VisitCreationActivity, the ABDM consent flow).
 */
object ConsentUtils {

    const val STATUS_ACTIVE = "active"
    const val APPLICATION_NAS_MOBILE = "NAS-mobile"

    @JvmStatic
    @JvmOverloads
    fun buildConsentValue(
        hwUuid: String?,
        language: String?,
        status: String = STATUS_ACTIVE,
        application: String = APPLICATION_NAS_MOBILE,
        date: Date = Date()
    ): String {
        val dateTime = DateTimeUtils.formatDate(date, DateTimeUtils.DD_MM_YYYY_HH_MM_SS, TimeZone.getDefault())
        return "$dateTime | ${hwUuid.orEmpty()} | ${language.orEmpty()} | $status | $application"
    }
}

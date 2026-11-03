package org.intelehealth.app.utilities

import org.intelehealth.core.utility.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        // Locale.ENGLISH is forced deliberately, instead of going through DateTimeUtils.formatDate
        // (which builds its SimpleDateFormat with no locale, i.e. whatever Locale.setDefault() last
        // left the JVM on). This value is a stored, machine-readable field the server parses, so it
        // must always render Latin digits (0-9) - some locales, Marathi included, render
        // SimpleDateFormat's digits in their own numbering system (e.g. Devanagari) otherwise, which
        // silently corrupted this field whenever consent was captured while the app's UI language
        // was set to one of those locales.
        val dateTime = SimpleDateFormat(DateTimeUtils.DD_MM_YYYY_HH_MM_SS, Locale.ENGLISH).format(date)
        return "$dateTime | ${hwUuid.orEmpty()} | ${language.orEmpty()} | $status | $application"
    }
}

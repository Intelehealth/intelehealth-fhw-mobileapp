package org.intelehealth.ncd.linelisting.utils

import android.util.Log
import com.google.gson.Gson
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.DateAndTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


object ProtocolParserHelper {

    private const val TAG = "ProtocolParserHelper"

    private val displayFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.US)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

    // FIX: Add end-date parser
    private val endDateFormat = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.US)

    private fun parseDateSafe(raw: String?): Date {
        if (raw.isNullOrBlank()) return Date(0)

        try { displayFormat.parse(raw)?.let { return it } } catch (_: Exception) {}
        try { isoFormat.parse(raw.replace(":", ""))?.let { return it } } catch (_: Exception) {}

        // FIX: Parse visitEndDate format: "Oct 7, 2025 3:11:34 PM"
        try { endDateFormat.parse(raw)?.let { return it } } catch (_: Exception) {}

        return Date(0)
    }

    fun extractComplaintBlocks(data: String): List<String> {
        val normalized = data.replace("\n", "")
            .replace("\\u003c", "<")
            .replace("\\u003e", ">")

        val splitter = "►<b>".toRegex()
        return normalized.split(splitter)
            .filter { it.isNotBlank() }
            .map { if (!it.startsWith("►<b>")) "►<b>$it" else it }
    }

    fun parsePatientHistory(allVisits: List<PatientVisitDetails>): PatientVisitDetails {
        Log.d(TAG, "parsePatientHistory: allVisits : ${allVisits}")
        if (allVisits.isEmpty()) return PatientVisitDetails()

        // FIX: Remove redundant ended filtering
        val endedVisits = allVisits
        Log.d(TAG, "parsePatientHistory: endedVisits : ${endedVisits}")

        // If still empty
        if (endedVisits.isEmpty()) return PatientVisitDetails()

        // Sorting FIXED because parseDateSafe now works
        val latestEndedVisit = endedVisits
            .sortedByDescending { parseDateSafe(it.startDate) }
            .first()

        val result = latestEndedVisit.copy()
        Log.d(TAG, "parsePatientHistory: result : "+result)

        result.isHypertensionFollowupGiven = null
        result.isHypertensionFollowupTodayOrLater = null
        result.isAnemiaFollowupGiven = null
        result.isAnemiaFollowupTodayOrLater = null
        result.isDiabetesFollowupGiven = null
        result.isDiabetesFollowupTodayOrLater = null

        val sortedVisits = endedVisits.sortedByDescending { parseDateSafe(it.startDate) }
        val today = Date()

        val foundGiven = mutableMapOf(
            "hypertension" to false,
            "anemia" to false,
            "diabetes" to false
        )

        val foundTodayOrLater = mutableMapOf(
            "hypertension" to false,
            "anemia" to false,
            "diabetes" to false
        )

        for (visit in sortedVisits) {
            if (!visit.isNcdVisit.equals("true", ignoreCase = true)) continue
            val blocks = visit.chiefComplaintData?.let { extractComplaintBlocks(it) }
            if (blocks.isNullOrEmpty()) continue

            for (block in blocks) {
                val complaint = "<b>(.*?)</b>".toRegex()
                    .find(block)
                    ?.groupValues?.getOrNull(1)
                    ?.trim()
                    ?.lowercase()

                val protocol = when (complaint) {
                    "hypertension screening", "hypertension followup" -> "hypertension"
                    "anemia screening", "anemia followup" -> "anemia"
                    "diabetes screening", "diabetes followup" -> "diabetes"
                    else -> null
                } ?: continue

                val followUpDateStr =
                    "(?i)Next follow Up(?: Date)? -\\s*([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4})"
                        .toRegex()
                        .find(block)
                        ?.groupValues?.getOrNull(1)


                if (!foundGiven[protocol]!!) {
                    when (protocol) {
                        "hypertension" -> result.isHypertensionFollowupGiven = true
                        "anemia"       -> result.isAnemiaFollowupGiven = true
                        "diabetes"     -> result.isDiabetesFollowupGiven = true
                    }
                    foundGiven[protocol] = true
                }
                val followUpDate = parseFollowUpDate(followUpDateStr)
                if (followUpDateStr != null && !foundTodayOrLater[protocol]!!) {
                    val isTodayOrLater = !today.before(followUpDate)

                    when (protocol) {
                        "hypertension" -> result.isHypertensionFollowupTodayOrLater = isTodayOrLater
                        "anemia"       -> result.isAnemiaFollowupTodayOrLater = isTodayOrLater
                        "diabetes"     -> result.isDiabetesFollowupTodayOrLater = isTodayOrLater
                    }
                    foundTodayOrLater[protocol] = true
                }
            }
        }
        return result
    }
    private fun parseFollowUpDate(raw: String?): Date? {
        if (raw.isNullOrBlank()) return null

        val formats = listOf(
            SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH),          // 07/Oct/2026
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),           // ISO fallback
            SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH) // Oct 7, 2025 3:11:34 PM
        )

        for (format in formats) {
            try {
                format.parse(raw)?.let { return it }
            } catch (_: Exception) {}
        }

        return null
    }
}

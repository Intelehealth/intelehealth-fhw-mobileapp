package org.intelehealth.ncd.utils

import android.content.Context
import org.intelehealth.ncd.R
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.Years
import java.text.SimpleDateFormat
import java.util.Locale


object DateAndTimeUtils {

    fun getAgeInYearMonth(s: String?, context: Context): String {
        if (s.isNullOrEmpty()) return ""

        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        val date = try {
            originalFormat.parse(s)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: return ""

        val formattedDate = targetFormat.format(date)
        val components = formattedDate.split("-").map { it.toInt() }
        if (components.size != 3) return ""

        val (day, month, year) = components
        val birthdate = LocalDate(year, month, day)
        val now = LocalDate()
        val period = Period(birthdate, now, PeriodType.yearMonthDay())

        val tYears = if (period.years > 0)
            "${period.years} ${context.resources.getString(R.string.years)}"
        else ""

        val tMonth = if (period.months > 0)
            "${period.months} ${context.resources.getString(R.string.months)}"
        else ""

        val tDays = if (period.days > 0)
            "${period.days} ${context.resources.getString(R.string.days)}"
        else ""

        return listOf(tYears, tMonth, tDays).filterNot(String::isEmpty).joinToString(" ")
    }

    fun calculateAgeInYears(birthDate: String?): Int {
        birthDate?.let {
            val today = LocalDate.now()
            val birth = LocalDate.parse(birthDate)
            return@let Years.yearsBetween(birth, today).years
        }
        return 0;
    }

}
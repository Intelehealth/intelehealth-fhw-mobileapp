package org.intelehealth.ncd.utils

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.intelehealth.ncd.R
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.Years
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
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
            return Years.yearsBetween(birth, today).years
        }
        return 0;
    }
    fun currentDateTime(): String {
        Locale.setDefault(Locale.ENGLISH)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        val todayDate = Date()
        return dateFormat.format(todayDate)
    }
    fun formatDateFromOnetoAnother(
        date: String,
        sourceFormat: String,
        targetFormat: String
    ): String {
        if (date.isEmpty()) return ""

        return try {
            val sdf = SimpleDateFormat(sourceFormat, Locale.ENGLISH)
            val sdf1 = SimpleDateFormat(targetFormat, Locale.ENGLISH)
            val parsedDate = sdf.parse(date)
            parsedDate?.let { sdf1.format(it) } ?: ""
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            ""
        }
    }
    fun getAgeFollowUp(dobString: String?, context: Context): String {

        if (dobString == null) return ""

        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        val date = try {
            originalFormat.parse(dobString)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

        val formattedDate = targetFormat.format(date ?: return "")
        val components = formattedDate.split("-")

        val year = components[2].toInt()
        val month = components[1].toInt()
        val day = components[0].toInt()

        val birthdate = LocalDate(year, month, day)   // Joda-Time LocalDate
        val now = LocalDate()                         // Current date
        val period = Period(birthdate, now, PeriodType.yearMonthDay())

        val years = period.getValue(0)
        val age = if (years > 0) {
            //CustomLog.d("TAG", "getAge_FollowUp: s : $years")
            years.toString()
        } else {
            "0"
        }

        return age
    }
    fun formatStartVisitDate(dateString: String?, format: String, resultFormat: String): String? {
        return try {
            val originalFormat = SimpleDateFormat(format, Locale.ENGLISH)
            val targetFormat = SimpleDateFormat(resultFormat, Locale.ENGLISH)
            val date = originalFormat.parse(dateString ?: return null)
            targetFormat.format(date ?: return null)
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            null
        }
    }

}
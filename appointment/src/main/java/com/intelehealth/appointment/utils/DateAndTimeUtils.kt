package com.intelehealth.appointment.utils

import android.content.Context
import android.text.format.DateUtils
import androidx.core.util.Preconditions
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class DateAndTimeUtils {
    fun currentDateTime(): String {
        Locale.setDefault(Locale.ENGLISH)
        val date: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        // you can get seconds by adding  "...:ss" to it
        val todayDate = Date()
        return date.format(todayDate)
    }

    fun currentDateTimeInHome(): String {
        val date: DateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        // you can get seconds by adding  "...:ss" to it
        val todayDate = Date()
        return date.format(todayDate)
    }

    fun getcurrentDateTime(): String {
        val date: DateFormat = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.ENGLISH)
        val todayDate = Date()
        return date.format(todayDate)
    }

    fun getcurrentDateTime(localeCode: String?): String {
        val date: DateFormat = SimpleDateFormat("hh:mm a, dd MMMM yyyy", Locale.ENGLISH)
        val todayDate = Date()
        return date.format(todayDate)
    }

    companion object {
        private const val TAG = "DateAndTimeUtils"
        const val D_FORMAT_dd_M_yyyy: String = "dd/M/yyyy"


        fun parse_DateToddMMyyyy(time: String?): String? {
            val inputPattern = "dd-MM-yyyy"
            val outputPattern = "dd MMM yyyy"
            val inputFormat = SimpleDateFormat(inputPattern)
            val outputFormat = SimpleDateFormat(outputPattern)

            var date: Date? = null
            var str: String? = null

            try {
                date = inputFormat.parse(time)
                str = outputFormat.format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return str
        }

        fun parse_DateToddMMyyyy_new(time: String?): String? {
            val inputPattern = "yyyy-MM-dd"
            val outputPattern = "dd MMM yyyy"
            val inputFormat = SimpleDateFormat(inputPattern)
            val outputFormat = SimpleDateFormat(outputPattern)

            var date: Date? = null
            var str: String? = null

            try {
                date = inputFormat.parse(time)
                str = outputFormat.format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return str
        }


        fun getDisplayDateForApp(date: String?): String {
            var finalDate = ""
            if (date != null && !date.isEmpty()) {
                val dateSplit =
                    date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val year = dateSplit[0]
                val month = dateSplit[1]
                val day = dateSplit[2]


                var monthString = ""
                when (month) {
                    "01" -> monthString = "Jan"
                    "02" -> monthString = "Feb"
                    "03" -> monthString = "March"
                    "04" -> monthString = "April"
                    "05" -> monthString = "May"
                    "06" -> monthString = "June"
                    "07" -> monthString = "July"
                    "08" -> monthString = "Aug"
                    "09" -> monthString = "Sept"
                    "10" -> monthString = "Oct"
                    "11" -> monthString = "Nov"
                    "12" -> monthString = "Dec"
                }
                // finalDate = day + " " + monthString + " " + year;
                finalDate = "$monthString $day, $year"
            }
            return finalDate
        }


        fun getDisplayDateAndTimeFromDDMMFormat(inputDate: String?): String {
            var finalDate = ""
            var hourFormated = ""
            val textTime: String
            if (!inputDate.isNullOrEmpty()) {
                val splitedString =
                    inputDate.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val splitedTime =
                    splitedString[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                if (splitedTime[0].toInt() > 12) {
                    hourFormated = getTwelveHourFormat(
                        splitedTime[0]
                    )
                    textTime = "pm"
                } else {
                    hourFormated = splitedTime[0]
                    textTime = "am"
                }
                val timeDisplay = hourFormated + ":" + splitedTime[1] + " " + textTime
                val displayDate = getDateWithDayAndMonth(
                    splitedString[0]
                )
                finalDate = "$displayDate, at $timeDisplay"
            }
            return finalDate
        }

        fun getTwelveHourFormat(hour: String?): String {
            var hourString = ""
            when (hour) {
                "12" -> hourString = "12"
                "13" -> hourString = "01"
                "14" -> hourString = "02"
                "15" -> hourString = "03"
                "16" -> hourString = "04"
                "17" -> hourString = "05"
                "18" -> hourString = "06"
                "19" -> hourString = "07"
                "20" -> hourString = "08"
                "21" -> hourString = "09"
                "22" -> hourString = "10"
                "23" -> hourString = "11"
                "24" -> hourString = "12"
            }
            return hourString
        }

        fun getDateWithDayAndMonth(date: String?): String {
            var finalDate = ""
            if (!date.isNullOrEmpty()) {
                val dateSplit =
                    date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val year = dateSplit[0]
                val month = dateSplit[1]
                val day = dateSplit[2]


                var monthString = ""
                when (month) {
                    "01" -> monthString = "Jan"
                    "02" -> monthString = "Feb"
                    "03" -> monthString = "March"
                    "04" -> monthString = "April"
                    "05" -> monthString = "May"
                    "06" -> monthString = "June"
                    "07" -> monthString = "July"
                    "08" -> monthString = "Aug"
                    "09" -> monthString = "Sept"
                    "10" -> monthString = "Oct"
                    "11" -> monthString = "Nov"
                    "12" -> monthString = "Dec"
                }
                finalDate = "$day $monthString"
            }
            return finalDate
        }

        fun getDateWithDayAndMonthFromDDMMFormat(date: String?): String {
            var finalDate = ""
            if (!date.isNullOrEmpty()) {
                val dateSplit =
                    date.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var month = dateSplit[1]
                val day = dateSplit[0]
                if (month.isNotEmpty() && month.length == 1) {
                    month = "0$month"
                }
                var monthString = ""
                when (month) {
                    "01" -> monthString = "Jan"
                    "02" -> monthString = "Feb"
                    "03" -> monthString = "March"
                    "04" -> monthString = "April"
                    "05" -> monthString = "May"
                    "06" -> monthString = "June"
                    "07" -> monthString = "July"
                    "08" -> monthString = "Aug"
                    "09" -> monthString = "Sept"
                    "10" -> monthString = "Oct"
                    "11" -> monthString = "Nov"
                    "12" -> monthString = "Dec"
                }
                finalDate = "$day $monthString"
            }
            return finalDate
        }

        fun getMonthAndYearFromGivenDate(date: String?): Array<String?> {
            var result = arrayOfNulls<String>(0)
            if (!date.isNullOrEmpty()) {
                val dateSplit =
                    date.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val month = dateSplit[1]
                val day = dateSplit[0]
                val year = dateSplit[2]


                var monthString = ""
                when (month) {
                    "01" -> monthString = "January"
                    "02" -> monthString = "February"
                    "03" -> monthString = "March"
                    "04" -> monthString = "April"
                    "05" -> monthString = "May"
                    "06" -> monthString = "June"
                    "07" -> monthString = "July"
                    "08" -> monthString = "August"
                    "09" -> monthString = "September"
                    "10" -> monthString = "October"
                    "11" -> monthString = "November"
                    "12" -> monthString = "December"
                }
                result = arrayOf(monthString, year)
            }
            return result
        }

        fun getDateInDDMMMMYYYYFormat(inputDate: String?): String {
            var dateFormatted = ""
            //input date must be in dd/mm/yyyy format
            if (!inputDate.isNullOrEmpty()) {
                val sdf = SimpleDateFormat("dd/MM/yyyy")
                var d: Date? = null
                try {
                    d = sdf.parse(inputDate)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                val sdf2 = SimpleDateFormat("dd MMMM, yyyy")
                dateFormatted = sdf2.format(d)
            }
            return dateFormatted
        }

        fun getDayOfMonthSuffix(date: String): String {
            var result = ""
            var splitedDate = arrayOfNulls<String>(0)
            if (date.isNotEmpty()) {
                splitedDate = date.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val n = splitedDate[0]!!.toInt()
                Preconditions.checkArgument(
                    n in 1..31,
                    "illegal day of month: $n"
                )
                if (n in 11..13) {
                    result = "th"
                }
                when (n % 10) {
                    1 -> {
                        result = "st"
                        result = "nd"
                        result = "rd"
                        result = "th"
                    }

                    2 -> {
                        result = "nd"
                        result = "rd"
                        result = "th"
                    }

                    3 -> {
                        result = "rd"
                        result = "th"
                    }

                    else -> result = "th"
                }
            }
            val resultMonth: Array<String?> = DateAndTimeUtils.getMonthAndYearFromGivenDate(date)
            val finalDate = splitedDate[0] + result + " " + resultMonth[0]
            return finalDate
        }
    }
}
package org.intelehealth.abdm.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object DateTimeUtils {

    @JvmStatic
    fun getCurrentDateTime(): String {
        val date: DateFormat = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.ENGLISH)
        val todayDate = Date()
        return date.format(todayDate)
    }
}
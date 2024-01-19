package org.intelehealth.klivekit.utils.extensions

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Created by Vaghela Mithun R. on 03-08-2023 - 20:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

fun String.toDate(format: String): Date {
    return DateTimeUtils.parseUTCDate(this, format)
}

fun String.toLocalDateFormat(format: String): String {
    return this.toDate(DateTimeUtils.DB_FORMAT).toWeekDays(format)
}

fun String.milliToLogTime(format: String): String {
    val different = System.currentTimeMillis() - this.toLong()
    val days = TimeUnit.MILLISECONDS.toDays(different)
    val hours = TimeUnit.MILLISECONDS.toHours(different)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(different)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(different)

    return if (days >= 1) Calendar.getInstance().let {
        it.timeInMillis = this.toLong()
        return@let it.time
    }.toWeekDaysWithTime(format)
    else if (hours >= 1) if (hours.toInt() == 1) "An hr ago" else "$hours hrs ago"
    else if (minutes >= 1) if (minutes.toInt() == 1) "A min ago" else "$minutes mins ago"
    else if (seconds >= 1) if (seconds.toInt() == 1) "A sec ago" else "$seconds secs ago"
    else "Now"
}

fun Date.toWeekDays(format: String): String {
    return if (DateTimeUtils.isToday(this)) "Today"
    else if (DateTimeUtils.isYesterday(this)) "Yesterday"
    else DateTimeUtils.formatToLocalDate(this, format)
}

fun Date.toWeekDaysWithTime(format: String): String {
    val time = DateTimeUtils.formatToLocalDate(this, DateTimeUtils.TIME_FORMAT)
    return if (DateTimeUtils.isToday(this)) "Today at $time"
    else if (DateTimeUtils.isYesterday(this)) "Yesterday at $time"
    else DateTimeUtils.formatToLocalDate(this, format)
}

fun String.span(@ColorRes colorRes: Int, context: Context) = SpannableString(this).apply {
    setSpan(
        ForegroundColorSpan(
            ContextCompat.getColor(context, colorRes)
        ), 0, this.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}
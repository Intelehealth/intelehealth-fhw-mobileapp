package org.intelehealth.klivekit.utils.extensions

import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.util.TimeUtils
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

/**
 * Created by Vaghela Mithun R. on 03-08-2023 - 20:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

fun String.toDate(format: String): Date {
    return DateTimeUtils.parseUTCDate(this, format)
}

fun String.toLocalDateFormat(format: String): String {
    val date = this.toDate(DateTimeUtils.DB_FORMAT)
    return if (DateTimeUtils.isToday(date)) "Today"
    else if (DateTimeUtils.isYesterday(date)) "Yesterday"
    else DateTimeUtils.formatIsdDate(this.toDate(DateTimeUtils.DB_FORMAT), format)
}

fun String.span(@ColorRes colorRes: Int, context: Context) = SpannableString(this).apply {
    setSpan(
        ForegroundColorSpan(
            ContextCompat.getColor(context, colorRes)
        ), 0, this.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}
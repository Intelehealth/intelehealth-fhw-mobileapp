package org.intelehealth.klivekit.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Vaghela Mithun R. on 03-08-2023 - 19:38.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class DateTimeUtils {
    public static final String DB_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String MESSAGE_TIME_FORMAT = "h:mm a";
    public static final String MESSAGE_DAY_FORMAT = "EEE, dd MMM yyyy";
    public static final String TIME_ZONE_UTC = "UTC";
    public static final String TIME_ZONE_ISD = "Asia/Kolkata";

    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat getSimpleDateFormat(String format, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (timeZone != null)
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        return sdf;
    }

//    public static String formatDate(String date, String format) {
//
//    }

    public static String formatIsdDate(Date date, String format) {
        SimpleDateFormat sdf = getSimpleDateFormat(format, TIME_ZONE_ISD);
        return sdf.format(date);
    }

    public static Date parseDate(String date, String format, String timeZone) {
        SimpleDateFormat sdf = getSimpleDateFormat(format, timeZone);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return getCurrentDate();
        }
    }

    public static Date parseUTCDate(String date, String format) {
        return parseDate(date, format, TIME_ZONE_UTC);
    }

    public static Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.getTime();
    }

    public static String getCurrentDateWithDBFormat() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat sdf = getSimpleDateFormat(DB_FORMAT, TIME_ZONE_UTC);
        return sdf.format(calendar.getTime());
    }

    public static boolean isToday(Date date) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            return LocalDate.now().compareTo(new LocalDate(date.getTime())) == 0;
//        } else {
        return DateUtils.isToday(date.getTime());
//        }
    }

    public static boolean isYesterday(Date date) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            return LocalDate.now().compareTo(LocalDate) == 0;
//        } else {
        return DateUtils.isToday(date.getTime() + DateUtils.DAY_IN_MILLIS);
//        }
    }
}

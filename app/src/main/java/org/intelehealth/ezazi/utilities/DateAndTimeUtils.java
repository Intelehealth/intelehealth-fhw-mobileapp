package org.intelehealth.ezazi.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.klivekit.utils.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.intelehealth.ezazi.R;


public class DateAndTimeUtils {
    private static final String TAG = "DateAndTimeUtils";
    public static final String FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static String twoMinutesAgo(String timeStamp) throws ParseException {
        // NOTE: Since server error -> "The encounter datetime should be between the visit start and stop dates."

//        long FIVE_MINS_IN_MILLIS = 2 * 60 * 1000;
        long TWO_MINS_IN_MILLIS = 2 * 60 * 1000;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long time = df.parse(timeStamp).getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time - TWO_MINS_IN_MILLIS);

        return df.format(calendar.getTime());
    }

    public static float getFloat_Age_Year_Month(String date_of_birth) {
        float year_month = 0;

        if (date_of_birth == null) return 0;

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(date_of_birth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) return 0;
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        int xyears, xmonths;
        String x_format = "";

        if (period.getYears() > 0)
            xyears = period.getYears();
        else
            xyears = 0;
        if (period.getMonths() > 0)
            xmonths = period.getMonths();
        else
            xmonths = 0;

        x_format = xyears + "." + xmonths;
        year_month = Float.parseFloat(x_format);

        return year_month;
    }

    public String currentDateTime() {
//        Locale.setDefault(Locale.getDefault());
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        //  DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getDefault());
        return date.format(calendar.getTime());
    }

    public static String currentDateTimeInUTC(String format) {
//        Locale.setDefault(Locale.getDefault());
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        //  DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return date.format(calendar.getTime());
    }

    public String twoMinuteDelayTime() {
//        Locale.setDefault(Locale.getDefault());
//        DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        //  DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(DateTimeUtils.getUTCTimeZone());
        calendar.add(Calendar.MINUTE, 2);
        return DateTimeUtils.formatDate(calendar.getTime(), AppConstants.UTC_FORMAT, DateTimeUtils.getUTCTimeZone());
    }

    public static int getAge(String s, Context context) {
        if (s == null) return 0;

        SessionManager sessionManager = new SessionManager(context);
        String language = sessionManager.getAppLanguage();
        Log.d("LANG", "LANG: " + sessionManager.getAppLanguage());

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        if (language.matches("ar") || language.equals("ar") || language == "ar") {
            int year = Integer.parseInt(components[2]);
            int month = Integer.parseInt(components[1]);
            int day = Integer.parseInt(components[0]);
            LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
            LocalDate now = new LocalDate();                    //Today's date
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
            return period.getYears();

        } else {
            int year = Integer.parseInt(components[0]);
            int month = Integer.parseInt(components[1]);
            int day = Integer.parseInt(components[2]);
            LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
            LocalDate now = new LocalDate();                    //Today's date
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
            return period.getYears();

        }

    }

    //calculate year, month, days from two date
    public static String getAgeInYearMonth(String s, Context context) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        //call to function to pass this year and month for age mindmaps questions...
        //getAge_Year_Month(year, month, day);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        String tyears = "", tmonth = "", tdays = "";
        //String xyears = "", xmonths = "";

        if (period.getYears() > 0) {
            tyears = period.getYears() + " " + context.getResources().getString(R.string.years);
            //xyears = String.valueOf(period.getYears());
        }
        if (period.getMonths() > 0) {
            tmonth = period.getMonths() + " " + context.getResources().getString(R.string.months);
            //xmonths = String.valueOf(period.getMonths());
        }
        if (period.getDays() > 0)
            tdays = period.getDays() + " " + context.getResources().getString(R.string.days);

        age = tyears + " " + tmonth + " " + tdays;

        return age;
    }

    //calculate year, month, days from two date
    @SuppressLint("SimpleDateFormat")
    public static String getAgeInYearMonthNew(String s, Context context) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        assert date != null;
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        //call to function to pass this year and month for age mindmaps questions...
        //getAge_Year_Month(year, month, day);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
        String age = "";

        if (period.getYears() > 0) {
            age = period.getYears() + " " + context.getResources().getString(R.string.years);
        }

        if (period.getMonths() > 0) {
            String m = period.getMonths() + " " + context.getResources().getString(R.string.months);
            age = age.length() > 0 ? age + " - " + m : m;
        }

        if (period.getDays() > 0) {
            String d = period.getDays() + " " + context.getResources().getString(R.string.days);
            age = age.length() > 0 ? age + " - " + d : d;
        }

        return age;
    }

    public static String getAgeInYearMonth(String s) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat(DateTimeUtils.DD_MMM_YYYY, Locale.getDefault());
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        LocalDate birthdate = new LocalDate(year, month + 1, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        String tyears = "0", tmonth = "0", tdays = "0";

        if (period.getYears() > 0)
            tyears = "" + period.getYears();

        if (period.getMonths() > 0)
            tmonth = "" + period.getMonths();

        if (period.getDays() > 0)
            tdays = "" + period.getDays();

        age = tyears + " " + tmonth + " " + tdays;

        return age;
    }

    public static String getAgeInYears(String s, Context context) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) return s;
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        //call to function to pass this year and month for age mindmaps questions...
        //getAge_Year_Month(year, month, day);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        String tyears = "";
        //String xyears = "", xmonths = "";

        if (period.getYears() > 0) {
            tyears = period.getYears() + " " + context.getResources().getString(R.string.years);
            //xyears = String.valueOf(period.getYears());
        }
        age = tyears;
        return age;
    }

    public static String getFormatedDateOfBirth(String oldformatteddate) {

        DateFormat originalFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = originalFormat.parse(oldformatteddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) {
            originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                date = originalFormat.parse(oldformatteddate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return targetFormat.format(date);
    }

    public static String getFormatedDateOfBirthAsView(String oldformatteddate) {
        DateFormat originalFormat = new SimpleDateFormat(DateTimeUtils.YYYY_MM_DD_HYPHEN, Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat(DateTimeUtils.DD_MMM_YYYY, Locale.getDefault());
        Date date = null;
        try {
            date = originalFormat.parse(oldformatteddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        return formattedDate;

    }


    public String currentDateTimeInHome() {
        DateFormat date = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public String getcurrentDateTime() {
        DateFormat date = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public static String SimpleDatetoLongFollowupDate(String dateString) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            DateFormat targetFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
        return formattedDate;
    }

    public static String SimpleDatetoLongDate(String dateString) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            DateFormat targetFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
        return formattedDate;
    }

    public static int getMonth(String s1) {
        if (s1 == null) return 0;

        String[] components = s1.split("\\-");

        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[2]);

//        LocalDate birthdate1 = new LocalDate(year, month + 1, day);          //Birth date
        LocalDate birthdate1 = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate1, now, PeriodType.yearMonthDay());
        return period.getMonths();
    }

    public static String formatDateFromOnetoAnother(String date, String sourceFormat, String anotherFormat) {

        String result = "";
        SimpleDateFormat sdf;
        SimpleDateFormat sdf1;

        try {
            sdf = new SimpleDateFormat(sourceFormat, Locale.getDefault());
            sdf1 = new SimpleDateFormat(anotherFormat, Locale.getDefault());
            result = sdf1.format(sdf.parse(date));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return "";
        } finally {
            sdf = null;
            sdf1 = null;
        }
        return result;
    }

    public static String[] getMonthAndYearFromGivenDate(String date) {
        String[] result = new String[0];
        if (date != null && !date.isEmpty()) {

            String[] dateSplit = date.split("/");
            String month = dateSplit[1];
            String day = dateSplit[0];
            String year = dateSplit[2];


            String monthString = "";
            switch (month) {
                case "01":
                    monthString = "January";
                    break;
                case "02":
                    monthString = "February";
                    break;
                case "03":
                    monthString = "March";
                    break;
                case "04":
                    monthString = "April";
                    break;
                case "05":
                    monthString = "May";
                    break;
                case "06":
                    monthString = "June";
                    break;
                case "07":
                    monthString = "July";
                    break;
                case "08":
                    monthString = "August";
                    break;
                case "09":
                    monthString = "September";
                    break;
                case "10":
                    monthString = "October";
                    break;
                case "11":
                    monthString = "November";
                    break;
                case "12":
                    monthString = "December";
                    break;

            }
            result = new String[]{monthString, year};
        }
        return result;


    }

    public static String getDateWithDayAndMonthFromDDMMFormat(String date) {
        Log.d(TAG, "getDateWithDayAndMonthFromDDMMFormat: date : " + date);
        String finalDate = "";
        if (date != null && !date.isEmpty()) {
            String[] dateSplit = date.split("/");
            String month = dateSplit[1];
            String day = dateSplit[0];
            if (!month.isEmpty() && month.length() == 1) {
                month = "0" + month;
            }


            String monthString = "";
            switch (month) {
                case "01":
                    monthString = "Jan";
                    break;
                case "02":
                    monthString = "Feb";
                    break;
                case "03":
                    monthString = "March";
                    break;
                case "04":
                    monthString = "April";
                    break;
                case "05":
                    monthString = "May";
                    break;
                case "06":
                    monthString = "June";
                    break;
                case "07":
                    monthString = "July";
                    break;
                case "08":
                    monthString = "Aug";
                    break;
                case "09":
                    monthString = "Sept";
                    break;
                case "10":
                    monthString = "Oct";
                    break;
                case "11":
                    monthString = "Nov";
                    break;
                case "12":
                    monthString = "Dec";
                    break;

            }

            finalDate = day + " " + monthString;


        }
        return finalDate;


    }

    public static String convertDateToYyyyMMddFormat(String dateToConvert) {
        Log.d(TAG, "convertDateToYyyyMMddFormat: dateToConvert : " + dateToConvert);

        java.text.DateFormat inputFormat = new SimpleDateFormat(DateTimeUtils.DD_MMM_YYYY);
        // java.text.DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy"); //gives month name
        java.text.DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date date = null;
        try {
            date = inputFormat.parse(dateToConvert);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputFormat.format(date);
    }

    public static String formatAgeInYearsMonthsDate(Context context, int year, int month, int day) {
        String age = "";
        if (year < 1) {
            age = month + " " + context.getResources().getString(R.string.identification_screen_text_months) + " - " + day + " " + context.getResources().getString(R.string.days);
        } else if (year < 3) {
            age = year + " " + context.getResources().getString(R.string.identification_screen_text_years) + " - " + month + " " + context.getResources().getString(R.string.identification_screen_text_months);
        } else {
            age = year + " " + context.getResources().getString(R.string.identification_screen_text_years);
        }
        return age;
    }

    public static String getCurrentDateInDDMMYYYY() {
        Date cDate = new Date();
        String fDate = new SimpleDateFormat("dd/MM/yyyy").format(cDate);
        return fDate;
    }

    public static int getMinutesDifferentFromUTC(String date, String format) {
        return 0;
    }

}

package org.intelehealth.app.utilities;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.intelehealth.app.R;


public class DateAndTimeUtils {
    private static final String TAG = "DateAndTimeUtils";

    public static float getFloat_Age_Year_Month(String date_of_birth) {
        float year_month = 0;

        if (date_of_birth == null) return 0;

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(date_of_birth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

        if (period.getYears() > 0) xyears = period.getYears();
        else xyears = 0;
        if (period.getMonths() > 0) xmonths = period.getMonths();
        else xmonths = 0;

        x_format = xyears + "." + xmonths;
        year_month = Float.parseFloat(x_format);

        return year_month;
    }

    public String currentDateTime() {
        Locale.setDefault(Locale.ENGLISH);
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public static int getAge(String s, Context context) {
        if (s == null) return 0;

        SessionManager sessionManager = new SessionManager(context);
        String language = sessionManager.getAppLanguage();
        Log.d("LANG", "LANG: " + sessionManager.getAppLanguage());

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
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

    public static String getAge_FollowUp(String s, Context context) {
        Log.d(TAG, "getAge_FollowUp: s: " + s);
        Log.d("TAG", "getAge_FollowUp: s : " + s);
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
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

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
        String age = "";
        String tyears = "", tmonth = "", tdays = "";

       /* if (period.getYears() > 0) {
            tyears = String.valueOf(period.getYears());
        }*/

        if (period.getValue(0) > 0) {  // o index -> years
            tyears = String.valueOf(period.getValue(0));
            age = tyears;
            Log.d("TAG", "getAge_FollowUp: s : " + age);
        }

        return age;
    }


    //calculate year, month, days from two date
    public static String getAgeInYearMonth(String s, Context context) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
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


    public static String getAgeInYearMonth(String s) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
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

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        String tyears = "0", tmonth = "0", tdays = "0";

        if (period.getYears() > 0) tyears = "" + period.getYears();

        if (period.getMonths() > 0) tmonth = "" + period.getMonths();

        if (period.getDays() > 0) tdays = "" + period.getDays();

        age = tyears + " " + tmonth + " " + tdays;

        return age;
    }

    public static String getAgeInYears(String s, Context context) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
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

        DateFormat originalFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(oldformatteddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) {
            originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            try {
                date = originalFormat.parse(oldformatteddate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return targetFormat.format(date);
    }

    public static String getFormatedDateOfBirthAsView(String oldformatteddate) {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
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
        DateFormat date = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public static String SimpleDatetoLongFollowupDate(String dateString) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
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
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
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
            sdf = new SimpleDateFormat(sourceFormat, Locale.ENGLISH);
            sdf1 = new SimpleDateFormat(anotherFormat, Locale.ENGLISH);
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

    public static String date_formatter(String dateString, String format, String result_format) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat(format, Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat(result_format, Locale.ENGLISH);
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().recordException(ex);
            Log.v("SearchPatient", "date_ex: " + ex);
        }
        return formattedDate;
    }

    /**
     * This function is used to calculate value like Eg: '2 hours ago' or '2 minutes ago'.
     *
     * @param datetime
     * @return
     */
    public static String timeAgoFormat(String datetime) {
        String time = "";

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            long date = format.parse(datetime).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(date, now, DateUtils.MINUTE_IN_MILLIS);
            time = String.valueOf(ago);
            Log.v("TimeAgo", "TimeAgo: " + time);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }

    public static String parse_DateToddMMyyyy(String time) {
        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "dd MMM yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }


    public static String getDisplayDateForApp(String date) {
        String finalDate = "";
        if (date != null && !date.isEmpty()) {

            String[] dateSplit = date.split("-");
            String year = dateSplit[0];
            String month = dateSplit[1];
            String day = dateSplit[2];


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

            // finalDate = day + " " + monthString + " " + year;
            finalDate = monthString + " " + day + ", " + year;

        }
        return finalDate;


    }

    public static String getCurrentDateNew() {
        Date cDate = new Date();
        String fDate = new SimpleDateFormat("dd/MM/yyyy").format(cDate);
        Log.d("TAG", "getCurrentDateNew: fDate : " + fDate);
        return fDate;
    }

    public static String getCurrentDateInDDMMYYYYFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    public static String getOneMonthAheadDateInDDMMYYYYFormat() {
        //get date with one month ahead of current date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));
    }

    public static String getDisplayDateAndTime(String dateTime) {
        Log.d(TAG, "getDisplayDateAndTime: dateTime : " + dateTime);
        String finalDate = "";
        String hourFormated = "";
        String textTime;
        if (dateTime != null && !dateTime.isEmpty()) {

            String[] splitedString = dateTime.split("\\s+");
            String[] splitedTime = splitedString[1].split(":");
            if (Integer.parseInt(splitedTime[0]) >= 12) {
                hourFormated = getTwelveHourFormat(splitedTime[0]);
                textTime = "pm";
            } else {
                hourFormated = splitedTime[0];
                textTime = "am";
            }
            String timeDisplay = hourFormated + ":" + splitedTime[1] + " " + textTime;
            String displayDate = getDateWithDayAndMonth(splitedString[0]);
            finalDate = displayDate + ", at " + timeDisplay;
        }
        return finalDate;


    }

    public static String getDisplayDateAndTimeFromDDMMFormat(String inputDate) {
        String finalDate = "";
        String hourFormated = "";
        String textTime;
        if (inputDate != null && !inputDate.isEmpty()) {

            String[] splitedString = inputDate.split("/");
            String[] splitedTime = splitedString[1].split(":");
            if (Integer.parseInt(splitedTime[0]) > 12) {
                hourFormated = getTwelveHourFormat(splitedTime[0]);
                textTime = "pm";
            } else {
                hourFormated = splitedTime[0];
                textTime = "am";
            }
            String timeDisplay = hourFormated + ":" + splitedTime[1] + " " + textTime;
            String displayDate = getDateWithDayAndMonth(splitedString[0]);
            finalDate = displayDate + ", at " + timeDisplay;
        }
        return finalDate;


    }

    public static String getTwelveHourFormat(String hour) {
        String hourString = "";
        switch (hour) {
            case "12":
                hourString = "12";
                break;
            case "13":
                hourString = "01";
                break;
            case "14":
                hourString = "02";
                break;
            case "15":
                hourString = "03";
                break;
            case "16":
                hourString = "04";
                break;
            case "17":
                hourString = "05";
                break;
            case "18":
                hourString = "06";
                break;
            case "19":
                hourString = "07";
                break;
            case "20":
                hourString = "08";
                break;
            case "21":
                hourString = "09";
                break;
            case "22":
                hourString = "10";
                break;
            case "23":
                hourString = "11";
                break;
            case "24":
                hourString = "12";
                break;

        }
        return hourString;
    }

    public static String getDateWithDayAndMonth(String date) {
        String finalDate = "";
        if (date != null && !date.isEmpty()) {

            String[] dateSplit = date.split("-");
            String year = dateSplit[0];
            String month = dateSplit[1];
            String day = dateSplit[2];


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

    public static String getDateInDDMMMMYYYYFormat(String inputDate) {
        String dateFormatted = "";
        //input date must be in dd/mm/yyyy format
        if (inputDate != null && !inputDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date d = null;
            try {
                d = sdf.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMMM, yyyy");
            dateFormatted = sdf2.format(d);

        }
        return dateFormatted;
    }

    public static String convertDateToYyyyMMddFormat(String dateToConvert) {
        Log.d(TAG, "convertDateToYyyyMMddFormat: dateToConvert : " + dateToConvert);

        java.text.DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
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

    public static String getTodaysDateInRequiredFormat(String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    private static Date convertStringToDateObject(String date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date parsedDate = null;

        try {
            parsedDate = simpleDateFormat.parse(date);
        } catch (ParseException exception) {
            exception.printStackTrace();
        }

        return parsedDate;
    }

    public static Calendar convertStringToCalendarObject(String date, String format) {
        Calendar calendar = Calendar.getInstance();
        Date parsedDate = convertStringToDateObject(date, format);

        if (parsedDate != null) {
            calendar.setTime(parsedDate);
        }

        return calendar;
    }

    public static long getTodaysDateInMilliseconds() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        return cal.getTimeInMillis();
    }

    public static long getEndDateInMilliseconds(String date, String format) {
        Calendar calendar = Calendar.getInstance();
        Date parsedDate = convertStringToDateObject(date, format);

        if (parsedDate != null) {
            calendar.setTime(parsedDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);
            calendar.clear();
            calendar.set(year, month, day, 23, 59, 59);
        }

        return calendar.getTimeInMillis();
    }

    public static long convertStringDateToMilliseconds(String date, String format) {
        Calendar calendarObject = convertStringToCalendarObject(date, format);
        return calendarObject.getTimeInMillis();
    }

    public static String convertDateObjectToString(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return simpleDateFormat.format(date);
    }

    public static boolean isGivenDateBetweenTwoDates(String date, String startDate, String endDate, String format) {
        Date createdDateObject = convertStringToDateObject(date, format);
        Date startDateObject = convertStringToDateObject(startDate, format);
        Date endDateObject = convertStringToDateObject(endDate, format);
        return createdDateObject.getTime() >= startDateObject.getTime() && createdDateObject.getTime() <= endDateObject.getTime();
    }

    public static String convertMillisecondsToHoursAndMinutes(long timeInMilliseconds) {
        int minutes = (int) ((timeInMilliseconds / (1000 * 60)) % 60);
        int hours = (int) ((timeInMilliseconds / (1000 * 60 * 60)) % 24);
        return String.format(Locale.ENGLISH, "%dh %dm", hours, minutes);
    }
}
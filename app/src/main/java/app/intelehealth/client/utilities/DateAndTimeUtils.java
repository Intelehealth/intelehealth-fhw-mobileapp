package app.intelehealth.client.utilities;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import app.intelehealth.client.R;


public class DateAndTimeUtils {

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

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        if (period.getMonths() != 0)
            age = period.getYears() + " " + context.getResources().getString(R.string.years) + ", " + period.getMonths() + " " + context.getResources().getString(R.string.months);
        else
            age = period.getYears() + " " + context.getResources().getString(R.string.years);

        return age;
    }

    public static String getSubtractedPulledExcutedTime(String lastPulledTime) {
        Calendar now = Calendar.getInstance();


        return "";
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
        DateFormat date = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
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
            Crashlytics.getInstance().core.logException(ex);
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
            Crashlytics.getInstance().core.logException(ex);
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
            Crashlytics.getInstance().core.logException(e);
            return "";
        } finally {
            sdf = null;
            sdf1 = null;
        }
        return result;
    }

}

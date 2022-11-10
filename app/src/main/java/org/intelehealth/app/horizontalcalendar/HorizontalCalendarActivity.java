package org.intelehealth.app.horizontalcalendar;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HorizontalCalendarActivity extends AppCompatActivity {
    private static final String TAG = "HorizontalCalendarActiv";
    RecyclerView rvHorizontalCal;
    int currentMonth;
    int currentYear;
    // Calendar calendar;
    ImageView ivPrevMonth, ivNextMonth;
    int monthNumber;
    String monthNAmeFromNo;
    TextView tvSelectedMonthYear;
    Calendar calendarInstance;
    String yearToCompare = "";
    String monthToCompare = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_calendar);
        rvHorizontalCal = findViewById(R.id.rv_horizontal_cal);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvHorizontalCal.setLayoutManager(linearLayoutManager);
        ivPrevMonth = findViewById(R.id.iv_prev_month1);
        ivNextMonth = findViewById(R.id.iv_next_month1);
        tvSelectedMonthYear = findViewById(R.id.tv_selected_month_year);

        calendarInstance = Calendar.getInstance();
        //  calendar = Calendar.getInstance();
        currentMonth = calendarInstance.getActualMaximum(Calendar.MONTH);
        currentYear = calendarInstance.get(Calendar.YEAR);
        monthToCompare = String.valueOf(currentMonth);
        yearToCompare = String.valueOf(currentYear);

        if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
            enableDisablePreviousButton(false);

        } else {
            enableDisablePreviousButton(true);

        }
        getAllDatesOfSelectedMonth(calendarInstance, true);

        ivNextMonth.setOnClickListener(v -> {
            getNextMonthDates();
        });
        ivPrevMonth.setOnClickListener(v -> {
            getPreviousMonthDates();
        });
    }

    private void getAllDatesOfSelectedMonth(Calendar calendar, boolean isCurrentMonth) {

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay;
        if (isCurrentMonth) {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            currentDay = 1;
        }
        int daysLeft = lastDay - currentDay;

        CalendarModel calendarModel;
        SimpleDateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE");

        List<CalendarModel> listOfDates = new ArrayList<>();
        for (int i = currentDay; i <= lastDay; i++) {

            try {
                String inputDate = i + "-" + currentMonth + "-" + currentYear;
                Date date = inFormat.parse(inputDate);
                if (date != null) {
                    String dayForDate = outFormat.format(date);
                    String dayForDateFinal = dayForDate.substring(0, 3);

                    if (i == currentDay) {
                        calendarModel = new CalendarModel("Today", i, currentDay, true);

                    } else {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, false);

                    }

                    listOfDates.add(calendarModel);

                } else {
                }

            } catch (ParseException e) {
                Log.d(TAG, "getAllDatesOfSelectedMonth: e : " + e.getLocalizedMessage());
                e.printStackTrace();
            }


        }

        HorizontalCalendarViewAdapter horizontalCalendarViewAdapter = new HorizontalCalendarViewAdapter(this, listOfDates);
        rvHorizontalCal.setAdapter(horizontalCalendarViewAdapter);

        System.out.println("getAllDatesOfSelectedMonth currentMonth: " + currentMonth);
        System.out.println("getAllDatesOfSelectedMonth Day: " + lastDay);
        System.out.println("getAllDatesOfSelectedMonth Day : " + currentDay);
        System.out.println("getAllDatesOfSelectedMonth are " + daysLeft + " days left in the month.");
    }

    private void newCode() {
        /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        /* ends after 1 month from now */
        //Calendar endDate = Calendar.getInstance();
        //endDate.add(Calendar.MONTH, 1);

        int lastDay = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = calendarInstance.get(Calendar.DAY_OF_MONTH);
        int daysLeft = lastDay - currentDay;
        tvSelectedMonthYear.setText(getMonthNumberByName(String.valueOf(currentMonth)) + ", " + currentYear);

        Log.d(TAG, "011newCode: currentMonth : " + currentMonth);
        CalendarModel calendarModel;
        SimpleDateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE");

        List<CalendarModel> listOfDates = new ArrayList<>();
        for (int i = currentDay; i <= lastDay; i++) {

            try {
                String inputDate = i + "-" + currentMonth + "-" + currentYear;
                Date date = inFormat.parse(inputDate);
                if (date != null) {
                    String dayForDate = outFormat.format(date);
                    String dayForDateFinal = dayForDate.substring(0, 3);

                    Log.d(TAG, "011newCode: dayForDate :" + dayForDateFinal + ", InputDate : " + inputDate);
                    if (i == currentDay) {
                        calendarModel = new CalendarModel("Today", i, currentDay, true);

                    } else {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, false);

                    }

                    listOfDates.add(calendarModel);

                } else {
                    Log.d(TAG, "011newCode: date is null");
                }

            } catch (ParseException e) {
                Log.d(TAG, "newCode: e : " + e.getLocalizedMessage());
                e.printStackTrace();
            }


        }

        HorizontalCalendarViewAdapter horizontalCalendarViewAdapter = new HorizontalCalendarViewAdapter(this, listOfDates);
        rvHorizontalCal.setAdapter(horizontalCalendarViewAdapter);

        System.out.println("011Last currentMonth: " + currentMonth);
        System.out.println("011Last Day: " + lastDay);
        System.out.println("011Current Day : " + currentDay);
        System.out.println("011There are " + daysLeft + " days left in the month.");

    }

    private void getPreviousMonthDates() {
        calendarInstance.add(Calendar.MONTH, -1);
        Date monthNameNEw = calendarInstance.getTime();
        String monthName = calendarInstance.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

            String[] dateSplit = formateDate.split("/");
            yearToCompare = dateSplit[2];
            monthToCompare = dateSplit[1];
            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);

            if (monthYear.length > 0) {
                String selectedPrevMonth = monthYear[0];
                String selectedPrevMonthYear = monthYear[1];
                tvSelectedMonthYear.setText(selectedPrevMonth + ", " + selectedPrevMonthYear);
                if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
                    enableDisablePreviousButton(false);

                    getAllDatesOfSelectedMonth(calendarInstance, true);

                } else {
                    enableDisablePreviousButton(false);

                    getAllDatesOfSelectedMonth(calendarInstance, false);

                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void getNextMonthDates() {
        enableDisablePreviousButton(true);

        calendarInstance.add(Calendar.MONTH, 1);
        Date monthNameNEw = calendarInstance.getTime();
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);
            String selectedNextMonth;
            String selectedMonthYear;

            if (monthYear.length > 0) {
                selectedNextMonth = monthYear[0];
                selectedMonthYear = monthYear[1];
                tvSelectedMonthYear.setText(selectedNextMonth + ", " + selectedMonthYear);
                if (selectedNextMonth.equals(String.valueOf(currentMonth)) && selectedMonthYear.equals(String.valueOf(currentYear))) {
                    getAllDatesOfSelectedMonth(calendarInstance, true);
                } else {
                    getAllDatesOfSelectedMonth(calendarInstance, false);

                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private String getMonthNumberByName(String monthNo) {

        switch (monthNo) {
            case "01":
                monthNAmeFromNo = "January";
                break;
            case "02":
                monthNAmeFromNo = "February";
                break;
            case "03":
                monthNAmeFromNo = "March";
                break;
            case "04":
                monthNAmeFromNo = "April";
                break;
            case "05":
                monthNAmeFromNo = "May";
                break;
            case "06":
                monthNAmeFromNo = "June";
                break;
            case "07":
                monthNAmeFromNo = "July";
                break;
            case "08":
                monthNAmeFromNo = "August";
                break;
            case "09":
                monthNAmeFromNo = "September";
                break;
            case "10":
                monthNAmeFromNo = "October";
                break;
            case "11":
                monthNAmeFromNo = "November";

                break;
            case "12":
                monthNAmeFromNo = "December";
                break;
        }
        return monthNAmeFromNo;

    }

    private void enableDisablePreviousButton(boolean wantToEnable) {
        if (wantToEnable) {
            ivPrevMonth.setEnabled(true);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        } else {
            ivPrevMonth.setEnabled(true);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.font_black_3), android.graphics.PorterDuff.Mode.SRC_IN);

        }
    }
}
package org.intelehealth.app.horizontalcalendar;

import android.os.Bundle;
import org.intelehealth.app.utilities.CustomLog;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HorizontalCalendarActivity extends BaseActivity {
    private static final String TAG = "HorizontalCalendarActiv";
    RecyclerView rvHorizontalCal;
    int currentMonth;
    int currentYear;
    ImageView ivPrevMonth, ivNextMonth;
    TextView tvSelectedMonthYear;
    Calendar calendarInstance;
    String yearToCompare = "";
    String monthToCompare = "";
    SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_calendar);
        initUI();
    }

    private void initUI() {
        rvHorizontalCal = findViewById(R.id.rv_horizontal_cal);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvHorizontalCal.setLayoutManager(linearLayoutManager);
        ivPrevMonth = findViewById(R.id.iv_prev_month1);
        ivNextMonth = findViewById(R.id.iv_next_month1);
        tvSelectedMonthYear = findViewById(R.id.tv_selected_month_year);
        calendarInstance = Calendar.getInstance();
        currentMonth = calendarInstance.getActualMaximum(Calendar.MONTH);
        currentYear = calendarInstance.get(Calendar.YEAR);
        monthToCompare = String.valueOf(currentMonth);
        yearToCompare = String.valueOf(currentYear);
        sessionManager = new SessionManager(HorizontalCalendarActivity.this);

        if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
            enableDisablePreviousButton(false);

        } else {
            enableDisablePreviousButton(true);

        }
        getAllDatesOfSelectedMonth(calendarInstance, true, String.valueOf(currentMonth), String.valueOf(currentYear), String.valueOf(currentMonth));

        ivNextMonth.setOnClickListener(v -> {
            getNextMonthDates();
        });
        ivPrevMonth.setOnClickListener(v -> {
            getPreviousMonthDates();
        });
    }

    private void getAllDatesOfSelectedMonth(Calendar calendar,
                                            boolean isCurrentMonth,
                                            String selectedMonth, String selectedYear, String selectedMonthForDays) {
        CustomLog.d(TAG, "getAllDatesOfSelectedMonth: selectedMonth : "+selectedMonth);
        CustomLog.d(TAG, "getAllDatesOfSelectedMonth: selectedYear : "+selectedYear);

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
                String inputDate = i + "-" + selectedMonthForDays + "-" + selectedYear;
                Date date = inFormat.parse(inputDate);
                if (date != null) {
                    String dayForDate = outFormat.format(date);
                    String dayForDateFinal = dayForDate.substring(0, 3);

                    if (i == currentDay) {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, true, selectedMonth, selectedYear, false,selectedMonthForDays);

                    } else {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, false, selectedMonth, selectedYear, false,selectedMonthForDays);

                    }

                    listOfDates.add(calendarModel);

                } else {
                }

            } catch (ParseException e) {
                CustomLog.d(TAG, "getAllDatesOfSelectedMonth: e : " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        //  HorizontalCalendarViewAdapter horizontalCalendarViewAdapter = new HorizontalCalendarViewAdapter(this, listOfDates,this);

        rvHorizontalCal.setAdapter(new HorizontalCalendarViewAdapter(this, listOfDates, calendarModel1 -> {
            int date = calendarModel1.getDate();
            String month = calendarModel1.getSelectedMonthForDays();
            String year = calendarModel1.getSelectedYear();

            Toast.makeText(this, getResources().getString(R.string.selected_date) + " : " + date + "-" + month + "-" + year, Toast.LENGTH_SHORT).show();
        }));

    }

    private void getPreviousMonthDates() {
        calendarInstance.add(Calendar.MONTH, -1);
        Date monthNameNEw = calendarInstance.getTime();
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
                if(sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    selectedPrevMonth = StringUtils.en__hi_dob(selectedPrevMonth);
                tvSelectedMonthYear.setText(selectedPrevMonth + ", " + selectedPrevMonthYear);
                if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
                    enableDisablePreviousButton(false);

                    getAllDatesOfSelectedMonth(calendarInstance, true, monthToCompare, selectedPrevMonthYear, monthToCompare);

                } else {
                    enableDisablePreviousButton(true);

                    getAllDatesOfSelectedMonth(calendarInstance, false, monthToCompare, selectedPrevMonthYear,monthToCompare);

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
                String[] dateSplit = formateDate.split("/");

                tvSelectedMonthYear.setText(selectedNextMonth + ", " + selectedMonthYear);
                if (selectedNextMonth.equals(String.valueOf(currentMonth)) && selectedMonthYear.equals(String.valueOf(currentYear))) {
                    getAllDatesOfSelectedMonth(calendarInstance, true, selectedNextMonth, selectedMonthYear,dateSplit[1]);
                } else {
                    getAllDatesOfSelectedMonth(calendarInstance, false, selectedNextMonth, selectedMonthYear,dateSplit[1]);

                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void enableDisablePreviousButton(boolean wantToEnable) {
        if (wantToEnable) {
            ivPrevMonth.setEnabled(true);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        } else {
            ivPrevMonth.setEnabled(false);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.font_black_3), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
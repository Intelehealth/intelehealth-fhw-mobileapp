package org.intelehealth.app.ui2.calendarviewcustom;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.intelehealth.app.R;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CalendarViewDemoActivity extends AppCompatActivity {
    private static final String TAG = "CalendarViewDemoActivit";
    RecyclerView rvCalendarView;
    Spinner spinnerMonths, spinnerYear;
    CalendarViewMonthModel spinnerSelectedMonthModel;
    CalendarviewYearModel spinnerSelectedYearModel;
    Calendar calendarInstanceDefault;
    int monthTotalDays;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view_demo_ui2);

        rvCalendarView = findViewById(R.id.rv_calendarview_new);
        spinnerMonths = findViewById(R.id.spinner_months_caleview);
        spinnerYear = findViewById(R.id.spinner_year_caleview);
        calendarInstanceDefault = Calendar.getInstance();
        int currentMonth = calendarInstanceDefault.getActualMaximum(Calendar.MONTH);
        Log.d(TAG, "onCreate: currentMonth : " + currentMonth);
        int currentYear = calendarInstanceDefault.get(Calendar.YEAR);
        monthTotalDays = calendarInstanceDefault.getActualMaximum(Calendar.DAY_OF_MONTH);
        Log.d(TAG, "default: monthTotalDays : " + monthTotalDays);


        spinnerSelectedYearModel = new CalendarviewYearModel(currentYear, true);
        spinnerSelectedMonthModel = new CalendarViewMonthModel("", currentMonth, true);


        fillMonthsSpinner();
        fillYearSpinner();
        fillDatesMonthsWise("default");
        setValuesToTheMonthSpinnerForDefault(currentMonth);
        setValuesToTheYearSpinnerForDefault(currentYear);

    }

    private void setValuesToTheMonthSpinnerForDefault(int currentMonth) {
        Log.d(TAG, "setValuesToTheMonthSpinnerForDefault: currentMonth : " + currentMonth);
        switch (currentMonth) {
            case 1:
                spinnerMonths.setSelection(0, true);
                break;
            case 2:
                spinnerMonths.setSelection(1, true);
                break;
            case 3:
                spinnerMonths.setSelection(2, true);
                break;
            case 4:
                spinnerMonths.setSelection(3, true);
                break;
            case 5:
                spinnerMonths.setSelection(4, true);
                break;
            case 6:
                spinnerMonths.setSelection(5, true);
                break;
            case 7:
                spinnerMonths.setSelection(6, true);
                break;
            case 8:
                spinnerMonths.setSelection(7, true);
                break;
            case 9:
                spinnerMonths.setSelection(8, true);
                break;
            case 10:
                spinnerMonths.setSelection(9, true);
                break;
            case 11:
                spinnerMonths.setSelection(10, true);
                break;
            case 12:
                spinnerMonths.setSelection(11, true);
                break;
        }

    }

    private void setValuesToTheYearSpinnerForDefault(int currentYear) {
        Log.d(TAG, "setValuesToTheYearSpinnerForDefault: currentYear : " + currentYear);
        switch (currentYear) {
            case 2022:
                spinnerYear.setSelection(0, true);
                break;
            case 2023:
                spinnerYear.setSelection(1, true);
                break;
            case 2024:
                spinnerYear.setSelection(2, true);
                break;

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fillDatesMonthsWise(String tag) {
        Log.d(TAG, "newfillDatesMonthsWise:month " + spinnerSelectedMonthModel.getMonthNo());
        Log.d(TAG, "newfillDatesMonthsWise:year " + spinnerSelectedYearModel.getYear());

        Log.d(TAG, "fillDatesMonthsWise: tag : " + tag);
        String firstDay = "";
        String lastDay = "";

        // 1. get previous months all dates
        // 2. get current months all dates
        // 3. get next months all dates
        // Logic  - total 42 entries must be in recyclerview
        // start day  - end day

        //  String spinnerSelectedYear = spinnerYear.getSelectedItem().toString();
        //  String spinnerSelectedMonth = spinnerMonths.getSelectedItem().toString();

        if (spinnerSelectedMonthModel != null && spinnerSelectedYearModel != null) {
            YearMonth ym = YearMonth.of(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo());


            firstDay = (ym.atDay(1).getDayOfWeek().name()).substring(0, 2);
            lastDay = (ym.atEndOfMonth().getDayOfWeek().name()).substring(0, 2);

            Log.d(TAG, "fillDatesMonthsWise: -firstDay : " + firstDay);
            Log.d(TAG, "fillDatesMonthsWise: lastDay : " + lastDay);


            //maximum 42 entries
            int headerDayPosition = 111;
            int headerDayPositionForLastDay = 222;
            int noOfPrevMonthDaysRequired = 0;
            int noOfNextMonthDaysRequired = 0;

            switch (firstDay.toLowerCase()) {
                case "mo":
                    headerDayPosition = 0;
                    noOfPrevMonthDaysRequired = 0;
                    break;
                case "tu":
                    headerDayPosition = 1;
                    noOfPrevMonthDaysRequired = 1;

                    break;
                case "we":
                    headerDayPosition = 2;
                    noOfPrevMonthDaysRequired = 2;

                    break;
                case "th":
                    headerDayPosition = 3;
                    noOfPrevMonthDaysRequired = 3;

                    break;
                case "fr":
                    headerDayPosition = 4;
                    noOfPrevMonthDaysRequired = 4;

                    break;
                case "sa":
                    headerDayPosition = 5;
                    noOfPrevMonthDaysRequired = 5;

                    break;
                case "su":
                    headerDayPosition = 6;
                    noOfPrevMonthDaysRequired = 6;

                    break;

            }

            switch (lastDay.toLowerCase()) {
                case "mo":
                    headerDayPositionForLastDay = 0;
                    noOfNextMonthDaysRequired = 6;
                    break;
                case "tu":
                    headerDayPositionForLastDay = 1;
                    noOfNextMonthDaysRequired = 5;

                    break;
                case "we":
                    headerDayPositionForLastDay = 2;
                    noOfNextMonthDaysRequired = 4;

                    break;
                case "th":
                    headerDayPositionForLastDay = 3;
                    noOfNextMonthDaysRequired = 3;

                    break;
                case "fr":
                    headerDayPositionForLastDay = 4;
                    noOfNextMonthDaysRequired = 2;

                    break;
                case "sa":
                    headerDayPositionForLastDay = 5;
                    noOfNextMonthDaysRequired = 1;

                    break;
                case "su":
                    headerDayPositionForLastDay = 6;
                    noOfNextMonthDaysRequired = 0;
                    break;

            }

            //calculate total days for recyclerview
            int totalViewDays = monthTotalDays + noOfPrevMonthDaysRequired;
            int daysFromNextMonth = 42 - totalViewDays;

            Log.d(TAG, "55fillDatesMonthsWise: monthMaxDays : " + monthTotalDays);
            Log.d(TAG, "55fillDatesMonthsWise: noOfPrevMonthDaysRequired : " + noOfPrevMonthDaysRequired);
            Log.d(TAG, "55fillDatesMonthsWise: daysFromNextMonth : " + daysFromNextMonth);
            Log.d(TAG, "55fillDatesMonthsWise: totalViewDays : " + totalViewDays);

            List<CalendarviewModel> listOfPrevMonthDays = new ArrayList<>();
            List<CalendarviewModel> listOfDates = new ArrayList<>();
            List<CalendarviewModel> listOfNextMonthDays = new ArrayList<>();

            CalendarviewModel calendarviewModel;
            Calendar calForSelectedMonth = Calendar.getInstance(); //current
            calForSelectedMonth.set(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo(), 1);//for selected
            calForSelectedMonth.add(Calendar.MONTH, -1); //2022/11/1 - 1


            Calendar calForPrevMonth = Calendar.getInstance();
            calForPrevMonth.set(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo(), 1);//2022/11/1
            Log.d(TAG, "\n\n88fillDatesMonthsWise:33new tag : " + tag);
            Log.d(TAG, "88fillDatesMonthsWise:33new spinner date check1****** : " + calForPrevMonth.getTime());

            calForPrevMonth.add(Calendar.MONTH, -2); //2022/11/1 - 1

            Log.d(TAG, "88fillDatesMonthsWise:33new prev date check2****** : " + calForPrevMonth.getTime() + "\n\n");
            Log.d(TAG, "88fillDatesMonthsWise: spinneryear :" + spinnerSelectedYearModel.getYear());
            Log.d(TAG, "88fillDatesMonthsWise: spinnermonth :" + spinnerSelectedMonthModel.getMonthNo());

            int prevMonthMaxDays = calForPrevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
            Log.d(TAG, "88fillDatesMonthsWise:33new prevMonthMaxDays : " + prevMonthMaxDays);

            //i=31
            //noOfPrevMonthDaysRequired = 5
            Log.d(TAG, "fillDatesMonthsWise:99 noOfPrevMonthDaysRequired : check : " + noOfPrevMonthDaysRequired);

            if (noOfPrevMonthDaysRequired > 0) {
                for (int i = prevMonthMaxDays; i >= 1; i--) {
                    calendarviewModel = new CalendarviewModel(i, 111, headerDayPositionForLastDay, false, true, false);
                    listOfPrevMonthDays.add(calendarviewModel);
                    if (listOfPrevMonthDays.size() == noOfPrevMonthDaysRequired) {
                        break;
                    }
                }
            }


            Log.d(TAG, "fillDatesMonthsWise: listOfPrevMonthDays : " + listOfPrevMonthDays.size());
            if (listOfPrevMonthDays.size() > 0) {
                Collections.reverse(listOfPrevMonthDays);
                listOfDates.addAll(listOfPrevMonthDays);
            }


            //  int monthTotalDays = calForSelectedMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
            monthTotalDays = calForSelectedMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

            Log.d(TAG, "99fillDatesMonthsWise: selcted monthmonthTotalDays : " + monthTotalDays);

            for (int i = 1; i <= monthTotalDays; i++) {
                if (i == 1) {
                    calendarviewModel = new CalendarviewModel(i, headerDayPosition, headerDayPositionForLastDay, false, false, false);

                } else {
                    calendarviewModel = new CalendarviewModel(i, 111, headerDayPositionForLastDay, false, false, false);

                }
                listOfDates.add(calendarviewModel);
            }

            Log.d(TAG, "666fillDatesMonthsWise: listOfDates size*************** : " + listOfDates.size() + "\n\n");
            for (int i = 0; i < listOfDates.size(); i++) {
                CalendarviewModel calendarviewModel1 = listOfDates.get(i);
                Log.d(TAG, "666fillDatesMonthsWise: listOfDates dates :" + calendarviewModel1.getDate());
            }

            Calendar calForNextMonth = Calendar.getInstance();
            calForNextMonth.set(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo(), 1);
            calForNextMonth.add(Calendar.MONTH, +1);
            //     int currentMonth = calendarInstanceDefault.getActualMaximum(Calendar.MONTH);
            //    int currentYear = calendarInstanceDefault.get(Calendar.YEAR);
            Log.d(TAG, "88fillDatesMonthsWise: new next date : " + calForNextMonth.get(Calendar.YEAR) + "/" + calForNextMonth.getActualMaximum(Calendar.MONTH) + "/1");

            int nextMonthMaxDays = calForNextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
            Log.d(TAG, "fillDatesMonthsWise: nextMonthMaxDays : " + nextMonthMaxDays);

            //i=31
            //noOfPrevMonthDaysRequired = 5

            int noOfNextMonthDaysRequiredNew = 42 - listOfDates.size();
            Log.d(TAG, "fillDatesMonthsWise:99 noOfNextMonthDaysRequiredNew : check : " + noOfNextMonthDaysRequiredNew);

            if (noOfNextMonthDaysRequiredNew > 0) {
                for (int i = 1; i <= nextMonthMaxDays; i++) {
                    calendarviewModel = new CalendarviewModel(i, 111, headerDayPositionForLastDay, false, false, true);
                    listOfNextMonthDays.add(calendarviewModel);
                    if (listOfNextMonthDays.size() == noOfNextMonthDaysRequiredNew) {
                        break;
                    }
                }
            }


            listOfDates.addAll(listOfNextMonthDays);

            Log.d(TAG, "sizes : fillDatesMonthsWise: listOfDates***** : " + listOfDates.size());
            Log.d(TAG, "sizes : fillDatesMonthsWise: prev month***** : " + listOfPrevMonthDays.size());
            Log.d(TAG, "sizes : fillDatesMonthsWise: next month***** : " + listOfNextMonthDays.size());

            rvCalendarView.setHasFixedSize(true);
            rvCalendarView.setLayoutManager(new GridLayoutManager(this, 7));
            rvCalendarView.setAdapter(new CalendarviewNewAdapter(this, listOfDates, calendarModel1 -> {
                int date = calendarModel1.getDate();
                String month = calendarModel1.getSelectedMonthForDays();
                String year = calendarModel1.getSelectedYear();

                Toast.makeText(this, "Selected date : " + date + "-" + month + "-" + year, Toast.LENGTH_SHORT).show();
            }));


        } else {
            Log.d(TAG, "fillDatesMonthsWise: models are null");
        }


    }

    private void fillMonthsSpinner() {

        List<CalendarViewMonthModel> monthsList = new ArrayList<>();
        String[] monthsArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        int[] monthsNoArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        for (int i = 0; i < monthsArray.length; i++) {
            CalendarViewMonthModel model1 = new CalendarViewMonthModel(monthsArray[i], monthsNoArray[i], false);
            monthsList.add(model1);
        }

        MonthsSpinnerAdapter adapter = new MonthsSpinnerAdapter(CalendarViewDemoActivity.this, android.R.layout.simple_spinner_item, monthsList);
        spinnerMonths.setAdapter(adapter);
        spinnerMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerSelectedMonthModel = adapter.getItem(position);
                fillDatesMonthsWise("fromSpinnerMonth");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    public class MonthsSpinnerAdapter extends ArrayAdapter<CalendarViewMonthModel> {

        private Context context;
        List<CalendarViewMonthModel> listOfMonths;

        public MonthsSpinnerAdapter(Context context, int textViewResourceId, List<CalendarViewMonthModel> listOfMonths) {
            super(context, textViewResourceId, listOfMonths);
            this.context = context;
            this.listOfMonths = listOfMonths;
        }

        @Override
        public int getCount() {
            return listOfMonths.size();
        }

        @Override
        public CalendarViewMonthModel getItem(int position) {
            return listOfMonths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfMonths.get(position).getMonthName());

            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfMonths.get(position).getMonthName());

            return label;
        }
    }


    private void fillYearSpinner() {

        List<CalendarviewYearModel> yearsList = new ArrayList<>();
        int[] yearArray = {2022, 2023, 2024};
        for (int i = 0; i < yearArray.length; i++) {
            CalendarviewYearModel model1 = new CalendarviewYearModel(yearArray[i], false);
            yearsList.add(model1);
        }

        YearSpinnerAdapter adapter = new YearSpinnerAdapter(CalendarViewDemoActivity.this, android.R.layout.simple_spinner_item, yearsList);
        spinnerYear.setAdapter(adapter);
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerSelectedYearModel = adapter.getItem(position);
                fillDatesMonthsWise("fromSpinnerYear");


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    public class YearSpinnerAdapter extends ArrayAdapter<CalendarviewYearModel> {

        private Context context;
        List<CalendarviewYearModel> listOfYears;

        public YearSpinnerAdapter(Context context, int textViewResourceId, List<CalendarviewYearModel> listOfYears) {
            super(context, textViewResourceId, listOfYears);
            this.context = context;
            this.listOfYears = listOfYears;
        }

        @Override
        public int getCount() {
            return listOfYears.size();
        }

        @Override
        public CalendarviewYearModel getItem(int position) {
            return listOfYears.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfYears.get(position).getYear() + "");

            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfYears.get(position).getYear() + "");

            return label;
        }
    }
}
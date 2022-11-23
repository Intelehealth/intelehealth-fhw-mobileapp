package org.intelehealth.app.ui2.calendarviewcustom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

public class CalendarViewDemoActivity extends AppCompatActivity {
    private static final String TAG = "CalendarViewDemoActivit";
    RecyclerView rvCalendarView;
    Spinner spinnerMonths, spinnerYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view_demo_ui2);

        rvCalendarView = findViewById(R.id.rv_calendarview_new);
        spinnerMonths = findViewById(R.id.spinner_months_caleview);
        spinnerYear = findViewById(R.id.spinner_year_caleview);

        List<CalendarviewModel> listOfDates = new ArrayList<>();

        for (int i = 1; i < 31; i++) {
            CalendarviewModel calendarviewModel = new CalendarviewModel(i);
            listOfDates.add(calendarviewModel);
        }


        rvCalendarView.setHasFixedSize(true);
        rvCalendarView.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendarView.setAdapter(new CalendarviewNewAdapter(this, listOfDates, calendarModel1 -> {
            int date = calendarModel1.getDate();
            String month = calendarModel1.getSelectedMonthForDays();
            String year = calendarModel1.getSelectedYear();

            Toast.makeText(this, "Selected date : " + date + "-" + month + "-" + year, Toast.LENGTH_SHORT).show();
        }));
        fillMonthsSpinner();
        fillYearSpinner();

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

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CalendarViewMonthModel calendarViewMonthModel = adapter.getItem(position);

                Log.d(TAG, "onItemSelected: month : " + calendarViewMonthModel.getMonthName());
                Log.d(TAG, "onItemSelected: no : " + calendarViewMonthModel.getMonthNo());

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
        int[] yearArray = {2021, 2022, 2023};
        for (int i = 0; i < yearArray.length; i++) {
            CalendarviewYearModel model1 = new CalendarviewYearModel(yearArray[i], false);
            yearsList.add(model1);
        }

        YearSpinnerAdapter adapter = new YearSpinnerAdapter(CalendarViewDemoActivity.this, android.R.layout.simple_spinner_item, yearsList);
        spinnerYear.setAdapter(adapter);
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CalendarviewYearModel calendarViewMonthModel = adapter.getItem(position);

                Log.d(TAG, "onItemSelected: year : " + calendarViewMonthModel.getYear());

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
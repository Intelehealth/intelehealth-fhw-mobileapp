package org.intelehealth.ezazi.ui.dialog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.intelehealth.ezazi.R;

import java.util.Calendar;

public class DatePickerActivity extends AppCompatActivity {
    private static final String TAG = "DatePickerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);

        CalendarDialog dialog = new CalendarDialog.Builder(this)
                .title("")
                .positiveButtonLabel(R.string.ok).build();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, 4, 11);
        dialog.setMaxDate(calendar.getTimeInMillis());
        dialog.setWeekStartFromDay(CalendarDialog.DayOfWeek.SUN);
        calendar = Calendar.getInstance();
        calendar.set(2011, 4, 11);
        dialog.setMinDate(calendar.getTimeInMillis());
        dialog.setListener((day, month, year, value) -> Log.e(TAG, "Date = >" + value));
        dialog.show(getSupportFragmentManager(), "DatePicker");
    }
}

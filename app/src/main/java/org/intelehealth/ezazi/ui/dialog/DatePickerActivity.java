package org.intelehealth.ezazi.ui.dialog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.intelehealth.ezazi.R;

public class DatePickerActivity extends AppCompatActivity {
    private static final String TAG = "DatePickerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);

        CalendarDialog dialog = new CalendarDialog.Builder(this)
                .title("")
                .positiveButtonLabel(R.string.ok).build();

//        dialog.setListener((day, month, year, value) -> Log.e(TAG, "Date = >" + value));
        dialog.show(getSupportFragmentManager(), "DatePicker");
    }
}

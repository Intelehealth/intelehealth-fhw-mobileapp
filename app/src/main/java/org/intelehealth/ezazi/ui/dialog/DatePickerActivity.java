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

        DatePickerDialog dialog = new DatePickerDialog.Builder(this)
                .title("")
                .positiveButtonLabel(R.string.ok).build();
        dialog.setListener((date) -> {
            /// boolean isPM = (hours >= 12);
            // String timeString = String.format("%02d:%02d %s", (hours == 12 || hours == 0) ? 12 : hours % 12, minutes, isPM ? "PM" : "AM");
            // Log.d(TAG, "selectTime: timeString : " + timeString);

        });
        dialog.show(getSupportFragmentManager(), "DatePicker");
    }
}

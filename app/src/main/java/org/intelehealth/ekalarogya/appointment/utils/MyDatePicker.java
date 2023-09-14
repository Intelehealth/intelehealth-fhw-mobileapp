package org.intelehealth.ekalarogya.appointment.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import org.intelehealth.ekalarogya.R;

import java.util.Calendar;

public class MyDatePicker extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar mCalendar = Calendar.getInstance();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.AlertDialogTheme, (DatePickerDialog.OnDateSetListener)
                getActivity(), year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() + 1000);
        return datePickerDialog ;
    }

}

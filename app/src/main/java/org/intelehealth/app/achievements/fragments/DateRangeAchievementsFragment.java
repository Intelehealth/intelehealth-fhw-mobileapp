package org.intelehealth.app.achievements.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;

import java.time.LocalDate;

public class DateRangeAchievementsFragment extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_frag_date_range_achievements_ui2, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        LinearLayout selectFromDate = view.findViewById(R.id.layout_select_from_date);
        selectFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });
    }


    private void selectDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                    }
                }, 0, 0, 0);
        datePickerDialog.show();
    }
}



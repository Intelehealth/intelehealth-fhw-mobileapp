package org.intelehealth.ezazi.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import androidx.core.content.ContextCompat;

import com.google.android.material.chip.ChipGroup;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.DialogCalendarViewBinding;
import org.intelehealth.ezazi.databinding.RowItenMonthBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Vaghela Mithun R. on 23-05-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class CalendarDialog extends BaseDialogFragment<Void> implements DatePicker.OnDateChangedListener {
    private DialogCalendarViewBinding calendarBinding;
    private static final String TAG = "CalendarDialog";
    private static final String DISPLAY_FORMAT = "dd LLL, yyyy";
    private OnDatePickListener listener;

    private Calendar calendar = Calendar.getInstance();

    private Long maxDate = System.currentTimeMillis();

    private Long minDate = 0L;
    private String dateFormat = "dd/MM/yyyy";

    public void setMaxDate(Long maxDate) {
        this.maxDate = maxDate;
    }

    public void setMinDate(Long minDate) {
        this.minDate = minDate;
    }

    public void setCurrentDate(Long currentDate) {
        calendar.setTimeInMillis(currentDate);
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    private void setDisplayDate() {
        String formattedDate = getDateFormatter(DISPLAY_FORMAT).format(calendar.getTime());
        calendarBinding.tvSelectedDate.setText(formattedDate);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        calendarBinding.calendar.btnYear.setText("" + year);
        changeMonth();
        setDisplayDate();
    }

    private void changeMonth() {
        calendarBinding.calendar.btnMonth.setText(getDateFormatter("LLL").format(calendar.getTime()));
        calendarBinding.calendar.chipGroupMonth.check(calendar.get(Calendar.MONTH));
    }

    public interface OnDatePickListener {
        void onDatePick(int day, int month, int year, String value);
    }

    public void setListener(OnDatePickListener listener) {
        this.listener = listener;
    }

    @Override
    View getContentView() {
        calendarBinding = DialogCalendarViewBinding.inflate(getLayoutInflater(), null, false);
        calendar.setTimeInMillis(System.currentTimeMillis());
        changeMonth();
        createMonthsGrid();
        hideHeader();
        setDisplayDate();
        calendarBinding.calendar.calendarView.setMaxDate(maxDate);
        if (minDate != 0)
            calendarBinding.calendar.calendarView.setMinDate(minDate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calendarBinding.calendar.calendarView.setOnDateChangedListener(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            calendarBinding.calendar.calendarView.setFirstDayOfWeek(2);
        }
        setupNavigationClickListener();
        return calendarBinding.getRoot();
    }

    private void setupNavigationClickListener() {
        calendarBinding.calendar.btnNextMonth.setOnClickListener(v -> moveToNextMonth());
        calendarBinding.calendar.btnPreviousMonth.setOnClickListener(v -> moveToPreviousMonth());
        calendarBinding.calendar.btnMonth.setOnClickListener(v -> showMonthPicker());
        calendarBinding.calendar.btnYear.setOnClickListener(v -> showYearPicker());
    }

    private void moveToNextMonth() {
        getViewByName("next").performClick();
        calendar.add(Calendar.MONTH, 1);
        changeMonth();
    }

    private void moveToPreviousMonth() {
        getViewByName("prev").performClick();
        calendar.add(Calendar.MONTH, -1);
        changeMonth();
    }

    private void showYearPicker() {
        View yearHeader = getViewByName("date_picker_header_year");
        if (yearHeader != null) {
            yearHeader.performClick();
            calendarBinding.calendar.chipGroupMonth.setVisibility(View.GONE);
        }
    }

    private void showMonthPicker() {
        boolean isVisible = calendarBinding.calendar.chipGroupMonth.isShown();
        calendarBinding.calendar.chipGroupMonth.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void hideHeader() {
        final View header = getViewByName("date_picker_header");
        if (header != null) {
            header.setVisibility(View.GONE);
        }
    }

    private View getViewByName(String resName) {
        return findTimePickerResourceView(calendarBinding.calendar.calendarView, resName);
    }

    private View findTimePickerResourceView(DatePicker picker, String name) {
        final int id = Resources.getSystem().getIdentifier(name, "id", "android");
        return picker.findViewById(id);
    }

    private void createMonthsGrid() {
        String[] months = getResources().getStringArray(R.array.months);
        calendarBinding.calendar.chipGroupMonth.removeAllViews();

        for (int i = 0; i < months.length; i++) {
            RowItenMonthBinding monthChip = RowItenMonthBinding.inflate(getLayoutInflater());
            monthChip.chipMonth.setText(months[i]);
            monthChip.getRoot().setId(i);
            Log.e(TAG, "current month " + calendar.get(Calendar.MONTH));
            calendarBinding.calendar.chipGroupMonth.addView(monthChip.getRoot(), i);
        }

        calendarBinding.calendar.chipGroupMonth.check(calendar.get(Calendar.MONTH));
        calendarBinding.calendar.chipGroupMonth.setOnCheckedChangeListener((group, checkedId) -> {
            Log.e(TAG, "Selected chip " + checkedId);
            calendar.set(Calendar.MONTH, checkedId);
            updateCalendar();
            calendarBinding.calendar.chipGroupMonth.setVisibility(View.GONE);
            calendarBinding.calendar.btnMonth.setText(months[checkedId]);
        });

    }

    private void updateCalendar() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendarBinding.calendar.calendarView.updateDate(year, month, dayOfMonth);
    }

    @Override
    boolean hasTitle() {
        return false;
    }

    @Override
    public void onSubmit() {
        if (listener != null) {
            String formattedDate = getDateFormatter(dateFormat).format(calendar.getTime());
            listener.onDatePick(
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.YEAR),
                    formattedDate
            );
        }
    }

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat getDateFormatter(String format) {
        return new SimpleDateFormat(format, Locale.getDefault());
    }

    public static class Builder extends BaseBuilder<Void, CalendarDialog> {

        private OnDatePickListener listener;

        public Builder(Context context) {
            super(context);
        }

        public Builder listener(OnDatePickListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public CalendarDialog build() {
            CalendarDialog fragment = new CalendarDialog();
            fragment.setArguments(bundle());
            fragment.setListener(listener);
            return fragment;
        }
    }
}

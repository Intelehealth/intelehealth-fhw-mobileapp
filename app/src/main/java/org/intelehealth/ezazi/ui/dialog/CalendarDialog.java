package org.intelehealth.ezazi.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.intelehealth.ezazi.BuildConfig;
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

    private final Calendar calendar = Calendar.getInstance();

    private Long maxDate = System.currentTimeMillis();

    private Long minDate = 0L;

    private Long currentDate = 0L;

    private String dateFormat = "dd/MM/yyyy";

    private DayOfWeek weekStartFromDay = DayOfWeek.MON;

    public void setWeekStartFromDay(DayOfWeek weekStartFromDay) {
        this.weekStartFromDay = weekStartFromDay;
    }

    public void setMaxDate(Long maxDate) {
        this.maxDate = maxDate;
    }

    public void setMinDate(Long minDate) {
        this.minDate = minDate;
    }

    public void setDefaultDate(Long currentDate) {
        this.currentDate = currentDate;
//        calendar.setTimeInMillis(currentDate);
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    private void setDisplayDate() {
        String formattedDate = getDateFormatter(DISPLAY_FORMAT).format(calendar.getTime());
        calendarBinding.tvSelectedDate.setText(formattedDate);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        changeMonth();
        setDisplayDate();
        calendarBinding.calendar.chipMonthYearGroup.clearCheck();
    }

    private void changeMonth() {
        calendarBinding.calendar.btnYear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        calendarBinding.calendar.btnMonth.setText(getDateFormatter("LLL").format(calendar.getTime()));
        calendarBinding.calendar.chipGroupMonth.check(calendar.get(Calendar.MONTH));
        calendarBinding.calendar.btnNextMonth.setVisibility(isNextButtonDisabled() ? View.GONE : View.VISIBLE);
    }

    private boolean isNextButtonDisabled() {
        return (isMaxYear() && getMaxCalendar().get(Calendar.MONTH) == calendar.get(Calendar.MONTH));
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
        setupCalendarSetting();
        setupNavigationClickListener();
        return calendarBinding.getRoot();
    }

    @Override
    public boolean isWrapContentDialog() {
        return true;
    }

    private void setupCalendarSetting() {
//        calendar.setTimeInMillis(maxDate);
        if (currentDate > 0) calendar.setTimeInMillis(currentDate);
        else calendar.setTimeInMillis(maxDate);

        changeMonth();
        createMonthsGrid();
        hideHeader();
        setDisplayDate();
        calendarBinding.calendar.calendarView.setMaxDate(maxDate);
        if (minDate != 0)
            calendarBinding.calendar.calendarView.setMinDate(minDate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calendarBinding.calendar.calendarView.setOnDateChangedListener(this);
            calendarBinding.calendar.calendarView.updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            calendarBinding.calendar.calendarView.init(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    this
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            calendarBinding.calendar.calendarView.setFirstDayOfWeek(weekStartFromDay.value);
        } else {
            calendarBinding.calendar.calendarView.getCalendarView().setFirstDayOfWeek(weekStartFromDay.value);
        }

        calendarBinding.calendar.calendarView.setMinDate(minDate);
    }

    private void setupNavigationClickListener() {
        calendarBinding.calendar.btnNextMonth.setOnClickListener(v -> moveToNextMonth());
        calendarBinding.calendar.btnPreviousMonth.setOnClickListener(v -> moveToPreviousMonth());
        calendarBinding.calendar.btnMonth.setOnClickListener(v -> showMonthPicker());
        calendarBinding.calendar.btnYear.setOnClickListener(v -> showYearPicker());
    }

    private void moveToNextMonth() {
        if (maxDate > calendar.getTimeInMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getViewByName("next").performClick();
            }
            calendar.add(Calendar.MONTH, 1);
            changeMonth();
        }
    }

    private void moveToPreviousMonth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getViewByName("prev").performClick();
        }
        calendar.add(Calendar.MONTH, -1);
        changeMonth();
    }

    private void showYearPicker() {
        View yearHeader;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            yearHeader = getViewByName("date_picker_header_year");
        } else {
            yearHeader = getViewByName("date_picker_year");
        }

        if (yearHeader != null) {
            yearHeader.performClick();
            calendarBinding.calendar.clMonthsContainer.setVisibility(View.GONE);
        }
    }

    private void showMonthPicker() {
        changeMonthEnableStatus();
        boolean isVisible = calendarBinding.calendar.clMonthsContainer.isShown();
        calendarBinding.calendar.clMonthsContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void hideHeader() {
        final View header = getViewByName("date_picker_header");
        if (header != null) {
            header.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            hideHeaderInLollipop();
            ConstraintLayout.LayoutParams params = getConstraintLayoutParams(calendarBinding.calendar.clNavigator);
            params.bottomToTop = calendarBinding.calendar.calendarView.getId();
            calendarBinding.calendar.clNavigator.setLayoutParams(params);
        }

        showChild(calendarBinding.calendar.calendarView);
    }

    private void hideHeaderInLollipop() {
        final View header = getViewByName("date_picker_month_day_year_layout");
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
            monthChip.chipMonth.setEnabled(!(isMaxYear() && i > getMaxCalendar().get(Calendar.MONTH)));
            calendarBinding.calendar.chipGroupMonth.addView(monthChip.getRoot(), i);
        }

        calendarBinding.calendar.chipGroupMonth.check(calendar.get(Calendar.MONTH));
        calendarBinding.calendar.chipGroupMonth.setOnCheckedChangeListener((group, checkedId) -> {
            Log.e(TAG, "Selected chip " + checkedId);
            if (checkedId > 0) {
                calendar.set(Calendar.MONTH, checkedId);
                updateCalendar();
                calendarBinding.calendar.chipMonthYearGroup.clearCheck();
                calendarBinding.calendar.clMonthsContainer.setVisibility(View.GONE);
                calendarBinding.calendar.btnMonth.setText(months[checkedId]);
            }
        });
    }

    private boolean isMaxYear() {
        return (getMaxCalendar().get(Calendar.YEAR) == calendar.get(Calendar.YEAR));
    }

    private Calendar getMaxCalendar() {
        Calendar max = Calendar.getInstance();
        max.setTimeInMillis(maxDate);
        return max;
    }

    private void changeMonthEnableStatus() {
        int monthCnt = calendarBinding.calendar.chipGroupMonth.getChildCount();
        for (int i = 0; i < monthCnt; i++) {
            boolean monthVisibility = isMaxYear() && i > getMaxCalendar().get(Calendar.MONTH);
            calendarBinding.calendar.chipGroupMonth.getChildAt(i).setEnabled(!monthVisibility);
        }
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

    public enum DayOfWeek {
        SUN(1),
        MON(2),
        TUE(3),
        WED(4),
        THU(5),
        FRI(6),
        SAT(7);
        private int value;

        DayOfWeek(int value) {
            this.value = value;
        }
    }

    private void showChild(DatePicker picker) {
        ViewGroup parent = ((ViewGroup) picker.getChildAt(0));
        for (int i = 0; i < parent.getChildCount(); i++) {
            Log.e("TimePicker", "getChild " + +(i + 1));
//            Log.e("TimePicker", "Id " + parent.getChildAt(i).getId());
//            Log.e("TimePicker", "tag " + parent.getChildAt(i).getTag());
//            parent.getChildAt(i).setVisibility(View.GONE);
            int resId = parent.getChildAt(i).getId();
            if (resId > -1)
                Log.e("TimePicker", "ResName=>" + Resources.getSystem().getResourceEntryName(resId));

            if (parent.getChildAt(i) instanceof ViewGroup) {
                findChild((ViewGroup) parent.getChildAt(i));
            }
        }
    }

    private void findChild(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Log.e("TimePicker", "ViewGroup getChild " + (i + 1));
//            Log.e("TimePicker", "ViewGroup Id " + group.getChildAt(i).getId());
//            Log.e("TimePicker", "ViewGroup tag " + group.getChildAt(i).getTag());
//            parent.getChildAt(i).setVisibility(View.GONE);
            int resId = group.getChildAt(i).getId();
            if (resId > -1)
                Log.e("TimePicker", "ViewGroup ResName=>" + Resources.getSystem().getResourceEntryName(resId));

            if (group.getChildAt(i) instanceof ViewGroup)
                findChild((ViewGroup) group.getChildAt(i));
        }
    }

    private ConstraintLayout.LayoutParams getConstraintLayoutParams(ConstraintLayout constraintLayout) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        return params;
    }
}

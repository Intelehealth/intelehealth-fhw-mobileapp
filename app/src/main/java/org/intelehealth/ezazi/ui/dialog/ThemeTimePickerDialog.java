package org.intelehealth.ezazi.ui.dialog;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.annotations.NotNull;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.DialogTimePickerViewBinding;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Vaghela Mithun R. on 23-05-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ThemeTimePickerDialog extends BaseDialogFragment<Void> implements TimePicker.OnTimeChangedListener {
    private DialogTimePickerViewBinding timePickerBinding;
    private static final String TAG = "ThemeTimePickerDialog";
    private String strAmPm;

    private OnTimePickListener listener;

    public interface OnTimePickListener {
        void onTimePick(int hours, int minutes, String amPm, String value);
    }

    public void setListener(OnTimePickListener listener) {
        this.listener = listener;
    }

    @Override
    View getContentView() {
        timePickerBinding = DialogTimePickerViewBinding.inflate(getLayoutInflater(), null, false);
        hideTimeHeaderLayout(timePickerBinding.timePickerClock);
        setupCurrentTime();
        setClickListener();
        return timePickerBinding.getRoot();
    }

    private void setClickListener() {
        timePickerBinding.hours.setOnClickListener(this);
        timePickerBinding.minutes.setOnClickListener(this);
        timePickerBinding.btnAm.setOnClickListener(this);
        timePickerBinding.btnPm.setOnClickListener(this);
    }

    private void setupCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        setAmPm(calendar);

        setTimePickerHours(hours);
        setTimePickerMinutes(minutes);

        setHourTextValue(hours);
        setMinuteTextValue(minutes);

        timePickerBinding.timePickerClock.setIs24HourView(false);
        timePickerBinding.timePickerClock.setOnTimeChangedListener(this);
    }

    private void setHourTextValue(int hours) {
        if (hours > 12) hours = hours - 12;
        timePickerBinding.hours.setText(String.format(Locale.getDefault(), "%02d", hours));
    }

    private void setMinuteTextValue(int minutes) {
        timePickerBinding.minutes.setText(String.format(Locale.getDefault(), "%02d", minutes));
    }

    private void setTimePickerHours(int hours) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerBinding.timePickerClock.setHour(hours);
        } else {
            timePickerBinding.timePickerClock.setCurrentHour(hours);
        }
    }

    private void setTimePickerMinutes(int minutes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerBinding.timePickerClock.setMinute(minutes);
        } else {
            timePickerBinding.timePickerClock.setCurrentMinute(minutes);
        }
    }

    private void setAmPm(Calendar calendar) {
        int amPm = calendar.get(Calendar.AM_PM);
        if (amPm == 0) setAmSelected();
        else setPmSelected();
    }

    private void setAmSelected() {
        strAmPm = "AM";
        timePickerBinding.btnAm.setSelected(true);
        timePickerBinding.btnPm.setSelected(false);
    }

    private void setPmSelected() {
        strAmPm = "PM";
        timePickerBinding.btnAm.setSelected(false);
        timePickerBinding.btnPm.setSelected(true);
    }

    private void hideTimeHeaderLayout(TimePicker picker) {
        final View header = findTimePickerResourceView(timePickerBinding.timePickerClock, "time_header");
        if (header != null) {
            header.setVisibility(View.GONE);
        }

        final View keyboardIcon = findTimePickerResourceView(timePickerBinding.timePickerClock, "toggle_mode");
        if (keyboardIcon != null) {
            keyboardIcon.setVisibility(View.GONE);
        }
    }

    private void triggerHourEvent() {
        final View hours = findTimePickerResourceView(timePickerBinding.timePickerClock, "hours");
        if (hours != null) {
            hours.performClick();
        }
    }

    private void triggerMinutesEvent() {
        final View minutes = findTimePickerResourceView(timePickerBinding.timePickerClock, "minutes");
        if (minutes != null) {
            minutes.performClick();
        }
    }

    private View findTimePickerResourceView(TimePicker picker, String name) {
        final int id = Resources.getSystem().getIdentifier(name, "id", "android");
        return picker.findViewById(id);
    }

//    private void hideClockLayout(TimePicker picker) {
//        final int id = Resources.getSystem().getIdentifier("radial_picker", "id", "android");
//        final View timeLayout = picker.findViewById(id);
//        if (timeLayout != null) {
//            timeLayout.setVisibility(View.GONE);
//        }

//        final int id = Resources.getSystem().getIdentifier("radial_picker", "id", "android");
//        final View timeLayout = picker.findViewById(id);
//        if (timeLayout != null) {
//            timeLayout.setVisibility(View.GONE);
//        }

//        final int id = Resources.getSystem().getIdentifier("radial_picker", "id", "android");
//        final View timeLayout = picker.findViewById(id);
//        if (timeLayout != null) {
//            timeLayout.setVisibility(View.GONE);
//        }

//        final int topLabelId = Resources.getSystem().getIdentifier("top_label", "id", "android");
//        final View topLabel = picker.findViewById(topLabelId);
//        if (topLabel != null) {
//            topLabel.setVisibility(View.GONE);
//        }
//
//        final int id2 = Resources.getSystem().getIdentifier("toggle_mode", "id", "android");
//        final View timeLayout2 = picker.findViewById(id2);
//        if (timeLayout2 != null) {
//            timeLayout2.setVisibility(View.GONE);
//        }
//
//        final int hoursId = Resources.getSystem().getIdentifier("hours", "id", "android");
//        final View hoursView = picker.findViewById(hoursId);
//        if (hoursView instanceof TextView) {
//            ((TextView) hoursView).setTextSize(getContext().getResources().getDimension(R.dimen.screen_title_size));
//            hoursView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//            ((TextView) hoursView).setMinWidth(0);
//            hoursView.setPadding(0,0,0,0);
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) hoursView.getLayoutParams();
//            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//            hoursView.setLayoutParams(params);
//        }
//
//        final int minId = Resources.getSystem().getIdentifier("minutes", "id", "android");
//        final View minsView = picker.findViewById(minId);
//        if (minsView instanceof TextView) {
//            ((TextView) minsView).setTextSize(getContext().getResources().getDimension(R.dimen.screen_title_size));
//            minsView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//            ((TextView) minsView).setMinWidth(0);
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) minsView.getLayoutParams();
//            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//            minsView.setPadding(0,0,0,0);
//            minsView.setLayoutParams(params);
//        }
//
//        final int separatorId = Resources.getSystem().getIdentifier("separator", "id", "android");
//        final View separator = picker.findViewById(separatorId);
//        if (minsView instanceof TextView) {
//            ((TextView) separator).setTextSize(getContext().getResources().getDimension(R.dimen.screen_title_size));
//            separator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
//        }
//
//        final int headerId = Resources.getSystem().getIdentifier("time_header", "id", "android");
//        final View header = picker.findViewById(headerId);
//        if (header != null) {
//            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) header.getLayoutParams();
//            params.gravity = Gravity.CENTER;
//            header.setPadding(0, 0, 0, 0);
////            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
//            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
//            header.setLayoutParams(params);
//        }
//
//        ViewGroup parent = ((ViewGroup) picker.getChildAt(0));
//        for (int i = 0; i < parent.getChildCount(); i++) {
//            Log.e("TimePicker", "getChild " + +(i + 1));
//            Log.e("TimePicker", "Id " + parent.getChildAt(i).getId());
//            Log.e("TimePicker", "tag " + parent.getChildAt(i).getTag());
////            parent.getChildAt(i).setVisibility(View.GONE);
//            int resId = parent.getChildAt(i).getId();
//            if (resId > -1)
//                Log.e("TimePicker", "ResName=>" + Resources.getSystem().getResourceEntryName(resId));
//
//            if (parent.getChildAt(i) instanceof ViewGroup) {
//                findChild((ViewGroup) parent.getChildAt(i));
//            }
//        }

//        timePickerBinding.timePickerInput
//    }

//    private void findChild(ViewGroup group) {
//        for (int i = 0; i < group.getChildCount(); i++) {
//            Log.e("TimePicker", "ViewGroup getChild " + (i + 1));
//            Log.e("TimePicker", "ViewGroup Id " + group.getChildAt(i).getId());
//            Log.e("TimePicker", "ViewGroup tag " + group.getChildAt(i).getTag());
////            parent.getChildAt(i).setVisibility(View.GONE);
//            int resId = group.getChildAt(i).getId();
//            if (resId > -1)
//                Log.e("TimePicker", "ViewGroup ResName=>" + Resources.getSystem().getResourceEntryName(resId));
//        }
//    }

    @Override
    boolean hasTitle() {
        return false;
    }

    @Override
    public void onSubmit() {
        if (listener != null) {
            listener.onTimePick(getHours(), getMinutes(), strAmPm, getHours() + ":" + getMinutes() + " " + strAmPm);
        }
    }

    private int getHours() {
        return Integer.parseInt(timePickerBinding.hours.getText().toString());
    }

    private int getMinutes() {
        return Integer.parseInt(timePickerBinding.minutes.getText().toString());
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hours, int minutes) {
        Log.d(TAG, hours + ":" + minutes);
        setHourTextValue(hours);
        setMinuteTextValue(minutes);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAm) {
            setAmSelected();
        } else if (view.getId() == R.id.btnPm) {
            setPmSelected();
        } else if (view.getId() == R.id.hours) {
            int hours = Integer.parseInt(((MaterialButton) view).getText().toString());
            setTimePickerHours(hours);
            triggerHourEvent();
        } else if (view.getId() == R.id.minutes) {
            int minutes = Integer.parseInt(((MaterialButton) view).getText().toString());
            setTimePickerMinutes(minutes);
            triggerMinutesEvent();
        } else {
            super.onClick(view);
        }
    }

    public static class Builder extends BaseBuilder<Void, ThemeTimePickerDialog> {

        private OnTimePickListener listener;

        public Builder(Context context) {
            super(context);
        }

        public Builder listener(OnTimePickListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public ThemeTimePickerDialog build() {
            ThemeTimePickerDialog fragment = new ThemeTimePickerDialog();
            fragment.setArguments(bundle());
            fragment.setListener(listener);
            return fragment;
        }
    }
}

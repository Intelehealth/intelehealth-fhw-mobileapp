package org.intelehealth.ezazi.ui.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
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

import com.google.firebase.database.annotations.NotNull;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.DialogTimePickerViewBinding;

/**
 * Created by Vaghela Mithun R. on 23-05-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ThemeTimePickerDialog extends BaseDialogFragment<Void> {
    DialogTimePickerViewBinding timePickerBinding;

    @Override
    View getContentView() {
        timePickerBinding = DialogTimePickerViewBinding.inflate(getLayoutInflater(), null, false);
        hideTimeHeaderLayout(timePickerBinding.timePickerClock);
//        timePickerBinding.timePickerInput.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
//        hideClockLayout(timePickerBinding.timePickerInput);
        return timePickerBinding.getRoot();
    }

    private void hideTimeHeaderLayout(TimePicker picker) {
        final int id = Resources.getSystem().getIdentifier("time_header", "id", "android");
        final View timeLayout = picker.findViewById(id);
        if (timeLayout != null) {
            timeLayout.setVisibility(View.GONE);
        }

        final int id2 = Resources.getSystem().getIdentifier("toggle_mode", "id", "android");
        final View timeLayout2 = picker.findViewById(id2);
        if (timeLayout2 != null) {
            timeLayout2.setVisibility(View.GONE);
        }

//        timePickerBinding.timePickerInput
    }

    private void hideClockLayout(TimePicker picker) {
        final int id = Resources.getSystem().getIdentifier("radial_picker", "id", "android");
        final View timeLayout = picker.findViewById(id);
        if (timeLayout != null) {
            timeLayout.setVisibility(View.GONE);
        }

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

        final int topLabelId = Resources.getSystem().getIdentifier("top_label", "id", "android");
        final View topLabel = picker.findViewById(topLabelId);
        if (topLabel != null) {
            topLabel.setVisibility(View.GONE);
        }

        final int id2 = Resources.getSystem().getIdentifier("toggle_mode", "id", "android");
        final View timeLayout2 = picker.findViewById(id2);
        if (timeLayout2 != null) {
            timeLayout2.setVisibility(View.GONE);
        }

        final int hoursId = Resources.getSystem().getIdentifier("hours", "id", "android");
        final View hoursView = picker.findViewById(hoursId);
        if (hoursView instanceof TextView) {
            ((TextView) hoursView).setTextSize(getContext().getResources().getDimension(R.dimen.screen_title_size));
            hoursView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            ((TextView) hoursView).setMinWidth(0);
            hoursView.setPadding(0,0,0,0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) hoursView.getLayoutParams();
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            hoursView.setLayoutParams(params);
        }

        final int minId = Resources.getSystem().getIdentifier("minutes", "id", "android");
        final View minsView = picker.findViewById(minId);
        if (minsView instanceof TextView) {
            ((TextView) minsView).setTextSize(getContext().getResources().getDimension(R.dimen.screen_title_size));
            minsView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            ((TextView) minsView).setMinWidth(0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) minsView.getLayoutParams();
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            minsView.setPadding(0,0,0,0);
            minsView.setLayoutParams(params);
        }

        final int separatorId = Resources.getSystem().getIdentifier("separator", "id", "android");
        final View separator = picker.findViewById(separatorId);
        if (minsView instanceof TextView) {
            ((TextView) separator).setTextSize(getContext().getResources().getDimension(R.dimen.screen_title_size));
            separator.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        }

        final int headerId = Resources.getSystem().getIdentifier("time_header", "id", "android");
        final View header = picker.findViewById(headerId);
        if (header != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) header.getLayoutParams();
            params.gravity = Gravity.CENTER;
            header.setPadding(0, 0, 0, 0);
//            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            header.setLayoutParams(params);
        }

        ViewGroup parent = ((ViewGroup) picker.getChildAt(0));
        for (int i = 0; i < parent.getChildCount(); i++) {
            Log.e("TimePicker", "getChild " + +(i + 1));
            Log.e("TimePicker", "Id " + parent.getChildAt(i).getId());
            Log.e("TimePicker", "tag " + parent.getChildAt(i).getTag());
//            parent.getChildAt(i).setVisibility(View.GONE);
            int resId = parent.getChildAt(i).getId();
            if (resId > -1)
                Log.e("TimePicker", "ResName=>" + Resources.getSystem().getResourceEntryName(resId));

            if (parent.getChildAt(i) instanceof ViewGroup) {
                findChild((ViewGroup) parent.getChildAt(i));
            }
        }

//        timePickerBinding.timePickerInput
    }

    private void findChild(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Log.e("TimePicker", "ViewGroup getChild " + (i + 1));
            Log.e("TimePicker", "ViewGroup Id " + group.getChildAt(i).getId());
            Log.e("TimePicker", "ViewGroup tag " + group.getChildAt(i).getTag());
//            parent.getChildAt(i).setVisibility(View.GONE);
            int resId = group.getChildAt(i).getId();
            if (resId > -1)
                Log.e("TimePicker", "ViewGroup ResName=>" + Resources.getSystem().getResourceEntryName(resId));
        }
    }

    @Override
    boolean hasTitle() {
        return false;
    }

    @Override
    public void onSubmit() {

    }

    public static class Builder extends BaseBuilder<Void, ThemeTimePickerDialog> {

        public Builder(Context context) {
            super(context);
        }

        @Override
        public ThemeTimePickerDialog build() {
            ThemeTimePickerDialog fragment = new ThemeTimePickerDialog();
            fragment.setArguments(bundle());
            return fragment;
        }
    }
}

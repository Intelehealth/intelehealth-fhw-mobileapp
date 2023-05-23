package org.intelehealth.ezazi.ui.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.google.firebase.database.annotations.NotNull;

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
        hideTimeHeaderLayout(timePickerBinding.timePicker);
        return timePickerBinding.getRoot();
    }

    private void hideTimeHeaderLayout(TimePicker picker) {
        final int id = Resources.getSystem().getIdentifier("time_header", "id", "android");
        final View timeLayout = picker.findViewById(id);
        if (timeLayout != null) {
            timeLayout.setVisibility(View.GONE);
        }
    }

    private void transferChildren(@NotNull final ViewGroup depart, @NotNull final ViewGroup arrival, final View child) {
        LayoutTransition transition = depart.getLayoutTransition();
        depart.setLayoutTransition(null);
        depart.removeView(child);
        arrival.addView(child);
        depart.setLayoutTransition(transition);
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

package org.intelehealth.ezazi.ui.binding;

import android.graphics.drawable.Drawable;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

/**
 * Created by Vaghela Mithun R. on 15-05-2023 - 23:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class StringBindingAdapter {
    @BindingAdapter("stringRes")
    public static void setStringRes(TextView textView, @StringRes int resId) {
        if (resId != 0) {
            textView.setText(textView.getContext().getResources().getText(resId));
        }
    }

    @BindingAdapter("selectedValue")
    public static void setStringRes(AutoCompleteTextView textView, String selected) {
        if (selected != null && textView != null) {
            textView.setText(selected, false);
            textView.setSelection(selected.length());
        }
    }

    @BindingAdapter("topDrawable")
    public static void bindTopDrawable(TextView textView, @DrawableRes int resId) {
        if (resId != 0) {
            Drawable drawable = ContextCompat.getDrawable(textView.getContext(), resId);
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
        }
    }
}

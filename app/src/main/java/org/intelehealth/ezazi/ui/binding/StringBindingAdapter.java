package org.intelehealth.ezazi.ui.binding;

import android.widget.TextView;

import androidx.annotation.StringRes;
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
}

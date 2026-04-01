package org.intelehealth.abdm.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import org.intelehealth.abdm.R;

public class SnackBarUtils {
    public void showSnackLinearLayoutParentSuccess(Context context, ViewGroup layoutParent, String message, boolean success) {
        Snackbar snackbar = Snackbar.make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();

        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);

        if (success) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccess));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFailure));
        }

        snackbar.show();
    }

    public void showSnackConstraintLayoutParentSuccess(Context context, View layoutParent, String message, boolean success) {
        Snackbar snackbar = Snackbar.make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();

        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);

        if (success) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccess));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFailure));
        }

        snackbar.show();
    }
}

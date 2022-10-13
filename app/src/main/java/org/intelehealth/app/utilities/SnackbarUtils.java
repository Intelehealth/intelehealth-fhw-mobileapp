package org.intelehealth.app.utilities;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import org.intelehealth.app.R;

public class SnackbarUtils {

    public void showSnackLinearLayoutParentSuccess(Context context, LinearLayout layoutParent, String message) {
        Snackbar snackbar = Snackbar
                .make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccess));
        snackbar.show();
    }

    public void showSnacksWithRelativeLayoutSuccess(Context context, String message,
                                                    RelativeLayout layoutParent) {
        Snackbar snackbar = Snackbar
                .make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccess));
        snackbar.show();
    }
}

package org.intelehealth.app.utilities;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import org.intelehealth.app.R;

public class SnackbarUtils {

    public void showSnackLinearLayoutParentSuccess(Context context, LinearLayout layoutParent, String message, boolean success) {
        Snackbar snackbar = Snackbar
                .make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);
        if(success)
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccess));
        else
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFailure));

        snackbar.show();
    }
    public void showSnackConstraintLayoutParentSuccess(Context context, View layoutParent, String message, boolean success) {
        Snackbar snackbar = Snackbar
                .make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);
        if(success)
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccess));
        else
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFailure));

        snackbar.show();
    }

    public Snackbar showSnackRelativeLayoutParentSuccess(Context context, RelativeLayout layoutParent, String message, boolean success) {
        Snackbar snackbar = Snackbar
                .make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);
        if(success)
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccess));
        else
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFailure));

        snackbar.show();
        return snackbar;
    }

    public void setImageActionForSnackBar(Context context, Snackbar snackbar, Uri uri) {
        snackbar.setActionTextColor(context.getColor(R.color.colorPrimary));
        snackbar.setAction("View", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "image/*");
                context.startActivity(intent);
            }
        });
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

    public void showSnackCoordinatorLayoutParentSuccess(Context context, CoordinatorLayout layoutParent, String message) {
        Snackbar snackbar = Snackbar
                .make(layoutParent, message, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) view.getLayoutParams();
        params1.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params1);
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.error_red));
        snackbar.show();
    }
    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}

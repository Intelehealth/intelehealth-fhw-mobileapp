package org.intelehealth.app.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

public class WindowsUtils {
    public static void hideSoftKeyboard(AppCompatActivity appCompatActivity) {
        try {
            InputMethodManager imm = (InputMethodManager) appCompatActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(appCompatActivity.getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setStatusBarColor(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}

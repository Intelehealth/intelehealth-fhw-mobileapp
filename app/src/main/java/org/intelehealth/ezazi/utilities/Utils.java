package org.intelehealth.ezazi.utilities;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Vaghela Mithun R. on 17-08-2023 - 12:49.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class Utils {
    public static void showKeyboard(Context context, View editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(AppCompatActivity activity) {
        hideKeyboard(activity, activity.getCurrentFocus());
    }

    public static void hideKeyboard(AppCompatActivity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

package org.intelehealth.ezazi.ui.validation;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

/**
 * Created by Kaveri Zaware on 28-07-2023
 * email - kaveri@intelehealth.org
 **/
public class FirstLetterUpperCaseInputFilter implements InputFilter {
    private static final String TAG = "FirstLaterFilter";

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Log.e(TAG, "filter: " + source);
        Log.e(TAG, "Spanned: " + dest);
        Log.e(TAG, "start: " + start);
        Log.e(TAG, "end: " + end);
        Log.e(TAG, "dstart: " + dstart);
        Log.e(TAG, "dend: " + dend);
        Log.e(TAG, "source.length: " + source.length());
        if (dstart == 0 && end == 1) {
            // Convert the first character to uppercase
            char c = source.charAt(0);
            if (Character.isLowerCase(c)) {
                String str = String.valueOf(Character.toUpperCase(c)) + source.subSequence(1, end);
                Log.e(TAG, "toUpperCase: " + str);
                return String.valueOf(Character.toUpperCase(c));
            } else return null;
        }
        return null; // Accept all other input
    }
}
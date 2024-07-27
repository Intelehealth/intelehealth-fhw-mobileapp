package org.intelehealth.app.shared;

import android.text.InputFilter;
import android.text.Spanned;
import org.intelehealth.app.utilities.CustomLog;

/**
 * Created by Kaveri Zaware on 28-07-2023
 * email - kaveri@intelehealth.org
 **/
public class FirstLetterUpperCaseInputFilter implements InputFilter {
    private static final String TAG = "FirstLaterFilter";

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CustomLog.e(TAG, "filter: " + source);
        CustomLog.e(TAG, "Spanned: " + dest);
        CustomLog.e(TAG, "start: " + start);
        CustomLog.e(TAG, "end: " + end);
        CustomLog.e(TAG, "dstart: " + dstart);
        CustomLog.e(TAG, "dend: " + dend);
        CustomLog.e(TAG, "source.length: " + source.length());
        if (dstart == 0 && end == 1) {
            // Convert the first character to uppercase
            char c = source.charAt(0);
            if (Character.isLowerCase(c)) {
                String str = String.valueOf(Character.toUpperCase(c)) + source.subSequence(1, end);
                CustomLog.e(TAG, "toUpperCase: " + str);
                return String.valueOf(Character.toUpperCase(c));
            } else return null;
        }
        return null; // Accept all other input
    }
}
package org.intelehealth.ezazi.ui.validation;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by Kaveri Zaware on 28-07-2023
 * email - kaveri@intelehealth.org
 **/
public class FirstLetterUpperCaseInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (dstart == 0 && end > 0) {
            // Convert the first character to uppercase
            char firstChar = Character.toUpperCase(source.charAt(0));
            // CharSequence result = firstChar + source.subSequence(1, end);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(firstChar);
            stringBuilder.append(source.subSequence(1, end));
            String result = stringBuilder.toString();
            return result;
        }
        return null; // Accept all other input
    }
}
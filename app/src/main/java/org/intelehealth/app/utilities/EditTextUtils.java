package org.intelehealth.app.utilities;

import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditTextUtils {

    private static String SPECIAL_CHARACTERS = "[0-9.,:;?/{}()% ]";

    /**
     * @param length
     * @param editText
     */
    public static void setEditTextMaxLength(int length, EditText editText) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        editText.setFilters(filterArray);
    }

    public static boolean isValidText(String text) {
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

    //return editext values
    public static void returnEditextValues(IReturnValues iReturnValues, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (Integer.valueOf(s.toString()) > 0)
                        iReturnValues.onReturnValue(s.toString());
                    else
                        iReturnValues.onReturnValue("0");
                } else
                    iReturnValues.onReturnValue("0");


            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static InputFilter inputFilterOthers = new InputFilter() { //filter input for all other fields
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = charSequence.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return charSequence;
            else {
                if (charSequence instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) charSequence, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            //   return Character.isLetterOrDigit(c) || Character.isSpaceChar(c);
            return Character.isLetter(c) || Character.isSpaceChar(c) || (c >= 0x0900 && c <= 0x097F); // Unicode range for Devanagari (Marathi, Hindi, etc.);   // This allows only alphabets.
        }
    };

    public static String decimalPlacesCount(String str, int maxBeforePoint, int maxDecimal) {
        if (str.charAt(0) == '.') str = "0" + str;
        int max = str.length();

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0;
        char t;

        while (i < max) {
            t = str.charAt(i);
            if (t != '.' && !after) {
                up++;
                if (up > maxBeforePoint) return rFinal;
            } else if (t == '.') {
                after = true;
            } else {
                decimal++;
                if (decimal > maxDecimal)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }

        return rFinal;
    }
}
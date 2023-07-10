package org.intelehealth.app.utilities;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditTextUtils {

    private static String SPECIAL_CHARACTERS = "[0-9.,:;?/{}()% ]";
    static int count = -1;
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
    public static void returnEditextValues(IReturnValues iReturnValues, EditText editText)
    {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0)
                {
                    if(Integer.valueOf(s.toString()) > 0 )
                        iReturnValues.onReturnValue(s.toString());
                    else
                        iReturnValues.onReturnValue("0");
                }
                else
                    iReturnValues.onReturnValue("0");


            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /**
     *
     * @param view EditText.
     * @param s Each letter that is entered.
     * @param decimalPlaces The number of decimal places allowed after decimal point.
     */
    public static void decimalPlacesCount(EditText view, Editable s, int decimalPlaces) {
        if (s.length() > 0) {
            String str = view.getText().toString();
            view.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    count--;
                    InputFilter[] fArray = new InputFilter[1];
                    fArray[0] = new InputFilter.LengthFilter(100);
                    view.setFilters(fArray);
                }
                return false;
            });
            char t = str.charAt(s.length() - 1);

            if (t == '.') count = 0;

            if (count >= 0) {
                if (count == decimalPlaces) {
                    InputFilter[] fArray = new InputFilter[1];
                    fArray[0] = new InputFilter.LengthFilter(s.length());
                    view.setFilters(fArray);
                    count = -1;  // resetting count for the next new edittext field to work.
                }
                count++;
            }
        }
    }



}

package io.intelehealth.client.utilities;

import android.text.InputFilter;
import android.widget.EditText;

public class EditTextUtils {


    /**
     * @param length
     * @param editText
     */
    public static void  setEditTextMaxLength(int length, EditText editText) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        editText.setFilters(filterArray);
    }
}

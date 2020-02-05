package app.intelehealth.client.utilities;

import android.text.InputFilter;
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

}

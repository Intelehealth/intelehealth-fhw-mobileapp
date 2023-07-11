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
     * @param str
     * @param MAX_BEFORE_POINT
     * @param MAX_DECIMAL
     * @return
     */
    public static String decimalPlacesCount(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL){
        if(str.charAt(0) == '.') str = "0"+str;
        int max = str.length();

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0; char t;

        while(i < max){
            t = str.charAt(i);
            if(t != '.' && after == false){
                up++;
                if(up > MAX_BEFORE_POINT) return rFinal;
            }else if(t == '.'){
                after = true;
            }else{
                decimal++;
                if(decimal > MAX_DECIMAL)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }

        return rFinal;
    }



}

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
    private static String BlockCharacterSet_Others = "0123456789\\@$!=><&^*+€¥£`~";
    private static String BlockCharacterSet_Name = "\\/_[]#@%&{}()-:;,?$!=><&^*+\"\'€¥£`~";

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
     * This input filter will not allow emoji's to be added by the HW into the editText field.
     * This filter is useful when we don't have to apply any other filter restriction other than emojis in our code.
     * Note: call emojiFilter only when no other filter is required else use the below inputFilter added.
     */
    public static InputFilter emojiFilter = (source, start, end, spanned, dstart, dend) -> {
        for (int index = start; index < end - 1; index++) {
            int type = Character.getType(source.charAt(index));
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                return "";
            }
        }
        return null;
    };
    
    /**
     * This inputfilter will allow only alphabets and full stop (.) as per requirement -> SYR-616.
     * This will handle SYR-612 as well of emojis.
     */
    public static InputFilter inputFilter = (source, start, end, spanned, dstart, dend) -> {
        boolean keepOriginal = true;
        StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (isCharAllowed(c) || String.valueOf(c).equalsIgnoreCase(".")) // to allow alphabet and full stop.
                sb.append(c);
            else
                keepOriginal = false;
        }
        if (keepOriginal)
            return null;
        else {
            if (source instanceof Spanned) {
                SpannableString sp = new SpannableString(sb);
                TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                return sp;
            } else {
                return sb;
            }
        }
    };

    private static boolean isCharAllowed(char c) {
        Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
        Matcher ms = ps.matcher(String.valueOf(c));
        return ms.matches();
    }



}

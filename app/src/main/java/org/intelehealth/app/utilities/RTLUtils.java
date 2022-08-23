package org.intelehealth.app.utilities;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.core.view.ViewCompat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created.
 */

public class RTLUtils {
    private static final Set<String> RTL;

    static {
        Set<String> lang = new HashSet<String>();
        lang.add("ar"); // Arabic
        lang.add("dv"); // Divehi
        lang.add("fa"); // Persian (Farsi)
        lang.add("ha"); // Hausa
        lang.add("he"); // Hebrew
        lang.add("iw"); // Hebrew (old code)
        lang.add("ji"); // Yiddish (old code)
        lang.add("ps"); // Pashto, Pushto
        lang.add("ur"); // Urdu
        lang.add("yi"); // Yiddish
        RTL = Collections.unmodifiableSet(lang);
    }


    public static boolean isRTL(String languageCode) {
        if (languageCode == null)
            return false;

        return RTL.contains(languageCode);
    }

    public static boolean isRTL(Locale locale) {
        if (locale == null)
            return false;

        // Character.getDirectionality(locale.getDisplayName().charAt(0))
        // can lead to NPE (Java 7 bug)
        // https://bugs.openjdk.java.net/browse/JDK-6992272?page=com.atlassian.streams.streams-jira-plugin:activity-stream-issue-tab
        // using hard coded list of locale instead
        return RTL.contains(locale.getLanguage());
    }

    @SuppressLint("WrongConstant")
    public static boolean isRTL(View view) {
        if (view == null)
            return false;

        // config.getLayoutDirection() only available since 4.2
        // -> using ViewCompat instead (from Android support library)
        return ViewCompat.getLayoutDirection(view) == View.LAYOUT_DIRECTION_RTL;
    }

    public static boolean isArabicText(String text) {

        String textWithoutSpace = text.trim().replaceAll(" ",""); //to ignore whitepace
        for (int i = 0; i < textWithoutSpace.length();) {
            int c = textWithoutSpace.codePointAt(i);
            //range of arabic chars/symbols is from 0x0600 to 0x06ff
            //the arabic letter 'لا' is special case having the range from 0xFE70 to 0xFEFF
            if (c >= 0x0600 && c <=0x06FF || (c >= 0xFE70 && c<=0xFEFF))
                i += Character.charCount(c);
            else{
                Logger.logV("isArabicText", text + " : "+false );
                return false;
            }

        }
        Logger.logV("isArabicText", text + " : "+true );
        return true;

    }

    public static boolean onlyDigits(String str) {
        if(str.contains("/")) return true;
        // Regex to check string
        // contains only digits
        String regex = "[0-9]+";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the string is empty
        // return false
        if (str == null) {
            return false;
        }

        // Find match between given string
        // and regular expression
        // using Pattern.matcher()
        Matcher m = p.matcher(str);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }
}

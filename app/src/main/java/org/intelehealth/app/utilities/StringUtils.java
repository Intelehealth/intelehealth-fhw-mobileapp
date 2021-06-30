/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.intelehealth.app.utilities;

import android.content.Context;
import android.widget.Spinner;

import java.io.File;
import java.util.List;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;

public final class StringUtils {
    private static final String NULL_AS_STRING = "null";
    private static final String SPACE_CHAR = " ";

    public static boolean notNull(String string) {
        return null != string && !NULL_AS_STRING.equals(string.trim());
    }

    public static boolean isBlank(String string) {
        return null == string || SPACE_CHAR.equals(string);
    }

    public static boolean notEmpty(String string) {
        return string != null && !string.isEmpty();
    }

    public static String unescapeJavaString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                    default:
                        // Do nothing
                        break;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public static String getValue(String value) {
        String val = "";
        if (value != null)
            val = value;
        return val;

    }

    public static String getValue1(String value) {
        String val = " ";
        if (value != null)
            val = value;
        return val;

    }

    public static String getProvided(Spinner spinner) {
        String val = "";
        if (spinner.getSelectedItemPosition() == 0)
            val = "Not provided";


        else if(spinner.getSelectedItem() == null)
        {
            val = "Not provided";

        }
        else
        {
            val = spinner.getSelectedItem().toString();
        }

        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if(sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            val = switch_hi_caste(val);
            val = switch_hi_economic(val);
            val = switch_hi_education(val);
        }
        else if(sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_caste(val);
            val = switch_or_economic(val);
            val = switch_or_education(val);
        }
        else if(sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            val = switch_ta_caste(val);
            val = switch_ta_economic(val);
            val = switch_ta_education(val);
        }

        return val;
    }

    public static String switch_hi_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "अशिक्षित";
                break;
            case "Primary":
                val = "प्रथम";
                break;
            case "Secondary":
                val = "माध्यमिक";
                break;
            case "Higher Secondary":
                val = "उच्च माध्यमिक";
                break;
            case "Graduation & Higher":
                val = "स्नातक और उच्चतर";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_education(String val) {
        switch (val) {
            case "अशिक्षित":
                val = "Illiterate";
                break;
            case "प्रथम":
                val = "Primary";
                break;
            case "माध्यमिक":
                val = "Secondary";
                break;
            case "उच्च माध्यमिक":
                val = "Higher Secondary";
                break;
            case "स्नातक और उच्चतर":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "ଅଶିକ୍ଷିତ";
                break;
            case "Primary":
                val = "ମୌଳିକ ଶିକ୍ଷା";
                break;
            case "Secondary":
                val = "ମାଧ୍ୟମିକ ଶିକ୍ଷା";
                break;
            case "Higher Secondary":
                val = "ଉଚ୍ଚ ମାଧ୍ୟମିକ ଶିକ୍ଷା";
                break;
            case "Graduation & Higher":
                val = "ସ୍ନାତକୋତର ଶିକ୍ଷା";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_or_education(String val) {
        switch (val) {
            case "ଅଶିକ୍ଷିତ":
                val = "Illiterate";
                break;
            case "ମୌଳିକ ଶିକ୍ଷା":
                val = "Primary";
                break;
            case "ମାଧ୍ୟମିକ ଶିକ୍ଷା":
                val = "Secondary";
                break;
            case "ଉଚ୍ଚ ମାଧ୍ୟମିକ ଶିକ୍ଷା":
                val = "Higher Secondary";
                break;
            case "ସ୍ନାତକୋତର ଶିକ୍ଷା":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "எழுதப்படிக்கத்தெரியாதவர்";
                break;
            case "Primary":
                val = "முதல்நிலை";
                break;
            case "Secondary":
                val = "இடைநிலை";
                break;
            case "Higher Secondary":
                val = "மேல்நிலை";
                break;
            case "Graduation & Higher":
                val = "பட்டம் மற்றும் உயர்";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_ta_education(String val) {
        switch (val) {
            case "எழுதப்படிக்கத்தெரியாதவர்":
                val = "Illiterate";
                break;
            case "முதல்நிலை":
                val = "Primary";
                break;
            case "இடைநிலை":
                val = "Secondary";
                break;
            case "மேல்நிலை":
                val = "Higher Secondary";
                break;
            case "பட்டம் மற்றும் உயர்":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_economic(String val) {
        switch (val) {
            case "गरीबी रेखा से ऊपर":
                val = "APL";
                break;
            case "गरीबी रेखा से नीचे":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "गरीबी रेखा से ऊपर";
                break;
            case "BPL":
                val = "गरीबी रेखा से नीचे";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_economic(String val) {
        switch (val) {
            case "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ଉପରେ":
                val = "APL";
                break;
            case "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ତଳେ":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ଉପରେ";
                break;
            case "BPL":
                val = "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ତଳେ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_economic(String val) {
        switch (val) {
            case "வறுமைக் கோட்டிற்கு மேல்":
                val = "APL";
                break;
            case "வறுமைக் கோட்டிற்குக் கீழ்":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "வறுமைக் கோட்டிற்கு மேல்";
                break;
            case "BPL":
                val = "வறுமைக் கோட்டிற்குக் கீழ்";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_caste(String val) {
        switch (val) {
            case "सामान्य":
                val = "General";
                break;
            case "अन्य पिछड़ा वर्ग":
                val = "OBC";
                break;
            case "अनुसूचित जाति":
                val = "SC";
                break;
            case "अनुसूचित जनजाति":
                val = "ST";
                break;
            case "अन्य":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_caste(String val) {
        switch (val) {
            case "ସାଧାରଣ":
                val = "General";
                break;
            case "ଅନ୍ୟାନ୍ୟ ପଛୁଆ ବର୍ଗ":
                val = "OBC";
                break;
            case "ଅନୁସୂଚିତ ଜାତି":
                val = "SC";
                break;
            case "ଅନୁସୂଚିତ ଜନଜାତି":
                val = "ST";
                break;
            case "ଅନ୍ୟାନ୍ୟ":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_caste(String val) {
        switch (val) {
            case "பொது":
                val = "General";
                break;
            case "ஓபிசி":
                val = "OBC";
                break;
            case "எஸ்சி":
                val = "SC";
                break;
            case "எஸ்டி":
                val = "ST";
                break;
            case "மற்றவை":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "सामान्य";
                break;
            case "OBC":
                val = "अन्य पिछड़ा वर्ग";
                break;
            case "SC":
                val = "अनुसूचित जाति";
                break;
            case "ST":
                val = "अनुसूचित जनजाति";
                break;
            case "others":
                val = "अन्य";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "ସାଧାରଣ";
                break;
            case "OBC":
                val = "ଅନ୍ୟାନ୍ୟ ପଛୁଆ ବର୍ଗ";
                break;
            case "SC":
                val = "ଅନୁସୂଚିତ ଜାତି";
                break;
            case "ST":
                val = "ଅନୁସୂଚିତ ଜନଜାତି";
                break;
            case "others":
                val = "ଅନ୍ୟାନ୍ୟ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "பொது";
                break;
            case "OBC":
                val = "ஓபிசி";
                break;
            case "SC":
                val = "எஸ்சி";
                break;
            case "ST":
                val = "எஸ்டி";
                break;
            case "others":
                val = "மற்றவை";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String getFileNameWithoutExtension(File file) {
        String fileName = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }

        return fileName;

    }

    public static String getFileNameWithoutExtensionString(String filename) {
        String fileName = "";

        try {
            if (filename.indexOf(".") > 0)
                fileName = filename.substring(0, filename.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }

        return fileName;

    }

    public static String convertUsingStringBuilder(List<String> names) {
        StringBuilder namesStr = new StringBuilder();
        for (String name : names) {
            namesStr = namesStr.length() > 0 ? namesStr.append("','").append(name) : namesStr.append(name);
        }
        return namesStr.toString();
    }

    public static String mobileNumberEmpty(String value) {
        String val = "N/A";
        if (value != null && !value.isEmpty())
            val = value;
        return val;
    }

    public static String en__hi_dob(String dob) { //English dob is replaced to Hindi text.
        String mdob_text = dob
                .replace("January", "जनवरी")
                .replace("February", "फ़रवरी")
                .replace("March", "मार्च")
                .replace("April", "अप्रैल")
                .replace("May", "मई")
                .replace("June", "जून")
                .replace("July", "जुलाई")
                .replace("August", "अगस्त")
                .replace("September", "सितंबर")
                .replace("October", "अक्टूबर")
                .replace("November", "नवंबर")
                .replace("December", "दिसंबर");

        return mdob_text;
    }

    public static String en__or_dob(String dob) { //English dob is replaced to Odiya text.
        String mdob_text = dob
                .replace("January", "ଜାନୁଆରୀ")
                .replace("February", "ଫେବୃଆରୀ")
                .replace("March", "ମାର୍ଚ୍ଚ")
                .replace("April", "ଏପ୍ରିଲ୍")
                .replace("May", "ମେ")
                .replace("June", "ଜୁନ୍")
                .replace("July", "ଜୁଲାଇ")
                .replace("August", "ଅଗଷ୍ଟ")
                .replace("September", "ସେପ୍ଟେମ୍ବର")
                .replace("October", "ଅକ୍ଟୋବର")
                .replace("November", "ନଭେମ୍ବର")
                .replace("December", "ଡିସେମ୍ବର");

        return mdob_text;
    }

    public static String en__ta_dob(String dob) { //English dob is replaced to Tamil text.
        String mdob_text = dob
                .replace("January", "ஜனவரி")
                .replace("February", "பிப்ரவரி")
                .replace("March", "மார்ச்")
                .replace("April", "ஏப்ரல்")
                .replace("May", "மே")
                .replace("June", "ஜூன்")
                .replace("July", "ஜூலை")
                .replace("August", "ஆகஸ்ட்")
                .replace("September", "செப்டம்பர்")
                .replace("October", "அக்டோபர்")
                .replace("November", "நவம்பர்")
                .replace("December", "டிசம்பர்");

        return mdob_text;
    }

    public static String hi_or__en_month(int month_index) {
        String dob_string = "";

        switch (month_index) {
            case 0:
                dob_string = "January";
                break;
            case 1:
                dob_string = "February";
                break;
            case 2:
                dob_string = "March";
                break;
            case 3:
                dob_string = "April";
                break;
            case 4:
                dob_string = "May";
                break;
            case 5:
                dob_string = "June";
                break;
            case 6:
                dob_string = "July";
                break;
            case 7:
                dob_string = "August";
                break;
            case 8:
                dob_string = "September";
                break;
            case 9:
                dob_string = "October";
                break;
            case 10:
                dob_string = "November";
                break;
            case 11:
                dob_string = "December";
                break;

            default:
                return dob_string;
        }

        return dob_string;
    }


    public static String hi_or__en_noEdit(String dobString, String locale) {

        if(locale.equalsIgnoreCase("hi")) {
            String dob = dobString
                    //Hindi
                    .replace("जनवरी", "January")
                    .replace("फ़रवरी", "February")
                    .replace("मार्च", "March")
                    .replace("अप्रैल", "April")
                    .replace("मई", "May")
                    .replace("जून", "June")
                    .replace("जुलाई", "July")
                    .replace("अगस्त", "August")
                    .replace("सितंबर", "September")
                    .replace("अक्टूबर", "October")
                    .replace("नवंबर", "November")
                    .replace("दिसंबर", "December");
            return dob;
        }
        else if(locale.equalsIgnoreCase("or")) {
            //Odiya
            String dob = dobString
                    .replace("ଜାନୁଆରୀ", "January")
                    .replace("ଫେବୃଆରୀ", "February")
                    .replace("ମାର୍ଚ୍ଚ", "March")
                    .replace("ଏପ୍ରିଲ୍", "April")
                    .replace("ମେ", "May")
                    .replace("ଜୁନ୍", "June")
                    .replace("ଜୁଲାଇ", "July")
                    .replace("ଅଗଷ୍ଟ", "August")
                    .replace("ସେପ୍ଟେମ୍ବର", "September")
                    .replace("ଅକ୍ଟୋବର", "October")
                    .replace("ନଭେମ୍ବର", "November")
                    .replace("ଡିସେମ୍ବର", "December");
            return dob;
        }
        else if(locale.equalsIgnoreCase("ta")) {
            //Bengali
            String dob = dobString
                    .replace("ஜனவரி", "January")
                    .replace("பிப்ரவரி", "February")
                    .replace("மார்ச்", "March")
                    .replace("ஏப்ரல்", "April")
                    .replace("மே", "May")
                    .replace("ஜூன்", "June")
                    .replace("ஜூலை", "July")
                    .replace("ஆகஸ்ட்", "August")
                    .replace("செப்டம்பர்", "September")
                    .replace("அக்டோபர்", "October")
                    .replace("நவம்பர்", "November")
                    .replace("டிசம்பர்", "December");
            return dob;
        }
        else {
            return dobString;
        }

    }

    public static String getLocaleGender(Context context, String gender) {
        switch (gender) {
            case "M":
                return context.getString(R.string.gender_male);
            case "F":
                return context.getString(R.string.gender_female);
            default:
                return gender;
        }
    }
}

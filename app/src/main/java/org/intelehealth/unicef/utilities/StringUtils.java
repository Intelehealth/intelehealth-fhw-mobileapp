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

package org.intelehealth.unicef.utilities;

import android.content.Context;
import android.widget.Spinner;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.IntelehealthApplication;

import java.io.File;
import java.util.List;

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


        else if (spinner.getSelectedItem() == null) {
            val = "Not provided";

        } else {
            val = spinner.getSelectedItem().toString();
        }

        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            val = switch_hi_caste(val);
            val = switch_hi_economic(val);
            val = switch_hi_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_caste(val);
            val = switch_or_economic(val);
            val = switch_or_education(val);
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

    public static String switch_ru_economic(String val) {
        switch (val) {
            case "выше черты бедности":
                val = "APL";
                break;
            case "За чертой бедностии":
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

    public static String switch_ru_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "выше черты бедности";
                break;
            case "BPL":
                val = "За чертой бедностии";
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

    public static String ru__or_dob(String dob) { //English dob is replaced to russian text.
        String mdob_text = dob
                .replace("January", "Январь")
                .replace("February", "Февраль")
                .replace("March", "Март")
                .replace("April", "апреля")
                .replace("May", "Май")
                .replace("June", "июнь")
                .replace("July", "июль")
                .replace("August", "август")
                .replace("September", "сентябрь")
                .replace("October", "Октябрь")
                .replace("November", "Ноябрь")
                .replace("December", "Декабрь");

        return mdob_text;
    }

    public static String node_ru__or_dob(String dob) { //English dob is replaced to russian text.
        return dob
                .replace("Jan", "Январь")
                .replace("Feb", "Февраль")
                .replace("Mar", "Март")
                .replace("Apr", "апреля")
                .replace("May", "Май")
                .replace("Jun", "июнь")
                .replace("Jul", "июль")
                .replace("Aug", "август")
                .replace("Sep", "сентябрь")
                .replace("Oct", "Октябрь")
                .replace("Nov", "Ноябрь")
                .replace("Dec", "Декабрь");
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

        if (locale.equalsIgnoreCase("ru")) {
            String dob = dobString
                    //Russian
                    .replace("Январь", "January")
                    .replace("Февраль", "February")
                    .replace("Март", "March")
                    .replace("апреля", "April")
                    .replace("Май", "May")
                    .replace("июнь", "June")
                    .replace("июль", "July")
                    .replace("август", "August")
                    .replace("сентябрь", "September")
                    .replace("Октябрь", "October")
                    .replace("Ноябрь", "November")
                    .replace("Декабрь", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("hi")) {
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
        } else if (locale.equalsIgnoreCase("or")) {
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
        } else {
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

    /**
     * Ritika======
     * get only selective Language from Db and Show accorfingly
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value according the Locals to show on CountrySpinner.
     */
    public static String mSwitch_Country_edit(String val, String locale) {
        if (locale.equalsIgnoreCase("ru")) {

            switch (val) {
                case "India":
                    val = "Индия";
                    break;
                case "Kyrgyzstan":
                    val = "Кыргызстан";
                    break;
                default:
                    return val;
            }

        } else {
            return val;
        }
        return val;
    }

    /**
     * Ritika======
     * put only english Language into Db
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value only english .
     */
    public static String mSwitch_Country(String val, String locale) {

        if (locale.equalsIgnoreCase("ru")) {

            switch (val) {
                case "Индия":
                    val = "India";
                    break;
                case "Кыргызстан":
                    val = "Kyrgyzstan";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }


    /**
     * Ritika======
     * get only selective Language from Db and Show accordingly
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value according the Locals to show on CountrySpinner.
     */

    public static String mSwitch_State_edit(String val, String locale) {
        if (locale.equalsIgnoreCase("ru")) {
            switch (val) {
                case "Andhra Pradesh":
                    val = "Андхра-Прадеш";
                    break;
                case "Arunachal Pradesh":
                    val = "Аруначал-Прадеш";
                    break;
                case "Assam":
                    val = "Ассам";
                    break;
                case "Bihar":
                    val = "Бихар";
                    break;
                case "Chhattisgarh":
                    val = "Чхаттисгарх";
                    break;
                case "Goa":
                    val = "Гоа";
                    break;
                case "Gujarat":
                    val = "Гуджарат";
                    break;
                case "Haryana":
                    val = "Харьяна";
                    break;
                case "Himachal Pradesh":
                    val = "Химачал-Прадеш";
                    break;
                case "Jammu & Kashmir":
                    val = "Джамму & Кашмир";
                    break;
                case "Jharkhand":
                    val = "Джаркханд";
                    break;
                case "Karnataka":
                    val = "Карнатака";
                    break;
                case "Kerala":
                    val = "Керала";
                    break;
                case "Madhya Pradesh":
                    val = "Мадхья-Прадеш";
                    break;
                case "Maharashtra":
                    val = "Махараштра";
                    break;
                case "Manipur":
                    val = "Манипур";
                    break;
                case "Meghalaya":
                    val = "Мегхалая";
                    break;
                case "Mizoram":
                    val = "Мизорам";
                    break;
                case "Nagaland":
                    val = "Нагаленд";
                    break;
                case "Odisha":
                    val = "Одиша";
                    break;
                case "Punjab":
                    val = "Пенджаб";
                    break;
                case "Rajasthan":
                    val = "Раджастхан";
                    break;
                case "Sikkim":
                    val = "Сикким";
                    break;
                case "Tamil Nadu":
                    val = "Тамил Наду";
                    break;
                case "Telangana":
                    val = "Telangana";
                    break;
                case "Tripura":
                    val = "Трипура";
                    break;
                case "Uttar Pradesh":
                    val = "Уттар-Прадеш";
                    break;
                case "Uttarakhand":
                    val = "Уттаракханд";
                    break;
                case "West Bengal":
                    val = "Западная Бенгалия";
                    break;


                //state of kirgistan
                case "Bishkek":
                    val = "г. Бишкек";
                    break;
                case "Tokmok":
                    val = "г. Токмок";
                    break;
                case "Kara–Balta":
                    val = "г. Кара-Балта";
                    break;
                case "Chuy Oblast'":
                    val = "Чуйская область";
                    break;
                case "Balykchi":
                    val = "г. Балыкчи";
                    break;
                case "Cholpon-Ata":
                    val = "г. Чолпон-Ата";
                    break;
                case "Karakol":
                    val = "г. Каракол";
                    break;
                case "Issyk-Kul' Oblast'":
                    val = "Иссык-Кульская область";
                    break;
                case "Talas":
                    val = "г. Талас";
                    break;
                case "Talas Oblast'":
                    val = "Таласская область";
                    break;
                case "Dzhalalаbad":
                    val = "г. Джалал-Абад";
                    break;
                case "Dzhalаlabad Oblast'":
                    val = "Джалал-Абадская область";
                    break;
                case "Osh":
                    val = "г. Ош";
                    break;
                case "Osh Oblast'":
                    val = "Ошская область";
                    break;
                case "Naryn":
                    val = "г. Нарын";
                    break;
                case "Naryn Oblast'":
                    val = "Нарынская область";
                    break;
                case "Batken":
                    val = "г. Баткен";
                    break;
                case "Batken Oblast'":
                    val = "Баткенская область";
                    break;

            }

        }

        return val;
    }

    /**
     * Ritika======
     * get only selective english Language from user input and insert into  Db
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value according the Locals to show on CountrySpinner.
     */

    public static String mSwitch_State(String val, String locale) {

        switch (val) {
            //india

            case "Андхра-Прадеш":
                val = "Andhra Pradesh";
                break;

            case "Аруначал-Прадеш":
                val = "Arunachal Pradesh";
                break;

            case "Ассам":
                val = "Assam";
                break;

            case "Бихар":
                val = "Bihar";
                break;

            case "Чхаттисгарх":
                val = "Chhattisgarh";
                break;

            case "Гоа":
                val = "Goa";
                break;

            case "Гуджарат":
                val = "Gujarat";
                break;

            case "Харьяна":
                val = "Haryana";
                break;

            case "Химачал-Прадеш":
                val = "Himachal Pradesh";
                break;

            case "Джамму &amp; Кашмир":
                val = "Jammu &amp; Kashmir";
                break;

            case "Джаркханд":
                val = "Jharkhand";
                break;

            case "Карнатака":
                val = "Karnataka";
                break;

            case "Керала":
                val = "Kerala";
                break;

            case "Мадхья-Прадеш":
                val = "Madhya Pradesh";
                break;

            case "Махараштра":
                val = "Maharashtra";
                break;

            case "Манипур":
                val = "Manipur";
                break;

            case "Мегхалая":
                val = "Meghalaya";
                break;

            case "Мизорам":
                val = "Mizoram";
                break;

            case "Нагаленд":
                val = "Nagaland";
                break;

            case "Одиша":
                val = "Odisha";
                break;

            case "Пенджаб":
                val = "Punjab";
                break;

            case "Раджастхан":
                val = "Rajasthan";
                break;

            case "Сикким":
                val = "Sikkim";
                break;

            case "Тамил Наду":
                val = "Tamil Nadu";
                break;

            case "Telangana":
                val = "Telangana";
                break;

            case "Трипура":
                val = "Tripura";
                break;

            case "Уттар-Прадеш":
                val = "Uttar Pradesh";
                break;

            case "Уттаракханд":
                val = "Uttarakhand";
                break;

            case "Западная Бенгалия":
                val = "West Bengal";
                break;


            case "г. Бишкек":
                val = "Bishkek";
                break;
            case "г. Токмок":
                val = "Tokmok";
                break;
            case "г. Кара-Балта":
                val = "Kara–Balta";
                break;
            case "Чуйская область":
                val = "Chuy Oblast'";
                break;
            case "г. Балыкчи":
                val = "Balykchi";
                break;
            case "г. Чолпон-Ата":
                val = "Cholpon-Ata";
                break;
            case "г. Каракол":
                val = "Karakol";
                break;
            case "Иссык-Кульская область":
                val = "Issyk-Kul' Oblast'";
                break;
            case "г. Талас":
                val = "Talas";
                break;
            case "Таласская область":
                val = "Talas Oblast'";
                break;
            case "г. Джалал-Абад":
                val = "Dzhalalаbad";
                break;
            case "Джалал-Абадская область":
                val = "Dzhalаlabad Oblast'";
                break;
            case "г. Ош":
                val = "Osh";
                break;
            case "Ошская область":
                val = "Osh Oblast'";
                break;
            case "г. Нарын":
                val = "Naryn";
                break;
            case "Нарынская область":
                val = "Naryn Oblast'";
                break;
            case "г. Баткен":
                val = "Batken";
                break;
            case "Баткенская область":
                val = "Batken Oblast'";
                break;
        }
        return val;
    }

    public static String getTranslatedDays(String val, String locale) {

        if (locale.equalsIgnoreCase("ru")) {

            switch (val) {
                case "Sunday":
                    val = "Воскресенье";
                    break;
                case "Monday":
                    val = "понедельник";
                    break;
                case "Tuesday":
                    val = "вторник";
                    break;
                case "Wednesday":
                    val = "среда";
                    break;

                case "Thursday":
                    val = "Четверг";
                    break;
                case "Friday":
                    val = "Пятница";
                    break;
                case "Saturday":
                    val = "Суббота";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }

    public static String getAppointmentBookStatus(String val, String locale) {

        if (locale.equalsIgnoreCase("ru")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "забронировано";
                    break;
                case "cancelled":
                    val = "отменен";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }

}

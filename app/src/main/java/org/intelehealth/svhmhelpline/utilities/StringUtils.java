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

package org.intelehealth.svhmhelpline.utilities;

import android.widget.Spinner;

import java.io.File;
import java.util.List;

import org.intelehealth.svhmhelpline.app.IntelehealthApplication;

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
        }

        return val;
    }

    public static String getCheckbox_Hi_En_Hi(String checkbox_text) {
        String val = "";

        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            switch (checkbox_text) {
                case "जवाब देने के लिए मना कर दिया":
                    val = "Declined to answer";
                    break;
                default:
                    return val;
            }
        } else {
            val = "Declined to answer";
        }

        return val;
    }

    public static String getSpinnerHi_En(Spinner spinner) {
        String val = "";
        val = spinner.getSelectedItem().toString();

        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            val = switch_hi_en_occupation(val);
            val = switch_hi_en_bankaccount(val);
            val = switch_hi_en_mobile(val);
            val = switch_hi_en_whatsapp(val);
            val = switch_hi_en_sourcewater(val);
            val = switch_hi_en_watersafe(val);
            val = switch_hi_en_wateravail(val);
            val = switch_hi_en_toiletfacil(val);
            val = switch_hi_en_housestructure(val);
        }
        return val;
    }


    public static String switch_hi_housestructure_edit(String val) {
        switch (val) {
            case "Kutcha House":
                val = "कच्चा घर";
                break;
            case "Pakka House":
                val = "पक्का घर";
                break;
            case "Homeless":
                val = "घर नहीं है";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_en_housestructure(String val) {
        switch (val) {
            case "कच्चा घर":
                val = "Kutcha House";
                break;
            case "पक्का घर":
                val = "Pakka House";
                break;
            case "घर नहीं है":
                val = "Homeless";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_toiletfacil_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "जवाब देने के लिए मना कर दिया";
                break;
            case "No facility /uses open space or field":
                val = "कोई सुविधा नहीं / खुली जगह या क्षेत्र का उपयोग करते हैं";
                break;
            case "Own toilet":
                val = "खुद का शौचालय";
                break;
            case "Community toilet":
                val = "सामुदायिक शौचालय";
                break;
            case "Shared toilet with other household":
                val = "अन्य घर के साथ साझा शौचालय";
                break;
            case "Other [Enter]":
                val = "अन्य [दर्ज करें]";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_en_toiletfacil(String val) {
        switch (val) {
            case "जवाब देने के लिए मना कर दिया":
                val = "Declined to answer";
                break;
            case "कोई सुविधा नहीं / खुली जगह या क्षेत्र का उपयोग करते हैं":
                val = "No facility /uses open space or field";
                break;
            case "खुद का शौचालय":
                val = "Own toilet";
                break;
            case "सामुदायिक शौचालय":
                val = "Community toilet";
                break;
            case "अन्य घर के साथ साझा शौचालय":
                val = "Shared toilet with other household";
                break;
            case "अन्य [दर्ज करें]":
                val = "Other [Enter]";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_en_wateravail(String val) {
        switch (val) {
            case "जवाब देने के लिए मना कर दिया":
                val = "Declined to answer";
                break;
            case "हाँ":
                val = "Yes";
                break;
            case "नहीं":
                val = "No";
                break;
            case "पता नहीं":
                val = "Don\'t know";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_watersafe(String val) {
        switch (val) {
            case "जवाब देने के लिए मना कर दिया":
                val = "Declined to answer";
                break;
            case "कुछ भी नहीं":
                val = "Nothing";
                break;
            case "उबलना":
                val = "Boil";
                break;
            case "अलम":
                val = "Alum";
                break;
            case "ब्लीच / क्लोरीन गोलियाँ जोड़ें":
                val = "Add Bleach/Chlorine tablets";
                break;
            case "कपड़े के माध्यम":
                val = "Strain through cloth";
                break;
            case "पानी फिल्टर (सिरेमिक / रेत / समग्र) आदि का उपयोग करें":
                val = "Use water filter(ceramic/sand/composite)etc";
                break;
            case "इलेक्ट्रॉनिक फ़िल्टर का उपयोग करें":
                val = "Use electronic filter";
                break;
            case "अन्य [दर्ज करें]":
                val = "Other[Enter]";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_sourcewater(String val) {
        switch (val) {
            case "जवाब देने के लिए मना कर दिया":
                val = "Declined to answer";
                break;
            case "चापाकल/हैण्ड पंप":
                val = "Chapakal/Hand Pump";
                break;
            case "कुंवा":
                val = "Wells";
                break;
            case "बोरिंग":
                val = "Boring";
                break;
            case "नदी/तालाब":
                val = "Rivers/ponds";
                break;
            case "टैंकर का पानी":
                val = "Tanker water ";
                break;
            case "कोई और":
                val = "Any other";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_occupation(String val) {
        switch (val) {
            case "जवाब देने के लिए मना कर दिया":
                val = "Declined to answer";
                break;
            case "सरकारी नौकरी":
                val = "Government job";
                break;
            case "बड़ा या मध्यम उद्योग":
                val = "Large scale to medium scale industry";
                break;
            case "निजी क्षेत्र में नौकरी":
                val = "Professional job in private sector";
                break;
            case "छोटा उद्योग":
                val = "Small scale industry";
                break;
            case "बड़ी दूकान के मालिक":
                val = "Big shop owner";
                break;
            case "तकनीशियन":
                val = "Technician/craftsman";
                break;
            case "छोटे दूकान का मालिक":
                val = "Small shop owner";
                break;
            case "बड़ा किसान":
                val = "Large scale farmer";
                break;
            case "दैनिक मजदूर":
                val = "Daily wage earner";
                break;
            case "छोटे किसान/ दुसरे के खेत में काम करने वाले":
                val = "Small scale farmer/farm worker";
                break;
            case "बेरोजगार":
                val = "Unemployed";
                break;
            case "ग्रहिणी":
                val = "Housewife";
                break;
            case "अन्य कुशलता (ड्राईवर,राज मिस्त्री)":
                val = "Other skills (driver,mason etc)";
                break;
            case "वर्णन करे":
                val = "[Describe]";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_bankaccount(String val) {
        switch (val) {
            case "जवाब देने के लिए मना कर दिया":
                val = "Declined to answer";
                break;
            case "हाँ":
                val = "Yes";
                break;
            case "नहीं":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_mobile(String val) {
        switch (val) {
            case "साधारण फोन":
                val = "Basic Phone";
                break;
            case "स्मार्टफोन":
                val = "Smartphone";
                break;
            case "मोबाइल फोन नहीं है":
                val = "Does not own mobile phone";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_whatsapp(String val) {
        switch (val) {
            case "हाँ":
                val = "Yes";
                break;
            case "नहीं":
                val = "No";
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

    public static String switch_hi_bankaccount_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "जवाब देने के लिए मना कर दिया";
                break;
            case "Yes":
                val = "हाँ";
                break;
            case "No":
                val = "नहीं";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_mobiletype_edit(String val) {
        switch (val) {
            case "Basic Phone":
                val = "साधारण फोन";
                break;
            case "Smartphone":
                val = "स्मार्टफोन";
                break;
            case "Does not own mobile phone":
                val = "मोबाइल फोन नहीं है";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_watersource_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "जवाब देने के लिए मना कर दिया";
                break;
            case "Chapakal/Hand Pump":
                val = "चापाकल/हैण्ड पंप";
                break;
            case "Wells":
                val = "कुंवा";
                break;
            case "Boring":
                val = "बोरिंग";
                break;
            case "Rivers/ponds":
                val = "नदी/तालाब";
                break;
            case "Tanker water":
                val = "टैंकर का पानी";
                break;
            case "Any other":
                val = "कोई और";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_wateravail_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "जवाब देने के लिए मना कर दिया";
                break;
            case "Yes":
                val = "हाँ";
                break;
            case "No":
                val = "नहीं";
                break;
            case "Don\'t know":
                val = "पता नहीं";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_watersafe_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "जवाब देने के लिए मना कर दिया";
                break;
            case "Nothing":
                val = "कुछ भी नहीं";
                break;
            case "Boil":
                val = "उबलना";
                break;
            case "Alum":
                val = "अलम";
                break;
            case "Add Bleach/Chlorine tablets":
                val = "ब्लीच / क्लोरीन गोलियाँ जोड़ें";
                break;
            case "Strain through cloth":
                val = "कपड़े के माध्यम";
                break;
            case "Use water filter(ceramic/sand/composite)etc":
                val = "पानी फिल्टर (सिरेमिक / रेत / समग्र) आदि का उपयोग करें";
                break;
            case "Use electronic filter":
                val = "इलेक्ट्रॉनिक फ़िल्टर का उपयोग करें";
                break;
            case "Other[Enter]":
                val = "अन्य [दर्ज करें]";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_whatsapp_edit(String val) {
        switch (val) {
            case "Yes":
                val = "हाँ";
                break;
            case "No":
                val = "नहीं";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_occupation_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "जवाब देने के लिए मना कर दिया";
                break;
            case "Government job":
                val = "सरकारी नौकरी";
                break;
            case "Large scale to medium scale industry":
                val = "बड़ा या मध्यम उद्योग";
                break;
            case "Professional job in private sector":
                val = "निजी क्षेत्र में नौकरी";
                break;
            case "Small scale industry":
                val = "छोटा उद्योग";
                break;
            case "Big shop owner":
                val = "बड़ी दूकान के मालिक";
                break;
            case "Technician/craftsman":
                val = "तकनीशियन";
                break;
            case "Small shop owner":
                val = "छोटे दूकान का मालिक";
                break;
            case "Large scale farmer":
                val = "बड़ा किसान";
                break;
            case "Daily wage earner":
                val = "दैनिक मजदूर";
                break;
            case "Small scale farmer/farm worker":
                val = "छोटे किसान/ दुसरे के खेत में काम करने वाले";
                break;
            case "Unemployed":
                val = "बेरोजगार";
                break;
            case "Housewife":
                val = "ग्रहिणी";
                break;
            case "Other skills (driver,mason etc)":
                val = "अन्य कुशलता (ड्राईवर,राज मिस्त्री)";
                break;
            case "[Describe]":
                val = "वर्णन करे";
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

    public static String hi_or_as__en_month(int month_index) {
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

    /*
    * @return mdob_text : Assamese month translation is passed.
    * @param dob : DOB in the format DD MMMM YYYY eg. 15 January 2021
    */
/*
    public static String en__as_dob(String dob) { //English dob is replaced to Assamese text.
        String mdob_text = dob
                .replace("January", "জানুৱাৰী")
                .replace("February", "ফেব্ৰুৱাৰী")
                .replace("March", "মাৰ্চ")
                .replace("April", "এপ্ৰিল")
                .replace("May", "মে")
                .replace("June", "জুন")
                .replace("July", "জুলাই")
                .replace("August", "আগষ্ট")
                .replace("September", "ছেপ্টেম্বৰ")
                .replace("October", "অক্টোবৰ")
                .replace("November", "নৱেম্বৰ")
                .replace("December", "ডিচেম্বৰ");

        return mdob_text;
    }
*/


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

    public static String hi_or_as__en_noEdit(String dobString, String locale) {

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
/*
        else if(locale.equalsIgnoreCase("as")) {
            //Odiya
            String dob = dobString
                    .replace("জানুৱাৰী", "January")
                    .replace("ফেব্ৰুৱাৰী", "February")
                    .replace("মাৰ্চ", "March")
                    .replace("এপ্ৰিল", "April")
                    .replace("মে", "May")
                    .replace("জুন", "June")
                    .replace("জুলাই", "July")
                    .replace("আগষ্ট", "August")
                    .replace("ছেপ্টেম্বৰ", "September")
                    .replace("অক্টোবৰ", "October")
                    .replace("নৱেম্বৰ", "November")
                    .replace("ডিচেম্বৰ", "December");
            return dob;
        }
*/
        else {
            return dobString;
        }
    }

    public static String getState(String m_State) {
        switch(m_State) {
            case "आंध्र प्रदेश":
                m_State = "Andhra Pradesh";
                break;
            case "अरुणाचल प्रदेश":
                m_State = "Arunachal Pradesh";
                break;
            case "असम":
                m_State = "Assam";
                break;
            case "बिहार":
                m_State = "Bihar";
                break;
            case "छत्तीसगढ":
                m_State = "Chhattisgarh";
                break;
            case "दिल्ली":
                m_State = "Delhi";
                break;
            case "गोवा":
                m_State = "Goa";
                break;
            case "गुजरात":
                m_State = "Gujarat";
                break;
            case "हरियाणा":
                m_State = "Haryana";
                break;
            case "हिमाचल प्रदेश":
                m_State = "Himachal Pradesh";
                break;
            case "जम्मू - कश्मीर":
                m_State = "Jammu &amp; Kashmir";
                break;
            case "झारखंड":
                m_State = "Jharkhand";
                break;
            case "कर्नाटक":
                m_State = "Karnataka";
                break;
            case "केरल":
                m_State = "Kerala";
                break;
            case "मध्य प्रदेश":
                m_State = "Madhya Pradesh";
                break;
            case "महाराष्ट्र":
                m_State = "Maharashtra";
                break;
            case "मणिपुर":
                m_State = "Manipur";
                break;
            case "मेघालय":
                m_State = "Meghalaya";
                break;
            case "मिजोरम":
                m_State = "Mizoram";
                break;
            case "नगालैंड":
                m_State = "Nagaland";
                break;
            case "ओडिशा":
                m_State = "Odisha";
                break;
            case "पंजाब":
                m_State = "Punjab";
                break;
            case "राजस्थान":
                m_State = "Rajasthan";
                break;
            case "सिक्किम":
                m_State = "Sikkim";
                break;
            case "तमिलनाडु":
                m_State = "Tamil Nadu";
                break;
            case "तेलंगाना":
                m_State = "Telangana";
                break;
            case "त्रिपुरा":
                m_State = "Tripura";
                break;
            case "उत्तर प्रदेश":
                m_State = "Uttar Pradesh";
                break;
            case "उत्तराखंड":
                m_State = "Uttarakhand";
                break;
            case "पश्चिम बंगाल":
                m_State = "West Bengal";
                break;
            default:
                return m_State;

        }
        return m_State;
    }

    public static String getState_edit(String m_State) {
        switch(m_State) {
            case "Andhra Pradesh":
                m_State = "आंध्र प्रदेश";
                break;
            case "Arunachal Pradesh":
                m_State = "अरुणाचल प्रदेश";
                break;
            case "Assam":
                m_State = "असम";
                break;
            case "Bihar":
                m_State = "बिहार";
                break;
            case "Chhattisgarh":
                m_State = "छत्तीसगढ";
                break;
            case "Delhi":
                m_State = "दिल्ली";
                break;
            case "Goa":
                m_State = "गोवा";
                break;
            case "Gujarat":
                m_State = "गुजरात";
                break;
            case "Haryana":
                m_State = "हरियाणा";
                break;
            case "Himachal Pradesh":
                m_State = "हिमाचल प्रदेश";
                break;
            case "Jammu &amp; Kashmir":
                m_State = "जम्मू - कश्मीर";
                break;
            case "Jharkhand":
                m_State = "झारखंड";
                break;
            case "Karnataka":
                m_State = "कर्नाटक";
                break;
            case "Kerala":
                m_State = "केरल";
                break;
            case "Madhya Pradesh":
                m_State = "मध्य प्रदेश";
                break;
            case "Maharashtra":
                m_State = "महाराष्ट्र";
                break;
            case "Manipur":
                m_State = "मणिपुर";
                break;
            case "Meghalaya":
                m_State = "मेघालय";
                break;
            case "Mizoram":
                m_State = "मिजोरम";
                break;
            case "Nagaland":
                m_State = "नगालैंड";
                break;
            case "Odisha":
                m_State = "ओडिशा";
                break;
            case "Punjab":
                m_State = "पंजाब";
                break;
            case "Rajasthan":
                m_State = "राजस्थान";
                break;
            case "Sikkim":
                m_State = "सिक्किम";
                break;
            case "Tamil Nadu":
                m_State = "तमिलनाडु";
                break;
            case "Telangana":
                m_State = "तेलंगाना";
                break;
            case "Tripura":
                m_State = "त्रिपुरा";
                break;
            case "Uttar Pradesh":
                m_State = "उत्तर प्रदेश";
                break;
            case "Uttarakhand":
                m_State = "उत्तराखंड";
                break;
            case "West Bengal":
                m_State = "पश्चिम बंगाल";
                break;
            default:
                return m_State;

        }
        return m_State;
    }

    public static String getDistrict(String m_District) {
        switch (m_District) {
            case "अल्मोड़ा":
                m_District = "Almora";
                break;
            case "बागेश्वर":
                m_District = "Bageshwar";
                break;
            case "चमोली":
                m_District = "Chamoli";
                break;
            case "चम्पावत":
                m_District = "Champawat";
                break;
            case "देहरादून":
                m_District = "Dehradun";
                break;
            case "हरिद्वार":
                m_District = "Haridwar";
                break;
            case "नैनीताल":
                m_District = "Nainital";
                break;
            case "पौरीस":
                m_District = "Pauri";
                break;
            case "पिथोरागढ़":
                m_District = "Pithoragarh";
                break;
            case "रुद्रप्रयाग":
                m_District = "Rudraprayag";
                break;
            case "टिहरी":
                m_District = "Tehri";
                break;
            case "ऊधम":
                m_District = "Udham";
                break;
            case "उत्तरकाशी":
                m_District = "Uttarkashi";
                break;
            case "अन्य":
                m_District = "Others";
                break;
            default:
                return m_District;
        }
        return m_District;
    }

    public static String getDistrict_edit(String m_District) {
        switch (m_District) {
            case "Almora":
                m_District = "अल्मोड़ा";
                break;
            case "Bageshwar":
                m_District = "बागेश्वर";
                break;
            case "Chamoli":
                m_District = "चमोली";
                break;
            case "Champawat":
                m_District = "चम्पावत";
                break;
            case "Dehradun":
                m_District = "देहरादून";
                break;
            case "Haridwar":
                m_District = "हरिद्वार";
                break;
            case "Nainital":
                m_District = "नैनीताल";
                break;
            case "Pauri":
                m_District = "पौरीस";
                break;
            case "Pithoragarh":
                m_District = "पिथोरागढ़";
                break;
            case "Rudraprayag":
                m_District = "रुद्रप्रयाग";
                break;
            case "Tehri":
                m_District = "टिहरी";
                break;
            case "Udham":
                m_District = "ऊधम";
                break;
            case "Uttarkashi":
                m_District = "उत्तरकाशी";
                break;
            case "Others":
                m_District = "अन्य";
                break;
            default:
                return m_District;
        }
        return m_District;
    }
}

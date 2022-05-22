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

package org.intelehealth.ekalarogya.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.io.File;
import java.util.List;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.json.JSONArray;

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

    public static String getSurveyValue(String value) {
        String val = "-";
        if (value != null && !value.equals(""))
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
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            switch (checkbox_text) {
                case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
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

    public static String getVaccinationSpinnerHi_En(Spinner spinner) {
        String val = "";
        val = spinner.getSelectedItem().toString();

        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            val = switch_hi_en_vaccination(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_en_vaccination(val);
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
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_en_occupation(val);
            val = switch_or_en_bankaccount(val);
            val = switch_or_en_mobile(val);
            val = switch_or_en_whatsapp(val);
            val = switch_or_en_sourcewater(val);
            val = switch_or_en_watersafe(val);
            val = switch_or_en_wateravail(val);
            val = switch_or_en_toiletfacil(val);
            val = switch_or_en_housestructure(val);
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

    public static String switch_or_housestructure_edit(String val) {
        switch (val) {
            case "Kutcha House":
                val = "କୁଚା ହାଉସ୍";
                break;
            case "Pakka House":
                val = "ପାକ୍କା ହାଉସ୍";
                break;
            case "Homeless":
                val = "ଗୃହହୀନ";
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

    public static String switch_or_en_housestructure(String val) {
        switch (val) {
            case "କୁଚା ହାଉସ୍":
                val = "Kutcha House";
                break;
            case "ପାକ୍କା ହାଉସ୍":
                val = "Pakka House";
                break;
            case "ଗୃହହୀନ":
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

    public static String switch_or_toiletfacil_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ";
                break;
            case "No facility /uses open space or field":
                val = "କ facility ଣସି ସୁବିଧା / ଖୋଲା ସ୍ଥାନ କିମ୍ବା କ୍ଷେତ୍ର ବ୍ୟବହାର କରେ ନାହିଁ";
                break;
            case "Own toilet":
                val = "ନିଜର ଶ et ଚାଳୟ";
                break;
            case "Community toilet":
                val = "ସମ୍ପ୍ରଦାୟ ଶ et ଚାଳୟ";
                break;
            case "Shared toilet with other household":
                val = "ଅନ୍ୟ ଘର ସହିତ ଶ toil ଚାଳୟ ଅଂଶୀଦାର";
                break;
            case "Other [Enter]":
                val = "ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]";
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

    public static String switch_or_en_toiletfacil(String val) {
        switch (val) {
            case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
                val = "Declined to answer";
                break;
            case "କ facility ଣସି ସୁବିଧା / ଖୋଲା ସ୍ଥାନ କିମ୍ବା କ୍ଷେତ୍ର ବ୍ୟବହାର କରେ ନାହିଁ":
                val = "No facility /uses open space or field";
                break;
            case "ନିଜର ଶ et ଚାଳୟ":
                val = "Own toilet";
                break;
            case "ସମ୍ପ୍ରଦାୟ ଶ et ଚାଳୟ":
                val = "Community toilet";
                break;
            case "ଅନ୍ୟ ଘର ସହିତ ଶ toil ଚାଳୟ ଅଂଶୀଦାର":
                val = "Shared toilet with other household";
                break;
            case "ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]":
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

    public static String switch_or_en_wateravail(String val) {
        switch (val) {
            case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
                val = "Declined to answer";
                break;
            case "ହଁ":
                val = "Yes";
                break;
            case "ନା":
                val = "No";
                break;
            case "ଜାଣ ନାହିଁ":
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

    public static String switch_or_en_watersafe(String val) {
        switch (val) {
            case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
                val = "Declined to answer";
                break;
            case "କିଛି ନାହିଁ":
                val = "Nothing";
                break;
            case "ଫୁଟାନ୍ତୁ":
                val = "Boil";
                break;
            case "ଆଲୁମ୍":
                val = "Alum";
                break;
            case "ବ୍ଲିଚ୍ / କ୍ଲୋରାଇନ୍ ଟାବଲେଟ୍ ମିଶାନ୍ତୁ":
                val = "Add Bleach/Chlorine tablets";
                break;
            case "କପଡା ମାଧ୍ୟମରେ ଛାଣନ୍ତୁ":
                val = "Strain through cloth";
                break;
            case "ପାଣି ଫିଲ୍ଟର (ସିରାମିକ୍ / ବାଲି / କମ୍ପୋଜିଟ୍) ଇତ୍ୟାଦି ବ୍ୟବହାର କରନ୍ତୁ":
                val = "Use water filter(ceramic/sand/composite)etc";
                break;
            case "ଇଲେକ୍ଟ୍ରୋନିକ୍ ଫିଲ୍ଟର୍ ବ୍ୟବହାର କରନ୍ତୁ":
                val = "Use electronic filter";
                break;
            case "ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]":
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

    public static String switch_or_en_sourcewater(String val) {
        switch (val) {
            case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
                val = "Declined to answer";
                break;
            case "ଚାପକାଲ / ହ୍ୟାଣ୍ଡ ପମ୍ପ |":
                val = "Chapakal/Hand Pump";
                break;
            case "କୂଅ":
                val = "Wells";
                break;
            case "ବିରକ୍ତିକର":
                val = "Boring";
                break;
            case "ନଦୀ / ପୋଖରୀ":
                val = "Rivers/ponds";
                break;
            case "ଟ୍ୟାଙ୍କର୍ ପାଣି":
                val = "Tanker water ";
                break;
            case "ଅନ୍ୟ କ .ଣସି":
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
            case "विद्यार्थी":
                val = "Student";
                break;
            case "5 साल से कम उम्र के बच्चे":
                val = "Under 5 child";
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

    public static String switch_or_en_occupation(String val) {
        switch (val) {
            case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
                val = "Declined to answer";
                break;
            case "ସରକାରୀ ଚାକିରି":
                val = "Government job";
                break;
            case "ମଧ୍ୟମ ଧରଣର ଶିଳ୍ପ ପାଇଁ ବଡ଼ ଆକାର":
                val = "Large scale to medium scale industry";
                break;
            case "ବେସରକାରୀ କ୍ଷେତ୍ରରେ ବୃତ୍ତିଗତ କାର୍ଯ୍ୟ":
                val = "Professional job in private sector";
                break;
            case "କ୍ଷୁଦ୍ର ଶିଳ୍ପ":
                val = "Small scale industry";
                break;
            case "ବଡ ଦୋକାନ ମାଲିକ":
                val = "Big shop owner";
                break;
            case "ଟେକ୍ନିସିଆନ୍ / କାରିଗର":
                val = "Technician/craftsman";
                break;
            case "ଛୋଟ ଦୋକାନ ମାଲିକ":
                val = "Small shop owner";
                break;
            case "ବଡ଼ ଧରଣର କୃଷକ":
                val = "Large scale farmer";
                break;
            case "ଦ Daily ନିକ ମଜୁରୀ":
                val = "Daily wage earner";
                break;
            case "କ୍ଷୁଦ୍ର କୃଷକ / ଚାଷ ଶ୍ରମିକ":
                val = "Small scale farmer/farm worker";
                break;
            case "ବେକାର":
                val = "Unemployed";
                break;
            case "ଗୃହିଣୀ":
                val = "Housewife";
                break;
            case "ଛାତ୍ର":
                val = "Student";
                break;
            case "5 ବର୍ଷରୁ କମ୍ ପିଲା":
                val = "Under 5 child";
                break;
            case "ଅନ୍ୟାନ୍ୟ କ skills ଶଳ (ଡ୍ରାଇଭର, ମସନ୍ ଇତ୍ୟାଦି)":
                val = "Other skills (driver,mason etc)";
                break;
            case "[ବର୍ଣ୍ଣନା କର]":
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

    public static String switch_or_en_bankaccount(String val) {
        switch (val) {
            case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
                val = "Declined to answer";
                break;
            case "ହଁ":
                val = "Yes";
                break;
            case "ନା":
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

    public static String switch_or_en_mobile(String val) {
        switch (val) {
            case "ମ Basic ଳିକ ଫୋନ୍":
                val = "Basic Phone";
                break;
            case "ସ୍ମାର୍ଟଫୋନ୍":
                val = "Smartphone";
                break;
            case "ମୋବାଇଲ୍ ଫୋନ୍ ର ମାଲିକାନା ନାହିଁ":
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

    public static String switch_or_en_whatsapp(String val) {
        switch (val) {
            case "ହଁ":
                val = "Yes";
                break;
            case "ନା":
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

    public static String switch_or_economic(String val) {
        switch (val) {
            case "APL"://------replace with odiya
                val = "APL";
                break;
            case "BPL"://------replace with odiya
                val = "BPL";
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

    public static String switch_or_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "APL";//------replace with odiya
                break;
            case "BPL":
                val = "BPL";
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

    //---------change condition in odiya--------
    public static String switch_or_caste(String val) {
        switch (val) {
            case "General"://-------replace with Odiya
                val = "General";
                break;
            case "OBC":
                val = "OBC";
                break;
            case "SC":
                val = "SC";
                break;
            case "ST":
                val = "ST";
                break;
            case "others":
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

    public static String switch_or_bankaccount_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ";
                break;
            case "Yes":
                val = "ହଁ";
                break;
            case "No":
                val = "ନା";
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

    public static String switch_or_mobiletype_edit(String val) {
        switch (val) {
            case "Basic Phone":
                val = "ମ Basic ଳିକ ଫୋନ୍";
                break;
            case "Smartphone":
                val = "ସ୍ମାର୍ଟଫୋନ୍";
                break;
            case "Does not own mobile phone":
                val = "ମୋବାଇଲ୍ ଫୋନ୍ ର ମାଲିକାନା ନାହିଁ";
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

    public static String switch_or_watersource_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ";
                break;
            case "Chapakal/Hand Pump":
                val = "ଚାପକାଲ / ହ୍ୟାଣ୍ଡ ପମ୍ପ |";
                break;
            case "Wells":
                val = "କୂଅ";
                break;
            case "Boring":
                val = "ବିରକ୍ତିକର";
                break;
            case "Rivers/ponds":
                val = "ନଦୀ / ପୋଖରୀ";
                break;
            case "Tanker water":
                val = "ଟ୍ୟାଙ୍କର୍ ପାଣି";
                break;
            case "Any other":
                val = "ଅନ୍ୟ କ .ଣସି";
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

    public static String switch_or_wateravail_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ";
                break;
            case "Yes":
                val = "ହଁ";
                break;
            case "No":
                val = "ନା";
                break;
            case "Don\'t know":
                val = "ଜାଣ ନାହିଁ";
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

    public static String switch_or_watersafe_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ";
                break;
            case "Nothing":
                val = "କିଛି ନାହିଁ";
                break;
            case "Boil":
                val = "ଫୁଟାନ୍ତୁ";
                break;
            case "Alum":
                val = "ଆଲୁମ୍";
                break;
            case "Add Bleach/Chlorine tablets":
                val = "ବ୍ଲିଚ୍ / କ୍ଲୋରାଇନ୍ ଟାବଲେଟ୍ ମିଶାନ୍ତୁ";
                break;
            case "Strain through cloth":
                val = "କପଡା ମାଧ୍ୟମରେ ଛାଣନ୍ତୁ";
                break;
            case "Use water filter(ceramic/sand/composite)etc":
                val = "ପାଣି ଫିଲ୍ଟର (ସିରାମିକ୍ / ବାଲି / କମ୍ପୋଜିଟ୍) ଇତ୍ୟାଦି ବ୍ୟବହାର କରନ୍ତୁ";
                break;
            case "Use electronic filter":
                val = "ଇଲେକ୍ଟ୍ରୋନିକ୍ ଫିଲ୍ଟର୍ ବ୍ୟବହାର କରନ୍ତୁ";
                break;
            case "Other[Enter]":
                val = "ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]";
                break;
            default:
                return val;
        }
        return val;
    }

    //Vaccination
    public static String switch_hi_en_vaccination(String val) {
        switch (val) {
            case "पहला टिका":
                val = "First dose";
                break;
            case "दूसरा टिका":
                val = "Second dose";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_vaccination(String val) {
        switch (val) {
            case "ପ୍ରଥମ ଡୋଜ୍":
                val = "First dose";
                break;
            case "ଦ୍ୱିତୀୟ ମାତ୍ରା":
                val = "Second dose";
                break;
            default:
                return val;
        }
        return val;
    }

    //Vaccination Edit
    public static String switch_hi_vaccination_edit(String val) {
        switch (val) {
            case "First dose":
                val = "पहला टिका";
                break;
            case "Second dose":
                val = "दूसरा टिका";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_vaccination_edit(String val) {
        switch (val) {
            case "First dose":
                val = "ପ୍ରଥମ ଡୋଜ୍";
                break;
            case "Second dose":
                val = "ଦ୍ୱିତୀୟ ମାତ୍ରା";
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

    public static String switch_or_whatsapp_edit(String val) {
        switch (val) {
            case "Yes":
                val = "ହଁ";
                break;
            case "No":
                val = "ନା";
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
            case "Student":
                val = "विद्यार्थी";
                break;
            case "Under 5 child":
                val = "5 साल से कम उम्र के बच्चे";
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

    public static String switch_or_occupation_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ";
                break;
            case "Government job":
                val = "ସରକାରୀ ଚାକିରି";
                break;
            case "Large scale to medium scale industry":
                val = "ମଧ୍ୟମ ଧରଣର ଶିଳ୍ପ ପାଇଁ ବଡ଼ ଆକାର";
                break;
            case "Professional job in private sector":
                val = "ବେସରକାରୀ କ୍ଷେତ୍ରରେ ବୃତ୍ତିଗତ କାର୍ଯ୍ୟ";
                break;
            case "Small scale industry":
                val = "କ୍ଷୁଦ୍ର ଶିଳ୍ପ";
                break;
            case "Big shop owner":
                val = "ବଡ ଦୋକାନ ମାଲିକ";
                break;
            case "Technician/craftsman":
                val = "ଟେକ୍ନିସିଆନ୍ / କାରିଗର";
                break;
            case "Small shop owner":
                val = "ଛୋଟ ଦୋକାନ ମାଲିକ";
                break;
            case "Large scale farmer":
                val = "ବଡ଼ ଧରଣର କୃଷକ";
                break;
            case "Daily wage earner":
                val = "ଦ Daily ନିକ ମଜୁରୀ";
                break;
            case "Small scale farmer/farm worker":
                val = "କ୍ଷୁଦ୍ର କୃଷକ / ଚାଷ ଶ୍ରମିକ";
                break;
            case "Unemployed":
                val = "ବେକାର";
                break;
            case "Housewife":
                val = "ଗୃହିଣୀ";
                break;
            case "Student":
                val = "ଛାତ୍ର";
                break;
            case "Under 5 child":
                val = "5 ବର୍ଷରୁ କମ୍ ପିଲା";
                break;
            case "Other skills (driver,mason etc)":
                val = "ଅନ୍ୟାନ୍ୟ କ skills ଶଳ (ଡ୍ରାଇଭର, ମସନ୍ ଇତ୍ୟାଦି)";
                break;
            case "[Describe]":
                val = "[ବର୍ଣ୍ଣନା କର]";
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
                val = "General";//-----replace with odiya
                break;
            case "OBC":
                val = "OBC";
                break;
            case "SC":
                val = "SC";
                break;
            case "ST":
                val = "ST";
                break;
            case "others":
                val = "others";
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

    public static String hi_or__en_noEdit(String dobString, String locale) {

        if (locale.equalsIgnoreCase("hi")) {
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

    public static String getTranslatedDays(String val, String locale) {

        if (locale.equalsIgnoreCase("hi")) {

            switch (val) {
                case "Sunday":
                    val = "रविवार";
                    break;
                case "Monday":
                    val = "सोमवार";
                    break;
                case "Tuesday":
                    val = "मंगलवार";
                    break;
                case "Wednesday":
                    val = "बुधवार";
                    break;

                case "Thursday":
                    val = "गुरूवार";
                    break;
                case "Friday":
                    val = "शुक्रवार";
                    break;
                case "Saturday":
                    val = "शनिवार";
                    break;
                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("or")) {

            switch (val) {
                case "Sunday":
                    val = "ରବିବାର";
                    break;
                case "Monday":
                    val = "ସୋମବାର";
                    break;
                case "Tuesday":
                    val = "ମଙ୍ଗଳବାର";
                    break;
                case "Wednesday":
                    val = "ବୁଧବାର";
                    break;

                case "Thursday":
                    val = "ଗୁରୁବାର";
                    break;
                case "Friday":
                    val = "ଶୁକ୍ରବାର";
                    break;
                case "Saturday":
                    val = "ଶନିବାର";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }

    public static String getAppointmentBookStatus(String val, String locale) {

        if (locale.equalsIgnoreCase("hi")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "बुक";
                    break;
                case "cancelled":
                    val = "रद्द";
                    break;
                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("or")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "ବୁକ୍ ହୋଇଛି";
                    break;
                case "cancelled":
                    val = "ବାତିଲ";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }

    public static boolean checkIfEmpty(Context context, String text) {
        return text.equals(context.getString(R.string.select)) || text.equals("");
    }

    public static boolean checkIfCheckboxesEmpty(ViewGroup viewGroup) {
        int selectedCheckboxes = 0;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof CheckBox && ((CheckBox) childAt).isChecked())
                selectedCheckboxes++;
        }
        return selectedCheckboxes == 0;
    }

    public static String getSelectedCheckboxes(ViewGroup viewGroup, Context context, Context updatedContext, String locale, String otherString) {
        if (viewGroup == null)
            return null;

        String text = "-";

        JSONArray result = new JSONArray();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof CheckBox && ((CheckBox) childAt).isChecked()) {
                text = getSurveyStrings(((CheckBox) childAt).getText().toString(), context, updatedContext, locale);

                // Handling cases when the user selects any string which relates to Other
                if (text.equalsIgnoreCase(updatedContext.getString(R.string.other_source_of_income_please_specify))
                        || text.equalsIgnoreCase(updatedContext.getString(R.string.other_specify))
                        || text.equalsIgnoreCase(updatedContext.getString(R.string.other_reasons_specify))
                ) {
                    text = text.concat(" : " + otherString);
                }
                result.put(text);
            }
        }

        return result.toString();
    }

    public static void setSelectedCheckboxes(ViewGroup viewGroup, String text, Context context, Context updatedContext, String locale) {
        if (viewGroup == null)
            return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) childAt;
                checkBox.setChecked(text.contains(getSurveyStrings(checkBox.getText().toString(), updatedContext, context, locale)));
            }

            if (childAt instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) childAt;
                if (text.contains(getSurveyStrings(radioButton.getText().toString(), updatedContext, context, locale))) {
                    radioButton.setChecked(true);
                    break;
                }
                if (text.contains(getSmokingHistoryStrings(radioButton.getText().toString(), updatedContext, context, locale))) {
                    radioButton.setChecked(true);
                    break;
                }
            }
        }
    }

    public static int getIndex(Spinner spinner, String s) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(s))
                return i;
        }
        return -1;
    }

    public static String getSurveyStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Pucca to English
            if (context.getString(R.string.pucca).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.pucca);
            }
            // Translate string Kucha to English
            if (context.getString(R.string.kucha).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.kucha);
            }
            // Translate string Hindu to English
            if (context.getString(R.string.hindu).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.hindu);
            }
            // Translate string Muslim to English
            if (context.getString(R.string.muslim).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.muslim);
            }
            // Translate string Christian to English
            if (context.getString(R.string.christian).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.christian);
            }
            // Translate string Sikh to English
            if (context.getString(R.string.sikh).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.sikh);
            }
            // Translate string Buddhist / Neo-Buddhist to English
            if (context.getString(R.string.buddhist_neo_buddhist).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.buddhist_neo_buddhist);
            }
            // Translate string Jain to English
            if (context.getString(R.string.jain).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.jain);
            }
            // Translate string Jewish to English
            if (context.getString(R.string.jewish).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.jewish);
            }
            // Translate string Parsi Zoroastrian to English
            if (context.getString(R.string.parsi_zoroastrian).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.parsi_zoroastrian);
            }
            // Translate string No Religion to English
            if (context.getString(R.string.no_religion).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_religion);
            }
            // Translate string Schedule Caste to English
            if (context.getString(R.string.schedule_caste).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.schedule_caste);
            }
            // Translate string Schedule Tribe to English
            if (context.getString(R.string.schedule_tribe).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.schedule_tribe);
            }
            // Translate string Other Backward Caste to English
            if (context.getString(R.string.survey_other_backward_caste).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.survey_other_backward_caste);
            }
            // Translate string General to English
            if (context.getString(R.string.survey_general).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.survey_general);
            }
            // Translate string Sale of Cereal Production to English
            if (context.getString(R.string.sale_of_cereal_production).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.sale_of_cereal_production);
            }
            // Translate string Sale of Animal Or Animal Products to English
            if (context.getString(R.string.sale_of_animals_or_animal_products).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.sale_of_animals_or_animal_products);
            }
            // Translate string Agricultural Wage Labor to English
            if (context.getString(R.string.agricultural_wage_labor_employed_for_farm_work).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.agricultural_wage_labor_employed_for_farm_work);
            }
            // Translate string Salaried Worker to English
            if (context.getString(R.string.salaried_worker_fixed_monthly_salary).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.salaried_worker_fixed_monthly_salary);
            }
            // Translate string Salaried Worker to English
            if (context.getString(R.string.self_employed_non_agricultural_petty_business).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.self_employed_non_agricultural_petty_business);
            }
            // Translate string Salaried Worker to English
            if (context.getString(R.string.self_employed_non_agricultural_petty_business).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.self_employed_non_agricultural_petty_business);
            }
            // Translate string Daily Labor to English
            if (context.getString(R.string.daily_labor_unskilled_work_agricultural_non_agricultural).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.daily_labor_unskilled_work_agricultural_non_agricultural);
            }
            // Translate string NREGA to English
            if (context.getString(R.string.nrega).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.nrega);
            }
            // Translate string Seasonal Labor to English
            if (context.getString(R.string.seasonal_labor).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.seasonal_labor);
            }
            // Translate string No Paid Work to English
            if (context.getString(R.string.no_paid_work).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_paid_work);
            }
            // Translate string Pension to English
            if (context.getString(R.string.pension).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.pension);
            }
            // Translate string Remittances to English
            if (context.getString(R.string.remittances_checkbox).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.remittances_checkbox);
            }
            // Translate string Other Source Of Income to English
            if (context.getString(R.string.other_source_of_income_please_specify).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.other_source_of_income_please_specify);
            }
            // Translate string Yes to English
            if (context.getString(R.string.survey_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.survey_yes);
            }
            // Translate string No to English
            if (context.getString(R.string.survey_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.survey_no);
            }
            // Translate string Bigha to English
            if (context.getString(R.string.bigha).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.bigha);
            }
            // Translate string Gunta to English
            if (context.getString(R.string.gunta).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.gunta);
            }
            // Translate string Acre to English
            if (context.getString(R.string.acre).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.acre);
            }
            // Translate string Hectare to English
            if (context.getString(R.string.hectare).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.hectare);
            }
            // Translate string More Than 10 to English
            if (context.getString(R.string.more_than_10).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_10);
            }
            // Translate string More Than 2,50,000 to English
            if (context.getString(R.string.more_than_two_lakh_fifty_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_two_lakh_fifty_thousand);
            }
            // Translate string More Than 25,000 to English
            if (context.getString(R.string.more_than_twenty_five_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_twenty_five_thousand);
            }
            // Translate string Yes, Card Seen to English
            if (context.getString(R.string.yes_card_seen).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.yes_card_seen);
            }
            // Translate string Yes, Card Not Seen to English
            if (context.getString(R.string.yes_card_not_seen).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.yes_card_not_seen);
            }
            // Translate string No Card to English
            if (context.getString(R.string.no_card).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_card);
            }
            // Translate string Do Not Know to English
            if (context.getString(R.string.do_not_know).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.do_not_know);
            }
            // Translate string Electricity to English
            if (context.getString(R.string.electricity).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.electricity);
            }
            // Translate string LPG or Natural Gas to English
            if (context.getString(R.string.lpg_natural_gas).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.lpg_natural_gas);
            }
            // Translate string Biogas to English
            if (context.getString(R.string.biogas_checkbox).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.biogas_checkbox);
            }
            // Translate string Kerosene to English
            if (context.getString(R.string.kerosene).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.kerosene);
            }
            // Translate string Coal or Lignite to English
            if (context.getString(R.string.coal_lignite).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.coal_lignite);
            }
            // Translate string Wood to English
            if (context.getString(R.string.wood).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.wood);
            }
            // Translate string Charcoal to English
            if (context.getString(R.string.charcoal).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.charcoal);
            }
            // Translate string Straw or Shrubs or Grass to English
            if (context.getString(R.string.straw_shrubs_grass).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.straw_shrubs_grass);
            }
            // Translate string Agricultural Crop Waste to English
            if (context.getString(R.string.agricultural_crop_waste).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.agricultural_crop_waste);
            }
            // Translate string Dung Cakes to English
            if (context.getString(R.string.dung_cakes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.dung_cakes);
            }
            // Translate string Other Specify to English
            if (context.getString(R.string.other_specify).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.other_specify);
            }
            // Translate string Lantern to English
            if (context.getString(R.string.lantern).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.lantern);
            }
            // Translate string Kerosene Lamp to English
            if (context.getString(R.string.kerosene_lamp).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.kerosene_lamp);
            }
            // Translate string Candle to English
            if (context.getString(R.string.candle).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.candle);
            }
            // Translate string Electric to English
            if (context.getString(R.string.electric).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.electric);
            }
            // Translate string LPG to English
            if (context.getString(R.string.lpg).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.lpg);
            }
            // Translate string Solar Energy to English
            if (context.getString(R.string.solar_energy).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.solar_energy);
            }
            // Translate string None to English
            if (context.getString(R.string.none).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.none);
            }
            // Translate string Piped Into Dwelling to English
            if (context.getString(R.string.piped_into_dwelling).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.piped_into_dwelling);
            }
            // Translate string Piped Into Yard Plot to English
            if (context.getString(R.string.piped_into_yard_plot).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.piped_into_yard_plot);
            }
            // Translate string Public Tab or Standpipe to English
            if (context.getString(R.string.public_tap_standpipe).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.public_tap_standpipe);
            }
            // Translate string Tube Well or Borehole to English
            if (context.getString(R.string.tube_well_borehole).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.tube_well_borehole);
            }
            // Translate string Protected Well to English
            if (context.getString(R.string.protected_well_checkbox).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.protected_well_checkbox);
            }
            // Translate string Unprotected Well to English
            if (context.getString(R.string.unprotected_well).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.unprotected_well);
            }
            // Translate string Protected Spring to English
            if (context.getString(R.string.protected_spring).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.protected_spring);
            }
            // Translate string Unprotected Spring to English
            if (context.getString(R.string.unprotected_spring).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.unprotected_spring);
            }
            // Translate string Rainwater to English
            if (context.getString(R.string.rainwater).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.rainwater);
            }
            // Translate string Tanker Truck to English
            if (context.getString(R.string.tanker_truck).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.tanker_truck);
            }
            // Translate string Cart With Small Truck to English
            if (context.getString(R.string.cart_with_small_tank).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.cart_with_small_tank);
            }
            // Translate string Surface Water to English
            if (context.getString(R.string.surface_water).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.surface_water);
            }
            // Translate string Common Hand Pump to English
            if (context.getString(R.string.common_hand_pump).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.common_hand_pump);
            }
            // Translate string Hand Pump At Home to English
            if (context.getString(R.string.hand_pump_at_home).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.hand_pump_at_home);
            }
            // Translate string Boil to English
            if (context.getString(R.string.boil).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.boil);
            }
            // Translate string Use Alum to English
            if (context.getString(R.string.use_alum).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.use_alum);
            }
            // Translate string Add Bleach or Chlorine Tablets or Drops to English
            if (context.getString(R.string.add_bleach_chlorine_tablets_drops).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.add_bleach_chlorine_tablets_drops);
            }
            // Translate string Strain Through A Cloth to English
            if (context.getString(R.string.strain_through_a_cloth).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.strain_through_a_cloth);
            }
            // Translate string Use Water Filter to English
            if (context.getString(R.string.use_water_filter_ceramic_sand_composite_etc).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.use_water_filter_ceramic_sand_composite_etc);
            }
            // Translate string Use Electronic Purifier to English
            if (context.getString(R.string.use_electronic_purifier).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.use_electronic_purifier);
            }
            // Translate string Let It Stand And Settle to English
            if (context.getString(R.string.let_it_stand_and_settle).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.let_it_stand_and_settle);
            }
            // Translate string Flush To Piped Sewer System to English
            if (context.getString(R.string.flush_to_piped_sewer_system).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.flush_to_piped_sewer_system);
            }
            // Translate string Flush To Septic Tank to English
            if (context.getString(R.string.flush_to_septic_tank).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.flush_to_septic_tank);
            }
            // Translate string Flush To Pit Latrine to English
            if (context.getString(R.string.flush_to_pit_latrine).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.flush_to_pit_latrine);
            }
            // Translate string Flush To Somewhere Else to English
            if (context.getString(R.string.flush_to_somewhere_else).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.flush_to_somewhere_else);
            }
            // Translate string Flush Don't Know Where to English
            if (context.getString(R.string.flush_dont_know_where).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.flush_dont_know_where);
            }
            // Translate string Ventilated Improved Pit (VIP) or Biogas Latrine to English
            if (context.getString(R.string.ventilated_improved_pit_biogas_latrine).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.ventilated_improved_pit_biogas_latrine);
            }
            // Translate string Pit Latrine With Slab to English
            if (context.getString(R.string.pit_latrine_with_slab).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.pit_latrine_with_slab);
            }
            // Translate string Pit Latrine Without Slab to English
            if (context.getString(R.string.pit_latrine_without_slab_open_pit).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.pit_latrine_without_slab_open_pit);
            }
            // Translate string Twin Pit Composting Toilet to English
            if (context.getString(R.string.twin_pit_composting_toilet).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.twin_pit_composting_toilet);
            }
            // Translate string Dry Toilet to English
            if (context.getString(R.string.dry_toilet).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.dry_toilet);
            }
            // Translate string No Facility Uses Open Space or Field to English
            if (context.getString(R.string.no_facility_uses_open_space_or_field).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_facility_uses_open_space_or_field);
            }
            // Translate string Pit Latrine With Slab to English
            if (context.getString(R.string.pit_latrine_with_slab).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.pit_latrine_with_slab);
            }
            // Translate string No Water Available In The Toilet to English
            if (context.getString(R.string.no_water_available_in_the_toilet).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_water_available_in_the_toilet);
            }
            // Translate string No Electricity Available In The Toilet to English
            if (context.getString(R.string.no_electricity_available_in_the_toilet).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_electricity_available_in_the_toilet);
            }
            // Translate string Toilet Is Occupied to English
            if (context.getString(R.string.toilet_is_often_occupied_by_other_members).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.toilet_is_often_occupied_by_other_members);
            }
            // Translate string Problem In Toilet Plumbing to English
            if (context.getString(R.string.problem_in_toilet_plumbing).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.problem_in_toilet_plumbing);
            }
            // Translate string Other Reasons (Specify) to English
            if (context.getString(R.string.other_reasons_specify).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.other_reasons_specify);
            }
            // Translate string No Soap Available In The Household to English
            if (context.getString(R.string.no_soap_available_in_the_household).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_soap_available_in_the_household);
            }
            // Translate string Soap Available But Did Not Use It To English
            if (context.getString(R.string.soap_available_in_the_household_but_did_not_use_it_to_wash_hands).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.soap_available_in_the_household_but_did_not_use_it_to_wash_hands);
            }
            // Translate string Before Cooking to English
            if (context.getString(R.string.before_cooking).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.before_cooking);
            }
            // Translate string After Handling Food to English
            if (context.getString(R.string.after_handling_food).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.after_handling_food);
            }
            // Translate string Before Eating to English
            if (context.getString(R.string.before_eating).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.before_eating);
            }
            // Translate string After Using Toilet to English
            if (context.getString(R.string.after_using_toilet).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.after_using_toilet);
            }
            // Translate string Starch Staple Food to English
            if (context.getString(R.string.starch_staple_food).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.starch_staple_food);
            }
            // Translate string Beans And Peas to English
            if (context.getString(R.string.beans_and_peas).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.beans_and_peas);
            }
            // Translate string Nuts And Cheese to English
            if (context.getString(R.string.nuts_and_cheese).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.nuts_and_cheese);
            }
            // Translate string Dairy to English
            if (context.getString(R.string.dairy).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.dairy);
            }
            // Translate string Eggs to English
            if (context.getString(R.string.eggs).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.eggs);
            }
            // Translate string Flesh Food to English
            if (context.getString(R.string.flesh_food).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.flesh_food);
            }
            // Translate string Any Vegetables to English
            if (context.getString(R.string.any_vegetables).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.any_vegetables);
            }
        }
        return text;
    }

    public static String getOtherStringEdit(String text) {
        String result = "-";
        int colonIndex = text.lastIndexOf(":");
        int quoteIndex = text.lastIndexOf("\"");
        result = text.substring(colonIndex + 1, quoteIndex);
        return result.trim();
    }

    public static String getOccupationString(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Salaried Government Job to English
            if (context.getString(R.string.salaried_government_job).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.salaried_government_job);
            }
            // Translate string Salaried Private Job to English
            if (context.getString(R.string.salaried_private_job).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.salaried_private_job);
            }
            // Translate string Technician or Craftsman or Other Skilled Work to English
            if (context.getString(R.string.technician_craftsman_other_skilled_work).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.technician_craftsman_other_skilled_work);
            }
            // Translate string Agricultural Farmer to English
            if (context.getString(R.string.agricultural_farmer).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.agricultural_farmer);
            }
            // Translate string Tenant Farmer to English
            if (context.getString(R.string.tenant_farmer_agricultural_daily_wage_worker).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.tenant_farmer_agricultural_daily_wage_worker);
            }
            // Translate string Daily Wage Worker to English
            if (context.getString(R.string.daily_wage_worker_unskilled_workers).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.daily_wage_worker_unskilled_workers);
            }
            // Translate string Household Work to English
            if (context.getString(R.string.household_work).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.household_work);
            }
            // Translate string Student to English
            if (context.getString(R.string.student).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.student);
            }
            // Translate string Unemployed to English
            if (context.getString(R.string.unemployed).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.unemployed);
            }
            // Translate string Retired With Pension to English
            if (context.getString(R.string.retired_with_pension).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.retired_with_pension);
            }
            // Translate string Retired Without Pension to English
            if (context.getString(R.string.retired_without_pension).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.retired_without_pension);
            }
        }
        return text;
    }

    public static String getMobilePhoneOwnership(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string None to English
            if (context.getString(R.string.none).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.none);
            }
            // Translate string Shared to English
            if (context.getString(R.string.shared).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.shared);
            }
            // Translate string Owns Smartphone to English
            if (context.getString(R.string.owns_smartphone).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.owns_smartphone);
            }
            // Translate string Owns Feature Phone to English
            if (context.getString(R.string.owns_feature_phone).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.owns_feature_phone);
            }
        }
        return text;
    }

    public static String getMaritalStatusStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Currently Married to English
            if (context.getString(R.string.currently_married).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.currently_married);
            }
            // Translate string Married, Gauna Not Performed to English
            if (context.getString(R.string.married_gauna_not_performed).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.married_gauna_not_performed);
            }
            // Translate string Widowed to English
            if (context.getString(R.string.widowed).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.widowed);
            }
            // Translate string Divorced to English
            if (context.getString(R.string.divorced).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.divorced);
            }
            // Translate string Separated to English
            if (context.getString(R.string.separated).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.separated);
            }
            // Translate string Deserted to English
            if (context.getString(R.string.deserted).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.deserted);
            }
            // Translate string Never Married to English
            if (context.getString(R.string.never_married).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.never_married);
            }
        }
        return text;
    }

    // Strings for BP Checked, HB Checked, Sugar Checked, and BMI Checked
    public static String getTestStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Never Checked to English
            if (context.getString(R.string.never_checked).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.never_checked);
            }
            // Translate string In Past One Week to English
            if (context.getString(R.string.in_past_one_week).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.in_past_one_week);
            }
            // Translate string Within Last Month to English
            if (context.getString(R.string.within_last_month).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.within_last_month);
            }
            // Translate string Between One Month To Three Months Ago to English
            if (context.getString(R.string.between_one_month_to_three_months_ago).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.between_one_month_to_three_months_ago);
            }
            // Translate string Between Three Months To Six Months Ago to English
            if (context.getString(R.string.between_three_months_to_six_months_ago).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.between_three_months_to_six_months_ago);
            }
            // Translate string More Than Six Months to English
            if (context.getString(R.string.more_than_six_months).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_six_months);
            }
        }
        return text;
    }

    public static String getEducationStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Cannot Read and Write to English
            if (context.getString(R.string.cannot_read_and_write).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.cannot_read_and_write);
            }
            // Translate string Can Read and Write to English
            if (context.getString(R.string.can_read_and_write).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.can_read_and_write);
            }
            // Translate string Primary Education Till Class 5 to English
            if (context.getString(R.string.primary_education_till_class_five).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.primary_education_till_class_five);
            }
            // Translate string Middle School to English
            if (context.getString(R.string.middle_school_sixth_eighth).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.middle_school_sixth_eighth);
            }
            // Translate string Passed Secondary to English
            if (context.getString(R.string.passed_secondary_school).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.passed_secondary_school);
            }
            // Translate string Passed Senior Secondary to English
            if (context.getString(R.string.passed_senior_secondary_school).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.passed_senior_secondary_school);
            }
            // Translate string Graduate to English
            if (context.getString(R.string.graduate).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.graduate);
            }
            // Translate string Postgraduate to English
            if (context.getString(R.string.postgraduate).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.postgraduate);
            }
        }
        return text;
    }

    public static String hohRelationship(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Spouse to English
            if (context.getString(R.string.spouse).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.spouse);
            }
            // Translate string Son or Daughter to English
            if (context.getString(R.string.son_daughter).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.son_daughter);
            }
            // Translate string Son or Daughter-In-Law to English
            if (context.getString(R.string.son_daughter_in_law).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.son_daughter_in_law);
            }
            // Translate string Grandchild to English
            if (context.getString(R.string.grandchild).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.grandchild);
            }
            // Translate string Father or Mother to English
            if (context.getString(R.string.father_mother).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.father_mother);
            }
            // Translate string Father or Mother-In-Law to English
            if (context.getString(R.string.father_mother_in_law).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.father_mother_in_law);
            }
            // Translate string Brother or Sister to English
            if (context.getString(R.string.brother_sister).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.brother_sister);
            }
            // Translate string Brother or Sister-In-Law to English
            if (context.getString(R.string.brother_sister_in_law).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.brother_sister_in_law);
            }
            // Translate string Niece or Nephew to English
            if (context.getString(R.string.niece_nephew).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.niece_nephew);
            }
            // Translate string Grandparent or Grandparent-In-Law to English
            if (context.getString(R.string.grandparent_grandparent_in_law).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.grandparent_grandparent_in_law);
            }
            // Translate string Other Relative to English
            if (context.getString(R.string.other_relative).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.other_relative);
            }
            // Translate string Adopted Foster Stepchild to English
            if (context.getString(R.string.adopted_foster_stepchild).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.adopted_foster_stepchild);
            }
            // Translate string Domestic Servant to English
            if (context.getString(R.string.domestic_servant).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.domestic_servant);
            }
            // Translate string Other (Specify) to English
            if (context.getString(R.string.other_specify).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.other_specify);
            }
            // Translate string Not Stated to English
            if (context.getString(R.string.not_stated).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.not_stated);
            }
        }
        return text;
    }

    public static String getMedicalHistoryStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes to English
            if (context.getString(R.string.yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.yes);
            }

            // Translate string No to English
            if (context.getString(R.string.no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no);
            }
        }
        return text;
    }

    public static String getSmokingHistoryStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Smoker to English
            if (context.getString(R.string.smoker).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.smoker);
            }

            // Translate string Non-Smoker to English
            if (context.getString(R.string.non_smoker).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.non_smoker);
            }

            // Translate string 5 - 10 bids / cigarette to English
            if (context.getString(R.string.five_ten_bids_cigarette).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.five_ten_bids_cigarette);
            }

            // Translate string More Than 10 bids / cigarette to English
            if (context.getString(R.string.more_than_ten_bids_cigarette).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_ten_bids_cigarette);
            }

            // Translate string Less Than A Year to English
            if (context.getString(R.string.less_than_a_year).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.less_than_a_year);
            }

            // Translate string From One Year To Five Years to English
            if (context.getString(R.string.from_one_year_to_five_year).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.from_one_year_to_five_year);
            }

            // Translate string From Five Years To Ten Years to English
            if (context.getString(R.string.from_five_years_to_ten_years).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.from_five_years_to_ten_years);
            }

            // Translate string More Than 10 Years to English
            if (context.getString(R.string.more_than_ten_years).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_ten_years);
            }
        }

        return text;
    }
}
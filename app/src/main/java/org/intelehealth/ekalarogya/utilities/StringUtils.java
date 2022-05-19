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
import android.widget.Spinner;

import java.io.File;
import java.util.List;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;

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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_caste(val);
            val = switch_or_economic(val);
            val = switch_or_education(val);
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            val = switch_gu_caste(val);
            val = switch_gu_economic(val);
            val = switch_gu_education(val);
        }

        return val;
    }

    public static String getCheckbox_Hi_En_Or_Gu(String checkbox_text) {
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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            switch (checkbox_text) {
                case "ଉତ୍ତର ଦେବାକୁ ମନା କରିଦେଲେ":
                    val = "Declined to answer";
                    break;
                default:
                    return val;
            }
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            switch (checkbox_text) {
                case "જવાબ આપવાનો ઇનકાર કર્યો"://----replace with gujrati
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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_en_vaccination(val);
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            val = switch_gu_en_vaccination(val);
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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_en_occupation(val);
            val = switch_or_en_bankaccount(val);
            val = switch_or_en_mobile(val);
            val = switch_or_en_whatsapp(val);
            val = switch_or_en_sourcewater(val);
            val = switch_or_en_watersafe(val);
            val = switch_or_en_wateravail(val);
            val = switch_or_en_toiletfacil(val);
            val = switch_or_en_housestructure(val);
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            val = switch_gu_en_occupation(val);
            val = switch_gu_en_bankaccount(val);
            val = switch_gu_en_mobile(val);
            val = switch_gu_en_whatsapp(val);
            val = switch_gu_en_sourcewater(val);
            val = switch_gu_en_watersafe(val);
            val = switch_gu_en_wateravail(val);
            val = switch_gu_en_toiletfacil(val);
            val = switch_gu_en_housestructure(val);
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

    public static String switch_gu_housestructure_edit(String val) {
        switch (val) {
            case "Kutcha House":
                val = "કચ્છ હાઉસ";//-------replace with gujrati
                break;
            case "Pakka House":
                val = "પક્કા હાઉસ";
                break;
            case "Homeless":
                val = "બેઘર";
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

    public static String switch_gu_en_housestructure(String val) {
        switch (val) {
            case "કચ્છ હાઉસ"://---------replace with gujrati
                val = "Kutcha House";
                break;
            case "પક્કા હાઉસ":
                val = "Pakka House";
                break;
            case "બેઘર":
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
                val = "ସୁବିଧା ନାହିଁ / ଖୋଲା ସ୍ଥାନ ବା ଖେତ ର ବ୍ୟବହାର";
                break;
            case "Own toilet":
                val = "ନିଜର ଶୌଚାଳୟ";
                break;
            case "Community toilet":
                val = "ଗୋଷ୍ଠୀ ଶୌଚାଳୟ";
                break;
            case "Shared toilet with other household":
                val = "ଅନ୍ୟ ପଡିଶା ଘର ଶୌଚାଳୟ ବ୍ୟବହାର";
                break;
            case "Other [Enter]":
                val = "ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_toiletfacil_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";//-------replace with gujrati
                break;
            case "No facility /uses open space or field":
                val = "કોઈ સુવિધા / ખુલ્લી જગ્યા અથવા ક્ષેત્રનો ઉપયોગ કરતું નથી";
                break;
            case "Own toilet":
                val = "પોતાનું શૌચાલય";
                break;
            case "Community toilet":
                val = "સામુદાયિક શૌચાલય";
                break;
            case "Shared toilet with other household":
                val = "અન્ય ઘર સાથે વહેંચાયેલ શૌચાલય";
                break;
            case "Other [Enter]":
                val = "અન્ય [દાખલ કરો]";
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
            case "ସୁବିଧା ନାହିଁ / ଖୋଲା ସ୍ଥାନ ବା ଖେତ ର ବ୍ୟବହାର":
                val = "No facility /uses open space or field";
                break;
            case "ନିଜର ଶୌଚାଳୟ":
                val = "Own toilet";
                break;
            case "ଗୋଷ୍ଠୀ ଶୌଚାଳୟ":
                val = "Community toilet";
                break;
            case "ଅନ୍ୟ ପଡିଶା ଘର ଶୌଚାଳୟ ବ୍ୟବହାର":
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

    public static String switch_gu_en_toiletfacil(String val) {
        switch (val) {
            case "જવાબ આપવાનો ઇનકાર કર્યો"://-------replace with gujrati
                val = "Declined to answer";
                break;
            case "કોઈ સુવિધા / ખુલ્લી જગ્યા અથવા ક્ષેત્રનો ઉપયોગ કરતું નથી":
                val = "No facility /uses open space or field";
                break;
            case "પોતાનું શૌચાલય":
                val = "Own toilet";
                break;
            case "સામુદાયિક શૌચાલય":
                val = "Community toilet";
                break;
            case "અન્ય ઘર સાથે વહેંચાયેલ શૌચાલય":
                val = "Shared toilet with other household";
                break;
            case "અન્ય [દાખલ કરો]":
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

    public static String switch_gu_en_wateravail(String val) {
        switch (val) {
            case "જવાબ આપવાનો ઇનકાર કર્યો"://-------replace with gujrati
                val = "Declined to answer";
                break;
            case "હા":
                val = "Yes";
                break;
            case "ના":
                val = "No";
                break;
            case "ખબર નથી":
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

    public static String switch_gu_en_watersafe(String val) {
        switch (val) {
            case "જવાબ આપવાનો ઇનકાર કર્યો"://----replace with gujrati
                val = "Declined to answer";
                break;
            case "કંઈ નહીં":
                val = "Nothing";
                break;
            case "ઉકાળો":
                val = "Boil";
                break;
            case "ફટકડી":
                val = "Alum";
                break;
            case "બ્લીચ/ક્લોરીનની ગોળીઓ ઉમેરો":
                val = "Add Bleach/Chlorine tablets";
                break;
            case "કાપડ દ્વારા તાણ":
                val = "Strain through cloth";
                break;
            case "વોટર ફિલ્ટર (સિરામિક/રેતી/કમ્પોઝિટ) વગેરેનો ઉપયોગ કરો":
                val = "Use water filter(ceramic/sand/composite)etc";
                break;
            case "ઇલેક્ટ્રોનિક ફિલ્ટરનો ઉપયોગ કરો":
                val = "Use electronic filter";
                break;
            case "અન્ય[દાખલ કરો]":
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

    public static String switch_gu_en_sourcewater(String val) {
        switch (val) {
            case "જવાબ આપવાનો ઇનકાર કર્યો"://-------replace with gujrati
                val = "Declined to answer";
                break;
            case "ચપકલ/હેન્ડ પંપ":
                val = "Chapakal/Hand Pump";
                break;
            case "કુવાઓ":
                val = "Wells";
                break;
            case "કંટાળાજનક":
                val = "Boring";
                break;
            case "નદીઓ/તળાવ":
                val = "Rivers/ponds";
                break;
            case "ટેન્કરનું પાણી":
                val = "Tanker water";
                break;
            case "કોઇ પણ બીજુ":
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
            case "ଦିନ ମଜୁରିଆ":
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
            case "ଅନ୍ୟ କୌଶଳ/ତାଲିମ ପାଇଥିବା (ଡ଼୍ରାଇଭର , ମିସ୍ତ୍ରୀ ଇତ୍ୟାଦି)":
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

    public static String switch_gu_en_occupation(String val) {
        switch (val) {
            case "જવાબ આપવાનો ઇનકાર કર્યો"://----------replace with gujrati
                val = "Declined to answer";
                break;
            case "સરકારી નોકરી":
                val = "Government job";
                break;
            case "મોટા પાયેથી મધ્યમ પાયાનો ઉદ્યોગ":
                val = "Large scale to medium scale industry";
                break;
            case "ખાનગી ક્ષેત્રમાં વ્યવસાયિક નોકરી":
                val = "Professional job in private sector";
                break;
            case "નાના પાયે ઉદ્યોગ":
                val = "Small scale industry";
                break;
            case "મોટા દુકાન માલિક":
                val = "Big shop owner";
                break;
            case "ટેકનિશિયન/કારીગર":
                val = "Technician/craftsman";
                break;
            case "નાની દુકાનનો માલિક":
                val = "Small shop owner";
                break;
            case "મોટા પાયે ખેડૂત":
                val = "Large scale farmer";
                break;
            case "દૈનિક વેતન મેળવનાર":
                val = "Daily wage earner";
                break;
            case "નાના પાયે ખેડૂત/ખેત કામદાર":
                val = "Small scale farmer/farm worker";
                break;
            case "બેરોજગાર":
                val = "Unemployed";
                break;
            case "ગૃહિણી":
                val = "Housewife";
                break;
            case "વિદ્યાર્થી":
                val = "Student";
                break;
            case "5 વર્ષથી ઓછી ઉંમરના બાળક":
                val = "Under 5 child";
                break;
            case "અન્ય કુશળતા (ડ્રાઈવર, મેસન વગેરે)":
                val = "Other skills (driver,mason etc)";
                break;
            case "[વર્ણન કરો]":
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

    public static String switch_gu_en_bankaccount(String val) {
        switch (val) {
            case "જવાબ આપવાનો ઇનકાર કર્યો"://--------replace with gujrati
                val = "Declined to answer";
                break;
            case "હા":
                val = "Yes";
                break;
            case "ના":
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
            case "ଛୋଟ ଫୋନ":
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

    public static String switch_gu_en_mobile(String val) {
        switch (val) {
            case "મૂળભૂત ફોન":///-------replace with gujrati
                val = "Basic Phone";
                break;
            case "સ્માર્ટફોન":
                val = "Smartphone";
                break;
            case "પોતાની પાસે મોબાઈલ ફોન નથી":
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

    public static String switch_gu_en_whatsapp(String val) {
        switch (val) {
            case "હા"://------replace with gujrati
                val = "Yes";
                break;
            case "ના":
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

    public static String switch_gu_education(String val) {
        switch (val) {
            case "અભણ"://------replace with gujrati
                val = "Illiterate";
                break;
            case "પ્રાથમિક":
                val = "Primary";
                break;
            case "ગૌણ":
                val = "Secondary";
                break;
            case "ઉચ્ચતર માધ્યમિક":
                val = "Higher Secondary";
                break;
            case "સ્નાતક અને ઉચ્ચ":
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
            case "ଏପିଏଲ"://------replace with odiya
                val = "APL";
                break;
            case "ବିପିଏଲ":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_economic(String val) {
        switch (val) {
            case "એપીએલ"://------replace with gujrati
                val = "APL";
                break;
            case "બીપીએલ":
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

    public static String switch_gu_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "અભણ";//-------replace with gujrati
                break;
            case "Primary":
                val = "પ્રાથમિક";
                break;
            case "Secondary":
                val = "ગૌણ";
                break;
            case "Higher Secondary":
                val = "ઉચ્ચતર માધ્યમિક";
                break;
            case "Graduation & Higher":
                val = "સ્નાતક અને ઉચ્ચ";
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
                val = "ଏପିଏଲ";//------replace with odiya
                break;
            case "BPL":
                val = "ବିପିଏଲ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "એપીએલ";//------replace with gujrati
                break;
            case "BPL":
                val = "બીપીએલ";
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

    //---------change condition in gujrati--------
    public static String switch_gu_caste(String val) {
        switch (val) {
            case "જનરલ"://-------replace with Gujrati
                val = "General";
                break;
            case "ઓબીસી":
                val = "OBC";
                break;
            case "એસસી":
                val = "SC";
                break;
            case "એસ.ટી":
                val = "ST";
                break;
            case "અન્ય":
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

    public static String switch_gu_bankaccount_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";//-------replace with gujrati
                break;
            case "Yes":
                val = "હા";
                break;
            case "No":
                val = "ના";
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
                val = "ଛୋଟ ଫୋନ";
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

    public static String switch_gu_mobiletype_edit(String val) {
        switch (val) {
            case "Basic Phone":
                val = "મૂળભૂત ફોન";//--------replace with gujrati
                break;
            case "Smartphone":
                val = "સ્માર્ટફોન";
                break;
            case "Does not own mobile phone":
                val = "પોતાની પાસે મોબાઈલ ફોન નથી";
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

    public static String switch_gu_watersource_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";//------replace with gujrati
                break;
            case "Chapakal/Hand Pump":
                val = "ચપકલ/હેન્ડ પંપ";
                break;
            case "Wells":
                val = "કુવાઓ";
                break;
            case "Boring":
                val = "કંટાળાજનક";
                break;
            case "Rivers/ponds":
                val = "નદીઓ/તળાવ";
                break;
            case "Tanker water":
                val = "ટેન્કરનું પાણી";
                break;
            case "Any other":
                val = "કોઇ પણ બીજુ";
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

    public static String switch_gu_wateravail_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";//-------replace with gujrati
                break;
            case "Yes":
                val = "હા";
                break;
            case "No":
                val = "ના";
                break;
            case "Don\'t know":
                val = "ખબર નથી";
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

    public static String switch_gu_watersafe_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";//------replace with gujrati
                break;
            case "Nothing":
                val = "કંઈ નહીં";
                break;
            case "Boil":
                val = "ઉકાળો";
                break;
            case "Alum":
                val = "ફટકડી";
                break;
            case "Add Bleach/Chlorine tablets":
                val = "બ્લીચ/ક્લોરીનની ગોળીઓ ઉમેરો";
                break;
            case "Strain through cloth":
                val = "કાપડ દ્વારા તાણ";
                break;
            case "Use water filter(ceramic/sand/composite)etc":
                val = "વોટર ફિલ્ટર (સિરામિક/રેતી/કમ્પોઝિટ) વગેરેનો ઉપયોગ કરો";
                break;
            case "Use electronic filter":
                val = "ઇલેક્ટ્રોનિક ફિલ્ટરનો ઉપયોગ કરો";
                break;
            case "Other[Enter]":
                val = "અન્ય[દાખલ કરો]";
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

    public static String switch_gu_en_vaccination(String val) {
        switch (val) {
            case "પ્રથમ ડોઝ"://-----replace with gujrati
                val = "First dose";
                break;
            case "બીજી માત્રા":
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

    public static String switch_gu_vaccination_edit(String val) {
        switch (val) {
            case "First dose":
                val = "પ્રથમ ડોઝ";//-----replace with gujrati
                break;
            case "Second dose":
                val = "બીજી માત્રા";
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

    public static String switch_gu_whatsapp_edit(String val) {
        switch (val) {
            case "Yes":
                val = "હા";//--------replace with gujrati
                break;
            case "No":
                val = "ના";
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
                val = "ଦିନ ମଜୁରିଆ";
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
                val = "ଅନ୍ୟ କୌଶଳ/ତାଲିମ ପାଇଥିବା (ଡ଼୍ରାଇଭର , ମିସ୍ତ୍ରୀ ଇତ୍ୟାଦି)";
                break;
            case "[Describe]":
                val = "[ବର୍ଣ୍ଣନା କର]";
                break;
            default:
                return val;
        }
        return val;

    }

    public static String switch_gu_occupation_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";//----------replace with gujrati
                break;
            case "Government job":
                val = "સરકારી નોકરી";
                break;
            case "Large scale to medium scale industry":
                val = "મોટા પાયેથી મધ્યમ પાયાનો ઉદ્યોગ";
                break;
            case "Professional job in private sector":
                val = "ખાનગી ક્ષેત્રમાં વ્યવસાયિક નોકરી";
                break;
            case "Small scale industry":
                val = "નાના પાયે ઉદ્યોગ";
                break;
            case "Big shop owner":
                val = "મોટા દુકાન માલિક";
                break;
            case "Technician/craftsman":
                val = "ટેકનિશિયન/કારીગર";
                break;
            case "Small shop owner":
                val = "નાની દુકાનનો માલિક";
                break;
            case "Large scale farmer":
                val = "મોટા પાયે ખેડૂત";
                break;
            case "Daily wage earner":
                val = "દૈનિક વેતન મેળવનાર";
                break;
            case "Small scale farmer/farm worker":
                val = "નાના પાયે ખેડૂત/ખેત કામદાર";
                break;
            case "Unemployed":
                val = "બેરોજગાર";
                break;
            case "Housewife":
                val = "ગૃહિણી";
                break;
            case "Student":
                val = "વિદ્યાર્થી";
                break;
            case "Under 5 child":
                val = "5 વર્ષથી ઓછી ઉંમરના બાળક";
                break;
            case "Other skills (driver,mason etc)":
                val = "અન્ય કુશળતા (ડ્રાઈવર, મેસન વગેરે)";
                break;
            case "[Describe]":
                val = "[વર્ણન કરો]";
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
                val = "જનરલ";//-----replace with odiya
                break;
            case "OBC":
                val = "ઓબીસી";
                break;
            case "SC":
                val = "એસસી";
                break;
            case "ST":
                val = "એસ.ટી";
                break;
            case "others":
                val = "અન્ય";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "જનરલ";//-----replace with gujrati
                break;
            case "OBC":
                val = "ઓબીસી";
                break;
            case "SC":
                val = "એસસી";
                break;
            case "ST":
                val = "એસ.ટી";
                break;
            case "others":
                val = "અન્ય";
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

    public static String hi_or_gu_en_month(int month_index) {
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
                .replace("April", "ଏପ୍ରିଲ୍")//ଅପ୍ରେଲ
                .replace("May", "ମେ")//ମଇ
                .replace("June", "ଜୁନ୍")
                .replace("July", "ଜୁଲାଇ")
                .replace("August", "ଅଗଷ୍ଟ")
                .replace("September", "ସେପ୍ଟେମ୍ବର")
                .replace("October", "ଅକ୍ଟୋବର")
                .replace("November", "ନଭେମ୍ବର")
                .replace("December", "ଡିସେମ୍ବର");

        return mdob_text;
    }

    public static String en__gu_dob(String dob) { //English dob is replaced to Odiya text.
        String mdob_text = dob
                .replace("January", "જાન્યુઆરી")//replace with gujrati---replacement value
                .replace("February", "ફેબ્રુઆરી")
                .replace("March", "કુચ")
                .replace("April", "એપ્રિલ")
                .replace("May", "મે")
                .replace("June", "જૂન")
                .replace("July", "જુલાઈ")
                .replace("August", "ઓગસ્ટ")
                .replace("September", "સપ્ટેમ્બર")
                .replace("October", "ઓક્ટોબર")
                .replace("November", "નવેમ્બર")
                .replace("December", "ડિસેમ્બર");

        return mdob_text;
    }

    public static String hi_or_gu_en_noEdit(String dobString, String locale) {

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
                    .replace("ଏପ୍ରିଲ୍", "April")//ଅପ୍ରେଲ
                    .replace("ମେ", "May")//ମଇ
                    .replace("ଜୁନ୍", "June")
                    .replace("ଜୁଲାଇ", "July")
                    .replace("ଅଗଷ୍ଟ", "August")
                    .replace("ସେପ୍ଟେମ୍ବର", "September")
                    .replace("ଅକ୍ଟୋବର", "October")
                    .replace("ନଭେମ୍ବର", "November")
                    .replace("ଡିସେମ୍ବର", "December");
            return dob;
        }else if(locale.equalsIgnoreCase("gu")) {
            //replace with gujrati
            String dob = dobString
                    .replace("જાન્યુઆરી", "January")//-----------replace with gujrati---target value
                    .replace("ફેબ્રુઆરી", "February")
                    .replace("કુચ", "March")
                    .replace("એપ્રિલ", "April")
                    .replace("મે", "May")
                    .replace("જૂન", "June")
                    .replace("જુલાઈ", "July")
                    .replace("ઓગસ્ટ", "August")
                    .replace("સપ્ટેમ્બર", "September")
                    .replace("ઓક્ટોબર", "October")
                    .replace("નવેમ્બર", "November")
                    .replace("ડિસેમ્બર", "December");
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

        if (locale.equalsIgnoreCase("gu")) {

            switch (val) {
                case "Sunday":
                    val = "રવિવાર";//--------repalce with gujrati---val value
                    break;
                case "Monday":
                    val = "સોમવાર";
                    break;
                case "Tuesday":
                    val = "મંગળવારે";
                    break;
                case "Wednesday":
                    val = "બુધવાર";
                    break;

                case "Thursday":
                    val = "ગુરુવાર";
                    break;
                case "Friday":
                    val = "શુક્રવાર";
                    break;
                case "Saturday":
                    val = "શનિવાર";
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

        } if (locale.equalsIgnoreCase("or")) {

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

        }if (locale.equalsIgnoreCase("gu")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "બુક કરેલ";//------replace with gujrati
                    break;
                case "cancelled":
                    val = "રદ કરેલ";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }
}

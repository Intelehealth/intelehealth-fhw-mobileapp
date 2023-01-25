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
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        if (value != null && !value.equals("") && !value.equals("-"))
            val = trimAdvanced(value);
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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            val = switch_bn_caste(val);
            val = switch_bn_economic(val);
            val = switch_bn_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            val = switch_gu_caste(val);
            val = switch_gu_economic(val);
            val = switch_gu_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            val = switch_as_caste(val);
            val = switch_as_economic(val);
            val = switch_as_education(val);
        }

        return val;
    }

    public static String getCheckbox_Hi_En_Or_Gu_As(String checkbox_text) {
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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            switch (checkbox_text) {
                case "উত্তর দিতে অস্বীকৃতি জানায়":
                    val = "Declined to answer";
                    break;
                default:
                    return val;
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            switch (checkbox_text) {
                case "જવાબ આપવાનો ઇનકાર કર્યો"://----replace with gujrati
                    val = "Declined to answer";
                    break;
                default:
                    return val;
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            switch (checkbox_text) {
                case "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে"://----replace with assamese
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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            val = switch_bn_en_vaccination(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            val = switch_gu_en_vaccination(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            val = switch_as_en_vaccination(val);
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
        }else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            val = switch_bn_en_occupation(val);
            val = switch_bn_en_bankaccount(val);
            val = switch_bn_en_mobile(val);
            val = switch_bn_en_whatsapp(val);
            val = switch_bn_en_sourcewater(val);
            val = switch_bn_en_watersafe(val);
            val = switch_bn_en_wateravail(val);
            val = switch_bn_en_toiletfacil(val);
            val = switch_bn_en_housestructure(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            val = switch_gu_en_occupation(val);
            val = switch_gu_en_bankaccount(val);
            val = switch_gu_en_mobile(val);
            val = switch_gu_en_whatsapp(val);
            val = switch_gu_en_sourcewater(val);
            val = switch_gu_en_watersafe(val);
            val = switch_gu_en_wateravail(val);
            val = switch_gu_en_toiletfacil(val);
            val = switch_gu_en_housestructure(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            val = switch_as_en_occupation(val);
            val = switch_as_en_bankaccount(val);
            val = switch_as_en_mobile(val);
            val = switch_as_en_whatsapp(val);
            val = switch_as_en_sourcewater(val);
            val = switch_as_en_watersafe(val);
            val = switch_as_en_wateravail(val);
            val = switch_as_en_toiletfacil(val);
            val = switch_as_en_housestructure(val);
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
    public static String switch_bn_housestructure_edit(String val) {
        switch (val) {
            case "Kutcha House":
                val = "কাচ্ছা বাড়ি";
                break;
            case "Pakka House":
                val = "পাক্কা বাড়ি";
                break;
            case "Homeless":
                val = "গৃহহীন";
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
                val = "કાચું ઘર";//-------replace with gujrati
                break;
            case "Pakka House":
                val = "પાકું ઘર";
                break;
            case "Homeless":
                val = "બેઘર";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_housestructure_edit(String val) {
        switch (val) {
            case "Kutcha House":
                val = "কেচাঁ ঘৰ";//-------replace with assamese
                break;
            case "Pakka House":
                val = "পকী ঘৰ";
                break;
            case "Homeless":
                val = "গৃহহীন";
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
    public static String switch_bn_en_housestructure(String val) {
        switch (val) {
            case "কাচ্ছা বাড়ি":
                val = "Kutcha House";
                break;
            case "পাক্কা বাড়ি":
                val = "Pakka House";
                break;
            case "গৃহহীন":
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
            case "કાચું ઘર"://---------replace with gujrati
                val = "Kutcha House";
                break;
            case "પાકું ઘર":
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

    public static String switch_as_en_housestructure(String val) {
        switch (val) {
            case "কেচাঁ ঘৰ"://---------replace with assamese
                val = "Kutcha House";
                break;
            case "পকী ঘৰ":
                val = "Pakka House";
                break;
            case "গৃহহীন":
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
    public static String switch_bn_toiletfacil_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "উত্তর দিতে অস্বীকৃতি জানায়";
                break;
            case "No facility /uses open space or field":
                val = "কোন সুবিধা / খোলা জায়গা বা মাঠ ব্যবহার করে না";
                break;
            case "Own toilet":
                val = "নিজস্ব টয়লেট";
                break;
            case "Community toilet":
                val = "কমিউনিটি টয়লেট";
                break;
            case "Shared toilet with other household":
                val = "অন্যান্য পরিবারের সাথে শেয়ার করা টয়লেট";
                break;
            case "Other [Enter]":
                val = "অন্যান্য [এন্টার]";
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

    public static String switch_as_toiletfacil_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে";
                break;
            case "No facility /uses open space or field":
                val = "কোনো সুবিধাই /মুকলি ঠাই বা পথাৰ ব্যৱহাৰ নকৰে";
                break;
            case "Own toilet":
                val = "নিজা শৌচাগাৰ";
                break;
            case "Community toilet":
                val = "কমিউনিটি টয়লেট";
                break;
            case "Shared toilet with other household":
                val = "আন ঘৰৰ সৈতে শ্বেয়াৰ টয়লেট";
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
           /* case "अन्य [दर्ज करें]":
                val = "Other [Enter]";
                break;*/
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_toiletfacil(String val) {
        switch (val) {
            case "উত্তর দিতে অস্বীকৃতি জানায়":
                val = "Declined to answer";
                break;
            case "কোন সুবিধা / খোলা জায়গা বা মাঠ ব্যবহার করে না":
                val = "No facility /uses open space or field";
                break;
            case "নিজস্ব টয়লেট":
                val = "Own toilet";
                break;
            case "কমিউনিটি টয়লেট":
                val = "Community toilet";
                break;
            case "অন্যান্য পরিবারের সাথে শেয়ার করা টয়লেট":
                val = "Shared toilet with other household";
                break;
           /* case "अन्य [दर्ज करें]":
                val = "Other [Enter]";
                break;*/
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
           /* case "ଅନ୍ୟାନ୍ୟ [ଏଣ୍ଟର୍]":
                val = "Other [Enter]";
                break;*/
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
           /* case "અન્ય [દાખલ કરો]":
                val = "Other [Enter]";
                break;*/
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_toiletfacil(String val) {
        switch (val) {
            case "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে"://-------replace with Assamese
                val = "Declined to answer";
                break;
            case "কোনো সুবিধাই /মুকলি ঠাই বা পথাৰ ব্যৱহাৰ নকৰে":
                val = "No facility /uses open space or field";
                break;
            case "নিজা শৌচাগাৰ":
                val = "Own toilet";
                break;
            case "কমিউনিটি টয়লেট":
                val = "Community toilet";
                break;
            case "আন ঘৰৰ সৈতে শ্বেয়াৰ টয়লেট":
                val = "Shared toilet with other household";
                break;
           /* case "અન્ય [દાખલ કરો]":
                val = "Other [Enter]";
                break;*/
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
    public static String switch_bn_en_wateravail(String val) {
        switch (val) {
            case "উত্তর দিতে অস্বীকৃতি জানায়":
                val = "Declined to answer";
                break;
            case "হ্যাঁ":
                val = "Yes";
                break;
            case "না":
                val = "No";
                break;
            case "জানি না":
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
            case "ଜାଣିନାହିଁ / ଜାଣେନାହିଁ":
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

    public static String switch_as_en_wateravail(String val) {
        switch (val) {
            case "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে"://-------replace with gujrati
                val = "Declined to answer";
                break;
            case "হয়":
                val = "Yes";
                break;
            case "নহয়":
                val = "No";
                break;
            case "নাজানো":
                val = "Don\'t know";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_watersafe(String val) {
        switch (val) {
            case "उबालते है"://----replace with Hindi
                val = "Boil";
                break;
            case "फिटकरी  का उपयोग करें":
                val = "Use Alum";
                break;
            case "्लीच/क्लोरीन की गोलियां/बूंदें जोड़ें":
                val = "Add Bleach or Chlorine Tablets or Drops";
                break;
            case "कपड़े के माध्यम से तनाव":
                val = "Strain Through A Cloth";
                break;
            case "पानी फिल्टर का उपयोग करते है (सिरेमिक/रेत/समग्र/ईटीसी।)":
                val = "Use Water Filter (Ceramic or Sand or Composite, etc.)";
                break;
            case "इलेक्ट्रॉनिक फ़िल्टर का उपयोग करते है":
                val = "Use Electronic Purifier";
                break;
            case "इसे खड़े रहने दें और व्यवस्थित करें":
                val = "Let It Stand And Settle";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_watersafe(String val) {
        switch (val) {
            case "ফুটন্ত"://----replace with Hindi
                val = "Boil";
                break;
            case "অ্যালাম ব্যবহার করুন":
                val = "Use Alum";
                break;
            case "ব্লিচ বা ক্লোরিন ট্যাবলেট বা ড্রপ যোগ করুন":
                val = "Add Bleach or Chlorine Tablets or Drops";
                break;
            case "একটি কাপড় মাধ্যমে স্ট্রেন":
                val = "Strain Through A Cloth";
                break;
            case "জল ফিল্টার ব্যবহার করুন (সিরামিক বা বালি বা যৌগিক, ইত্যাদি)":
                val = "Use Water Filter (Ceramic or Sand or Composite, etc.)";
                break;
            case "ইলেকট্রনিক পিউরিফায়ার ব্যবহার করুন":
                val = "Use Electronic Purifier";
                break;
            case "লেট ইট স্ট্যান্ড অ্যান্ড সেটেল":
                val = "Let It Stand And Settle";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_watersafe(String val) {
        switch (val) {
            case "ଫୁଟାପାଣି"://----replace with Oriya
                val = "Boil";
                break;
            case "ଆଲମ ବ୍ୟବହାର":
                val = "Use Alum";
                break;
            case "ବ୍ଲିଚିଙ୍ଗ/କ୍ଲୋରାଇନ ପକାନ୍ତି":
                val = "Add Bleach or Chlorine Tablets or Drops";
                break;
            case "କପଡାରେ ଛାଣନ୍ତି":
                val = "Strain Through A Cloth";
                break;
            case "ୱାଟର ଫିଲଟର ବ୍ୟବହାର":
                val = "Use Water Filter (Ceramic or Sand or Composite, etc.)";
                break;
            case "ବିଦ୍ୟୁତ ବିଶୋଧନକାରୀ ଯନ୍ତ୍ର":
                val = "Use Electronic Purifier";
                break;
            case "ଏହାକୁ ଛିଡ଼ା ହେବାକୁ ଦିଅନ୍ତୁ ଏଵଂ ସ୍ଥିର / ବ୍ୟବସ୍ଥା କରନ୍ତୁ":
                val = "Let It Stand And Settle";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_en_watersafe(String val) {
        switch (val) {
            case "ઉકાળો"://----replace with Gujarati
                val = "Boil";
                break;
            case "ફટકડીનો ઉપયોગ કરો":
                val = "Use Alum";
                break;
            case "બ્લીચ/ક્લોરીન ટેબ્લેટ/ટીપાં ઉમેરો":
                val = "Add Bleach or Chlorine Tablets or Drops";
                break;
            case "કાપડ દ્વારા તાણ":
                val = "Strain Through A Cloth";
                break;
            case "વોટર ફિલ્ટરનો ઉપયોગ કરો (સિરામિક/રેતી/કમ્પોઝિટ/ઇટીસી.)":
                val = "Use Water Filter (Ceramic or Sand or Composite, etc.)";
                break;
            case "ઈલેક્ટ્રોનિક પ્યુરીફાયરનો ઉપયોગ કરો":
                val = "Use Electronic Purifier";
                break;
            case "મુકી રાખો  અને સ્થિર થવા દો":
                val = "Let It Stand And Settle";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_watersafe(String val) {
        switch (val) {
            case "উতলোৱা"://----replace with Assamese
                val = "Boil";
                break;
            case "এলুম ব্যৱহাৰ কৰক":
                val = "Use Alum";
                break;
            case "ব্লিচ বা ক্ল’ৰিন টেবলেট বা ড্ৰপ দিব":
                val = "Add Bleach or Chlorine Tablets or Drops";
                break;
            case "ষ্ট্ৰেইন থ্ৰু এ ক্লথ":
                val = "Strain Through A Cloth";
                break;
            case "পানী ফিল্টাৰ ব্যৱহাৰ কৰক (চিৰামিক বা বালি বা কম্পোজিট আদি)":
                val = "Use Water Filter (Ceramic or Sand or Composite, etc.)";
                break;
            case "ইলেক্ট্ৰনিক পিউৰিফায়াৰ ব্যৱহাৰ কৰক":
                val = "Use Electronic Purifier";
                break;
            case "থিয় হৈ থিতাপি লওক":
                val = "Let It Stand And Settle";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_sourcewater(String val) {
        switch (val) {
            case "आवास में बोर किया गया"://-------replace with hindi
                val = "Piped Into Dwelling";
                break;
            case "यार्ड/प्लॉट में बोर किया गया":
                val = "Piped Into Yard or Plot";
                break;
            case "सार्वजनिक नल/स्टैंडपाइप":
                val = "Public Tap or Standpipe";
                break;
            case "ट्यूब वेल/बोरहोल":
                val = "Tube Well or Borehole";
                break;
            case "संरक्षित कुआं":
                val = "Protected Well";
                break;
            case "असुरक्षित कुआं":
                val = "Unprotected Well";
                break;
            case "संरक्षित झरना":
                val = "Protected Spring";
                break;
            case "असुरक्षित झरना":
                val = "Unprotected Spring";
                break;
            case "वर्षा जल":
                val = "Rainwater";
                break;
            case "टैंकर  ट्रक":
                val = "Tanker Truck";
                break;
            case "टैंकर छोटे टैंक के साथ":
                val = "Cart With Small Truck";
                break;
            case "सतही जल":
                val = "Surface Water";
                break;
            case "सामान्य हैंड पंप":
                val = "Common Hand Pump";
                break;
            case "घर पर हैंड पंप":
                val = "Hand Pump At Home";
                break;
            case "नल जल योजना":
                val = "Nal Jal Yojana";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_sourcewater(String val) {
        switch (val) {
            case "বাসস্থান মধ্যে পাইপ"://-------replace with hindi
                val = "Piped Into Dwelling";
                break;
            case "গজ/প্লটে পাইপ করা":
                val = "Piped Into Yard or Plot";
                break;
            case "পাবলিক ট্যাপ/স্ট্যান্ডপাইপ":
                val = "Public Tap or Standpipe";
                break;
            case "নলকূপ/বোরহোল":
                val = "Tube Well or Borehole";
                break;
            case "ভাল সুরক্ষিত":
                val = "Protected Well";
                break;
            case "অরক্ষিত কূপ":
                val = "Unprotected Well";
                break;
            case "সুরক্ষিত বসন্ত":
                val = "Protected Spring";
                break;
            case "অরক্ষিত বসন্ত":
                val = "Unprotected Spring";
                break;
            case "বৃষ্টির জল":
                val = "Rainwater";
                break;
            case "ট্যাঙ্কার ট্রাক":
                val = "Tanker Truck";
                break;
            case "ছোট ট্যাঙ্ক সহ কার্ট":
                val = "Cart With Small Truck";
                break;
            case "পৃষ্ঠ জল":
                val = "Surface Water";
                break;
            case "সাধারণ হাত পাম্প":
                val = "Common Hand Pump";
                break;
            case "বাড়িতে হাত পাম্প":
                val = "Hand Pump At Home";
                break;
            case "ট্যাপ ওয়াটার স্কিম":
                val = "Nal Jal Yojana";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_sourcewater(String val) {
        switch (val) {
            case "ବାସଗୃହକୁ ପାଇପଯୋଗେ ପାଣି"://-------replace with Oriya
                val = "Piped Into Dwelling";
                break;
            case "ଅଗଣାକୁ ପାଇପ ପାଣି ଯୋଗାଣ":
                val = "Piped Into Yard or Plot";
                break;
            case "ସର୍ବସାଧାରଣ ଟ୍ୟାବ":
                val = "Public Tap or Standpipe";
                break;
            case "ନଳକୁପ/ ବୋର ୱେଲ":
                val = "Tube Well or Borehole";
                break;
            case "ସୁରକ୍ଷିତ କୂଅ":
                val = "Protected Well";
                break;
            case "ଅସୁରକ୍ଷିତ ପୂଅ":
                val = "Unprotected Well";
                break;
            case "ସୁରକ୍ଷିତ ଝରଣା":
                val = "Protected Spring";
                break;
            case "ଅସୁରକ୍ଷିତ ଝରଣା":
                val = "Unprotected Spring";
                break;
            case "ବର୍ଷାପାଣି":
                val = "Rainwater";
                break;
            case "ଟ୍ୟାଙ୍କର ପାଣି":
                val = "Tanker Truck";
                break;
            case "ଛୋଟ ଟ୍ୟାଙ୍କର ଗାଡି":
                val = "Cart With Small Truck";
                break;
            case "ଭୂପୃଷ୍ଠ ଜଳ":
                val = "Surface Water";
                break;
            case "ସାଧାରଣ ହାତ ପମ୍ପ":
                val = "Common Hand Pump";
                break;
            case "ଘରେ ଥିବା ହାତ ପମ୍ପ":
                val = "Hand Pump At Home";
                break;
            case "ଜଳ ଯୋଜନା ଟ୍ୟାପ୍ କରନ୍ତୁ":
                val = "Nal Jal Yojana";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_en_sourcewater(String val) {
        switch (val) {
            case "રહેઠાણમાં પાઈપ"://-------replace with Gujarati
                val = "Piped Into Dwelling";
                break;
            case "પાઈપ ટુ યાર્ડ/પ્લોટ":
                val = "Piped Into Yard or Plot";
                break;
            case "પબ્લિક ટેપ/સ્ટેન્ડપાઈપ":
                val = "Public Tap or Standpipe";
                break;
            case "ટ્યુબ વેલ/બોરહોલ":
                val = "Tube Well or Borehole";
                break;
            case "સંરક્ષિત કૂવો":
                val = "Protected Well";
                break;
            case "અસુરક્ષિત કૂવો":
                val = "Unprotected Well";
                break;
            case "સુરક્ષિત ઝરણું":
                val = "Protected Spring";
                break;
            case "અસુરક્ષિત ઝરણું":
                val = "Unprotected Spring";
                break;
            case "વરસાદી પાણી":
                val = "Rainwater";
                break;
            case "ટેન્કર ટ્રક":
                val = "Tanker Truck";
                break;
            case "નાની ટાંકી સાથેનું કાર્ટ":
                val = "Cart With Small Truck";
                break;
            case "સપાટીનું પાણી":
                val = "Surface Water";
                break;
            case "કોમન હેન્ડ પંપ":
                val = "Common Hand Pump";
                break;
            case "ઘરે હેન્ડ પંપ":
                val = "Hand Pump At Home";
                break;
            case "નળ જલ યોજના":
                val = "Nal Jal Yojana";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_sourcewater(String val) {
        switch (val) {
            case "বাসস্থান মধ্য পাইপ"://-------replace with Assamese
                val = "Piped Into Dwelling";
                break;
            case "পাইপ ইনটু য়াৰ্ড বা প্লট":
                val = "Piped Into Yard or Plot";
                break;
            case "ৰাজহুৱা টেপ বা ষ্টেণ্ডপাইপ":
                val = "Public Tap or Standpipe";
                break;
            case "টিউব ৱেল বা ব’ৰহ’ল":
                val = "Tube Well or Borehole";
                break;
            case "সুৰক্ষিত ভাল":
                val = "Protected Well";
                break;
            case "অসুৰক্ষিত কুঁৱা":
                val = "Unprotected Well";
                break;
            case "সুৰক্ষিত বসন্ত":
                val = "Protected Spring";
                break;
            case "অসুৰক্ষিত বসন্ত":
                val = "Unprotected Spring";
                break;
            case "বৰষুণৰ পানী":
                val = "Rainwater";
                break;
            case "টেংকাৰ ট্ৰাক":
                val = "Tanker Truck";
                break;
            case "সৰু ট্ৰাকৰ সৈতে কাৰ্ট":
                val = "Cart With Small Truck";
                break;
            case "পৃষ্ঠীয় পানী":
                val = "Surface Water";
                break;
            case "সাধাৰণ হেণ্ড পাম্প":
                val = "Common Hand Pump";
                break;
            case "হেণ্ড পাম্প এট হোম":
                val = "Hand Pump At Home";
                break;
            case "নল জল যোজনা":
                val = "Nal Jal Yojana";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_occupation(String val) {
        switch (val) {
            case "वेतनभोगी सरकारी नौकरी"://----------replace with Hindi
                val = "Salaried Government Job";
                break;
            case "वेतनभोगी निजी नौकरी":
                val = "Salaried Private Job";
                break;
            case "छोटा व्यापार/दुकान का मालिक":
                val = "Petty Business or Shop Owner";
                break;
            case "तकनीशियन/शिल्पकार/अन्य कुशल कार्य (चालक, राजमिस्त्री आदि)":
                val = "Technician or Craftsman or Other Skilled Work (Driver, Mason, etc.)";
                break;
            case "कृषि":
                val = "Agricultural Farmer";
                break;
            case "किराये की खेती / खेतिहर मजदूर":
                val = "Tenant Farmer or Agricultural Daily Wage Worker";
                break;
            case "दैनिक वेतन भोगी (अकुशल कार्य)":
                val = "Daily Wage Worker (Unskilled Worker)";
                break;
            case "घर का काम":
                val = "Household Work";
                break;
            case "छात्र":
                val = "Student";
                break;
            case "बेरोजगार":
                val = "Unemployed";
                break;
            case "सेवानिवृत्त (पेंशन के साथ)":
                val = "Retired (With Pension)";
                break;
            case "सेवानिवृत्त (पेंशन के बिना)":
                val = "Retired (Without Pension)";
                break;
            case "लागू नहीं":
                val = "Not Applicable";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_occupation(String val) {
        switch (val) {
            case "বেতনভুক্ত সরকারি চাকরি"://----------replace with Hindi
                val = "Salaried Government Job";
                break;
            case "বেতনভোগী বেসরকারি চাকরি":
                val = "Salaried Private Job";
                break;
            case "ক্ষুদ্র ব্যবসা বা দোকানের মালিক":
                val = "Petty Business or Shop Owner";
                break;
            case "টেকনিশিয়ান বা কারিগর বা অন্যান্য দক্ষ কাজ (ড্রাইভার, রাজমিস্ত্রি, ইত্যাদি)":
                val = "Technician or Craftsman or Other Skilled Work (Driver, Mason, etc.)";
                break;
            case "কৃষি কৃষক":
                val = "Agricultural Farmer";
                break;
            case "कভাড়াটিয়া কৃষক বা কৃষি দৈনিক মজুরি কর্মী":
                val = "Tenant Farmer or Agricultural Daily Wage Worker";
                break;
            case "দৈনিক মজুরি কর্মী (অদক্ষ শ্রমিক)":
                val = "Daily Wage Worker (Unskilled Worker)";
                break;
            case "গৃহ কর্ম":
                val = "Household Work";
                break;
            case "ছাত্র":
                val = "Student";
                break;
            case "বেকার":
                val = "Unemployed";
                break;
            case "অবসরপ্রাপ্ত (পেনশন সহ)":
                val = "Retired (With Pension)";
                break;
            case "অবসরপ্রাপ্ত (পেনশন ছাড়া)":
                val = "Retired (Without Pension)";
                break;
            case "প্রযোজ্য নয়":
                val = "Not Applicable";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_occupation(String val) {
        switch (val) {
            case "ଦରମାପ୍ରାପ୍ତ ସରକାରୀ କର୍ମଚାରୀ"://----------replace with Oriya
                val = "Salaried Government Job";
                break;
            case "ଦରମାପ୍ରାପ୍ତ ବେସରକାରୀ କର୍ମଚାରୀ":
                val = "Salaried Private Job";
                break;
            case "କ୍ଷୁଦ୍ର ବ୍ୟବସାୟୀ(ଦୋକାନୀ)":
                val = "Petty Business or Shop Owner";
                break;
            case "କୁଶଳୀ (ବଢେଇ, ଗାଡି ଚାଳକ ଇଚ୍ଛ୍ୟାଦି)":
                val = "Technician or Craftsman or Other Skilled Work (Driver, Mason, etc.)";
                break;
            case "କୃଷକ":
                val = "Agricultural Farmer";
                break;
            case "ଭାଗଚାଷୀ":
                val = "Tenant Farmer or Agricultural Daily Wage Worker";
                break;
            case "ଚାଷ ଜମି ଶ୍ରମିକ":
                val = "Daily Wage Worker (Unskilled Worker)";
                break;
            case "ଗୃହକାର୍ଯ୍ୟ":
                val = "Household Work";
                break;
            case "ଛାତ୍ର / ଛାତ୍ରୀ(ଅଣସଂରକ୍ଷିତ)":
                val = "Student";
                break;
            case "ବେକାରୀ":
                val = "Unemployed";
                break;
            case "ଚାକିରୀରୁ ଅବସର(ପେନସନ ଗ୍ରହୀତା)":
                val = "Retired (With Pension)";
                break;
            case "ଦିନ ମଜୁରୀ":
                val = "Retired (Without Pension)";
                break;
            case "ଉପଯୁକ୍ତ ନୁହେଁ":
                val = "Not Applicable";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_en_occupation(String val) {
        switch (val) {
            case "પગારવાળી સરકારી નોકરી"://----------replace with Gujarati
                val = "Salaried Government Job";
                break;
            case "પગારવાળી ખાનગી નોકરી":
                val = "Salaried Private Job";
                break;
            case "નાનો ધંધો/દુકાનના માલિક":
                val = "Petty Business or Shop Owner";
                break;
            case "ટેક્નીસિયન/સિલ્પકાર/અન્ય કુશળ કારીગર (ડ્રાઇવર, સુથાર વગેરે)":
                val = "Technician or Craftsman or Other Skilled Work (Driver, Mason, etc.)";
                break;
            case "ખેતી":
                val = "Agricultural Farmer";
                break;
            case "ભાડોકી ખેતી/ ખેત મજૂર":
                val = "Tenant Farmer or Agricultural Daily Wage Worker";
                break;
            case "દહાડી મજૂર( અકુશળ કારીગર )":
                val = "Daily Wage Worker (Unskilled Worker)";
                break;
            case "ઘરનું કામ":
                val = "Household Work";
                break;
            case "વિદ્યાર્થી":
                val = "Student";
                break;
            case "બેરોજગાર":
                val = "Unemployed";
                break;
            case "સેવા નિવૃત (પેન્સન સાથે)":
                val = "Retired (With Pension)";
                break;
            case "સેવા નિવૃત( પેન્સન વગર)":
                val = "Retired (Without Pension)";
                break;
            case "લાગુ પડતું નથી":
                val = "Not Applicable";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_occupation(String val) {
        switch (val) {

            case "দৰমহা পোৱা চৰকাৰী চাকৰি"://----------replace with assamese
                val = "Salaried Government Job";
                break;
            case "দৰমহা পোৱা ব্যক্তিগত চাকৰি":
                val = "Salaried Private Job";
                break;
            case "ক্ষুদ্ৰ ব্যৱসায় বা দোকানৰ মালিক":
                val = "Petty Business or Shop Owner";
                break;
            case "টেকনিচিয়ান বা শিল্পী বা অন্যান্য দক্ষ কাম (ড্ৰাইভাৰ, মেছন আদি)":
                val = "Technician or Craftsman or Other Skilled Work (Driver, Mason, etc.)";
                break;
            case "কৃষি কৃষক":
                val = "Agricultural Farmer";
                break;
            case "ভাড়াতীয়া কৃষক বা কৃষি দৈনিক মজুৰি শ্ৰমিক":
                val = "Tenant Farmer or Agricultural Daily Wage Worker";
                break;
            case "দৈনিক মজুৰি শ্ৰমিক (অদক্ষ শ্ৰমিক)":
                val = "Daily Wage Worker (Unskilled Worker)";
                break;
            case "ঘৰুৱা কাম":
                val = "Household Work";
                break;
            case "ছাত্ৰ":
                val = "Student";
                break;
            case "নিবনুৱা":
                val = "Unemployed";
                break;
            case "অৱসৰপ্ৰাপ্ত (পেঞ্চনৰ সৈতে)":
                val = "Retired (With Pension)";
                break;
            case "অৱসৰপ্ৰাপ্ত (পেঞ্চনবিহীন)":
                val = "Retired (Without Pension)";
                break;
            case "প্ৰযোজ্য নহয়":
                val = "Not Applicable";
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
    public static String switch_bn_en_bankaccount(String val) {
        switch (val) {
            case "উত্তর দিতে অস্বীকৃতি জানায়":
                val = "Declined to answer";
                break;
            case "हाँ":
                val = "হ্যাঁ";
                break;
            case "না":
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

    public static String switch_as_en_bankaccount(String val) {
        switch (val) {
            case "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে"://--------replace with Assamese
                val = "Declined to answer";
                break;
            case "হয়":
                val = "Yes";
                break;
            case "নহয়":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_mobile(String val) {
        switch (val) {
            case "कोई नहीं":///-------replace with Hindi
                val = "None";
                break;
            case "स्वयं का स्मार्टफोन है":
                val = "Own Smartphone";
                break;
            case "स्वयं का फीचर फोन है":
                val = "Own Feature Phone";
                break;
            case "साझा स्मार्टफोन है":
                val = "Shared Smartphone";
                break;
            case "साझा फीचर फोन है":
                val = "Shared Feature Phone";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_mobile(String val) {
        switch (val) {
            case "কোনোটিই নয়":///-------replace with Hindi
                val = "None";
                break;
            case "নিজস্ব স্মার্টফোন":
                val = "Own Smartphone";
                break;
            case "নিজস্ব ফিচার ফোন":
                val = "Own Feature Phone";
                break;
            case "শেয়ার করা স্মার্টফোন":
                val = "Shared Smartphone";
                break;
            case "শেয়ার করা ফিচার ফোন":
                val = "Shared Feature Phone";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_mobile(String val) {
        switch (val) {
            case "କିଛି ନୁହେଁ":///-------replace with Oriya
                val = "None";
                break;
            case "ନିଜର ସ୍ମାର୍ଟ ଫୋନ୍":
                val = "Own Smartphone";
                break;
            case "ନିଜର ସ୍ଵତନ୍ତ୍ର ଫୋନ୍":
                val = "Own Feature Phone";
                break;
            case "ଅନ୍ୟ ସହ ଅଂଶୀଦାର ସ୍ମାର୍ଟ ଫୋନ୍":
                val = "Shared Smartphone";
                break;
            case "ଅନ୍ୟ ସହ ଅଂଶୀଦାର ସ୍ଵତନ୍ତ୍ର ଫୋନ୍":
                val = "Shared Feature Phone";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_en_mobile(String val) {
        switch (val) {
            case "કોઈ નથી":///-------replace with Gujarati
                val = "None";
                break;
            case "પોતાનો સ્માર્ટફોન":
                val = "Own Smartphone";
                break;
            case "પોતાનો ફીચર ફોન":
                val = "Own Feature Phone";
                break;
            case "શેર કરેલ સ્માર્ટફોન":
                val = "Shared Smartphone";
                break;
            case "શેર કરેલ ફીચર ફોન":
                val = "Shared Feature Phone";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_mobile(String val) {
        switch (val) {
            case "একো নাই":///-------replace with Assamese
                val = "None";
                break;
            case "স্মাৰ্টফোনৰ মালিক":
                val = "Own Smartphone";
                break;
            case "বৈশিষ্ট্য ফোন মালিক":
                val = "Own Feature Phone";
                break;
            case "শ্বেয়াৰ কৰা স্মাৰ্টফোন":
                val = "Shared Smartphone";
                break;
            case "শ্বেয়াৰ কৰা বৈশিষ্ট্য ফোন":
                val = "Shared Feature Phone";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_whatsapp(String val) {
        switch (val) {
            case "हां (परिवार के सदस्य)"://------replace with Hindi
                val = "Yes (Family Member)";
                break;
            case "हां (व्यक्तिगत)":
                val = "Yes (Personal)";
                break;
            case "नहीं":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_whatsapp(String val) {
        switch (val) {
            case "হ্যাঁ (পরিবারের সদস্য)"://------replace with Hindi
                val = "Yes (Family Member)";
                break;
            case "হ্যাঁ (ব্যক্তিগত)":
                val = "Yes (Personal)";
                break;
            case "না":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_whatsapp(String val) {
        switch (val) {
            case "ହଁ (ପରିବାର ସଦସ୍ୟ)"://------replace with Oriya
                val = "Yes (Family Member)";
                break;
            case "ହଁ (ନିଜସ୍ଵ)":
                val = "Yes (Personal)";
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
            case "હા (કુટુંબના સભ્ય)"://------replace with Gujarati
                val = "Yes (Family Member)";
                break;
            case "હા (વ્યક્તિગત)":
                val = "Yes (Personal)";
                break;
            case "ના":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_whatsapp(String val) {
        switch (val) {
            case "হয় (পৰিয়ালৰ সদস্য)"://------replace with Assamese
                val = "Yes (Family Member)";
                break;
            case "হয় (ব্যক্তিগত)":
                val = "Yes (Personal)";
                break;
            case "নহয়":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_education(String val) {
        switch (val) {
            case "पढ़ या लिख नहीं सकता"://------replace with Hindi
                val = "Cannot read or write";
                break;
            case "पढ़ और लिख सकते हैं":
                val = "Can read and write";
                break;
            case "प्राथमिक शिक्षा (कक्षा 5 तक)":
                val = "Primary education (Till Class 5)";
                break;
            case "मिडिल स्कूल (6वीं-8वीं)":
                val = "Middle school (6th - 8th)";
                break;
            case "माध्यमिक विद्यालय उत्तीर्ण (कक्षा 10वीं बोर्ड)":
                val = "Passed secondary school (Class 10th boards)";
                break;
            case "वरिष्ठ माध्यमिक विद्यालय (12वीं बोर्ड) उत्तीर्ण":
                val = "Passed senior secondary school (Class 12th boards)";
                break;
            case "स्नातक":
                val = "Graduate";
                break;
            case "स्नातकोत्तर":
                val = "Postgraduate";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_education(String val) {
        switch (val) {
            case "পড়তে বা লিখতে পারে না"://------replace with Hindi
                val = "Cannot read or write";
                break;
            case "পড়তে ও লিখতে পারে":
                val = "Can read and write";
                break;
            case "প্রাথমিক শিক্ষা (5 শ্রেণী পর্যন্ত)":
                val = "Primary education (Till Class 5)";
                break;
            case "মাধ্যমিক বিদ্যালয় (৬ষ্ঠ - ৮ম)":
                val = "Middle school (6th - 8th)";
                break;
            case "মাধ্যমিক বিদ্যালয়ে উত্তীর্ণ (দশম শ্রেণী বোর্ড)":
                val = "Passed secondary school (Class 10th boards)";
                break;
            case "সিনিয়র সেকেন্ডারি স্কুল পাশ (দ্বাদশ বোর্ড)":
                val = "Passed senior secondary school (Class 12th boards)";
                break;
            case "স্নাতক":
                val = "Graduate";
                break;
            case "স্নাতকোত্তর":
                val = "Postgraduate";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_education(String val) {
        switch (val) {
            case "ପଢିବା ଏବଂ ଲେଖିବା ଜାଣେନାହିଁ"://------replace with Oriya
                val = "Cannot read or write";
                break;
            case "ପଢିପାରେ ଏବଂ ଲେଖିପାରେ":
                val = "Can read and write";
                break;
            case "ପ୍ରାଥମିକ ଶିକ୍ଷା( ୫ମ ଶ୍ରେଣୀ ପର୍ଯ୍ୟନ୍ତ)":
                val = "Primary education (Till Class 5)";
                break;
            case "ଉଚ୍ଚ ପ୍ରାଥମିକ ଶିକ୍ଷା( ଷଷ୍ଠ ରୁ ଅଷ୍ଟମ ପର୍ଯ୍ୟନ୍ତ)":
                val = "Middle school (6th - 8th)";
                break;
            case "ମାଧ୍ୟମିକ ଶତକ୍ଷା (ଦଶମ)":
                val = "Passed secondary school (Class 10th boards)";
                break;
            case "ଯୁକ୍ତ ଦୁଇ (ଦ୍ୱାଦଶ)":
                val = "Passed senior secondary school (Class 12th boards)";
                break;
            case "ସ୍ନାତକ ଶିକ୍ଷା":
                val = "Graduate";
                break;
            case "ସ୍ନାତକୋତ୍ତତର":
                val = "Postgraduate";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_education(String val) {
        switch (val) {
            case "વાંચી કે લખી શકતા નથી"://------replace with Gujarati
                val = "Cannot read or write";
                break;
            case "વાંચી અને લખી શકે છે":
                val = "Can read and write";
                break;
            case "પ્રાથમિક શિક્ષા(ધોરણ 5 સુધી)":
                val = "Primary education (Till Class 5)";
                break;
            case "મિડલ સ્કૂલ (6ઠ્ઠી-8મી)":
                val = "Middle school (6th - 8th)";
                break;
            case "માધ્યમિક શાળા(10 બોર્ડ ) પાસ":
                val = "Passed secondary school (Class 10th boards)";
                break;
            case "ઉચ્ચતર માધ્યમિક શાળા(12 બોર્ડ) પાસ":
                val = "Passed senior secondary school (Class 12th boards)";
                break;
            case "સ્નાતક":
                val = "Graduate";
                break;
            case "અનુસ્નાતક":
                val = "Postgraduate";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_education(String val) {
        switch (val) {
            case "পঢ়িব বা লিখিব নোৱাৰি"://------replace with Assamese
                val = "Cannot read or write";
                break;
            case "পঢ়িব আৰু লিখিব পাৰে":
                val = "Can read and write";
                break;
            case "প্ৰাথমিক শিক্ষা (৫ম শ্ৰেণীলৈকে)":
                val = "Primary education (Till Class 5)";
                break;
            case "মধ্যবিদ্যালয় (৬ম - ৮ম)":
                val = "Middle school (6th - 8th)";
                break;
            case "মাধ্যমিক বিদ্যালয় উত্তীৰ্ণ (দশম শ্ৰেণীৰ বৰ্ড)":
                val = "Passed secondary school (Class 10th boards)";
                break;
            case "জ্যেষ্ঠ মাধ্যমিক বিদ্যালয়ত উত্তীৰ্ণ (ক্লাছ দ্বাদশ বৰ্ড)":
                val = "Passed senior secondary school (Class 12th boards)";
                break;
            case "স্নাতক":
                val = "Graduate";
                break;
            case "স্নাতকোত্তৰ":
                val = "Postgraduate";
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
    public static String switch_bn_economic(String val) {
        switch (val) {
            case "এপিএল":
                val = "APL";
                break;
            case "বিপিএল":
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

    public static String switch_as_economic(String val) {
        switch (val) {
            case "এ পি এল"://------replace with assamese
                val = "APL";
                break;
            case "বি পি এল":
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
    public static String switch_bn_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "নিরক্ষর";
                break;
            case "Primary":
                val = "প্রাথমিক";
                break;
            case "Secondary":
                val = "মাধ্যমিক";
                break;
            case "Higher Secondary":
                val = "উচ্চ মাধ্যমিক";
                break;
            case "Graduation & Higher":
                val = "স্নাতক এবং উচ্চতর";
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

    public static String switch_as_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "Illiterate";//-------replace with Assamese
                break;
            case "Primary":
                val = "Primary";
                break;
            case "Secondary":
                val = "Secondary";
                break;
            case "Higher Secondary":
                val = "Higher Secondary";
                break;
            case "Graduation & Higher":
                val = "Graduation & Higher";
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
    public static String switch_bn_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "এপিএল";
                break;
            case "BPL":
                val = "বিপিএল";
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

    public static String switch_as_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "এ পি এল";//------replace with assamese
                break;
            case "BPL":
                val = "বি পি এল";
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
    public static String switch_bn_caste(String val) {
        switch (val) {
            case "সাধারণ":
                val = "General";
                break;
            case "ওবিসি":
                val = "OBC";
                break;
            case "এসসি":
                val = "SC";
                break;
            case "স টি ":
                val = "ST";
                break;
            case "অন্যান্য":
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
            case "ଜେନେରାଲ୍"://-------replace with Odiya
                val = "General";
                break;
            case "ଅନ୍ୟ ପଛୁଆ":
                val = "OBC";
                break;
            case "କାର୍ଯ୍ୟସୂଚୀ":
                val = "SC";
                break;
            case "ଅନୁସୂଚିତ ଜନଜାତି":
                val = "ST";
                break;
            case "ଅନ୍ୟମାନେ":
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

    //---------change condition in assamese--------
    public static String switch_as_caste(String val) {
        switch (val) {
            case "সাধাৰণ"://-------replace with assamese
                val = "General";
                break;
            case "অ’বিচি":
                val = "OBC";
                break;
            case "অনুসুচিত জাতিৰ":
                val = "SC";
                break;
            case "অনুসুচিত জনজাতিৰ":
                val = "ST";
                break;
            case "আন কিছুমান":
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
    public static String switch_bn_bankaccount_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "উত্তর দিতে অস্বীকৃতি জানায়";
                break;
            case "Yes":
                val = "হ্যাঁ";
                break;
            case "No":
                val = "না";
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

    public static String switch_as_bankaccount_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে";//-------replace with assamese
                break;
            case "Yes":
                val = "হয়";
                break;
            case "No":
                val = "নহয়";
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

    public static String switch_as_mobiletype_edit(String val) {
        switch (val) {
            case "Basic Phone":
                val = "Basic Phone";//--------replace with assamese
                break;
            case "Smartphone":
                val = "Smartphone";
                break;
            case "Does not own mobile phone":
                val = "Does not own mobile phone";
                break;
            default:
                return val;
        }
        return val;
    }

    /*public static String switch_hi_watersource_edit(String val) {
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
    }*/

    /*public static String switch_or_watersource_edit(String val) {
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
    }*/

   /* public static String switch_gu_watersource_edit(String val) {
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
    }*/

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
    public static String switch_bn_wateravail_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "উত্তর দিতে অস্বীকৃতি জানায়";
                break;
            case "Yes":
                val = "হ্যাঁ";
                break;
            case "No":
                val = "না";
                break;
            case "Don\'t know":
                val = "জানি না";
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
                val = "ଜାଣିନାହିଁ / ଜାଣେନାହିଁ";
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

    public static String switch_as_wateravail_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে";//-------replace with assamese
                break;
            case "Yes":
                val = "হয়";
                break;
            case "No":
                val = "নহয়";
                break;
            case "Don\'t know":
                val = "নাজানো";
                break;
            default:
                return val;
        }
        return val;
    }

    /*public static String switch_hi_watersafe_edit(String val) {
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
    }*/

    /*public static String switch_or_watersafe_edit(String val) {
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
    }*/

    /*public static String switch_gu_watersafe_edit(String val) {
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
    }*/

    //Vaccination
    public static String switch_hi_en_vaccination(String val) {
        switch (val) {
            case "पहला टिका":
                val = "First dose";
                break;
            case "दूसरा टिका":
                val = "Second dose";
                break;
            case "तीसरी खुराक":
                val = "Third dose";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_vaccination(String val) {
        switch (val) {
            case "প্রথম ডোজ":
                val = "First dose";
                break;
            case "দ্বিতীয় ডোজ":
                val = "Second dose";
                break;
            case "তৃতীয় ডোজ":
                val = "Third dose";
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
            case "ତୃତୀୟ ଦୋଜ":
                val = "Third dose";
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
            case "ત્રીજો ડોઝ":
                val = "Third dose";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_vaccination(String val) {
        switch (val) {
            case "প্ৰথম ড’জ"://-----replace with gujrati
                val = "First dose";
                break;
            case "দ্বিতীয় ড’জ":
                val = "Second dose";
                break;
            case "তৃতীয় ড’জ":
                val = "Third dose";
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
            case "Third dose":
                val = "तीसरी खुराक";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_vaccination_edit(String val) {
        switch (val) {
            case "First dose":
                val = "প্রথম ডোজ";
                break;
            case "Second dose":
                val = "দ্বিতীয় ডোজ";
                break;
            case "Third dose":
                val = "তৃতীয় ডোজ";
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
            case "Third dose":
                val = "ତୃତୀୟ ଦୋଜ";
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
            case "Third dose":
                val = "ત્રીજો ડોઝ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_vaccination_edit(String val) {
        switch (val) {
            case "First dose":
                val = "প্ৰথম ড’জ";//-----replace with Assamese
                break;
            case "Second dose":
                val = "দ্বিতীয় ড’জ";
                break;
            case "Third dose":
                val = "তৃতীয় ড’জ";
                break;
            default:
                return val;
        }
        return val;
    }

    /*public static String switch_hi_whatsapp_edit(String val) {
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
    }*/

    /*public static String switch_or_whatsapp_edit(String val) {
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
    }*/

   /* public static String switch_gu_whatsapp_edit(String val) {
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
    }*/

   /* public static String switch_hi_occupation_edit(String val) {
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

    }*/

    /*public static String switch_or_occupation_edit(String val) {
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

    }*/

    /*public static String switch_gu_occupation_edit(String val) {
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

    }*/

    /*public static String switch_as_occupation_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "Declined to answer";//----------replace with Assamese
                break;
            case "Government job":
                val = "Government job";
                break;
            case "Large scale to medium scale industry":
                val = "Large scale to medium scale industry";
                break;
            case "Professional job in private sector":
                val = "Professional job in private sector";
                break;
            case "Small scale industry":
                val = "Small scale industry";
                break;
            case "Big shop owner":
                val = "Big shop owner";
                break;
            case "Technician/craftsman":
                val = "Technician/craftsman";
                break;
            case "Small shop owner":
                val = "Small shop owner";
                break;
            case "Large scale farmer":
                val = "Large scale farmer";
                break;
            case "Daily wage earner":
                val = "Daily wage earner";
                break;
            case "Small scale farmer/farm worker":
                val = "Small scale farmer/farm worker";
                break;
            case "Unemployed":
                val = "Unemployed";
                break;
            case "Housewife":
                val = "Housewife";
                break;
            case "Student":
                val = "Student";
                break;
            case "Under 5 child":
                val = "Under 5 child";
                break;
            case "Other skills (driver,mason etc)":
                val = "Other skills (driver,mason etc)";
                break;
            case "[Describe]":
                val = "[Describe]";
                break;
            default:
                return val;
        }
        return val;

    }*/

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
    public static String switch_bn_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "সাধারণ";
                break;
            case "OBC":
                val = "ওবিসি";
                break;
            case "SC":
                val = "এসসি";
                break;
            case "ST":
                val = "স টি ";
                break;
            case "others":
                val = "অন্যান্য";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "ଜେନେରାଲ୍";//-----replace with odiya
                break;
            case "OBC":
                val = "ଅନ୍ୟ ପଛୁଆ";
                break;
            case "SC":
                val = "କାର୍ଯ୍ୟସୂଚୀ";
                break;
            case "ST":
                val = "ଅନୁସୂଚିତ ଜନଜାତି";
                break;
            case "others":
                val = "ଅନ୍ୟମାନେ";
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

    public static String switch_as_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "সাধাৰণ";//-----replace with assamese
                break;
            case "OBC":
                val = "অ’বিচি";
                break;
            case "SC":
                val = "অনুসুচিত জাতিৰ";
                break;
            case "ST":
                val = "অনুসুচিত জনজাতিৰ";
                break;
            case "others":
                val = "আন কিছুমান";
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

    public static String hi_or_gu_as_en_month(int month_index) {
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
    public static String en__bn_dob(String dob) { //English dob is replaced to Hindi text.
        String mdob_text = dob
                .replace("January", "জানুয়ারি")
                .replace("February", "ফেব্রুয়ারি")
                .replace("March", "মার্চ")
                .replace("April", "এপ্রিল")
                .replace("May", "মে")
                .replace("June", "জুন")
                .replace("July", "জুলাই")
                .replace("August", "আগস্ট")
                .replace("September", "সেপ্টেম্বর")
                .replace("October", "অক্টোবর")
                .replace("November", "নভেম্বর")
                .replace("December", "ডিসেম্বর");

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

    public static String en__as_dob(String dob) { //English dob is replaced to Odiya text.
        String mdob_text = dob
                .replace("January", "জানুৱাৰী")//replace with gujrati---replacement value
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

    public static String hi_or_gu_as_en_noEdit(String dobString, String locale) {

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
        } else if (locale.equalsIgnoreCase("bn")) {
            //Odiya
            String dob = dobString
                    .replace("জানুয়ারি", "January")
                    .replace("ফেব্রুয়ারি", "February")
                    .replace("মার্চ", "March")
                    .replace("এপ্রিল", "April")//ଅପ୍ରେଲ
                    .replace("মে", "May")//ମଇ
                    .replace("জুন", "June")
                    .replace("জুলাই", "July")
                    .replace("আগস্ট", "August")
                    .replace("সেপ্টেম্বর", "September")
                    .replace("অক্টোবর", "October")
                    .replace("নভেম্বর", "November")
                    .replace("ডিসেম্বর", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("gu")) {
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
        } else if (locale.equalsIgnoreCase("as")) {
            //replace with assamese
            String dob = dobString
                    .replace("জানুৱাৰী", "January")//-----------replace with assamese---target value
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

        if (locale.equalsIgnoreCase("as")) {

            switch (val) {
                case "Sunday":
                    val = "দেওবাৰ";//--------repalce with assamese---val value
                    break;
                case "Monday":
                    val = "সোমবাৰ";
                    break;
                case "Tuesday":
                    val = "মঙ্গলবাৰ";
                    break;
                case "Wednesday":
                    val = "বুধবাৰ";
                    break;

                case "Thursday":
                    val = "বৃহস্পতিবাৰ";
                    break;
                case "Friday":
                    val = "শুক্ৰবাৰ";
                    break;
                case "Saturday":
                    val = "শনিবাৰ";
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
        if (locale.equalsIgnoreCase("gu")) {

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

        if (locale.equalsIgnoreCase("as")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "বুকিং কৰা হৈছে";//------replace with assamese
                    break;
                case "cancelled":
                    val = "বাতিল কৰা হৈছে";
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
        Logger.logD("Selected", String.valueOf(selectedCheckboxes));
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
                text = getCheckboxesString(((CheckBox) childAt).getText().toString(), context, updatedContext, locale);

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

        Logger.logD("Toast", result.toString());
        return result.toString();
    }

    public static void setSelectedCheckboxes(ViewGroup viewGroup, String text, Context context, Context updatedContext, String locale) {
        if (viewGroup == null)
            return;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) childAt;
                checkBox.setChecked(text.contains(getCheckboxesString(checkBox.getText().toString(), updatedContext, context, locale)));
            }

            if (childAt instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) childAt;
                if (text.contains(getRadioButtonStrings(radioButton.getText().toString(), updatedContext, context, locale))) {
                    radioButton.setChecked(true);
                    break;
                }
                if (text.contains(getSmokingHistoryStrings(radioButton.getText().toString(), updatedContext, context, locale))) {
                    radioButton.setChecked(true);
                    break;
                }
                if (text.contains(getAlcoholHistory(radioButton.getText().toString(), updatedContext, context, locale))) {
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

    public static String getReligionStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
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
        }
        return text;
    }

    public static String getRadioButtonStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
            }
            // Translate string No Expense to English
            if (context.getString(R.string.no_expense).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_expense);
            }
            // Translate string 0 - 1 to English
            if (context.getString(R.string.zero_one).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.zero_one);
            }
            // Translate string 1 - 2 to English
            if (context.getString(R.string.one_two).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.one_two);
            }
            // Translate string 2 - 5 to English
            if (context.getString(R.string.two_five).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.two_five);
            }
            // Translate string 5 - 10 to English
            if (context.getString(R.string.five_ten).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.five_ten);
            }
            // Translate string More than 10 to English
            if (context.getString(R.string.more_than_10).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_10);
            }
            // Translate string 1 - 3000 to English
            if (context.getString(R.string.one_three_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.one_three_thousand);
            }
            // Translate string 0 - 30000 to English
            if (context.getString(R.string.zero_thirty_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.zero_thirty_thousand);
            }
            // Translate string 30000 - 50000 to English
            if (context.getString(R.string.thirty_fifty_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.thirty_fifty_thousand);
            }
            // Translate string 50000 - 100000 to English
            if (context.getString(R.string.fifty_thousand_one_lakh).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.fifty_thousand_one_lakh);
            }
            // Translate string 100000 - 250000 to English
            if (context.getString(R.string.one_lakh_two_lakh_fifty_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.one_lakh_two_lakh_fifty_thousand);
            }
            // Translate string More Than 2,50,000 to English
            if (context.getString(R.string.more_than_two_lakh_fifty_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_two_lakh_fifty_thousand);
            }
            // Translate string 0 - 3000 to English
            if (context.getString(R.string.zero_three_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.zero_three_thousand);
            }
            // Translate string 3000 - 5000 to English
            if (context.getString(R.string.three_five_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.three_five_thousand);
            }
            // Translate string 5000 - 10000 to English
            if (context.getString(R.string.five_ten_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.five_ten_thousand);
            }
            // Translate string 0 - 3000 to English
            if (context.getString(R.string.ten_twenty_five_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.ten_twenty_five_thousand);
            }
            // Translate string More Than 25,000 to English
            if (context.getString(R.string.more_than_twenty_five_thousand).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_twenty_five_thousand);
            }
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
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
        }
        return text;
    }

    public static String getLandOwnedStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
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
        }
        return text;
    }

    public static String getCheckboxesString(String text, Context context, Context updatedContext, String locale) {
        Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));

        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
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
            // Translate string Rainwater to English
            if (context.getString(R.string.rainwater).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.rainwater);
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
            // Translate string Nal Jal Yojana to English
            if (context.getString(R.string.nal_jal_yojana).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.nal_jal_yojana);
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
            // Translate string No Expense to English
            if (context.getString(R.string.no_expense).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_expense);
            }
        }
        return text;
    }

    public static String getSurveyStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
            }

            // Translate string Hours to English
            if (context.getString(R.string.identification_screen_picker_hours).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.identification_screen_picker_hours);
            }
            // Translate string Days to English
            if (context.getString(R.string.days).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.days);
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
            // Translate string Petty Business or Shop Owner to English
            if (context.getString(R.string.petty_business_shop_owner).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.petty_business_shop_owner);
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
            // Translate string Not Applicable to English
            if (context.getString(R.string.not_applicable).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.not_applicable);
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
            // Translate string Own Smartphone to English
            if (context.getString(R.string.own_smartphone).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.own_smartphone);
            }
            // Translate string Own Feature Phone to English
            if (context.getString(R.string.own_feature_phone).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.own_feature_phone);
            }
            // Translate string Shared Smartphone to English
            if (context.getString(R.string.shared_smartphone).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.shared_smartphone);
            }
            // Translate string Shared Feature Phone to English
            if (context.getString(R.string.shared_feature_phone).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.shared_feature_phone);
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
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }

            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
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
            // Translate string Less than 5 bidis / cigarettes
            if (context.getString(R.string.less_than_5_cigarettes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.less_than_5_cigarettes);
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
            // Translate strings Daily to English
            if (context.getString(R.string.daily).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.daily);
            }
            // Translate strings Once A Week to English
            if (context.getString(R.string.once_a_week).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.once_a_week);
            }
            // Translate strings Twice A Week to English
            if (context.getString(R.string.twice_a_week).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.twice_a_week);
            }
            // Translate strings Occasionally to English
            if (context.getString(R.string.occasionally_once_a_month_or_less).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.occasionally_once_a_month_or_less);
            }
            // Translate string Denied to Answer
            if (context.getString(R.string.denied_to_answer).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.denied_to_answer);
            }
        }

        return text;
    }

    public static String getAlcoholHistory(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }
            // Translate string No or Denied to English
            if (context.getString(R.string.no_denied).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.no_denied);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
            }
            // Translate string Denied to Answer
            if (context.getString(R.string.denied_to_answer).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.denied_to_answer);
            }
            // Translate string Zero To Five Times to English
            if (context.getString(R.string.zero_five_times).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.zero_five_times);
            }
            // Translate string Five To Ten Times to English
            if (context.getString(R.string.five_ten_times).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.five_ten_times);
            }
            // Translate string More Than Ten Times to English
            if (context.getString(R.string.more_than_10_times).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_10_times);
            }
            // Translate string Less Than A Year to English
            if (context.getString(R.string.less_than_a_year).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.less_than_a_year);
            }
            // Translate string From One Year To Five Year to English
            if (context.getString(R.string.from_one_year_to_five_year).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.from_one_year_to_five_year);
            }
            // Translate string From Five Year To Ten Year to English
            if (context.getString(R.string.from_five_years_to_ten_years).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.from_five_years_to_ten_years);
            }
            // Translate string More Than Ten Years to English
            if (context.getString(R.string.more_than_ten_years).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_ten_years);
            }
            // Translate strings Daily to English
            if (context.getString(R.string.daily).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.daily);
            }
            // Translate strings Once A Week to English
            if (context.getString(R.string.once_a_week).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.once_a_week);
            }
            // Translate strings Twice A Week to English
            if (context.getString(R.string.twice_a_week).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.twice_a_week);
            }
            // Translate strings Occasionally to English
            if (context.getString(R.string.occasionally_once_a_month_or_less).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.occasionally_once_a_month_or_less);
            }
            // Translate string 0 - 50 ml to English
            if (context.getString(R.string.zero_fifty_ml).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.zero_fifty_ml);
            }
            // Translate string 50 - 100 ml to English
            if (context.getString(R.string.fifty_hundred_ml).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.fifty_hundred_ml);
            }
            // Translate string 100 - 250 ml to English
            if (context.getString(R.string.hundred_two_hundred_fifty_ml).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.hundred_two_hundred_fifty_ml);
            }
            // Translate string 250 - 500 ml to English
            if (context.getString(R.string.two_hundred_fifty_five_hundred_ml).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.two_hundred_fifty_five_hundred_ml);
            }
            // Translate string More than 500 ml to English
            if (context.getString(R.string.more_than_five_hundred_ml).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_five_hundred_ml);
            }
        }

        return text;
    }

    public static String getDistanceStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Within 5 minutes to English
            if (context.getString(R.string.within_5_minutes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.within_5_minutes);
            }
            // Translate string Within Five To Fifteen Minutes to English
            if (context.getString(R.string.five_fifteen_minutes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.five_fifteen_minutes);
            }
            // Translate string Within Fifteen To Thirty Minutes to English
            if (context.getString(R.string.fifteen_thirty_minutes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.fifteen_thirty_minutes);
            }
            // Translate string More Than Thirty Minutes to English
            if (context.getString(R.string.more_than_thirty_minutes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_thirty_minutes);
            }
            // Translate string Within 1 km to English
            if (context.getString(R.string.within_1_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.within_1_km);
            }
            // Translate string Within 1 - 3 km to English
            if (context.getString(R.string.one_three_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.one_three_km);
            }
            // Translate string Within 3 - 5 km to English
            if (context.getString(R.string.three_five_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.one_three_km);
            }
            // Translate string More Than 5 km to English
            if (context.getString(R.string.more_than_five_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_five_km);
            }
            // Translate string Within 3 km to English
            if (context.getString(R.string.within_3_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.within_3_km);
            }
            // Translate string 3 - 6 km to English
            if (context.getString(R.string.three_six_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.three_six_km);
            }
            // Translate string 6 - 10 km to English
            if (context.getString(R.string.six_ten_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.six_ten_km);
            }
            // Translate string More Than 10 km to English
            if (context.getString(R.string.more_than_10km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_10km);
            }
            // Translate string Within 5 km to English
            if (context.getString(R.string.within_5_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.within_5_km);
            }
            // Translate string Within 5 - 10 km to English
            if (context.getString(R.string.five_ten_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.five_ten_km);
            }
            // Translate string 10 - 20 km to English
            if (context.getString(R.string.ten_twenty_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.ten_twenty_km);
            }
            // Translate string Within 20 - 30 km to English
            if (context.getString(R.string.twenty_thirty_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.twenty_thirty_km);
            }
            // Translate string More than 30 km to English
            if (context.getString(R.string.more_than_thirty_km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_thirty_km);
            }
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
            }
        }
        return text;
    }

    public static String getWhatsAppStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes (Family Member) to English
            if (context.getString(R.string.yes_family_member).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.yes_family_member);
            }
            // Translate string Yes (Personal) to English
            if (context.getString(R.string.yes_personal).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.yes_personal);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
            }
        }
        return text;
    }

    public static String getTimeStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        if (!locale.equalsIgnoreCase("en")) {
            // Translate hours to English
            if (context.getString(R.string.identification_screen_picker_hours).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.identification_screen_picker_hours);
            }
            // Translate days to English
            if (context.getString(R.string.days).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.days);
            }
        }
        return text;
    }

    private static String trimAdvanced(String value) {

        int strLength = value.length();
        int len = value.length();
        int st = 0;
        char[] val = value.toCharArray();

        if (strLength == 0) {
            return "";
        }

        while ((st < len) && (val[st] <= ' ') || (val[st] == '\u00A0')) {
            st++;
            if (st == strLength) {
                break;
            }
        }

        while ((st < len) && (val[len - 1] <= ' ') || (val[len - 1] == '\u00A0')) {
            len--;
            if (len == 0) {
                break;
            }
        }


        return (st > len) ? "" : ((st > 0) || (len < strLength)) ? value.substring(st, len) : value;
    }
}
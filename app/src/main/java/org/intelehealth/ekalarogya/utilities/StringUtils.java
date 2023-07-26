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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;
import org.intelehealth.ekalarogya.models.dto.ObsDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            val = switch_kn_caste(val);
            val = switch_kn_economic(val);
            val = switch_kn_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            val = switch_mr_caste(val);
            val = switch_mr_economic(val);
            val = switch_mr_education(val);
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
                case "खुले में शौच":
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
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            switch (checkbox_text) {
                case "ಉತ್ತರಿಸಲು ನಿರಾಕರಿಸಿದರು":
                    val = "Declined to answer";
                    break;
                default:
                    return val;
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            switch (checkbox_text) {
                case "उत्तर देण्यास नकार दिला":
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
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            val = switch_kn_en_vaccination(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {    // vaccin que is removed so marathi not added.
            val = switch_mr_en_vaccination(val);
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
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            val = switch_kn_en_occupation(val);
            val = switch_kn_en_bankaccount(val);
            val = switch_kn_en_mobile(val);
            val = switch_kn_en_whatsapp(val);
            val = switch_kn_en_sourcewater(val);
            val = switch_kn_en_watersafe(val);
            val = switch_kn_en_wateravail(val);
            val = switch_kn_en_toiletfacil(val);
            val = switch_kn_en_housestructure(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            val = switch_mr_en_occupation(val);
            val = switch_mr_en_bankaccount(val);
            val = switch_mr_en_mobile(val);
            val = switch_mr_en_whatsapp(val);
            val = switch_mr_en_sourcewater(val);
            val = switch_mr_en_watersafe(val);
            val = switch_mr_en_wateravail(val);
            val = switch_mr_en_toiletfacil(val);
            val = switch_mr_en_housestructure(val);
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
    public static String switch_kn_housestructure_edit(String val) {
        switch (val) {
            case "Kutcha House":
                val = "ಕಚ್ಚಾ ಮನೆ";
                break;
            case "Pakka House":
                val = "ಪಕ್ಕಾ ಮನೆ";
                break;
            case "Homeless":
                val = "ಮನೆಯಿಲ್ಲದವರು";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_mr_housestructure_edit(String val) {
        switch (val) {
            case "Kutcha House":
                val = "कच्चा घर";
                break;
            case "Pakka House":
                val = "पक्का घर";
                break;
            case "Homeless":
                val = "बेघर";
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
    public static String switch_kn_en_housestructure(String val) {
        switch (val) {
            case "ಕಚ್ಚಾ ಮನೆ":
                val = "Kutcha House";
                break;
            case "ಪಕ್ಕಾ ಮನೆ":
                val = "Pakka House";
                break;
            case "ಮನೆಯಿಲ್ಲದವರು":
                val = "Homeless";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_housestructure(String val) {
        switch (val) {
            case "कच्चा घर":
                val = "Kutcha House";
                break;
            case "पक्का घर":
                val = "Pakka House";
                break;
            case "बेघर":
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
            case "Open Defacation":
                val = "खुले में शौच";
                break;
            case "Own a toilet but it is unusable":
                val = "खुद का शौचालय है लेकिन वह अनुपयोगी है";
                break;
            case "Use toilet at home":
                val = "घर में शौचालय का प्रयोग करते है";
                break;
            case "Use Community Toilet":
                val = "सामुदायिक शौचालय का प्रयोग करते है";
                break;
            case "Shared Toilet with other households":
                val = "अन्य घरों के साथ साझा शौचालय है";
                break;
            case "Declined to answer":
                val = "जवाब देने से मना कर दिया";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_toiletfacil_edit(String val) {
        switch (val) {
            case "Open Defacation":
                val = "খোলা মলত্যাগ";
                break;
            case "Own a toilet but it is unusable":
                val = "একটি টয়লেটের মালিক কিন্তু এটি ব্যবহারের অনুপযোগী";
                break;
            case "Use toilet at home":
                val = "বাড়িতে টয়লেট ব্যবহার করুন।";
                break;
            case "Use Community Toilet":
                val = "কমিউনিটি টয়লেট ব্যবহার করুন";
                break;
            case "Shared Toilet with other households":
                val = "অন্যান্য পরিবারের সাথে শেয়ারড টয়লেট";
                break;
            case "Declined to answer":
                val = "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_kn_toiletfacil_edit(String val) {
        switch (val) {
            case "Open Defacation":
                val = "ಬಯಲು ಮಲವಿಸರ್ಜನೆ";
                break;
            case "Own a toilet but it is unusable":
                val = "ಸ್ವಂತ ಶೌಚಾಲಯ ಹೊಂದಿದ್ದರೂ ಅದನ್ನು ಬಳಸಲಾಗುವುದಿಲ್ಲ";
                break;
            case "Use toilet at home":
                val = "ಮನೆಯಲ್ಲಿ ಶೌಚಾಲಯವನ್ನು ಬಳಸಿ.";
                break;
            case "Use Community Toilet":
                val = "ಸಮುದಾಯ ಶೌಚಾಲಯ ಬಳಸಿ";
                break;
            case "Shared Toilet with other households":
                val = "ಇತರ ಮನೆಗಳೊಂದಿಗೆ ಹಂಚಿದ ಶೌಚಾಲಯ";
                break;
            case "Declined to answer":
                val = "ಉತ್ತರಿಸಲು ನಿರಾಕರಿಸಿದರು";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_toiletfacil_edit(String val) {
        switch (val) {
            case "Open Defacation":
                val = "उघड्यावर शौच";
                break;
            case "Own a toilet but it is unusable":
                val = "स्वतःचे शौचालय आहे पण ते निरुपयोगी आहे";
                break;
            case "Use toilet at home":
                val = "घरात शौचालय वापरा";
                break;
            case "Use Community Toilet":
                val = "सामुदायिक शौचालय वापरा";
                break;
            case "Shared Toilet with other households":
                val = "इतर कुटुंबांसह सामायिक शौचालय";
                break;
            case "Declined to answer":
                val = "उत्तर देण्यास नकार दिला";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_toiletfacil_edit(String val) {
        switch (val) {
            case "Open Defacation":
                val = "ମୁକ୍ତ ଜାଗା";
                break;
            case "Own a toilet but it is unusable":
                val = "ନିଜର ପାଇଖାନା କିନ୍ତୁ ବ୍ୟବହାର କରା ଯାଏନି";
                break;
            case "Use toilet at home":
                val = "ଘରେ ପାଇଖାନା ରବ୍ୟବହାର";
                break;
            case "Use Community Toilet":
                val = "ସର୍ବ ସାଧାରଣ ପାଇଖାନା ର ବ୍ୟବହାର";
                break;
            case "Shared Toilet with other households":
                val = "ଅନ୍ୟ ଘର ର ପାଇଖାନା ର ବ୍ୟବହାର";
                break;
            case "Declined to answer":
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କଲେ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_toiletfacil_edit(String val) {
        switch (val) {
            case "Open Defacation":
                val = "ઓપન ડીફેકેશન";//-------replace with gujrati
                break;
            case "Own a toilet but it is unusable":
                val = "શૌચાલયની માલિકી છે પરંતુ તે બિનઉપયોગી છે";
                break;
            case "Use toilet at home":
                val = "ઘરમાં શૌચાલયનો ઉપયોગ કરો.";
                break;
            case "Use Community Toilet":
                val = "સામુદાયિક શૌચાલયનો ઉપયોગ કરો";
                break;
            case "Shared Toilet with other households":
                val = "અન્ય ઘરો સાથે વહેંચાયેલ શૌચાલય";
                break;
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_toiletfacil_edit(String val) {
        switch (val) {
            case "Open Defacation":
                val = "মুকলিকৈ শৌচ কৰা";
                break;
            case "Own a toilet but it is unusable":
                val = "শৌচাগাৰৰ মালিক হওক কিন্তু ই ব্যৱহাৰ কৰিব নোৱাৰা";
                break;
            case "Use toilet at home":
                val = "ঘৰতে শৌচাগাৰ ব্যৱহাৰ কৰক।";
                break;
            case "Use Community Toilet":
                val = "সামূহিক শৌচাগাৰ ব্যৱহাৰ কৰক";
                break;
            case "Shared Toilet with other households":
                val = "আন ঘৰৰ সৈতে শ্বেয়াৰ টয়লেট";
                break;
            case "Declined to answer":
                val = "જવાબ આપવાનો ઇનકાર કર્યો";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_toiletfacil(String val) {
        switch (val) {
            case "खुले में शौच":
                val = "Open Defacation";
                break;
            case "खुद का शौचालय है लेकिन वह अनुपयोगी है":
                val = "Own a toilet but it is unusable";
                break;
            case "घर में शौचालय का प्रयोग करते है":
                val = "Use toilet at home";
                break;
            case "सामुदायिक शौचालय का प्रयोग करते है":
                val = "Use Community Toilet";
                break;
            case "अन्य घरों के साथ साझा शौचालय है":
                val = "Shared Toilet with other households";
                break;
            case "जवाब देने से मना कर दिया":
                val = "Declined to answer";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_bn_en_toiletfacil(String val) {
        switch (val) {
            case "খোলা মলত্যাগ":
                val = "Open Defacation";
                break;
            case "একটি টয়লেটের মালিক কিন্তু এটি ব্যবহারের অনুপযোগী":
                val = "Own a toilet but it is unusable";
                break;
            case "বাড়িতে টয়লেট ব্যবহার করুন।":
                val = "Use toilet at home";
                break;
            case "কমিউনিটি টয়লেট ব্যবহার করুন":
                val = "Use Community Toilet";
                break;
            case "অন্যান্য পরিবারের সাথে শেয়ারড টয়লেট":
                val = "Shared Toilet with other households";
                break;
            case "উত্তৰ দিবলৈ অস্বীকাৰ কৰিলে":
                val = "Declined to answer";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_kn_en_toiletfacil(String val) {
        switch (val) {
            case "ಬಯಲು ಮಲವಿಸರ್ಜನೆ":
                val = "Open Defacation";
                break;
            case "ಸ್ವಂತ ಶೌಚಾಲಯ ಹೊಂದಿದ್ದರೂ ಅದನ್ನು ಬಳಸಲಾಗುವುದಿಲ್ಲ":
                val = "Own a toilet but it is unusable";
                break;
            case "ಮನೆಯಲ್ಲಿ ಶೌಚಾಲಯವನ್ನು ಬಳಸಿ.":
                val = "Use toilet at home";
                break;
            case "ಸಮುದಾಯ ಶೌಚಾಲಯ ಬಳಸಿ":
                val = "Use Community Toilet";
                break;
            case "ಇತರ ಮನೆಗಳೊಂದಿಗೆ ಹಂಚಿದ ಶೌಚಾಲಯ":
                val = "Shared Toilet with other households";
                break;
            case "ಉತ್ತರಿಸಲು ನಿರಾಕರಿಸಿದರು":
                val = "Declined to answer";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_toiletfacil(String val) {
        switch (val) {
            case "उघड्यावर शौच":
                val = "Open Defacation";
                break;
            case "स्वतःचे शौचालय आहे पण ते निरुपयोगी आहे":
                val = "Own a toilet but it is unusable";
                break;
            case "घरात शौचालय वापरा":
                val = "Use toilet at home";
                break;
            case "सामुदायिक शौचालय वापरा":
                val = "Use Community Toilet";
                break;
            case "इतर कुटुंबांसह सामायिक शौचालय":
                val = "Shared Toilet with other households";
                break;
            case "उत्तर देण्यास नकार दिला":
                val = "Declined to answer";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_toiletfacil(String val) {
        switch (val) {
            case "ମୁକ୍ତ ଜାଗା":
                val = "Open Defacation";
                break;
            case "ନିଜର ପାଇଖାନା କିନ୍ତୁ ବ୍ୟବହାର କରା ଯାଏନି":
                val = "Own a toilet but it is unusable";
                break;
            case "ଘରେ ପାଇଖାନା ରବ୍ୟବହାର":
                val = "Use toilet at home";
                break;
            case "ସର୍ବ ସାଧାରଣ ପାଇଖାନା ର ବ୍ୟବହାର":
                val = "Use Community Toilet";
                break;
            case "ଅନ୍ୟ ଘର ର ପାଇଖାନା ର ବ୍ୟବହାର":
                val = "Shared Toilet with other households";
                break;
            case "ଉତ୍ତର ଦେବାକୁ ମନା କଲେ":
                val = "Declined to answer";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_en_toiletfacil(String val) {
        switch (val) {
            case "ઓપન ડીફેકેશન"://-------replace with gujrati
                val = "Open Defacation";
                break;
            case "શૌચાલયની માલિકી છે પરંતુ તે બિનઉપયોગી છે":
                val = "Own a toilet but it is unusable";
                break;
            case "ઘરમાં શૌચાલયનો ઉપયોગ કરો.":
                val = "Use toilet at home";
                break;
            case "સામુદાયિક શૌચાલયનો ઉપયોગ કરો":
                val = "Use Community Toilet";
                break;
            case "અન્ય ઘરો સાથે વહેંચાયેલ શૌચાલય":
                val = "Shared Toilet with other households";
                break;
           /* case "જવાબ આપવાનો ઇનકાર કર્યો":
                val = "Declined to answer";
                break;*/
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_en_toiletfacil(String val) {
        switch (val) {
            case "মুকলিকৈ শৌচ কৰা"://-------replace with Assamese
                val = "Open Defacation";
                break;
            case "শৌচাগাৰৰ মালিক হওক কিন্তু ই ব্যৱহাৰ কৰিব নোৱাৰা":
                val = "Own a toilet but it is unusable";
                break;
            case "ঘৰতে শৌচাগাৰ ব্যৱহাৰ কৰক।":
                val = "Use toilet at home";
                break;
            case "সামূহিক শৌচাগাৰ ব্যৱহাৰ কৰক":
                val = "Use Community Toilet";
                break;
            case "আন ঘৰৰ সৈতে শ্বেয়াৰ টয়লেট":
                val = "Shared Toilet with other households";
                break;
            case "જવાબ આપવાનો ઇનકાર કર્યો":
                val = "Declined to answer";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_en_wateravail(String val) {
        switch (val) {
            case "खुले में शौच":
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
    public static String switch_kn_en_wateravail(String val) {
        switch (val) {
            case "ಉತ್ತರಿಸಲು ನಿರಾಕರಿಸಿದರು":
                val = "Declined to answer";
                break;
            case "ಹೌದು":
                val = "Yes";
                break;
            case "ಅಲ್ಲ":
                val = "No";
                break;
            case "ಗೊತ್ತಿಲ್ಲ":
                val = "Don\'t know";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_wateravail(String val) {
        switch (val) {
            case "उत्तर देण्यास नकार दिला":
                val = "Declined to answer";
                break;
            case "होय":
                val = "Yes";
                break;
            case "नाही":
                val = "No";
                break;
            case "माहीत नाही":
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
                val = "Boiling";
                break;
            case "फिटकरी  का उपयोग करें":
                val = "Use Alum";
                break;
            case "ब्लीच डालते हैं":
                val = "Add bleach";
                break;
            case "कपड़े से छानते है":
                val = "Strain Through A Cloth";
                break;
            case "पानी फिल्टर का उपयोग करते है":
                val = "Water Filter";
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
                val = "Boiling";
                break;
            case "অ্যালাম ব্যবহার করুন":
                val = "Use Alum";
                break;
            case "ব্লিচ যোগ করুন":
                val = "Add bleach";
                break;
            case "একটি কাপড় মাধ্যমে স্ট্রেন":
                val = "Strain Through A Cloth";
                break;
            case "জল ফিল্টার":
                val = "Water Filter";
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
    public static String switch_kn_en_watersafe(String val) {
        switch (val) {
            case "ಕುದಿಸಿ"://----replace with Hindi
                val = "Boiling";
                break;
            case "ಹರಳೆಣ್ಣೆ ಬಳಸಿ":
                val = "Use Alum";
                break;
            case "ಬ್ಲೀಚ್ ಸೇರಿಸಿ":
                val = "Add bleach";
                break;
            case "ಒಂದು ಬಟ್ಟೆಯ ಮೂಲಕ ಸ್ಟ್ರೈನ್ ಮಾಡಿ":
                val = "Strain Through A Cloth";
                break;
            case "ವಾಟರ್ ಫಿಲ್ಟರ್ ಬಳಸಿ":
                val = "Water Filter";
                break;
           /* case "ইলেকট্রনিক পিউরিফায়ার ব্যবহার করুন":
                val = "Use Electronic Purifier";
                break;
            case "লেট ইট স্ট্যান্ড অ্যান্ড সেটেল":
                val = "Let It Stand And Settle";
                break;*/
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_watersafe(String val) {
        switch (val) {
            case "उकळणे"://----replace with Hindi
                val = "Boiling";
                break;
            case "तुरटी वापरणे":
                val = "Use Alum";
                break;
            case "ब्लीच / क्लोरीन गोळ्या/ ड्रॉप्स":
                val = "Add bleach";
                break;
            case "कापडाने गाळणे":
                val = "Strain Through A Cloth";
                break;
            case "पाणी फिल्टर वापरा (सिरेमिक / वाळू / संयुक्त / इ.)":
                val = "Water Filter";
                break;
           /* case "ইলেকট্রনিক পিউরিফায়ার ব্যবহার করুন":
                val = "Use Electronic Purifier";
                break;
            case "লেট ইট স্ট্যান্ড অ্যান্ড সেটেল":
                val = "Let It Stand And Settle";
                break;*/
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_en_watersafe(String val) {
        switch (val) {
            case "ଫୁଟାପାଣି"://----replace with Oriya
                val = "Boiling";
                break;
            case "ଆଲମ ବ୍ୟବହାର":
                val = "Use Alum";
                break;
            case "ବିରଞ୍ଜନ ଯୁକ୍ତ କରିବା":
                val = "Add bleach";
                break;
            case "କପଡ଼ାରେ ଛ।ନିବା":
                val = "Strain Through A Cloth";
                break;
            case "ଫିଲ୍ଟର ପାଣି":
                val = "Water Filter";
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
                val = "Boiling";
                break;
            case "ફટકડીનો ઉપયોગ કરો":
                val = "Use Alum";
                break;
            case "બ્લિચ ઉમેરવું":
                val = "Add bleach";
                break;
            case "કાપડ દ્વારા તાણ":
                val = "Strain Through A Cloth";
                break;
            case "વોટર ફિલ્ટરનો ઉપયોગ":
                val = "Water Filter";
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
                val = "Boiling";
                break;
            case "এলুম ব্যৱহাৰ কৰক":
                val = "Use Alum";
                break;
            case "ব্লিচ যোগ কৰক":
                val = "Add bleach";
                break;
            case "ষ্ট্ৰেইন থ্ৰু এ ক্লথ":
                val = "Strain Through A Cloth";
                break;
            case "পানী ফিল্টাৰ":
                val = "Water Filter";
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
    public static String switch_kn_en_sourcewater(String val) {
        switch (val) {
            case "ವಾಸಕ್ಕೆ ಪೈಪ್ ಹಾಕಲಾಗಿದೆ"://-------replace with hindi
                val = "Piped Into Dwelling";
                break;
            case "ಯಾರ್ಡ್ ಅಥವಾ ಪ್ಲಾಟ್ಗೆ ಪೈಪ್ ಹಾಕಲಾಗಿದೆ":
                val = "Piped Into Yard or Plot";
                break;
            case "ಸಾರ್ವಜನಿಕ ಟ್ಯಾಪ್ ಅಥವಾ ಸ್ಟ್ಯಾಂಡ್ ಪೈಪ್":
                val = "Public Tap or Standpipe";
                break;
            case "ಕೊಳವೆ ಬಾವಿ ಅಥವಾ ಕೊಳವೆಬಾವಿ":
                val = "Tube Well or Borehole";
                break;
            case "ಚೆನ್ನಾಗಿ ರಕ್ಷಿಸಲಾಗಿದೆ":
                val = "Protected Well";
                break;
            case "ರಕ್ಷಣೆಯಿಲ್ಲದ ಬಾವಿ":
                val = "Unprotected Well";
                break;
            case "ರಕ್ಷಿತ ವಸಂತ":
                val = "Protected Spring";
                break;
            case "ಅಸುರಕ್ಷಿತ ವಸಂತ":
                val = "Unprotected Spring";
                break;
            case "ಮಳೆನೀರು":
                val = "Rainwater";
                break;
            case "ಟ್ಯಾಂಕರ್ ಟ್ರಕ್":
                val = "Tanker Truck";
                break;
            case "ಸಣ್ಣ ಟ್ರಕ್ ಜೊತೆ ಕಾರ್ಟ್":
                val = "Cart With Small Truck";
                break;
            case "ಮೇಲ್ಮೈ ನೀರು":
                val = "Surface Water";
                break;
            case "ಸಾಮಾನ್ಯ ಕೈ ಪಂಪ್":
                val = "Common Hand Pump";
                break;
            case "ಮನೆಯಲ್ಲಿ ಕೈ ಪಂಪ್":
                val = "Hand Pump At Home";
                break;
            case "ನಲ್ ಜಲ ಯೋಜನೆ":
                val = "Nal Jal Yojana";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_sourcewater(String val) {
        switch (val) {
            case "घरामध्येच पाइपद्वारे"://-------replace with hindi
                val = "Piped Into Dwelling";
                break;
            case "अंगणात किंवा आसपास परिसरात पाइपद्वारे":
                val = "Piped Into Yard or Plot";
                break;
            case "सार्वजनिक नळ / स्टँडपाइप":
                val = "Public Tap or Standpipe";
                break;
            case "कूपनलिका / विंधन विहीर / बोअरवेल":
                val = "Tube Well or Borehole";
                break;
            case "संरक्षित विहिर":
                val = "Protected Well";
                break;
            case "असंरक्षित विहिर":
                val = "Unprotected Well";
                break;
            case "संरक्षित झऱ्याचे पाणीी":
                val = "Protected Spring";
                break;
            case "असंरक्षित झऱ्याचे पाणी":
                val = "Unprotected Spring";
                break;
            case "पावसाचे पाणी":
                val = "Rainwater";
                break;
            case "टँकर ट्रक":
                val = "Tanker Truck";
                break;
            case "लहान टाकी-गाडी":
                val = "Cart With Small Truck";
                break;
            case "पृष्ठभागाचे पाणी":
                val = "Surface Water";
                break;
            case "सामान्य हात पंप/ हापसा":
                val = "Common Hand Pump";
                break;
            case " हँड पंप (घरी)":
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
    public static String switch_kn_en_occupation(String val) {
        switch (val) {
            case "ಸಂಬಳದ ಸರ್ಕಾರಿ ಕೆಲಸ"://----------replace with Hindi
                val = "Salaried Government Job";
                break;
            case "ಸಂಬಳದ ಖಾಸಗಿ ಕೆಲಸ":
                val = "Salaried Private Job";
                break;
            case "ಸಣ್ಣ ವ್ಯಾಪಾರ ಅಥವಾ ಅಂಗಡಿ ಮಾಲೀಕರು":
                val = "Petty Business or Shop Owner";
                break;
            case "ತಂತ್ರಜ್ಞ ಅಥವಾ ಕುಶಲಕರ್ಮಿ ಅಥವಾ ಇತರ ನುರಿತ ಕೆಲಸ (ಚಾಲಕ, ಮೇಸನ್, ಇತ್ಯಾದಿ)":
                val = "Technician or Craftsman or Other Skilled Work (Driver, Mason, etc.)";
                break;
            case "ಕೃಷಿ ರೈತ":
                val = "Agricultural Farmer";
                break;
            case "ಗೇಣಿದಾರ ರೈತ ಅಥವಾ ಕೃಷಿ ದೈನಂದಿನ ಕೂಲಿ ಕೆಲಸಗಾರ":
                val = "Tenant Farmer or Agricultural Daily Wage Worker";
                break;
            case "ದಿನಗೂಲಿ ಕೆಲಸಗಾರ (ಕೌಶಲ್ಯವಿಲ್ಲದ ಕೆಲಸಗಾರ)":
                val = "Daily Wage Worker (Unskilled Worker)";
                break;
            case "ಮನೆಯ ಕೆಲಸ":
                val = "Household Work";
                break;
            case "ವಿದ್ಯಾರ್ಥಿ":
                val = "Student";
                break;
            case "ನಿರುದ್ಯೋಗಿ":
                val = "Unemployed";
                break;
            case "ನಿವೃತ್ತಿ (ಪಿಂಚಣಿಯೊಂದಿಗೆ)":
                val = "Retired (With Pension)";
                break;
            case "ನಿವೃತ್ತಿ (ಪಿಂಚಣಿ ಇಲ್ಲದೆ)":
                val = "Retired (Without Pension)";
                break;
            case "ಅನ್ವಯಿಸುವುದಿಲ್ಲ":
                val = "Not Applicable";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_occupation(String val) {
        switch (val) {
            case "पगारदार सरकारी नोकरी"://----------replace with Hindi
                val = "Salaried Government Job";
                break;
            case "पगारदार खाजगी नोकरी":
                val = "Salaried Private Job";
                break;
            case "लहान व्यवसाय / दुकान मालक":
                val = "Petty Business or Shop Owner";
                break;
            case "तंत्रज्ञ किंवा कारागीर किंवा इतर कुशल काम (ड्रायव्हर, मेसन इ.)":
                val = "Technician or Craftsman or Other Skilled Work (Driver, Mason, etc.)";
                break;
            case "कृषी शेतकरी":
                val = "Agricultural Farmer";
                break;
            case "भाडेकरी शेतकरी / कृषी दैनिक वेतन कार्यकर्ता":
                val = "Tenant Farmer or Agricultural Daily Wage Worker";
                break;
            case "रोजंदारी कामगार (अकुशल कामगार)":
                val = "Daily Wage Worker (Unskilled Worker)";
                break;
            case "घरगुती काम":
                val = "Household Work";
                break;
            case "विद्यार्थी":
                val = "Student";
                break;
            case "बेरोजगार":
                val = "Unemployed";
                break;
            case "सेवानिवृत्त (पेंशनसह)":
                val = "Retired (With Pension)";
                break;
            case "सेवानिवृत्त (पेंशनशिवाय)":
                val = "Retired (Without Pension)";
                break;
            case "लागू नाही":
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
            case "खुले में शौच":
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
            case "হ্যাঁ":
                val = "Yes";
                break;
            case "না":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_kn_en_bankaccount(String val) {
        switch (val) {
            case "ಉತ್ತರಿಸಲು ನಿರಾಕರಿಸಿದರು":
                val = "Declined to answer";
                break;
            case "ಹೌದು":
                val = "Yes";
                break;
            case "ಅಲ್ಲ":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_bankaccount(String val) {
        switch (val) {
            case "उत्तर देण्यास नकार दिला":
                val = "Declined to answer";
                break;
            case "होय":
                val = "Yes";
                break;
            case "नाही":
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
    public static String switch_kn_en_mobile(String val) {
        switch (val) {
            case "ಯಾವುದೂ ಇಲ್ಲ":///-------replace with Hindi
                val = "None";
                break;
            case "ಸ್ಮಾರ್ಟ್ಫೋನ್ ಹೊಂದಿದ್ದಾರೆ":
                val = "Own Smartphone";
                break;
            case "ಫೀಚರ್ ಫೋನ್ ಅನ್ನು ಹೊಂದಿದೆ":
                val = "Own Feature Phone";
                break;
            case "ಹಂಚಿದ ಸ್ಮಾರ್ಟ್ಫೋನ್":
                val = "Shared Smartphone";
                break;
            case "ಹಂಚಿದ ವೈಶಿಷ್ಟ್ಯ ಫೋನ್":
                val = "Shared Feature Phone";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_mobile(String val) {
        switch (val) {
            case "काहीही नाही":///-------replace with Hindi
                val = "None";
                break;
            case "स्वतःचा स्मार्टफोन":
                val = "Own Smartphone";
                break;
            case "स्वतःचा साधारण फोन":
                val = "Own Feature Phone";
                break;
            case "सामायिक स्मार्टफोन":
                val = "Shared Smartphone";
                break;
            case "सामायिक साधारण फोन":
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
    public static String switch_kn_en_whatsapp(String val) {
        switch (val) {
            case "ಹೌದು (ಕುಟುಂಬದ ಸದಸ್ಯ)"://------replace with Hindi
                val = "Yes (Family Member)";
                break;
            case "ಹೌದು (ವೈಯಕ್ತಿಕ)":
                val = "Yes (Personal)";
                break;
            case "ಅಲ್ಲ":
                val = "No";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_en_whatsapp(String val) {
        switch (val) {
            case "होय (कुटुंब सदस्य)"://------replace with Hindi
                val = "Yes (Family Member)";
                break;
            case "होय (वैयक्तिक)":
                val = "Yes (Personal)";
                break;
            case "नाही":
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
    public static String switch_kn_education(String val) {
        switch (val) {
            case "ಓದಲು ಅಥವಾ ಬರೆಯಲು ಬರುವುದಿಲ್ಲ"://------
                val = "Cannot read or write";
                break;
            case "ಓದಲು ಮತ್ತು ಬರೆಯಲು ಸಾಧ್ಯವಾಗುತ್ತದೆ":
                val = "Can read and write";
                break;
            case "ಪ್ರಾಥಮಿಕ ಶಿಕ್ಷಣ (5  ನೇ ತರಗತಿಯವರೆಗೆ)":
                val = "Primary education (Till Class 5)";
                break;
            case "ಮಧ್ಯಮ ಶಾಲೆ ( 6ನೇ - 8ನೇ)":
                val = "Middle school (6th - 8th)";
                break;
            case "ಮಾಧ್ಯಮಿಕ ಶಾಲೆಯಲ್ಲಿ ಉತ್ತೀರ್ಣರಾಗಿದ್ದಾರೆ (10 ನೇ ತರಗತಿ ಬೋರ್ಡ್ಗಳು)":
                val = "Passed secondary school (Class 10th boards)";
                break;
            case "ಹಿರಿಯ ಮಾಧ್ಯಮಿಕ ಶಾಲೆಯಲ್ಲಿ ಉತ್ತೀರ್ಣರಾಗಿದ್ದಾರೆ (12 ನೇ ತರಗತಿ ಮಂಡಳಿಗಳು)":
                val = "Passed senior secondary school (Class 12th boards)";
                break;
            case "ಪದವಿಧರ":
                val = "Graduate";
                break;
            case "ಸ್ನಾತಕೋತ್ತರ ಪದವಿ":
                val = "Postgraduate";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_education(String val) {
        switch (val) {
            case "लिहिता-वाचता येत नाही"://------
                val = "Cannot read or write";
                break;
            case "वाचू आणि लिहू शकतो":
                val = "Can read and write";
                break;
            case "प्राथमिक शिक्षण (वर्ग 5 पर्यंत)":
                val = "Primary education (Till Class 5)";
                break;
            case "माध्यमिक शाळा (६ वी ते ८ वी)":
                val = "Middle school (6th - 8th)";
                break;
            case "माध्यमिक शाळा उत्तीर्ण (इयत्ता 10वी बोर्ड)":
                val = "Passed secondary school (Class 10th boards)";
                break;
            case "उच्च माध्यमिक शाळा (इयत्ता 12वी बोर्ड) उत्तीर्ण":
                val = "Passed senior secondary school (Class 12th boards)";
                break;
            case "पदवीधर":
                val = "Graduate";
                break;
            case "पदव्युत्तर शिक्षण":
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
    public static String switch_kn_economic(String val) {
        switch (val) {
            case "ಎಪಿಎಲ್":
                val = "APL";
                break;
            case "ಬಿಪಿಎಲ್":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_economic(String val) {
        switch (val) {
            case "दारिद्र्यरेषेच्या वर":
                val = "APL";
                break;
            case "दारिद्र्यरेषेखालील":
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
    public static String switch_kn_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "ಅನಕ್ಷರಸ್ಥ";
                break;
            case "Primary":
                val = "ಪ್ರಾಥಮಿಕ";
                break;
            case "Secondary":
                val = "ದ್ವಿತೀಯ";
                break;
            case "Higher Secondary":
                val = "ಹೈಯರ್ ಸೆಕೆಂಡರಿ";
                break;
            case "Graduation & Higher":
                val = "ಪದವಿ ಮತ್ತು ಉನ್ನತ";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_education_edit(String val) {
        switch (val) {
            case "Illiterate":
                val = "निरक्षर";
                break;
            case "Primary":
                val = "प्राथमिक";
                break;
            case "Secondary":
                val = "दुय्यम";
                break;
            case "Higher Secondary":
                val = "उच्च माध्यमिक";
                break;
            case "Graduation & Higher":
                val = "पदवी आणि उच्च";
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
    public static String switch_kn_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "ಎಪಿಎಲ್";
                break;
            case "BPL":
                val = "ಬಿಪಿಎಲ್";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_economic_edit(String val) {
        switch (val) {
            case "APL":
                val = "दारिद्र्यरेषेच्या वर";
                break;
            case "BPL":
                val = "दारिद्र्यरेषेखालील";
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
            case "স টি":
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
    public static String switch_kn_caste(String val) {
        switch (val) {
            case "ಸಾಮಾನ್ಯ":
                val = "General";
                break;
            case "ಒಬಿಸಿ":
                val = "OBC";
                break;
            case "ಎಸ್ಸಿ":
                val = "SC";
                break;
            case "ಎಸ್ಟಿ":
                val = "ST";
                break;
            case "ಇತರರು":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_caste(String val) {
        switch (val) {
            case "सामान्य":
                val = "General";
                break;
            case "इतर मागास जाती (OBC)":
                val = "OBC";
                break;
            case "अनुसूचित जाती":
                val = "SC";
                break;
            case "अनुसूचित जमाती":
                val = "ST";
                break;
            case "इतर":
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
                val = "खुले में शौच";
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
    public static String switch_kn_bankaccount_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "ಉತ್ತರಿಸಲು ನಿರಾಕರಿಸಿದರು";
                break;
            case "Yes":
                val = "ಹೌದು";
                break;
            case "No":
                val = "ಅಲ್ಲ";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_bankaccount_edit(String val) {
        switch (val) {
            case "Declined to answer":
                val = "उत्तर देण्यास नकार दिला";
                break;
            case "Yes":
                val = "होय";
                break;
            case "No":
                val = "नाही";
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
                val = "खुले में शौच";
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
                val = "खुले में शौच";
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
                val = "खुले में शौच";
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
                val = "जवाब देने से मना कर दिया";
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
                val = "ଉତ୍ତର ଦେବାକୁ ମନା କଲେ";
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
    public static String switch_kn_en_vaccination(String val) {
        switch (val) {
            case "ಮೊದಲ ಡೋಸ್":
                val = "First dose";
                break;
            case "ಎರಡನೇ ಡೋಸ್":
                val = "Second dose";
                break;
            case "ಮೂರನೇ ಡೋಸ್":
                val = "Third dose";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_mr_en_vaccination(String val) {
        switch (val) {
            case "पहिला डोस":
                val = "First dose";
                break;
            case "दुसरा डोस":
                val = "Second dose";
                break;
            case "तिसरा डोस":
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
    public static String switch_kn_vaccination_edit(String val) {
        switch (val) {
            case "First dose":
                val = "ಮೊದಲ ಡೋಸ್";
                break;
            case "Second dose":
                val = "ಎರಡನೇ ಡೋಸ್";
                break;
            case "Third dose":
                val = "ಮೂರನೇ ಡೋಸ್";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_vaccination_edit(String val) {
        switch (val) {
            case "First dose":
                val = "पहिला डोस";
                break;
            case "Second dose":
                val = "दुसरा डोस";
                break;
            case "Third dose":
                val = "तिसरा डोस";
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
                val = "खुले में शौच";
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
                val = "স টি";
                break;
            case "others":
                val = "অন্যান্য";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_kn_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "ಸಾಮಾನ್ಯ";
                break;
            case "OBC":
                val = "ಒಬಿಸಿ";
                break;
            case "SC":
                val = "ಎಸ್ಸಿ";
                break;
            case "ST":
                val = "ಎಸ್ಟಿ";
                break;
            case "others":
                val = "ಇತರರು";
                break;
            default:
                return val;
        }
        return val;
    }
    public static String switch_mr_caste_edit(String val) {
        switch (val) {
            case "General":
                val = "सामान्य";
                break;
            case "OBC":
                val = "इतर मागास जाती (OBC)";
                break;
            case "SC":
                val = "अनुसूचित जाती";
                break;
            case "ST":
                val = "अनुसूचित जमाती";
                break;
            case "others":
                val = "इतर";
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
    public static String en__kn_dob(String dob) { //English dob is replaced to Hindi text.
        String mdob_text = dob
                .replace("January", "ಜನವರಿ")
                .replace("February", "ಫೆಬ್ರವರಿ")
                .replace("March", "ಮಾರ್ಚ್")
                .replace("April", "ಎಪ್ರಿಲ್")
                .replace("May", "ಮೇ")
                .replace("June", "ಜೂನ್")
                .replace("July", "ಜುಲೈ")
                .replace("August", "ಆಗಸ್ಟ್")
                .replace("September", "ಸೆಪ್ಟೆಂಬರ್")
                .replace("October", "ಅಕ್ಟೋಬರ್")
                .replace("November", "ನವೆಂಬರ್")
                .replace("December", "ಡಿಸೆಂಬರ್");

        return mdob_text;
    }
    public static String en__mr_dob(String dob) { //English dob is replaced to Hindi text.
        String mdob_text = dob
                .replace("January", "जानेवारी")
                .replace("February", "फेब्रुवारी")
                .replace("March", "मार्च")
                .replace("April", "एप्रिल")
                .replace("May", "मे")
                .replace("June", "जून")
                .replace("July", "जुलै")
                .replace("August", "ऑगस्ट")
                .replace("September", "सप्टेंबर")
                .replace("October", "ऑक्टोबर")
                .replace("November", "नोव्हेंबर")
                .replace("December", "डिसेंबर");

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
        }  else if (locale.equalsIgnoreCase("kn")) {
            //Odiya
            String dob = dobString
                    .replace("ಜನವರಿ", "January")
                    .replace("ಫೆಬ್ರವರಿ", "February")
                    .replace("ಮಾರ್ಚ್", "March")
                    .replace("ಎಪ್ರಿಲ್", "April")//ଅପ୍ରେଲ
                    .replace("ಮೇ", "May")//ମଇ
                    .replace("ಜೂನ್", "June")
                    .replace("ಜುಲೈ", "July")
                    .replace("ಆಗಸ್ಟ್", "August")
                    .replace("ಸೆಪ್ಟೆಂಬರ್", "September")
                    .replace("ಅಕ್ಟೋಬರ್", "October")
                    .replace("ನವೆಂಬರ್", "November")
                    .replace("ಡಿಸೆಂಬರ್", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("mr")) {
            //Odiya
            String dob = dobString
                    .replace("जानेवारी", "January")
                    .replace("फेब्रुवारी", "February")
                    .replace("मार्च", "March")
                    .replace("एप्रिल", "April")//ଅପ୍ରେଲ
                    .replace("मे", "May")//ମଇ
                    .replace("जून", "June")
                    .replace("जुलै", "July")
                    .replace("ऑगस्ट", "August")
                    .replace("सप्टेंबर", "September")
                    .replace("ऑक्टोबर", "October")
                    .replace("नोव्हेंबर", "November")
                    .replace("डिसेंबर", "December");
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
        if (locale.equalsIgnoreCase("kn")) {

            switch (val) {
                case "Sunday":
                    val = "ಭಾನುವಾರ";
                    break;
                case "Monday":
                    val = "ಸೋಮವಾರ";
                    break;
                case "Tuesday":
                    val = "ಮಂಗಳವಾರ";
                    break;
                case "Wednesday":
                    val = "ಬುಧವಾರ";
                    break;
                case "Thursday":
                    val = "ಗುರುವಾರ";
                    break;
                case "Friday":
                    val = "ಶುಕ್ರವಾರ";
                    break;
                case "Saturday":
                    val = "ಶನಿವಾರ";
                    break;
                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("mr")) {

            switch (val) {
                case "Sunday":
                    val = "रविवार";
                    break;
                case "Monday":
                    val = "सोमवार";
                    break;
                case "Tuesday":
                    val = "मंगळवार";
                    break;
                case "Wednesday":
                    val = "बुधवार";
                    break;
                case "Thursday":
                    val = "गुरुवार";
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
        if (locale.equalsIgnoreCase("bn")) {

            switch (val) {
                case "Sunday":
                    val = "রবিবার";
                    break;
                case "Monday":
                    val = "সোমবার";
                    break;
                case "Tuesday":
                    val = "মঙ্গলবার";
                    break;
                case "Wednesday":
                    val = "বুধবার";
                    break;
                case "Thursday":
                    val = "বৃহস্পতিবার";
                    break;
                case "Friday":
                    val = "শুক্রবার";
                    break;
                case "Saturday":
                    val = "শনিবার";
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
        if (locale.equalsIgnoreCase("bn")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "বুক করা";
                    break;
                case "cancelled":
                    val = "বাতিল";
                    break;
                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("kn")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "ಬುಕ್ ಮಾಡಿದೆ";
                    break;
                case "cancelled":
                    val = "ರದ್ದುಗೊಳಿಸಲಾಗಿದೆ";
                    break;
                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("mr")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "बुक केले";
                    break;
                case "cancelled":
                    val = "रद्द केले";
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
                String a = getRadioButtonStrings(radioButton.getText().toString(), updatedContext, context, locale);
                String b = text;
                if (text.equalsIgnoreCase(getRadioButtonStrings(radioButton.getText().toString(), updatedContext, context, locale))) {
                    radioButton.setChecked(true);
                    break;
                }
                if (text.equalsIgnoreCase(getSmokingHistoryStrings(radioButton.getText().toString(), updatedContext, context, locale))) {
                    radioButton.setChecked(true);
                    break;
                }
                if (text.equalsIgnoreCase(getTobaccoHistoryStrings(radioButton.getText().toString(), updatedContext, context, locale))) {
                    radioButton.setChecked(true);
                    break;
                }
                if (text.equalsIgnoreCase(getAlcoholHistory(radioButton.getText().toString(), updatedContext, context, locale))) {
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

    public static String getReligionStrings(String text, Context inputContext, Context outputContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.

     //   if (!locale.equalsIgnoreCase("en")) {
        //    if (!inputContext.getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("en")) {
            // Translate string Hindu to English
            if (inputContext.getString(R.string.hindu).equalsIgnoreCase(text)) {
                String v = outputContext.getString(R.string.hindu);
                Log.v("King", "King: " + v);
                return outputContext.getString(R.string.hindu);
            }
            // Translate string Muslim to English
            if (inputContext.getString(R.string.muslim).equalsIgnoreCase(text)) {
                return outputContext.getString(R.string.muslim);
            }
            // Translate string Christian to English
            if (inputContext.getString(R.string.christian).equalsIgnoreCase(text)) {
                return outputContext.getString(R.string.christian);
            }
            // Translate string Sikh to English
            if (inputContext.getString(R.string.sikh).equalsIgnoreCase(text)) {
                return outputContext.getString(R.string.sikh);
            }
            // Translate string Buddhist / Neo-Buddhist to English
            if (inputContext.getString(R.string.buddhist_neo_buddhist).equalsIgnoreCase(text)) {
                return outputContext.getString(R.string.buddhist_neo_buddhist);
            }
            // Translate string Jain to English
            if (inputContext.getString(R.string.jain).equalsIgnoreCase(text)) {
                return outputContext.getString(R.string.jain);
            }
          /*  // Translate string Jewish to English
            if (inputContext.getString(R.string.jewish).equalsIgnoreCase(text)) {
                return outputContext.getString(R.string.jewish);
            }
            // Translate string Parsi Zoroastrian to English
            if (inputContext.getString(R.string.parsi_zoroastrian).equalsIgnoreCase(text)) {
                String v = outputContext.getString(R.string.parsi_zoroastrian);
                Log.v("King", "King: " + v);
                return outputContext.getString(R.string.parsi_zoroastrian);
            }
            // Translate string No Religion to English
            if (inputContext.getString(R.string.no_religion).equalsIgnoreCase(text)) {
                return outputContext.getString(R.string.no_religion);
            }*/
        // Translate string Others to English
        if (inputContext.getString(R.string.others).equalsIgnoreCase(text)) {
            return outputContext.getString(R.string.others);
        }
     //   }
        return text;
    }

    public static String getRadioButtonStrings(String text, Context context, Context updatedContext, String locale) {
        String a = text;
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        //  if (!locale.equalsIgnoreCase("en")) {

        // Translate string Yes to English
        if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.generic_yes);
        }
        // Translate string No to English
        if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.generic_no);
        }
        // Translate string Yes.. to English
        if (context.getString(R.string.yes).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.yes);
        }
        // Translate string No.. to English
        if (context.getString(R.string.no).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.no);
        }
        // Translate string Not Sure to English
        if (context.getString(R.string.not_sure).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.not_sure);
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
        //   }
        return text;
    }

    public static String getLandOwnedStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
      //  if (!locale.equalsIgnoreCase("en")) {
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
            // Translate string Landless to English
            if (context.getString(R.string.landless).equalsIgnoreCase(text)) {
               return updatedContext.getString(R.string.landless);
            }
      //  }
        return text;
    }

    public static String getCheckboxesString(String text, Context context, Context updatedContext, String locale) {
        Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));

        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
     //   if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }
            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
            }
            // Translate string Not Sure to English
            if (context.getString(R.string.not_sure).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.not_sure);
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
        // Translate string Others to English
        if (context.getString(R.string.others).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.others);
        }
            /*// Translate string Lantern to English
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
            }*/

        // Translate string Electric to English
        if (context.getString(R.string.lantern_kerosenelamp_candle).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.lantern_kerosenelamp_candle);
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
          /*  // Translate string Nal Jal Yojana to English
            if (context.getString(R.string.nal_jal_yojana).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.nal_jal_yojana);
            }*/

            // Translate string Boil to English
            if (context.getString(R.string.boil).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.boil);
            }
            // Translate string Use Alum to English
            if (context.getString(R.string.use_alum).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.use_alum);
            }
            // Translate string Add bleach to English
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
           /* // Translate string Use Electronic Purifier to English
            if (context.getString(R.string.use_electronic_purifier).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.use_electronic_purifier);
            }
            // Translate string Let It Stand And Settle to English
            if (context.getString(R.string.let_it_stand_and_settle).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.let_it_stand_and_settle);
            }*/
         // Translate string Handmade purifier to English
            if (context.getString(R.string.handmade_purifier).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.handmade_purifier);
            }
            // Translate string Do Nothing to English
            if (context.getString(R.string.do_nothing).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.do_nothing);
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
          /*  // Translate string No Soap Available In The Household to English
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
            }*/

        // Translate string Use Soap to English
        if (context.getString(R.string.use_soap).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.use_soap);
        }
        // Translate string -> Use other means... to English
        if (context.getString(R.string.use_other_means_wash_hands).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.use_other_means_wash_hands);
        }
        // Translate string -> Wash after using... to English
        if (context.getString(R.string.wash_after_using_toilet).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.wash_after_using_toilet);
        }
        // Translate string -> Wash before cooking to English
        if (context.getString(R.string.wash_before_cooking).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.wash_before_cooking);
        }
        // Translate string -> Wash before and after eating to English
        if (context.getString(R.string.wash_before_after_eating).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.wash_before_after_eating);
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
     //   }
        return text;
    }

    public static String getSurveyStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
     //   if (!locale.equalsIgnoreCase("en")) {
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
     //   }
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
     //   if (!locale.equalsIgnoreCase("en")) {
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
      //  }
        return text;
    }

    public static String getMobilePhoneOwnership(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
     //   if (!locale.equalsIgnoreCase("en")) {
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
     //   }
        return text;
    }

    public static String getMaritalStatusStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
     //   if (!locale.equalsIgnoreCase("en")) {
            // Translate string Currently Married to English
            if (context.getString(R.string.currently_married).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.currently_married);
            }
//            // Translate string Married, Gauna Not Performed to English
//            if (context.getString(R.string.married_gauna_not_performed).equalsIgnoreCase(text)) {
//                return updatedContext.getString(R.string.married_gauna_not_performed);
//            }

        // Translate string Married to English
        if (context.getString(R.string.married).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.married);
        }
        // Translate string Unmarried to English
        if (context.getString(R.string.unmarried).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.unmarried);
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
     //   }
        return text;
    }

    // Strings for BP Checked, HB Checked, Sugar Checked, and BMI Checked
    public static String getTestStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        //   if (!locale.equalsIgnoreCase("en")) {
            // Translate string Never Checked to English
            if (context.getString(R.string.never_checked).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.never_checked);
            }
            // Translate string Within Last Month to English
            if (context.getString(R.string.within_last_month).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.within_last_month);
            }
            // Translate string More Than Six Months to English
            if (context.getString(R.string.more_than_six_months).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.more_than_six_months);
            }
            //   }
        return text;
    }

    public static String getEducationStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        //   if (!locale.equalsIgnoreCase("en")) {
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
           /* // Translate string Graduate to English
            if (context.getString(R.string.graduate).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.graduate);
            }
            // Translate string Postgraduate to English
            if (context.getString(R.string.postgraduate).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.postgraduate);
            }*/

        // Translate string -> Graduation and above.
        if (context.getString(R.string.graduation_and_above).equalsIgnoreCase(text)) {
            return updatedContext.getString(R.string.graduation_and_above);
        }
            //  }
        return text;
    }

    public static String hohRelationship(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        //    if (!locale.equalsIgnoreCase("en")) {
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
            //  }
        return text;
    }

    public static String getMedicalHistoryStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // If the app language is not in English, only in that case will the strings be translated.
        //    if (!locale.equalsIgnoreCase("en")) {
            // Translate string Yes to English
            if (context.getString(R.string.generic_yes).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_yes);
            }

            // Translate string No to English
            if (context.getString(R.string.generic_no).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.generic_no);
            }
            //    }
        return text;
    }

    public static String getTobaccoHistoryStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // Yes string conversion
        if (context.getString(R.string.yes).equalsIgnoreCase(text))
            return updatedContext.getString(R.string.yes);

        // No string conversion
        if (context.getString(R.string.no).equalsIgnoreCase(text))
            return updatedContext.getString(R.string.no);

        // Declined to answer string conversion
        if (context.getString(R.string.denied_to_answer).equalsIgnoreCase(text))
            return updatedContext.getString(R.string.denied_to_answer);

        return text;
    }

    public static String getSmokingHistoryStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        //  if (!locale.equalsIgnoreCase("en")) {
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
            //    }

        return text;
    }

    public static String getAlcoholHistory(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        //  if (!locale.equalsIgnoreCase("en")) {
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
            //   }

        return text;
    }

    public static String getDistanceStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        //  if (!locale.equalsIgnoreCase("en")) {
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
            //   }
        return text;
    }

    public static String getWhatsAppStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        //  if (!locale.equalsIgnoreCase("en")) {
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
        //   }
        return text;
    }

    public static String getTimeStrings(String text, Context context, Context updatedContext, String locale) {
        text = getSurveyValue(text);
        // if (!locale.equalsIgnoreCase("en")) {
            // Translate hours to English
            if (context.getString(R.string.identification_screen_picker_hours).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.identification_screen_picker_hours);
            }
            // Translate days to English
            if (context.getString(R.string.days).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.days);
            }
        //   }
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


    ///
    @NonNull
    public static String getStringByLocal(Activity context, int resId, String locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            return getStringByLocalPlus17(context, resId, locale);
        else
            return getStringByLocalBefore17(context, resId, locale);
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static String getStringByLocalPlus17(Activity context, int resId, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(resId);
    }

    private static String getStringByLocalBefore17(Context context,int resId, String language) {
        Resources currentResources = context.getResources();
        AssetManager assets = currentResources.getAssets();
        DisplayMetrics metrics = currentResources.getDisplayMetrics();
        Configuration config = new Configuration(currentResources.getConfiguration());
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        config.locale = locale;
        /*
         * Note: This (temporarily) changes the devices locale! TODO find a
         * better way to get the string in the specific locale
         */
        Resources defaultLocaleResources = new Resources(assets, metrics, config);
        String string = defaultLocaleResources.getString(resId);
        // Restore device-specific locale
        new Resources(assets, metrics, currentResources.getConfiguration());
        return string;
    }

    /***
     * It will help in making code readable and reduce boiler-plate code.
     * @param context Activity context.
     * @param sessionManager This contains the application context.
     * @param currentNode This is the node class object.
     * @return It will return a localized string.
     */
    public static String node_fetch_local_language(Context context, SessionManager sessionManager, Node currentNode) {
        String currentNodeVal = "";

        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            currentNodeVal = currentNode.formQuestionAnswer(0);
            currentNodeVal = currentNodeVal
                    .replace("Question not answered", "सवाल का जवाब नहीं दिया")
                    .replace("Patient reports -", "पेशेंट ने सूचित किया -")
                    .replace("Patient denies -", "पेशेंट ने मना कर दिया -")
                    .replace("Minutes", "मिनट")
                    .replace("Hours", "घंटे").replace("Days", "दिन")
                    .replace("Weeks", "हफ्तों").replace("Months", "महीने")
                    .replace("Years", "वर्ष")
                    .replace("times per hour", "प्रति घंटे बार").replace("time per day", "प्रति दिन का समय")
                    .replace("times per week", "प्रति सप्ताह बार").replace("times per month", "प्रति माह बार")
                    .replace("times per year", "प्रति वर्ष बार");
            currentNodeVal = Node.dateformat_en_hi_or_gu_as(currentNodeVal, sessionManager);
            Log.d("tag", currentNodeVal);
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNodeVal));

        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            currentNodeVal = currentNode.formQuestionAnswer(0);
            currentNodeVal = currentNodeVal
                    .replace("Question not answered", "ପ୍ରଶ୍ନର ଉତ୍ତର ନାହିଁ |")
                    .replace("Patient reports -", "ରୋଗୀ ରିପୋର୍ଟ -")
                    .replace("Patient denies -", "ରୋଗୀ ଅସ୍ୱୀକାର କରନ୍ତି -")
                    .replace("Minutes", "ମିନିଟ୍ |")
                    .replace("Hours", "ଘଣ୍ଟା").replace("Days", "ଦିନ")
                    .replace("Weeks", "ସପ୍ତାହ").replace("Months", "ମାସ")
                    .replace("Years", "ବର୍ଷ")
                    .replace("times per hour", "ସମୟ ପ୍ରତି ଘଣ୍ଟା").replace("time per day", "ସମୟ ପ୍ରତିଦିନ")
                    .replace("times per week", "ସମୟ ପ୍ରତି ସପ୍ତାହ").replace("times per month", "ସମୟ ପ୍ରତି ମାସରେ |")
                    .replace("times per year", "ସମୟ ପ୍ରତିବର୍ଷ");
            currentNodeVal = Node.dateformat_en_hi_or_gu_as(currentNodeVal, sessionManager);
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNodeVal));
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            currentNodeVal = currentNode.formQuestionAnswer(0);
            currentNodeVal = currentNodeVal
                    .replace("Question not answered", "પ્રશ્નનો જવાબ મળ્યો નથી")
                    .replace("Patient reports -", "દર્દીના અહેવાલો -")
                    .replace("Patient denies -", "દર્દી નકારે છે -")
                    .replace("Minutes", "મિનિટ")
                    .replace("Hours", "કલાકો").replace("Days", "દિવસ")
                    .replace("Weeks", "અઠવાડિયા").replace("Months", "મહિનાઓ")
                    .replace("Years", "વર્ષ")
                    .replace("times per hour", "કલાક દીઠ વખત").replace("time per day", "દિવસ દીઠ વખત")
                    .replace("times per week", "સપ્તાહ દીઠ વખત").replace("times per month", "દર મહિને વખત")
                    .replace("times per year", "દર વર્ષે વખત");
            currentNodeVal = Node.dateformat_en_hi_or_gu_as(currentNodeVal, sessionManager);
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNodeVal));
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            currentNodeVal = currentNode.formQuestionAnswer(0);
            currentNodeVal = currentNodeVal
                    .replace("Question not answered", "প্রশ্নের উত্তর দেওয়া হয়নি")
                    .replace("Patient reports -", "রোগীর রিপোর্ট -")
                    .replace("Patient denies -", "রোগী অস্বীকার করে-")
                    .replace("Minutes", "মিনিট")
                    .replace("Hours", "ঘন্টার").replace("Days", "দিন")
                    .replace("Weeks", "সপ্তাহ").replace("Months", "মাস")
                    .replace("Years", "বছর")
                    .replace("times per hour", "প্রতি ঘন্টায় বার")
                    .replace("time per day", "প্রতিদিন সময়")
                    .replace("times per week", "প্রতি সপ্তাহে বার")
                    .replace("times per month", "প্রতি মাসে বার")
                    .replace("times per year", "প্রতি বছর বার");
            currentNodeVal = Node.dateformat_en_hi_or_gu_as(currentNodeVal, sessionManager);
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNodeVal));
        }
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            currentNodeVal = currentNode.formQuestionAnswer(0);
            currentNodeVal = currentNodeVal
                    .replace("Question not answered", "ಪ್ರಶ್ನೆಗೆ ಉತ್ತರವಿಲ್ಲ")
                    .replace("Patient reports -", "ರೋಗಿಗಳ ವರದಿಗಳು -")
                    .replace("Patient denies -", "ರೋಗಿಯು ನಿರಾಕರಿಸುತ್ತಾನೆ -")
                    .replace("Minutes", "ನಿಮಿಷಗಳು")
                    .replace("Hours", "ಗಂಟೆಗಳು").replace("Days", "ದಿನಗಳು")
                    .replace("Weeks", "ವಾರಗಳು").replace("Months", "ತಿಂಗಳುಗಳು")
                    .replace("Years", "ವರ್ಷಗಳು")
                    .replace("times per hour", "ಗಂಟೆಗೆ ಬಾರಿ")
                    .replace("time per day", "ದಿನಕ್ಕೆ ಸಮಯ")
                    .replace("times per week", "ವಾರಕ್ಕೆ ಬಾರಿ")
                    .replace("times per month", "ತಿಂಗಳಿಗೆ ಬಾರಿ")
                    .replace("times per year", "ವರ್ಷಕ್ಕೆ ಬಾರಿ");
            currentNodeVal = Node.dateformat_en_hi_or_gu_as(currentNodeVal, sessionManager);
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNodeVal));
        }
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            currentNodeVal = currentNode.formQuestionAnswer(0);
            currentNodeVal = currentNodeVal
                    .replace("Question not answered", "प्रश्नाचे उत्तर मिळाले नाही")
                    .replace("Patient reports -", "रुग्ण अहवाल -")
                    .replace("Patient denies -", "रुग्ण नकार देतो -")
                    .replace("Minutes", "मिनिट")
                    .replace("Hours", "तास").replace("Days", "दिवस")
                    .replace("Weeks", "आठवडे").replace("Months", "महिने")
                    .replace("Years", "वर्षे")
                    .replace("times per hour", "प्रति तास वेळा")
                    .replace("time per day", "प्रती दिन वेळा")
                    .replace("times per week", "प्रति आठवडा वेळा")
                    .replace("times per month", "दर महिन्याला")
                    .replace("times per year", "दर वर्षी");
            currentNodeVal = Node.dateformat_en_hi_or_gu_as(currentNodeVal, sessionManager);
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNodeVal));
        }
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            currentNodeVal = currentNode.formQuestionAnswer(0);
            currentNodeVal = currentNodeVal
                    .replace("Question not answered", "પপ্ৰশ্নৰ উত্তৰ নাই")
                    .replace("Patient reports -", "ৰোগীৰ প্ৰতিবেদন -")
                    .replace("Patient denies -", "ৰোগীৰ ৰিপৰ্ট -")
                    .replace("Minutes", "মিনিটবোৰ")
                    .replace("Hours", "ঘণ্টা").replace("Days", "দিনবোৰ")
                    .replace("Weeks", "সপ্তাহ").replace("Months", "মাহবোৰ")
                    .replace("Years", "বছৰবোৰ")
                    .replace("times per hour", "প্ৰতি ঘণ্টাত বাৰকৈ").replace("time per day", "প্ৰতিদিনে সময়")
                    .replace("times per week", "প্ৰতি সপ্তাহত বাৰকৈ").replace("times per month", "প্ৰতিমাহে বাৰ")
                    .replace("times per year", "বছৰি বাৰকৈ");
            currentNodeVal = Node.dateformat_en_hi_or_gu_as(currentNodeVal, sessionManager);
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNodeVal));
        } else {
//            alertDialogBuilder.setMessage(Html.fromHtml(currentNode.formQuestionAnswer(0)));
            currentNodeVal = currentNode.formQuestionAnswer(0);
        }

        return currentNodeVal;
    }

    /**
     * This function will read the regional lang from the json if the regional lang string is present for this current
     * set app language than it will show the regional lang data else it will show the standard earlier one english data.
     * @param value_REG this obs object contains regional lang data.
     * @param value this obs objet contains english lang data.
     * @return either regional or english data as per need.
     */
    public static String fetchObsValue_REG(ObsDTO value_REG, ObsDTO value, SessionManager sessionManager) {
        if (value_REG.getValue() != null) {
            try {
                JSONObject jsonObject = new JSONObject(value_REG.getValue());
                String text = jsonObject.getString("text_" + sessionManager.getAppLanguage());
                return text;
            } catch (JSONException e) {
                if (value.getValue() != null)
                    return value.getValue();
            }
        }
        return (value.getValue() != null) ? value.getValue() : "";
    }

    /**
     * This function will read the regional lang from the json if the regional lang string is present for this current
     * set app language than it will show the regional lang data else it will show the standard earlier one english data.
     * @param value_REG this String object contains regional lang data.
     * @param value this String objet contains english lang data.
     * @return either regional or english data as per need.
     */
    public static String fetchValue_REG(String value_REG, String value, SessionManager sessionManager) {
        if (value_REG != null) {
            try {
                JSONObject jsonObject = new JSONObject(value_REG);
                String text = jsonObject.getString("text_" + sessionManager.getAppLanguage());
                return text;
            } catch (JSONException e) {
                if (value != null)
                    return value;
            }
        }
        return (value != null) ? value : "";
    }

    public static String second_filter(String value) {

        // when values are dynamic than we will check by using contains() of String...
        if (value.contains("बीघा") || value.contains("বিঘা"))
            value = value.replace("बीघा", "Bigha")
                    .replace("বিঘা", "Bigha");

        if (value.contains("एकड") || value.contains("એકર") || value.contains("ಎಕರೆ"))
            value = value.replace("एकड", "Acre")
                    .replace("એકર", "Acre")
                    .replace("ಎಕರೆ", "Acre");

        if (value.contains("भूमिहीन") || value.contains("ভূমিহীন") || value.contains("ಭೂರಹಿತರ"))
            value = value.replace("भूमिहीन", "Landless")
                    .replace("ভূমিহীন", "Landless")
                    .replace("ಭೂರಹಿತರ", "Landless");

        if (value.contains("चुनते हैं"))
            value = value.replace("चुनते हैं", "Select");

        if (value.contains("পোৰা") || value.contains("गुंटा"))
            value = value.replace("পোৰা", "Gunta")
                    .replace("गुंटा", "Gunta");

        if (value.contains("2,50,000 से अधिक") || value.contains("২,৫০,০০০ তকৈ অধিক"))
            value = value.replace("2,50,000 से अधिक", "More than 2,50,000")
                    .replace("২,৫০,০০০ তকৈ অধিক", "More than 2,50,000");

        if (value.contains("एलपीजी\\/प्राकृतिक गैस") || value.contains("এল পি জি বা প্ৰাকৃতিক গেছ") ||
                value.contains("એલપીજી\\/નેચરલ ગેસ") || value.contains("ಎಲ್ ಪಿ ಜಿ ಅಥವಾ ನೈಸರ್ಗಿಕ ಅನಿಲ"))
            value = value.replace("एलपीजी\\/प्राकृतिक गैस", "LPG or Natural Gas")
                    .replace("এল পি জি বা প্ৰাকৃতিক গেছ", "LPG or Natural Gas")
                    .replace("એલપીજી\\/નેચરલ ગેસ", "LPG or Natural Gas")
                    .replace("ಎಲ್ ಪಿ ಜಿ ಅಥವಾ ನೈಸರ್ಗಿಕ ಅನಿಲ", "LPG or Natural Gas");


        if (value.contains("मिट्टी का तेल") || value.contains("कोयला\\/लिग्नाइट") || value.contains("लकड़ी का कोयला")
                || value.contains("पुआल\\/झाड़ियाँ\\/घास") || value.contains("कृषि फसल अपशिष्ट") || value.contains("गोबर के कंडे")) {
            value = value.replace("कोयला\\/लिग्नाइट", "Coal or Lignite")
                    .replace("लकड़ी का कोयला", "Charcoal")
                    .replace("पुआल\\/झाड़ियाँ\\/घास", "Straw or Shrubs or Grass")
                    .replace("कृषि फसल अपशिष्ट", "Agricultural Crop Waste")
                    .replace("गोबर के कंडे", "Dung Cakes");
        }

        if (value.contains("बायोगैस") || value.contains("ಜೈವಿಕ ಅನಿಲ"))
            value = value.replace("बायोगैस", "Biogas")
                    .replace("ಜೈವಿಕ ಅನಿಲ", "Biogas");

        if (value.contains("বিদ্যুৎ") || value.contains("વીજળી") || value.contains("बिजली") || value.contains("ವಿದ್ಯುತ್"))
            value = value.replace("বিদ্যুৎ", "Electricity").replace("વીજળી", "Electricity")
                    .replace("बिजली", "Electricity").replace("ವಿದ್ಯುತ್", "Electricity");

        if (value.contains("কাঠ") || value.contains("લાકડા") || value.contains("लकड़ी"))
            value = value.replace("কাঠ", "Wood")
                    .replace("लकड़ी", "Wood")
                    .replace("લાકડા", "Wood");

        if (value.contains("কেৰাচিন") || value.contains("मिट्टी का तेल"))
            value = value.replace("কেৰাচিন", "Kerosene").replace("मिट्टी का तेल", "Kerosene");

        if (value.contains("लालटेन/मिट्टी के तेल का लैम्प/मोमबत्ती") || value.contains("বন্তি/কেৰাচিন লেম্প/মমবাতি"))
            value = value.replace("लालटेन/मिट्टी के तेल का लैम्प/मोमबत्ती", "Lantern/Kerosene Lamp/Candle")
                    .replace("বন্তি/কেৰাচিন লেম্প/মমবাতি", "Lantern/Kerosene Lamp/Candle");

        if (value.contains("दिन") || value.contains("দিন"))
            value = value.replace("दिन", "Days")
                    .replace("দিন", "Days");

        if (value.contains("घंटे") || value.contains("ঘণ্টা") || value.contains("ঘন্টার") || value.contains("মিনিট"))
            value = value.replace("घंटे", "Hours")
                    .replace("ঘন্টার", "Hours")
                    .replace("ঘণ্টা", "Hours")
                    .replace("মিনিট", "Minutes");

        // drinking water source.
        if (value.contains("सार्वजनिक नल\\/स्टैंडपाइप") || value.contains("घर पर हैंड पंप")
                || value.contains("आवास में बोर किया गया") || value.contains("বাসস্থান মধ্য পাইপ")
                || value.contains("यार्ड\\/प्लॉट में बोर किया गया") || value.contains("सार्वजनिक नल\\/स्टैंडपाइप")
                || value.contains("ट्यूब वेल\\/बोरहोल") || value.contains("संरक्षित कुआं") || value.contains("सामान्य हैंड पंप")
                || value.contains("সুৰক্ষিত ভাল") || value.contains("টিউব ৱেল বা ব’ৰহ’ল")) {
            value = value.replace("सार्वजनिक नल\\/स्टैंडपाइप", "Public Tap or Standpipe")
                    .replace("घर पर हैंड पंप", "Hand Pump At Home")
                    .replace("आवास में बोर किया गया", "Piped Into Dwelling")
                    .replace("বাসস্থান মধ্য পাইপ", "Piped Into Dwelling")
                    .replace("यार्ड\\/प्लॉट में बोर किया गया", "Piped Into Yard or Plot")
                    .replace("सार्वजनिक नल\\/स्टैंडपाइप", "Public Tap or Standpipe")
                    .replace("ट्यूब वेल\\/बोरहोल", "Tube Well or Borehole")
                    .replace("संरक्षित कुआं", "Protected Well")
                    .replace("सामान्य हैंड पंप", "Common Hand Pump")
                    .replace("সুৰক্ষিত ভাল", "Protected Well")
                    .replace("টিউব ৱেল বা ব’ৰহ’ল", "Tube Well or Borehole");
        }

        if (value.contains("ब्लीच डालते हैं") || value.contains("उबालते है") ||
                value.contains("উতলোৱা") || value.contains("ফুটান") || value.contains("ಕುದಿಸಿ"))
            value = value.replace("ब्लीच डालते हैं", "Add bleach")
                    .replace("उबालते है", "Boiling")
                    .replace("উতলোৱা", "Boiling")
                    .replace("ফুটান", "Boiling")
                    .replace("ಕುದಿಸಿ", "Boiling");

        if (value.contains("कपड़े से छानते है") || value.contains("कुछ नहीं करते है") || value.contains("કાપડ દ્વારા તાણ"))
            value = value.replace("कपड़े से छानते है", "Strain Through A Cloth")
                    .replace("कुछ नहीं करते है", "Do Nothing")
                    .replace("કાપડ દ્વારા તાણ", "Strain Through A Cloth");

        if (value.contains("পানী ফিল্টাৰ") || value.contains("फिटकरी का उपयोग करें") || value.contains("পানী ফিল্টাৰ"))
            value = value.replace("পানী ফিল্টাৰ", "Water Filter")
                    .replace("পানী ফিল্টাৰ", "Water Filter")
                    .replace("फिटकरी का उपयोग करें", "Use Alum");

        if (value.contains("হাতেৰে নিৰ্মিত বিশুদ্ধকৰণ"))
            value = value.replace("হাতেৰে নিৰ্মিত বিশুদ্ধকৰণ", "Hand Made Purifier");

        if (value.contains("हिंदू") || value.contains("হিন্দু") || value.contains("હિંદુ") || value.contains("ಹಿಂದೂ")
                || value.contains("খ্ৰীষ্টান") || value.contains("ಮುಸ್ಲಿಂ") || value.contains("স টি") || value.contains("ಕಚ್ಚಾ ಮನೆ")
                || value.contains("ಪಕ್ಕಾ ಮನೆ"))
            value = value.replace("हिंदू", "Hindu")
                    .replace("হিন্দু", "Hindu").replace("હિંદુ", "Hindu")
                    .replace("ಹಿಂದೂ", "Hindu").replace("খ্ৰীষ্টান", "Christian")
                    .replace("ಮುಸ್ಲಿಂ", "Muslim").replace("স টি", "ST")
                    .replace("ಕಚ್ಚಾ ಮನೆ", "Kutcha House")
                    .replace("ಪಕ್ಕಾ ಮನೆ", "Pakka House");

        if (value.contains("नहीं") || value.contains("নহয়") || value.contains("ના"))
            value = value.replace("नहीं", "No")
                    .replace("নহয়", "No").replace("ના", "No");

        if (value.contains("हाँ") || value.contains("હા") || value.contains("হয়") || value.contains("হ্যাঁ") || value.contains("ಹೌದು"))
            value = value.replace("हाँ", "Yes")
                    .replace("હા", "Yes").replace("হয়", "Yes")
                    .replace("হ্যাঁ", "Yes").replace("ಹೌದು", "Yes");

        if (value.contains("पता नहीं") || value.contains("নিশ্চিত নহয়") || value.contains("हाँ, कार्ड नहीं देखा")
                || value.contains("हाँ, कार्ड देखा") || value.contains("হয়, কাৰ্ড দেখা") || value.contains("হ্যাঁ, কার্ড দেখা গেছে"))
            value = value.replace("पता नहीं", "Not Sure")
                    .replace("নিশ্চিত নহয়", "Not Sure")
                    .replace("हाँ, कार्ड नहीं देखा", "Yes, Card Not Seen")
                    .replace("हाँ, कार्ड देखा", "Yes, Card Seen")
                    .replace("হয়, কাৰ্ড দেখা", "Yes, Card Seen")
                    .replace("হ্যাঁ, কার্ড দেখা গেছে", "Yes, Card Seen");

        // soap hand washing occasion.
        if (value.contains("साबुन का प्रयोग करते है") || value.contains("हाथ धोने के लिए अन्य साधनों का प्रयोग करते है") ||
                value.contains("शौचालय का उपयोग करने के बाद धो लेते है")
                || value.contains("खाना पकाने से पहले धो लेते है")
                || value.contains("खाने से पहले और बाद में धो लेते है")
                || value.contains("खाना पकाने से पहले")
                || value.contains("खाना पकाने से पहले धो लेते है")
                || value.contains("চাবোন ব্যৱহাৰ কৰক")
                || value.contains("শৌচাগাৰ ব্যৱহাৰ কৰাৰ পিছত ধুব")
                || value.contains("ৰন্ধা আগতে ধুব")
                || value.contains("রান্নার আগে")
                || value.contains("সাবান ব্যবহার করুন")
                || value.contains("খাবার আগে")
                || value.contains("ಶೌಚಾಲಯವನ್ನು ಬಳಸಿದ ನಂತರ ತೊಳೆಯಿರಿ")
                || value.contains("સાબુનો ઉપયોગ")
                || value.contains("ಸೋಪ್ ಬಳಸಿ")
                || value.contains("ತಿನ್ನುವ ಮೊದಲು ಮತ್ತು ನಂತರ ತೊಳೆಯಿರಿ.")
                || value.contains("ಅಡುಗೆ ಮಾಡುವ ಮೊದಲು ತೊಳೆಯಿರಿ")
                || value.contains("જમ્યા પહેલાં અને જમ્યા પછી")
                || value.contains("જમવાનું બનાવવા પહેલા")
                || value.contains("શૌચાલયનો ઉપયોગ કર્યા પછી")
                || value.contains("হাত ধুবলৈ অন্য উপায় ব্যৱহাৰ কৰক")
                || value.contains("খাওয়ার আগে এবং পরে ধুয়ে ফেলুন।")
                || value.contains("খোৱাৰ আগতে আৰু পিছত ধুব লাগে।")
                || value.contains("शौचालय का उपयोग करने के बाद धो लेते है"))

            value = value.replace("साबुन का प्रयोग करते है", "Use Soap")
                    .replace("हाथ धोने के लिए अन्य साधनों का प्रयोग करते है", "Use other means to wash hands")
                    .replace("शौचालय का उपयोग करने के बाद धो लेते है", "Wash after using toilet")
                    .replace("खाना पकाने से पहले धो लेते है", "Wash before cooking")
                    .replace("खाने से पहले और बाद में धो लेते है", "Wash before and after eating")
                    .replace("खाना पकाने से पहले", "Before Cooking")
                    .replace("खाना पकाने से पहले धो लेते है", "Wash before cooking")
                    .replace("চাবোন ব্যৱহাৰ কৰক", "Use Soap")
                    .replace("শৌচাগাৰ ব্যৱহাৰ কৰাৰ পিছত ধুব", "Wash after using toilet")
                    .replace("ৰন্ধা আগতে ধুব", "Wash before cooking")
                    .replace("খোৱাৰ আগতে আৰু পিছত ধুব লাগে।", "Wash before and after eating")
                    .replace("রান্নার আগে", "Before Cooking")
                    .replace("খাবার আগে", "Before Eating")
                    .replace("ತಿನ್ನುವ ಮೊದಲು ಮತ್ತು ನಂತರ ತೊಳೆಯಿರಿ.", "Wash before and after eating")
                    .replace("ಅಡುಗೆ ಮಾಡುವ ಮೊದಲು ತೊಳೆಯಿರಿ", "Wash before cooking")
                    .replace("ಶೌಚಾಲಯವನ್ನು ಬಳಸಿದ ನಂತರ ತೊಳೆಯಿರಿ", "Wash after using toilet")
                    .replace("સાબુનો ઉપયોગ", "Use Soap")
                    .replace("ಸೋಪ್ ಬಳಸಿ", "Use Soap")
                    .replace("જમ્યા પહેલાં અને જમ્યા પછી", "Wash before and after eating")
                    .replace("જમવાનું બનાવવા પહેલા", "Wash before cooking")
                    .replace("શૌચાલયનો ઉપયોગ કર્યા પછી", "Wash after using toilet")
                    .replace("হাত ধুবলৈ অন্য উপায় ব্যৱহাৰ কৰক", "Use other means to wash hands")
                    .replace("সাবান ব্যবহার করুন", "Use Soap")
                    .replace("খাওয়ার আগে এবং পরে ধুয়ে ফেলুন।", "Wash before and after eating")
                    .replace("शौचालय का उपयोग करने के बाद धो लेते है", "Wash after using toilet");

        if (value.contains("অন্য পৰিয়ালৰ সৈতে ভাগ কৰা শৌচাগাৰ"))
            value = value.replace("অন্য পৰিয়ালৰ সৈতে ভাগ কৰা শৌচাগাৰ", "Shared Toilet with other households");


        Logger.logV("StringUtils", "second_filter: " + value);
        return value;
    }

}
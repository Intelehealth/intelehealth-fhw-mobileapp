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

package org.intelehealth.vikalphelpline.utilities;

import android.widget.Spinner;

import java.io.File;
import java.util.List;

import org.intelehealth.vikalphelpline.app.IntelehealthApplication;

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

    //todo once will fix from backend side May be this method is not necessory.
  public static String getValueNonMandetory(String value) {
        String val = "";
        if (value != null){
        if(value.equalsIgnoreCase("")){
            val="_";
        }else{
            val = value;
        }

        }
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
//            val = switch_hi_HelplineKnowledge(val);
//            val = switch_hi_CallRelation(val);
            val = switch_hi_caste(val);
            val = switch_hi_economic(val);
            val = switch_hi_education(val);
        }

        return val;
    }

    public static String getProvidedOthers(Spinner spinner) {
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
            val = switch_hi_genderSpinner(val);
            val = switch_hi_educationSpinner(val);
            val = switch_hi_current_maritual_statusSpinner(val);
            val = switch_hi_occupationSelectionSpinner(val);
            val = switch_hi_husbandoccupationSelectionSpinner(val);
            val = switch_hi_childrenSpinner(val);
            val = switch_hi_CasteSelectionSpinner(val);
            val = switch_hi_ContactTypeSpinner(val);
            val = switch_hi_WhereDidU_Spinner(val);
            val = switch_hi_CallRelation(val);
            val = switch_hi_HelplineKnowledge(val);
            val = switch_hi_PhoneType(val);

        }

        return val;
    }


    public static String getProvidedOthers_1(Spinner spinner) {
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

            val = switch_hi_jobSpinner(val);
            val = switch_hi_DescribeLocationSpinner(val);
            val = switch_hi_Who_Referred_Spinner(val);
            val = switch_hi_am_i_speaking_Spinner(val);
            val = switch_hi_survivor_ever_married_Spinner(val);
            val = switch_hi_type_marriage_Spinner(val);
            val = switch_hi_survivor_address_Spinner(val);
            val = switch_hi_with_whom_living_Spinner(val);

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


    public static String switch_hi__edit(String val) {
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

    public static String switch_hi_genderSpinner(String val) {
        switch (val) {

            case "पुरुष":
                val = "Male";
                break;
            case "महिला":
                val = "Female";
                break;
            case "गैर-बाइनरी / तृतीय लिंग":
                val = "Non-binary / third gender";
                break;
            case "नहीं कहना पसंद करें":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_genderSpinner_edit(String val) {
        switch (val) {

            case "Male":
                val = "पुरुष";
                break;
            case "Female":
                val = "महिला";
                break;
            case "Non-binary / third gender":
                val = "गैर-बाइनरी / तृतीय लिंग";
                break;
            case "Prefer not to say":
                val = "नहीं कहना पसंद करें";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_educationSpinner(String val) {
        switch (val) {

            case "औपचारिक रूप से शिक्षित नहीं":
                val = "Not formally educated";
                break;
            case "कुछ/पूर्ण प्राथमिक विद्यालय":
                val = "Some/completed primary school";
                break;
            case "कुछ/पूर्ण माध्यमिक विद्यालय":
                val = "Some/completed secondary school";
                break;
            case "कुछ/पूर्ण उच्चतर माध्यमिक":
                val = "Some/completed higher secondary";
                break;
            case "पूर्ण स्नातक डिग्री/डिप्लोमा/तकनीकी स्कूल":
                val = "Completed undergraduate degree/diploma/technical school";
                break;
            case "पूर्ण स्नातकोत्तर डिग्री":
                val = "Completed postgraduate degree";
                break;
            case "नहीं कहना पसंद करते हैं":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_educationSpinner_edit(String val) {
        switch (val) {
            case "Not formally educated":
                val = "औपचारिक रूप से शिक्षित नहीं";
                break;
            case "Some/completed primary school":
                val = "कुछ/पूर्ण प्राथमिक विद्यालय";
                break;
            case "Some/completed secondary school":
                val = "कुछ/पूर्ण माध्यमिक विद्यालय";
                break;
            case "Some/completed higher secondary":
                val = "कुछ/पूर्ण उच्चतर माध्यमिक";
                break;
            case "Completed undergraduate degree/diploma/technical school":
                val = "पूर्ण स्नातक डिग्री/डिप्लोमा/तकनीकी स्कूल";
                break;
            case "Completed postgraduate degree":
                val = "पूर्ण स्नातकोत्तर डिग्री";
                break;
            case "Prefer not to say":
                val = "नहीं कहना पसंद करते हैं";
                break;
            case "Other":
                val = "अन्य";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_current_maritual_statusSpinner(String val) {
        switch (val) {

            case "विवाहित":
                val = "Married";
                break;
            case "अविवाहित/अकेली":
                val = "Unmarried/single";
                break;
            case "ओ एक रिश्ते में":
                val = "In a relationship";
                break;
            case "लिव-इन रिलेशनशिप में":
                val = "In a live-in relationship";
                break;
            case "ओ तलाकशुदा":
                val = "Divorced";
                break;
            case "ओ विधवा":
                val = "Widowed";
                break;
            case "ओ नता":
                val = "Nata";
                break;
            case "दूसरी शादी":
                val = "Second Marriage";
                break;
            case "नहीं कहना पसंद करते हैं":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_current_maritual_statusSpinner_edit(String val) {
        switch (val) {
            case "Married":
                val = "विवाहित";
                break;
            case "Unmarried/single":
                val = "अविवाहित/अकेली";
                break;
            case "In a relationship":
                val = "ओ एक रिश्ते में";
                break;
            case "In a live-in relationship":
                val = "लिव-इन रिलेशनशिप में";
                break;
            case "Divorced":
                val = "ओ तलाकशुदा";
                break;
            case "Widowed":
                val = "ओ विधवा";
                break;
            case "Nata":
                val = "ओ नता";
                break;
            case "Second Marriage":
                val = "दूसरी शादी";
                break;
            case "Prefer not to say":
                val = "नहीं कहना पसंद करते हैं";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_occupationSelectionSpinner(String val) {
        switch (val) {

            case "विद्यार्थी":
                val = "Student";
                break;
            case "विद्यार्थी लेकिन वर्तमान में स्कूल से बाहर है":
                val = "Student but currently out of school";
                break;
            case "अभी-अभी स्कूल समाप्त हुआ":
                val = "Just finished school";
                break;
            case "गृहिणी (घर के बाहर काम नहीं कर रही)":
                val = "Housewife (not working outside the home)";
                break;
            case "कर्मचारी":
                val = "Employee";
                break;
            case "स्वरोजगार":
                val = "Self-employed";
                break;
            case "बेरोजगार और नौकरी की तलाश में":
                val = "Unemployed and looking for a job";
                break;
            case "बेरोजगार और नौकरी की तलाश नहीं कर रहे हैं":
                val = "Unemployed and not looking for a job";
                break;
            case "स्वयंसेवक":
                val = "Volunteer";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_occupationSelectionSpinner_edit(String val) {
        switch (val) {
            case "Student":
                val = "विद्यार्थी";
                break;
            case "Student but currently out of school":
                val = "विद्यार्थी लेकिन वर्तमान में स्कूल से बाहर है";
                break;
            case "Just finished school":
                val = "अभी-अभी स्कूल समाप्त हुआ";
                break;
            case "Housewife (not working outside the home)":
                val = "गृहिणी (घर के बाहर काम नहीं कर रही)";
                break;
            case "Employee":
                val = "कर्मचारी";
                break;
            case "Self-employed":
                val = "स्वरोजगार";
                break;
            case "Unemployed and looking for a job":
                val = "बेरोजगार और नौकरी की तलाश में";
                break;
            case "Unemployed and not looking for a job":
                val = "बेरोजगार और नौकरी की तलाश नहीं कर रहे हैं";
                break;
            case "Volunteer":
                val = "स्वयंसेवक";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_husbandoccupationSelectionSpinner(String val) {
        switch (val) {

            case "विद्यार्थी":
                val = "Student";
                break;
            case "विद्यार्थी लेकिन वर्तमान में स्कूल से बाहर है":
                val = "Student but currently out of school";
                break;
            case "अभी-अभी स्कूल समाप्त हुआ":
                val = "Just finished school";
                break;
            case "गृहिणी (घर के बाहर काम नहीं कर रही)":
                val = "Housewife (not working outside the home)";
                break;
            case "कर्मचारी":
                val = "Employee";
                break;
            case "स्वरोजगार":
                val = "Self-employed";
                break;
            case "बेरोजगार और नौकरी की तलाश में":
                val = "Unemployed and looking for a job";
                break;
            case "बेरोजगार और नौकरी की तलाश नहीं कर रहे हैं":
                val = "Unemployed and not looking for a job";
                break;
            case "स्वयंसेवक":
                val = "Volunteer";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_husbandoccupationSelectionSpinner_edit(String val) {
        switch (val) {
            case "Student":
                val = "विद्यार्थी";
                break;
            case "Student but currently out of school":
                val = "विद्यार्थी लेकिन वर्तमान में स्कूल से बाहर है";
                break;
            case "Just finished school":
                val = "अभी-अभी स्कूल समाप्त हुआ";
                break;
            case "Housewife (not working outside the home)":
                val = "गृहिणी (घर के बाहर काम नहीं कर रही)";
                break;
            case "Employee":
                val = "कर्मचारी";
                break;
            case "Self-employed":
                val = "स्वरोजगार";
                break;
            case "Unemployed and looking for a job":
                val = "बेरोजगार और नौकरी की तलाश में";
                break;
            case "Unemployed and not looking for a job":
                val = "बेरोजगार और नौकरी की तलाश नहीं कर रहे हैं";
                break;
            case "Volunteer":
                val = "स्वयंसेवक";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_childrenSpinner(String val) {
        switch (val) {

            case "हां":
                val = "Yes";
                break;
            case "नहीं":
                val = "No";
                break;
            case "नहीं कहना पसंद करें":
                val = "Prefer not to say";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_childrenSpinner_edit(String val) {
        switch (val) {

            case "Yes":
                val = "हां";
                break;
            case "No":
                val = "नहीं";
                break;
            case "Prefer not to say":
                val = "नहीं कहना पसंद करें";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_CasteSelectionSpinner(String val) {
        switch (val) {

            case "एससी":
                val = "SC";
                break;
            case "एसटी":
                val = "ST";
                break;
            case "एमबीसी":
                val = "MBC";
                break;
            case "ओबीसी":
                val = "OBC";
                break;
            case "एसबीसी":
                val = "SBC";
                break;
            case "सामान्य":
                val = "General";
                break;
            case "अल्पसंख्यक (मुस्लिम)":
                val = "Minority (Muslim)";
                break;
            case "अल्पसंख्यक (ईसाई)":
                val = "Minority (Christian)";
                break;
            case "अल्पसंख्यक (जैन)":
                val = "Minority (Jain)";
                break;
            case "अल्पसंख्यक (अन्य - कृपया निर्दिष्ट करें)":
                val = "Minority (other - please specify)";
                break;
            case "नहीं कहना पसंद करें":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_CasteSelectionSpinner_edit(String val) {
        switch (val) {

            case "SC":
                val = "एससी";
                break;
            case "ST":
                val = "एसटी";
                break;
            case "MBC":
                val ="एमबीसी" ;
                break;
            case "OBC":
                val = "ओबीसी";
                break;
            case "SBC":
                val = "एसबीसी";
                break;
            case "General":
                val = "सामान्य";
                break;
            case "Minority (Muslim)":
                val = "अल्पसंख्यक (मुस्लिम)";
                break;
            case "Minority (Christian)":
                val = "अल्पसंख्यक (ईसाई)";
                break;
            case "Minority (Jain)":
                val = "अल्पसंख्यक (जैन)";
                break;
            case "Minority (other - please specify)":
                val = "अल्पसंख्यक (अन्य - कृपया निर्दिष्ट करें)";
                break;
            case "Prefer not to say":
                val = "नहीं कहना पसंद करें";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_ContactTypeSpinner(String val) {
        switch (val) {

            case "कार्यालय का दौरा":
                val = "Office visit";
                break;
            case "हॉटलाइन कॉलर":
                val = "Hotline caller";
                break;
            case "समुदाय तक पहुंच":
                val = "Community outreach";
                break;
            case "विकल्प उत्तरजीवी/संपर्ककर्ता तक पहुंच रहा है":
                val = "Vikalp reaching out to survivor/contact";
                break;
            case "पिछला कॉलर":
                val = "Previous caller";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_ContactTypeSpinner_edit(String val) {
        switch (val) {

            case "Office visit":
                val = "कार्यालय का दौरा";
                break;
            case "Hotline caller":
                val = "हॉटलाइन कॉलर";
                break;
            case "Community outreach":
                val = "समुदाय तक पहुंच";
                break;
            case "Vikalp reaching out to survivor/contact":
                val = "विकल्प उत्तरजीवी/संपर्ककर्ता तक पहुंच रहा है";
                break;
            case "Previous caller":
                val = "पिछला कॉलर";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_WhereDidU_Spinner(String val) {
        switch (val) {

            case "परिवार द्वारा संदर्भित":
                val = "Referred by family";
                break;
            case "मित्र द्वारा संदर्भित":
                val = "Referred by friend";
                break;
            case "विकल्प कर्मचारी":
                val = "Vikalp staff";
                break;
            case "सोशल मीडिया/वेबसाइट":
                val = "Social media/website";
                break;
            case "प्रदाता":
                val = "Provider";
                break;
            case "सामुदायिक संगठन":
                val = "Community organization";
                break;
            case "नहीं कहना पसंद करें":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_WhereDidU_Spinner_edit(String val) {
        switch (val) {

            case "Referred by family":
                val = "परिवार द्वारा संदर्भित";
                break;
            case "Referred by friend":
                val = "मित्र द्वारा संदर्भित";
                break;
            case "Vikalp staff":
                val = "विकल्प कर्मचारी";
                break;
            case "Social media/website":
                val = "सोशल मीडिया/वेबसाइट";
                break;
            case "Provider":
                val = "प्रदाता";
                break;
            case "Community organization":
                val = "सामुदायिक संगठन";
                break;
            case "Prefer not to say":
                val = "नहीं कहना पसंद करें";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_CallRelation(String val) {
        switch (val) {
            case "रोगी स्वयं":
                val = "Patient themselves";
                break;
            case "मां":
                val = "Mother";
                break;
            case "पिता":
                val = "Father";
                break;
            case "पति":
                val = "Spouse";
                break;
            case "बेटा":
                val = "Son";
                break;
            case "बेटी":
                val = "Daughter";
                break;
            case "बहन":
                val = "Sister";
                break;
            case "भाई":
                val = "Brother";
                break;
            case "अन्य रिश्तेदार":
                val = "Other Relative";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_CallRelation_edit(String val) {
        switch (val) {
            case "Patient themselves":
                val = "रोगी स्वयं";
                break;
            case "Mother":
                val = "मां";
                break;
            case "Father":
                val = "पिता";
                break;
            case "Spouse":
                val = "पति";
                break;
            case "Son":
                val = "बेटा";
                break;
            case "Daughter":
                val = "बेटी";
                break;
            case "Sister":
                val = "बहन";
                break;
            case "Brother":
                val = "भाई";
                break;
            case "Other Relative":
                val = "अन्य रिश्तेदार";
                break;

            default:
                return val;
        }
        return val;
    }



    public static String switch_hi_HelplineKnowledge(String val) {
        switch (val) {

            case "सोशल मीडिया":
                val = "Social Media";
                break;
            case "दीवार के पोस्टर या पेंटिंग":
                val = "Wall Posters or Painting";
                break;
            case "मुंह की बात (रिश्तेदारों या दोस्तों के माध्यम से)":
                val = "Word of Mouth (through relatives or friends)";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_HelplineKnowledge_edit(String val) {
        switch (val) {

            case "Social Media":
                val = "सोशल मीडिया";
                break;
            case "Wall Posters or Painting":
                val = "दीवार के पोस्टर या पेंटिंग";
                break;
            case "Word of Mouth (through relatives or friends)":
                val = "मुंह की बात (रिश्तेदारों या दोस्तों के माध्यम से)";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_PhoneType(String val) {
        switch (val) {
            case "स्मार्टफोन":
                val = "Smartphone";
                break;
            case "कीपैड फोन":
                val = "Keypad phone";
                break;
            default:
                return val;
        }
        return val;
    }
  public static String switch_hi_PhoneType_edit(String val) {
        switch (val) {
            case "Smartphone":
                val = "स्मार्टफोन";
                break;
            case "Keypad phone":
                val = "कीपैड फोन";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_jobSpinner(String val) {
        switch (val) {

            case "है,पार्ट टाईम":
                val = "Yes, part time";
                break;
            case "है,फुल टाइम":
                val = "Yes, full time";
                break;
            case "नहीं है":
                val = "No";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_jobSpinner_edit(String val) {
        switch (val) {

            case "Yes, part time":
                val = "है,पार्ट टाईम";
                break;
            case "Yes, full time":
                val = "है,फुल टाइम";
                break;
            case "No":
                val = "नहीं है";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_DescribeLocationSpinner(String val) {
        switch (val) {

            case "शहरी":
                val = "Urban";
                break;
            case "ग्रामीण":
                val = "Rural";
                break;
            case "नगर":
                val = "Peri-urban";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_DescribeLocationSpinner_edit(String val) {
        switch (val) {

            case "Urban":
                val = "शहरी";
                break;
            case "Rural":
                val = "ग्रामीण";
                break;
            case "Peri-urban":
                val = "नगर";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_Who_Referred_Spinner(String val) {
        switch (val) {

            case "आउटरीच नेता":
                val = "Outreach leaders";
                break;
            case "समुदाय तक पहुंच/स्वयंसेवक":
                val = "Community outreach/ volunteers";
                break;
            case "एनजीओ":
                val = "NGO";
                break;
            case "पुलिस स्टेशन":
                val = "Police station";
                break;
            case "वेबसाइट/सोशल मीडिया":
                val = "Website/ social media";
                break;
            case "अखबार":
                val = "Newspaper";
                break;
            case "नहीं कहना पसंद करें":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_Who_Referred_Spinner_edit(String val) {
        switch (val) {
            case "Outreach leaders":
                val = "आउटरीच नेता";
                break;
            case "Community outreach/ volunteers":
                val = "समुदाय तक पहुंच/स्वयंसेवक";
                break;
            case "NGO":
                val = "एनजीओ";
                break;
            case "Police station":
                val = "पुलिस स्टेशन";
                break;
            case "Website/ social media":
                val = "वेबसाइट/सोशल मीडिया";
                break;
            case "Newspaper":
                val = "अखबार";
                break;
            case "Prefer not to say":
                val = "नहीं कहना पसंद करें";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_am_i_speaking_Spinner(String val) {
        switch (val) {

//            case "स्वयं जीवित रहें":
//                val = "Survivor themselves";
//                break;
                case "खुद हिंसा का सामना करने वाली महिला से":
                val = "Survivor themselves";
                break;
            case "कोई और है (पूछें कि वह कौन और उनका नाम और रिश्ता यहाँ एंटर करें)":
                val = "Someone else (Ask who and enter)";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_am_i_speaking_Spinner_edit(String val) {
        switch (val) {

//            case "Survivor themselves":
//                val = "स्वयं जीवित रहें";
//                break;

                case "Survivor themselves":
                val = "खुद हिंसा का सामना करने वाली महिला से";
                break;
            case "Someone else (Ask who and enter)":
                val = "कोई और है (पूछें कि वह कौन और उनका नाम और रिश्ता यहाँ एंटर करें)";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_survivor_ever_married_Spinner(String val) {
        switch (val) {

            case "हां":
                val = "Yes";
                break;
            case "नहीं":
                val = "No";
                break;
            case "अनिश्चित":
                val = "Unsure";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_survivor_ever_married_Spinner_edit(String val) {
        switch (val) {

            case "Yes":
                val = "हां";
                break;
            case "No":
                val = "नहीं";
                break;
            case "Unsure":
                val = "अनिश्चित";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_type_marriage_Spinner(String val) {
        switch (val) {

            case "माता-पिता की मर्जी से":
                val = "By parents";
                break;
            case "अपनी मर्जी से":
                val = "Own will";
                break;
            case "लिव-इन":
                val = "Single/ live-in";
                break;
            case "अट्टा-साता":
                val = "Atta-sata";
                break;
            case "कोर्ट में":
                val = "In court";
                break;
            case "वविशेष विवाह अधिनियम":
                val = "Special marriage act";
                break;
            case "बाल विवाह":
                val = "Child marriage";
                break;
            case "बताना नहीं चाहते/अनिश्चित हैं":
                val = "Prefer not to say/unsure";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_type_marriage_Spinner_edit(String val) {
        switch (val) {

            case "By parents":
                val = "माता-पिता की मर्जी से";
                break;
            case "Own will":
                val = "अपनी मर्जी से";
                break;
            case "Single/ live-in":
                val = "लिव-इन";
                break;
            case "Atta-sata":
                val = "अट्टा-साता";
                break;
            case "In court":
                val = "कोर्ट में";
                break;
            case "Special marriage act":
                val = "विशेष विवाह अधिनियम";
                break;
            case "Child marriage":
                val = "बाल विवाह";
                break;
            case "Prefer not to say/unsure":
                val = "बताना नहीं चाहते/अनिश्चित हैं";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_survivor_address_Spinner(String val) {
        switch (val) {

            case "मायके का पता":
                val = "Maternal Address";
                break;
            case "ससुराल वालों का पता":
                val = "Address of in-laws";
                break;
            case "कुछ बताना नहीं चाहते हैं":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_survivor_address_Spinner_edit(String val) {
        switch (val) {

            case "Maternal Address":
                val = "मायके का पता";
                break;
            case "Address of in-laws":
                val = "ससुराल वालों का पता";
                break;
            case "Prefer not to say":
                val = "कुछ बताना नहीं चाहते हैं";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_with_whom_living_Spinner(String val) {
        switch (val) {

            case "पति":
                val = "Husband";
                break;
            case "परिवार":
                val = "Family";
                break;
            case "दोस्त":
                val = "Friend";
                break;
            case "अकेले":
                val = "No one";
                break;
            case "कुछ कहना नहीं चाहते हैं":
                val = "Prefer not to say";
                break;
            case "अन्य":
                val = "Other";
                break;

            default:
                return val;
        }
        return val;
    }
    public static String switch_hi_with_whom_living_Spinner_edit(String val) {
        switch (val) {

            case "Husband":
                val = "पति";
                break;
            case "Family":
                val = "परिवार";
                break;
            case "Friend":
                val = "दोस्त";
                break;
            case "No one":
                val = "अकेले";
                break;
            case "Prefer not to say":
                val = "कुछ कहना नहीं चाहते हैं";
                break;
            case "Other":
                val = "अन्य";
                break;

            default:
                return val;
        }
        return val;
    }

    //complaint - end...

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

    public static boolean isValidFileName(String file) {
        String valid = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789`~!@#$%^&*()-_+=[{]}\\|;:'\";:/?.>,< ";
        boolean result = true;
        for (char c : file.toCharArray()) {
            if (!valid.contains(String.valueOf(c))) {
                result = false;
                break;
            }
        }
        return result;
    }
}
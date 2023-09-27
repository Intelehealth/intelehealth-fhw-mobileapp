package org.intelehealth.ezazi.ui.validation;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kaveri Zaware on 27-09-2023
 * email - kaveri@intelehealth.org
 **/
public class CheckValueInDropdown {

    public static boolean checkValueIsInDropdown(String[] dataArray, String enteredText) {
        List<String> dataList = Arrays.asList(dataArray);
        if (dataList.contains(enteredText)) return true;
        else return false;
    }
}

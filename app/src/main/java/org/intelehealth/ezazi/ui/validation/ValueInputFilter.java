package org.intelehealth.ezazi.ui.validation;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kaveri Zaware on 27-09-2023
 * email - kaveri@intelehealth.org
 **/
public class ValueInputFilter implements InputFilter {
    private static final String TAG = "ValueInputFilter";
    private String[] dataArray;
    private List<String> valuesList;


    public ValueInputFilter(String[] dataArray) {
        this.dataArray = dataArray;

        valuesList = Arrays.asList(dataArray);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            if (valuesList.contains(source.toString()))
                return null;
            else
                return "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

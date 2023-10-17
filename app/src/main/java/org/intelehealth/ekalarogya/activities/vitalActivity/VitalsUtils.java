package org.intelehealth.ekalarogya.activities.vitalActivity;

import android.content.Context;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.models.ObsImageModel.Concept;

/**
 * Created by Kaveri Zaware on 06-10-2023
 * email - kaveri@intelehealth.org
 **/
public class VitalsUtils {
    public static String convertHeightIntoFeets(String height, Context context) {
        int val = Integer.parseInt(height);
        double centemeters = val / 2.54;
        int inche = (int) centemeters % 12;
        int feet = (int) centemeters / 12;
        String heightVal = feet + context.getString(R.string.ft) + " " + inche + context.getString(R.string.in);
        System.out.println("value of height=" + val);
        return heightVal;
    }

    public static String convertHeightIntoCm(String height, Context context) {
        height = height.replaceAll(context.getString(R.string.ft), "").replaceAll(context.getString(R.string.in), "");
        String[] heightArr = height.split(" ");
        int feets = Integer.parseInt(heightArr[0]) * 12;
        int inches = Integer.parseInt(heightArr[1]);
        int val = (int) ((feets + inches) * 2.54) + 1;
        System.out.println("value of height=" + val);

        //heightvalue = val + "";
        return val + "";
    }
}

package org.intelehealth.ezazi.partogram.model;

import android.content.Context;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.utilities.UuidDictionary;

/**
 * Created by Kaveri Zaware on 15-09-2023
 * email - kaveri@intelehealth.org
 **/
public class ValidatePartogramFields {

    public static boolean isValidSystolicBP(String systolicBP) {
        if (systolicBP != null && !systolicBP.isEmpty()) {
            int value = Integer.parseInt(systolicBP);
            return value >= 50 && value <= 250;
        } else {
            return true;
        }
    }

    public static boolean isValidDiastolicBP(String systolicBp, String diastolicBp) {
        if (diastolicBp != null && systolicBp != null && !diastolicBp.isEmpty() && !systolicBp.isEmpty()) {
            int value = Integer.parseInt(diastolicBp);
            int systolicBpValue = Integer.parseInt(systolicBp);
            return value >= 30 && value <= 150 && value < systolicBpValue;
        } else {
            return true;
        }
    }


    public static boolean isValidParameter(String enteredValue, String conceptUUID) {
        double value;
        boolean result = true;
        if (enteredValue != null && !enteredValue.isEmpty()) {
            value = Double.parseDouble(enteredValue);
            switch (conceptUUID) {
                case UuidDictionary.BASELINE_FHR:
                    result = value >=Double.parseDouble(AppConstants.MINIMUM_BASELINE_FHR) && value <= Double.parseDouble(AppConstants.MAXIMUM_BASELINE_FHR);
                    break;
                case UuidDictionary.PULSE:
                    result = value >= Double.parseDouble(AppConstants.MINIMUM_PULSE) && value <= Double.parseDouble(AppConstants.MAXIMUM_PULSE);
                    break;
                case UuidDictionary.TEMPERATURE:
                    result = value >= Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS) && value <= Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS);
                    break;
                case UuidDictionary.DURATION_OF_CONTRACTION:
                    result = value >= Double.parseDouble(AppConstants.MINIMUM_CONTRACTION_DURATION) && value <= Double.parseDouble(AppConstants.MAXIMUM_CONTRACTION_DURATION);
                    break;
            }
        }
        return result;
    }


    /*public static  String getFormattedMessage(String stringName, String minimumValue, String maximumValue){
        return stringName
                .replace("%1$s", AppConstants.MINIMUM_BASELINE_FHR)
                .replace("%2$s", AppConstants.MAXIMUM_BASELINE_FHR);
    }*/
}

package org.intelehealth.ezazi.partogram.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.ezazi.partogram.PartogramConstants;
import org.intelehealth.ezazi.partogram.adapter.PartogramQueryListingAdapter;
import org.intelehealth.ezazi.utilities.UuidDictionary;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ParamInfo implements Serializable {
    private String paramSectionName;
    private String paramName;
    private String paramDateType;
    private String[] options;
    private String[] values;
    private boolean isHalfHourField;
    private boolean isFifteenMinField;
    private boolean isOnlyOneHourField;
    private String capturedValue;
    private String conceptUUID;

    private int currentStage;

    private String[] radioOptions;
    private String[] status;

//    private HashMap<String, String> jsonMap;

    private Medication medication;

    private RadioOptions checkedRadioOption = RadioOptions.NO_VALUE;

    public enum RadioOptions {
        YES, NO, NO_VALUE
    }

    public String getParamSectionName() {
        return paramSectionName;
    }

    public void setParamSectionName(String paramSectionName) {
        this.paramSectionName = paramSectionName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamDateType() {
        return paramDateType;
    }

    public void setParamDateType(String paramDateType) {
        this.paramDateType = paramDateType;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public boolean isHalfHourField() {
        return isHalfHourField;
    }

    public void setHalfHourField(boolean halfHourField) {
        isHalfHourField = halfHourField;
    }

    public boolean isFifteenMinField() {
        return isFifteenMinField;
    }

    public void setFifteenMinField(boolean fifteenMinField) {
        isFifteenMinField = fifteenMinField;
    }

    public String getCapturedValue() {
        return capturedValue;
    }

    public void setCapturedValue(String capturedValue) {
        this.capturedValue = capturedValue;
    }

    public String getConceptUUID() {
        return conceptUUID;
    }

    public void setConceptUUID(String conceptUUID) {
        this.conceptUUID = conceptUUID;
    }

    public String[] getStatus() {
        return status;
    }

    public void setStatus(String[] status) {
        this.status = status;
    }

    public boolean isOnlyOneHourField() {
        return isOnlyOneHourField;
    }

    public void setOnlyOneHourField(boolean onlyOneHourField) {
        isOnlyOneHourField = onlyOneHourField;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setRadioOptions(String[] radioOptions) {
        this.radioOptions = radioOptions;
    }

    public String[] getRadioOptions() {
        return radioOptions;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public Medication getMedication() {
        if (medication == null) medication = new Medication();
        return medication;
    }

    public void saveJson() {
        if ((getConceptUUID().equals(UuidDictionary.IV_FLUIDS) && getMedication().isValidIVFluid())
                || (getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN) && getMedication().isValidOxytocin()))
            setCapturedValue(getMedication().toJson());
        else
            setCapturedValue(RadioOptions.NO.name());
    }

//    public void saveValueInJsonMap(String key, String value) {
//        if (jsonMap == null) jsonMap = new HashMap<>();
//        jsonMap.put(key, value);
//    }
//
//    public void convertJsonMapToJSONAndSaveCapturedValue() {
//        if (jsonMap == null) return;
//        setCapturedValue(new Gson().toJson(jsonMap));
//    }

    public boolean isValidJson() {
        Log.e("ParamInfo", "isValidJson: " + getParamName() + " checked: " + checkedRadioOption);
        Log.e("ParamInfo", "isValidJson: " + getParamName() + " value: " + getMedication().toJson());
        if (getConceptUUID().equals(UuidDictionary.IV_FLUIDS) || getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
            if (getCapturedValue() == null) return true;
            else if (checkedRadioOption == RadioOptions.NO) return true;
            else if (checkedRadioOption == RadioOptions.NO_VALUE) return true;
            else {
                if (checkedRadioOption != null && getConceptUUID().equals(UuidDictionary.IV_FLUIDS)) {
                    return getMedication().isValidIVFluid();
                } else if (checkedRadioOption != null && getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
                    return getMedication().isValidOxytocin();
                }
            }
        }
        return true;
    }

    public void setCheckedRadioOption(RadioOptions checkedRadioOption) {
        this.checkedRadioOption = checkedRadioOption;
    }
}
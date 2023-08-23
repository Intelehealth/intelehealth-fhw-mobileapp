package org.intelehealth.ezazi.partogram.model;

import java.io.Serializable;

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
    private String[] ivInfusionStatus;

    public String[] getIvInfusionStatus() {
        return ivInfusionStatus;
    }

    public void setIvInfusionStatus(String[] ivInfusionStatus) {
        this.ivInfusionStatus = ivInfusionStatus;
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
}

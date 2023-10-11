package org.intelehealth.app.models;

import java.io.Serializable;

public class MedicationAidModel implements Serializable {
    private String value;
    private boolean isChecked;

    public MedicationAidModel(String value, boolean isChecked) {
        this.value = value;
        this.isChecked = isChecked;
    }

    public MedicationAidModel() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}

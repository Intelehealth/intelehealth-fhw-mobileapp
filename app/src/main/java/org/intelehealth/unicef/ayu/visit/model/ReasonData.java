package org.intelehealth.unicef.ayu.visit.model;

import java.io.Serializable;

public class ReasonData implements Serializable {
    private String reasonName;
    private boolean isSelected;
    private boolean isEnabled;


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}

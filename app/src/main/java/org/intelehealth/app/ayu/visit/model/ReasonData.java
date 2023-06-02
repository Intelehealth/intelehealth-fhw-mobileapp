package org.intelehealth.app.ayu.visit.model;

import java.io.Serializable;

public class ReasonData implements Serializable {
    private String reasonName; // same as file name
    private String reasonNameLocalized; // locale wise name
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

    public String getReasonNameLocalized() {
        return reasonNameLocalized;
    }

    public void setReasonNameLocalized(String reasonNameLocalized) {
        this.reasonNameLocalized = reasonNameLocalized;
    }
}

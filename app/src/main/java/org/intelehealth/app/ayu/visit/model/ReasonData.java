package org.intelehealth.app.ayu.visit.model;

import java.util.List;

public class ReasonData {
    private String reasonName;
    private boolean isSelected;



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
}

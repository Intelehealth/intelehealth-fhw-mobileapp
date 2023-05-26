package org.intelehealth.ezazi.models;

import org.intelehealth.ezazi.ui.dialog.model.MultiChoiceItem;

public class RiskFactorsModel implements MultiChoiceItem {
    String riskFactor;

    public RiskFactorsModel(String riskFactor, int position) {
        this.riskFactor = riskFactor;
        this.position = position;
    }

    int position;

    @Override
    public boolean isHeader() {
        return false;
    }
}

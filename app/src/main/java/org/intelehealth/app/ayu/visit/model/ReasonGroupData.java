package org.intelehealth.app.ayu.visit.model;

import java.util.List;

public class ReasonGroupData {
    private String alphabet;
    private List<String> reasons;

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }
}

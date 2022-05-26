package org.intelehealth.ekalarogya.activities.identificationActivity.data_classes;

public class MedicalHistory {
    private String hypertension = "-";
    private String diabetes = "-";
    private String arthritis = "-";
    private String anaemia = "-";
    private String anySurgeries = "-";
    private String reasonForSurgery = "-";
    private String other = "-";

    public String getHypertension() {
        return hypertension;
    }

    public void setHypertension(String hypertension) {
        this.hypertension = hypertension;
    }

    public String getDiabetes() {
        return diabetes;
    }

    public void setDiabetes(String diabetes) {
        this.diabetes = diabetes;
    }

    public String getArthritis() {
        return arthritis;
    }

    public void setArthritis(String arthritis) {
        this.arthritis = arthritis;
    }

    public String getAnaemia() {
        return anaemia;
    }

    public void setAnaemia(String anaemia) {
        this.anaemia = anaemia;
    }

    public String getAnySurgeries() {
        return anySurgeries;
    }

    public void setAnySurgeries(String anySurgeries) {
        this.anySurgeries = anySurgeries;
    }

    public String getReasonForSurgery() {
        return reasonForSurgery;
    }

    public void setReasonForSurgery(String reasonForSurgery) {
        this.reasonForSurgery = reasonForSurgery;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}

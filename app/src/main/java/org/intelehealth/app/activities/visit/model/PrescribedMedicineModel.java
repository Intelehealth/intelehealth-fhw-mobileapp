package org.intelehealth.app.activities.visit.model;

public class PrescribedMedicineModel {
    private String medicineName;
    private String strength;
    private String noOfDays;
    private String timing;
    private String remark;

    public String getMedicineName() {
        return medicineName != null ? medicineName : "";
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getStrength() {
        return strength != null ? strength : "";
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getNoOfDays() {
        return noOfDays != null ? noOfDays : "";
    }

    public void setNoOfDays(String noOfDays) {
        this.noOfDays = noOfDays;
    }

    public String getTiming() {
        return timing != null ? timing : "";
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getRemark() {
        return remark != null ? remark : "";
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

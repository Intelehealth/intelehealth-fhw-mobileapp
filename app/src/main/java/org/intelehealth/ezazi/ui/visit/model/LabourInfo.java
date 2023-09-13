package org.intelehealth.ezazi.ui.visit.model;

import android.util.Log;

import org.intelehealth.ezazi.utilities.UuidDictionary;

/**
 * Created by Vaghela Mithun R. on 17-08-2023 - 22:57.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class LabourInfo {
    private String birthOutcome;
    private String birthWeight;
    private String apgar1Min;
    private String apgar5Min;
    private String gender;
    private String babyStatus;
    private String motherStatus;
    private String otherComment;
    private String motherDeceasedReason;

    public String getBirthOutcome() {
        return birthOutcome;
    }

    public void setBirthOutcome(String birthOutcome) {
        this.birthOutcome = birthOutcome;
    }

    public String getBirthWeight() {
        return birthWeight;
    }

    public void setBirthWeight(String birthWeight) {
        this.birthWeight = birthWeight;
    }

    public String getApgar1Min() {
        return apgar1Min;
    }

    public void setApgar1Min(String apgar1Min) {
        this.apgar1Min = apgar1Min;
    }

    public String getApgar5Min() {
        return apgar5Min;
    }

    public void setApgar5Min(String apgar5Min) {
        this.apgar5Min = apgar5Min;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBabyStatus() {
        return babyStatus;
    }

    public void setBabyStatus(String babyStatus) {
        this.babyStatus = babyStatus;
    }

    public String getMotherStatus() {
        return motherStatus;
    }

    public void setMotherStatus(String motherStatus) {
        this.motherStatus = motherStatus;
    }

    public String getOtherComment() {
        return otherComment;
    }

    public void setOtherComment(String otherComment) {
        this.otherComment = otherComment;
    }

    public String getMotherDeceasedReason() {
        return motherDeceasedReason;
    }

    public void setMotherDeceasedReason(String motherDeceasedReason) {
        this.motherDeceasedReason = motherDeceasedReason;
    }

    public boolean isInvalidData() {
        return birthOutcome == null && birthWeight == null
                && apgar1Min == null && apgar5Min == null
                && gender == null && babyStatus == null
                && motherStatus == null && otherComment == null
                && motherDeceasedReason == null;
    }


    public boolean isValidSystolicBP(String systolicBP) {
        if (systolicBP != null && !systolicBP.isEmpty()) {
            int value = Integer.parseInt(systolicBP);
            return value >= 50 && value <= 250;
        } else {
            return true;
        }
    }

    public boolean isValidDiastolicBP(String systolicBp, String diastolicBp) {
        if (diastolicBp != null && systolicBp != null && !diastolicBp.isEmpty() && !systolicBp.isEmpty()) {
            int value = Integer.parseInt(diastolicBp);
            int systolicBpValue = Integer.parseInt(systolicBp);
            return value >= 30 && value <= 150 && value < systolicBpValue;
        } else {
            return true;
        }
    }


    public boolean isValidParameter(String enteredValue, String conceptUUID) {
        double value;
        boolean result = true;
        if (enteredValue != null && !enteredValue.isEmpty()) {
            value = Double.parseDouble(enteredValue);
            switch (conceptUUID) {
                case UuidDictionary.BASELINE_FHR:
                    result = value >= 90 && value <= 220;
                    break;
                case UuidDictionary.PULSE:
                    result = value >= 30 && value <= 240;
                    break;
               /* case UuidDictionary.TEMPERATURE:
                    result = value >= 32 && value <= 42.8;
                    break;*/
                case UuidDictionary.DURATION_OF_CONTRACTION:
                    result = value >= 0 && value <= 100;
                    break;
            }
        }
        return result;
    }
}

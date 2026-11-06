package org.intelehealth.app.ayu.visit.ncd;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The CBAC answers of one visit, held in memory only.
 * Nothing here is saved or synced yet; the storage ticket will read from this object.
 */
public class CbacAssessmentState {

    private int ageYears;
    // Question id → selected option id, as in the CBAC file
    private final Map<String, String> answers = new LinkedHashMap<>();
    // Typed waist rounded to 0.5 cm; its band option id is in answers
    private double waistCm;
    private int totalScore;

    public int getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(int ageYears) {
        this.ageYears = ageYears;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public double getWaistCm() {
        return waistCm;
    }

    public void setWaistCm(double waistCm) {
        this.waistCm = waistCm;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public boolean isHighRisk() {
        return CbacAssessmentRules.isHighRisk(totalScore);
    }
}

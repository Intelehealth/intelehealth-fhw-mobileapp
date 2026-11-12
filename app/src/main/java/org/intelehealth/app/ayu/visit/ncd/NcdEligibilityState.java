package org.intelehealth.app.ayu.visit.ncd;

import java.util.ArrayList;
import java.util.List;

/**
 * The NCD eligibility answers of one visit.
 * NcdScreeningRepository saves them, with the medicine photos, when the step is done.
 */
public class NcdEligibilityState {

    private Boolean pregnant; // null when the question was not shown
    private boolean multimorbidity;
    private String knownCaseOptionId;
    private String knownCaseText; // English option text, as saved
    private final List<String> medicinePhotoPaths = new ArrayList<>();
    private final List<String> urgentSymptomIds = new ArrayList<>();
    private final List<String> urgentSymptomTexts = new ArrayList<>(); // English option texts, as saved
    private NcdEligibilityRules.Result result;

    public Boolean getPregnant() {
        return pregnant;
    }

    public void setPregnant(Boolean pregnant) {
        this.pregnant = pregnant;
    }

    public boolean isMultimorbidity() {
        return multimorbidity;
    }

    public void setMultimorbidity(boolean multimorbidity) {
        this.multimorbidity = multimorbidity;
    }

    public String getKnownCaseOptionId() {
        return knownCaseOptionId;
    }

    public void setKnownCaseOptionId(String knownCaseOptionId) {
        this.knownCaseOptionId = knownCaseOptionId;
    }

    public String getKnownCaseText() {
        return knownCaseText;
    }

    public void setKnownCaseText(String knownCaseText) {
        this.knownCaseText = knownCaseText;
    }

    public List<String> getMedicinePhotoPaths() {
        return medicinePhotoPaths;
    }

    public List<String> getUrgentSymptomIds() {
        return urgentSymptomIds;
    }

    public List<String> getUrgentSymptomTexts() {
        return urgentSymptomTexts;
    }

    public NcdEligibilityRules.Result getResult() {
        return result;
    }

    public void setResult(NcdEligibilityRules.Result result) {
        this.result = result;
    }

    public boolean isKnownCaseBoth() {
        return NcdEligibilityRules.KNOWN_CASE_BOTH.equals(knownCaseOptionId);
    }

    public NcdEligibilityRules.Reason getNotEligibleReason() {
        return NcdEligibilityRules.notEligibleReason(Boolean.TRUE.equals(pregnant), multimorbidity, isKnownCaseBoth());
    }
}

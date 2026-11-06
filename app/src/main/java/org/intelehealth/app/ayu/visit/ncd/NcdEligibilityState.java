package org.intelehealth.app.ayu.visit.ncd;

import java.util.ArrayList;
import java.util.List;

/**
 * The NCD eligibility answers of one visit, held in memory only.
 * Nothing here is saved or synced yet; the storage ticket will read from this object.
 */
public class NcdEligibilityState {

    private Boolean pregnant; // null when the question was not shown
    private boolean multimorbidity;
    private String knownCaseOptionId;
    private final List<String> medicinePhotoPaths = new ArrayList<>();
    private final List<String> urgentSymptomIds = new ArrayList<>();
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

    public List<String> getMedicinePhotoPaths() {
        return medicinePhotoPaths;
    }

    public List<String> getUrgentSymptomIds() {
        return urgentSymptomIds;
    }

    public NcdEligibilityRules.Result getResult() {
        return result;
    }

    public void setResult(NcdEligibilityRules.Result result) {
        this.result = result;
    }
}

package org.intelehealth.app.ayu.visit.ncd;

/**
 * Which DM/HTN screening protocols the NCD result leaves at Visit reason (no Android code, so it is unit tested).
 * Skipped ones are hidden; the others stay for the health worker to choose. Rules signed off by the PO.
 */
public final class NcdScreeningPlan {

    // Protocol (file) names in engines/
    public static final String DM_SCREENING = "Screening for Diabetes";
    public static final String HTN_SCREENING = "Screening for Hypertension";

    public enum SkipReason { CONSENT_DECLINED, URGENT, NOT_ELIGIBLE, KNOWN_DM, KNOWN_HTN, LOW_CBAC_SCORE }

    private final SkipReason dmSkipReason; // null when DM screening is offered
    private final SkipReason htnSkipReason; // null when HTN screening is offered

    private NcdScreeningPlan(SkipReason dmSkipReason, SkipReason htnSkipReason) {
        this.dmSkipReason = dmSkipReason;
        this.htnSkipReason = htnSkipReason;
    }

    public static NcdScreeningPlan consentDeclined() {
        return new NcdScreeningPlan(SkipReason.CONSENT_DECLINED, SkipReason.CONSENT_DECLINED);
    }

    // cbacScore is null when CBAC was not done
    public static NcdScreeningPlan of(NcdEligibilityRules.Result result, String knownCaseOptionId, Integer cbacScore) {
        if (result == NcdEligibilityRules.Result.URGENT) return new NcdScreeningPlan(SkipReason.URGENT, SkipReason.URGENT);
        if (result == NcdEligibilityRules.Result.NOT_ELIGIBLE) return new NcdScreeningPlan(SkipReason.NOT_ELIGIBLE, SkipReason.NOT_ELIGIBLE);
        // A known case skips its own screening whatever the CBAC score
        if (NcdEligibilityRules.KNOWN_CASE_DM.equals(knownCaseOptionId)) return new NcdScreeningPlan(SkipReason.KNOWN_DM, null);
        if (NcdEligibilityRules.KNOWN_CASE_HTN.equals(knownCaseOptionId)) return new NcdScreeningPlan(null, SkipReason.KNOWN_HTN);
        if (cbacScore != null && !CbacAssessmentRules.isHighRisk(cbacScore)) {
            return new NcdScreeningPlan(SkipReason.LOW_CBAC_SCORE, SkipReason.LOW_CBAC_SCORE);
        }
        return new NcdScreeningPlan(null, null);
    }

    public SkipReason getDmSkipReason() {
        return dmSkipReason;
    }

    public SkipReason getHtnSkipReason() {
        return htnSkipReason;
    }

    public boolean isHidden(String protocolName) {
        if (DM_SCREENING.equalsIgnoreCase(protocolName)) return dmSkipReason != null;
        if (HTN_SCREENING.equalsIgnoreCase(protocolName)) return htnSkipReason != null;
        return false;
    }
}

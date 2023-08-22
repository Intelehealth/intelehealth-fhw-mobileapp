package org.intelehealth.ezazi.ui.visit.model;

/**
 * Created by Vaghela Mithun R. on 21-08-2023 - 19:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class VisitOutcome {
    private String outcome;
    private boolean hasMotherDeceased;
    private String motherDeceasedReason;
    private String otherComment;

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public boolean isHasMotherDeceased() {
        return hasMotherDeceased;
    }

    public void setHasMotherDeceased(boolean hasMotherDeceased) {
        this.hasMotherDeceased = hasMotherDeceased;
    }

    public String getMotherDeceasedReason() {
        return motherDeceasedReason;
    }

    public void setMotherDeceasedReason(String motherDeceasedReason) {
        this.motherDeceasedReason = motherDeceasedReason;
    }

    public String getOtherComment() {
        return otherComment;
    }

    public void setOtherComment(String otherComment) {
        this.otherComment = otherComment;
    }
}

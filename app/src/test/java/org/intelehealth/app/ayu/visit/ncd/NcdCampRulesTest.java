package org.intelehealth.app.ayu.visit.ncd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.intelehealth.app.ayu.visit.ncd.NcdCampRules.Condition;
import org.intelehealth.app.ayu.visit.ncd.NcdEligibilityRules.Result;
import org.junit.Test;

import java.util.EnumSet;

public class NcdCampRulesTest {

    private static final BpReading HIGH_BP = new BpReading(2, 150, 85);
    private static final BpReading NORMAL_BP = new BpReading(1, 130, 85);

    private static NcdScreeningPlan newCase(int cbacScore) {
        return NcdScreeningPlan.of(Result.ELIGIBLE, "NCD_KNOWN_CASE_NONE", cbacScore);
    }

    @Test
    public void noBannerWithoutAnEligibleScreening() {
        assertNull(NcdCampRules.campEligible(null, HIGH_BP, 200.0));
        assertNull(NcdCampRules.campEligible(NcdScreeningPlan.consentDeclined(), HIGH_BP, 200.0));
        assertNull(NcdCampRules.campEligible(NcdScreeningPlan.of(Result.URGENT, "NCD_KNOWN_CASE_NONE", null), HIGH_BP, null));
        assertNull(NcdCampRules.campEligible(NcdScreeningPlan.of(Result.NOT_ELIGIBLE, NcdEligibilityRules.KNOWN_CASE_BOTH, null), HIGH_BP, null));
    }

    @Test
    public void highBpOrHighRbsMakesANewCaseEligible() {
        assertEquals(Boolean.TRUE, NcdCampRules.campEligible(newCase(6), HIGH_BP, 100.0));
        assertEquals(Boolean.TRUE, NcdCampRules.campEligible(newCase(6), NORMAL_BP, 141.0));
        assertEquals(Boolean.TRUE, NcdCampRules.campEligible(newCase(6), new BpReading(1, 120, 90), null));
    }

    @Test
    public void normalBpAndRbsAtCutOffIsNotEligible() {
        assertEquals(Boolean.FALSE, NcdCampRules.campEligible(newCase(6), NORMAL_BP, 140.0));
        assertEquals(Boolean.FALSE, NcdCampRules.campEligible(newCase(6), new BpReading(1, 139, 89), null));
    }

    @Test
    public void cbacScore4OrLessIsNotEligible() {
        assertEquals(Boolean.FALSE, NcdCampRules.campEligible(newCase(4), HIGH_BP, 200.0));
    }

    @Test
    public void knownHtnIsDecidedByRbsOnly() {
        NcdScreeningPlan knownHtn = NcdScreeningPlan.of(Result.ELIGIBLE, NcdEligibilityRules.KNOWN_CASE_HTN, 6);
        assertEquals(Boolean.FALSE, NcdCampRules.campEligible(knownHtn, HIGH_BP, 120.0));
        assertEquals(Boolean.TRUE, NcdCampRules.campEligible(knownHtn, NORMAL_BP, 150.0));
    }

    @Test
    public void knownDmIsDecidedByBpOnly() {
        NcdScreeningPlan knownDm = NcdScreeningPlan.of(Result.ELIGIBLE, NcdEligibilityRules.KNOWN_CASE_DM, 6);
        assertEquals(Boolean.FALSE, NcdCampRules.campEligible(knownDm, NORMAL_BP, 250.0));
        assertEquals(Boolean.TRUE, NcdCampRules.campEligible(knownDm, HIGH_BP, null));
    }

    @Test
    public void campConditionsNameWhatMadeThePatientEligible() {
        assertEquals(EnumSet.of(Condition.DIABETES, Condition.HYPERTENSION), NcdCampRules.campConditions(newCase(6), HIGH_BP, 200.0));
        assertEquals(EnumSet.of(Condition.HYPERTENSION), NcdCampRules.campConditions(newCase(6), HIGH_BP, 120.0));
        assertEquals(EnumSet.of(Condition.DIABETES), NcdCampRules.campConditions(newCase(6), NORMAL_BP, 141.0));
        assertEquals(EnumSet.noneOf(Condition.class), NcdCampRules.campConditions(newCase(4), HIGH_BP, 200.0));
        assertNull(NcdCampRules.campConditions(NcdScreeningPlan.consentDeclined(), HIGH_BP, 200.0));
    }
}

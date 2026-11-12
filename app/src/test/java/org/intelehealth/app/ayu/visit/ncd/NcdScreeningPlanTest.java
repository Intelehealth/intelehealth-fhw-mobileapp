package org.intelehealth.app.ayu.visit.ncd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.intelehealth.app.ayu.visit.ncd.NcdEligibilityRules.Result;
import org.intelehealth.app.ayu.visit.ncd.NcdScreeningPlan.SkipReason;
import org.junit.Test;

// One test per row of the NAS-1861 decision table
public class NcdScreeningPlanTest {

    private static final String DM = NcdScreeningPlan.DM_SCREENING;
    private static final String HTN = NcdScreeningPlan.HTN_SCREENING;

    @Test
    public void declinedConsentHidesBoth() {
        NcdScreeningPlan plan = NcdScreeningPlan.consentDeclined();
        assertTrue(plan.isHidden(DM));
        assertTrue(plan.isHidden(HTN));
        assertEquals(SkipReason.CONSENT_DECLINED, plan.getDmSkipReason());
    }

    @Test
    public void urgentHidesBoth() {
        NcdScreeningPlan plan = NcdScreeningPlan.of(Result.URGENT, "NCD_KNOWN_CASE_NONE", null);
        assertTrue(plan.isHidden(DM));
        assertTrue(plan.isHidden(HTN));
        assertEquals(SkipReason.URGENT, plan.getHtnSkipReason());
    }

    @Test
    public void notEligibleHidesBoth() {
        NcdScreeningPlan plan = NcdScreeningPlan.of(Result.NOT_ELIGIBLE, NcdEligibilityRules.KNOWN_CASE_BOTH, null);
        assertTrue(plan.isHidden(DM));
        assertTrue(plan.isHidden(HTN));
        assertEquals(SkipReason.NOT_ELIGIBLE, plan.getDmSkipReason());
    }

    @Test
    public void knownDmShowsOnlyHtnWhateverTheScore() {
        for (int score : new int[]{2, 4, 5, 12}) {
            NcdScreeningPlan plan = NcdScreeningPlan.of(Result.ELIGIBLE, NcdEligibilityRules.KNOWN_CASE_DM, score);
            assertTrue(plan.isHidden(DM));
            assertFalse(plan.isHidden(HTN));
            assertEquals(SkipReason.KNOWN_DM, plan.getDmSkipReason());
        }
    }

    @Test
    public void knownHtnShowsOnlyDmWhateverTheScore() {
        for (int score : new int[]{2, 4, 5, 12}) {
            NcdScreeningPlan plan = NcdScreeningPlan.of(Result.ELIGIBLE, NcdEligibilityRules.KNOWN_CASE_HTN, score);
            assertFalse(plan.isHidden(DM));
            assertTrue(plan.isHidden(HTN));
            assertEquals(SkipReason.KNOWN_HTN, plan.getHtnSkipReason());
        }
    }

    @Test
    public void newCaseWithScore4OrLessHidesBoth() {
        for (String knownCase : new String[]{"NCD_KNOWN_CASE_NONE", "NCD_KNOWN_CASE_VAGUE"}) {
            NcdScreeningPlan atCutOff = NcdScreeningPlan.of(Result.ELIGIBLE, knownCase, 4);
            assertTrue(atCutOff.isHidden(DM));
            assertTrue(atCutOff.isHidden(HTN));
            assertEquals(SkipReason.LOW_CBAC_SCORE, atCutOff.getDmSkipReason());
            assertTrue(NcdScreeningPlan.of(Result.ELIGIBLE, knownCase, 1).isHidden(DM));
        }
    }

    @Test
    public void newCaseAbove4ShowsBoth() {
        for (int score : new int[]{5, 12}) {
            NcdScreeningPlan plan = NcdScreeningPlan.of(Result.ELIGIBLE, "NCD_KNOWN_CASE_NONE", score);
            assertFalse(plan.isHidden(DM));
            assertFalse(plan.isHidden(HTN));
            assertNull(plan.getDmSkipReason());
        }
    }

    @Test
    public void missingScoreHidesNothing() {
        NcdScreeningPlan plan = NcdScreeningPlan.of(Result.ELIGIBLE, "NCD_KNOWN_CASE_NONE", null);
        assertFalse(plan.isHidden(DM));
        assertFalse(plan.isHidden(HTN));
    }

    @Test
    public void otherProtocolsAreNeverHidden() {
        NcdScreeningPlan plan = NcdScreeningPlan.consentDeclined();
        assertFalse(plan.isHidden("Fever"));
        assertFalse(plan.isHidden("Follow up in person"));
    }

    @Test
    public void savedValuesAreReadBack() {
        String saved = "{\"multimorbidity\":\"No\",\"knownCase\":\"Yes, Diabetes only\",\"urgentSymptoms\":[],\"result\":\"Eligible\"}";
        assertEquals(Result.ELIGIBLE, NcdScreeningValues.savedResult(saved));
        assertEquals("Yes, Diabetes only", NcdScreeningValues.savedKnownCaseText(saved));
        assertEquals(Result.NOT_ELIGIBLE, NcdScreeningValues.savedResult("{\"result\":\"Not eligible\"}"));
        assertEquals(Result.URGENT, NcdScreeningValues.savedResult("{\"result\":\"Urgent attention\"}"));
        assertEquals(Integer.valueOf(4), NcdScreeningValues.savedScore("4"));
        assertEquals(Integer.valueOf(7), NcdScreeningValues.savedScore("7.0"));
    }

    @Test
    public void unreadableSavedValuesGiveNull() {
        assertNull(NcdScreeningValues.savedResult(null));
        assertNull(NcdScreeningValues.savedResult("not json"));
        assertNull(NcdScreeningValues.savedResult("{\"result\":\"Something else\"}"));
        assertNull(NcdScreeningValues.savedKnownCaseText("{}"));
        assertNull(NcdScreeningValues.savedScore(null));
        assertNull(NcdScreeningValues.savedScore("x"));
    }
}

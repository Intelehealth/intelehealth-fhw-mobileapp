package org.intelehealth.app.ayu.visit.ncd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.intelehealth.app.ayu.visit.ncd.NcdEligibilityRules.Result;
import org.junit.Test;

public class NcdEligibilityRulesTest {

    @Test
    public void consentIsShownFromAge30() {
        assertFalse(NcdEligibilityRules.isScreeningAge(29.11f));
        assertTrue(NcdEligibilityRules.isScreeningAge(30.00f));
        assertTrue(NcdEligibilityRules.isScreeningAge(30.06f));
        assertTrue(NcdEligibilityRules.isScreeningAge(75.00f));
    }

    @Test
    public void missingAgeSkipsConsent() {
        assertFalse(NcdEligibilityRules.isScreeningAge(0f));
    }

    @Test
    public void pregnancyShownForWomen30To49() {
        assertTrue(NcdEligibilityRules.isPregnancyQuestionShown("F", 30.00f));
        assertTrue(NcdEligibilityRules.isPregnancyQuestionShown("F", 49.00f));
        assertTrue(NcdEligibilityRules.isPregnancyQuestionShown("f", 49.11f));
    }

    @Test
    public void pregnancySkippedOutsideAgeRange() {
        assertFalse(NcdEligibilityRules.isPregnancyQuestionShown("F", 29.11f));
        assertFalse(NcdEligibilityRules.isPregnancyQuestionShown("F", 50.00f));
    }

    @Test
    public void pregnancySkippedForOtherGenders() {
        assertFalse(NcdEligibilityRules.isPregnancyQuestionShown("M", 35.00f));
        assertFalse(NcdEligibilityRules.isPregnancyQuestionShown("O", 35.00f));
        assertFalse(NcdEligibilityRules.isPregnancyQuestionShown("", 35.00f));
        assertFalse(NcdEligibilityRules.isPregnancyQuestionShown(null, 35.00f));
    }

    @Test
    public void noExclusionsIsEligible() {
        assertEquals(Result.ELIGIBLE, NcdEligibilityRules.evaluate(false, false, false, false));
    }

    @Test
    public void pregnancyOrMultimorbidityIsNotEligible() {
        assertEquals(Result.NOT_ELIGIBLE, NcdEligibilityRules.evaluate(true, false, false, false));
        assertEquals(Result.NOT_ELIGIBLE, NcdEligibilityRules.evaluate(false, true, false, false));
        assertEquals(Result.NOT_ELIGIBLE, NcdEligibilityRules.evaluate(true, true, false, false));
    }

    @Test
    public void knownCaseOfBothIsNotEligible() {
        assertEquals(Result.NOT_ELIGIBLE, NcdEligibilityRules.evaluate(false, false, true, false));
    }

    @Test
    public void urgentSymptomWinsOverExclusions() {
        assertEquals(Result.URGENT, NcdEligibilityRules.evaluate(false, false, false, true));
        assertEquals(Result.URGENT, NcdEligibilityRules.evaluate(true, true, true, true));
    }

    @Test
    public void onlyEligibleGoesToCbac() {
        assertTrue(NcdEligibilityRules.isCbacShown(Result.ELIGIBLE));
        assertFalse(NcdEligibilityRules.isCbacShown(Result.NOT_ELIGIBLE));
        assertFalse(NcdEligibilityRules.isCbacShown(Result.URGENT));
        assertFalse(NcdEligibilityRules.isCbacShown(null));
    }
}

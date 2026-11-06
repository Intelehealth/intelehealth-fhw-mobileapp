package org.intelehealth.app.ayu.visit.ncd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CbacAssessmentRulesTest {

    @Test
    public void waistQuestionMatchesPatientGender() {
        assertTrue(CbacAssessmentRules.isQuestionShown("1", "M"));
        assertFalse(CbacAssessmentRules.isQuestionShown("1", "F"));
        assertTrue(CbacAssessmentRules.isQuestionShown("0", "F"));
        assertFalse(CbacAssessmentRules.isQuestionShown("0", "M"));
        assertFalse(CbacAssessmentRules.isQuestionShown("1", "O"));
        assertFalse(CbacAssessmentRules.isQuestionShown("0", "O"));
    }

    @Test
    public void questionsWithoutGenderAlwaysShow() {
        assertTrue(CbacAssessmentRules.isQuestionShown(null, "M"));
        assertTrue(CbacAssessmentRules.isQuestionShown("", "F"));
        assertTrue(CbacAssessmentRules.isQuestionShown("", "O"));
    }

    @Test
    public void agePointsFollowTheBands() {
        assertEquals(0, CbacAssessmentRules.agePoints(29));
        assertEquals(1, CbacAssessmentRules.agePoints(30));
        assertEquals(1, CbacAssessmentRules.agePoints(39));
        assertEquals(2, CbacAssessmentRules.agePoints(40));
        assertEquals(2, CbacAssessmentRules.agePoints(49));
        assertEquals(3, CbacAssessmentRules.agePoints(50));
        assertEquals(3, CbacAssessmentRules.agePoints(59));
        assertEquals(4, CbacAssessmentRules.agePoints(60));
        assertEquals(4, CbacAssessmentRules.agePoints(85));
    }

    @Test
    public void pointsReadFromOptionText() {
        assertEquals(Integer.valueOf(0), CbacAssessmentRules.pointsFromText("Never (0)"));
        assertEquals(Integer.valueOf(1), CbacAssessmentRules.pointsFromText("Past user / Sometimes now (1)"));
        assertEquals(Integer.valueOf(2), CbacAssessmentRules.pointsFromText("More than 100 cm (2) "));
        assertEquals(Integer.valueOf(1), CbacAssessmentRules.pointsFromText("Less than 150 minutes (1)"));
    }

    @Test
    public void textWithoutPointsGivesNull() {
        assertNull(CbacAssessmentRules.pointsFromText("Never"));
        assertNull(CbacAssessmentRules.pointsFromText("(1) Yes"));
        assertNull(CbacAssessmentRules.pointsFromText(null));
    }

    @Test
    public void typedWaistParsesDecimals() {
        assertEquals(85.7, CbacAssessmentRules.parseWaistCm("85.7"), 0);
        assertEquals(90.0, CbacAssessmentRules.parseWaistCm(" 90 "), 0);
        assertEquals(85.5, CbacAssessmentRules.parseWaistCm("85,5"), 0);
        assertEquals(85.0, CbacAssessmentRules.parseWaistCm("85."), 0);
    }

    @Test
    public void invalidTypedWaistGivesNull() {
        assertNull(CbacAssessmentRules.parseWaistCm(null));
        assertNull(CbacAssessmentRules.parseWaistCm(""));
        assertNull(CbacAssessmentRules.parseWaistCm("."));
        assertNull(CbacAssessmentRules.parseWaistCm("0"));
        assertNull(CbacAssessmentRules.parseWaistCm("%"));
    }

    @Test
    public void waistRoundsToNearestHalf() {
        assertEquals(85.5, CbacAssessmentRules.roundWaistToHalf(85.7), 0);
        assertEquals(86.0, CbacAssessmentRules.roundWaistToHalf(85.9), 0);
        assertEquals(85.0, CbacAssessmentRules.roundWaistToHalf(85.2), 0);
        assertEquals(85.5, CbacAssessmentRules.roundWaistToHalf(85.25), 0);
        assertEquals(90.0, CbacAssessmentRules.roundWaistToHalf(90), 0);
    }

    @Test
    public void maleWaistBands() {
        assertEquals(0, CbacAssessmentRules.waistBand("M", 90));
        assertEquals(1, CbacAssessmentRules.waistBand("M", 90.5));
        assertEquals(1, CbacAssessmentRules.waistBand("M", 100));
        assertEquals(2, CbacAssessmentRules.waistBand("M", 100.5));
    }

    @Test
    public void femaleWaistBands() {
        assertEquals(0, CbacAssessmentRules.waistBand("F", 80));
        assertEquals(1, CbacAssessmentRules.waistBand("F", 80.5));
        assertEquals(1, CbacAssessmentRules.waistBand("F", 90));
        assertEquals(2, CbacAssessmentRules.waistBand("F", 90.5));
    }

    @Test
    public void noWaistBandForOtherGenders() {
        assertEquals(-1, CbacAssessmentRules.waistBand("O", 85));
        assertEquals(-1, CbacAssessmentRules.waistBand(null, 85));
    }

    @Test
    public void fourIsLowRiskFiveIsHigh() {
        assertFalse(CbacAssessmentRules.isHighRisk(0));
        assertFalse(CbacAssessmentRules.isHighRisk(4));
        assertTrue(CbacAssessmentRules.isHighRisk(5));
        assertTrue(CbacAssessmentRules.isHighRisk(12));
    }
}

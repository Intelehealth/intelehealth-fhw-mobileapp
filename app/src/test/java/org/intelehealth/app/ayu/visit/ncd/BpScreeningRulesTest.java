package org.intelehealth.app.ayu.visit.ncd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.intelehealth.app.ayu.visit.ncd.NcdEligibilityRules.Result;
import org.junit.Test;

import java.util.Arrays;

public class BpScreeningRulesTest {

    private static BpReading bp(int number, int sys, int dia) {
        return new BpReading(number, sys, dia);
    }

    @Test
    public void thresholdIsSystolic140OrDiastolic90() {
        assertFalse(BpScreeningRules.atOrAboveThreshold(bp(1, 139, 89)));
        assertTrue(BpScreeningRules.atOrAboveThreshold(bp(1, 140, 60)));
        assertTrue(BpScreeningRules.atOrAboveThreshold(bp(1, 120, 90)));
    }

    @Test
    public void readingsDifferWhenEitherValueMovesMoreThan5() {
        assertFalse(BpScreeningRules.differ(bp(1, 150, 95), bp(2, 145, 100)));
        assertTrue(BpScreeningRules.differ(bp(1, 150, 95), bp(2, 144, 95)));
        assertTrue(BpScreeningRules.differ(bp(1, 150, 95), bp(2, 150, 101)));
    }

    @Test
    public void reading1BelowThresholdIsFinal() {
        BpReading reading1 = bp(1, 130, 85);
        assertEquals(1, BpScreeningRules.requiredCount(reading1, null));
        assertSame(reading1, BpScreeningRules.finalReading(reading1, null, null));
    }

    @Test
    public void highReading1NeedsReading2() {
        BpReading reading1 = bp(1, 150, 90);
        assertEquals(2, BpScreeningRules.requiredCount(reading1, null));
        assertNull(BpScreeningRules.finalReading(reading1, null, null));
    }

    @Test
    public void closeReading2IsFinal() {
        BpReading reading1 = bp(1, 150, 90);
        BpReading reading2 = bp(2, 145, 85);
        assertEquals(2, BpScreeningRules.requiredCount(reading1, reading2));
        assertSame(reading2, BpScreeningRules.finalReading(reading1, reading2, null));
    }

    @Test
    public void differentReading2NeedsReading3WhichIsFinal() {
        BpReading reading1 = bp(1, 140, 90);
        BpReading reading2 = bp(2, 150, 90);
        assertEquals(3, BpScreeningRules.requiredCount(reading1, reading2));
        assertNull(BpScreeningRules.finalReading(reading1, reading2, null));
        BpReading reading3 = bp(3, 150, 90);
        assertSame(reading3, BpScreeningRules.finalReading(reading1, reading2, reading3));
    }

    @Test
    public void lastSavedReadingIsTheHighestNumber() {
        BpReading reading3 = bp(3, 150, 80);
        assertSame(reading3, BpScreeningRules.lastReading(Arrays.asList(bp(2, 160, 80), reading3, bp(1, 150, 90))));
        assertNull(BpScreeningRules.lastReading(null));
    }

    @Test
    public void multipleReadingsOnlyWhenHtnScreeningIsOffered() {
        assertFalse(BpScreeningRules.usesMultipleReadings(null));
        assertFalse(BpScreeningRules.usesMultipleReadings(NcdScreeningPlan.consentDeclined()));
        assertTrue(BpScreeningRules.usesMultipleReadings(NcdScreeningPlan.of(Result.ELIGIBLE, "NCD_KNOWN_CASE_NONE", 5)));
        assertFalse(BpScreeningRules.usesMultipleReadings(NcdScreeningPlan.of(Result.ELIGIBLE, "NCD_KNOWN_CASE_NONE", 4)));
        assertFalse(BpScreeningRules.usesMultipleReadings(NcdScreeningPlan.of(Result.ELIGIBLE, NcdEligibilityRules.KNOWN_CASE_HTN, 8)));
    }

    @Test
    public void readingNumberComesFromTheComment() {
        assertEquals(1, BpReading.numberFromComment(null));
        assertEquals(2, BpReading.numberFromComment("2"));
        assertEquals(3, BpReading.numberFromComment(" 3 "));
        assertEquals(1, BpReading.numberFromComment("NA"));
        assertEquals(1, BpReading.numberFromComment("7"));
        assertNull(BpReading.of(1, "", "80"));
        assertEquals("150/90", BpReading.of(2, "150", "90").toString());
    }
}

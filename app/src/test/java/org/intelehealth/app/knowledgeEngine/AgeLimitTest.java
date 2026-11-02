package org.intelehealth.app.knowledgeEngine;

import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AgeLimitTest {

    private static final LocalDate TODAY = new LocalDate(2026, 10, 5);

    private static AgeLimit age(int years, int months, int days) {
        return new AgeLimit(years, months, days);
    }

    private static AgeLimit limit(String json) throws Exception {
        return AgeLimit.parse(new JSONObject("{\"v\":" + json + "}").opt("v"));
    }

    private static void assertAge(AgeLimit actual, int years, int months, int days) {
        assertNotNull(actual);
        assertEquals(years, actual.getYears());
        assertEquals(months, actual.getMonths());
        assertEquals(days, actual.getDays());
    }

    // ---- parsing ----

    @Test
    public void oldFormat_integer_isYearsOnly() throws Exception {
        AgeLimit min = limit("12");
        assertAge(min, 12, 0, 0);
        assertTrue(min.isLegacy());
        assertAge(limit("50"), 50, 0, 0);
    }

    @Test
    public void oldFormat_string_isYearsOnly() throws Exception {
        assertAge(limit("\"12\""), 12, 0, 0);
        assertAge(limit("\" 12 \""), 12, 0, 0);
    }

    @Test
    public void oldFormat_decimal_keepsLegacyYearMonthMeaning() throws Exception {
        assertAge(limit("0.6"), 0, 6, 0);
        assertAge(limit("\"5.11\""), 5, 11, 0);
        assertAge(limit("12.0"), 12, 0, 0);
    }

    @Test
    public void newFormat_yearsOnly() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":0,\"days\":0}");
        assertAge(min, 12, 0, 0);
        assertFalse(min.isLegacy());
    }

    @Test
    public void newFormat_withMonthsAndDays() throws Exception {
        assertAge(limit("{\"year\":12,\"month\":6,\"days\":15}"), 12, 6, 15);
    }

    @Test
    public void newFormat_missingKeysDefaultToZero() throws Exception {
        assertAge(limit("{\"month\":6}"), 0, 6, 0);
        assertAge(limit("{\"year\":3}"), 3, 0, 0);
    }

    @Test
    public void newFormat_asJsonString() throws Exception {
        assertAge(AgeLimit.parse("{\"year\":1,\"month\":2,\"days\":3}"), 1, 2, 3);
    }

    @Test
    public void missingNullOrInvalidValues_areNoLimit() throws Exception {
        assertNull(AgeLimit.parse(null));
        assertNull(AgeLimit.parse(JSONObject.NULL));
        assertNull(limit("null"));
        assertNull(limit("\"\""));
        assertNull(limit("\"abc\""));
        assertNull(limit("-5"));
        assertNull(limit("{}"));
        assertNull(limit("{\"year\":-1}"));
        assertNull(AgeLimit.parse(new JSONObject("{}").opt("age_min")));
    }

    // ---- patient age ----

    @Test
    public void fromDateOfBirth_exactYearsMonthsDays() {
        assertAge(AgeLimit.fromDateOfBirth(new LocalDate(2014, 3, 20), TODAY), 12, 6, 15);
        assertAge(AgeLimit.fromDateOfBirth(TODAY, TODAY), 0, 0, 0);
    }

    @Test
    public void fromDateOfBirth_futureDateClampsToZero() {
        assertAge(AgeLimit.fromDateOfBirth(TODAY.plusDays(3), TODAY), 0, 0, 0);
    }

    @Test
    public void fromYearMonthFloat_matchesLegacyEncoding() {
        assertAge(AgeLimit.fromYearMonthFloat(5.11f), 5, 11, 0);
        assertAge(AgeLimit.fromYearMonthFloat(12f), 12, 0, 0);
        assertAge(AgeLimit.fromYearMonthFloat(0f), 0, 0, 0);
    }

    @Test
    public void forPatient_fallsBackToFloatWhenDobMissingOrInvalid() {
        assertAge(AgeLimit.forPatient(null, 7.3f), 7, 3, 0);
        assertAge(AgeLimit.forPatient("", 7.3f), 7, 3, 0);
        assertAge(AgeLimit.forPatient("not-a-date", 7.3f), 7, 3, 0);
    }

    // ---- range checks: old format (min 12, max 50) ----

    @Test
    public void oldFormat_insideRangeAndBoundaries() throws Exception {
        AgeLimit min = limit("12"), max = limit("50");
        assertFalse(AgeLimit.isOutsideRange(age(30, 0, 0), min, max));
        assertFalse(AgeLimit.isOutsideRange(age(12, 0, 0), min, max)); // equal to min
        assertFalse(AgeLimit.isOutsideRange(age(50, 0, 0), min, max)); // equal to max
    }

    @Test
    public void oldFormat_belowMinAndAboveMax() throws Exception {
        AgeLimit min = limit("12"), max = limit("50");
        assertTrue(AgeLimit.isOutsideRange(age(11, 11, 29), min, max)); // just below min
        assertTrue(AgeLimit.isOutsideRange(age(50, 1, 0), min, max));   // just above max
    }

    @Test
    public void oldFormat_ignoresPatientDays_likeBefore() throws Exception {
        // previously 50y 0m 15d was the float 50.0, which is not > 50
        assertFalse(AgeLimit.isOutsideRange(age(50, 0, 15), limit("12"), limit("50")));
    }

    @Test
    public void oldFormat_decimalLimitsCompareMonthsCorrectly() throws Exception {
        // the old float comparison treated 5y 11m (5.11) as less than 5y 2m (5.2)
        AgeLimit min = limit("5.2"), max = limit("10");
        assertFalse(AgeLimit.isOutsideRange(age(5, 11, 0), min, max));
        assertTrue(AgeLimit.isOutsideRange(age(5, 1, 0), min, max));
    }

    // ---- range checks: new format ----

    @Test
    public void newFormat_yearsOnlyBoundaries() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":0,\"days\":0}");
        AgeLimit max = limit("{\"year\":50,\"month\":0,\"days\":0}");
        assertFalse(AgeLimit.isOutsideRange(age(12, 0, 0), min, max)); // equal to min
        assertFalse(AgeLimit.isOutsideRange(age(50, 0, 0), min, max)); // equal to max
        assertTrue(AgeLimit.isOutsideRange(age(11, 11, 30), min, max)); // one day below min
        assertTrue(AgeLimit.isOutsideRange(age(50, 0, 1), min, max));   // one day above max
    }

    @Test
    public void newFormat_monthsAndDaysBoundaries() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":6,\"days\":15}");
        AgeLimit max = limit("{\"year\":13,\"month\":2,\"days\":10}");
        assertFalse(AgeLimit.isOutsideRange(age(12, 6, 15), min, max)); // equal to min
        assertFalse(AgeLimit.isOutsideRange(age(13, 2, 10), min, max)); // equal to max
        assertFalse(AgeLimit.isOutsideRange(age(12, 11, 0), min, max));
        assertTrue(AgeLimit.isOutsideRange(age(12, 6, 14), min, max));  // just below min
        assertTrue(AgeLimit.isOutsideRange(age(12, 5, 20), min, max));
        assertTrue(AgeLimit.isOutsideRange(age(13, 2, 11), min, max));  // just above max
        assertTrue(AgeLimit.isOutsideRange(age(13, 3, 0), min, max));
    }

    @Test
    public void newFormat_infantMonthsOnly() throws Exception {
        AgeLimit min = limit("{\"year\":0,\"month\":0,\"days\":0}");
        AgeLimit max = limit("{\"year\":0,\"month\":6,\"days\":0}");
        assertFalse(AgeLimit.isOutsideRange(age(0, 5, 29), min, max));
        assertTrue(AgeLimit.isOutsideRange(age(0, 6, 1), min, max));
    }

    @Test
    public void mixedFormats() throws Exception {
        AgeLimit min = limit("12");
        AgeLimit max = limit("{\"year\":12,\"month\":6,\"days\":0}");
        assertFalse(AgeLimit.isOutsideRange(age(12, 0, 0), min, max));
        assertTrue(AgeLimit.isOutsideRange(age(12, 6, 1), min, max));
    }

    // ---- missing values ----

    @Test
    public void missingLimitOrAge_doesNotFilter() throws Exception {
        AgeLimit min = limit("12"), max = limit("50");
        assertFalse(AgeLimit.isOutsideRange(age(5, 0, 0), min, null));
        assertFalse(AgeLimit.isOutsideRange(age(60, 0, 0), null, max));
        assertFalse(AgeLimit.isOutsideRange(age(60, 0, 0), null, null));
        assertFalse(AgeLimit.isOutsideRange(null, min, max));
    }

    // ---- QA scenarios ----

    private static final String MAX_50 = "{\"year\":50,\"month\":0,\"days\":0}";

    @Test
    public void scenario1_oldFormat_sameAsBefore() throws Exception {
        AgeLimit min = limit("12"), max = limit("50");
        assertFalse(AgeLimit.isOutsideRange(age(12, 0, 0), min, max));
        assertFalse(AgeLimit.isOutsideRange(age(35, 6, 10), min, max));
        assertFalse(AgeLimit.isOutsideRange(age(50, 0, 0), min, max));
        assertTrue(AgeLimit.isOutsideRange(age(11, 11, 0), min, max));
        assertTrue(AgeLimit.isOutsideRange(age(51, 0, 0), min, max));
        // the previous float comparison gave the same answers for these ages
        assertFalse(AgeLimit.isOutsideRange(AgeLimit.fromYearMonthFloat(12.0f), min, max));
        assertTrue(AgeLimit.isOutsideRange(AgeLimit.fromYearMonthFloat(11.11f), min, max));
        assertTrue(AgeLimit.isOutsideRange(AgeLimit.fromYearMonthFloat(50.1f), min, max));
    }

    @Test
    public void scenario3_yearsAndMonths_belowAtAbove() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":6,\"days\":0}"), max = limit(MAX_50);
        assertTrue(AgeLimit.isOutsideRange(age(12, 5, 30), min, max));  // below
        assertFalse(AgeLimit.isOutsideRange(age(12, 6, 0), min, max));  // at
        assertFalse(AgeLimit.isOutsideRange(age(12, 6, 1), min, max));  // above
        assertFalse(AgeLimit.isOutsideRange(age(12, 7, 0), min, max));
    }

    @Test
    public void scenario4_yearsMonthsDays() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":6,\"days\":15}"), max = limit(MAX_50);
        assertTrue(AgeLimit.isOutsideRange(age(12, 6, 14), min, max));  // fail
        assertFalse(AgeLimit.isOutsideRange(age(12, 6, 15), min, max)); // pass
        assertFalse(AgeLimit.isOutsideRange(age(12, 6, 16), min, max)); // pass
    }

    @Test
    public void scenario5_minimumBoundary() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":0,\"days\":0}"), max = limit(MAX_50);
        assertFalse(AgeLimit.isOutsideRange(age(12, 0, 0), min, max));  // exactly min
        assertTrue(AgeLimit.isOutsideRange(age(11, 11, 30), min, max)); // 1 day below
        assertTrue(AgeLimit.isOutsideRange(age(11, 11, 0), min, max));  // 1 month below
        assertFalse(AgeLimit.isOutsideRange(age(12, 0, 1), min, max));  // 1 day above
    }

    @Test
    public void scenario6_maximumBoundary() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":0,\"days\":0}"), max = limit(MAX_50);
        assertFalse(AgeLimit.isOutsideRange(age(50, 0, 0), min, max));  // exactly max
        assertFalse(AgeLimit.isOutsideRange(age(49, 11, 30), min, max)); // 1 day below
        assertTrue(AgeLimit.isOutsideRange(age(50, 0, 1), min, max));   // 1 day above
    }

    @Test
    public void scenario6_maximumBoundary_fromRealDates() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":0,\"days\":0}"), max = limit(MAX_50);
        LocalDate fiftiethBirthday = TODAY.minusYears(50);
        assertFalse(AgeLimit.isOutsideRange(AgeLimit.fromDateOfBirth(fiftiethBirthday, TODAY), min, max));
        assertFalse(AgeLimit.isOutsideRange(AgeLimit.fromDateOfBirth(fiftiethBirthday.plusDays(1), TODAY), min, max));
        assertTrue(AgeLimit.isOutsideRange(AgeLimit.fromDateOfBirth(fiftiethBirthday.minusDays(1), TODAY), min, max));
    }

    @Test
    public void scenario7_zeroValues() throws Exception {
        AgeLimit zero = limit("{\"year\":0,\"month\":0,\"days\":0}");
        assertAge(zero, 0, 0, 0);
        assertFalse(AgeLimit.isOutsideRange(age(0, 0, 0), zero, limit(MAX_50)));
        assertFalse(AgeLimit.isOutsideRange(age(30, 0, 0), zero, limit(MAX_50)));
        // min and max both zero: only a newborn (0 days) is in range
        assertFalse(AgeLimit.isOutsideRange(age(0, 0, 0), zero, zero));
        assertTrue(AgeLimit.isOutsideRange(age(0, 0, 1), zero, zero));
        assertAge(limit("0"), 0, 0, 0);
    }

    @Test
    public void scenario8_missingKeys_treatedAsZero() throws Exception {
        assertAge(limit("{\"month\":6,\"days\":15}"), 0, 6, 15);    // no year
        assertAge(limit("{\"year\":12,\"days\":15}"), 12, 0, 15);   // no month
        assertAge(limit("{\"year\":12,\"month\":6}"), 12, 6, 0);    // no days
        assertAge(limit("{\"year\":12,\"month\":null,\"days\":null}"), 12, 0, 0);
    }

    @Test
    public void scenario8_missingOrNullLimits_noCrashNoFilter() throws Exception {
        assertNull(limit("{}"));
        assertNull(limit("null"));
        assertNull(limit("{\"year\":\"abc\"}"));
        assertFalse(AgeLimit.isOutsideRange(age(5, 0, 0), limit("{}"), limit(MAX_50)));
        assertFalse(AgeLimit.isOutsideRange(age(60, 0, 0), limit("12"), limit("null")));
        // an invalid max must not hide the node from every patient
        assertFalse(AgeLimit.isOutsideRange(age(30, 0, 0), limit("12"), limit("{\"year\":\"abc\"}")));
        assertAge(limit("{\"year\":\"12\",\"month\":\"6\"}"), 12, 6, 0);
    }

    // ---- visit flow rule (VisitUtils): single limits enforced, zero = not set ----

    @Test
    public void visitFlow_bothLimits() throws Exception {
        AgeLimit min = limit("{\"year\":12,\"month\":6,\"days\":15}"), max = limit(MAX_50);
        assertFalse(AgeLimit.isWithinVisitRange(age(12, 6, 14), min, max));
        assertTrue(AgeLimit.isWithinVisitRange(age(12, 6, 15), min, max));
        assertTrue(AgeLimit.isWithinVisitRange(age(50, 0, 0), min, max));
        assertFalse(AgeLimit.isWithinVisitRange(age(50, 0, 1), min, max));
    }

    @Test
    public void visitFlow_singleLimitEnforced() throws Exception {
        assertFalse(AgeLimit.isWithinVisitRange(age(11, 0, 0), limit("12"), null));
        assertTrue(AgeLimit.isWithinVisitRange(age(80, 0, 0), limit("12"), null));
        assertFalse(AgeLimit.isWithinVisitRange(age(51, 0, 0), null, limit("50")));
    }

    @Test
    public void visitFlow_zeroMeansNotSet() throws Exception {
        AgeLimit zero = limit("{\"year\":0,\"month\":0,\"days\":0}");
        assertTrue(AgeLimit.isWithinVisitRange(age(70, 0, 0), limit("12"), zero));
        assertTrue(AgeLimit.isWithinVisitRange(age(70, 0, 0), limit("12"), limit("0")));
        assertTrue(AgeLimit.isWithinVisitRange(age(0, 0, 0), zero, limit("50")));
    }

    @Test
    public void visitFlow_missingOrInvalid() throws Exception {
        assertTrue(AgeLimit.isWithinVisitRange(age(30, 0, 0), null, null));
        assertTrue(AgeLimit.isWithinVisitRange(null, limit("12"), limit("50")));
        assertTrue(AgeLimit.isWithinVisitRange(age(30, 0, 0), limit("{\"year\":\"abc\"}"), limit("50")));
    }

    // ---- API response formats: "age_min": "10" (string) and {"year": 12, ...} (object) ----

    private static final String STR_MIN_10 = "\"10\"";
    private static final String STR_MAX_120 = "\"120\"";
    private static final String OBJ_MIN_12 = "{\"year\":12,\"month\":0,\"days\":0}";
    private static final String OBJ_MAX_120 = "{\"year\":120,\"month\":0,\"days\":0}";

    @Test
    public void apiFormat1_stringLimits_parsed() throws Exception {
        assertAge(limit(STR_MIN_10), 10, 0, 0);
        assertAge(limit(STR_MAX_120), 120, 0, 0);
        assertAge(limit("10"), 10, 0, 0); // number instead of string
    }

    @Test
    public void apiFormat1_stringLimits_belowAtAbove() throws Exception {
        AgeLimit min = limit(STR_MIN_10), max = limit(STR_MAX_120);
        assertFalse(AgeLimit.isWithinVisitRange(age(9, 11, 29), min, max)); // below min
        assertTrue(AgeLimit.isWithinVisitRange(age(10, 0, 0), min, max));   // at min
        assertTrue(AgeLimit.isWithinVisitRange(age(10, 0, 1), min, max));   // above min
        assertTrue(AgeLimit.isWithinVisitRange(age(120, 0, 0), min, max));  // at max
        assertFalse(AgeLimit.isWithinVisitRange(age(120, 1, 0), min, max)); // above max
    }

    @Test
    public void apiFormat2_objectLimits_belowAtAbove() throws Exception {
        AgeLimit min = limit(OBJ_MIN_12), max = limit(OBJ_MAX_120);
        assertFalse(AgeLimit.isWithinVisitRange(age(11, 11, 30), min, max)); // below min
        assertTrue(AgeLimit.isWithinVisitRange(age(12, 0, 0), min, max));    // at min
        assertTrue(AgeLimit.isWithinVisitRange(age(12, 0, 1), min, max));    // above min
        assertTrue(AgeLimit.isWithinVisitRange(age(120, 0, 0), min, max));   // at max
        assertFalse(AgeLimit.isWithinVisitRange(age(120, 0, 1), min, max));  // above max
    }

    @Test
    public void breastPain18_sameResultForEitherFormat() throws Exception {
        AgeLimit[][] formats = {
                {limit("\"18\""), limit(STR_MAX_120)},
                {limit("18"), limit("120")},
                {limit("{\"year\":18,\"month\":0,\"days\":0}"), limit(OBJ_MAX_120)},
        };
        for (AgeLimit[] f : formats) {
            assertFalse(AgeLimit.isWithinVisitRange(age(9, 0, 0), f[0], f[1]));
            assertFalse(AgeLimit.isWithinVisitRange(age(17, 11, 0), f[0], f[1]));
            assertTrue(AgeLimit.isWithinVisitRange(age(18, 0, 0), f[0], f[1]));
            assertTrue(AgeLimit.isWithinVisitRange(age(30, 0, 0), f[0], f[1]));
        }
    }
}

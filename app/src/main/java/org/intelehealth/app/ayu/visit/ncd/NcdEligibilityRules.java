package org.intelehealth.app.ayu.visit.ncd;

/**
 * Display rules for the NCD consent and eligibility screens (no Android code, so it is unit tested).
 * Age is the visit screen's float "years.months" value, e.g. 30.06 = 30 years 6 months.
 */
public final class NcdEligibilityRules {

    public static final int MIN_SCREENING_AGE = 30;
    public static final int PREGNANCY_MAX_AGE = 49;
    public static final String KNOWN_CASE_BOTH = "NCD_KNOWN_CASE_BOTH";
    private static final String FEMALE = "F";

    public enum Result { ELIGIBLE, NOT_ELIGIBLE, URGENT }

    private NcdEligibilityRules() {
    }

    public static int completedYears(float ageYearMonth) {
        return (int) ageYearMonth;
    }

    public static boolean isScreeningAge(float ageYearMonth) {
        return completedYears(ageYearMonth) >= MIN_SCREENING_AGE;
    }

    public static boolean isPregnancyQuestionShown(String gender, float ageYearMonth) {
        int years = completedYears(ageYearMonth);
        return FEMALE.equalsIgnoreCase(gender) && years >= MIN_SCREENING_AGE && years <= PREGNANCY_MAX_AGE;
    }

    // Urgent symptoms win over exclusions; a known case of both DM and HTN is not eligible
    public static Result evaluate(boolean pregnant, boolean multimorbidity, boolean knownCaseBoth, boolean hasUrgentSymptom) {
        if (hasUrgentSymptom) return Result.URGENT;
        if (pregnant || multimorbidity || knownCaseBoth) return Result.NOT_ELIGIBLE;
        return Result.ELIGIBLE;
    }

    // Only an Eligible result goes on to CBAC
    public static boolean isCbacShown(Result result) {
        return result == Result.ELIGIBLE;
    }
}

package org.intelehealth.app.ayu.visit.ncd;

/**
 * Display rules for the NCD consent and eligibility screens (no Android code, so it is unit tested).
 * Age is the visit screen's float "years.months" value, e.g. 30.06 = 30 years 6 months.
 */
public final class NcdEligibilityRules {

    public static final int MIN_SCREENING_AGE = 30;
    public static final int PREGNANCY_MAX_AGE = 49;
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

    // Urgent symptoms win over exclusions
    public static Result evaluate(boolean pregnant, boolean multimorbidity, boolean hasUrgentSymptom) {
        if (hasUrgentSymptom) return Result.URGENT;
        if (pregnant || multimorbidity) return Result.NOT_ELIGIBLE;
        return Result.ELIGIBLE;
    }
}

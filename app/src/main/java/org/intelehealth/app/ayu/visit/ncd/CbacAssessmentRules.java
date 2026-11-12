package org.intelehealth.app.ayu.visit.ncd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CBAC display and scoring rules (no Android code, so it is unit tested).
 * Points come from the option text of the CBAC file, e.g. "Daily (2)", as in the CBAC Excel.
 */
public final class CbacAssessmentRules {

    // Above this total the patient goes on to DM/HTN screening; 4 or less means counselling
    public static final int LOW_RISK_MAX_SCORE = 4;
    private static final String MALE = "M";
    private static final String FEMALE = "F";
    // Question "gender" values in the CBAC file
    private static final String MALE_QUESTION = "1";
    private static final String FEMALE_QUESTION = "0";
    private static final Pattern POINTS = Pattern.compile("\\((\\d+)\\)\\s*$");

    private CbacAssessmentRules() {
    }

    // Gender-specific questions (waist) only show for the matching patient gender
    public static boolean isQuestionShown(String questionGender, String patientGender) {
        if (questionGender == null || questionGender.isEmpty()) return true;
        if (MALE_QUESTION.equals(questionGender)) return MALE.equalsIgnoreCase(patientGender);
        if (FEMALE_QUESTION.equals(questionGender)) return FEMALE.equalsIgnoreCase(patientGender);
        return false;
    }

    // Age bands: 0-29 = 0, 30-39 = 1, 40-49 = 2, 50-59 = 3, 60+ = 4
    public static int agePoints(int years) {
        if (years < 30) return 0;
        if (years < 40) return 1;
        if (years < 50) return 2;
        if (years < 60) return 3;
        return 4;
    }

    // Null when the text has no "(n)" at the end
    public static Integer pointsFromText(String optionText) {
        if (optionText == null) return null;
        Matcher matcher = POINTS.matcher(optionText);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    // Option text without its points, e.g. "Daily (2)" → "Daily"
    public static String withoutPoints(String optionText) {
        return optionText == null ? null : POINTS.matcher(optionText).replaceFirst("").trim();
    }

    public static boolean isHighRisk(int totalScore) {
        return totalScore > LOW_RISK_MAX_SCORE;
    }

    // Typed waist in cm; null unless a positive number (a comma counts as the decimal point)
    public static Double parseWaistCm(String text) {
        if (text == null) return null;
        try {
            double value = Double.parseDouble(text.trim().replace(',', '.'));
            return value > 0 && !Double.isInfinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // CBAC file guidance: record to the nearest 0.0 or 0.5 cm (85.7 → 85.5, 85.9 → 86)
    public static double roundWaistToHalf(double waistCm) {
        return Math.round(waistCm * 2) / 2.0;
    }

    // Band index 0, 1 or 2 in the order of the CBAC file (M: ≤90, ≤100, >100; F: ≤80, ≤90, >90); -1 for other genders
    public static int waistBand(String patientGender, double waistCm) {
        double lowMax;
        if (MALE.equalsIgnoreCase(patientGender)) lowMax = 90;
        else if (FEMALE.equalsIgnoreCase(patientGender)) lowMax = 80;
        else return -1;
        if (waistCm <= lowMax) return 0;
        if (waistCm <= lowMax + 10) return 1;
        return 2;
    }
}

package org.intelehealth.app.ayu.visit.ncd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Obs values of the NCD Screening encounter (no Android code, so it is unit tested).
 * Consent, eligibility and CBAC answers are Text concepts, the score Numeric, as agreed with the Data team.
 * High risk is not stored: reports work it out from the score.
 */
public final class NcdScreeningValues {

    static final String YES = "Yes";
    static final String NO = "No";

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    // CBAC question ids from the CBAC file → short keys for reports
    private static final Map<String, String> CBAC_KEYS = new HashMap<>();
    static final String WAIST_BAND = "waistBand";

    static {
        CBAC_KEYS.put("0.7752185439835569", "tobacco");
        CBAC_KEYS.put("0.16904166135693055", "alcohol");
        CBAC_KEYS.put("0.5979849577726372", WAIST_BAND); // male
        CBAC_KEYS.put("0.744606179434009", WAIST_BAND); // female
        CBAC_KEYS.put("0.167082731656357", "physicalActivity");
        CBAC_KEYS.put("0.4592980225875687", "familyHistory");
    }

    private NcdScreeningValues() {
    }

    public static String consent(boolean accepted) {
        return accepted ? YES : NO;
    }

    // e.g. {"pregnant":"No","multimorbidity":"No","knownCase":"Yes, Diabetes only","urgentSymptoms":[],"result":"Eligible"}
    public static String eligibility(NcdEligibilityState state) {
        Map<String, Object> json = new LinkedHashMap<>();
        if (state.getPregnant() != null) json.put("pregnant", state.getPregnant() ? YES : NO);
        json.put("multimorbidity", state.isMultimorbidity() ? YES : NO);
        json.put("knownCase", state.getKnownCaseText());
        json.put("urgentSymptoms", state.getUrgentSymptomTexts());
        json.put("result", resultText(state.getResult()));
        if (state.getResult() == NcdEligibilityRules.Result.NOT_ELIGIBLE) json.put("reason", reasonText(state.getNotEligibleReason()));
        return GSON.toJson(json);
    }

    // e.g. {"age":36,"tobacco":"Never","alcohol":"No","waistCm":92.5,"waistBand":"90-100 cm",...}
    public static String cbacAnswers(CbacAssessmentState state) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("age", state.getAgeYears());
        for (Map.Entry<String, String> answer : state.getAnswerTexts().entrySet()) {
            String key = CBAC_KEYS.get(answer.getKey());
            if (key == null) key = answer.getKey();
            if (WAIST_BAND.equals(key)) json.put("waistCm", state.getWaistCm());
            json.put(key, CbacAssessmentRules.withoutPoints(answer.getValue()));
        }
        return GSON.toJson(json);
    }

    public static String cbacScore(int totalScore) {
        return String.valueOf(totalScore);
    }

    // Tells the medicine photos apart from Physical Exam photos, which share their concept
    public static String medicinePhotoComment(String knownCaseText) {
        return "NCD medicine photo: " + knownCaseText;
    }

    private static String resultText(NcdEligibilityRules.Result result) {
        if (result == null) return null;
        switch (result) {
            case ELIGIBLE: return "Eligible";
            case NOT_ELIGIBLE: return "Not eligible";
            default: return "Urgent attention";
        }
    }

    private static String reasonText(NcdEligibilityRules.Reason reason) {
        switch (reason) {
            case KNOWN_CASE_BOTH: return "Known case of both diabetes and hypertension";
            case PREGNANT_AND_MULTIMORBIDITY: return "Pregnant and serious illness or multimorbidity";
            case PREGNANT: return "Pregnant";
            default: return "Serious illness or multimorbidity";
        }
    }
}

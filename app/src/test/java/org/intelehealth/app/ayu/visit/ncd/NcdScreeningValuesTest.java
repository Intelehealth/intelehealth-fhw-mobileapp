package org.intelehealth.app.ayu.visit.ncd;

import static org.junit.Assert.assertEquals;

import org.intelehealth.app.ayu.visit.ncd.NcdEligibilityRules.Result;
import org.junit.Test;

public class NcdScreeningValuesTest {

    @Test
    public void consentIsYesOrNo() {
        assertEquals("Yes", NcdScreeningValues.consent(true));
        assertEquals("No", NcdScreeningValues.consent(false));
    }

    @Test
    public void eligibleManHasNoPregnancyAnswerOrReason() {
        NcdEligibilityState state = new NcdEligibilityState();
        state.setMultimorbidity(false);
        state.setKnownCaseOptionId("NCD_KNOWN_CASE_DM");
        state.setKnownCaseText("Yes, Diabetes only");
        state.setResult(Result.ELIGIBLE);
        assertEquals("{\"multimorbidity\":\"No\",\"knownCase\":\"Yes, Diabetes only\",\"urgentSymptoms\":[],\"result\":\"Eligible\"}",
                NcdScreeningValues.eligibility(state));
    }

    @Test
    public void notEligibleWomanKeepsTheReasonShownInThePopUp() {
        NcdEligibilityState state = new NcdEligibilityState();
        state.setPregnant(true);
        state.setMultimorbidity(true);
        state.setKnownCaseOptionId("NCD_KNOWN_CASE_NONE");
        state.setKnownCaseText("No (new case)");
        state.setResult(Result.NOT_ELIGIBLE);
        assertEquals("{\"pregnant\":\"Yes\",\"multimorbidity\":\"Yes\",\"knownCase\":\"No (new case)\",\"urgentSymptoms\":[],"
                        + "\"result\":\"Not eligible\",\"reason\":\"Pregnant and serious illness or multimorbidity\"}",
                NcdScreeningValues.eligibility(state));
    }

    @Test
    public void urgentKeepsEverySymptom() {
        NcdEligibilityState state = new NcdEligibilityState();
        state.setPregnant(false);
        state.setKnownCaseOptionId("NCD_KNOWN_CASE_BOTH");
        state.setKnownCaseText("Yes, Both");
        state.getUrgentSymptomTexts().add("Chest pain");
        state.getUrgentSymptomTexts().add("Blurred vision");
        state.setResult(Result.URGENT);
        assertEquals("{\"pregnant\":\"No\",\"multimorbidity\":\"No\",\"knownCase\":\"Yes, Both\","
                        + "\"urgentSymptoms\":[\"Chest pain\",\"Blurred vision\"],\"result\":\"Urgent attention\"}",
                NcdScreeningValues.eligibility(state));
    }

    @Test
    public void cbacAnswersUseShortKeysWithoutPoints() {
        CbacAssessmentState state = new CbacAssessmentState();
        state.setAgeYears(36);
        state.getAnswerTexts().put("0.7752185439835569", "Never (0)");
        state.getAnswerTexts().put("0.16904166135693055", "No (0)");
        state.getAnswerTexts().put("0.5979849577726372", "90-100 cm (1)");
        state.getAnswerTexts().put("0.167082731656357", "At least 150 minutes (0)");
        state.getAnswerTexts().put("0.4592980225875687", "Yes (2)");
        state.setWaistCm(92.5);
        state.setTotalScore(4);
        assertEquals("{\"age\":36,\"tobacco\":\"Never\",\"alcohol\":\"No\",\"waistCm\":92.5,\"waistBand\":\"90-100 cm\","
                        + "\"physicalActivity\":\"At least 150 minutes\",\"familyHistory\":\"Yes\"}",
                NcdScreeningValues.cbacAnswers(state));
    }

    @Test
    public void femaleWaistUsesTheSameKeys() {
        CbacAssessmentState state = new CbacAssessmentState();
        state.setAgeYears(62);
        state.getAnswerTexts().put("0.744606179434009", "More than 90 cm (2)");
        state.setWaistCm(95);
        assertEquals("{\"age\":62,\"waistCm\":95.0,\"waistBand\":\"More than 90 cm\"}",
                NcdScreeningValues.cbacAnswers(state));
    }

    @Test
    public void medicinePhotoCommentNamesTheKnownCase() {
        assertEquals("NCD medicine photo: Yes, Diabetes only", NcdScreeningValues.medicinePhotoComment("Yes, Diabetes only"));
    }

    @Test
    public void scoreIsAPlainNumber() {
        assertEquals("1", NcdScreeningValues.cbacScore(1));
        assertEquals("12", NcdScreeningValues.cbacScore(12));
    }
}

package org.intelehealth.app.knowledgeEngine;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Runs the age_min / age_max scenarios on a device, with Android's own org.json, the real
 * {@link Node} parser, {@link Node#fetchAge} and {@link VisitUtils#checkNodeValidByGenderAndAge}.
 */
@RunWith(AndroidJUnit4.class)
public class AgeLimitInstrumentedTest {

    private static final String OLD_12_50 = "\"age_min\":12,\"age_max\":50";
    private static final String NEW_12_50 =
            "\"age_min\":{\"year\":12,\"month\":0,\"days\":0},\"age_max\":{\"year\":50,\"month\":0,\"days\":0}";
    private static final String NEW_12Y6M15D_50 =
            "\"age_min\":{\"year\":12,\"month\":6,\"days\":15},\"age_max\":{\"year\":50,\"month\":0,\"days\":0}";

    private static int nextId = 0;

    // ---- helpers ----

    private static String nodeJson(String text, String extra) {
        return "{\"id\":\"" + (nextId++) + "\",\"text\":\"" + text + "\"" + (extra.isEmpty() ? "" : "," + extra) + "}";
    }

    private static Node node(String extra) throws Exception {
        return new Node(new JSONObject(nodeJson("n", extra)));
    }

    /** Root node whose options carry the given age attributes, named by index. */
    private static Node tree(String... optionAgeAttributes) throws Exception {
        JSONArray options = new JSONArray();
        for (int i = 0; i < optionAgeAttributes.length; i++) {
            options.put(new JSONObject(nodeJson("option" + i, optionAgeAttributes[i])));
        }
        JSONObject root = new JSONObject(nodeJson("root", ""));
        root.put("options", options);
        return new Node(root);
    }

    private static List<String> remaining(Node root) {
        List<String> names = new ArrayList<>();
        for (Node option : root.getOptionsList()) names.add(option.getText());
        return names;
    }

    private static boolean shown(String ageAttributes, AgeLimit patientAge) throws Exception {
        Node root = tree(ageAttributes);
        root.fetchAge(patientAge);
        return root.getOptionsList().size() == 1;
    }

    private static boolean shownInVisitFlow(String ageAttributes, AgeLimit patientAge) throws Exception {
        Node n = node("\"gender\":\"0\"" + (ageAttributes.isEmpty() ? "" : "," + ageAttributes));
        return VisitUtils.checkNodeValidByGenderAndAge("F", patientAge, n.getGender(), n.getMinAgeLimit(), n.getMaxAgeLimit());
    }

    private static AgeLimit age(int y, int m, int d) {
        return new AgeLimit(y, m, d);
    }

    /** The age_min/age_max comparison exactly as it was before this change (legacy Node.fetchAge). */
    private static boolean oldNodeShown(String min, String max, float age) {
        if (min.isEmpty() || max.isEmpty()) return true;
        if (age < Float.parseFloat(min.trim())) return false;
        return !(age > Float.parseFloat(max));
    }

    // ---- 1. old format ----

    @Test
    public void s1_oldFormat_parsedOnDevice() throws Exception {
        Node n = node(OLD_12_50);
        assertEquals("12", n.getMin_age());
        assertEquals("50", n.getMax_age());
        assertEquals(12, n.getMinAgeLimit().getYears());
        assertTrue(n.getMinAgeLimit().isLegacy());
        assertEquals(50, n.getMaxAgeLimit().getYears());
    }

    @Test
    public void s1_oldFormat_fetchAgeMatchesPreviousLogicForAllAges() throws Exception {
        // every age 0y0m .. 60y9m with months 0-9 (where the old float is unambiguous)
        for (int y = 0; y <= 60; y++) {
            for (int m = 0; m <= 9; m++) {
                float legacyAge = Float.parseFloat(y + "." + m);
                Node root = tree(OLD_12_50);
                root.fetchAge(legacyAge);
                boolean nowShown = root.getOptionsList().size() == 1;
                assertEquals("age " + y + "y" + m + "m", oldNodeShown("12", "50", legacyAge), nowShown);
            }
        }
    }

    @Test
    public void s1_oldFormat_visitFlowMatchesPreviousLogicForAllAges() throws Exception {
        for (int y = 0; y <= 60; y++) {
            for (int m = 0; m <= 9; m++) {
                float legacyAge = Float.parseFloat(y + "." + m);
                boolean before = VisitUtils.checkNodeValidByGenderAndAge("F", legacyAge, "0", "12", "50");
                boolean now = shownInVisitFlow(OLD_12_50, age(y, m, 0));
                assertEquals("age " + y + "y" + m + "m", before, now);
            }
        }
    }

    @Test
    public void s1_oldFormat_visitFlowSingleLimitStillEnforced() throws Exception {
        assertFalse(shownInVisitFlow("\"age_min\":12", age(11, 0, 0)));
        assertTrue(shownInVisitFlow("\"age_min\":12", age(70, 0, 0)));
        assertFalse(shownInVisitFlow("\"age_max\":50", age(51, 0, 0)));
        assertTrue(shownInVisitFlow("\"age_max\":50", age(1, 0, 0)));
    }

    // ---- 2. new format, years only ----

    @Test
    public void s2_newFormatYearsOnly() throws Exception {
        Node n = node(NEW_12_50);
        assertEquals(12, n.getMinAgeLimit().getYears());
        assertFalse(n.getMinAgeLimit().isLegacy());
        assertTrue(shown(NEW_12_50, age(12, 0, 0)));
        assertTrue(shown(NEW_12_50, age(50, 0, 0)));
        assertFalse(shown(NEW_12_50, age(11, 11, 30)));
        assertFalse(shown(NEW_12_50, age(50, 0, 1)));
        assertTrue(shownInVisitFlow(NEW_12_50, age(12, 0, 0)));
        assertFalse(shownInVisitFlow(NEW_12_50, age(50, 0, 1)));
    }

    // ---- 3. years + months ----

    @Test
    public void s3_yearsAndMonths() throws Exception {
        String attrs = "\"age_min\":{\"year\":12,\"month\":6,\"days\":0},\"age_max\":{\"year\":50,\"month\":0,\"days\":0}";
        assertFalse(shown(attrs, age(12, 5, 30)));
        assertTrue(shown(attrs, age(12, 6, 0)));
        assertTrue(shown(attrs, age(12, 6, 1)));
        assertFalse(shownInVisitFlow(attrs, age(12, 5, 30)));
        assertTrue(shownInVisitFlow(attrs, age(12, 6, 0)));
    }

    // ---- 4. years + months + days ----

    @Test
    public void s4_yearsMonthsDays() throws Exception {
        assertFalse(shown(NEW_12Y6M15D_50, age(12, 6, 14)));
        assertTrue(shown(NEW_12Y6M15D_50, age(12, 6, 15)));
        assertTrue(shown(NEW_12Y6M15D_50, age(12, 6, 16)));
        assertFalse(shownInVisitFlow(NEW_12Y6M15D_50, age(12, 6, 14)));
        assertTrue(shownInVisitFlow(NEW_12Y6M15D_50, age(12, 6, 15)));
        assertTrue(shownInVisitFlow(NEW_12Y6M15D_50, age(12, 6, 16)));
    }

    // ---- 5 & 6. boundaries, using real dates of birth on the device clock ----

    @Test
    public void s5_minimumBoundaryFromDateOfBirth() throws Exception {
        LocalDate today = new LocalDate();
        LocalDate twelfthBirthday = today.minusYears(12);
        assertTrue(shown(NEW_12_50, AgeLimit.fromDateOfBirth(twelfthBirthday, today)));
        assertFalse(shown(NEW_12_50, AgeLimit.fromDateOfBirth(twelfthBirthday.plusDays(1), today)));
        assertFalse(shown(NEW_12_50, AgeLimit.fromDateOfBirth(twelfthBirthday.plusMonths(1), today)));
        assertTrue(shown(NEW_12_50, AgeLimit.fromDateOfBirth(twelfthBirthday.minusDays(1), today)));
    }

    @Test
    public void s6_maximumBoundaryFromDateOfBirth() throws Exception {
        LocalDate today = new LocalDate();
        LocalDate fiftiethBirthday = today.minusYears(50);
        assertTrue(shown(NEW_12_50, AgeLimit.fromDateOfBirth(fiftiethBirthday, today)));
        assertTrue(shown(NEW_12_50, AgeLimit.fromDateOfBirth(fiftiethBirthday.plusDays(1), today)));
        assertFalse(shown(NEW_12_50, AgeLimit.fromDateOfBirth(fiftiethBirthday.minusDays(1), today)));
        assertFalse(shownInVisitFlow(NEW_12_50, AgeLimit.fromDateOfBirth(fiftiethBirthday.minusDays(1), today)));
    }

    // ---- 7. zero values ----

    @Test
    public void s7_zeroValues() throws Exception {
        String zeroMin = "\"age_min\":{\"year\":0,\"month\":0,\"days\":0},\"age_max\":{\"year\":50,\"month\":0,\"days\":0}";
        assertTrue(shown(zeroMin, age(0, 0, 0)));
        assertTrue(shown(zeroMin, age(30, 0, 0)));
        assertTrue(shownInVisitFlow(zeroMin, age(0, 0, 0)));
        // in the visit flow a zero limit has always meant "not set"
        String zeroMax = "\"age_min\":{\"year\":12,\"month\":0,\"days\":0},\"age_max\":{\"year\":0,\"month\":0,\"days\":0}";
        assertTrue(shownInVisitFlow(zeroMax, age(70, 0, 0)));
        assertTrue(VisitUtils.checkNodeValidByGenderAndAge("F", 70f, "0", "12", "0"));
    }

    // ---- 8. missing / null / invalid values ----

    @Test
    public void s8_missingKeysAreZero() throws Exception {
        AgeLimit l = node("\"age_min\":{\"year\":12,\"days\":15}").getMinAgeLimit();
        assertEquals(12, l.getYears());
        assertEquals(0, l.getMonths());
        assertEquals(15, l.getDays());
        l = node("\"age_min\":{\"month\":6}").getMinAgeLimit();
        assertEquals(0, l.getYears());
        assertEquals(6, l.getMonths());
    }

    @Test
    public void s8_missingNullInvalid_noCrashNoFilter() throws Exception {
        String[] attrs = {
                "",
                "\"age_min\":null,\"age_max\":null",
                "\"age_min\":\"\",\"age_max\":\"\"",
                "\"age_min\":{},\"age_max\":{}",
                "\"age_min\":{\"year\":\"abc\"},\"age_max\":50",
                "\"age_min\":12,\"age_max\":{\"year\":\"abc\"}",
                "\"age_min\":\"abc\",\"age_max\":\"xyz\"",
        };
        for (String a : attrs) {
            assertTrue("old flow: " + a, shown(a, age(30, 0, 0)));
            assertTrue("visit flow: " + a, shownInVisitFlow(a, age(30, 0, 0)));
        }
        assertNull(node("\"age_min\":null").getMinAgeLimit());
    }

    @Test
    public void s8_onlyOneLimit_oldFlowStillIgnoresIt() throws Exception {
        assertTrue(shown("\"age_min\":12", age(5, 0, 0)));
        assertTrue(shown("\"age_max\":{\"year\":50,\"month\":0,\"days\":0}", age(60, 0, 0)));
    }

    // ---- 9. regression ----

    @Test
    public void s9_fetchAgeStillWalksNestedLevels() throws Exception {
        JSONObject child = new JSONObject(nodeJson("child", NEW_12_50));
        JSONObject level2 = new JSONObject(nodeJson("level2", ""));
        level2.put("options", new JSONArray().put(child));
        JSONObject root = new JSONObject(nodeJson("root", ""));
        root.put("options", new JSONArray().put(level2));
        Node rootNode = new Node(root);
        rootNode.fetchAge(age(5, 0, 0));
        assertEquals(1, rootNode.getOptionsList().size());
        assertEquals(0, rootNode.getOptionsList().get(0).getOptionsList().size());
    }

    @Test
    public void s9_mixedTreeKeepsOnlyMatchingOptions() throws Exception {
        Node root = tree(OLD_12_50, NEW_12Y6M15D_50, "", "\"age_min\":0,\"age_max\":5");
        root.fetchAge(age(12, 6, 0));
        List<String> left = remaining(root);
        assertEquals(2, left.size());
        assertTrue(left.contains("option0"));
        assertTrue(left.contains("option2"));
    }

    @Test
    public void s9_copyConstructorKeepsLimits() throws Exception {
        Node copy = new Node(node(NEW_12Y6M15D_50));
        assertNotNull(copy.getMinAgeLimit());
        assertEquals(15, copy.getMinAgeLimit().getDays());
    }

    @Test
    public void s9_visitFlowGenderRulesUnchanged() throws Exception {
        Node maleOnlyNode = node("\"gender\":\"1\"," + NEW_12_50); // "1" = not for females
        assertFalse(VisitUtils.checkNodeValidByGenderAndAge("F", age(30, 0, 0), maleOnlyNode.getGender(),
                maleOnlyNode.getMinAgeLimit(), maleOnlyNode.getMaxAgeLimit()));
        Node femaleOnlyNode = node("\"gender\":\"0\"");
        assertFalse(VisitUtils.checkNodeValidByGenderAndAge("M", age(30, 0, 0), femaleOnlyNode.getGender(), null, null));
        assertTrue(VisitUtils.checkNodeValidByGenderAndAge("F", age(30, 0, 0), femaleOnlyNode.getGender(), null, null));
    }

    // ---- 10. age check without a gender attribute, at every level, and in the edit flow ----

    @Test
    public void s10_ageCheckedEvenWithoutGender() throws Exception {
        Node noGender = node(NEW_12_50);
        assertFalse(VisitUtils.checkNodeValidByGenderAndAge("F", age(5, 0, 0), noGender.getGender(),
                noGender.getMinAgeLimit(), noGender.getMaxAgeLimit()));
        assertTrue(VisitUtils.checkNodeValidByGenderAndAge("M", age(30, 0, 0), noGender.getGender(),
                noGender.getMinAgeLimit(), noGender.getMaxAgeLimit()));
        Node noLimits = node("");
        assertTrue(VisitUtils.checkNodeValidByGenderAndAge("F", age(5, 0, 0), noLimits.getGender(), null, null));
    }

    /** Breast Pain style: options 3 levels down, no gender, minimum age 18. */
    private static Node breastPainLike() throws Exception {
        JSONObject menstrual = new JSONObject(nodeJson("Menstrual period", "\"age_min\":18,\"age_max\":100"));
        JSONObject feeding = new JSONObject(nodeJson("Breast feeding", "\"age_min\":{\"year\":18,\"month\":0,\"days\":0}"));
        JSONObject trauma = new JSONObject(nodeJson("Trauma", ""));
        JSONObject subQuestion = new JSONObject(nodeJson("Related to", ""));
        subQuestion.put("options", new JSONArray().put(menstrual).put(feeding).put(trauma));
        JSONObject answer = new JSONObject(nodeJson("Yes", ""));
        answer.put("options", new JSONArray().put(subQuestion));
        JSONObject question = new JSONObject(nodeJson("Associated factors", ""));
        question.put("options", new JSONArray().put(answer));
        JSONObject root = new JSONObject(nodeJson("Breast Pain", ""));
        root.put("options", new JSONArray().put(question));
        return new Node(root);
    }

    private static List<String> textsAtLevel3(Node root) {
        List<String> names = new ArrayList<>();
        for (Node option : root.getOptionsList().get(0).getOptionsList().get(0).getOptionsList().get(0).getOptionsList()) {
            names.add(option.getText());
        }
        return names;
    }

    @Test
    public void s10_deepOptionsFilteredByAge() throws Exception {
        Node child = breastPainLike();
        VisitUtils.removeNodesInvalidByGenderAndAge(child.getOptionsList(), "F", age(9, 0, 0));
        List<String> left = textsAtLevel3(child);
        assertEquals(1, left.size());
        assertEquals("Trauma", left.get(0));

        Node adult = breastPainLike();
        VisitUtils.removeNodesInvalidByGenderAndAge(adult.getOptionsList(), "F", age(18, 0, 0));
        assertEquals(3, textsAtLevel3(adult).size());
    }

    /** Abdominal Pain style: two age-limited questions and one for everyone. */
    private static List<Node> abdominalPainFor(AgeLimit patientAge) throws Exception {
        Node root = tree("\"gender\":\"0\",\"age_min\":12,\"age_max\":50", // Painful periods
                "\"gender\":\"0\",\"age_min\":{\"year\":12,\"month\":0,\"days\":0}", // Abnormal discharge
                ""); // Duration
        root.getOptionsList().get(0).setText("Painful periods");
        root.getOptionsList().get(1).setText("Abnormal discharge");
        root.getOptionsList().get(2).setText("Duration");
        VisitUtils.removeNodesInvalidByGenderAndAge(root.getOptionsList(), "F", patientAge);
        List<Node> roots = new ArrayList<>();
        roots.add(root);
        return roots;
    }

    @Test
    public void s10_editFlow_ageLowered_hidesQuestionsKeepsOtherAnswers() throws Exception {
        List<Node> saved = abdominalPainFor(age(30, 0, 0)); // recorded at 30y
        assertEquals(3, saved.get(0).getOptionsList().size());
        Node savedDuration = saved.get(0).getOptionsList().get(2);
        savedDuration.setSelected(true);
        saved.get(0).getOptionsList().get(0).setSelected(true);

        List<Node> rebuilt = abdominalPainFor(age(5, 0, 0)); // patient edited to 5y
        List<Node> merged = VisitUtils.mergeSavedAnswers(rebuilt, saved);

        List<Node> questions = merged.get(0).getOptionsList();
        assertEquals(1, questions.size());
        assertEquals("Duration", questions.get(0).getText());
        assertTrue(questions.get(0) == savedDuration); // saved answer kept
        assertTrue(questions.get(0).isSelected());
    }

    @Test
    public void s10_editFlow_ageRaised_showsQuestionsAgain() throws Exception {
        List<Node> saved = abdominalPainFor(age(5, 0, 0)); // recorded at 5y
        assertEquals(1, saved.get(0).getOptionsList().size());
        saved.get(0).getOptionsList().get(0).setSelected(true);

        List<Node> rebuilt = abdominalPainFor(age(30, 0, 0)); // patient edited to 30y
        List<Node> merged = VisitUtils.mergeSavedAnswers(rebuilt, saved);

        List<Node> questions = merged.get(0).getOptionsList();
        assertEquals(3, questions.size());
        assertEquals("Painful periods", questions.get(0).getText());
        assertFalse(questions.get(0).isSelected()); // new question, not answered yet
        assertTrue(questions.get(2).isSelected()); // saved answer kept
    }

    // ---- 11. hyphenated keys used by newer protocols ("age-min" / "age-max") ----

    @Test
    public void s11_hyphenatedKeysParsed() throws Exception {
        Node n = node("\"gender\":\"0\",\"age-min\":{\"year\":12,\"month\":0,\"days\":0}");
        assertNotNull(n.getMinAgeLimit());
        assertEquals(12, n.getMinAgeLimit().getYears());
        assertNull(n.getMaxAgeLimit());
        assertFalse(shownInVisitFlow("\"age-min\":{\"year\":12,\"month\":0,\"days\":0}", age(5, 0, 0)));
        assertTrue(shownInVisitFlow("\"age-min\":{\"year\":12,\"month\":0,\"days\":0}", age(30, 0, 0)));

        Node both = node("\"age-min\":18,\"age-max\":{\"year\":50,\"month\":0,\"days\":0}");
        assertEquals(18, both.getMinAgeLimit().getYears());
        assertEquals(50, both.getMaxAgeLimit().getYears());
        assertTrue(both.getMinAgeLimit().isLegacy());
    }

    @Test
    public void s11_underscoreKeyWinsWhenBothPresent() throws Exception {
        Node n = node("\"age_min\":10,\"age-min\":20");
        assertEquals(10, n.getMinAgeLimit().getYears());
    }

    /** Abdominal pain (newer protocol): "Painful periods" under "Other associated symptoms", "age-min" 12y, female only. */
    @Test
    public void s11_abdominalPainNewProtocol_creationAndEdit() throws Exception {
        String painful = "\"gender\":\"0\",\"age-min\":{\"year\":12,\"month\":0,\"days\":0}";
        JSONObject root = new JSONObject(nodeJson("Abdominal pain", ""));
        JSONObject other = new JSONObject(nodeJson("Other associated symptoms", ""));
        other.put("options", new JSONArray()
                .put(new JSONObject(nodeJson("Painful periods", painful)))
                .put(new JSONObject(nodeJson("Abnormal vaginal discharge", painful)))
                .put(new JSONObject(nodeJson("Nausea", ""))));
        root.put("options", new JSONArray().put(other));
        String json = root.toString();

        // creation at 30y: all three shown
        Node at30 = new Node(new JSONObject(json));
        VisitUtils.removeNodesInvalidByGenderAndAge(at30.getOptionsList(), "F", age(30, 0, 0));
        assertEquals(3, at30.getOptionsList().get(0).getOptionsList().size());

        // edit to 5y: rebuild + merge drops the two age-limited ones
        Node at5 = new Node(new JSONObject(json));
        VisitUtils.removeNodesInvalidByGenderAndAge(at5.getOptionsList(), "F", age(5, 0, 0));
        List<Node> saved = new ArrayList<>();
        saved.add(at30);
        List<Node> rebuilt = new ArrayList<>();
        rebuilt.add(at5);
        List<Node> merged = VisitUtils.mergeSavedAnswers(rebuilt, saved);
        List<Node> left = merged.get(0).getOptionsList().get(0).getOptionsList();
        assertEquals(1, left.size());
        assertEquals("Nausea", left.get(0).getText());
    }

    // ---- 12. both API formats through Node, creation and edit (Breast Pain 18+) ----

    private static final String[] BREAST_PAIN_LIMITS = {
            "\"age_min\":\"18\",\"age_max\":\"120\"",
            "\"age_min\":18,\"age_max\":120",
            "\"age_min\":{\"year\":18,\"month\":0,\"days\":0},\"age_max\":{\"year\":120,\"month\":0,\"days\":0}",
            "\"age-min\":{\"year\":18,\"month\":0,\"days\":0},\"age-max\":{\"year\":120,\"month\":0,\"days\":0}",
    };

    /** Breast Pain: "Menstrual period" and "Breast feeding" (18+) under a question, plus an unrestricted option. */
    private static String breastPainJson(String limits) throws Exception {
        JSONObject question = new JSONObject(nodeJson("Associated factors", ""));
        question.put("options", new JSONArray()
                .put(new JSONObject(nodeJson("Menstrual period", limits)))
                .put(new JSONObject(nodeJson("Breast feeding", limits)))
                .put(new JSONObject(nodeJson("Trauma", ""))));
        JSONObject root = new JSONObject(nodeJson("Breast Pain", ""));
        root.put("options", new JSONArray().put(question));
        return root.toString();
    }

    private static List<String> breastPainOptions(Node root) {
        List<String> names = new ArrayList<>();
        for (Node n : root.getOptionsList().get(0).getOptionsList()) names.add(n.getText());
        return names;
    }

    private static Node breastPainFor(String json, AgeLimit age) throws Exception {
        Node root = new Node(new JSONObject(json));
        VisitUtils.removeNodesInvalidByGenderAndAge(root.getOptionsList(), "F", age);
        return root;
    }

    @Test
    public void s12_breastPain_creation_bothFormats_belowAtAbove() throws Exception {
        for (String limits : BREAST_PAIN_LIMITS) {
            String json = breastPainJson(limits);
            assertEquals(limits, 1, breastPainOptions(breastPainFor(json, age(9, 0, 0))).size());
            assertEquals(limits, 1, breastPainOptions(breastPainFor(json, age(17, 11, 29))).size());
            assertEquals(limits, 3, breastPainOptions(breastPainFor(json, age(18, 0, 0))).size());
            assertEquals(limits, 3, breastPainOptions(breastPainFor(json, age(30, 0, 0))).size());
        }
    }

    @Test
    public void s12_breastPain_edit_bothFormats() throws Exception {
        for (String limits : BREAST_PAIN_LIMITS) {
            String json = breastPainJson(limits);

            // recorded at 30y, edited to 9y: 18+ options hidden, Trauma answer kept
            Node saved = breastPainFor(json, age(30, 0, 0));
            saved.getOptionsList().get(0).getOptionsList().get(2).setSelected(true);
            List<Node> merged = VisitUtils.mergeSavedAnswers(
                    java.util.Collections.singletonList(breastPainFor(json, age(9, 0, 0))),
                    java.util.Collections.singletonList(saved));
            List<String> left = breastPainOptions(merged.get(0));
            assertEquals(limits, 1, left.size());
            assertEquals("Trauma", left.get(0));
            assertTrue(merged.get(0).getOptionsList().get(0).getOptionsList().get(0).isSelected());

            // recorded at 9y, edited to 18y: 18+ options shown again
            Node savedChild = breastPainFor(json, age(9, 0, 0));
            List<Node> mergedAdult = VisitUtils.mergeSavedAnswers(
                    java.util.Collections.singletonList(breastPainFor(json, age(18, 0, 0))),
                    java.util.Collections.singletonList(savedChild));
            assertEquals(limits, 3, breastPainOptions(mergedAdult.get(0)).size());
        }
    }

    @Test
    public void s12_stringFormat_10_to_120_belowAtAbove() throws Exception {
        String attrs = "\"age_min\":\"10\",\"age_max\":\"120\"";
        assertFalse(shownInVisitFlow(attrs, age(9, 11, 29)));
        assertTrue(shownInVisitFlow(attrs, age(10, 0, 0)));
        assertTrue(shownInVisitFlow(attrs, age(10, 0, 1)));
        assertTrue(shownInVisitFlow(attrs, age(120, 0, 0)));
        assertFalse(shownInVisitFlow(attrs, age(120, 1, 0)));
    }

    @Test
    public void s12_objectFormat_12_to_120_belowAtAbove() throws Exception {
        String attrs = "\"age_min\":{\"year\":12,\"month\":0,\"days\":0},\"age_max\":{\"year\":120,\"month\":0,\"days\":0}";
        assertFalse(shownInVisitFlow(attrs, age(11, 11, 30)));
        assertTrue(shownInVisitFlow(attrs, age(12, 0, 0)));
        assertTrue(shownInVisitFlow(attrs, age(12, 0, 1)));
        assertTrue(shownInVisitFlow(attrs, age(120, 0, 0)));
        assertFalse(shownInVisitFlow(attrs, age(120, 0, 1)));
    }

    @Test
    public void s10_editFlow_nothingSaved_usesRebuiltTree() throws Exception {
        List<Node> rebuilt = abdominalPainFor(age(30, 0, 0));
        assertTrue(VisitUtils.mergeSavedAnswers(rebuilt, null) == rebuilt);
        assertTrue(VisitUtils.mergeSavedAnswers(rebuilt, new ArrayList<>()) == rebuilt);
        assertNull(VisitUtils.mergeSavedAnswers(null, rebuilt));
    }

    @Test
    public void s9_patientAgeFromDatabase_unknownPatientFallsBack() {
        String dob = PatientsDAO.fetch_dob("no-such-patient-uuid");
        assertEquals("", dob);
        AgeLimit a = AgeLimit.forPatient(dob, 7.3f);
        assertEquals(7, a.getYears());
        assertEquals(3, a.getMonths());
    }
}

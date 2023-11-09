package org.intelehealth.ezazi.partogram;

import static org.intelehealth.ezazi.utilities.UuidDictionary.TEMPERATURE;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.partogram.model.ParamInfo;
import org.intelehealth.ezazi.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class PartogramConstants {
    public static String DROPDOWN_SINGLE_SELECT_TYPE = "A";
    public static String DROPDOWN_MULTI_SELECT_TYPE = "B";
    public static String AUTOCOMPLETE_SUGGESTION_EDITTEXT = "A1";

    public static String INPUT_TXT_TYPE = "C";
    public static String INPUT_TXT_2_CHR_TYPE = "D";

    public static String INPUT_INT_1_DIG_TYPE = "E";
    public static String INPUT_INT_2_DIG_TYPE = "F";
    public static String INPUT_INT_3_DIG_TYPE = "G";
    public static String INPUT_DOUBLE_4_DIG_TYPE = "H";

    public static String RADIO_SELECT_TYPE = "I";

    private static SessionManager sessionManager = null;

    public static final int STAGE_1 = 1;
    public static final int STAGE_2 = 2;

    public enum AccessMode {
        WRITE, EDIT, READ
    }

    public static final String TIMELINE_MODE = "access_mode";

    public static String[] SECTION_LIST = {
            "Supportive care",
            "Baby",
            "Woman",
            "Labour Progress",
            "Medication Administration",
            "Shared Decision Making"
//            ,
//            "Initials"
    };

    public enum Params {
        SYSTOLIC_BP("Systolic BP"),
        DIASTOLIC_BP("Diastolic BP"),
        IV_FLUID("IV Fluids"),

        TEMPERATURE("Temperature(C)"),
        BASELINE_FHR("Baseline FHR"),
        PULSE("Pulse"),
        DURATION_OF_CONTRACTION("Duration of contractions");


        public final String value;

        Params(String value) {
            this.value = value;
        }
    }

    public static TreeMap<String, List<ParamInfo>> getSectionParamInfoMasterMap(int stage) {
        TreeMap<String, List<ParamInfo>> sectionParamInfoMap = new TreeMap<>();
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        //Supportive care
        List<ParamInfo> stringList = new ArrayList<ParamInfo>();
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[0]);
        paramInfo.setParamName("Companion");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"Yes", "No", "Woman Declines"});
        paramInfo.setValues(new String[]{"Y", "N", "D"});
        paramInfo.setConceptUUID("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[0]);
        paramInfo.setParamName("Pain relief");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"Yes", "No", "Woman Declines"});
        paramInfo.setValues(new String[]{"Y", "N", "D"});
        paramInfo.setConceptUUID("9d313f72-538f-11e6-9cfe-86f436325720");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[0]);
        paramInfo.setParamName("Oral Fluid");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"Yes", "No", "Woman Declines"});
        paramInfo.setValues(new String[]{"Y", "N", "D"});
        paramInfo.setConceptUUID("9d31451b-538f-11e6-9cfe-86f436325720");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[0]);
        paramInfo.setParamName("Posture");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"Supine", "Mobile"});
        paramInfo.setValues(new String[]{"SP", "MO"});
        paramInfo.setConceptUUID("9d3148b1-538f-11e6-9cfe-86f436325720");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        sectionParamInfoMap.put(SECTION_LIST[0], stringList);

        //BABY
        stringList = new ArrayList<>();

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[1]);
        paramInfo.setParamName("Baseline FHR");
        paramInfo.setParamDateType(INPUT_INT_3_DIG_TYPE);
        paramInfo.setHalfHourField(true);
        paramInfo.setFifteenMinField(true);
        paramInfo.setConceptUUID("9d315400-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[1]);
        paramInfo.setParamName("FHR Deceleration");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"No", "Early", "Late", "Variable"});
        paramInfo.setValues(new String[]{"N", "E", "L", "V"});
        paramInfo.setHalfHourField(true);
        paramInfo.setFifteenMinField(true);
        paramInfo.setConceptUUID("9d31573c-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[1]);
        paramInfo.setParamName("Amniotic Fluid Meconium");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"Intact", "Clear", "Meconium-Stained Fluid (Non-significant)", "Meconium-Stained Fluid (Medium)",
                "Meconium-Stained Fluid (Thick)", "Blood Stained"});
        paramInfo.setValues(new String[]{"I", "C", "M+", "M++", "M+++", "B"});
        paramInfo.setConceptUUID("9d3160a6-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[1]);
        paramInfo.setParamName("Fetal Position");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"Any occiput anterior position", "Any occiput posterior position", "Any occiput transverse position"});
        paramInfo.setValues(new String[]{"A", "P", "T"});
        paramInfo.setConceptUUID("9d316387-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[1]);
        paramInfo.setParamName("Caput");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"None", "+(Marked)", "++(Marked)", "+++(Marked)"});
        paramInfo.setValues(new String[]{"N", "+", "++", "+++"});
        paramInfo.setConceptUUID("9d316761-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[1]);
        paramInfo.setParamName("Moulding");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
//        paramInfo.setOptions(new String[]{"None", "Sutures apposed", "Sutures overlapped but reducible", "Sutures overlapped and not reducible"});
        paramInfo.setOptions(new String[]{"0 (None)", "+ (Sutures apposed)", "++ (Sutures overlapped but reducible)", "+++ (Sutures overlapped but not reducible)"});
        paramInfo.setValues(new String[]{"N", "+", "++", "+++"});
        paramInfo.setConceptUUID("9d316823-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        sectionParamInfoMap.put(SECTION_LIST[1], stringList); //BABY

        //Woman
        stringList = new ArrayList<>();

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[2]);
        paramInfo.setParamName("Pulse");
        paramInfo.setParamDateType(INPUT_INT_3_DIG_TYPE);
        paramInfo.setConceptUUID("5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[2]);
        paramInfo.setParamName(Params.SYSTOLIC_BP.value);
        paramInfo.setParamDateType(INPUT_INT_3_DIG_TYPE);
        paramInfo.setConceptUUID("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[2]);
        paramInfo.setParamName(Params.DIASTOLIC_BP.value);
        paramInfo.setParamDateType(INPUT_INT_3_DIG_TYPE);
        paramInfo.setConceptUUID("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[2]);
        paramInfo.setParamName(Params.TEMPERATURE.value); // in centigrade i.e. C
        paramInfo.setParamDateType(INPUT_DOUBLE_4_DIG_TYPE);
        paramInfo.setConceptUUID("5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[2]);
        paramInfo.setParamName("Urine protein");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"No Proteinuria (P-)", "Trace of Proteinuria (P Trace)", "P1+", "P2+", "P3+", "P4+"});
//        paramInfo.setValues(new String[]{"P-", "P", "P1", "P2", "P3"});
        paramInfo.setValues(new String[]{"Negative", "Trace", "P1+", "P2+", "P3+", "P4+"});
        paramInfo.setConceptUUID("9d3168a7-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[2]);
        paramInfo.setParamName("Urine Acetone");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"No Acetonuria (A-)", "Trace of Acetonuria (A Trace)", "A1+", "A2+", "A3+", "A4+"});
        paramInfo.setValues(new String[]{"Negative", "Trace", "A1+", "A2+", "A3+", "A4+"});
        paramInfo.setConceptUUID("968f9bc2-b33d-4daf-b59f-79d9a899e018");
        stringList.add(paramInfo);

        sectionParamInfoMap.put(SECTION_LIST[2], stringList);//Woman

        //Labour Progress
        stringList = new ArrayList<>();

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[3]);
        paramInfo.setParamName("Contractions per 10 min");
        paramInfo.setParamDateType(INPUT_INT_1_DIG_TYPE);
        paramInfo.setHalfHourField(true);
        paramInfo.setFifteenMinField(true);
        paramInfo.setConceptUUID("9d316929-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[3]);
        paramInfo.setParamName("Duration of contractions");
        paramInfo.setParamDateType(INPUT_INT_3_DIG_TYPE);
        paramInfo.setHalfHourField(true);
        paramInfo.setFifteenMinField(true);
        paramInfo.setConceptUUID("9d3169af-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[3]);
        paramInfo.setParamName("Cervix Plot[X]");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"10", "9", "8", "7", "6", "5"});
        paramInfo.setValues(new String[]{"10", "9", "8", "7", "6", "5"});
        if (stage == STAGE_2) {
            paramInfo.setOptions(new String[]{"10"});
            paramInfo.setValues(new String[]{"P"});
        }
        paramInfo.setConceptUUID("9d316ab5-538f-11e6-9cfe-86f436325720");
        paramInfo.setFifteenMinField(true);
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[3]);
        paramInfo.setParamName("Descent Plot[O]");
        paramInfo.setParamDateType(DROPDOWN_SINGLE_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"5", "4", "3", "2", "1", "0"});
        paramInfo.setValues(new String[]{"5", "4", "3", "2", "1", "0"});
        paramInfo.setConceptUUID("9d316d41-538f-11e6-9cfe-86f436325720");
        stringList.add(paramInfo);

        sectionParamInfoMap.put(SECTION_LIST[3], stringList);//Labour Progress

        //Medication
        stringList = new ArrayList<>();

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[4]);
//        if (sessionManager.getOxytocinValue() != null)
//            paramInfo.setParamName("Oxytocin (" + sessionManager.getOxytocinValue() + ")");
//        else
        paramInfo.setParamName("Oxytocin (U/L, drops/min)");
        // paramInfo.setParamDateType(INPUT_TXT_TYPE);
        paramInfo.setParamDateType(RADIO_SELECT_TYPE);
        paramInfo.setStatus(new String[]{"Started", "Continued", "Stopped"});
        paramInfo.setConceptUUID("9d316d82-538f-11e6-9cfe-86f436325720");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);


        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[4]);
        paramInfo.setParamName("Medicine");
        paramInfo.setParamDateType(RADIO_SELECT_TYPE);
        paramInfo.setOptions(new String[]{"Yes", "No"});
        //paramInfo.setValues(new String[]{"Y", "N"});
        paramInfo.setConceptUUID("c38c0c50-2fd2-4ae3-b7ba-7dd25adca4ca");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[4]);
        paramInfo.setParamName(Params.IV_FLUID.value);
        paramInfo.setParamDateType(RADIO_SELECT_TYPE);
        paramInfo.setRadioOptions(new String[]{"Yes", "No"});
        paramInfo.setOptions(new String[]{"Ringer Lactate", "Normal Saline", "Dextrose 5% (D5)", "Other IV Fluid*"});
        paramInfo.setValues(new String[]{"Ringer Lactate", "Normal Saline", "Dextrose 5% (D5)", AppConstants.OTHER_OPTION});
        paramInfo.setStatus(new String[]{"Started", "Continued", "Stopped"});
        paramInfo.setConceptUUID("98c5881f-b214-4597-83d4-509666e9a7c9");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        sectionParamInfoMap.put(SECTION_LIST[4], stringList);//Medication

        //Shared Decision Making
        stringList = new ArrayList<>();

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[5]);
        paramInfo.setParamName("Assessment");
        paramInfo.setParamDateType(INPUT_TXT_TYPE);
        paramInfo.setConceptUUID("67a050c1-35e5-451c-a4ab-fff9d57b0db1");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[5]);
        paramInfo.setParamName("Plan");
        paramInfo.setParamDateType(INPUT_TXT_TYPE);
        paramInfo.setConceptUUID("162169AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        paramInfo.setOnlyOneHourField(true);
        stringList.add(paramInfo);

        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName(SECTION_LIST[5]);
        paramInfo.setParamName("Supervisor Doctor");
        paramInfo.setParamDateType(INPUT_TXT_TYPE);
        paramInfo.setConceptUUID("7a9cb7bc-9ab9-4ff0-ae82-7a1bd2cca93e");
        stringList.add(paramInfo);

        sectionParamInfoMap.put(SECTION_LIST[5], stringList);//Shared Decision Making

        //Initials
//        stringList = new ArrayList<>();
//        paramInfo = new ParamInfo();
//        paramInfo.setParamSectionName(SECTION_LIST[6]);
//        paramInfo.setParamName("Initial");
//        paramInfo.setParamDateType(INPUT_TXT_TYPE);
//        paramInfo.setConceptUUID("165171AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//        stringList.add(paramInfo);
//
//        sectionParamInfoMap.put(SECTION_LIST[6], stringList);//Initials

        //Actions
        stringList = new ArrayList<>();
        paramInfo = new ParamInfo();
        paramInfo.setParamSectionName("Action");
        paramInfo.setParamName("Birth Outcome");
        paramInfo.setParamDateType(INPUT_TXT_TYPE);
        paramInfo.setConceptUUID("23601d71-50e6-483f-968d-aeef3031346d");
        stringList.add(paramInfo);
        sectionParamInfoMap.put("Action", stringList);//Actions

        return sectionParamInfoMap;
    }
}

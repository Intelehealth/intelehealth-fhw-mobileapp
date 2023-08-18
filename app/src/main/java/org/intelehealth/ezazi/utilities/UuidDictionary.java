package org.intelehealth.ezazi.utilities;

/**
 * Created by Dexter Barretto on 7/26/17.
 * Github : @dbarretto
 */

public class UuidDictionary {

    //Concepts
    public static final String CURRENT_COMPLAINT = "3edb0e09-9135-481e-b8f0-07a26fa9a5ce";
    public static final String PHYSICAL_EXAMINATION = "e1761e85-9b50-48ae-8c4d-e6b7eeeba084";
    public static final String HEIGHT = "5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String WEIGHT = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    //    public static final String PULSE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
//    public static final String SYSTOLIC_BP = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
//    public static final String DIASTOLIC_BP = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
//    public static final String TEMPERATURE = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String RESPIRATORY = "5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String SPO2 = "5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String RHK_MEDICAL_HISTORY_BLURB = "62bff84b-795a-45ad-aae1-80e7f5163a82";
    public static final String RHK_FAMILY_HISTORY_BLURB = "d63ae965-47fb-40e8-8f08-1f46a8a60b2b";
    public static final String FOLLOW_UP_VISIT = "e8caffd6-5d22-41c4-8d6a-bc31a44d0c86";
    //m added
    public static final String EMERGENCY = "ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c";

    public static final String TELEMEDICINE_DIAGNOSIS = "537bb20d-d09d-4f88-930b-cc45c7d662df";
    public static final String JSV_MEDICATIONS = "c38c0c50-2fd2-4ae3-b7ba-7dd25adca4ca";
    //    public static final String MEDICAL_ADVICE = "0308000d-77a2-46e0-a6fa-a8c1dcbc3141"; //old uuid medical advice
    public static final String MEDICAL_ADVICE = "67a050c1-35e5-451c-a4ab-fff9d57b0db1";
    public static final String REQUESTED_TESTS = "23601d71-50e6-483f-968d-aeef3031346d";
    public static final String ADDITIONAL_COMMENTS = "162169AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    public static final String COMPLEX_IMAGE_AD = "07a816ce-ffc0-49b9-ad92-a1bf9bf5e2ba";

    public static final String COMPLEX_IMAGE_PE = "200b7a45-77bc-4986-b879-cc727f5f7d5b";

    public static final String SON_WIFE_DAUGHTER = "35c3afdd-bb96-4b61-afb9-22a5fc2d088e";
    public static final String OCCUPATION = "5fe2ef6f-bbf7-45df-a6ea-a284aee82ddc";
    //Encounter Types
    public static final String ENCOUNTER_ADULTINITIAL = "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f";
    public static final String ENCOUNTER_VITALS = "67a71486-1a54-468f-ac3e-7091a9a79584";
    public static final String ENCOUNTER_VISIT_NOTE = "d7151f82-c1f3-4152-a605-2f9ea7414a79";
    public static final String ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e";
    public static final String ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30";
    public static final String ENCOUNTER_ROLE = "73bbb069-9781-4afc-a9d1-54b6b2270e04";
    public static final String ENCOUNTER_DR_ROLE = "73bbb069-9781-4afc-a9d1-54b6b2270e03";
    public static final String ENCOUNTER_DR_PROVIDER = "f2f9948e-50e2-434e-8bf1-00a7426d6cc8";
    public static final String OBS_DOCTORDETAILS = "7a9cb7bc-9ab9-4ff0-ae82-7a1bd2cca93e";

    public static final String EMERGENCY_OBS = "ca5f5dc3-4f0b-4097-9cae-5cf2eb44a09c";

    //Patient Identifier Type
    public static final String IDENTIFIER_OPENMRS_ID = "05a29f94-c0ed-11e2-94be-8c13b969e334";

    //Person Attribute Type
    public static final String ATTRIBUTE_PHONE_NUMBER = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
    public static final String ATTRIBUTE_CASTE = "5a889d96-0c84-4a04-88dc-59a6e37db2d3";
    public static final String ATTRIBUTE_EDUCATION_LEVEL = "1c718819-345c-4368-aad6-d69b4c267db7";
    public static final String ATTRIBUTE_ECONOMIC_STATUS = "f4af0ef3-579c-448a-8157-750283409122";
    public static final String ATTRIBUTE_SON_WIFE_DAUGHTER = "1b2f34f7-2bf8-4ef7-9736-f5b858afc160";
    public static final String ATTRIBUTE_OCCUPATION = "ecdaadb6-14a0-4ed9-b5b7-cfed87b44b87";
    public static final String ATTRIBUTE_HEALTH_CENTER = "8d87236c-c2cc-11de-8d13-0010c6dffd0f";


    //Visit Types
    public static final String VISIT_TELEMEDICINE = "a86ac96e-2e07-47a7-8e72-8216a1a75bfd";

    //Survey Types
    public static final String RATING = "78284507-fb71-4354-9b34-046ab205e18f";
    public static final String COMMENTS = "36d207d6-bee7-4b3e-9196-7d053c6eddce";

    // TimeLine Concept Obs UUIDs...
    // Supportive Care
    public static final String COMPANION = "5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PAIN_RELIEF = "9d313f72-538f-11e6-9cfe-86f436325720";
    public static final String ORAL_FLUID = "9d31451b-538f-11e6-9cfe-86f436325720";
    public static final String POSTURE = "9d3148b1-538f-11e6-9cfe-86f436325720";

    // Baby
    public static final String BASELINE_FHR = "9d315400-538f-11e6-9cfe-86f436325720";
    public static final String FHR_DECELERATION = "9d31573c-538f-11e6-9cfe-86f436325720";
    public static final String AMNIOTIC_FLUID = "9d3160a6-538f-11e6-9cfe-86f436325720";
    public static final String FETAL_POSITION = "9d316387-538f-11e6-9cfe-86f436325720";
    public static final String CAPUT = "9d316761-538f-11e6-9cfe-86f436325720";
    public static final String MOULDING = "9d316823-538f-11e6-9cfe-86f436325720";

    // Woman
    public static final String PULSE = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String SYSTOLIC_BP = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String DIASTOLIC_BP = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String TEMPERATURE = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String URINE_PROTEIN = "9d3168a7-538f-11e6-9cfe-86f436325720";

    // LABOUR PROGRESS
    public static final String CONTRACTIONS_PER_10MIN = "9d316929-538f-11e6-9cfe-86f436325720";
    public static final String DURATION_OF_CONTRACTION = "9d3169af-538f-11e6-9cfe-86f436325720";
    public static final String CERVIX_PLOT_X = "9d316ab5-538f-11e6-9cfe-86f436325720";
    public static final String DESCENT_PLOT_0 = "9d316d41-538f-11e6-9cfe-86f436325720";

    // MEDICATION
    public static final String OXYTOCIN_UL_DROPS_MIN = "9d316d82-538f-11e6-9cfe-86f436325720";
    public static final String MEDICINE = "c38c0c50-2fd2-4ae3-b7ba-7dd25adca4ca";
    public static final String IV_FLUIDS = "98c5881f-b214-4597-83d4-509666e9a7c9";

    // SHARED_DECISION_MAKING
    public static final String ASSESSMENT = "67a050c1-35e5-451c-a4ab-fff9d57b0db1";
    public static final String PLAN = "162169AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    // MISSED ENCOUNTER _ OBS
    public static final String MISSED_ENCOUNTER = "35c3afdd-bb96-4b61-afb9-22a5fc2d088e";

    // BIRTH OUTCOME
    public static final String BIRTH_OUTCOME = "23601d71-50e6-483f-968d-aeef3031346d";

    // Refer Obs
    public static final String REFER_TYPE = "9414cc17-1f97-4a27-8066-17591c11e513";
    public static final String REFER_HOSPITAL = "0092c193-8025-4ede-8aab-8a2db731da51";
    public static final String REFER_DR_NAME = "d9625aed-c5ee-4d5c-92b1-ee3782e31ef3";
    public static final String REFER_NOTE = "9965063f-ec0c-4a58-bfba-5ca4ac53129c";

    // Ending stage 2 capture additional advice
    public static final String BIRTH_WEIGHT = "debdcf33-e565-466a-aca1-6d98a8441553";
    public static final String APGAR_1_MIN = "8f218b75-2576-45a3-b6e6-1429dede71bd";
    public static final String APGAR_5_MIN = "f3925225-f792-4a6e-b890-5f6fb3941c1d";
    public static final String SEX = "7f6ceddb-3a9b-4917-b783-c174b95ea7d4";
    public static final String BABY_STATUS = "4a1edd41-ffda-4da8-93e0-cc7af502b80e";
    public static final String MOTHER_STATUS = "69a33fc0-8bc2-4828-80d2-7e55dedd89f3";

    public static final String ENCOUNTER_TYPE = "d1fb190a-9ebb-448f-8d61-dfeeb20fd931";

    public static final String MOTHER_DECEASED = "91c94e0b-b967-4dd0-9eec-75d770af7f5b "; //newly added

    public static final String OUT_OF_TIME = "893b3d20-171b-4023-a6f5-3d3bdf81a094"; //newly added
    public static final String MOTHER_DECEASED_FLAG = "9d31f9f8-538f-11e6-9cfe-86f436325720"; //newly added

    public static final String LABOUR_OTHER = "ac99754c-d3a9-4736-a8b8-276c78953315";

    public static final String END_2ND_STAGE_OTHER = "d319ebbc-5260-4eff-aa3f-2af11b177ec4";

}

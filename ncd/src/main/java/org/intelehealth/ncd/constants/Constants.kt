package org.intelehealth.ncd.constants

object Constants {
    const val OTHER_MEDICAL_HISTORY: String = "0bc1ce08-4013-4959-80c6-a6ccf7a208c2"
    const val ATTRIBUTE_PHONE_NUMBER = "14d4f066-15f5-102d-96e4-000c29c2a5d7"
    const val ANEMIA_EXCLUSION_AGE: Int = 11 // Greater or equal to 11 are considered
    const val DIABETES_EXCLUSION_AGE: Int = 20 // Greater or equal to 20 are considered
    const val HYPERTENSION_EXCLUSION_AGE: Int = 18 // Greater than or equal to 18 are considered
    const val GENERAL_EXCLUSION_AGE: Int = 11 // Below age 11 are considered

    const val INTENT_PATIENT_UUID = "patientUuid"
    const val INTENT_PATIENT_NAME = "patientName"
    const val INTENT_PATIENT_STATUS = "status"
    const val INTENT_PATIENT_TAG = "tag"
    const val INTENT_HAS_PRESCRIPTION = "hasPrescription"
    const val INTENT_NCD_CATEGORY = "tabName"

    // category
    const val ANEMIA_SCREENING = "anemia_screening"
    const val ANEMIA_FOLLOW_UP = "anemia_follow_up"
    const val DIABETES_SCREENING = "diabetes_screening"
    const val DIABETES_FOLLOW_UP = "diabetes_follow_up"
    const val HYPERTENSION_SCREENING = "hypertension_screening"
    const val HYPERTENSION_FOLLOW_UP = "hypertension_follow_up"
    const val GENERAL = "general"

    // intents
    const val IS_PRIVACY_NOTICE = "isPrivacyNotice"

    //const val VISIT_NOTE = "d7151f82-c1f3-4152-a605-2f9ea7414a79" //ENCOUNTER_VISIT_NOTE
    const val ENCOUNTER_VISIT_COMPLETE: String = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"  //Prescription
    const val IS_NCD_VISIT_ATTRIBUTE: String = "bc79d2ab-3c83-48f2-820d-08a02b32faab"  // is ncd visit attribute
    const val SPECIALITY: String = "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d"  //visit speciality
    const val PATIENT_PHONE: String = "14d4f066-15f5-102d-96e4-000c29c2a5d7"  //patient phone


}
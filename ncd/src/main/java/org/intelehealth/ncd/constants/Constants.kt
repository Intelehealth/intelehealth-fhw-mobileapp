package org.intelehealth.ncd.constants

object Constants {
    const val OTHER_MEDICAL_HISTORY: String = "0bc1ce08-4013-4959-80c6-a6ccf7a208c2"
    const val ANEMIA_EXCLUSION_AGE: Int = 11 // Greater or equal to 11 are considered
    const val DIABETES_EXCLUSION_AGE: Int = 20 // Greater or equal to 20 are considered
    const val HYPERTENSION_EXCLUSION_AGE: Int = 18 // Greater than or equal to 18 are considered
    const val GENERAL_EXCLUSION_AGE: Int = 11 // Below age 11 are considered

    const val INTENT_PATIENT_UUID = "patientUuid"
    const val INTENT_PATIENT_NAME = "patientName"
    const val INTENT_PATIENT_STATUS = "status"
    const val INTENT_PATIENT_TAG = "tag"
    const val INTENT_HAS_PRESCRIPTION = "hasPrescription"

    // category
    const val ANEMIA_SCREENING = "anemia_screening"
    const val ANEMIA_FOLLOW_UP = "anemia_follow_up"
    const val DIABETES_SCREENING = "diabetes_screening"
    const val DIABETES_FOLLOW_UP = "diabetes_follow_up"
    const val HYPERTENSION_SCREENING = "hypertension_screening"
    const val HYPERTENSION_FOLLOW_UP = "hypertension_follow_up"
    const val GENERAL = "general"

}
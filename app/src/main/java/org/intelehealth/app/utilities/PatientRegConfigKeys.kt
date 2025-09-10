package org.intelehealth.app.utilities

/**
 * Created by Tanvir Hasan on 29-04-2024 : 16-31.
 * Email: mhasan@intelehealth.org
 *
 * Patient register config keys
 */
class PatientRegConfigKeys {
    companion object {
        //PERSONAL
        const val PROFILE_PHOTO = "p_profile_photo"
        const val FIRST_NAME = "p_first_name"
        const val MIDDLE_NAME = "p_middle_name"
        const val LAST_NAME = "p_last_name"
        const val GENDER = "p_gender"
        const val DOB = "p_date_of_birth"
        const val AGE = "p_age"
        const val PHONE_NUM = "p_phone_number"
        const val GUARDIAN_NAME = "p_guardian_name"
        const val GUARDIAN_TYPE = "p_guardian_type"
        const val EM_CONTACT_NAME = "p_emergency_contact_name"
        const val EM_CONTACT_NUMBER = "p_emergency_contact_number"
        const val EM_CONTACT_TYPE = "p_contact_type"

        //ADDRESS
        const val POSTAL_CODE = "a_postal_address"
        const val COUNTRY = "a_country"
        const val STATE = "a_state"
        const val DISTRICT = "a_district"
        const val VILLAGE_TOWN_CITY = "a_village_town_city"
        const val ADDRESS_1 = "a_corresponding_address_1"
        const val ADDRESS_2 = "a_corresponding_address_2"

        //OTHERS
        const val NATIONAL_ID = "o_national_id"
        const val OCCUPATION = "o_occupation"
        const val SOCIAL_CATEGORY = "o_social_category"
        const val EDUCATION = "o_education"
        const val ECONOMIC_CATEGORY = "o_economic_category"

        // GENERAL
        const val AYUSHMAN_CARD = "general_ayushman_card"
        const val MGNREGA_CARD = "general_mgnrega_card"
        const val BANK_ACCOUNT = "general_bank_account"
        const val PHONE_OWNERSHIP = "general_phone_ownership"
        const val FAMILY_WHATSAPP = "general_family_whatsapp"
        const val MARITAL_STATUS = "general_marital_status"
        const val GENERAL_OCCUPATION = "general_occupation"
        const val GENERAL_CASTE = "general_caste"
        const val GENERAL_EDUCATION = "general_education"
        const val GENERAL_ECONOMIC_STATUS = "general_economic_status"

        // MEDICAL
        const val HB_CHECK = "hb_check"
        const val BP_CHECK = "bp_check"
        const val SUGAR_CHECK = "sugar_check"
        const val BP_VALUE = "bp_value"
        const val DIABETES_VALUE = "diabetes_value"
        const val ARTHRITIS_VALUE = "arthritis_value"
        const val ANEMIA_VALUE = "anemia_value"
        const val SURGERY_VALUE = "surgery_value"
        const val SURGERY_REASON = "surgery_reason"
        const val SMOKING_HISTORY = "smoking_history"
        const val SMOKING_RATE = "smoking_rate"
        const val SMOKING_DURATION = "smoking_duration"
        const val SMOKING_FREQUENCY = "smoking_frequency"
        const val CHEW_TOBACCO = "chew_tobacco"
        const val ALCOHOL_HISTORY = "alcohol_history"
        const val ALCOHOL_RATE = "alcohol_rate"
        const val ALCOHOL_DURATION = "alcohol_duration"
        const val ALCOHOL_FREQUENCY = "alcohol_frequency"
        const val TAKING_ANY_MEDICATION_FOR_ANEMIA = "taking_any_medication_for_anemia"
        const val HAVE_YOU_SEEN_TO_HW_IN_PAST_ONE_YEAR_FOR_ANEMIA = "have_you_seen_to_hw_in_past_one_year_for_anemia"
        const val REASON_FOR_NOT_TAKING_ANEMIA_MEDICATION = "reason_for_not_taking_anemia_medication"
        const val OTHER_REASON_FOR_NOT_TAKING_ANEMIA_MEDICATION = "other_reason_for_not_taking_anemia_medication"
        const val TAKING_ANY_MEDICATION_FOR_BP = "taking_any_medication_for_bp"
        const val HAVE_YOU_SEEN_TO_HW_IN_PAST_ONE_YEAR_FOR_BP = "have_you_seen_to_hw_in_past_one_year_for_bp"
        const val REASON_FOR_NOT_TAKING_BP_MEDICATION = "reason_for_not_taking_bp_medication"

        // OTHERS
        const val HEAD_OF_HOUSEHOLD = "HEAD_OF_HOUSEHOLD"
        const val RATION_CARD_CHECK = "RATION_CARD_CHECK"
        const val ECONOMIC_STATUS = "ECONOMIC_STATUS"
        const val OTHER_BASELINE_RELIGION = "OTHER_BASELINE_RELIGION"
        const val TOTAL_HOUSEHOLD_MEMBERS = "TOTAL_HOUSEHOLD_MEMBERS"
        const val USUAL_HOUSEHOLD_MEMBERS = "USUAL_HOUSEHOLD_MEMBERS"
        const val NUMBER_OF_SMARTPHONES = "NUMBER_OF_SMARTPHONES"
        const val NUMBER_OF_FEATURE_PHONES = "NUMBER_OF_FEATURE_PHONES"
        const val NUMBER_OF_EARNING_MEMBERS = "NUMBER_OF_EARNING_MEMBERS"
        const val ELECTRICITY_CHECK = "ELECTRICITY_CHECK"
        const val LOAD_SHEDDING_HOURS = "LOAD_SHEDDING_HOURS"
        const val LOAD_SHEDDING_DAYS = "LOAD_SHEDDING_DAYS"
        const val WATER_CHECK = "WATER_CHECK"
        const val WATER_AVAILABILITY_HOURS = "WATER_AVAILABILITY_HOURS"
        const val WATER_AVAILABILITY_DAYS = "WATER_AVAILABILITY_DAYS"
        const val SOURCE_OF_WATER = "SOURCE_OF_WATER"
        const val SAFEGUARD_WATER = "SAFEGUARD_WATER"
        const val DISTANCE_FROM_WATER = "DISTANCE_FROM_WATER"
        const val TOILET_FACILITY = "TOILET_FACILITY"
        const val HOUSE_STRUCTURE = "HOUSE_STRUCTURE"
        const val CULTIVABLE_LAND = "CULTIVABLE_LAND"
        const val CULTIVABLE_LAND_VALUE = "CULTIVABLE_LAND_VALUE"
        const val AVERAGE_INCOME = "AVERAGE_INCOME"
        const val FUEL_TYPE = "FUEL_TYPE"
        const val SOURCE_OF_LIGHT = "SOURCE_OF_LIGHT"
        const val HANDWASH_PRACTICES = "HANDWASH_PRACTICES"
        const val EKAL_SERVICE_CHECK = "EKAL_SERVICE_CHECK"
        const val RELATION_WITH_HOUSEHOLD = "RELATION_WITH_HOUSEHOLD"
    }
}
package org.intelehealth.app.utilities

/**
 * Created by Tanvir Hasan on 29-04-2024 : 16-31.
 * Email: mhasan@intelehealth.org
 *
 * Patient register config keys
 */
class PatientRegConfigKeys {
    companion object{
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
    }
}
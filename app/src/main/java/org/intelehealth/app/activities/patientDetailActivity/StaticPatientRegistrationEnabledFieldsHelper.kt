package org.intelehealth.app.activities.patientDetailActivity

import org.intelehealth.app.utilities.PatientRegConfigKeys
import org.intelehealth.config.room.entity.PatientRegistrationFields

object StaticPatientRegistrationEnabledFieldsHelper {

    fun getEnabledPersonalInfoFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        // Profile photo
        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.PROFILE_PHOTO,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // First name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.FIRST_NAME,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Middle name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.MIDDLE_NAME,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Last name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.LAST_NAME,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Gender
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GENDER,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Dob
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.DOB,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Age
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.AGE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Guardian Name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GUARDIAN_NAME,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Guardian Type
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GUARDIAN_TYPE,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Phone Number
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.PHONE_NUM,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Emergency Contact Type
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EM_CONTACT_TYPE,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Emergency Contact Name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EM_CONTACT_NAME,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Emergency Phone Number
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EM_CONTACT_NUMBER,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)
        return fields
    }

    fun getEnabledAddressInfoFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        // Postal Code
        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.POSTAL_CODE,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Country
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.COUNTRY,
            isMandatory = true,
            isEditable = false,
            isEnabled = true
        )

        fields.add(currentField)

        // State
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.STATE,
            isMandatory = true,
            isEditable = false,
            isEnabled = true
        )

        fields.add(currentField)

        // District
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.DISTRICT,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // City / Village
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.VILLAGE_TOWN_CITY,
            isMandatory = true,
            isEditable = false,
            isEnabled = true
        )

        fields.add(currentField)

        // Address 1
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ADDRESS_1,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Address 2
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ADDRESS_2,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)
        return fields
    }

    fun getEnabledOtherInfoFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        // National Id
        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.NATIONAL_ID,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Occupations
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.OCCUPATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Social Category
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SOCIAL_CATEGORY,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Education
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EDUCATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Economic Category
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ECONOMIC_CATEGORY,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        return fields
    }

    fun getAllEnabledPatientInfoFields(): List<PatientRegistrationFields> {
        return mutableListOf<PatientRegistrationFields>().also {
            it.addAll(getEnabledPersonalInfoFields())
            it.addAll(getEnabledAddressInfoFields())
            it.addAll(getEnabledOtherInfoFields())
        }
    }

    fun isGuardianActivated(): Boolean {
        val field = StaticPatientRegistrationEnabledFieldsHelper.getAllEnabledPatientInfoFields()
        field.forEach {
            if (it.idKey == PatientRegConfigKeys.GUARDIAN_TYPE) {
                return it.isEnabled
            }
        }
        return false
    }

    fun getEnabledGeneralBaselineFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.AYUSHMAN_CARD,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.MGNREGA_CARD,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.BANK_ACCOUNT,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.PHONE_OWNERSHIP,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.FAMILY_WHATSAPP,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.MARITAL_STATUS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GENERAL_OCCUPATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GENERAL_CASTE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GENERAL_EDUCATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GENERAL_ECONOMIC_STATUS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)


        return fields
    }

    fun getEnabledMedicalBaselineFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.HB_CHECK,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.BP_CHECK,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SUGAR_CHECK,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.BP_VALUE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.DIABETES_VALUE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ARTHRITIS_VALUE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ANEMIA_VALUE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SURGERY_VALUE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SURGERY_REASON,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SMOKING_HISTORY,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SMOKING_RATE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SMOKING_DURATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SMOKING_FREQUENCY,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.CHEW_TOBACCO,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ALCOHOL_HISTORY,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ALCOHOL_RATE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ALCOHOL_DURATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ALCOHOL_FREQUENCY,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.TAKING_ANY_MEDICATION_FOR_ANEMIA,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )
        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.HAVE_YOU_SEEN_TO_HW_IN_PAST_ONE_YEAR_FOR_ANEMIA,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )
        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.REASON_FOR_NOT_TAKING_ANEMIA_MEDICATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )
        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.OTHER_REASON_FOR_NOT_TAKING_ANEMIA_MEDICATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )
        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.TAKING_ANY_MEDICATION_FOR_HYPERTENSION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )
        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.HAVE_YOU_SEEN_TO_HW_IN_PAST_ONE_YEAR_FOR_HYPERTENSION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )
        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.REASON_FOR_NOT_TAKING_HYPERTENSION_MEDICATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )
        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.HYPERTENSION_VALUE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        return fields
    }

    fun getEnabledOtherBaselineFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.HEAD_OF_HOUSEHOLD,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.RATION_CARD_CHECK,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ECONOMIC_STATUS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.OTHER_BASELINE_RELIGION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.TOTAL_HOUSEHOLD_MEMBERS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.USUAL_HOUSEHOLD_MEMBERS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.NUMBER_OF_SMARTPHONES,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.NUMBER_OF_FEATURE_PHONES,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.NUMBER_OF_EARNING_MEMBERS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ELECTRICITY_CHECK,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.LOAD_SHEDDING_HOURS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.LOAD_SHEDDING_DAYS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.WATER_CHECK,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.WATER_AVAILABILITY_HOURS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.WATER_AVAILABILITY_DAYS,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SOURCE_OF_WATER,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SAFEGUARD_WATER,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SAFEGUARD_WATER,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.DISTANCE_FROM_WATER,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.TOILET_FACILITY,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.HOUSE_STRUCTURE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.CULTIVABLE_LAND,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.CULTIVABLE_LAND_VALUE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.AVERAGE_INCOME,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.FUEL_TYPE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SOURCE_OF_LIGHT,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.HANDWASH_PRACTICES,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EKAL_SERVICE_CHECK,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.RELATION_WITH_HOUSEHOLD,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        return fields
    }

}
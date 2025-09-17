package org.intelehealth.app.utilities

import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.hbb20.CountryCodePicker
import org.intelehealth.app.ui.baseline_survey.config.GeneralBaselineConfig
import org.intelehealth.app.ui.baseline_survey.config.MedicalBaselineConfig
import org.intelehealth.app.ui.baseline_survey.config.OtherBaselineConfig
import org.intelehealth.app.ui.patient.config.AddressInfoConfig
import org.intelehealth.app.ui.patient.config.OtherInfoConfig
import org.intelehealth.app.ui.patient.config.PersonalInfoConfig
import org.intelehealth.config.room.entity.PatientRegistrationFields
import java.lang.StringBuilder

/**
 * Created by Tanvir Hasan on 30-04-2024 : 12-31.
 * Email: mhasan@intelehealth.org
 */
object PatientRegFieldsUtils {
    fun configField(
        isEditMode: Boolean,
        field: PatientRegistrationFields,
        layoutView: View,
        fieldViewPrimary: View?,
        fieldViewSecondary: View?,
        titleTv: TextView?
    ) {
        layoutView.visibility = if (field.isEnabled) View.VISIBLE else View.GONE

        //if existing title has * then removing it first
        var titleStr = titleTv?.text
        titleStr?.let {
            if (it.contains('*')) {
                titleStr = it.toString().replace("*", "")
            }
        }
        //setting asterisk for mandatory fields
        if (field.isMandatory) {
            titleTv?.text = StringBuilder().append(titleStr).append(" *")
        } else {
            titleTv?.text = titleStr
        }

        //view updating while edit mode is false
        if (isEditMode && !field.isEditable) {
            //disabling radio group child here
            if (fieldViewPrimary is RadioGroup) {
                for (i in 0 until fieldViewPrimary.childCount) {
                    val child: View = fieldViewPrimary.getChildAt(i)
                    val radioButton = child as RadioButton
                    radioButton.isClickable = false
                }
            } else {
                fieldViewPrimary?.isEnabled = false
                //some fields has secondary fields, ex: phone num has country picker
                //this type of view are handling here
                fieldViewSecondary?.let {
                    when (fieldViewSecondary) {
                        is CountryCodePicker -> {
                            fieldViewSecondary.setCcpClickable(false)
                        }

                        is Button -> {
                            fieldViewSecondary.setVisibility(View.GONE)
                        }

                        is TextView -> {
                            fieldViewSecondary.setVisibility(View.GONE)
                        }

                        else -> {
                            fieldViewSecondary.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    /**
     * checking enable status of each field
     */
    fun getFieldEnableStatus(
        patientRegistrationFields: List<PatientRegistrationFields>,
        fieldType: String
    ): Boolean {
        if (patientRegistrationFields.isEmpty()) return false
        patientRegistrationFields.find { it.idKey == fieldType }?.let {
            return it.isEnabled
        }
        return false
    }

    /**
     * checking mandatory status of each field
     */
    fun getFieldMandatoryStatus(
        patientRegistrationFields: List<PatientRegistrationFields>,
        fieldType: String
    ): Boolean {
        if (patientRegistrationFields.isEmpty()) return false
        patientRegistrationFields.find { it.idKey == fieldType }?.let {
            return it.isMandatory
        }
        return false
    }

    @JvmStatic
    fun buildPatientPersonalInfoConfig(patientRegistrationFields: List<PatientRegistrationFields>): PersonalInfoConfig {
        return PersonalInfoConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.FIRST_NAME -> firstName = it
                    PatientRegConfigKeys.MIDDLE_NAME -> middleName = it
                    PatientRegConfigKeys.LAST_NAME -> lastName = it
                    PatientRegConfigKeys.DOB -> dob = it
                    PatientRegConfigKeys.AGE -> age = it
                    PatientRegConfigKeys.PHONE_NUM -> phone = it
                    PatientRegConfigKeys.PROFILE_PHOTO -> profilePic = it
                    PatientRegConfigKeys.GENDER -> gender = it
                    PatientRegConfigKeys.GUARDIAN_NAME -> guardianName = it
                    PatientRegConfigKeys.GUARDIAN_TYPE -> guardianType = it
                    PatientRegConfigKeys.EM_CONTACT_NAME -> emergencyContactName = it
                    PatientRegConfigKeys.EM_CONTACT_NUMBER -> emergencyContactNumber = it
                    PatientRegConfigKeys.EM_CONTACT_TYPE -> emergencyContactType = it
                }
            }
        }
    }

    @JvmStatic
    fun buildPatientAddressInfoConfig(patientRegistrationFields: List<PatientRegistrationFields>): AddressInfoConfig {
        return AddressInfoConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.POSTAL_CODE -> postalCode = it
                    PatientRegConfigKeys.COUNTRY -> country = it
                    PatientRegConfigKeys.STATE -> state = it
                    PatientRegConfigKeys.DISTRICT -> district = it
                    PatientRegConfigKeys.VILLAGE_TOWN_CITY -> cityVillage = it
                    PatientRegConfigKeys.ADDRESS_1 -> address1 = it
                    PatientRegConfigKeys.ADDRESS_2 -> address2 = it
                }
            }
        }
    }

    @JvmStatic
    fun buildPatientOtherInfoConfig(patientRegistrationFields: List<PatientRegistrationFields>): OtherInfoConfig {
        return OtherInfoConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.NATIONAL_ID -> nationalId = it
                    PatientRegConfigKeys.OCCUPATION -> occuptions = it
                    PatientRegConfigKeys.SOCIAL_CATEGORY -> socialCategory = it
                    PatientRegConfigKeys.EDUCATION -> education = it
                    PatientRegConfigKeys.ECONOMIC_CATEGORY -> economicCategory = it
                }
            }
        }
    }


    @JvmStatic
    fun buildGeneralBaselineConfig(patientRegistrationFields: List<PatientRegistrationFields>): GeneralBaselineConfig {
        return GeneralBaselineConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.AYUSHMAN_CARD -> ayushmanCard = it
                    PatientRegConfigKeys.MGNREGA_CARD -> mgnrega = it
                    PatientRegConfigKeys.BANK_ACCOUNT -> bankAccount = it
                    PatientRegConfigKeys.PHONE_OWNERSHIP -> phoneOwnership = it
                    PatientRegConfigKeys.FAMILY_WHATSAPP -> familyWhatsapp = it
                    PatientRegConfigKeys.MARITAL_STATUS -> maritalStatus = it
                    PatientRegConfigKeys.GENERAL_OCCUPATION -> occupation = it
                    PatientRegConfigKeys.GENERAL_CASTE -> caste = it
                    PatientRegConfigKeys.GENERAL_EDUCATION -> education = it
                    PatientRegConfigKeys.GENERAL_ECONOMIC_STATUS -> economicStatus = it
                    PatientRegConfigKeys.SELF_FAMILY_WHATSAPP_NUMBER -> selfOrFamilyWhatsappNumber = it
                    PatientRegConfigKeys.CAN_EKAL_SEND_WHATSAPP_MESSAGE -> canEkalSendFreeWhatsAppMessageForVisitSummary = it
                }
            }
        }
    }


    @JvmStatic
    fun buildMedicalBaselineConfig(patientRegistrationFields: List<PatientRegistrationFields>): MedicalBaselineConfig {
        return MedicalBaselineConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.HB_CHECK -> hbCheck = it
                    PatientRegConfigKeys.BP_CHECK -> bpCheck = it
                    PatientRegConfigKeys.SUGAR_CHECK -> sugarCheck = it
                    PatientRegConfigKeys.BP_VALUE -> bpValue = it
                    PatientRegConfigKeys.DIABETES_VALUE -> diabetesValue = it
                    PatientRegConfigKeys.ARTHRITIS_VALUE -> arthritisValue = it
                    PatientRegConfigKeys.ANEMIA_VALUE -> anemiaValue = it
                    PatientRegConfigKeys.SURGERY_VALUE -> surgeryValue = it
                    PatientRegConfigKeys.SURGERY_REASON -> surgeryReason = it
                    PatientRegConfigKeys.SMOKING_HISTORY -> smokingHistory = it
                    PatientRegConfigKeys.SMOKING_RATE -> smokingRate = it
                    PatientRegConfigKeys.SMOKING_DURATION -> smokingDuration = it
                    PatientRegConfigKeys.SMOKING_FREQUENCY -> smokingFrequency = it
                    PatientRegConfigKeys.CHEW_TOBACCO -> chewTobacco = it
                    PatientRegConfigKeys.ALCOHOL_HISTORY -> alcoholHistory = it
                    PatientRegConfigKeys.ALCOHOL_RATE -> alcoholRate = it
                    PatientRegConfigKeys.ALCOHOL_DURATION -> alcoholDuration = it
                    PatientRegConfigKeys.ALCOHOL_FREQUENCY -> alcoholFrequency = it
                    PatientRegConfigKeys.TAKING_ANY_MEDICATION_FOR_ANEMIA -> takingAnyMedicationForAnemia = it
                    PatientRegConfigKeys.HAVE_YOU_SEEN_TO_HW_IN_PAST_ONE_YEAR_FOR_ANEMIA -> haveYouSeenToHWinPastOneYearForAnemia = it
                    PatientRegConfigKeys.REASON_FOR_NOT_TAKING_ANEMIA_MEDICATION -> reasonForNotTakingAnemiaMedication = it
                    PatientRegConfigKeys.OTHER_REASON_FOR_NOT_TAKING_ANEMIA_MEDICATION -> otherRreasonForNotTakingAnemiaMedication = it
                    PatientRegConfigKeys.TAKING_ANY_MEDICATION_FOR_BP -> takingAnyMedicationForBP = it
                    PatientRegConfigKeys.HAVE_YOU_SEEN_TO_HW_IN_PAST_ONE_YEAR_FOR_BP -> haveYouSeenToHWinPastOneYearForBP = it
                    PatientRegConfigKeys.REASON_FOR_NOT_TAKING_BP_MEDICATION -> reasonForNotTakingBPMedication = it
                }
            }
        }
    }


    @JvmStatic
    fun buildOtherBaselineConfig(patientRegistrationFields: List<PatientRegistrationFields>): OtherBaselineConfig {
        return OtherBaselineConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.HEAD_OF_HOUSEHOLD -> headOfHousehold = it
                    PatientRegConfigKeys.RATION_CARD_CHECK -> rationCardCheck = it
                    PatientRegConfigKeys.ECONOMIC_STATUS -> economicStatus = it
                    PatientRegConfigKeys.OTHER_BASELINE_RELIGION -> religion = it
                    PatientRegConfigKeys.TOTAL_HOUSEHOLD_MEMBERS -> totalHouseholdMembers = it
                    PatientRegConfigKeys.USUAL_HOUSEHOLD_MEMBERS -> usualHouseholdMembers = it
                    PatientRegConfigKeys.NUMBER_OF_SMARTPHONES -> numberOfSmartphones = it
                    PatientRegConfigKeys.NUMBER_OF_FEATURE_PHONES -> numberOfFeaturePhones = it
                    PatientRegConfigKeys.NUMBER_OF_EARNING_MEMBERS -> numberOfEarningMembers = it
                    PatientRegConfigKeys.ELECTRICITY_CHECK -> electricityCheck = it
                    PatientRegConfigKeys.LOAD_SHEDDING_HOURS -> loadSheddingHours = it
                    PatientRegConfigKeys.LOAD_SHEDDING_DAYS -> loadSheddingDays = it
                    PatientRegConfigKeys.WATER_CHECK -> waterCheck = it
                    PatientRegConfigKeys.WATER_AVAILABILITY_DAYS -> waterAvailabilityHours = it
                    PatientRegConfigKeys.WATER_AVAILABILITY_HOURS -> waterAvailabilityDays = it
                    PatientRegConfigKeys.SOURCE_OF_WATER -> sourceOfWater = it
                    PatientRegConfigKeys.SAFEGUARD_WATER -> safeguardWater = it
                    PatientRegConfigKeys.DISTANCE_FROM_WATER -> distanceFromWater = it
                    PatientRegConfigKeys.TOILET_FACILITY -> toiletFacility = it
                    PatientRegConfigKeys.HOUSE_STRUCTURE -> houseStructure = it
                    PatientRegConfigKeys.CULTIVABLE_LAND -> cultivableLand = it
                    PatientRegConfigKeys.CULTIVABLE_LAND_VALUE -> cultivableLandValue = it
                    PatientRegConfigKeys.AVERAGE_INCOME -> averageIncome = it
                    PatientRegConfigKeys.FUEL_TYPE -> fuelType = it
                    PatientRegConfigKeys.SOURCE_OF_LIGHT -> sourceOfLight = it
                    PatientRegConfigKeys.HANDWASH_PRACTICES -> handWashPractices = it
                    PatientRegConfigKeys.EKAL_SERVICE_CHECK -> ekalServiceCheck = it
                    PatientRegConfigKeys.RELATION_WITH_HOUSEHOLD -> relationWithHousehold = it
                }
            }
        }
    }
}
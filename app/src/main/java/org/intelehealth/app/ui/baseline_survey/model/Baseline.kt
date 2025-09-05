package org.intelehealth.app.ui.baseline_survey.model

import org.intelehealth.app.R
import org.intelehealth.config.room.entity.PatientRegistrationFields

data class Baseline(

    // General
    var occupation: String = "",
    var caste: String = "",
    var education: String = "",
    var ayushmanCard: String = "",
    var mgnregaCard: String = "",
    var bankAccount: String = "",
    var phoneOwnership: String = "",
    var familyWhatsApp: String = "",
    var martialStatus: String = "",

    // Medical
    var hbCheck: String = "",
    var bpCheck: String = "",
    var sugarCheck: String = "",
    var bpValue: String = "",
    var diabetesValue: String = "",
    var arthritisValue: String = "",
    var anemiaValue: String = "",
    var hypertensionValue: String = "",
    var surgeryValue: String = "",
    var surgeryReason: String = "",
    var smokingHistory: String = "",
    var smokingRate: String = "",
    var smokingDuration: String = "",
    var smokingFrequency: String = "",
    var chewTobacco: String = "",
    var alcoholHistory: String = "",
    var alcoholRate: String = "",
    var alcoholDuration: String = "",
    var alcoholFrequency: String = "",
    var takingAnyMedicationForBP: String = "",
    var haveYouSeenToHWinPastOneYearForBP: String = "",
    var reasonForNotTakingBPMedication: String = "",
    var otherReasonForNotTakingBPMedication: String = "",
    var takingAnyMedicationForDiabetes: String = "",
    var haveYouSeenToHWinPastOneYearForDiabetes: String = "",
    var reasonForNotTakingDiabetesMedication: String = "",
    var otherReasonForNotTakingDiabetesMedication: String = "",
    var takingAnyMedicationForAnemia: String = "",
    var haveYouSeenToHWinPastOneYearForAnemia: String = "",
    var reasonForNotTakingAnemiaMedication: String = "",
    var otherReasonForNotTakingAnemiaMedication: String = "",
    var takingAnyMedicationForHypertension: String = "",
    var haveYouSeenToHWinPastOneYearForHypertension: String = "",
    var reasonForNotTakingHypertensionMedication: String = "",
    var otherReasonForNotTakingHypertensionMedication: String = "",
    // Other
    var headOfHousehold: String = "",
    var rationCardCheck: String = "",
    var economicStatus: String = "",
    var religion: String = "",
    var totalHouseholdMembers: String = "",
    var usualHouseholdMembers: String = "",
    var numberOfSmartphones: String = "",
    var numberOfFeaturePhones: String = "",
    var numberOfEarningMembers: String = "",
    var electricityCheck: String = "",
    var waterCheck: String = "",
    var loadSheddingHours: String = "",
    var loadSheddingDays: String = "",
    var sourceOfWater: String = "",
    var waterAvailabilityHours: String = "",
    var waterAvailabilityDays: String = "",
    var safeguardWater: String = "",
    var distanceFromWater: String = "",
    var toiletFacility: String = "",
    var houseStructure: String = "",
    var cultivableLand: String = "",
    var cultivableLandValue: String = "",
    var averageIncome: String = "",
    var fuelType: String = "",
    var sourceOfLight: String = "",
    var handWashPractices: String = "",
    var ekalServiceCheck: String = "",
    var relationWithHousehold: String = "",
) {
    fun setOptionalFieldsInOtherWithHyphen() {
        this.rationCardCheck = "-"
        this.economicStatus = "-"
        this.religion = "-"
        this.totalHouseholdMembers = "-"
        this.usualHouseholdMembers = "-"
        this.numberOfSmartphones = "-"
        this.numberOfFeaturePhones = "-"
        this.numberOfEarningMembers = "-"
        this.electricityCheck = "-"
        this.waterCheck = "-"
        this.loadSheddingHours = "-"
        this.loadSheddingDays = "-"
        this.sourceOfWater = "-"
        this.waterAvailabilityHours = "-"
        this.waterAvailabilityDays = "-"
        this.safeguardWater = "-"
        this.distanceFromWater = "-"
        this.toiletFacility = "-"
        this.houseStructure = "-"
        this.cultivableLand = "-"
        this.cultivableLandValue = "-"
        this.averageIncome = "-"
        this.fuelType = "-"
        this.sourceOfLight = "-"
        this.handWashPractices = "-"
        this.ekalServiceCheck = "-"
    }

    var occupationArrayConstants: Int = R.array.occupation
    var casteArrayConstants: Int = R.array.caste
    var educationArrayConstants: Int = R.array.education
    var phoneOwnershipArrayConstants: Int = R.array.phone_ownership

    var hbCheckArrayConstants: Int = R.array.hb_check
    var bpCheckArrayConstants: Int = R.array.bp_check
    var sugarCheckArrayConstants: Int = R.array.sugar_check

    var religionArrayConstants: Int = R.array.baseline_religion
    var economicStatusArrayConstants: Int = R.array.economic
    var toiletFacilityArrayConstants: Int = R.array.baseline_toilet_facilities
    var houseStructureArrayConstants: Int = R.array.baseline_house_structure
    var cultivableLandArrayConstants: Int = R.array.baseline_cultivable_land

    var reasonForNotTakingMedicationValues: Int = R.array.reason_for_not_taking_bp_medication

}

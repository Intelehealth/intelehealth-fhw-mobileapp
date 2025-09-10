
package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PatientAttributesDTO implements Serializable {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("person_attribute_type_uuid")
    @Expose
    private String personAttributeTypeUuid;
    @SerializedName("patientuuid")
    @Expose
    private String patientuuid;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPersonAttributeTypeUuid() {
        return personAttributeTypeUuid;
    }

    public void setPersonAttributeTypeUuid(String personAttributeTypeUuid) {
        this.personAttributeTypeUuid = personAttributeTypeUuid;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    @Override
    public String toString() {
        return "PatientAttributesDTO{" +
                "uuid='" + uuid + '\'' +
                ", value='" + value + '\'' +
                ", personAttributeTypeUuid='" + personAttributeTypeUuid + '\'' +
                ", patientuuid='" + patientuuid + '\'' +
                '}';
    }

    public enum Column {
        TELEPHONE("Telephone Number"),
        PROVIDER_ID("providerUUID"),
        SWD("Son/wife/daughter"),
        NATIONAL_ID("NationalID"),
        HOUSEHOLD_ID("householdID"),
        PROFILE_IMG_TIMESTAMP("ProfileImageTimestamp"),
        CREATED_DATE("createdDate"),

        OCCUPATION("occupation"),
        CASTE("Caste"),
        EDUCATION("Education Level"),
        AYUSHMAN_CARD_STATUS("ayushmanCardStatus"),
        MGNREGA_CARD_STATUS("mgnregaCardStatus"),
        BANK_ACCOUNT("Bank Account"),
        MOBILE_PHONE_TYPE("Mobile Phone Type"),
        USE_WHATSAPP("Use WhatsApp"),
        MARTIAL_STATUS("martialStatus"),

        HB_CHECKED("hbChecked"),
        BP_CHECKED("bpChecked"),
        SUGAR_CHECKED("sugarChecked"),
        OTHER_MEDICAL_HISTORY("otherMedicalHistory"),
        SMOKING_STATUS("smokingStatus"),
        TOBACCO_STATUS("TobaccoStatus"),
        ALCOHOL_CONSUMPTION_STATUS("alcoholConsumptionStatus"),

        HOH_RELATIONSHIP("hohRelationship"),
        RATION_CARD("rationCardStatus"),
        ECONOMIC_STATUS("Economic Status"),
        RELIGION("religion"),
        TOTAL_FAMILY_MEMBERS("Total Family Members"),
        TOTAl_FAMILY_MEMBERS_STAYING("Total Family Members Staying"),
        NUMBER_OF_SMARTPHONES("numberOfSmartphones"),
        NUMBER_OF_FEATURE_PHONES("numberOfFeaturePhones"),
        NUMBER_OF_EARNING_MEMBERS("numberOfEarningMembers"),
        ELECTRICITY_STATUS("electricityStatus"),
        LOAD_SHEDDING_HOURS_PER_DAY("loadSheddingHoursPerDay"),
        LOAD_SHEDDING_DAYS_PER_WEEK("loadSheddingDaysPerWeek"),
        RUNNING_WATER_AVAILABILITY("runningWaterAvailability"),
        WATER_SUPPLY_AVAILABILITY_HOURS_PER_DAY("waterSupplyAvailabilityHoursPerDay"),
        WATER_SUPPLY_AVAILABILITY_DAYS_PER_WEEK("waterSupplyAvailabilityDaysPerWeek"),
        DRINKING_WATER_SOURCE("Drinking Water Source"),
        SAFE_DRINKING_WATER("Safe Drinking Water"),
        TIME_DRINKING_WATER_SOURCE("Time Drinking Water Source"),
        TOILET_FACILITY("Toilet Facility"),
        HOUSE_STRUCTURE("House Structure"),
        FAMILY_CULTIVABLE_LAND("Family Cultivable Land"),
        AVERAGE_ANNUAL_HOUSEHOLD_INCOME("averageAnnualHouseholdIncome"),
        COOKING_FUEL("cookingFuel"),
        HOUSEHOLD_LIGHTING("householdLighting"),
        SOAP_HAND_WASHING_OCCASION("soapHandWashingOccasion"),
        TAKE_OUR_SERVICE("TakeOurService");
        public final String value;

        Column(String value) {
            this.value = value;
        }
    }
}
package org.intelehealth.helpline.models;

public class Patient {
    private String uuid;
    private String openmrs_id;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String date_of_birth; // ISO 8601
    private String phone_number;
    private String address1;
    private String address2;
    private String city_village;
    private String state_province;
    private String postal_code;
    private String country; // ISO 3166-1 alpha-2
    private String gender;
    private String patient_photo;
    private String sdw;
    private String economic_status;
    private String education_level;
    private String caste;
    private String occupation;
    private String bank_account;
    private String mobile_type;
    private String whatsapp_mobile;
    private String no_of_family_members;
    private String no_of_family_currently_live;
    private String source_of_water;
    private String water_safe;
    private String time_travel_water;
    private String water_availability;
    private String toilet_facility;
    private String structure_house;
    private String hectars_land;
    private String emergency;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOpenmrs_id() {
        return openmrs_id;
    }

    public void setOpenmrs_id(String openmrs_id) {
        this.openmrs_id = openmrs_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity_village() {
        return city_village;
    }

    public void setCity_village(String city_village) {
        this.city_village = city_village;
    }

    public String getState_province() {
        return state_province;
    }

    public void setState_province(String state_province) {
        this.state_province = state_province;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPatient_photo() {
        return patient_photo;
    }

    public void setPatient_photo(String patient_photo) {
        this.patient_photo = patient_photo;
    }

    public String getSdw() {
        return sdw;
    }

    public void setSdw(String sdw) {
        this.sdw = sdw;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getEconomic_status() {
        return economic_status;
    }

    public void setEconomic_status(String economic_status) {
        this.economic_status = economic_status;
    }

    public String getEducation_level() {
        return education_level;
    }

    public void setEducation_level(String education_level) {
        this.education_level = education_level;
    }

    public String getCaste() {
        return caste;
    }

    public void setCaste(String caste) {
        this.caste = caste;
    }

    public String getBank_account() {
        return bank_account;
    }

    public void setBank_account(String bank_account) {
        this.bank_account = bank_account;
    }

    public String getMobile_type() {
        return mobile_type;
    }

    public void setMobile_type(String mobile_type) {
        this.mobile_type = mobile_type;
    }

    public String getWhatsapp_mobile() {
        return whatsapp_mobile;
    }

    public void setWhatsapp_mobile(String whatsapp_mobile) {
        this.whatsapp_mobile = whatsapp_mobile;
    }

    public String getNo_of_family_members() {
        return no_of_family_members;
    }

    public void setNo_of_family_members(String no_of_family_members) {
        this.no_of_family_members = no_of_family_members;
    }

    public String getNo_of_family_currently_live() {
        return no_of_family_currently_live;
    }

    public void setNo_of_family_currently_live(String no_of_family_currently_live) {
        this.no_of_family_currently_live = no_of_family_currently_live;
    }

    public String getSource_of_water() {
        return source_of_water;
    }

    public void setSource_of_water(String source_of_water) {
        this.source_of_water = source_of_water;
    }

    public String getWater_safe() {
        return water_safe;
    }

    public void setWater_safe(String water_safe) {
        this.water_safe = water_safe;
    }

    public String getTime_travel_water() {
        return time_travel_water;
    }

    public void setTime_travel_water(String time_travel_water) {
        this.time_travel_water = time_travel_water;
    }

    public String getWater_availability() {
        return water_availability;
    }

    public void setWater_availability(String water_availability) {
        this.water_availability = water_availability;
    }

    public String getToilet_facility() {
        return toilet_facility;
    }

    public void setToilet_facility(String toilet_facility) {
        this.toilet_facility = toilet_facility;
    }

    public String getStructure_house() {
        return structure_house;
    }

    public void setStructure_house(String structure_house) {
        this.structure_house = structure_house;
    }

    public String getHectars_land() {
        return hectars_land;
    }

    public void setHectars_land(String hectars_land) {
        this.hectars_land = hectars_land;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }
}

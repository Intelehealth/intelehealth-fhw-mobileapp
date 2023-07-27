package org.intelehealth.ezazi.models;

import com.google.gson.annotations.SerializedName;

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

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    private String city_village;
    private String state_province;
    private String postal_code;
    private String country; // ISO 3166-1 alpha-2
    private String gender;
    private String patient_photo;
    private String sdw;
    private String occupation;
    private String economic_status;
    private String education_level;
    private String caste;
    private String emergency;
    /*new*/
    private String parity;
    private String laborOnset;
    private String activeLaborDiagnosed;
    private String membraneRupturedTimestamp;
    private String riskFactors;
    private String hospitalMaternity;

    private String alternateNo;
    private String wifeDaughterOf;
    private String admissionDate;
    private String admissionTime;

    private String primaryDoctor;
    private String secondaryDoctor;
    private String eZaziRegNumber;
    String bedNumber; //new flow
    /*end*/

    private String creatorUuid;
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

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }

    public String getHospitalMaternity() {
        return hospitalMaternity;
    }

    public void setHospitalMaternity(String hospitalMaternity) {
        this.hospitalMaternity = hospitalMaternity;
    }

    public String getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getMembraneRupturedTimestamp() {
        return membraneRupturedTimestamp;
    }

    public void setMembraneRupturedTimestamp(String membraneRupturedTimestamp) {
        this.membraneRupturedTimestamp = membraneRupturedTimestamp;
    }

    public String getActiveLaborDiagnosed() {
        return activeLaborDiagnosed;
    }

    public void setActiveLaborDiagnosed(String activeLaborDiagnosed) {
        this.activeLaborDiagnosed = activeLaborDiagnosed;
    }

    public String getParity() {
        return parity;
    }

    public void setParity(String parity) {
        this.parity = parity;
    }

    public String getLaborOnset() {
        return laborOnset;
    }

    public void setLaborOnset(String laborOnset) {
        this.laborOnset = laborOnset;
    }

    public String getAdmissionTime() {
        return admissionTime;
    }

    public void setAdmissionTime(String admissionTime) {
        this.admissionTime = admissionTime;
    }

    public String getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getWifeDaughterOf() {
        return wifeDaughterOf;
    }

    public void setWifeDaughterOf(String wifeDaughterOf) {
        this.wifeDaughterOf = wifeDaughterOf;
    }

    public String getAlternateNo() {
        return alternateNo;
    }

    public void setAlternateNo(String alternateNo) {
        this.alternateNo = alternateNo;
    }

    public String getSecondaryDoctor() {
        return secondaryDoctor;
    }

    public void setSecondaryDoctor(String secondaryDoctor) {
        this.secondaryDoctor = secondaryDoctor;
    }

    public String getPrimaryDoctor() {
        return primaryDoctor;
    }

    public void setPrimaryDoctor(String primaryDoctor) {
        this.primaryDoctor = primaryDoctor;
    }

    public String geteZaziRegNumber() {
        return eZaziRegNumber;
    }

    public void seteZaziRegNumber(String eZaziRegNumber) {
        this.eZaziRegNumber = eZaziRegNumber;
    }

    public void setCreatorUuid(String creatorUuid) {
        this.creatorUuid = creatorUuid;
    }

    public String getCreatorUuid() {
        return creatorUuid;
    }
}

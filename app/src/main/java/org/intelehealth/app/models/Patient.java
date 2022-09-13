package org.intelehealth.app.models;

import org.intelehealth.app.utilities.StringUtils;

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
    private String caste;
    private String emergency;
    private String villageNameSurvey;
    private String blockSurvey;

    // Roster

    private String phoneownership;
    private String bpchecked;
    private String sugarchecked;
    private String hbtest;
    private String bmi;
    private String healthissuereported;
    private String noepisodes;
    private String primaryhealthprovider;
    private String firstlocation;
    private String referredto;
    private String modetransport;
    private String costtravel;
    private String costconsult;
    private String costmedicines;
    private String scoreexperience;
    private String timespregnant;
    private String pasttwoyrs;
    private String noOfPregnancyOutcomeTwoYrs;

    private String outcomepregnancy;
    private String childalive;
    private String yearsofpregnancy;
    private String lastmonthspregnancy;
    private String monthsofpregnancy;
    private String placedelivery;
    private String focalfacility;
    private String singlemultiplebirth;
    private String sexofbaby;
    private String agediedbaby;
    private String plannedpregnancy;
    private String highriskpregnancy;
    private String complications;

    //new syriana questions
    private String headOfHousehold;
    private String relationshiphoh;
    private String maritalstatus;
    private String patientAidType;
    private String husbandStatus;
    private String independentResidence;
    private String mainOfficial;
    private String mainTendency;
    private String conditionOfSupport;
    private String sinceChangeHappened;
    private String occupation;
    private String education_level;
    private String residentialComplexNature;
    private String linkNature;
    private String percentageOfIncome;
    private String sinceWhenMainResponsible;


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

    public String getVillageNameSurvey() {
        return villageNameSurvey;
    }

    public void setVillageNameSurvey(String villageNameSurvey) {
        this.villageNameSurvey = villageNameSurvey;
    }

    public String getBlockSurvey() {
        return blockSurvey;
    }

    public void setBlockSurvey(String blockSurvey) {
        this.blockSurvey = blockSurvey;
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
        return StringUtils.getValueForAppLanguage(occupation);
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
        return StringUtils.getValueForAppLanguage(education_level);
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

    public String getRelationshiphoh() {
        return StringUtils.getValueForAppLanguage(relationshiphoh);
    }

    public void setRelationshiphoh(String relationshiphoh) {
        this.relationshiphoh = relationshiphoh;
    }

    public String getMaritalstatus() {
        return StringUtils.getValueForAppLanguage(maritalstatus);
    }

    public void setMaritalstatus(String maritalstatus) {
        this.maritalstatus = maritalstatus;
    }

    public String getPhoneownership() {
        return phoneownership;
    }

    public void setPhoneownership(String phoneownership) {
        this.phoneownership = phoneownership;
    }

    public String getBpchecked() {
        return bpchecked;
    }

    public void setBpchecked(String bpchecked) {
        this.bpchecked = bpchecked;
    }

    public String getSugarchecked() {
        return sugarchecked;
    }

    public void setSugarchecked(String sugarchecked) {
        this.sugarchecked = sugarchecked;
    }

    public String getHbtest() {
        return hbtest;
    }

    public void setHbtest(String hbtest) {
        this.hbtest = hbtest;
    }

    public String getBmi() {
        return bmi;
    }

    public void setBmi(String bmi) {
        this.bmi = bmi;
    }

    public String getHealthissuereported() {
        return healthissuereported;
    }

    public void setHealthissuereported(String healthissuereported) {
        this.healthissuereported = healthissuereported;
    }

    public String getNoepisodes() {
        return noepisodes;
    }

    public void setNoepisodes(String noepisodes) {
        this.noepisodes = noepisodes;
    }

    public String getPrimaryhealthprovider() {
        return primaryhealthprovider;
    }

    public void setPrimaryhealthprovider(String primaryhealthprovider) {
        this.primaryhealthprovider = primaryhealthprovider;
    }

    public String getFirstlocation() {
        return firstlocation;
    }

    public void setFirstlocation(String firstlocation) {
        this.firstlocation = firstlocation;
    }

    public String getReferredto() {
        return referredto;
    }

    public void setReferredto(String referredto) {
        this.referredto = referredto;
    }

    public String getModetransport() {
        return modetransport;
    }

    public void setModetransport(String modetransport) {
        this.modetransport = modetransport;
    }

    public String getCosttravel() {
        return costtravel;
    }

    public void setCosttravel(String costtravel) {
        this.costtravel = costtravel;
    }

    public String getCostconsult() {
        return costconsult;
    }

    public void setCostconsult(String costconsult) {
        this.costconsult = costconsult;
    }

    public String getCostmedicines() {
        return costmedicines;
    }

    public void setCostmedicines(String costmedicines) {
        this.costmedicines = costmedicines;
    }

    public String getScoreexperience() {
        return scoreexperience;
    }

    public void setScoreexperience(String scoreexperience) {
        this.scoreexperience = scoreexperience;
    }

    public String getTimespregnant() {
        return timespregnant;
    }

    public void setTimespregnant(String timespregnant) {
        this.timespregnant = timespregnant;
    }

    public String getPasttwoyrs() {
        return pasttwoyrs;
    }

    public void setPasttwoyrs(String pasttwoyrs) {
        this.pasttwoyrs = pasttwoyrs;
    }

    public String getOutcomepregnancy() {
        return outcomepregnancy;
    }

    public void setOutcomepregnancy(String outcomepregnancy) {
        this.outcomepregnancy = outcomepregnancy;
    }

    public String getChildalive() {
        return childalive;
    }

    public void setChildalive(String childalive) {
        this.childalive = childalive;
    }

    public String getYearsofpregnancy() {
        return yearsofpregnancy;
    }

    public void setYearsofpregnancy(String yearsofpregnancy) {
        this.yearsofpregnancy = yearsofpregnancy;
    }

    public String getLastmonthspregnancy() {
        return lastmonthspregnancy;
    }

    public void setLastmonthspregnancy(String lastmonthspregnancy) {
        this.lastmonthspregnancy = lastmonthspregnancy;
    }

    public String getMonthsofpregnancy() {
        return monthsofpregnancy;
    }

    public void setMonthsofpregnancy(String monthsofpregnancy) {
        this.monthsofpregnancy = monthsofpregnancy;
    }

    public String getPlacedelivery() {
        return placedelivery;
    }

    public void setPlacedelivery(String placedelivery) {
        this.placedelivery = placedelivery;
    }

    public String getFocalfacility() {
        return focalfacility;
    }

    public void setFocalfacility(String focalfacility) {
        this.focalfacility = focalfacility;
    }

    public String getSinglemultiplebirth() {
        return singlemultiplebirth;
    }

    public void setSinglemultiplebirth(String singlemultiplebirth) {
        this.singlemultiplebirth = singlemultiplebirth;
    }

    public String getSexofbaby() {
        return sexofbaby;
    }

    public void setSexofbaby(String sexofbaby) {
        this.sexofbaby = sexofbaby;
    }

    public String getAgediedbaby() {
        return agediedbaby;
    }

    public void setAgediedbaby(String agediedbaby) {
        this.agediedbaby = agediedbaby;
    }

    public String getPlannedpregnancy() {
        return plannedpregnancy;
    }

    public void setPlannedpregnancy(String plannedpregnancy) {
        this.plannedpregnancy = plannedpregnancy;
    }

    public String getHighriskpregnancy() {
        return highriskpregnancy;
    }

    public void setHighriskpregnancy(String highriskpregnancy) {
        this.highriskpregnancy = highriskpregnancy;
    }

    public String getComplications() {
        return complications;
    }

    public void setComplications(String complications) {
        this.complications = complications;
    }

    public String getNoOfPregnancyOutcomeTwoYrs() {
        return noOfPregnancyOutcomeTwoYrs;
    }

    public void setNoOfPregnancyOutcomeTwoYrs(String noOfPregnancyOutcomeTwoYrs) {
        this.noOfPregnancyOutcomeTwoYrs = noOfPregnancyOutcomeTwoYrs;
    }

    public String getHeadOfHousehold() {
        return StringUtils.getValueForAppLanguage(headOfHousehold);
    }

    public void setHeadOfHousehold(String headOfHousehold) {
        this.headOfHousehold = headOfHousehold;
    }

    public String getPatientAidType() {
        return StringUtils.getValueForAppLanguage(patientAidType);
    }

    public void setPatientAidType(String patientAidType) {
        this.patientAidType = patientAidType;
    }

    public String getHusbandStatus() {
        return StringUtils.getValueForAppLanguage(husbandStatus);
    }

    public void setHusbandStatus(String husbandStatus) {
        this.husbandStatus = husbandStatus;
    }

    public String getIndependentResidence() {
        return StringUtils.getValueForAppLanguage(independentResidence);
    }

    public void setIndependentResidence(String independentResidence) {
        this.independentResidence = independentResidence;
    }

    public String getMainOfficial() {
        return StringUtils.getValueForAppLanguage(mainOfficial);
    }

    public void setMainOfficial(String mainOfficial) {
        this.mainOfficial = mainOfficial;
    }

    public String getMainTendency() {
        return StringUtils.getValueForAppLanguage(mainTendency);
    }

    public void setMainTendency(String mainTendency) {
        this.mainTendency = mainTendency;
    }

    public String getConditionOfSupport() {
        return StringUtils.getValueForAppLanguage(conditionOfSupport);
    }

    public void setConditionOfSupport(String conditionOfSupport) {
        this.conditionOfSupport = conditionOfSupport;
    }

    public String getSinceChangeHappened() {
        return StringUtils.getValueForAppLanguage(sinceChangeHappened);
    }

    public void setSinceChangeHappened(String sinceChangeHappened) {
        this.sinceChangeHappened = sinceChangeHappened;
    }

    public String getResidentialComplexNature() {
        return StringUtils.getValueForAppLanguage(residentialComplexNature);
    }

    public void setResidentialComplexNature(String residentialComplexNature) {
        this.residentialComplexNature = residentialComplexNature;
    }

    public String getLinkNature() {
        return StringUtils.getValueForAppLanguage(linkNature);
    }

    public void setLinkNature(String linkNature) {
        this.linkNature = linkNature;
    }

    public String getPercentageOfIncome() {
        return StringUtils.getValueForAppLanguage(percentageOfIncome);
    }

    public void setPercentageOfIncome(String percentageOfIncome) {
        this.percentageOfIncome = percentageOfIncome;
    }

    public String getSinceWhenMainResponsible() {
        return StringUtils.getValueForAppLanguage(sinceWhenMainResponsible);
    }

    public void setSinceWhenMainResponsible(String sinceWhenMainResponsible) {
        this.sinceWhenMainResponsible = sinceWhenMainResponsible;
    }

}

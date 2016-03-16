package edu.jhu.bme.cbid.healthassistantsclient;

/**
 * Patient information class for Gson data serialization
 */
public class Patient {

    private Integer id;
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
    private String patient_identifier1;
    private String patient_identifier2;
    private String patient_identifier3;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOpenmrsId() {
        return openmrs_id;
    }

    public void setOpenmrsId(String openmrs_id) {
        this.openmrs_id = openmrs_id;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddleName() {
        return middle_name;
    }

    public void setMiddleName(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getDateOfBirth() {
        return date_of_birth;
    }

    public void setDateOfBirth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public void setPhoneNumber(String phone_number) {
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

    public String getCityVillage() {
        return city_village;
    }

    public void setCityVillage(String city_village) {
        this.city_village = city_village;
    }

    public String getStateProvince() {
        return state_province;
    }

    public void setStateProvince(String state_province) {
        this.state_province = state_province;
    }

    public String getPostalCode() {
        return postal_code;
    }

    public void setPostalCode(String postal_code) {
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

    public String getPatientIdentifier1() {
        return patient_identifier1;
    }

    public void setPatientIdentifier1(String patient_identifier1) {
        this.patient_identifier1 = patient_identifier1;
    }

    public String getPatientIdentifier2() {
        return patient_identifier2;
    }

    public void setPatientIdentifier2(String patient_identifier2) {
        this.patient_identifier2 = patient_identifier2;
    }

    public String getPatientIdentifier3() {
        return patient_identifier3;
    }

    public void setPatientIdentifier3(String patient_identifier3) {
        this.patient_identifier3 = patient_identifier3;
    }

    @Override
    public String toString() {
        return super.toString(); // TODO
    }
}
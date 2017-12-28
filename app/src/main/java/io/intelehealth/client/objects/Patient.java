package io.intelehealth.client.objects;

/**
 * Patient information class for Gson data serialization
 */
public class Patient {

    private Integer id;
    private String openmrs_id;
    private String openmrs_patient_id;
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
    private String occupation;
    private String economic_status;
    private String education_level;
    private String caste;
    private String patient_status;
    private  String cell_no;
    private  String prison_name;
    private String commune;


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


    public String getPatientStatus(){
        return patient_status;
    }
    public void setPatientStatus(String patient_status){
        this.patient_status = patient_status;
    }


    public String getCellNo(){
        return cell_no;
    }
    public void setCellNo(String cell_no){
        this.cell_no= cell_no;
    }

    public String getCommune(){
        return commune;
    }
    public void setCommune(String commune){
        this.commune=commune;
    }


    public String getPrisonName(){
        return prison_name;
    }

    public void setPrisonName(String prison_name){
        this.prison_name = prison_name;
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

    public String getPatientPhoto() {
        return patient_photo;
    }

    public void setPatientPhoto(String patient_photo) {
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

    public String getOpenmrs_patient_id() {
        return openmrs_patient_id;
    }

    public void setOpenmrs_patient_id(String openmrs_patient_id) {
        this.openmrs_patient_id = openmrs_patient_id;
    }
}
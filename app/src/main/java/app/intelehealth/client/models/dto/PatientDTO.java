
package app.intelehealth.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PatientDTO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("openmrs_id")
    @Expose
    private String openmrsId;
    @SerializedName("firstname")
    @Expose
    private String firstname;
    @SerializedName("middlename")
    @Expose
    private String middlename;
    @SerializedName("lastname")
    @Expose
    private String lastname;
    @SerializedName("dateofbirth")
    @Expose
    private String dateofbirth;
    @SerializedName("phonenumber")
    @Expose
    private String phonenumber;
    @SerializedName("address2")
    @Expose
    private String address2;
    @SerializedName("address1")
    @Expose
    private String address1;
    @SerializedName("cityvillage")
    @Expose
    private String cityvillage;
    @SerializedName("stateprovince")
    @Expose
    private String stateprovince;
    @SerializedName("postalcode")
    @Expose
    private String postalcode;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("education")
    @Expose
    private String education;
    @SerializedName("economic")
    @Expose
    private String economic;
    @SerializedName("gender")
    @Expose
    private String gender;


    private String patientPhoto;

    private List<PatientAttributesDTO> patientAttributesDTOList;
    @SerializedName("dead")
    @Expose
    private Integer dead;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOpenmrsId() {
        return openmrsId;
    }

    public void setOpenmrsId(String openmrsId) {
        this.openmrsId = openmrsId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getCityvillage() {
        return cityvillage;
    }

    public void setCityvillage(String cityvillage) {
        this.cityvillage = cityvillage;
    }

    public String getStateprovince() {
        return stateprovince;
    }

    public void setStateprovince(String stateprovince) {
        this.stateprovince = stateprovince;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
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

    public Integer getDead() {
        return dead;
    }

    public void setDead(Integer dead) {
        this.dead = dead;
    }

    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getEconomic() {
        return economic;
    }

    public void setEconomic(String economic) {
        this.economic = economic;
    }

    public List<PatientAttributesDTO> getPatientAttributesDTOList() {
        return patientAttributesDTOList;
    }


    public void setPatientAttributesDTOList(List<PatientAttributesDTO> patientAttributesDTOList) {
        this.patientAttributesDTOList = patientAttributesDTOList;
    }

    public String getPatientPhoto() {
        return patientPhoto;
    }

    public void setPatientPhoto(String patientPhoto) {
        this.patientPhoto = patientPhoto;
    }
}
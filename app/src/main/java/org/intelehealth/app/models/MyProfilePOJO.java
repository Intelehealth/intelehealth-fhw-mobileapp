package org.intelehealth.app.models;

public class MyProfilePOJO {

    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String dateOfBirth;
    private String countryCode;
    private String phoneNumber;
    private String email;

    private String newFirstName;
    private String newMiddleName;
    private String newLastName;
    private String newGender;
    private String newDateOfBirth;
    private String newCountryCode;
    private String newPhoneNumber;
    private String newEmail;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.newFirstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
        this.newMiddleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.newLastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
        this.newGender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.newDateOfBirth = dateOfBirth;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        this.newCountryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.newPhoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.newEmail = email;
    }

    public String getNewFirstName() {
        return newFirstName;
    }

    public void setNewFirstName(String newFirstName) {
        this.newFirstName = newFirstName;
    }

    public String getNewMiddleName() {
        return newMiddleName;
    }

    public void setNewMiddleName(String newMiddleName) {
        this.newMiddleName = newMiddleName;
    }

    public String getNewLastName() {
        return newLastName;
    }

    public void setNewLastName(String newLastName) {
        this.newLastName = newLastName;
    }

    public String getNewGender() {
        return newGender;
    }

    public void setNewGender(String newGender) {
        this.newGender = newGender;
    }

    public String getNewDateOfBirth() {
        return newDateOfBirth;
    }

    public void setNewDateOfBirth(String newDateOfBirth) {
        this.newDateOfBirth = newDateOfBirth;
    }

    public String getNewCountryCode() {
        return newCountryCode;
    }

    public void setNewCountryCode(String newCountryCode) {
        this.newCountryCode = newCountryCode;
    }

    public String getNewPhoneNumber() {
        return newPhoneNumber;
    }

    public void setNewPhoneNumber(String newPhoneNumber) {
        this.newPhoneNumber = newPhoneNumber;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public boolean hasDataChanged() {
        final boolean result = false;
        if (!firstName.equalsIgnoreCase(newFirstName)) return true;
        if (!lastName.equalsIgnoreCase(newLastName)) return true;
        if (!middleName.equalsIgnoreCase(newMiddleName)) return true;
        if (!gender.equalsIgnoreCase(newGender)) return true;
        if (!dateOfBirth.equalsIgnoreCase(newDateOfBirth)) return true;
        if (!countryCode.equalsIgnoreCase(newCountryCode)) return true;
        if (!phoneNumber.equalsIgnoreCase(newPhoneNumber)) return true;
        if (!email.equalsIgnoreCase(newEmail)) return true;
        return result;
    }

    public void updateProfileDetails() {
        firstName = newFirstName;
        lastName = newLastName;
        middleName = newMiddleName;
        gender = newGender;
        dateOfBirth = newDateOfBirth;
        countryCode = newCountryCode;
        phoneNumber = newPhoneNumber;
        email = newEmail;
    }
}
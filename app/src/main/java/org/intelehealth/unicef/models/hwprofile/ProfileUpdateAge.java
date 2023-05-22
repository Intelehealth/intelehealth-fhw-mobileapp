package org.intelehealth.unicef.models.hwprofile;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateAge {
    @SerializedName("age")
    public Integer age;

    @SerializedName("birthdate")
    public String dateOfBirth;

    @SerializedName("gender")
    public String gender;

    public ProfileUpdateAge(Integer age, String dateOfBirth, String gender) {
        this.age = age;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

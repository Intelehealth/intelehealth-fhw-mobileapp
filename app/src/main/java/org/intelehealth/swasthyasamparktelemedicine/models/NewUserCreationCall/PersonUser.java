package org.intelehealth.swasthyasamparktelemedicine.models.NewUserCreationCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sagar Shimpi
 */
public class PersonUser {

    public PersonUser() {
    }

    public PersonUser(List<NameUser> names, String gender) {
        this.names = names;
        this.gender = gender;
    }

    @SerializedName("names")
    @Expose
    public List<NameUser> names = null;
    @SerializedName("gender")
    @Expose
    public String gender;


    public List<NameUser> getNames() {
        return names;
    }

    public void setNames(List<NameUser> names) {
        this.names = names;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

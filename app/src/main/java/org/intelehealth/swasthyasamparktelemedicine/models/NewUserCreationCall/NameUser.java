package org.intelehealth.swasthyasamparktelemedicine.models.NewUserCreationCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sagar Shimpi
 */
public class NameUser {

    public NameUser(String givenName, String middleName, String familyName) {
        this.givenName = givenName;
        this.middleName = middleName;
        this.familyName = familyName;
    }

    public NameUser() {
    }

    @SerializedName("givenName")
    @Expose
    public String givenName;
    @SerializedName("middleName")
    @Expose
    public String middleName;
    @SerializedName("familyName")
    @Expose
    public String familyName;


    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
}

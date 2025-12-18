package org.intelehealth.app.abdm.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateIdentifierReqBody {

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @SerializedName("identifier")
    @Expose
    private String identifier;
    @SerializedName("identifierType")
    @Expose
    private String identifierType;
    @SerializedName("location")
    @Expose
    private String location;

}

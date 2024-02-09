
package org.intelehealth.app.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Identifier {

    @SerializedName("identifierType")
    @Expose
    private String identifierType;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("preferred")
    @Expose
    private Boolean preferred;
    @SerializedName("identifier")
    @Expose
    private String identifier;

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

    public Boolean getPreferred() {
        return preferred;
    }

    public void setPreferred(Boolean preferred) {
        this.preferred = preferred;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "identifierType='" + identifierType + '\'' +
                ", location='" + location + '\'' +
                ", preferred=" + preferred +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}

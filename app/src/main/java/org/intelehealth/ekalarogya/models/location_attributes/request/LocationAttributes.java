package org.intelehealth.ekalarogya.models.location_attributes.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationAttributes {

    @SerializedName("attributeType")
    @Expose
    private String attributeType;

    @SerializedName("value")
    @Expose
    private String value;

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

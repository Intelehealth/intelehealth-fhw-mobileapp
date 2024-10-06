package org.intelehealth.app.models.locationAttributes.pull;

import com.google.gson.annotations.SerializedName;

public class PullLocationAttributesData {

    @SerializedName("name")
    private String attributeName;

    @SerializedName("uuid")
    private String attributeUuid;

    @SerializedName("value")
    private String attributeValue;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeUuid() {
        return attributeUuid;
    }

    public void setAttributeUuid(String attributeUuid) {
        this.attributeUuid = attributeUuid;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
}

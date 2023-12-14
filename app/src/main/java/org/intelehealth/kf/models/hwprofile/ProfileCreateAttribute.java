package org.intelehealth.kf.models.hwprofile;

import com.google.gson.annotations.SerializedName;

public class ProfileCreateAttribute {
    @SerializedName("value")
    public String value;

    @SerializedName("attributeType")
    public String attributeTypeUuid;

    public ProfileCreateAttribute(String value, String attributeTypeUuid) {
        this.value = value;
        this.attributeTypeUuid = attributeTypeUuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAttributeTypeUuid() {
        return attributeTypeUuid;
    }

    public void setAttributeTypeUuid(String attributeTypeUuid) {
        this.attributeTypeUuid = attributeTypeUuid;
    }
}

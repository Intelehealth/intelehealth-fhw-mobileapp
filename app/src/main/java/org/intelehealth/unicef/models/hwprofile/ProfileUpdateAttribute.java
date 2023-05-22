package org.intelehealth.unicef.models.hwprofile;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateAttribute {

    @SerializedName("value")
    public String value;

    public ProfileUpdateAttribute(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

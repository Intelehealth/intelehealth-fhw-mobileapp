
package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientAttributeTypeMasterDTO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("description")
    @Expose
    private String description;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

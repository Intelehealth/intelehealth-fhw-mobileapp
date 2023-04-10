package org.intelehealth.app.models.statewise_location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Setup_VillageModel {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("uuid")
    @Expose
    private String uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String name) {
        this.uuid = uuid;
    }

}

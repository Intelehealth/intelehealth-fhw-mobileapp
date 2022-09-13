package org.intelehealth.app.models.statewise_location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Setup_DistrictModel {

    @SerializedName("villages")
    @Expose
    private List<Setup_VillageModel> villages;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("uuid")
    @Expose
    private String uuid;

    public List<Setup_VillageModel> getVillages() {
        return villages;
    }

    public void setVillages(List<Setup_VillageModel> villages) {
        this.villages = villages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

package org.intelehealth.app.models.statewise_location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Setup_StateModel {
    @SerializedName("districts")
    @Expose
    private List<Setup_DistrictModel> districts;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("uuid")
    @Expose
    private String uuid;

    public List<Setup_DistrictModel> getDistricts() {
        return districts;
    }

    public void setDistricts(List<Setup_DistrictModel> districts) {
        this.districts = districts;
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
        this.name = uuid;
    }
}

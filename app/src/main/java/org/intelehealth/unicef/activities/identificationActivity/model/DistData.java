package org.intelehealth.unicef.activities.identificationActivity.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DistData implements Serializable {
    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("tahasil")
    private List<String> tahasilList;

    public List<String> getTahasilList() {
        return tahasilList;
    }

    public void setTahasilList(List<String> tahasilList) {
        this.tahasilList = tahasilList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

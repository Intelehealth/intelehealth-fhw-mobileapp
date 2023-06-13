package org.intelehealth.ezazi.activities.addNewPatient.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kaveri Zaware on 12-06-2023
 * email - kaveri@intelehealth.org
 **/
public class StateData implements Serializable {
    @Expose
    @SerializedName("state")
    private String state;

    @Expose
    @SerializedName("state-hi")
    private String stateHindi;

    @Expose
    @SerializedName("districts")
    private List<DistData> distDataList;

    public List<DistData> getDistDataList() {
        return distDataList;
    }

    public void setDistDataList(List<DistData> distDataList) {
        this.distDataList = distDataList;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateHindi() {
        return stateHindi;
    }

    public void setStateHindi(String stateHindi) {
        this.stateHindi = stateHindi;
    }
}
package org.intelehealth.ezazi.activities.addNewPatient.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kaveri Zaware on 12-06-2023
 * email - kaveri@intelehealth.org
 **/
public class StateDistMaster implements Serializable {
    @Expose
    @SerializedName("states")
    private List<StateData> stateDataList;

    public List<StateData> getStateDataList() {
        return stateDataList;
    }

    public void setStateDataList(List<StateData> stateDataList) {
        this.stateDataList = stateDataList;
    }

}